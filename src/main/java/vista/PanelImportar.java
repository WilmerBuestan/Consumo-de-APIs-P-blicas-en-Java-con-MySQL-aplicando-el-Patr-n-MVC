package vista;

import controlador.PaisController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.util.List;

/**
 * Caso principal del taller: al presionar [IMPORTAR PAÍSES] la aplicación
 * 1) consume la API REST, 2) obtiene el JSON, 3) lo convierte a objetos Java,
 * 4) guarda los registros en la base de datos y 5) muestra un mensaje de confirmación.
 */
public class PanelImportar extends PanelAnimado {

    private static final String[] PASOS = {
        "Consume la API REST",
        "Obtiene información en formato JSON",
        "Convierte el JSON a objetos Java",
        "Guarda los registros en MySQL",
        "Muestra mensaje de confirmación"
    };

    private final JFrame ventana;
    private final PaisController controller;
    private final BotonModerno btnImportar;
    private final BarraProgreso barra = new BarraProgreso();
    private final JLabel lblEstado = new JLabel("Listo para importar.");
    private final JLabel lblPorcentaje = new JLabel("0 %");
    private final ListaPasos pasos = new ListaPasos();
    private final TarjetaStat statTotal;
    private final TarjetaStat statNuevos;
    private final TarjetaStat statActualizados;
    private boolean hayResultado = false;

    public PanelImportar(JFrame ventana, PaisController controller) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        add(Cabecera.crear("Importar países",
                "Consume la API REST Countries, convierte el JSON a objetos Java y guarda los registros en la base de datos.",
                null), BorderLayout.NORTH);

