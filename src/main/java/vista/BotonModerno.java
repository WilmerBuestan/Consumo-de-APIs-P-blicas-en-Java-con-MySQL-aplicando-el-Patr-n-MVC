package vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.Timer;

/** Botón redondeado con degradado, brillo al pasar el mouse y efecto al presionar. */
public class BotonModerno extends JButton {

    public enum Estilo {
        PRIMARIO, SECUNDARIO, PELIGRO, EXITO
    }

    private final Estilo estilo;
    private float hover = 0f;
    private boolean presionado = false;
    private Timer timerHover;

    public BotonModerno(String texto, Icon icono, Estilo estilo) {
        super(texto, icono);
        this.estilo = estilo;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(Tema.F_BOTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 18, 10, 18));
        setIconTextGap(10);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animarHover(1f);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animarHover(0f);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                presionado = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                presionado = false;
                repaint();
            }
        });
    }

    public BotonModerno(String texto, Estilo estilo) {
        this(texto, null, estilo);
    }

    private void animarHover(float destino) {
        if (timerHover != null) {
            timerHover.stop();
        }
        final float desde = hover;
        timerHover = Animador.animar(180, x -> {
            hover = (float) (desde + (destino - desde) * x);
            repaint();
        }, null);
    }

    private Color colorBase() {
        switch (estilo) {
            case PELIGRO:
                return Tema.ERROR;
            case EXITO:
                return new Color(0x10B981);
            case SECUNDARIO:
                return Tema.TARJETA_2;
            default:
                return Tema.ACENTO;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.calidad(g);
        int w = getWidth();
        int h = getHeight();
        Color base = colorBase();
        Color arriba = Tema.mezclar(base, Color.WHITE, 0.10f * hover + (presionado ? -0.0f : 0f));
        Color abajo = estilo == Estilo.PRIMARIO
                ? Tema.mezclar(Tema.ACENTO, Tema.ACENTO2, 0.55f + 0.2f * hover)
                : Tema.mezclar(base, Color.BLACK, 0.12f);
        if (presionado) {
            arriba = Tema.mezclar(arriba, Color.BLACK, 0.18f);
            abajo = Tema.mezclar(abajo, Color.BLACK, 0.18f);
        }
        if (!isEnabled()) {
            arriba = Tema.alfa(arriba, 90);
            abajo = Tema.alfa(abajo, 90);
        }

        // resplandor al pasar el mouse
        if (hover > 0.01f && isEnabled() && estilo != Estilo.SECUNDARIO) {
            for (int i = 5; i >= 1; i--) {
                g2.setColor(Tema.alfa(abajo, (int) (14 * hover)));
                g2.fill(new RoundRectangle2D.Double(1 - i, 1 - i + 2, w - 2 + 2 * i, h - 2 + 2 * i - 2, 16 + i, 16 + i));
            }
        }

        g2.setPaint(new GradientPaint(0, 0, arriba, w, h, abajo));
        g2.fill(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 16, 16));

        if (estilo == Estilo.SECUNDARIO) {
            g2.setColor(Tema.alfa(Tema.ACENTO, (int) (60 + 140 * hover)));
            g2.draw(new RoundRectangle2D.Double(1.5, 1.5, w - 3, h - 3, 16, 16));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
