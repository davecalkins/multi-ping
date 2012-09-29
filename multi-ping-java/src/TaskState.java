public class TaskState
{
    private final String name;

    private TaskState(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return name;
    }

    public static final TaskState IDLE =
        new TaskState("IDLE");
    public static final TaskState WAIT =
        new TaskState("WAIT");
    public static final TaskState EXECUTE =
        new TaskState("EXECUTE");
}
