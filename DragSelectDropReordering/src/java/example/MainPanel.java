package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.*;
//import java.util.List;
import java.util.stream.*;
import javax.activation.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        DefaultListModel<ListItem> model = new DefaultListModel<>();
        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        model.addElement(new ListItem("asdasdfsd",  "wi0009-32.png"));
        model.addElement(new ListItem("12345",      "wi0054-32.png"));
        model.addElement(new ListItem("ADFFDF.asd", "wi0062-32.png"));
        model.addElement(new ListItem("test",       "wi0063-32.png"));
        model.addElement(new ListItem("32.png",     "wi0064-32.png"));
        model.addElement(new ListItem("asdfsd.jpg", "wi0096-32.png"));
        model.addElement(new ListItem("6896",       "wi0111-32.png"));
        model.addElement(new ListItem("t467467est", "wi0122-32.png"));
        model.addElement(new ListItem("test123",    "wi0124-32.png"));
        model.addElement(new ListItem("test(1)",    "wi0126-32.png"));

        add(new JScrollPane(new ReorderbleList<ListItem>(model)));
        setPreferredSize(new Dimension(320, 240));
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

class ListItem {
    public final ImageIcon nicon;
    public final ImageIcon sicon;
    public final String title;
    public ListItem(String title, String iconfile) {
        this.nicon = new ImageIcon(getClass().getResource(iconfile));
        ImageProducer ip = new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter());
        this.sicon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
        this.title = title;
    }
}

class ReorderbleList<E extends ListItem> extends JList<E> {
    private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);
    private transient RubberBandingListener rbl;
    private Color polygonColor;
    public ReorderbleList(ListModel<E> model) {
        super(model);
    }
    @Override public void updateUI() {
        setSelectionForeground(null); //Nimbus
        setSelectionBackground(null); //Nimbus
        setCellRenderer(null);
        setTransferHandler(null);
        removeMouseListener(rbl);
        removeMouseMotionListener(rbl);
        super.updateUI();

        polygonColor = makeRubberBandColor(getSelectionBackground());
        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(0);
        setFixedCellWidth(62);
        setFixedCellHeight(62);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setCellRenderer(new ListItemListCellRenderer<E>());
        rbl = new RubberBandingListener();
        addMouseMotionListener(rbl);
        addMouseListener(rbl);

        //putClientProperty("List.isFileList", Boolean.TRUE);
        getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setTransferHandler(new ListItemTransferHandler());
        setDropMode(DropMode.INSERT);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getDragEnabled() || rbl == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getSelectionBackground());
        g2.draw(rbl.polygon);
        g2.setComposite(ALPHA);
        g2.setPaint(polygonColor);
        g2.fill(rbl.polygon);
        g2.dispose();
    }
    private static Color makeRubberBandColor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
                     : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
    }
}

class SelectedImageFilter extends RGBImageFilter {
    //public SelectedImageFilter() {
    //    canFilterIndexColorModel = false;
    //}
    @Override public int filterRGB(int x, int y, int argb) {
        //Color color = new Color(argb, true);
        //float[] array = new float[4];
        //color.getComponents(array);
        //return new Color(array[0], array[1], array[2] * .5f, array[3]).getRGB();
        return (argb & 0xffffff00) | ((argb & 0xff) >> 1);
    }
}

class DotBorder extends EmptyBorder {
    public DotBorder(Insets borderInsets) {
        super(borderInsets);
    }
    public DotBorder(int top, int left, int bottom, int right) {
        super(top, left, bottom, right);
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(new Color(~SystemColor.activeCaption.getRGB()));
        //new Color(200, 150, 150));
        //g2.setStroke(dashed);
        //g2.drawRect(0, 0, w - 1, h - 1);
        BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
        //g2.translate(-x, -y);
        g2.dispose();
    }
    //@Override public Insets getBorderInsets(Component c)
    //@Override public Insets getBorderInsets(Component c, Insets insets)
}

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
    private final JPanel p = new JPanel(new BorderLayout());
    private final JLabel icon  = new JLabel((Icon) null, JLabel.CENTER);
    private final JLabel label = new JLabel("", JLabel.CENTER);
    private final Border dotBorder = new DotBorder(2, 2, 2, 2);
    private final Border empBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

    public ListItemListCellRenderer() {
        icon.setOpaque(false);
        label.setOpaque(true);
        label.setForeground(p.getForeground());
        label.setBackground(p.getBackground());
        label.setBorder(empBorder);
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        p.add(icon);
        p.add(label, BorderLayout.SOUTH);
    }
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E item, int index, boolean isSelected, boolean cellHasFocus) {
        label.setText(item.title);
        label.setBorder(cellHasFocus ? dotBorder : empBorder);
        if (isSelected) {
            icon.setIcon(item.sicon);
            label.setForeground(list.getSelectionForeground());
            label.setBackground(list.getSelectionBackground());
        } else {
            icon.setIcon(item.nicon);
            label.setForeground(list.getForeground());
            label.setBackground(list.getBackground());
        }
        return p;
    }
}

