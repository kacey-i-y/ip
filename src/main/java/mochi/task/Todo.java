package mochi.task;

/**
 * Represents a to-do task (a task without any associated date/time).
 *
 * <p>Storage format:
 * {@code T | <doneFlag> | <description>}
 *
 * @author Kacey Isaiah Yonathan
 */
public class Todo extends Task {

    /**
     * Creates a to-do task with the given description.
     *
     * @param description The task description.
     * @throws IllegalArgumentException If {@code description} is {@code null} or blank.
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Returns the UI representation of this to-do task.
     *
     * @return Display string in the format {@code [T] [X] description} or {@code [T] [ ] description}.
     */
    @Override
    public String toString() {
        return "[T] " + super.toString();
    }

    /**
     * Returns the save-file representation of this to-do task.
     *
     * @return Storage string in the format {@code T | <doneFlag> | <description>}.
     */
    @Override
    public String toWrite() {
        return "T | " + super.toWrite();
    }
}