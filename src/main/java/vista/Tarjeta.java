package vista;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

/** Panel tipo "card": fondo redondeado con borde sutil. */
public class Tarjeta extends JPanel {

    private final int arco;

    public Tarjeta(LayoutManager layout, int arco) {
        super(layout);
        this.arco = arco;
        setOpaque(false);
    }

    public Tarjeta(LayoutManager layout) {
        this(layout, 22);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.calidad(g);
        g2.setColor(Tema.TARJETA);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arco, arco));
        g2.setColor(Tema.alfa(Tema.BORDE, 150));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, arco, arco));
        g2.dispose();
        super.paintComponent(g);
    }
}
