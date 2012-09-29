public class PingHost
{
    private Host host;
    private PingListener pingListener;

    public PingHost(Host host, PingListener pingListener)
    {
        this.host = host;
        this.pingListener = pingListener;
    }

    public Host getHost()
    {
        return host;
    }

    public PingListener getPingListener()
    {
        return pingListener;
    }
}
