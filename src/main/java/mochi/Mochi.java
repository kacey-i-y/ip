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

/**
 * Main entry point for the Mochi CLI chatbot.
 *
 * <p>Mochi orchestrates the chatbot workflow:
 * parse → execute → save, while delegating UI printing/reading to {@link Ui}.
 *
 * @author Kacey Isaiah Yonathan
 */
public class Mochi {

    /** Directory name for persistence data (relative to project root). */
    private static final String DATA_DIR_NAME = "data";

    /** Save file name used by {@link Storage}. */
    private static final String SAVE_FILE_NAME = "tasks.txt";

    /**
     * Runs the chatbot in a read-eval-print loop.
     *
     * @param args Command line arguments (unused).
     * @throws IOException If an I/O error occurs while reading user input.
     */
    public static void main(String[] args) throws IOException {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        Ui ui = new Ui(consoleReader);
        Storage storage = new Storage(DATA_DIR_NAME, SAVE_FILE_NAME);

        ui.showWelcome();

        TaskList taskList = storage.load();
        ui.showLoadStatus(taskList);

        ui.showHelp();

        while (true) {
            String userInput = ui.readCommand();
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

            boolean hasChanged = handleCommand(parsed, taskList, ui);
            if (hasChanged) {
                saveOrWarn(storage, taskList, ui);
            }

            ui.showSeparator();
        }

        ui.showGoodbye();
    }

    /**
     * Saves tasks to disk and prints a warning if saving fails.
     *
     * @param storage Storage handler used for persistence.
     * @param taskList Current task list to save.
     * @param ui UI handler for output.
     */
    private static void saveOrWarn(Storage storage, TaskList taskList, Ui ui) {
        try {
            storage.save(taskList);
        } catch (IOException e) {
            ui.showSaveError(e.getMessage());
        }
    }

    /**
     * Executes a parsed command against the given task list.
     *
     * @param parsed Parsed command from {@link Parser}.
     * @param tasks Current in-memory task list.
     * @param ui UI handler for output.
     * @return {@code true} if the task list changed and should be saved.
     */
    private static boolean handleCommand(ParsedCommand parsed, TaskList tasks, Ui ui) {
        return switch (parsed.command()) {
            case LIST -> {
                ui.showTaskList(tasks);
                yield false;
            }

            case MARK -> markTask(tasks, parsed.index(), true, ui);
            case UNMARK -> markTask(tasks, parsed.index(), false, ui);
            case DELETE -> deleteTask(tasks, parsed.index(), ui);

            case TODO, DEADLINE, EVENT -> {
                tasks.add(parsed.task());
                ui.showTaskAdded(parsed.task(), tasks.size());
                yield true;
            }

            case BYE -> false; // already handled in main loop
        };
    }

    /**
     * Marks or unmarks the task at the given 0-based index.
     *
     * @param tasks Task list.
     * @param index 0-based index of the task.
     * @param shouldMark True to mark as done, false to unmark.
     * @param ui UI handler for output.
     * @return {@code true} if successful, {@code false} if index invalid.
     */
    private static boolean markTask(TaskList tasks, int index, boolean shouldMark, Ui ui) {
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
     * Deletes the task at the given 0-based index.
     *
     * @param tasks Task list.
     * @param index 0-based index of the task.
     * @param ui UI handler for output.
     * @return {@code true} if deleted, {@code false} if index invalid.
     */
    private static boolean deleteTask(TaskList tasks, int index, Ui ui) {
        try {
            Task removed = tasks.remove(index);
            ui.showTaskRemoved(removed, tasks.size());
            return true;
        } catch (IndexOutOfBoundsException e) {
            ui.showError();
            return false;
        }
    }
}