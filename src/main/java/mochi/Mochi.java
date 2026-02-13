package mochi;

import java.io.IOException;

import mochi.parser.Parser;
import mochi.parser.Parser.ParsedCommand;
import mochi.storage.Storage;
import mochi.task.Task;
import mochi.task.TaskList;
import mochi.ui.Ui;

/**
 * Main logic class for Mochi.
 *
 * <p>This class is UI-agnostic: it can be used by both a CLI and a GUI.
 * Call {@link #getResponse(String)} to process a user command and get a
 * user-facing reply string.</p>
 */
public class Mochi {

    /** Directory name for persistence data (relative to current working directory). */
    private static final String DATA_DIR_NAME = "data";

    /** Save file name used by {@link Storage}. */
    private static final String SAVE_FILE_NAME = "tasks.txt";

    /**
     * Produces user-facing messages (e.g. error messages, formatted task lists).
     */
    private final Ui ui;

    /**
     * Persists tasks to disk and loads them back.
     */
    private final Storage storage;

    /**
     * In-memory list of tasks currently managed by Mochi.
     */
    private final TaskList tasks;

    /**
     * Set to {@code true} after the user issues the exit command.
     * The GUI can check this via {@link #shouldExit()}.
     */
    private boolean shouldExit;

    private final String startupMessage;

    /**
     * Creates a Mochi instance and loads tasks from disk.
     *
     * <p>If the save file does not exist, Mochi starts with an empty task list.
     * If the save file contains corrupted lines, they are skipped by {@link Storage}.</p>
     */
    public Mochi() {
        this.ui = new Ui();
        this.storage = new Storage(DATA_DIR_NAME, SAVE_FILE_NAME);
        this.tasks = storage.load();
        this.shouldExit = false;

        String loadMsg = storage.getLastLoadMessage();
        this.startupMessage = (loadMsg == null)
                ? ui.getWelcome()
                : ui.getWelcome() + "\n" + loadMsg;
    }

    /**
     * Returns whether the last processed command requested the app to exit.
     *
     * @return {@code true} if an exit command (e.g. {@code bye}) has been processed.
     */
    public boolean shouldExit() {
        return shouldExit;
    }

    /**
     * Processes user input and returns Mochi's response string.
     *
     * @param input Raw user input from the user.
     * @return The response to be shown to the user.
     */
    public String getResponse(String input) {
        ParsedCommand parsed = parseOrNull(input);
        if (parsed == null) {
            return formatError(ui.getGenericError());
        }

        String response = handleCommand(parsed);
        response = appendSaveErrorIfAny(response, parsed.command());

        return response;
    }

    /**
     * Parses the user input into a {@link ParsedCommand}.
     *
     * @param input Raw user input.
     * @return ParsedCommand if parsing succeeds, otherwise {@code null}.
     */
    private ParsedCommand parseOrNull(String input) {
        try {
            return Parser.parse(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Formats a message as an error so the GUI can detect it easily.
     *
     * @param message The error message to show.
     * @return Error message with prefix.
     */
    private String formatError(String message) {
        return "Error: " + message;
    }

    /**
     * Saves the task list if needed and appends a save error message if saving fails.
     *
     * @param currentResponse Existing response message.
     * @param command The parsed command.
     * @return Response with save error appended (if any).
     */
    private String appendSaveErrorIfAny(String currentResponse, Parser.Command command) {
        if (!shouldSave(command)) {
            return currentResponse;
        }

        try {
            storage.save(tasks);
            return currentResponse;
        } catch (IOException e) {
            return currentResponse + "\n" + ui.getSaveError(e.getMessage());
        }
    }

    /**
     * Determines whether a given command should trigger a save to disk.
     *
     * <p>Only commands that mutate the task list (add, delete, mark/unmark)
     * require persistence.</p>
     *
     * @param command The parsed command type.
     * @return {@code true} if the command modifies the stored task list.
     */
    private boolean shouldSave(Parser.Command command) {
        return command == Parser.Command.MARK
                || command == Parser.Command.UNMARK
                || command == Parser.Command.DELETE
                || command == Parser.Command.TODO
                || command == Parser.Command.DEADLINE
                || command == Parser.Command.EVENT;
    }

    /**
     * Executes a parsed command and returns the user-facing response.
     *
     * @param parsed Parsed command returned by {@link Parser#parse(String)}.
     * @return Response string for the UI to display.
     */
    private String handleCommand(ParsedCommand parsed) {
        return switch (parsed.command()) {
        case LIST -> ui.getTaskList(tasks);
        case MARK -> markTask(parsed.index(), true);
        case UNMARK -> markTask(parsed.index(), false);
        case DELETE -> deleteTask(parsed.index());

        case TODO, DEADLINE, EVENT -> {
            Task task = parsed.task();
            tasks.add(task);
            yield ui.getTaskAdded(task, tasks.size());
        }

        case FIND -> ui.getFindResults(tasks.find(parsed.keyword()));
        case HELP -> ui.getHelpMessage();

        case BYE -> {
            shouldExit = true;
            yield ui.getGoodbye();
        }
        };
    }

    /**
     * Marks or unmarks a task at the given index.
     *
     * @param index 0-based index of the task in the list.
     * @param shouldMark {@code true} to mark done, {@code false} to unmark.
     * @return A user-facing status message.
     */
    private String markTask(int index, boolean shouldMark) {
        try {
            Task task = tasks.get(index);
            if (shouldMark) {
                task.mark();
            } else {
                task.unmark();
            }
            return ui.getTaskMarkStatus(task, shouldMark);
        } catch (IndexOutOfBoundsException e) {
            return "Error: Invalid task number.";
        }
    }

    /**
     * Deletes a task at the given index.
     *
     * @param index 0-based index of the task in the list.
     * @return A user-facing status message.
     */
    private String deleteTask(int index) {
        try {
            Task removed = tasks.remove(index);
            return ui.getTaskRemoved(removed, tasks.size());
        } catch (IndexOutOfBoundsException e) {
            return "Error: Invalid task number.";
        }
    }

    /**
     * Returns the message to show when the app starts (welcome + optional load warning).
     *
     * @return Startup message.
     */
    public String getStartupMessage() {
        return startupMessage;
    }
}
