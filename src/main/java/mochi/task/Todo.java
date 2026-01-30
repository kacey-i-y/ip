package mochi.task;

/**
 * Represents a task without a date/time attached to it.
 */
public class Todo extends Task {

    /**
     * Creates a to-do task.
     *
     * @param description The task description.
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Returns the UI representation of this to-do task.
     *
     * @return A formatted string for display.
     */
    @Override
    public String toString() {
        return "[T] " + super.toString();
    }

    /**
     * Returns the save-file representation of this to-do task.
     *
     * @return A formatted string for storage.
     */
    @Override
    public String toWrite() {
        return "T | " + super.toWrite();
    }
}