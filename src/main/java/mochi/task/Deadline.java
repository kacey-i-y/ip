package mochi.task;

/**
 * Represents a task that has to be completed by a specific deadline.
 */
public class Deadline extends Task {

    /** The deadline for the task. */
    protected String by;

    /**
     * Creates a deadline task.
     *
     * @param description The task description.
     * @param by The deadline (e.g. date/time).
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the UI representation of this deadline task.
     *
     * @return A formatted string for display.
     */
    @Override
    public String toString() {
        return "[D] " + super.toString() + " (by: " + this.by + ")";
    }

    /**
     * Returns the save-file representation of this deadline task.
     *
     * @return A formatted string for storage.
     */
    @Override
    public String toWrite() {
        return "D | " + super.toWrite() + " | " + this.by;
    }
}