class RubberBandingListener extends MouseAdapter {
    public final Path2D polygon = new Path2D.Double();
    private Point srcPoint;
    @Override public void mouseDragged(MouseEvent e) {
        JList l = (JList) e.getComponent();
        if (l.getDragEnabled()) {
            Component glassPane = l.getRootPane().getGlassPane();
            glassPane.setVisible(true);
            return;
        }
        if (srcPoint == null) {
            srcPoint = e.getPoint();
            //XXX: return;
        }
        Point destPoint = e.getPoint();
        polygon.reset();
        polygon.moveTo(srcPoint.x,  srcPoint.y);
        polygon.lineTo(destPoint.x, srcPoint.y);
        polygon.lineTo(destPoint.x, destPoint.y);
        polygon.lineTo(srcPoint.x,  destPoint.y);
        polygon.closePath();
        //JDK 1.7.0: list.setSelectedIndices(getIntersectsIcons(list, polygon));
        l.setSelectedIndices(IntStream.range(0, l.getModel().getSize()).filter(i -> polygon.intersects(l.getCellBounds(i, i))).toArray());
        l.repaint();
    }
    @Override public void mouseReleased(MouseEvent e) {
        JList l = (JList) e.getComponent();
        l.setFocusable(true);
        //if (srcPoint == null || !getDragEnabled()) {
        //    Component glassPane = l.getRootPane().getGlassPane();
        //    glassPane.setVisible(false);
        //}
        srcPoint = null;
        polygon.reset();
        l.setDragEnabled(l.getSelectedIndices().length > 0);
        l.repaint();
    }
    @Override public void mousePressed(MouseEvent e) {
        JList l = (JList) e.getComponent();
        int index = l.locationToIndex(e.getPoint());
        Rectangle rect = l.getCellBounds(index, index);
        if (rect.contains(e.getPoint())) {
            l.setFocusable(true);
            if (l.getDragEnabled()) {
                return;
            }
            //System.out.println("ccc:" + startSelectedIndex);
            l.setSelectedIndex(index);
        } else {
            l.clearSelection();
            l.getSelectionModel().setAnchorSelectionIndex(-1);
            l.getSelectionModel().setLeadSelectionIndex(-1);
            l.setFocusable(false);
            l.setDragEnabled(false);
        }
        l.repaint();
    }
//     //JDK 1.7.0
//     private static int[] getIntersectsIcons(JList<?> l, Shape rect) {
//         ListModel model = l.getModel();
//         List<Integer> ll = new ArrayList<>(model.getSize());
//         for (int i = 0; i < model.getSize(); i++) {
//             if (rect.intersects(l.getCellBounds(i, i))) {
//                 ll.add(i);
//             }
//         }
//         //JDK 1.8.0: return ll.stream().mapToInt(i -> i).toArray();
//         int[] il = new int[ll.size()];
//         for (int i = 0; i < ll.size(); i++) {
//             il[i] = ll.get(i);
//         }
//         return il;
//     }
}

class ListItemTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    private int[] indices;
    private int addIndex = -1; //Location where items were added
    private int addCount; //Number of items added.

    public ListItemTransferHandler() {
        super();
        localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }
    @Override protected Transferable createTransferable(JComponent c) {
        JList source = (JList) c;
        indices = source.getSelectedIndices();
        @SuppressWarnings("deprecation") Object[] transferedObjects = source.getSelectedValues();
        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }
    @Override public boolean canImport(TransferSupport info) {
        return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
//         if (info.isDrop() && info.isDataFlavorSupported(localObjectFlavor)) {
//             info.setShowDropLocation(true);
//             info.setDropAction(MOVE);
//             return true;
//         } else {
//             return false;
//         }
    }
    @Override public int getSourceActions(JComponent c) {
        System.out.println("getSourceActions");
        Component glassPane = c.getRootPane().getGlassPane();
        glassPane.setCursor(DragSource.DefaultMoveDrop);
        //glassPane.setVisible(true);
        return MOVE; //TransferHandler.COPY_OR_MOVE;
    }
    @SuppressWarnings("unchecked")
    @Override public boolean importData(TransferSupport info) {
        TransferHandler.DropLocation tdl = info.getDropLocation();
        if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
            return false;
        }
        JList.DropLocation dl = (JList.DropLocation) tdl;
        JList target = (JList) info.getComponent();
        DefaultListModel listModel = (DefaultListModel) target.getModel();
        int index = dl.getIndex();
        //boolean insert = dl.isInsert();
        int max = listModel.getSize();
        if (index < 0 || index > max) {
            index = max;
        }
        addIndex = index;
        try {
            Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
            for (int i = 0; i < values.length; i++) {
                int idx = index++;
                listModel.add(idx, values[i]);
                target.addSelectionInterval(idx, idx);
            }
            addCount = values.length;
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    @Override protected void exportDone(JComponent c, Transferable data, int action) {
        System.out.println("exportDone");
        Component glassPane = c.getRootPane().getGlassPane();
        //glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        glassPane.setVisible(false);
        cleanup(c, action == MOVE);
    }
    private void cleanup(JComponent c, boolean remove) {
        if (remove && indices != null) {
            //If we are moving items around in the same list, we
            //need to adjust the indices accordingly, since those
            //after the insertion point have moved.
            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] >= addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            JList source = (JList) c;
            DefaultListModel model = (DefaultListModel) source.getModel();
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }
        indices  = null;
        addCount = 0;
        addIndex = -1;
    }
}
