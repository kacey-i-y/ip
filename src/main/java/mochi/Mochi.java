package mochi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mochi.parser.Parser;
import mochi.parser.Parser.ParsedCommand;
import mochi.storage.Storage;
import mochi.task.Task;
import mochi.task.TaskList;
import mochi.ui.Ui;

//Code has been made to have JavaDoc comments
//Code has been made to follow the proper Coding Standard

/**
 * Main application class for the Mochi CLI chatbot.
 *
 * <p>Mochi wires together:
 * <ul>
 *   <li>{@link Ui} for user interaction</li>
 *   <li>{@link Parser} for interpreting commands</li>
 *   <li>{@link TaskList} for task operations</li>
 *   <li>{@link Storage} for persistence</li>
 * </ul>
 *
 * @author Kacey Isaiah Yonathan
 */
public class Mochi {

    /** Directory name for persistence data (relative to project root). */
    private static final String DATA_DIR_NAME = "data";

    /** Save file name used by {@link Storage}. */
    private static final String SAVE_FILE_NAME = "tasks.txt";

    private final Ui ui;
    private final Storage storage;
    private final TaskList tasks;

    /**
     * Creates a Mochi instance and loads tasks from disk.
     *
     * <p>If loading fails, Mochi starts with an empty task list.
     */
    public Mochi() {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        this.ui = new Ui(consoleReader);
        this.storage = new Storage(DATA_DIR_NAME, SAVE_FILE_NAME);

        ui.showWelcome();

        TaskList loaded;
        try {
            loaded = storage.load();
        } catch (Exception e) {
            // In case your Storage.load() never throws, this still keeps Mochi robust.
            loaded = new TaskList();
        }

        this.tasks = loaded;
        ui.showLoadStatus(tasks);
        ui.showHelp();
    }

    /**
     * Runs the chatbot in a read-eval-print loop (REPL).
     */
    public void run() {
        while (true) {
            String userInput;
            try {
                userInput = ui.readCommand();
            } catch (IOException e) {
                ui.showSeparator();
                ui.showError();
                ui.showSeparator();
                break;
            }

            if (userInput == null) {
                break;
            }

            ParsedCommand parsed;
            try {
                parsed = Parser.parse(userInput);
            } catch (IllegalArgumentException e) {
                ui.showSeparator();
                ui.showError();
                ui.showSeparator();
                continue;
            }

            if (parsed.command() == Parser.Command.BYE) {
                break;
            }

            ui.showSeparator();

            boolean hasChanged = handleCommand(parsed);
            if (hasChanged) {
                saveOrWarn();
            }

            ui.showSeparator();
        }

        ui.showGoodbye();
    }

    /**
     * Saves tasks to disk and warns if saving fails.
     */
    private void saveOrWarn() {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            ui.showSaveError(e.getMessage());
        }
    }

    /**
     * Executes a parsed command against the current task list.
     *
     * @param parsed Parsed command from {@link Parser}.
     * @return {@code true} if the task list changed and should be saved.
     */
    private boolean handleCommand(ParsedCommand parsed) {
        return switch (parsed.command()) {
            case LIST -> {
                ui.showTaskList(tasks);
                yield false;
            }

            case MARK -> markTask(parsed.index(), true);
            case UNMARK -> markTask(parsed.index(), false);
            case DELETE -> deleteTask(parsed.index());

            case TODO, DEADLINE, EVENT -> {
                tasks.add(parsed.task());
                ui.showTaskAdded(parsed.task(), tasks.size());
                yield true;
            }

            case BYE -> false; // handled in run()
        };
    }

    /**
     * Marks or unmarks a task.
     *
     * @param index 0-based index.
     * @param shouldMark True to mark done, false to unmark.
     * @return {@code true} if successful.
     */
    private boolean markTask(int index, boolean shouldMark) {
        try {
            Task task = tasks.get(index);

            if (shouldMark) {
                task.mark();
            } else {
                task.unmark();
            }

            ui.showTaskMarkStatus(task, shouldMark);
            return true;
        } catch (IndexOutOfBoundsException e) {
            ui.showError();
            return false;
        }
    }

    /**
     * Deletes a task.
     *
     * @param index 0-based index.
     * @return {@code true} if successful.
     */
    private boolean deleteTask(int index) {
        try {
            Task removed = tasks.remove(index);
            ui.showTaskRemoved(removed, tasks.size());
            return true;
        } catch (IndexOutOfBoundsException e) {
            ui.showError();
            return false;
        }
    }

    /**
     * Program entry point.
     *
     * @param args Command line arguments (unused).
     */
    public static void main(String[] args) {
        new Mochi().run();
    }
}