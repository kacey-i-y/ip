package mochi.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a deadline task that must be completed by a specific date.
 *
 * <p>UI format:
 * {@code [D] [X] description (by: MMM d yyyy)}
 *
 * <p>Storage format:
 * {@code D | <doneFlag> | <description> | <byDate>}
 * where {@code <byDate>} is stored in ISO-8601 format ({@code yyyy-MM-dd}).
 *
 * @author Kacey Isaiah Yonathan
 */
public class Deadline extends Task {

    /** Output format used when displaying the deadline date to the user. */
    private static final DateTimeFormatter OUTPUT_FORMAT =
            DateTimeFormatter.ofPattern("MMM d yyyy");

    /** The deadline date for this task. */
    private final LocalDate by;

    /**
     * Constructs a {@code Deadline} task with the given description and deadline date.
     *
     * @param description The task description.
     * @param by The deadline date.
     * @throws IllegalArgumentException If {@code description} is {@code null} or blank, or {@code by} is {@code null}.
     */
    public Deadline(String description, LocalDate by) {
        super(description);

        if (by == null) {
            throw new IllegalArgumentException("Deadline date cannot be null");
        }

        this.by = by;
    }

    /**
     * Returns the deadline date.
     *
     * @return The deadline date.
     */
    public LocalDate getBy() {
        return by;
    }

    /**
     * Returns the user-facing string representation of this deadline task.
     *
     * @return Display string in the format {@code [D] [X] description (by: MMM d yyyy)}.
     */
    @Override
    public String toString() {
        return "[D] " + super.toString() + " (by: " + by.format(OUTPUT_FORMAT) + ")";
    }

    /**
     * Returns the save-file representation of this deadline task.
     *
     * @return Storage string in the format {@code D | <doneFlag> | <description> | yyyy-MM-dd}.
     */
    @Override
    public String toWrite() {
        return "D | " + super.toWrite() + " | " + by;
    }
}