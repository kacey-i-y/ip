package mochi.ui;

import java.io.BufferedReader;
import java.io.IOException;

import mochi.task.Task;
import mochi.task.TaskList;

/**
 * Handles all user interaction for the Mochi chatbot.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Read user input from stdin</li>
 *   <li>Print messages (welcome/help/errors/status updates)</li>
 *   <li>Print task-related feedback (list/add/delete/mark)</li>
 * </ul>
 *
 * @author Kacey Isaiah Yonathan
 */
public class Ui {

    /** Horizontal separator line used in CLI output. */
    private static final String SEPARATOR =
            "____________________________________________________________";

    /** Input source for reading user commands. */
    private final BufferedReader reader;

    /**
     * Creates a UI that reads from the given {@link BufferedReader}.
     *
     * @param reader Reader used to read user input.
     */
    public Ui(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Reads the next line of user input.
     *
     * @return The next input line, or {@code null} if EOF is reached.
     * @throws IOException If reading fails.
     */
    public String readCommand() throws IOException {
        return reader.readLine();
    }

    /**
     * Prints a simple load status message after reading tasks from disk.
     *
     * @param taskList Loaded task list.
     */
    public void showLoadStatus(TaskList taskList) {
        if (taskList.isEmpty()) {
            System.out.println("No tasks loaded from disk.");
            return;
        }
        System.out.println("Loaded " + taskList.size() + " task(s) from disk.");
    }

    /**
     * Prints all tasks with 1-based indexing.
     *
     * @param tasks Task list to print.
     */
    public void showTaskList(TaskList tasks) {
        if (tasks.isEmpty()) {
            System.out.println("Your task list is empty.");
            return;
        }

        System.out.println("The following tasks are listed in the task list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ". " + tasks.get(i));
        }
    }

    /**
     * Prints a message confirming a task has been added.
     *
     * @param task Newly added task.
     * @param size Current size of task list.
     */
    public void showTaskAdded(Task task, int size) {
        System.out.println("Added: " + task);
        System.out.println("Currently, we have " + size + " task(s) on the list.");
    }

    /**
     * Prints a message confirming a task has been removed.
     *
     * @param removed Removed task.
     * @param size Current size of task list.
     */
    public void showTaskRemoved(Task removed, int size) {
        System.out.println("Removed: " + removed);
        System.out.println("Currently, we have " + size + " task(s) on the list.");
    }

    /**
     * Prints a message confirming a task has been marked or unmarked.
     *
     * @param task Task that was updated.
     * @param isMarked True if marked as done, false if unmarked.
     */
    public void showTaskMarkStatus(Task task, boolean isMarked) {
        if (isMarked) {
            System.out.println("Marked as done: " + task);
            return;
        }
        System.out.println("Marked as not done: " + task);
    }

    /**
     * Prints the ASCII logo and greeting message.
     */
    public void showWelcome() {
        String logo =
                "             .@@@@@@@@@@@@@@@.\n"
                        + "          .@@@@@@@@@@@@@@@@@@@@@.\n"
                        + "        .@@@@@@@@@@@@@@@@@@@@@@@@@.\n"
                        + "       @@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                        + "      @@@@@@   (* )     ( *)   @@@@@@\n"
                        + "      @@@@@@       .  ^  .       @@@@@@\n"
                        + "       @@@@@@@      ' w '      @@@@@@@\n"
                        + "        @@@@@@@@.           .@@@@@@@@\n"
                        + "          \"@@@@@@@@@@@@@@@@@@@@@@@\"" + "\n"
                        + "      __  __   ____    ____  _   _  ___\n"
                        + "     |  \\/  | / __ \\  / ___|| | | ||_ _|\n"
                        + "     | |\\/| || |  | || |    | |_| | | |\n"
                        + "     | |  | || |__| || |___ |  _  | | |\n"
                        + "     |_|  |_| \\____/  \\____||_| |_||___|\n";

        showSeparator();
        System.out.print(logo);
        showSeparator();
        System.out.println("Hello I'm MOCHI, your cutest personal chatbot (˶˃ ᵕ ˂˶)");
        System.out.println("Is there anything I can help you with today?");
        showSeparator();
    }

    /**
     * Prints the list of accepted command formats.
     */
    public void showHelp() {
        System.out.println("Please follow the following formats:");
        System.out.println("todo <task>");
        System.out.println("deadline <task> /by yyyy-MM-dd");
        System.out.println("event <task> /from yyyy-MM-dd HHmm /to yyyy-MM-dd HHmm");
        System.out.println("mark <number>");
        System.out.println("unmark <number>");
        System.out.println("delete <number>");
        System.out.println("list");
        System.out.println("bye");
        showSeparator();
    }

    /**
     * Prints a generic error message for invalid commands.
     */
    public void showError() {
        System.out.println("Error, please follow the specified formats:");
        System.out.println("todo <task>");
        System.out.println("deadline <task> /by yyyy-MM-dd");
        System.out.println("event <task> /from yyyy-MM-dd HHmm /to yyyy-MM-dd HHmm");
        System.out.println("mark <number>");
        System.out.println("unmark <number>");
        System.out.println("delete <number>");
        System.out.println("list");
        System.out.println("find");
        System.out.println("bye");
    }

    /**
     * Prints a warning when saving fails.
     *
     * @param message Error message from exception.
     */
    public void showSaveError(String message) {
        System.out.println("Error saving tasks: " + message);
    }

    /**
     * Prints the goodbye message.
     */
    public void showGoodbye() {
        showSeparator();
        System.out.println("Bye-bye, Please come back soon ദ്ദി(˵ •̀ ᴗ - ˵ ) ✧!!!");
        showSeparator();
    }

    /**
     * Prints a horizontal separator.
     */
    public void showSeparator() {
        System.out.println(SEPARATOR);
    }

    /**
     * Prints tasks that match a keyword search.
     *
     * @param matches The list of matching tasks.
     */
    public void showFindResults(TaskList matches) {
        if (matches.isEmpty()) {
            System.out.println("No matching tasks found.");
            return;
        }

        System.out.println("Here are the matching tasks in your list:");
        for (int i = 0; i < matches.size(); i++) {
            System.out.println((i + 1) + ". " + matches.get(i));
        }
    }
}
