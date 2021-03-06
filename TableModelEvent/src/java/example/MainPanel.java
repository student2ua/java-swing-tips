package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
    private final Object[][] data = {
        {true, 1, "BBB"}, {false, 12, "AAA"},
        {true, 2, "DDD"}, {false,  5, "CCC"},
        {true, 3, "EEE"}, {false,  6, "GGG"},
        {true, 4, "FFF"}, {false,  7, "HHH"}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        private static final int MODEL_COLUMN_INDEX = 0;
        private transient HeaderCheckBoxHandler handler;
        @Override public void updateUI() {
            // Bug ID: 6788475 Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
            // http://bugs.java.com/view_bug.do?bug_id=6788475
            // XXX: set dummy ColorUIResource
            setSelectionForeground(new ColorUIResource(Color.RED));
            setSelectionBackground(new ColorUIResource(Color.RED));
            getTableHeader().removeMouseListener(handler);
            TableModel m = getModel();
            if (Objects.nonNull(m)) {
                m.removeTableModelListener(handler);
            }
            super.updateUI();

            m = getModel();
            for (int i = 0; i < m.getColumnCount(); i++) {
                TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
                if (r instanceof Component) {
                    SwingUtilities.updateComponentTreeUI((Component) r);
                }
            }
            TableColumn column = getColumnModel().getColumn(MODEL_COLUMN_INDEX);
            column.setHeaderRenderer(new HeaderRenderer());
            column.setHeaderValue(Status.INDETERMINATE);

            handler = new HeaderCheckBoxHandler(this, MODEL_COLUMN_INDEX);
            m.addTableModelListener(handler);
            getTableHeader().addMouseListener(handler);
        }
        @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
            Component c = super.prepareEditor(editor, row, column);
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                b.setBackground(getSelectionBackground());
                b.setBorderPainted(true);
            }
            return c;
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    class AddRowAction extends AbstractAction {
        private final boolean isSelected;
        public AddRowAction(String label, boolean isSelected) {
            super(label);
            this.isSelected = isSelected;
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {isSelected, 0, ""});
            table.scrollRectToVisible(table.getCellRect(model.getRowCount() - 1, 0, true));
        }
    }

    class DeleteAction extends AbstractAction {
        public DeleteAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if (selection.length == 0) {
                return;
            }
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete");
        public TablePopupMenu() {
            super();
            add(new AddRowAction("add(true)", true));
            add(new AddRowAction("add(false)", false));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l.length > 0);
            super.show(c, x, y);
        }
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class HeaderRenderer implements TableCellRenderer {
    private final JCheckBox check = new JCheckBox("");
    private final JLabel label = new JLabel("Check All");
    @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        if (val instanceof Status) {
            switch ((Status) val) {
              case SELECTED:      check.setSelected(true);  check.setEnabled(true);  break;
              case DESELECTED:    check.setSelected(false); check.setEnabled(true);  break;
              case INDETERMINATE: check.setSelected(true);  check.setEnabled(false); break;
              default:            throw new AssertionError("Unknown Status");
            }
        } else {
            check.setSelected(true); check.setEnabled(false);
        }
        check.setOpaque(false);
        check.setFont(tbl.getFont());
        TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
        JLabel l = (JLabel) r.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
        label.setIcon(new ComponentIcon(check));
        l.setIcon(new ComponentIcon(label));
        l.setText(null); //XXX: Nimbus???
//         System.out.println("getHeaderRect: " + tbl.getTableHeader().getHeaderRect(col));
//         System.out.println("getPreferredSize: " + l.getPreferredSize());
//         System.out.println("getMaximunSize: " + l.getMaximumSize());
//         System.out.println("----");
//         if (l.getPreferredSize().height > 1000) { //XXX: Nimbus???
//             System.out.println(l.getPreferredSize().height);
//             Rectangle rect = tbl.getTableHeader().getHeaderRect(col);
//             l.setPreferredSize(new Dimension(0, rect.height));
//         }
        return l;
    }
}

