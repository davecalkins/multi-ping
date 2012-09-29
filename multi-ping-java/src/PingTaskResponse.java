public class PingTaskResponse
{
    private PingTask pingTask;
    private ProtoState protoState;

    public PingTaskResponse(PingTask pingTask, ProtoState protoState)
    {
        this.pingTask = pingTask;
        this.protoState = protoState;
    }

    public PingTask getPingTask()
    {
        return pingTask;
    }

    public ProtoState getProtoState()
    {
        return protoState;
    }
}