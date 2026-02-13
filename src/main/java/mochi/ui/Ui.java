package mochi.ui;

import mochi.task.Task;
import mochi.task.TaskList;

/**
 * Provides user-facing messages for Mochi.
 *
 * <p>In GUI mode, these strings can be displayed in the chat window.
 * In CLI mode, they can be printed directly.
 */
public class Ui {

    /**
     * Returns the welcome message.
     *
     * @return Welcome message.
     */
    public String getWelcome() {
        return "Hello! I'm Mochi.\nWhat can I do for you?";
    }

    /**
     * Returns the goodbye message.
     *
     * @return Goodbye message.
     */
    public String getGoodbye() {
        return "Bye. Hope to see you again soon!";
    }

    /**
     * Returns a generic error message.
     *
     * @return Error message.
     */
    public String getGenericError() {
        return "Oops, I don't understand that. Try 'help' or check your format.";
    }

    /**
     * Returns a message after adding a task.
     *
     * @param task Added task.
     * @param size New size of task list.
     * @return Message string.
     */
    public String getTaskAdded(Task task, int size) {
        return "Got it. I've added this task:\n  " + task
                + "\nNow you have " + size + " tasks in the list.";
    }

    /**
     * Returns a message after removing a task.
     *
     * @param task Removed task.
     * @param size New size of task list.
     * @return Message string.
     */
    public String getTaskRemoved(Task task, int size) {
        return "Noted. I've removed this task:\n  " + task
                + "\nNow you have " + size + " tasks in the list.";
    }

    /**
     * Returns a message after marking or unmarking a task.
     *
     * @param task Task updated.
     * @param isMark True if marking done, false if unmarking.
     * @return Message string.
     */
    public String getTaskMarkStatus(Task task, boolean isMark) {
        String prefix = isMark ? "Nice! I've marked this task as done:" : "OK, I've marked this task as not done yet:";
        return prefix + "\n  " + task;
    }

    /**
     * Returns a formatted list of tasks.
     *
     * @param tasks Task list.
     * @return Message string.
     */
    public String getTaskList(TaskList tasks) {
        if (tasks.isEmpty()) {
            return "Your list is empty for now.";
        }

        StringBuilder sb = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append("\n").append(i + 1).append(". ").append(tasks.get(i));
        }
        return sb.toString();
    }

    /**
     * Returns a formatted find result list.
     *
     * @param matches Matching tasks.
     * @return Message string.
     */
    public String getFindResults(TaskList matches) {
        if (matches.isEmpty()) {
            return "No matching tasks found.";
        }

        StringBuilder sb = new StringBuilder("Here are the matching tasks in your list:");
        for (int i = 0; i < matches.size(); i++) {
            sb.append("\n").append(i + 1).append(". ").append(matches.get(i));
        }
        return sb.toString();
    }

    /**
     * Returns a message when saving fails.
     *
     * @param message IOException message.
     * @return Message string.
     */
    public String getSaveError(String message) {
        return "Warning: failed to save tasks (" + message + ").";
    }

    /**
     * Returns a message when input is help
     *
     * @return Message string.
     */
    public String getHelpMessage() {
        return """
            Here are the commands you can use:

            list
              Shows all tasks

            todo <description>
              Example: todo read book

            deadline <description> /by <yyyy-MM-dd>
              Example: deadline return book /by 2026-01-30

            event <description> /from <yyyy-MM-dd HHmm> /to <yyyy-MM-dd HHmm>
              Example: event meeting /from 2026-01-30 1800 /to 2026-01-30 2000

            mark <task number>
              Example: mark 2

            unmark <task number>
              Example: unmark 2

            delete <task number>
              Example: delete 2

            find <keyword>
              Example: find book

            help
              Shows this help message

            bye
              Exits the app
            """;
    }

    /**
     * Returns a message when tasks.txt file is not found
     *
     * @return Message string.
     */
    public String getMissingDataFileMessage(String path) {
        return "I couldn't find " + path + ".\n"
                + "Starting with an empty task list.\n"
                + "I'll create the file automatically when you save tasks.";
    }

    /**
     * Returns a sorted, grouped view of the task list for display.
     *
     * @param tasks Task list to sort and format.
     * @return A formatted string of sorted tasks.
     */
    public String getSortedTaskList(TaskList tasks) {
        assert tasks != null : "TaskList must not be null";
        return tasks.getSortedForDisplay();
    }
}
