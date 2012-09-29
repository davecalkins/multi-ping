public class PingTask
{
    private PingHost pingHost;
    private HostProto hostProto;
    private static long nextSer = 1;
    private long ser;

    public PingTask(PingHost pingHost, HostProto hostProto)
    {
        this.pingHost = pingHost;
        this.hostProto = hostProto;

        synchronized(this)
        {
            ser = nextSer++;
        }
    }

    public PingHost getPingHost()
    {
        return pingHost;
    }

    public HostProto getHostProto()
    {
        return hostProto;
    }

    public long getSer()
    {
        return ser;
    }
}
