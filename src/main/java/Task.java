public class Task {
    private boolean marked;
    private String echo;

    public Task(String echo) {
        this.marked = false;
        this.echo = echo;
    }

    public void mark() {
        this.marked = true;
    }

    public void unmark() {
        this.marked = false;
    }

    @Override
    public String toString() {
        return "[" + (!this.marked ? " " : "X") + "] " + this.echo;
    }
}