class HeaderCheckBoxHandler extends MouseAdapter implements TableModelListener {
    private final JTable table;
    private final int targetColumnIndex;
    public HeaderCheckBoxHandler(JTable table, int index) {
        super();
        this.table = table;
        this.targetColumnIndex = index;
    }
    @Override public void tableChanged(TableModelEvent e) {
        int vci = table.convertColumnIndexToView(targetColumnIndex);
        TableColumn column = table.getColumnModel().getColumn(vci);
        Object status = column.getHeaderValue();
        TableModel m = table.getModel();
        boolean repaint = false;
        if (e.getType() == TableModelEvent.DELETE) {
            //System.out.println("DELETE");
            //System.out.println(status + ":   " + Status.INDETERMINATE.equals(status));
            repaint = fireDeleteEvent(m, column, status);
        } else if (e.getType() == TableModelEvent.INSERT && !Status.INDETERMINATE.equals(status)) {
            //System.out.println("INSERT");
            repaint = fireInsertEvent(m, column, status, e);
        } else if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
            //System.out.println("UPDATE");
            repaint = fireUpdateEvent(m, column, status);
        }
        if (repaint) {
            JTableHeader h = table.getTableHeader();
            h.repaint(h.getHeaderRect(vci));
        }
    }
    private boolean fireDeleteEvent(TableModel m, TableColumn column, Object status) {
        if (m.getRowCount() == 0) {
            column.setHeaderValue(Status.DESELECTED);
        } else if (Status.INDETERMINATE.equals(status)) {
            boolean selected = true;
            boolean deselected = true;
            for (int i = 0; i < m.getRowCount(); i++) {
                Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
                selected &= b;
                deselected &= !b;
            }
            //System.out.println(selected);
            //System.out.println(deselected);
            if (deselected) {
                column.setHeaderValue(Status.DESELECTED);
            } else if (selected) {
                column.setHeaderValue(Status.SELECTED);
            } else {
                return false;
            }
        }
        return true;
    }
    private boolean fireInsertEvent(TableModel m, TableColumn column, Object status, TableModelEvent e) {
        boolean selected = Status.DESELECTED.equals(status);
        boolean deselected = Status.SELECTED.equals(status);
        for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
            Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
            selected &= b;
            deselected &= !b;
        }
        if (selected && m.getRowCount() == 1) {
            column.setHeaderValue(Status.SELECTED);
        } else if (selected || deselected) {
            column.setHeaderValue(Status.INDETERMINATE);
        } else {
            return false;
        }
        return true;
    }
    private boolean fireUpdateEvent(TableModel m, TableColumn column, Object status) {
        if (Status.INDETERMINATE.equals(status)) {
            boolean selected = true;
            boolean deselected = true;
            for (int i = 0; i < m.getRowCount(); i++) {
                Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
                selected &= b;
                deselected &= !b;
                if (selected == deselected) {
                    return false;
                }
            }
            if (deselected) {
                column.setHeaderValue(Status.DESELECTED);
            } else if (selected) {
                column.setHeaderValue(Status.SELECTED);
            } else {
                return false;
            }
        } else {
            column.setHeaderValue(Status.INDETERMINATE);
        }
        return true;
    }
    @Override public void mouseClicked(MouseEvent e) {
        JTableHeader header = (JTableHeader) e.getComponent();
        JTable table = header.getTable();
        TableColumnModel columnModel = table.getColumnModel();
        TableModel m = table.getModel();
        int vci = columnModel.getColumnIndexAtX(e.getX());
        int mci = table.convertColumnIndexToModel(vci);
        if (mci == targetColumnIndex && m.getRowCount() > 0) {
            TableColumn column = columnModel.getColumn(vci);
            Object v = column.getHeaderValue();
            boolean b = Status.DESELECTED.equals(v);
            for (int i = 0; i < m.getRowCount(); i++) {
                m.setValueAt(b, i, mci);
            }
            column.setHeaderValue(b ? Status.SELECTED : Status.DESELECTED);
            //header.repaint();
        }
    }
}

class ComponentIcon implements Icon {
    private final JComponent cmp;
    public ComponentIcon(JComponent cmp) {
        this.cmp = cmp;
    }
    @Override public int getIconWidth() {
        return cmp.getPreferredSize().width;
    }
    @Override public int getIconHeight() {
        return cmp.getPreferredSize().height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight());
    }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }
