package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final Color EVEN_COLOR = new Color(250, 250, 250);
    private MainPanel() {
        super(new BorderLayout());
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JTable table1 = makeTable();
        URLRenderer1 renderer1 = new URLRenderer1();
        table1.setDefaultRenderer(URL.class, renderer1);
        table1.addMouseListener(renderer1);
        table1.addMouseMotionListener(renderer1);

        JTable table = makeTable();
        URLRenderer renderer = new URLRenderer();
        table.setDefaultRenderer(URL.class, renderer);
        table.addMouseListener(renderer);
        table.addMouseMotionListener(renderer);


        sp.setTopComponent(new JScrollPane(table1));
        sp.setBottomComponent(new JScrollPane(table));
        sp.setResizeWeight(.5);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JTable makeTable() {
        TestModel model = new TestModel();
        try {
            model.addTest(new Test("FrontPage", new URL("http://ateraimemo.com/")));
            model.addTest(new Test("Java Swing Tips", new URL("http://ateraimemo.com/Swing.html")));
            model.addTest(new Test("Example", new URL("http://www.example.com/")));
            model.addTest(new Test("Example.jp", new URL("http://www.example.jp/")));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                c.setForeground(getForeground());
                c.setBackground((row % 2 == 0) ? EVEN_COLOR : getBackground());
                return c;
            }
        };
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setAutoCreateRowSorter(true);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setResizable(false);

        return table;
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

class URLRenderer1 extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
    private int row = -1;
    private int col = -1;
    private boolean isRollover;
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        String str = Objects.toString(value, "");

        if (!table.isEditing() && this.row == row && this.col == column && this.isRollover) {
            setText("<html><u><font color='blue'>" + str);
        } else if (hasFocus) {
            setText("<html><font color='blue'>" + str);
        } else {
            setText(str);
        }
        return this;
    }
    private static boolean isURLColumn(JTable table, int column) {
        return column >= 0 && table.getColumnClass(column).equals(URL.class);
    }
    @Override public void mouseMoved(MouseEvent e) {
        JTable table = (JTable) e.getComponent();
        Point pt = e.getPoint();
        int prevRow = row;
        int prevCol = col;
        boolean prevRollover = isRollover;
        row = table.rowAtPoint(pt);
        col = table.columnAtPoint(pt);
        isRollover = isURLColumn(table, col); // && pointInsidePrefSize(table, pt);
        if (row == prevRow && col == prevCol && isRollover == prevRollover || !isRollover && !prevRollover) {
            return;
        }

// >>>> HyperlinkCellRenderer.java
// @see http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/demos/table/HyperlinkCellRenderer.java
        Rectangle repaintRect;
        if (isRollover) {
            Rectangle r = table.getCellRect(row, col, false);
            repaintRect = prevRollover ? r.union(table.getCellRect(prevRow, prevCol, false)) : r;
        } else { //if (prevRollover) {
            repaintRect = table.getCellRect(prevRow, prevCol, false);
        }
        table.repaint(repaintRect);
// <<<<
        //table.repaint();
    }
    @Override public void mouseExited(MouseEvent e) {
        JTable table = (JTable) e.getComponent();
        if (isURLColumn(table, col)) {
            table.repaint(table.getCellRect(row, col, false));
            row = -1;
            col = -1;
            isRollover = false;
        }
    }
    @Override public void mouseClicked(MouseEvent e) {
        JTable table = (JTable) e.getComponent();
        Point pt = e.getPoint();
        int ccol = table.columnAtPoint(pt);
        if (isURLColumn(table, ccol)) { // && pointInsidePrefSize(table, pt)) {
            int crow = table.rowAtPoint(pt);
            URL url = (URL) table.getValueAt(crow, ccol);
            System.out.println(url);
            try {
                //Web Start
                //BasicService bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
                //bs.showDocument(url);
                if (Desktop.isDesktopSupported()) { // JDK 1.6.0
                    Desktop.getDesktop().browse(url.toURI());
                }
            } catch (URISyntaxException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    @Override public void mouseDragged(MouseEvent e)  { /* not needed */ }
    @Override public void mouseEntered(MouseEvent e)  { /* not needed */ }
    @Override public void mousePressed(MouseEvent e)  { /* not needed */ }
    @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
}

class URLRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
    private static Rectangle lrect = new Rectangle();
    private static Rectangle irect = new Rectangle();
    private static Rectangle trect = new Rectangle();
    private int row = -1;
    private int col = -1;
    private boolean isRollover;
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        int mw = table.getColumnModel().getColumnMargin();
        int rh = table.getRowMargin();
        int w  = table.getColumnModel().getColumn(column).getWidth();
        int h  = table.getRowHeight(row);

        Insets i = this.getInsets();
        lrect.x = i.left;
        lrect.y = i.top;
        lrect.width  = w - mw - i.right  - lrect.x;
        lrect.height = h - rh - i.bottom - lrect.y;
        irect.setBounds(0, 0, 0, 0); //.x = irect.y = irect.width = irect.height = 0;
        trect.setBounds(0, 0, 0, 0); //.x = trect.y = trect.width = trect.height = 0;

        String str = SwingUtilities.layoutCompoundLabel(
            this,
            this.getFontMetrics(this.getFont()),
            value.toString(), //this.getText(),
            this.getIcon(),
            this.getVerticalAlignment(),
            this.getHorizontalAlignment(),
            this.getVerticalTextPosition(),
            this.getHorizontalTextPosition(),
            lrect,
            irect, //icon
            trect, //text
            this.getIconTextGap());

        if (!table.isEditing() && this.row == row && this.col == column && this.isRollover) {
            setText("<html><u><font color='blue'>" + str);
        } else if (hasFocus) {
            setText("<html><font color='blue'>" + str);
        } else {
            setText(str);
        }
        return this;
    }
    //@see SwingUtilities2.pointOutsidePrefSize(...)
    //private static boolean pointInsidePrefSize(JTable table, Point p) {
    //    int row = table.rowAtPoint(p);
    //    int col = table.columnAtPoint(p);
    //    TableCellRenderer tcr = table.getCellRenderer(row, col);
    //    Object value = table.getValueAt(row, col);
    //    Component cell = tcr.getTableCellRendererComponent(table, value, false, false, row, col);
    //    Dimension itemSize = cell.getPreferredSize();
    //    Insets i = ((JComponent) cell).getInsets();
    //    Rectangle cellBounds = table.getCellRect(row, col, false);
    //    cellBounds.width = itemSize.width-i.right-i.left;
    //    cellBounds.translate(i.left, i.top);
    //    return cellBounds.contains(p);
    //}
    private static boolean isURLColumn(JTable table, int column) {
        return column >= 0 && table.getColumnClass(column).equals(URL.class);
    }
    @Override public void mouseMoved(MouseEvent e) {
        JTable table = (JTable) e.getComponent();
        Point pt = e.getPoint();
        int prevRow = row;
        int prevCol = col;
        boolean prevRollover = isRollover;
        row = table.rowAtPoint(pt);
        col = table.columnAtPoint(pt);
        isRollover = isURLColumn(table, col); // && pointInsidePrefSize(table, pt);
        if (row == prevRow && col == prevCol && isRollover == prevRollover || !isRollover && !prevRollover) {
            return;
        }

// >>>> HyperlinkCellRenderer.java
// @see http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/demos/table/HyperlinkCellRenderer.java
        Rectangle repaintRect;
        if (isRollover) {
            Rectangle r = table.getCellRect(row, col, false);
            repaintRect = prevRollover ? r.union(table.getCellRect(prevRow, prevCol, false)) : r;
        } else { //if (prevRollover) {
            repaintRect = table.getCellRect(prevRow, prevCol, false);
        }
        table.repaint(repaintRect);
// <<<<
        //table.repaint();
    }
    @Override public void mouseExited(MouseEvent e) {
        JTable table = (JTable) e.getComponent();
        if (isURLColumn(table, col)) {
            table.repaint(table.getCellRect(row, col, false));
            row = -1;
            col = -1;
            isRollover = false;
        }
    }
    @Override public void mouseClicked(MouseEvent e) {
        JTable table = (JTable) e.getComponent();
        Point pt = e.getPoint();
        int ccol = table.columnAtPoint(pt);
        if (isURLColumn(table, ccol)) { // && pointInsidePrefSize(table, pt)) {
            int crow = table.rowAtPoint(pt);
            URL url = (URL) table.getValueAt(crow, ccol);
            System.out.println(url);
            try {
                //Web Start
                //BasicService bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
                //bs.showDocument(url);
                if (Desktop.isDesktopSupported()) { // JDK 1.6.0
                    Desktop.getDesktop().browse(url.toURI());
                }
            } catch (URISyntaxException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    @Override public void mouseDragged(MouseEvent e)  { /* not needed */ }
    @Override public void mouseEntered(MouseEvent e)  { /* not needed */ }
    @Override public void mousePressed(MouseEvent e)  { /* not needed */ }
    @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
}

class TestModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        new ColumnContext("No.",  Integer.class, false),
        new ColumnContext("Name", String.class,  false),
        new ColumnContext("URL",  URL.class,     false)
    };
    private int number;
    public void addTest(Test t) {
        Object[] obj = {number, t.getName(), t.getURL()};
        super.addRow(obj);
        number++;
    }
    @Override public boolean isCellEditable(int row, int col) {
        return COLUMN_ARRAY[col].isEditable;
    }
    @Override public Class<?> getColumnClass(int modelIndex) {
        return COLUMN_ARRAY[modelIndex].columnClass;
    }
    @Override public int getColumnCount() {
        return COLUMN_ARRAY.length;
    }
    @Override public String getColumnName(int modelIndex) {
        return COLUMN_ARRAY[modelIndex].columnName;
    }
    private static class ColumnContext {
        public final String  columnName;
        public final Class   columnClass;
        public final boolean isEditable;
        public ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}

class Test {
    private String name;
    private URL url;
    public Test(String name, URL url) {
        this.name = name;
        this.url = url;
    }
    public void setName(String str) {
        this.name = str;
    }
    public void setURL(URL url) {
        this.url = url;
    }
    public String getName() {
        return name;
    }
    public URL getURL() {
        return url;
    }
}
