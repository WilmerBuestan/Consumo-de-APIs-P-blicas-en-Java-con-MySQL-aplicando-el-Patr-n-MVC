package vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JToggleButton;
import javax.swing.Timer;

/** Botón tipo "pastilla" para filtrar por región; se ilumina con el color de la región. */
public class ChipRegion extends JToggleButton {

    private final Color color;
    private float seleccion = 0f;
    private float hover = 0f;
    private Timer timerSel;
    private Timer timerHover;

    public ChipRegion(String texto, Color color) {
        super(texto);
        this.color = color;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFont(Tema.F_BOTON);
        setForeground(Tema.TEXTO);
        setMargin(new Insets(7, 16, 7, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addItemListener(e -> animarSeleccion(e.getStateChange() == ItemEvent.SELECTED ? 1f : 0f));
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

    private void animarSeleccion(float destino) {
        if (timerSel != null) {
            timerSel.stop();
        }
        final float desde = seleccion;
        timerSel = Animador.animar(220, x -> {
            seleccion = (float) (desde + (destino - desde) * x);
            Color texto = seleccion > 0.5f ? new Color(0x0B1020) : Tema.TEXTO;
            if (!texto.equals(getForeground())) {
                setForeground(texto);
            }
            repaint();
        }, null);
    }

    private void animarHover(float destino) {
        if (timerHover != null) {
            timerHover.stop();
        }
        final float desde = hover;
        timerHover = Animador.animar(150, x -> {
            hover = (float) (desde + (destino - desde) * x);
            repaint();
        }, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.calidad(g);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(Tema.mezclar(Tema.TARJETA_2, color, 0.85f * seleccion + 0.10f * hover));
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, h, h));
        g2.setColor(Tema.alfa(color, (int) (70 + 150 * Math.max(seleccion, hover))));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, h, h));
        g2.dispose();
        super.paintComponent(g);
    }
}
