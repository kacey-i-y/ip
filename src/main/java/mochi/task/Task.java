package mochi.task;

/**
 * Represents a task with a description and completion status.
 */
public abstract class Task {

    /** Indicates whether the task is marked as done. */
    protected boolean isDone;

    /** The task description as entered by the user. */
    protected String description;

    /**
     * Creates a task.
     *
     * @param description The task description.
     */
    public Task(String description) {
        this.isDone = false;
        this.description = description;
    }

    /**
     * Marks the task as completed.
     */
    public void mark() {
        this.isDone = true;
    }

    /**
     * Marks the task as not completed.
     */
    public void unmark() {
        this.isDone = false;
    }

    /**
     * Returns the string representation of the task in the list format.
     *
     * @return A formatted string representing the task.
     */
    @Override
    public String toString() {
        return "[" + (this.isDone ? "X" : " ") + "] " + this.description;
    }

    /**
     * Returns the task data in a format suitable for writing to a save file.
     *
     * @return A formatted string representing the task for storage.
     */
    public String toWrite() {
        return (this.isDone ? "1 " : "0 ") + "| " + this.description;
    }
}