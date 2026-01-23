public class Event extends Task {

    protected String from;
    protected String to;

    public Event(String echo, String from, String to) {
        super(echo);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + this.from + " to: " + this.to + ")";
    }
}
