package mochi.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an event task that occurs within a specific time range.
 *
 * <p>UI format:
 * {@code [E] [X] description (from: MMM d yyyy, HH:mm to: MMM d yyyy, HH:mm)}
 *
 * <p>Storage format:
 * {@code E | <doneFlag> | <description> | <fromDateTime> | <toDateTime>}
 * where {@code <fromDateTime>} and {@code <toDateTime>} are stored using
 * {@code yyyy-MM-dd HHmm}.
 *
 * <p>Invariant: {@code to} must be strictly after {@code from}.
 *
 * @author Kacey Isaiah Yonathan
 */
public class Event extends Task {

    /** Output format used when displaying event date/time to the user. */
    private static final DateTimeFormatter OUTPUT_FORMAT =
            DateTimeFormatter.ofPattern("MMM d yyyy, HH:mm");

    /** Save format used when persisting event date/time to disk. */
    private static final DateTimeFormatter SAVE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /** The start date/time of the event. */
    private final LocalDateTime from;

    /** The end date/time of the event. Must be strictly after {@link #from}. */
    private final LocalDateTime to;

    /**
     * Constructs an {@code Event} task with the given description and time range.
     *
     * @param description The task description.
     * @param from The start date/time of the event.
     * @param to The end date/time of the event.
     * @throws IllegalArgumentException If {@code description} is {@code null} or blank,
     *                                  if {@code from} or {@code to} is {@code null},
     *                                  or if {@code to} is not strictly after {@code from}.
     */
    public Event(String description, LocalDateTime from, LocalDateTime to) {
        super(description);

        if (from == null || to == null) {
            throw new IllegalArgumentException("Event /from and /to cannot be null");
        }
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("Event /to must be after /from");
        }

        this.from = from;
        this.to = to;
    }

    /**
     * Returns the start date/time of this event.
     *
     * @return Start date/time.
     */
    public LocalDateTime getFrom() {
        return from;
    }

    /**
     * Returns the end date/time of this event.
     *
     * @return End date/time.
     */
    public LocalDateTime getTo() {
        return to;
    }

    /**
     * Returns the user-facing string representation of this event task.
     *
     * @return Display string in the format
     *         {@code [E] [X] description (from: MMM d yyyy, HH:mm to: MMM d yyyy, HH:mm)}.
     */
    @Override
    public String toString() {
        return "[E] " + super.toString()
                + " (from: " + from.format(OUTPUT_FORMAT)
                + " to: " + to.format(OUTPUT_FORMAT) + ")";
    }

    /**
     * Returns the save-file representation of this event task.
     *
     * @return Storage string in the format
     *         {@code E | <doneFlag> | <description> | yyyy-MM-dd HHmm | yyyy-MM-dd HHmm}.
     */
    @Override
    public String toWrite() {
        return "E | " + super.toWrite()
                + " | " + from.format(SAVE_FORMAT)
                + " | " + to.format(SAVE_FORMAT);
    }
}
