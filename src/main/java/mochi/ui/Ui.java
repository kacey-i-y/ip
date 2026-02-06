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

    private static final String LINE = "____________________________________________________________";

    /**
     * Returns the divider line.
     *
     * @return Divider line.
     */
    public String getSeparator() {
        return LINE;
    }

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
}
