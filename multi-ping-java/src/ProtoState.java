public class ProtoState
{
    private final String name;

    private ProtoState(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return name;
    }

    public static final ProtoState DOWN =
        new ProtoState("DOWN");
    public static final ProtoState UP =
        new ProtoState("UP");
    public static final ProtoState UNKNOWN =
        new ProtoState("UNKNOWN");
}
