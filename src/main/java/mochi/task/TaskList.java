package mochi.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a list of tasks and provides basic operations on it.
 */
public class TaskList {

    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Adds a task to the list.
     *
     * @param task Task to add.
     */
    public void add(Task task) {
        assert task != null : "Cannot add null task";
        tasks.add(task);
    }

    /**
     * Gets a task by 0-based index.
     *
     * @param index 0-based index.
     * @return Task at the index.
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Removes a task by 0-based index.
     *
     * @param index 0-based index.
     * @return Removed task.
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns number of tasks in the list.
     *
     * @return Size of the list.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether the list is empty.
     *
     * @return True if empty, false otherwise.
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Finds tasks whose descriptions contain the given keyword (case-insensitive).
     *
     * @param keyword Keyword to search for.
     * @return A new TaskList of matching tasks.
     */
    public TaskList find(String keyword) {
        assert keyword != null : "Keyword must not be null";
        assert !keyword.isBlank() : "Keyword must not be blank";

        TaskList matches = new TaskList();
        if (keyword == null) {
            return matches;
        }

        String key = keyword.trim().toLowerCase();
        if (key.isEmpty()) {
            return matches;
        }

        for (Task t : tasks) {
            String s = t.toString().toLowerCase();
            if (s.contains(key)) {
                matches.add(t);
            }
        }
        return matches;
    }

    /**
     * Returns a formatted string of tasks grouped and sorted for display.
     *
     * Deadlines: undone first (by date), then done (by date)
     * Events: undone first (by end time), then done (by end time)
     * Todos: undone first (alphabetical), then done (alphabetical)
     *
     * @return Sorted and grouped task list as a string.
     */
    public String getSortedForDisplay() {
        List<Deadline> deadlinesUndone = new ArrayList<>();
        List<Deadline> deadlinesDone = new ArrayList<>();
        List<Event> eventsUndone = new ArrayList<>();
        List<Event> eventsDone = new ArrayList<>();
        List<Todo> todosUndone = new ArrayList<>();
        List<Todo> todosDone = new ArrayList<>();

        for (Task t : tasks) {
            if (t instanceof Deadline d) {
                if (d.isDone()) {
                    deadlinesDone.add(d);
                } else {
                    deadlinesUndone.add(d);
                }
            } else if (t instanceof Event e) {
                if (e.isDone()) {
                    eventsDone.add(e);
                } else {
                    eventsUndone.add(e);
                }
            } else if (t instanceof Todo td) {
                if (td.isDone()) {
                    todosDone.add(td);
                } else {
                    todosUndone.add(td);
                }
            }
        }

        deadlinesUndone.sort(Comparator.comparing(Deadline::getBy));
        deadlinesDone.sort(Comparator.comparing(Deadline::getBy));

        eventsUndone.sort(Comparator.comparing(Event::getTo));
        eventsDone.sort(Comparator.comparing(Event::getTo));

        Comparator<Todo> todoAlpha =
                Comparator.comparing(x -> x.getDescription().toLowerCase());
        todosUndone.sort(todoAlpha);
        todosDone.sort(todoAlpha);

        StringBuilder sb = new StringBuilder();
        sb.append("Great! Here is a sorted list of your tasks\n\n");

        sb.append("Deadlines:\n");
        appendSection(sb, deadlinesUndone, deadlinesDone);

        sb.append("\nEvents:\n");
        appendSection(sb, eventsUndone, eventsDone);

        sb.append("\nTodos:\n");
        appendSection(sb, todosUndone, todosDone);

        return sb.toString().trim();
    }

    /**
     * Appends undone tasks first, then done tasks, in numbered order.
     *
     * @param sb Output builder.
     * @param undone Undone tasks (already sorted).
     * @param done Done tasks (already sorted).
     */
    private static void appendSection(StringBuilder sb, List<? extends Task> undone,
                                      List<? extends Task> done) {
        if (undone.isEmpty() && done.isEmpty()) {
            sb.append("  (none)\n");
            return;
        }

        int i = 1;
        for (Task t : undone) {
            sb.append(i++).append(". ").append(t).append("\n");
        }
        for (Task t : done) {
            sb.append(i++).append(". ").append(t).append("\n");
        }
    }

    /**
     * Returns true if an event has ended (its end time is <= now).
     *
     * @param e Event to check.
     * @param now Current time.
     * @return True if ended, false otherwise.
     */

    private static boolean hasEnded(Event e, LocalDateTime now) {
        return !e.getTo().isAfter(now); // ended if to <= now
    }

    /**
     * Appends tasks as a 1-based numbered list using each task's toString().
     *
     * @param sb StringBuilder to append to.
     * @param list Tasks to print.
     */
    private static void appendNumberedList(StringBuilder sb, List<? extends Task> list) {
        if (list.isEmpty()) {
            sb.append("No tasks.\n");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            sb.append(i + 1).append(". ").append(list.get(i)).append("\n");
        }
    }
}
