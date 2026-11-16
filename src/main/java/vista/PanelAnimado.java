package vista;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/** Panel que aparece con desvanecimiento y un pequeño deslizamiento hacia arriba. */
public class PanelAnimado extends JPanel {

    private float alfa = 1f;

    public PanelAnimado(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    /** Llamar cuando el panel se vuelve visible. */
    public void entrar() {
        alfa = 0f;
        repaint();
        Animador.animar(420, x -> {
            alfa = (float) x;
            repaint();
        }, () -> {
            alfa = 1f;
            repaint();
        });
    }

    @Override
    public void paint(Graphics g) {
        if (alfa >= 1f) {
            super.paint(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(0, (1f - alfa) * 18);
        g2.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, Math.min(1f, alfa))));
        super.paint(g2);
        g2.dispose();
    }
}
