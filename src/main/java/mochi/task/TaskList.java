package mochi.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a list of tasks and provides basic operations on it.
 */
public class TaskList {

    /** Stores all tasks in insertion order. */
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
     * <p>Deadlines: undone first (by date), then done (by date).
     * Events: undone first (by end time), then done (by end time).
     * Todos: undone first (alphabetical), then done (alphabetical).
     *
     * @return Sorted and grouped task list as a string.
     */
    public String getSortedForDisplay() {
        SortedBuckets buckets = bucketTasks();
        sortBuckets(buckets);
        return formatBuckets(buckets).trim();
    }

    /**
     * Groups tasks into typed buckets, split into undone and done.
     *
     * @return A {@link SortedBuckets} object containing grouped tasks.
     */
    private SortedBuckets bucketTasks() {
        SortedBuckets buckets = new SortedBuckets();

        for (Task t : tasks) {
            buckets.add(t);
        }

        return buckets;
    }

    /**
     * Sorts each bucket according to the display rules.
     *
     * @param buckets Buckets to sort.
     */
    private void sortBuckets(SortedBuckets buckets) {
        buckets.sortDeadlines();
        buckets.sortEvents();
        buckets.sortTodos();
    }

    /**
     * Formats all buckets into the final user-facing string.
     *
     * @param buckets Buckets to format.
     * @return A formatted string for display.
     */
    private String formatBuckets(SortedBuckets buckets) {
        StringBuilder sb = new StringBuilder();
        sb.append("Great! Here is a sorted list of your tasks\n\n");

        sb.append("Deadlines:\n");
        appendSection(sb, buckets.deadlinesUndone, buckets.deadlinesDone);

        sb.append("\nEvents:\n");
        appendSection(sb, buckets.eventsUndone, buckets.eventsDone);

        sb.append("\nTodos:\n");
        appendSection(sb, buckets.todosUndone, buckets.todosDone);

        return sb.toString();
    }

    /**
     * Appends undone tasks first, then done tasks, in numbered order.
     *
     * @param sb     Output builder.
     * @param undone Undone tasks (already sorted).
     * @param done   Done tasks (already sorted).
     */
    private static void appendSection(
            StringBuilder sb,
            List<? extends Task> undone,
            List<? extends Task> done
    ) {
        if (undone.isEmpty() && done.isEmpty()) {
            sb.append("  (none)\n");
            return;
        }

        int i = 1;
        i = appendTasks(sb, undone, i);
        appendTasks(sb, done, i);
    }

    /**
     * Appends each task to the StringBuilder in numbered format.
     *
     * @param sb    Output builder.
     * @param tasks Tasks to append.
     * @param start Starting number to use.
     * @return Next number after the last appended task.
     */
    private static int appendTasks(StringBuilder sb, List<? extends Task> tasks, int start) {
        int i = start;
        for (Task t : tasks) {
            sb.append(i++).append(". ").append(t).append("\n");
        }
        return i;
    }

    /**
     * Container object holding grouped task lists for sorting and display.
     */
    private static class SortedBuckets {

        /** Undone deadline tasks. */
        private final List<Deadline> deadlinesUndone = new ArrayList<>();

        /** Done deadline tasks. */
        private final List<Deadline> deadlinesDone = new ArrayList<>();

        /** Undone event tasks. */
        private final List<Event> eventsUndone = new ArrayList<>();

        /** Done event tasks. */
        private final List<Event> eventsDone = new ArrayList<>();

        /** Undone todo tasks. */
        private final List<Todo> todosUndone = new ArrayList<>();

        /** Done todo tasks. */
        private final List<Todo> todosDone = new ArrayList<>();

        /**
         * Adds a task to the correct bucket based on its type and done status.
         *
         * @param task Task to bucket.
         */
        private void add(Task task) {
            if (task instanceof Deadline d) {
                addDeadline(d);
                return;
            }

            if (task instanceof Event e) {
                addEvent(e);
                return;
            }

            if (task instanceof Todo td) {
                addTodo(td);
            }
        }

        /**
         * Adds a deadline task into either the done or undone list.
         *
         * @param d Deadline to add.
         */
        private void addDeadline(Deadline d) {
            if (d.isDone()) {
                deadlinesDone.add(d);
            } else {
                deadlinesUndone.add(d);
            }
        }

        /**
         * Adds an event task into either the done or undone list.
         *
         * @param e Event to add.
         */
        private void addEvent(Event e) {
            if (e.isDone()) {
                eventsDone.add(e);
            } else {
                eventsUndone.add(e);
            }
        }

        /**
         * Adds a todo task into either the done or undone list.
         *
         * @param td Todo to add.
         */
        private void addTodo(Todo td) {
            if (td.isDone()) {
                todosDone.add(td);
            } else {
                todosUndone.add(td);
            }
        }

        /**
         * Sorts deadline buckets by deadline date.
         */
        private void sortDeadlines() {
            deadlinesUndone.sort(Comparator.comparing(Deadline::getBy));
            deadlinesDone.sort(Comparator.comparing(Deadline::getBy));
        }

        /**
         * Sorts event buckets by end time.
         */
        private void sortEvents() {
            eventsUndone.sort(Comparator.comparing(Event::getTo));
            eventsDone.sort(Comparator.comparing(Event::getTo));
        }

        /**
         * Sorts todo buckets alphabetically (case-insensitive).
         */
        private void sortTodos() {
            Comparator<Todo> todoAlpha =
                    Comparator.comparing(x -> x.getDescription().toLowerCase());
            todosUndone.sort(todoAlpha);
            todosDone.sort(todoAlpha);
        }
    }
}
