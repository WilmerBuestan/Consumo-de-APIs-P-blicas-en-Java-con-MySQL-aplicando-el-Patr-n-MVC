package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import modelo.Pais;

/** JTable (Actividad 3) con estilo moderno: filas con hover, región como "pastilla" de color. */
public class TablaPaises extends JTable {

    private static final String[] COLUMNAS = {"#", "País", "Capital", "Región", "Población", "Moneda"};
    private static final NumberFormat MILES = NumberFormat.getIntegerInstance(new Locale("es", "EC"));

    private final ModeloPaises modelo = new ModeloPaises();
    private int filaHover = -1;

    public TablaPaises() {
        setModel(modelo);
        setRowHeight(38);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setBackground(Tema.TARJETA);
        setForeground(Tema.TEXTO);
        setSelectionBackground(Tema.alfa(Tema.ACENTO, 120));
        setSelectionForeground(Color.WHITE);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setFont(Tema.F_NORMAL);
        setAutoCreateRowSorter(true);                      // clic en el encabezado = ordenar

        JTableHeader cabecera = getTableHeader();
        cabecera.setReorderingAllowed(false);
        cabecera.setPreferredSize(new Dimension(100, 42));
        cabecera.setDefaultRenderer(new RenderCabecera());

        TableCellRenderer base = new RenderCelda(false);
        setDefaultRenderer(Object.class, base);
        getColumnModel().getColumn(0).setCellRenderer(new RenderCelda(false));
        getColumnModel().getColumn(3).setCellRenderer(new RenderRegion());
        getColumnModel().getColumn(4).setCellRenderer(new RenderCelda(true));
        getColumnModel().getColumn(0).setMaxWidth(70);
        getColumnModel().getColumn(0).setPreferredWidth(56);
        getColumnModel().getColumn(1).setPreferredWidth(190);
        getColumnModel().getColumn(2).setPreferredWidth(150);
        getColumnModel().getColumn(3).setPreferredWidth(120);
        getColumnModel().getColumn(4).setPreferredWidth(130);
        getColumnModel().getColumn(5).setPreferredWidth(240);

        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = rowAtPoint(e.getPoint());
                if (fila != filaHover) {
                    filaHover = fila;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                filaHover = -1;
                repaint();
            }
        };
        addMouseMotionListener(hover);
        addMouseListener(hover);
    }

    /** Envuelve la tabla en un scroll sin bordes, listo para colocar dentro de una Tarjeta. */
    public JScrollPane envolver() {
        JScrollPane sp = new JScrollPane(this);
        sp.setBorder(new EmptyBorder(0, 0, 0, 0));
        sp.getViewport().setBackground(Tema.TARJETA);
        sp.setOpaque(false);
        return sp;
    }

    public void setPaises(List<Pais> paises) {
        modelo.paises = new ArrayList<>(paises);
        modelo.fireTableDataChanged();
    }

    public int cantidad() {
        return modelo.paises.size();
    }

    public Pais paisSeleccionado() {
        int fila = getSelectedRow();
        return fila < 0 ? null : modelo.paises.get(convertRowIndexToModel(fila));
    }

    public List<Pais> paisesSeleccionados() {
        List<Pais> lista = new ArrayList<>();
        for (int fila : getSelectedRows()) {
            lista.add(modelo.paises.get(convertRowIndexToModel(fila)));
        }
        return lista;
    }

    public List<Pais> todos() {
        return new ArrayList<>(modelo.paises);
    }

    // ------------------------------------------------------------------ modelo
    private static class ModeloPaises extends AbstractTableModel {
        List<Pais> paises = new ArrayList<>();

        @Override
        public int getRowCount() {
            return paises.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNAS.length;
        }

        @Override
        public String getColumnName(int c) {
            return COLUMNAS[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 0 ? Integer.class : c == 4 ? Long.class : String.class;
        }

        @Override
        public Object getValueAt(int fila, int col) {
            Pais p = paises.get(fila);
            switch (col) {
                case 0:
                    return p.getId();
                case 1:
                    return p.getNombre();
                case 2:
                    return p.getCapital();
                case 3:
                    return p.getRegion();
                case 4:
                    return p.getPoblacion();
                default:
                    return p.getMoneda();
            }
        }
    }

    // ------------------------------------------------------------------ renderizadores
    private class RenderCelda extends DefaultTableCellRenderer {
        private final boolean numero;

        RenderCelda(boolean numero) {
            this.numero = numero;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, fila, col);
            if (numero && v instanceof Number) {
                setText(MILES.format(((Number) v).longValue()));
            }
            if (col == 0 && v instanceof Integer && ((Integer) v) == 0) {
                setText("—");                                      // aún sin id (resultados de la API)
            }
            setHorizontalAlignment(numero ? RIGHT : LEFT);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setFont(Tema.F_NORMAL);
            setForeground(col == 0 ? Tema.SUAVE : Tema.TEXTO);
            setBackground(fondoFila(sel, fila));
            if (sel) {
                setForeground(Color.WHITE);
            }
            return this;
        }
    }

    private Color fondoFila(boolean seleccionada, int fila) {
        if (seleccionada) {
            return Tema.mezclar(Tema.TARJETA, Tema.ACENTO, 0.45f);
        }
        if (fila == filaHover) {
            return Tema.mezclar(Tema.TARJETA, Tema.ACENTO, 0.14f);
        }
        return fila % 2 == 0 ? Tema.TARJETA : Tema.mezclar(Tema.TARJETA, Tema.TARJETA_2, 0.45f);
    }

    private class RenderRegion extends JComponent implements TableCellRenderer {
        private String region = "";
        private Color fondo = Tema.TARJETA;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            region = v == null ? "" : v.toString();
            fondo = fondoFila(sel, fila);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.calidad(g);
            g2.setColor(fondo);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (!region.isEmpty()) {
                Color c = Tema.colorRegion(region);
                g2.setFont(Tema.fuente(java.awt.Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int ancho = fm.stringWidth(region) + 22;
                int alto = 24;
                int y = (getHeight() - alto) / 2;
                g2.setColor(Tema.alfa(c, 45));
                g2.fill(new RoundRectangle2D.Double(10, y, ancho, alto, alto, alto));
                g2.setColor(Tema.alfa(c, 160));
                g2.draw(new RoundRectangle2D.Double(10.5, y + 0.5, ancho - 1, alto - 1, alto, alto));
                g2.setColor(c);
                g2.drawString(region, 21, y + (alto + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    private static class RenderCabecera extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, fila, col);
            l.setText(String.valueOf(v).toUpperCase());
            l.setFont(Tema.fuente(java.awt.Font.BOLD, 11));
            l.setForeground(Tema.SUAVE);
            l.setBackground(Tema.TARJETA_2);
            l.setBorder(new EmptyBorder(0, 14, 0, 14));
            l.setHorizontalAlignment(col == 4 ? RIGHT : LEFT);
            l.setOpaque(true);
            return l;
        }
    }
}
