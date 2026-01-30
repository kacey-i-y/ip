package mochi.task;

/**
 * Represents a task that happens within a time range.
 */
public class Event extends Task {

    /** The start time of the event. */
    protected String from;

    /** The end time of the event. */
    protected String to;

    /**
     * Creates an event task.
     *
     * @param echo The task description.
     * @param from The start time of the event.
     * @param to The end time of the event.
     */
    public Event(String echo, String from, String to) {
        super(echo);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + this.from + " to: " + this.to + ")";
    }

    @Override
    public String toWrite() {
        return "D | " + super.toWrite() + " | " + this.from + " | " + this.to;
    }
}
