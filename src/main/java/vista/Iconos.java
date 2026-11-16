package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/** Iconos dibujados con vectores (no dependen de fuentes ni de imágenes). */
public class Iconos implements Icon {

    public enum Tipo {
        DESCARGA, BUSCAR, TABLA, GRAFICO, MAS, EDITAR, BORRAR, REFRESCAR, MUNDO, GUARDAR, CHECK
    }

    private final Tipo tipo;
    private final int tamano;
    private final Color color;

    public Iconos(Tipo tipo, int tamano, Color color) {
        this.tipo = tipo;
        this.tamano = tamano;
        this.color = color;
    }

    public static Icon de(Tipo tipo, int tamano, Color color) {
        return new Iconos(tipo, tamano, color);
    }

    @Override
    public int getIconWidth() {
        return tamano;
    }

    @Override
    public int getIconHeight() {
        return tamano;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = Tema.calidad(g);
        g2.translate(x, y);
        double escala = tamano / 24.0;
        g2.scale(escala, escala);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (tipo) {
            case DESCARGA:
                g2.draw(new Line2D.Double(12, 4, 12, 15));
                g2.draw(poli(7, 11, 12, 16, 17, 11));
                g2.draw(new Line2D.Double(5, 20, 19, 20));
                break;
            case BUSCAR:
                g2.draw(new Ellipse2D.Double(4.5, 4.5, 12, 12));
                g2.draw(new Line2D.Double(15, 15, 20, 20));
                break;
            case TABLA:
                g2.draw(new RoundRectangle2D.Double(4, 5, 16, 14, 3, 3));
                g2.draw(new Line2D.Double(4, 10, 20, 10));
                g2.draw(new Line2D.Double(4, 14.5, 20, 14.5));
                g2.draw(new Line2D.Double(10, 10, 10, 19));
                break;
            case GRAFICO:
                g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Double(7, 19, 7, 13));
                g2.draw(new Line2D.Double(12, 19, 12, 6));
                g2.draw(new Line2D.Double(17, 19, 17, 10));
                break;
            case MAS:
                g2.draw(new Line2D.Double(12, 5, 12, 19));
                g2.draw(new Line2D.Double(5, 12, 19, 12));
                break;
            case EDITAR:
                g2.draw(poli(5, 19, 5.8, 15, 16, 4.8, 19.2, 8, 9, 18.2, 5, 19));
                g2.draw(new Line2D.Double(13.5, 7.5, 16.5, 10.5));
                break;
            case BORRAR:
                g2.draw(new RoundRectangle2D.Double(7, 8, 10, 12, 3, 3));
                g2.draw(new Line2D.Double(5, 8, 19, 8));
                g2.draw(poli(10, 8, 10, 5, 14, 5, 14, 8));
                g2.draw(new Line2D.Double(10.5, 11.5, 10.5, 16.5));
                g2.draw(new Line2D.Double(13.5, 11.5, 13.5, 16.5));
                break;
            case REFRESCAR:
                g2.draw(new Arc2D.Double(5, 5, 14, 14, 50, 280, Arc2D.OPEN));
                g2.draw(poli(17.8, 3.8, 17.8, 8.2, 13.4, 8.2));
                break;
            case MUNDO:
                g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g2.draw(new Ellipse2D.Double(8, 3, 8, 18));
                g2.draw(new Line2D.Double(3, 12, 21, 12));
                break;
            case GUARDAR:
                g2.draw(new RoundRectangle2D.Double(5, 4, 14, 16, 3, 3));
                g2.draw(new Line2D.Double(8, 4, 8, 8.5));
                g2.draw(new Line2D.Double(8, 8.5, 15, 8.5));
                g2.draw(new RoundRectangle2D.Double(8, 13, 8, 7, 2, 2));
                break;
            case CHECK:
                g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(poli(5, 12.5, 10, 17.5, 19, 7));
                break;
            default:
                break;
        }
        g2.dispose();
    }

    private static Path2D poli(double... p) {
        Path2D path = new Path2D.Double();
        path.moveTo(p[0], p[1]);
        for (int i = 2; i < p.length; i += 2) {
            path.lineTo(p[i], p[i + 1]);
        }
        return path;
    }
}
