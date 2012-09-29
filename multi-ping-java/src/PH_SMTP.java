import java.util.Vector;
import java.net.*;
import java.io.*;

public class PH_SMTP implements ProtoHandler
{
    private String protoName;
    private ProtoArgs defaultArgs;

    public PH_SMTP()
    {
        protoName = "SMTP";

        defaultArgs = new ProtoArgs();
        defaultArgs.setArgValue("Port","25");
    }

    public ProtoState execute(String hostName, ProtoArgs protoArgs)
        throws InterruptedException
    {
        ProtoState result = ProtoState.DOWN;

        Log.log("SMTP: starting for " + hostName);

        try
        {
            int port;

            try
            {
                port = Integer.decode(protoArgs.getArgValue("Port")).intValue();
            }

            catch (NumberFormatException e)
            {
                port = 80;
            }

            NetStreamConnection nsc = new NetStreamConnection();

            try
            {
                if (nsc.open(hostName,port))
                {
                    PrintWriter out = nsc.getWriter();
                    BufferedReader in = nsc.getReader();

                    String s;

                    s = in.readLine();
                    if ((s != null) &&
                        (s.length() > 0) &&
                        (s.indexOf("220") >= 0))
                    {
                        out.println("EHLO\n");
                        s = in.readLine();

                        if ((s != null) &&
                            (s.length() > 0) &&
                            (s.indexOf("250") >= 0))
                        {
                            out.println("QUIT\n");
                            result = ProtoState.UP;
                        }
                    }

                    if (Thread.interrupted())
                    {
                        throw new InterruptedException();
                    }
                }
            }

            finally
            {
                nsc.close();
            }
        }

        catch (Exception e)
        {
           Log.log("SMTP: exception (" + hostName + "): " + e);
        }

        Log.log("SMTP: finished for " + hostName + " with " + result.toString());
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
