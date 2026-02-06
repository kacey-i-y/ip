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
 * Use {@link #getResponse(String)} to process user input and get a reply.
 */
public class Mochi {

    /** Directory name for persistence data (relative to current working directory). */
    private static final String DATA_DIR_NAME = "data";

    /** Save file name used by {@link Storage}. */
    private static final String SAVE_FILE_NAME = "tasks.txt";

    private final Ui ui;
    private final Storage storage;
    private final TaskList tasks;

    private boolean shouldExit;

    /**
     * Creates a Mochi instance and loads tasks from disk.
     * If loading fails, Mochi starts with an empty task list.
     */
    public Mochi() {
        this.ui = new Ui();
        this.storage = new Storage(DATA_DIR_NAME, SAVE_FILE_NAME);
        this.tasks = storage.load();
        this.shouldExit = false;
    }

    /**
     * Returns whether the last processed command requested an exit.
     *
     * @return True if the app should exit.
     */
    public boolean shouldExit() {
        return shouldExit;
    }

    /**
     * Processes user input and returns Mochi's response.
     *
     * @param input Raw user input.
     * @return Response string for display.
     */
    public String getResponse(String input) {
        ParsedCommand parsed;
        try {
            parsed = Parser.parse(input);
        } catch (IllegalArgumentException e) {
            return ui.getGenericError();
        }

        String response = handleCommand(parsed);

        if (shouldSave(parsed.command())) {
            try {
                storage.save(tasks);
            } catch (IOException e) {
                response = response + "\n" + ui.getSaveError(e.getMessage());
            }
        }

        return response;
    }

    private boolean shouldSave(Parser.Command command) {
        return command == Parser.Command.MARK
                || command == Parser.Command.UNMARK
                || command == Parser.Command.DELETE
                || command == Parser.Command.TODO
                || command == Parser.Command.DEADLINE
                || command == Parser.Command.EVENT;
    }

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

            case BYE -> {
                shouldExit = true;
                yield ui.getGoodbye();
            }
        };
    }

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
            return "Invalid task number.";
        }
    }

    private String deleteTask(int index) {
        try {
            Task removed = tasks.remove(index);
            return ui.getTaskRemoved(removed, tasks.size());
        } catch (IndexOutOfBoundsException e) {
            return "Invalid task number.";
        }
    }
}
