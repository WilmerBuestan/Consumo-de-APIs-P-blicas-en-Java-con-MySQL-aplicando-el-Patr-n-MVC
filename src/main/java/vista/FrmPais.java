package vista;

import controlador.PaisController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * VISTA principal (JFrame): barra lateral de navegación + paneles.
 * Solo se comunica con el CONTROLADOR; nunca toca la base de datos ni la API.
 */
public class FrmPais extends JFrame {

    private final PaisController controller;
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final List<BotonNav> navegacion = new ArrayList<>();
    private final IndicadorEstado estado = new IndicadorEstado();

    private final PanelImportar pImportar;
    private final PanelConsulta pConsulta;
    private final PanelPaises pPaises;
    private final PanelReportes pReportes;

    public FrmPais() {
        this(new PaisController());
    }

    public FrmPais(PaisController controller) {
        super("Países · Consumo de APIs públicas con MVC");
        this.controller = controller;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 680));
        setSize(1240, 760);
        setLocationRelativeTo(null);

        pImportar = new PanelImportar(this, controller);
        pConsulta = new PanelConsulta(this, controller);
        pPaises = new PanelPaises(this, controller);
        pReportes = new PanelReportes(this, controller);

        contenido.setBackground(Tema.FONDO);
        contenido.add(pImportar, "importar");
        contenido.add(pConsulta, "consulta");
        contenido.add(pPaises, "paises");
        contenido.add(pReportes, "reportes");

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);
        raiz.add(crearLateral(), BorderLayout.WEST);
        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);

        mostrar("importar");
        iniciarBaseDeDatos();
    }

    // ------------------------------------------------------------------ navegación
    private JPanel crearLateral() {
        JPanel lateral = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.calidad(g);
                g2.setPaint(new GradientPaint(0, 0, Tema.SIDEBAR, 0, getHeight(), new Color(0x0A0F20)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Tema.alfa(Tema.BORDE, 120));
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        lateral.setOpaque(false);
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setPreferredSize(new Dimension(250, 100));
        lateral.setBorder(BorderFactory.createEmptyBorder(26, 16, 20, 16));

        lateral.add(new Logo());
        lateral.add(javax.swing.Box.createVerticalStrut(30));

        lateral.add(item("importar", "Importar países", Iconos.Tipo.DESCARGA));
        lateral.add(javax.swing.Box.createVerticalStrut(6));
        lateral.add(item("consulta", "Consultar API", Iconos.Tipo.BUSCAR));
        lateral.add(javax.swing.Box.createVerticalStrut(6));
        lateral.add(item("paises", "Países (CRUD)", Iconos.Tipo.TABLA));
        lateral.add(javax.swing.Box.createVerticalStrut(6));
        lateral.add(item("reportes", "Reportes", Iconos.Tipo.GRAFICO));

        lateral.add(javax.swing.Box.createVerticalGlue());
        lateral.add(estado);
        return lateral;
    }

    private BotonNav item(String id, String texto, Iconos.Tipo icono) {
        BotonNav b = new BotonNav(texto, icono);
        b.addActionListener(e -> mostrar(id));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.putClientProperty("id", id);
        navegacion.add(b);
        return b;
    }

    private void mostrar(String id) {
        for (BotonNav b : navegacion) {
            b.setSeleccionado(id.equals(b.getClientProperty("id")));
        }
        cartas.show(contenido, id);
        switch (id) {
            case "importar":
                pImportar.entrar();
                break;
            case "consulta":
                pConsulta.entrar();
                break;
            case "paises":
                pPaises.entrar();
                break;
            default:
                pReportes.entrar();
                break;
        }
    }

    // ------------------------------------------------------------------ base de datos al iniciar
    private void iniciarBaseDeDatos() {
        estado.setEstado(IndicadorEstado.Estado.CONECTANDO, "Conectando a la base de datos…");
        Async.ejecutar(() -> {
            controller.inicializarBD();
            return Boolean.TRUE;
        }, ok -> {
            estado.setEstado(IndicadorEstado.Estado.OK, "Base de datos conectada");
            pImportar.entrar();
        }, ex -> {
            estado.setEstado(IndicadorEstado.Estado.ERROR, "Sin conexión a la base de datos");
            Toast.mostrar(this, "No se pudo conectar a la base de datos. Revisa config/db.properties. ("
                    + Async.mensaje(ex) + ")", Toast.Tipo.ERROR);
        });
    }

    // ------------------------------------------------------------------ componentes del lateral
    private static class Logo extends JComponent {
        Logo() {
            setPreferredSize(new Dimension(218, 56));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.calidad(g);
            g2.setPaint(new GradientPaint(0, 0, Tema.ACENTO, 48, 48, Tema.ACENTO2));
            g2.fill(new RoundRectangle2D.Double(4, 4, 46, 46, 16, 16));
            Iconos.de(Iconos.Tipo.MUNDO, 28, Color.WHITE).paintIcon(this, g2, 13, 13);
            g2.setColor(Tema.TEXTO);
            g2.setFont(Tema.fuente(java.awt.Font.BOLD, 19));
            g2.drawString("Países", 64, 27);
            g2.setColor(Tema.SUAVE);
            g2.setFont(Tema.F_PEQUENA);
            g2.drawString("API REST · MVC · MySQL", 64, 46);
            g2.dispose();
        }
    }

    /** Botón de menú con barra de acento animada. */
    private static class BotonNav extends JButton {
        private final Icono icono;
        private float seleccion = 0f;
        private float hover = 0f;
        private Timer tSel;
        private Timer tHover;
        private boolean seleccionado = false;

        BotonNav(String texto, Iconos.Tipo tipo) {
            super(texto);
            this.icono = new Icono(tipo);
            setIcon(icono);
            setHorizontalAlignment(LEFT);
            setIconTextGap(14);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(Tema.fuente(java.awt.Font.BOLD, 14));
            setForeground(Tema.SUAVE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new java.awt.Insets(12, 16, 12, 12));
            setPreferredSize(new Dimension(218, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animarHover(1f);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    animarHover(0f);
                }
            });
        }

        void setSeleccionado(boolean sel) {
            if (sel == seleccionado) {
                return;
            }
            seleccionado = sel;
            if (tSel != null) {
                tSel.stop();
            }
            final float desde = seleccion;
            final float hasta = sel ? 1f : 0f;
            tSel = Animador.animar(260, x -> {
                seleccion = (float) (desde + (hasta - desde) * x);
                setForeground(Tema.mezclar(Tema.SUAVE, Tema.TEXTO, seleccion));
                repaint();
            }, null);
        }

        private void animarHover(float destino) {
            if (tHover != null) {
                tHover.stop();
            }
            final float desde = hover;
            tHover = Animador.animar(150, x -> {
                hover = (float) (desde + (destino - desde) * x);
                repaint();
            }, null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.calidad(g);
            int w = getWidth();
            int h = getHeight();
            float fuerza = Math.max(seleccion, hover * 0.5f);
            g2.setColor(Tema.alfa(Tema.ACENTO, (int) (46 * seleccion + 22 * hover * (1 - seleccion))));
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 14, 14));
            if (seleccion > 0.01f) {
                int alto = (int) ((h - 18) * seleccion);
                g2.setPaint(new GradientPaint(0, 0, Tema.ACENTO, 0, h, Tema.ACENTO2));
                g2.fill(new RoundRectangle2D.Double(0, (h - alto) / 2.0, 4, alto, 4, 4));
            }
            icono.color = Tema.mezclar(Tema.SUAVE, Tema.ACENTO2, Math.min(1f, fuerza * 1.4f));
            g2.dispose();
            super.paintComponent(g);
        }

        private static class Icono implements Icon {
            private final Iconos.Tipo tipo;
            Color color = Tema.SUAVE;

            Icono(Iconos.Tipo tipo) {
                this.tipo = tipo;
            }

            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Iconos.de(tipo, 20, color).paintIcon(c, g, x, y);
            }

            @Override
            public int getIconWidth() {
                return 20;
            }

            @Override
            public int getIconHeight() {
                return 20;
            }
        }
    }

    /** Punto de color que pulsa + texto del estado de la base de datos. */
    private static class IndicadorEstado extends JComponent {
        enum Estado { CONECTANDO, OK, ERROR }

        private Estado estado = Estado.CONECTANDO;
        private String texto = "";
        private float pulso = 0f;

        IndicadorEstado() {
            setPreferredSize(new Dimension(218, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(LEFT_ALIGNMENT);
            new Timer(40, e -> {
                pulso += 0.08f;
                repaint();
            }).start();
        }

        void setEstado(Estado e, String texto) {
            this.estado = e;
            this.texto = texto;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.calidad(g);
            Color c = estado == Estado.OK ? Tema.OK : estado == Estado.ERROR ? Tema.ERROR : Tema.AVISO;
            g2.setColor(Tema.alfa(Tema.TARJETA, 255));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
            double r = 5 + (estado == Estado.ERROR ? 0 : 3 * Math.abs(Math.sin(pulso)));
            g2.setColor(Tema.alfa(c, 70));
            g2.fill(new Ellipse2D.Double(22 - r, getHeight() / 2.0 - r, r * 2, r * 2));
            g2.setColor(c);
            g2.fill(new Ellipse2D.Double(18, getHeight() / 2.0 - 4, 8, 8));
            g2.setColor(Tema.SUAVE);
            g2.setFont(Tema.F_PEQUENA);
            g2.drawString(texto, 38, getHeight() / 2 + 4);
            g2.dispose();
        }
    }
}
