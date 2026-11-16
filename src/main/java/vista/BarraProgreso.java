package vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.Timer;

/** Barra de progreso redondeada con degradado, avance suave y brillo en movimiento. */
public class BarraProgreso extends JComponent {

    private double valor = 0;       // 0..100 mostrado
    private Timer animacion;
    private float brillo = 0f;
    private final Timer timerBrillo;
    private boolean activa = false;

    public BarraProgreso() {
        setPreferredSize(new Dimension(300, 16));
        setOpaque(false);
        timerBrillo = new Timer(30, e -> {
            brillo += 0.02f;
            if (brillo > 1.3f) {
                brillo = -0.3f;
            }
            repaint();
        });
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
        if (activa) {
            timerBrillo.start();
        } else {
            timerBrillo.stop();
            repaint();
        }
    }

    /** Lleva la barra suavemente hasta 'objetivo' (0..100). */
    public void setObjetivo(int objetivo) {
        if (animacion != null) {
            animacion.stop();
        }
        final double desde = valor;
        animacion = Animador.animar(320, x -> {
            valor = desde + (objetivo - desde) * x;
            repaint();
        }, null);
    }

    public void reiniciar() {
        if (animacion != null) {
            animacion.stop();
        }
        valor = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.calidad(g);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(Tema.TARJETA_2);
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, h, h));

        double ancho = Math.max(0, w * valor / 100.0);
        if (ancho > 1) {
            Shape relleno = new RoundRectangle2D.Double(0, 0, Math.max(h, ancho), h, h, h);
            g2.setPaint(new GradientPaint(0, 0, Tema.ACENTO, w, 0, Tema.ACENTO2));
            g2.fill(relleno);
            if (activa) {
                g2.setClip(relleno);
                int x = (int) (brillo * w);
                g2.setPaint(new GradientPaint(x - 60, 0, new Color(255, 255, 255, 0), x, 0, new Color(255, 255, 255, 90)));
                g2.fillRect(x - 60, 0, 60, h);
                g2.setPaint(new GradientPaint(x, 0, new Color(255, 255, 255, 90), x + 60, 0, new Color(255, 255, 255, 0)));
                g2.fillRect(x, 0, 60, h);
            }
        }
        g2.dispose();
    }
}
