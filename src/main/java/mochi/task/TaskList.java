package mochi.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the in-memory collection of {@link Task} objects.
 *
 * <p>{@code TaskList} provides basic operations used by the chatbot such as adding,
 * retrieving, removing, and marking tasks. It intentionally keeps logic minimal and
 * delegates task-specific behaviour (e.g., formatting, done state) to {@link Task}.
 *
 * <p><b>Implementation notes</b>
 * <ul>
 *   <li>Indices used by this class are 0-based.</li>
 *   <li>{@link #asUnmodifiableList()} should be preferred for read-only iteration.</li>
 *   <li>{@link #asList()} returns the underlying mutable list for persistence usage
 *       (e.g., {@link mochi.storage.Storage}) and should be used carefully.</li>
 * </ul>
 *
 * @author Kacey Isaiah Yonathan
 */
public class TaskList {

    private final ArrayList<Task> tasks;

    /**
     * Creates an empty {@code TaskList}.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Adds a task to the end of this task list.
     *
     * @param task Task to add.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    public void add(Task task) {
        this.tasks.add(task);
    }

    /**
     * Returns the task at the given 0-based index.
     *
     * @param index 0-based index of the task.
     * @return The task at {@code index}.
     * @throws IndexOutOfBoundsException If {@code index} is out of range.
     */
    public Task get(int index) {
        return this.tasks.get(index);
    }

    /**
     * Removes and returns the task at the given 0-based index.
     *
     * @param index 0-based index of the task.
     * @return The removed task.
     * @throws IndexOutOfBoundsException If {@code index} is out of range.
     */
    public Task remove(int index) {
        return this.tasks.remove(index);
    }

    /**
     * Marks the task at the given 0-based index as done.
     *
     * @param index 0-based index of the task.
     * @throws IndexOutOfBoundsException If {@code index} is out of range.
     */
    public void mark(int index) {
        this.tasks.get(index).mark();
    }

    /**
     * Unmarks the task at the given 0-based index as not done.
     *
     * @param index 0-based index of the task.
     * @throws IndexOutOfBoundsException If {@code index} is out of range.
     */
    public void unmark(int index) {
        this.tasks.get(index).unmark();
    }

    /**
     * Returns the number of tasks currently in this list.
     *
     * @return Task count.
     */
    public int size() {
        return this.tasks.size();
    }

    /**
     * Returns {@code true} if this list contains no tasks.
     *
     * @return {@code true} if empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this.tasks.isEmpty();
    }

    /**
     * Returns an unmodifiable view of the tasks for read-only iteration.
     *
     * @return Unmodifiable list view of tasks.
     */
    public List<Task> asUnmodifiableList() {
        return Collections.unmodifiableList(this.tasks);
    }

    /**
     * Returns the underlying mutable list of tasks.
     *
     * <p><b>Warning:</b> This exposes the internal representation and should be used
     * only where necessary (e.g., saving to disk). Prefer {@link #asUnmodifiableList()}
     * for normal iteration.
     *
     * @return The underlying mutable {@link ArrayList}.
     */
    public ArrayList<Task> asList() {
        return this.tasks;
    }

    /**
     * Finds and returns tasks whose descriptions contain the given keyword.
     *
     * @param keyword Keyword to search for (case-insensitive).
     * @return A TaskList containing all matching tasks.
     */
    public TaskList find(String keyword) {
        TaskList matches = new TaskList();

        for (Task task : this.tasks) {
            if (task.matchesKeyword(keyword)) {
                matches.add(task);
            }
        }

        return matches;
    }
}
