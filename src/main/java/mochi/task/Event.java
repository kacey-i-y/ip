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
     * @param description The task description.
     * @param from The start time of the event.
     * @param to The end time of the event.
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the UI representation of this event task.
     *
     * @return A formatted string for display.
     */
    @Override
    public String toString() {
        return "[E] " + super.toString() + " (from: " + this.from + " to: " + this.to + ")";
    }

    /**
     * Returns the save-file representation of this event task.
     *
     * @return A formatted string for storage.
     */
    @Override
    public String toWrite() {
        return "E | " + super.toWrite() + " | " + this.from + " | " + this.to;
    }
}