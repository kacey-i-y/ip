package mochi.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task that must be completed by a specific deadline date.
 */
public class Deadline extends Task {

    /**
     * Output format used when displaying the deadline date to the user.
     */
    private static final DateTimeFormatter OUTPUT_FORMAT =
            DateTimeFormatter.ofPattern("MMM d yyyy");

    /**
     * The deadline date for this task.
     */
    private final LocalDate by;

    /**
     * Constructs a {@code Deadline} task with the given description and deadline date.
     *
     * @param description The task description.
     * @param by The deadline date.
     */
    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the user-facing string representation of this deadline task.
     *
     * @return A formatted string for display.
     */
    @Override
    public String toString() {
        return "[D] " + super.toString() + " (by: " + this.by.format(OUTPUT_FORMAT) + ")";
    }

    /**
     * Returns the save-file representation of this deadline task.
     * The deadline date is stored in ISO-8601 format (yyyy-MM-dd).
     *
     * @return A formatted string for storage.
     */
    @Override
    public String toWrite() {
        return "D | " + super.toWrite() + " | " + this.by;
    }
}
