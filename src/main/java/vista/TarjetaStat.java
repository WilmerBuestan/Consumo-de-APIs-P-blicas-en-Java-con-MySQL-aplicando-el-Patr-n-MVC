package vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.LongFunction;
import javax.swing.Icon;

/** Tarjeta de estadística con número animado (cuenta de 0 hasta el valor). */
public class TarjetaStat extends Tarjeta {

    private final String titulo;
    private final Color acento;
    private final Icon icono;
    private String valor = "—";
    private String nota = "";
    private double mostrado = 0;
    private javax.swing.Timer animacion;

    public TarjetaStat(String titulo, Color acento, Icon icono) {
        super(null, 22);
        this.titulo = titulo;
        this.acento = acento;
        this.icono = icono;
        setPreferredSize(new Dimension(240, 120));
    }

    /** Anima el número hasta 'destino'. */
    public void setNumero(long destino, LongFunction<String> formato, String nota) {
        this.nota = nota;
        if (animacion != null) {
            animacion.stop();
        }
        final double desde = mostrado;
        animacion = Animador.animar(900, x -> {
            mostrado = desde + (destino - desde) * x;
            valor = formato.apply(Math.round(mostrado));
            repaint();
        }, () -> {
            mostrado = destino;
            valor = formato.apply(destino);
            repaint();
        });
    }

    /** Valor de texto (por ejemplo el nombre de un país). */
    public void setTexto(String valor, String nota) {
        this.valor = valor;
        this.nota = nota;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = Tema.calidad(g);
        int w = getWidth();

        // franja de color superior
        g2.setColor(Tema.alfa(acento, 210));
        g2.fill(new RoundRectangle2D.Double(22, 0, 44, 4, 4, 4));

        // icono en círculo
        int d = 38;
        g2.setColor(Tema.alfa(acento, 38));
        g2.fill(new Ellipse2D.Double(w - d - 18, 18, d, d));
        if (icono != null) {
            icono.paintIcon(this, g2, w - d - 18 + (d - icono.getIconWidth()) / 2, 18 + (d - icono.getIconHeight()) / 2);
        }

        g2.setFont(Tema.F_PEQUENA);
        g2.setColor(Tema.SUAVE);
        g2.drawString(titulo.toUpperCase(), 22, 36);

        int maxAncho = w - 44;
        int tam = 30;
        Font f = Tema.fuente(Font.BOLD, tam);
        FontMetrics fm = g2.getFontMetrics(f);
        while (fm.stringWidth(valor) > maxAncho && tam > 14) {
            tam -= 2;
            f = Tema.fuente(Font.BOLD, tam);
            fm = g2.getFontMetrics(f);
        }
        g2.setFont(f);
        g2.setColor(Tema.TEXTO);
        g2.drawString(valor, 22, 78);

        g2.setFont(Tema.F_PEQUENA);
        g2.setColor(Tema.SUAVE);
        g2.drawString(nota, 22, 102);
        g2.dispose();
    }
}
