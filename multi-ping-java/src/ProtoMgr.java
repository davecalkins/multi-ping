import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

public class ProtoMgr
{
    private static Hashtable protoHandlers;
    private static Vector protoHandlerNames;
    private static boolean initialized = false;

    private static void initialize()
    {
        if (!initialized)
        {
            protoHandlers = new Hashtable();
            protoHandlerNames = new Vector();

            ProtoHandler ph;

            ph = new PH_ICMP_Echo();
            protoHandlers.put(ph.getProtoName(), ph);
            protoHandlerNames.add(ph.getProtoName());

            ph = new PH_HTTP();
            protoHandlers.put(ph.getProtoName(), ph);
            protoHandlerNames.add(ph.getProtoName());

            ph = new PH_SMTP();
            protoHandlers.put(ph.getProtoName(), ph);
            protoHandlerNames.add(ph.getProtoName());

            ph = new PH_POP();
            protoHandlers.put(ph.getProtoName(), ph);
            protoHandlerNames.add(ph.getProtoName());

            initialized = true;
        }
    }

    public static Vector getProtoNames()
    {
        initialize();

        return protoHandlerNames;
    }

    public static ProtoHandler getProtoHandler(String protoName)
    {
        initialize();

        return (ProtoHandler)protoHandlers.get(protoName);
    }
}
