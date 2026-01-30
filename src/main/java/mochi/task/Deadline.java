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
     * @param echo The task description.
     * @param by The deadline (e.g. date/time).
     */
    public Deadline(String echo, String by) {
        super(echo);
        this.by = by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + this.by + ")";
    }

    @Override
    public String toWrite() {
        return "D | " + super.toWrite() + " | " + this.by;
    }
}
