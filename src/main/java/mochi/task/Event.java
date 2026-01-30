package mochi.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task that occurs within a specified time range.
 */
public class Event extends Task {

    /**
     * Output format used when displaying event date/time to the user.
     */
    private static final DateTimeFormatter OUTPUT_FORMAT =
            DateTimeFormatter.ofPattern("MMM d yyyy, HH:mm");

    /**
     * Save format used when persisting event date/time to disk.
     */
    private static final DateTimeFormatter SAVE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /**
     * The start date/time of the event.
     */
    protected LocalDateTime from;

    /**
     * The end date/time of the event.
     */
    protected LocalDateTime to;

    /**
     * Constructs an {@code Event} task with the given description and time range.
     *
     * @param description The task description.
     * @param from The start date/time of the event.
     * @param to The end date/time of the event.
     */
    public Event(String description, LocalDateTime from, LocalDateTime to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the user-facing string representation of this event task.
     *
     * @return A formatted string for display.
     */
    @Override
    public String toString() {
        return "[E] " + super.toString()
                + " (from: " + this.from.format(OUTPUT_FORMAT)
                + " to: " + this.to.format(OUTPUT_FORMAT) + ")";
    }

    /**
     * Returns the save-file representation of this event task.
     * The start and end date/times are stored using the save format
     * {@code yyyy-MM-dd HHmm}.
     *
     * @return A formatted string for storage.
     */
    @Override
    public String toWrite() {
        return "E | " + super.toWrite()
                + " | " + this.from.format(SAVE_FORMAT)
                + " | " + this.to.format(SAVE_FORMAT);
    }
}