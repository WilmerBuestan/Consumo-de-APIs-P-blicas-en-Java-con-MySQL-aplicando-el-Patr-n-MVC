package vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.Timer;

/** Notificación flotante que entra deslizando desde la derecha y se desvanece sola. */
public final class Toast extends JComponent {

    public enum Tipo {
        OK, ERROR, INFO, AVISO
    }

    private static final List<Toast> VIVOS = new ArrayList<>();
    private static final int MAX_TEXTO = 400;

    private final List<String> lineas;
    private final Tipo tipo;
    private float opacidad = 1f;

    private Toast(List<String> lineas, Tipo tipo) {
        this.lineas = lineas;
        this.tipo = tipo;
        setOpaque(false);
    }

    public static void mostrar(JFrame ventana, String mensaje, Tipo tipo) {
        JLayeredPane capa = ventana.getLayeredPane();
        FontMetrics fm = capa.getFontMetrics(Tema.F_NORMAL);
        List<String> lineas = partir(mensaje, fm, MAX_TEXTO);
        int ancho = 84;
        int maxLinea = 0;
        for (String l : lineas) {
            maxLinea = Math.max(maxLinea, fm.stringWidth(l));
        }
        ancho += maxLinea;
        int alto = 28 + lineas.size() * 20;

        Toast toast = new Toast(lineas, tipo);
        int apilado = 0;
        for (Toast t : VIVOS) {
            apilado += t.getHeight() + 10;
        }
        final int yFinal = capa.getHeight() - alto - 28 - apilado;     // abajo y al centro, apilados hacia arriba
        final int yInicio = capa.getHeight();
        final int xFinal = (capa.getWidth() - ancho) / 2;
        toast.opacidad = 0f;
        toast.setBounds(xFinal, yInicio, ancho, alto);
        VIVOS.add(toast);
        capa.add(toast, JLayeredPane.POPUP_LAYER);

        Animador.animar(360, x -> {
            toast.setLocation(xFinal, (int) (yInicio + (yFinal - yInicio) * x));
            toast.opacidad = (float) x;
            toast.repaint();
        }, () -> {
            Timer espera = new Timer(3600, e -> Animador.animar(450, x -> {
                toast.opacidad = (float) (1 - x);
                toast.repaint();
            }, () -> {
                capa.remove(toast);
                capa.repaint();
                VIVOS.remove(toast);
            }));
            espera.setRepeats(false);
            espera.start();
        });
    }

    private static List<String> partir(String texto, FontMetrics fm, int ancho) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split("\\s+")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (fm.stringWidth(prueba) > ancho && actual.length() > 0) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) {
            lineas.add(actual.toString());
        }
        return lineas.size() > 4 ? new ArrayList<>(lineas.subList(0, 4)) : lineas;
    }

    private Color color() {
        switch (tipo) {
            case OK:
                return Tema.OK;
            case ERROR:
                return Tema.ERROR;
            case AVISO:
                return Tema.AVISO;
            default:
                return Tema.ACENTO2;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.calidad(g);
        g2.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, Math.min(1f, opacidad))));
        int w = getWidth();
        int h = getHeight();
        Color c = color();

        g2.setColor(Tema.TARJETA_2);
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 18, 18));
        g2.setColor(Tema.alfa(c, 170));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 18, 18));

        // círculo con símbolo
        int d = 30;
        int cy = h / 2 - d / 2;
        g2.setColor(Tema.alfa(c, 45));
        g2.fill(new Ellipse2D.Double(16, cy, d, d));
        g2.setColor(c);
        String simbolo = tipo == Tipo.OK ? "✓" : tipo == Tipo.ERROR ? "!" : tipo == Tipo.AVISO ? "!" : "i";
        g2.setFont(Tema.fuente(java.awt.Font.BOLD, 16));
        FontMetrics fmS = g2.getFontMetrics();
        g2.drawString(simbolo, 16 + (d - fmS.stringWidth(simbolo)) / 2, cy + (d + fmS.getAscent() - fmS.getDescent()) / 2);

        g2.setColor(Tema.TEXTO);
        g2.setFont(Tema.F_NORMAL);
        FontMetrics fm = g2.getFontMetrics();
        int y = 14 + fm.getAscent();
        for (String linea : lineas) {
            g2.drawString(linea, 62, y);
            y += 20;
        }
        g2.dispose();
    }
}
