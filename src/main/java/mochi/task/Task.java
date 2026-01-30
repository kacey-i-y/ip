package mochi.task;

/**
 * Represents a task with a description and completion status.
 */
public abstract class Task {

    /** Indicates whether the task is marked as done. */
    protected boolean marked;

    /** The task description as entered by the user. */
    protected String echo;

    /**
     * Creates a task.
     *
     * @param echo The task description.
     */
    public Task(String echo) {
        this.marked = false;
        this.echo = echo;
    }

    /**
     * Marks the task as completed.
     */
    public void mark() {
        this.marked = true;
    }

    /**
     * Marks the task as not completed.
     */
    public void unmark() {
        this.marked = false;
    }

    /**
     * Returns the string representation of the task in the list format.
     *
     * @return A formatted string representing the task.
     */
    @Override
    public String toString() {
        return "[" + (!this.marked ? " " : "X") + "] " + this.echo;
    }

    public String toWrite() {
        return (this.marked ? "1 " : "0 ") + "| " + this.echo;
    }
}
