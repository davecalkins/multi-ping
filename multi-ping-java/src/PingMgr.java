import java.util.Vector;

public class PingMgr
{
    private Vector hosts;
    private BlockingQueue tasks;

    private TaskFeeder taskFeeder;
    private Thread taskFeederThread;

    private Vector consumers;

    private final long pingInterval = 30000;
    private final long numTaskConsumers = 10;

    public PingMgr()
    {
        hosts = new Vector();
        tasks = new BlockingQueue();

        taskFeeder = new TaskFeeder();
        taskFeederThread = new Thread(taskFeeder,taskFeeder.toString());
        taskFeederThread.start();

        consumers = new Vector();
        for (int i = 0; i < numTaskConsumers; i++)
        {
            TaskConsumer taskConsumer = new TaskConsumer();
            Thread consumerThread = new Thread(taskConsumer,taskConsumer.toString());
            consumers.add(consumerThread);
            consumerThread.start();
        }
    }

    public void addHost(Host host, PingListener pingListener)
    {
        synchronized(hosts)
        {
            Vector protos = host.getProtoNames();
            for (int i = 0; i < protos.size(); i++)
            {
                String protoName = (String)protos.get(i);
                HostProto hp = host.getProto(protoName);
                hp.setTaskState(TaskState.IDLE);
                hp.setProtoState(ProtoState.UNKNOWN);
            }

            PingHost pingHost = new PingHost(host,pingListener);
            hosts.add(pingHost);
            addTasksForPingHost(pingHost);
        }

    }

    public void updateHost(Host host)
    {
        synchronized(hosts)
        {
            for (int i = 0; i < hosts.size(); i++)
            {
                PingHost h = (PingHost)hosts.get(i);
                if (h.getHost() == host)
                {
                    addTasksForPingHost(h);
                }
            }
        }
    }

    public void removeHost(Host host)
    {
        synchronized(hosts)
        {
            for (int i = 0; i < hosts.size(); i++)
            {
                PingHost h = (PingHost)hosts.get(i);
                if (h.getHost() == host)
                {
                    hosts.remove(i);
                    i--;
                }
            }
        }
    }

    private void addTasksForPingHost(PingHost phost)
    {
        Host host = phost.getHost();
        Vector protoNames = host.getProtoNames();
        for (int i = 0; i < protoNames.size(); i++)
        {
            String protoName = (String)protoNames.get(i);
            HostProto hostProto = host.getProto(protoName);
            if (hostProto.getTaskState() == TaskState.IDLE)
            {
                phost.getPingListener().updateTaskState(host,hostProto.getProtoHandler(),TaskState.WAIT);
                PingTask pingTask = new PingTask(phost,hostProto);
                tasks.put(pingTask);

                Log.log("F-" + pingTask.getSer() + ": added task: " + protoName + ", " + host.getHostName());
            }
            else
            {
                Log.log("F-n/a: ignored non-idle task, " + hostProto.getTaskState());
            }
        }
    }

    private class TaskFeeder implements Runnable
    {
        public void run()
        {
            while (true)
            {
                try { Thread.sleep(pingInterval); } catch(Exception e) {}

                try
                {
                    for (int i = 0; i < hosts.size(); i++)
                    {
                        PingHost pingHost = (PingHost)hosts.get(i);
                        addTasksForPingHost(pingHost);
                    }
                }

                catch (Exception e)
                {
                    Log.log("F: exception: " + e);
                }
            }
        }
    }

    private class TaskConsumer implements Runnable
    {
        private BlockingQueue taskQueue;
        private BlockingQueue responseQueue;

        private Thread taskConsumerChildThread;

        private final long taskTimeout = 5000;

        public TaskConsumer()
        {
            taskQueue = new BlockingQueue();
            responseQueue = new BlockingQueue();

            TaskConsumerChild taskConsumerChild = new TaskConsumerChild();
            taskConsumerChildThread = new Thread(taskConsumerChild,taskConsumerChild.toString());
            taskConsumerChildThread.start();
        }

        public void run()
        {
            while (true)
            {
                PingTask pingTask = null;

                try
                {
                    pingTask = (PingTask)tasks.get(0);
                    Log.log("P-" + pingTask.getSer() + ": sending task to child");
                    pingTask.getPingHost().getPingListener().updateTaskState(
                        pingTask.getPingHost().getHost(),
                        pingTask.getHostProto().getProtoHandler(),
                        TaskState.EXECUTE);
                    taskQueue.put(pingTask);

                    ProtoState protoState = null;
                    while (protoState == null)
                    {
                        PingTaskResponse pingTaskResponse = (PingTaskResponse)responseQueue.get(taskTimeout);
                        if (pingTaskResponse == null)
                        {
                            Log.log("P-" + pingTask.getSer() + ": timed out waiting for child response");
                            taskConsumerChildThread.interrupt();
                            protoState = ProtoState.DOWN;
                        }
                        else
                        {
                            if (pingTaskResponse.getPingTask().getSer() ==
                                pingTask.getSer())
                            {
                                protoState = pingTaskResponse.getProtoState();
                                Log.log("P-" + pingTask.getSer() + ": got response from child " + protoState.toString());
                            }
                            else
                            {
                                Log.log("P-" + pingTask.getSer() + ": ignored response for " + pingTaskResponse.getPingTask().getSer());
                            }
                        }
                    }

                    Log.log("P-" + pingTask.getSer() + ": notifying listener");
                    pingTask.getPingHost().getPingListener().pingResult(
                        pingTask.getPingHost().getHost(),
                        pingTask.getHostProto().getProtoHandler(),
                        protoState);
                    pingTask.getPingHost().getPingListener().updateTaskState(
                        pingTask.getPingHost().getHost(),
                        pingTask.getHostProto().getProtoHandler(),
                        TaskState.IDLE);
                    Log.log("P-" + pingTask.getSer() + ": finished notifying listener");
                }

                catch (Exception e)
                {
                    if (pingTask != null)
                    {
                        Log.log("P-" + pingTask.getSer() + ": exception " + e);
                    }
                    else
                    {
                        Log.log("P-NULL: exception " + e);
                    }
                }
            }
        }

        private class TaskConsumerChild implements Runnable
        {
            public void run()
            {
                while (true)
                {
                    PingTask pingTask = null;

                    try
                    {
                        pingTask = (PingTask)taskQueue.get(0);
                        Log.log("C-" + pingTask.getSer() + ": got task from parent, executing");

                        ProtoState protoState = ProtoState.DOWN;

                        try
                        {
                            protoState = pingTask.getHostProto().getProtoHandler().execute(
                                pingTask.getPingHost().getHost().getHostName(),
                                pingTask.getHostProto().getProtoArgs());
                            Log.log("C-" + pingTask.getSer() + ": executed");
                        }

                        catch (Exception e)
                        {
                            Log.log("C-" + pingTask.getSer() + ": exception");
                        }

                        PingTaskResponse pingTaskResponse = new PingTaskResponse(
                            pingTask,
                            protoState);

                        responseQueue.put(pingTaskResponse);
                        Log.log("C-" + pingTask.getSer() + ": sent result to parent");
                    }

                    catch (Exception e)
                    {
                        Log.log("C-" + pingTask.getSer() + ": exception");
                    }
                }
            }
        }
    }
}
