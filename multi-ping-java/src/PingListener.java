public interface PingListener
{
    public void pingResult(Host host, ProtoHandler protoHandler, ProtoState protoState);
    public void updateTaskState(Host host, ProtoHandler protoHandler, TaskState taskState);
}
