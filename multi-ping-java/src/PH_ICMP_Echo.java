import java.util.*;
import java.io.*;
import java.net.*;

public class PH_ICMP_Echo implements ProtoHandler
{
    private String protoName;
    private ProtoArgs defaultArgs;

    private final int timeout = 5000;

    public PH_ICMP_Echo()
    {
        protoName = "ICMP Echo";

        defaultArgs = new ProtoArgs();
    }

    public ProtoState execute(String hostName, ProtoArgs protoArgs)
        throws InterruptedException
    {
        ProtoState result = ProtoState.DOWN;

        Log.log("ICMP: starting for " + hostName);

        try
        {
            InetAddress addr = InetAddress.getByName(hostName);
            if (addr.isReachable(timeout))
            {
                result = ProtoState.UP;
            }
        }

        catch(IOException e)
        {
            Log.log("ICMP: exception (" + hostName + "): " + e);
        }

        Log.log("ICMP: finished for " + hostName + " with " + result.toString());

        return result;
    }

    public String getProtoName()
    {
        return protoName;
    }

    public Vector validateArgs(ProtoArgs protoArgs)
    {
        return null;
    }

    public ProtoArgs getDefaultArgs()
    {
        return defaultArgs;
    }
}
