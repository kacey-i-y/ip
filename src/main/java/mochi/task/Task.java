package mochi.task;

/**
 * Represents a generic task with a description and a completion status.
 *
 * <p>Concrete subclasses (e.g. {@code Todo}, {@code Deadline}, {@code Event}) should:
 * <ul>
 *   <li>provide their own task-type prefix when saving (e.g. {@code T | ...})</li>
 *   <li>override {@link #toWrite()} to include any additional fields</li>
 * </ul>
 *
 * @author Kacey Isaiah Yonathan
 */
public abstract class Task {

    /** Task description as entered by the user. */
    protected final String description;

    /** Indicates whether the task is marked as done. */
    protected boolean isDone;

    /**
     * Creates a task with the given description. New tasks are unmarked by default.
     *
     * @param description The task description.
     * @throws IllegalArgumentException If {@code description} is {@code null} or blank.
     */
    public Task(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be null/blank");
        }

        this.description = description.trim();
        this.isDone = false;
    }

    /**
     * Marks this task as completed.
     */
    public void mark() {
        this.isDone = true;
    }

    /**
     * Marks this task as not completed.
     */
    public void unmark() {
        this.isDone = false;
    }

    /**
     * Returns the task description.
     *
     * @return Task description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether this task is marked as done.
     *
     * @return {@code true} if done, {@code false} otherwise.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns the string representation of the task used for displaying in the list.
     *
     * @return Display string in the format {@code [X] description} or {@code [ ] description}.
     */
    @Override
    public String toString() {
        return "[" + (isDone ? "X" : " ") + "] " + description;
    }

    /**
     * Returns the task data in a format suitable for writing to the save file.
     *
     * <p>Subclasses should typically override this to prepend a type code
     * (e.g. {@code T |}, {@code D |}, {@code E |}) and append any extra fields.
     *
     * @return Storage string containing at least the done flag and description.
     */
    public String toWrite() {
        return (isDone ? "1" : "0") + " | " + description;
    }

    /**
     * Returns whether this task matches the given keyword (case-insensitive).
     *
     * @param keyword Keyword to search for.
     * @return True if the description contains the keyword.
     */
    public boolean matchesKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }

        return this.description.toLowerCase().contains(keyword.trim().toLowerCase());
    }
}
