import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class HostEditorWnd
{
    private JDialog dlg;
    private Frame parent;
    private Host origHost;
    private Host host;
    private MainWnd mainWnd;

    private ProtosTableModel protosTableModel;
    private JTable protosTable;

    private ProtoArgsTableModel protoArgsTableModel;
    private JTable protoArgsTable;

    private JTextField hostNameFld;

    private class ProtosTableSelectionListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            if (! e.getValueIsAdjusting())
            {
                protoArgsTableModel.fireTableDataChanged();
            }
        }
    }

    private class ProtosTableModel extends AbstractTableModel
    {
        public int getColumnCount()
        {
            return 2;
        }

        public int getRowCount()
        {
            return ProtoMgr.getProtoNames().size();
        }

        public String getColumnName(int col)
        {
            if (col == 0)
            {
                return "Enabled";
            }
            else
            {
                return "Name";
            }
        }

        public Object getValueAt(int row, int col)
        {
            String protoName = (String)ProtoMgr.getProtoNames().get(row);

            if (col == 1)
            {
                return protoName;
            }
            else
            {
                if (host.getProto(protoName) != null)
                {
                    return new Boolean(true);
                }
                else
                {
                    return new Boolean(false);
                }
            }
        }

        public void setValueAt(Object val, int row, int col)
        {
            if (col == 0)
            {
                String protoName = (String)protosTable.getValueAt(row,1);
                if (((Boolean)val).booleanValue())
                {
                    ProtoHandler protoHandler = ProtoMgr.getProtoHandler(protoName);
                    ProtoArgs protoArgs = new ProtoArgs();
                    protoArgs.copyFrom(protoHandler.getDefaultArgs());

                    host.setProto(protoName,
                                  new HostProto(protoHandler,
                                                protoArgs));
                }
                else
                {
                    host.removeProto(protoName);
                }

                protoArgsTableModel.fireTableDataChanged();
            }
        }

        public boolean isCellEditable(int row, int col)
        {
            if (col == 1)
            {
                return false;
            }
            else
            {
                return true;
            }
        }

        public Class getColumnClass(int c)
        {
            return getValueAt(0,c).getClass();
        }
    }

    private class ProtoArgsTableModel extends AbstractTableModel
    {
        public int getColumnCount()
        {
            return 2;
        }

        public int getRowCount()
        {
            int row = protosTable.getSelectedRow();
            if (row >= 0)
            {
                String protoName = (String)protosTable.getValueAt(row,1);
                HostProto hp = host.getProto(protoName);
                if (hp == null)
                {
                    return 0;
                }
                else
                {
                    return hp.getProtoHandler().getDefaultArgs().getArgNames().size();
                }
            }
            else
            {
                return 0;
            }
        }

        public String getColumnName(int col)
        {
            if (col == 0)
            {
                return "Name";
            }
            else
            {
                return "Value";
            }
        }

        public Object getValueAt(int row, int col)
        {
            Object result = null;

            int r = protosTable.getSelectedRow();
            if (r >= 0)
            {
                String protoName = (String)protosTable.getValueAt(r,1);
                HostProto hp = host.getProto(protoName);
                if (hp != null)
                {
                    String argName = (String)hp.getProtoHandler().getDefaultArgs().getArgNames().get(row);
                    if (col == 0)
                    {
                        return argName;
                    }
                    else
                    {
                        return hp.getProtoArgs().getArgValue(argName);
                    }
                }
            }

            return result;
        }

        public void setValueAt(Object val, int row, int col)
        {
            if (col == 1)
            {
                int r = protosTable.getSelectedRow();
                if (r >= 0)
                {
                    String protoName = (String)protosTable.getValueAt(r,1);
                    String argName = (String)protoArgsTable.getValueAt(row,0);
                    HostProto hp = host.getProto(protoName);
                    if (hp != null)
                    {
                        hp.getProtoArgs().setArgValue(argName,(String)val);
                    }
                }
            }
        }

        public boolean isCellEditable(int row, int col)
        {
            if (col > 0)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    private class HostNameActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            host.setHostName(hostNameFld.getText());
        }
    }

    private class OKBtnActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            host.setHostName(hostNameFld.getText());
            origHost.copyFrom(host);
            mainWnd.acceptHostUpdate(origHost);
            dlg.dispose();
        }
    }

    private class CancelBtnActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            dlg.dispose();
        }
    }

    public HostEditorWnd(MainWnd mainWnd, Frame parent, Host host)
    {
        this.parent = parent;
        this.origHost = host;
        this.host = new Host(host.getHostName());
        this.host.copyFrom(host);
        this.mainWnd = mainWnd;

        dlg = new JDialog(parent,"Host Editor",true);

        Container contentPane = dlg.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        JLabel l = new JLabel("Host");
        p.add(l);
        hostNameFld = new JTextField();
        hostNameFld.setText(host.getHostName());
        hostNameFld.addActionListener(new HostNameActionListener());
        p.add(hostNameFld);
        contentPane.add(p);

        l = new JLabel("Protocols");
        p = new JPanel();
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(l);
        contentPane.add(p);
        protosTableModel = new ProtosTableModel();
        protosTable = new JTable(protosTableModel);
        protosTableModel.addTableModelListener(protosTable);
        protosTable.getSelectionModel().addListSelectionListener(
            new ProtosTableSelectionListener());
        protosTable.setPreferredScrollableViewportSize(new Dimension(300, 70));
        JScrollPane scrollPane = new JScrollPane(protosTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        contentPane.add(scrollPane);

        l = new JLabel("Protocol Arguments");
        p = new JPanel();
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(l);
        contentPane.add(p);
        protoArgsTableModel = new ProtoArgsTableModel();
        protoArgsTable = new JTable(protoArgsTableModel);
        protoArgsTableModel.addTableModelListener(protoArgsTable);
        protoArgsTable.setPreferredScrollableViewportSize(new Dimension(300, 70));
        scrollPane = new JScrollPane(protoArgsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        contentPane.add(scrollPane);

        p = new JPanel();
        p.setLayout(new GridLayout(1,3));
        p.add(new JPanel());
        p.add(new JPanel());
        JPanel p2 = new JPanel();

        JButton b = new JButton("OK");
        b.addActionListener(new OKBtnActionListener());
        p2.add(b);

        b = new JButton("Cancel");
        b.addActionListener(new CancelBtnActionListener());
        p2.add(b);

        p.add(p2);

        contentPane.add(p);

        dlg.pack();
        dlg.setVisible(true);
    }
}
