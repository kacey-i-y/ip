public class Todo extends Task {

    public Todo(String echo) {
        super(echo);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
