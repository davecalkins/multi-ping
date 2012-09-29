import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.net.URL;
import java.io.*;

public class MainWnd
{
    private MainWnd outer;
    private JFrame mainFrame;
    private HostsTableModel hostsTableModel;
    private ProtoStateRenderer protoStateRenderer;
    private JTable hostsTable;

    private JPopupMenu popupMenu;
    private JMenuItem addMenuItem, editMenuItem, delMenuItem;
    private int contextRow;

    private PingMgr pingMgr;

    private String getDataFile()
    {
        String cf = getClass().getName() + ".class";
        URL u = getClass().getResource(cf);
        String su = u.toString();
        String myjar = "multiping.jar";
        int idx = su.lastIndexOf(myjar);
        String result = su.substring(10,idx) + "multiping.dat";

        return result;
    }

    private void save()
    {
        try
        {
            File f = new File(getDataFile());
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            hostsTableModel.save(out);
            out.flush();
            out.close();
        }

        catch(Exception e)
        {
            System.out.println("Exception: " + e);
        }
    }

    private void load()
    {
        try
        {
            File f = new File(getDataFile());
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fis);
            hostsTableModel.load(in);
            in.close();
        }

        catch(Exception e)
        {
            System.out.println("Exception: " + e);
        }
    }

    private class HostsMouseListener extends MouseInputAdapter
    {
        private Component parent;

        public HostsMouseListener(Component parent)
        {
            this.parent = parent;
        }

        public void mouseReleased(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                Point pt = e.getPoint();

                int row = hostsTable.rowAtPoint(pt);
                contextRow = row;
                if (row == -1)
                {
                    hostsTable.getSelectionModel().clearSelection();
                    editMenuItem.setEnabled(false);
                    delMenuItem.setEnabled(false);
                }
                else
                {
                    hostsTable.getSelectionModel().setSelectionInterval(row,row);
                    editMenuItem.setEnabled(true);
                    delMenuItem.setEnabled(true);
                }

                popupMenu.show(parent,(int)pt.getX(),(int)pt.getY());
            }
        }
    }

    public void acceptHostUpdate(Host host)
    {
        if (contextRow == -1)
        {
            hostsTableModel.addHost(host);
        }
        else
        {
            hostsTableModel.refreshHost(contextRow);
        }
    }

    private class onHostAdd implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            contextRow = -1;
            HostEditorWnd hostEditorWnd =
                new HostEditorWnd(outer,mainFrame,new Host(""));
        }
    }

    private class onHostEdit implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            HostEditorWnd hostEditorWnd =
                new HostEditorWnd(outer,mainFrame,hostsTableModel.getHost(contextRow));
        }
    }

    private class onHostDelete implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            hostsTableModel.deleteHost(contextRow);
        }
    }

    private class HostsTableModel extends AbstractTableModel
    {
        private Vector hosts;
        private PingListenerImpl pingListenerImpl;

        private class PingListenerImpl implements PingListener
        {
            public void pingResult(Host host, ProtoHandler protoHandler, ProtoState protoState)
            {
                host.getProto(protoHandler.getProtoName()).setProtoState(protoState);

                for (int i = 0; i < hosts.size(); i++)
                {
                    Host h = (Host)hosts.get(i);
                    if (h == host)
                    {
                        fireTableRowsUpdated(i,i);
                    }
                }
            }

            public void updateTaskState(Host host, ProtoHandler protoHandler, TaskState taskState)
            {
                host.getProto(protoHandler.getProtoName()).setTaskState(taskState);

                for (int i = 0; i < hosts.size(); i++)
                {
                    Host h = (Host)hosts.get(i);
                    if (h == host)
                    {
                        fireTableRowsUpdated(i,i);
                    }
                }
            }
        }

        public void addHost(Host h)
        {
            hosts.add(h);
            fireTableRowsInserted(hosts.size(),hosts.size());
            outer.save();
            pingMgr.addHost(h,pingListenerImpl);
        }

        public Host getHost(int index)
        {
            return (Host)hosts.get(index);
        }

        public void refreshHost(int index)
        {
            fireTableRowsUpdated(index,index);
            outer.save();
            pingMgr.updateHost((Host)hosts.get(index));
        }

        public void deleteHost(int index)
        {
            pingMgr.removeHost((Host)hosts.get(index));
            hosts.removeElementAt(index);
            fireTableRowsDeleted(index,index);
            outer.save();
        }

        public void save(ObjectOutput out)
        {
            try
            {
                out.writeObject(hosts);
            }

            catch(Exception e)
            {
                System.out.println("Exception: " + e);
            }
        }

        public void load(ObjectInput in)
        {
            try
            {
                Object o = in.readObject();
                if (o instanceof Vector)
                {
                    hosts = (Vector)o;

                    for (int i = 0; i < hosts.size(); i++)
                    {
                        Host h = (Host)hosts.get(i);

                        pingMgr.addHost(h,pingListenerImpl);
                    }
                }
            }

            catch(Exception e)
            {
                System.out.println("Exception: " + e);
            }
        }

        public HostsTableModel()
        {
            hosts = new Vector();
            pingListenerImpl = new PingListenerImpl();
        }

        public int getColumnCount()
        {
            return ProtoMgr.getProtoNames().size() + 1;
        }

        public int getRowCount()
        {
            return hosts.size();
        }

        public String getColumnName(int col)
        {
            if (col == 0)
            {
                return "Host";
            }
            else
            {
                Vector pn = ProtoMgr.getProtoNames();
                return ProtoMgr.getProtoHandler((String)pn.get(col-1)).getProtoName();
            }
        }

        public Object getValueAt(int row, int col)
        {
            Host h = (Host)hosts.get(row);

            if (col == 0)
            {
                return h.getHostName();
            }
            else
            {
                String protoName = (String)ProtoMgr.getProtoNames().get(col-1);
                HostProto hp = h.getProto(protoName);
                if (hp == null)
                {
                    return null;
                }
                else
                {
                    return hp.getProtoState().toString();
                }
            }
        }

        public boolean isCellEditable(int row, int col)
        {
            return false;
        }
    }

    private class ProtoStateRenderer extends JLabel implements TableCellRenderer
    {
        private final Color cEmpty = new Color(255,255,255);
        private final Color cUnknown = new Color(200,200,200);
        private final Color cDown = new Color(200,100,100);
        private final Color cUp = new Color(100,200,100);

        private Font idleFont = null;
        private Font executeFont = null;

        public ProtoStateRenderer()
        {
            setOpaque(true);

            Font f = getFont();
            idleFont = f.deriveFont(Font.PLAIN);
            executeFont = f.deriveFont(Font.BOLD);
        }

        public Component getTableCellRendererComponent(
                                    JTable table, Object color,
                                    boolean isSelected, boolean hasFocus,
                                    int row, int column)
        {
            String v = (String)table.getValueAt(row,column);

            if (v == null)
            {
                setText("");
                setBackground(cEmpty);
            }
            else
            {
                if (v == "UNKNOWN")
                {
                    setText(v);
                    setBackground(cUnknown);
                }
                else if (v == "DOWN")
                {
                    setText(v);
                    setBackground(cDown);
                }
                else if (v == "UP")
                {
                    setText(v);
                    setBackground(cUp);
                }

                setHorizontalAlignment(SwingConstants.CENTER);

                Host h = hostsTableModel.getHost(row);
                String protoName = (String)ProtoMgr.getProtoNames().get(column-1);
                HostProto hp = h.getProto(protoName);
                TaskState ts = hp.getTaskState();
                if (ts == TaskState.EXECUTE)
                {
                    setFont(executeFont);
                }
                else
                {
                    setFont(idleFont);
                }
            }

            return this;
        }
    }

    public MainWnd()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                createAndShowGUI();
            }
        });
    }

    public void createAndShowGUI()
    {
        JFrame.setDefaultLookAndFeelDecorated(true);

        outer = this;
        mainFrame = new JFrame("MultiPing v0.2 (beta)");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        hostsTableModel = new HostsTableModel();
        hostsTable = new JTable(hostsTableModel);
        hostsTable.setBackground(Color.WHITE);
        protoStateRenderer = new ProtoStateRenderer();

        for (int x = 1; x <= ProtoMgr.getProtoNames().size(); x++)
        {
            hostsTable.getColumnModel().getColumn(x).setCellRenderer(protoStateRenderer);
        }

        hostsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hostsTable.setRowSelectionAllowed(true);
        hostsTable.setColumnSelectionAllowed(false);
        hostsTable.setPreferredScrollableViewportSize(new Dimension(400, 350));

        JScrollPane scrollPane = new JScrollPane(hostsTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        addMenuItem = new JMenuItem("Add Host");
        addMenuItem.addActionListener(new onHostAdd());
        editMenuItem = new JMenuItem("Edit Host");
        editMenuItem.addActionListener(new onHostEdit());
        delMenuItem = new JMenuItem("Delete Host");
        delMenuItem.addActionListener(new onHostDelete());
        popupMenu = new JPopupMenu();
        popupMenu.add(addMenuItem);
        popupMenu.add(editMenuItem);
        popupMenu.add(delMenuItem);

        scrollPane.addMouseListener(new HostsMouseListener(scrollPane));
        hostsTable.addMouseListener(new HostsMouseListener(hostsTable));

        pingMgr = new PingMgr();

        load();
        save();

        mainFrame.pack();
        mainFrame.setVisible(true);
    }

}
