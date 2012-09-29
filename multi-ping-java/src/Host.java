import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

public class Host implements Serializable
{
    private String hostName;
    private Hashtable protos;

    public Host(String hostName)
    {
        this.hostName = hostName;
        protos = new Hashtable();
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public Vector getProtoNames()
    {
        Vector result = new Vector();

        for (Enumeration e = protos.keys(); e.hasMoreElements(); )
        {
            result.add(e.nextElement());
        }

        return result;
    }

    public void removeAllProtos()
    {
        protos.clear();
    }

    public HostProto getProto(String protoName)
    {
        return (HostProto)protos.get(protoName);
    }

    public void setProto(String protoName, HostProto hostProto)
    {
        protos.put(protoName,hostProto);
    }

    public void removeProto(String protoName)
    {
        protos.remove(protoName);
    }

    public void copyFrom(Host host)
    {
        setHostName(host.getHostName());
        removeAllProtos();
        Vector protoNames = host.getProtoNames();
        for (int i = 0; i < protoNames.size(); i++)
        {
            String protoName = (String)protoNames.get(i);
            HostProto hostProto = host.getProto(protoName);
            ProtoArgs protoArgs = hostProto.getProtoArgs();
            ProtoArgs newProtoArgs = new ProtoArgs();
            Vector argNames = protoArgs.getArgNames();
            for (int j = 0; j < argNames.size(); j++)
            {
                String argName = (String)argNames.get(j);
                newProtoArgs.setArgValue(argName,protoArgs.getArgValue(argName));
            }
            HostProto newHostProto = new HostProto(hostProto.getProtoHandler(),newProtoArgs);
            setProto(protoName,newHostProto);
        }
    }
}