        // ---------------- tarjeta principal
        Tarjeta hero = new Tarjeta(new BorderLayout(28, 0));
        hero.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));

        hero.add(pasos, BorderLayout.WEST);

        JPanel derecha = new JPanel();
        derecha.setOpaque(false);
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Sincronizar con REST Countries");
        titulo.setFont(Tema.F_SECCION);
        titulo.setForeground(Tema.TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel detalle = new JLabel("<html>Descarga todos los países (nombre, capital, región, población y moneda). "
                + "Si un país ya existe se actualiza, así no se duplican registros.</html>");
        detalle.setFont(Tema.F_NORMAL);
        detalle.setForeground(Tema.SUAVE);
        detalle.setAlignmentX(LEFT_ALIGNMENT);

        btnImportar = new BotonModerno("IMPORTAR PAÍSES", Iconos.de(Iconos.Tipo.DESCARGA, 20, Color.WHITE),
                BotonModerno.Estilo.PRIMARIO);
        btnImportar.setFont(Tema.fuente(java.awt.Font.BOLD, 15));
        btnImportar.setMargin(new java.awt.Insets(14, 28, 14, 28));
        btnImportar.addActionListener(e -> importar());
        JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaBoton.setOpaque(false);
        filaBoton.setAlignmentX(LEFT_ALIGNMENT);
        filaBoton.add(btnImportar);

        JPanel filaBarra = new JPanel(new BorderLayout(12, 0));
        filaBarra.setOpaque(false);
        filaBarra.setAlignmentX(LEFT_ALIGNMENT);
        filaBarra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        barra.setPreferredSize(new Dimension(100, 14));
        lblPorcentaje.setFont(Tema.fuente(java.awt.Font.BOLD, 13));
        lblPorcentaje.setForeground(Tema.TEXTO);
        filaBarra.add(barra, BorderLayout.CENTER);
        filaBarra.add(lblPorcentaje, BorderLayout.EAST);

        lblEstado.setFont(Tema.F_NORMAL);
        lblEstado.setForeground(Tema.SUAVE);
        lblEstado.setAlignmentX(LEFT_ALIGNMENT);

        derecha.add(titulo);
        derecha.add(javax.swing.Box.createVerticalStrut(8));
        derecha.add(detalle);
        derecha.add(javax.swing.Box.createVerticalStrut(26));
        derecha.add(filaBoton);
        derecha.add(javax.swing.Box.createVerticalStrut(26));
        derecha.add(filaBarra);
        derecha.add(javax.swing.Box.createVerticalStrut(10));
        derecha.add(lblEstado);
        hero.add(derecha, BorderLayout.CENTER);

        // ---------------- estadísticas
        JPanel fila = new JPanel(new GridLayout(1, 3, 18, 0));
        fila.setOpaque(false);
        fila.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        statTotal = new TarjetaStat("Países en la base", Tema.ACENTO, Iconos.de(Iconos.Tipo.MUNDO, 20, Tema.ACENTO));
        statNuevos = new TarjetaStat("Nuevos agregados", Tema.OK, Iconos.de(Iconos.Tipo.MAS, 20, Tema.OK));
        statActualizados = new TarjetaStat("Actualizados", Tema.ACENTO2, Iconos.de(Iconos.Tipo.REFRESCAR, 20, Tema.ACENTO2));
        fila.add(statTotal);
        fila.add(statNuevos);
        fila.add(statActualizados);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        abajo.add(fila, BorderLayout.NORTH);
        centro.add(hero, BorderLayout.NORTH);
        centro.add(abajo, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    /** Se llama cada vez que el usuario entra a esta pantalla. */
    @Override
    public void entrar() {
        super.entrar();
        Async.ejecutar(controller::contarPaises,
                n -> {
                    statTotal.setNumero(n, Tema::miles, "registros guardados");
                    if (!hayResultado) {
                        statNuevos.setTexto("—", "aún sin importar en esta sesión");
                        statActualizados.setTexto("—", "aún sin importar en esta sesión");
                    }
                },
                e -> statTotal.setTexto("—", "sin conexión a la base de datos"));
    }

    // ------------------------------------------------------------------ acción del botón
    private void importar() {
        btnImportar.setEnabled(false);
        barra.reiniciar();
        barra.setActiva(true);
        pasos.setPaso(1);
        lblPorcentaje.setText("0 %");
        lblEstado.setForeground(Tema.SUAVE);

        new SwingWorker<PaisController.ResultadoImportacion, Object[]>() {
            @Override
            protected PaisController.ResultadoImportacion doInBackground() throws Exception {
                return controller.importarPaises((fase, mensaje, pct) -> publish(new Object[]{fase, mensaje, pct}));
            }

            @Override
            protected void process(List<Object[]> avances) {
                Object[] a = avances.get(avances.size() - 1);       // solo el más reciente
                pasos.setPaso((Integer) a[0]);
                lblEstado.setText((String) a[1]);
                int pct = (Integer) a[2];
                barra.setObjetivo(pct);
                lblPorcentaje.setText(pct + " %");
            }

            @Override
            protected void done() {
                btnImportar.setEnabled(true);
                barra.setActiva(false);
                try {
                    PaisController.ResultadoImportacion r = get();
                    barra.setObjetivo(100);
                    lblPorcentaje.setText("100 %");
                    pasos.setPaso(5);                               // 5. mensaje de confirmación
                    hayResultado = true;
                    statTotal.setNumero(r.total(), Tema::miles, "registros guardados");
                    statNuevos.setNumero(r.insertados(), v -> String.valueOf(v), "agregados ahora");
                    statActualizados.setNumero(r.actualizados(), v -> String.valueOf(v), "ya existían");
                    if (r.desdeApi()) {
                        lblEstado.setText("Importación finalizada: " + r.total() + " países.");
                        Toast.mostrar(ventana, "Importación finalizada: " + r.total() + " países", Toast.Tipo.OK);
                        JOptionPane.showMessageDialog(ventana, "Importación Finalizada");
                    } else {
                        lblEstado.setText("API no disponible: se usan los datos guardados.");
                        Toast.mostrar(ventana, r.aviso(), Toast.Tipo.AVISO);
                    }
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    pasos.setPaso(0);
                    barra.setObjetivo(0);
                    lblPorcentaje.setText("0 %");
                    lblEstado.setForeground(Tema.ERROR);
                    String msg = Async.mensaje(causa instanceof Exception ? (Exception) causa : new Exception(causa));
                    lblEstado.setText("Falló la importación.");
                    Toast.mostrar(ventana, msg, Toast.Tipo.ERROR);
                }
            }
        }.execute();
    }

    // ------------------------------------------------------------------ lista de 5 pasos animada
    private static class ListaPasos extends JComponent {
        private int paso = 0;          // 0 = ninguno, 1..5 = paso activo, 6 = todo completo
        private float pulso = 0f;
        private final Timer timer = new Timer(33, e -> {
            pulso += 0.06f;
            if (paso >= 1 && paso < 5) {
                repaint();
            }
        });

        ListaPasos() {
            setPreferredSize(new Dimension(340, 230));
            setOpaque(false);
            timer.start();
        }

        void setPaso(int p) {
            this.paso = p;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.calidad(g);
            int alto = 46;
            for (int i = 0; i < PASOS.length; i++) {
                int num = i + 1;
                int y = 4 + i * alto;
                boolean hecho = paso > num || paso == 5 && num == 5;
                boolean activo = paso == num && paso != 5;

                // línea conectora
                if (i < PASOS.length - 1) {
                    g2.setColor(hecho ? Tema.alfa(Tema.OK, 150) : Tema.alfa(Tema.BORDE, 200));
                    g2.fillRect(15, y + 32, 2, alto - 24);
                }

                Color c = hecho ? Tema.OK : activo ? Tema.ACENTO : Tema.BORDE;
                if (activo) {
                    double r = 16 + 5 * Math.abs(Math.sin(pulso));
                    g2.setColor(Tema.alfa(Tema.ACENTO, 55));
                    g2.fill(new Ellipse2D.Double(16 - r, y + 16 - r, r * 2, r * 2));
                }
                g2.setColor(hecho || activo ? c : Tema.TARJETA_2);
                g2.fill(new Ellipse2D.Double(0, y, 32, 32));
                g2.setColor(c);
                g2.draw(new Ellipse2D.Double(0.5, y + 0.5, 31, 31));

                if (hecho) {
                    Iconos.de(Iconos.Tipo.CHECK, 18, new Color(0x0B1020)).paintIcon(this, g2, 7, y + 7);
                } else {
                    g2.setFont(Tema.fuente(java.awt.Font.BOLD, 14));
                    g2.setColor(activo ? Color.WHITE : Tema.SUAVE);
                    String s = String.valueOf(num);
                    g2.drawString(s, 16 - g2.getFontMetrics().stringWidth(s) / 2, y + 21);
                }

                g2.setFont(Tema.F_NORMAL);
                g2.setColor(hecho || activo ? Tema.TEXTO : Tema.SUAVE);
                g2.drawString(PASOS[i], 46, y + 21);
            }
            g2.dispose();
        }
    }
}
