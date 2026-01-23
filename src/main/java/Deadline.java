public class Deadline extends Task {

    protected String by;

    public Deadline(String echo, String by) {
        super(echo);
        this.by = by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + this.by + ")";
    }
}
