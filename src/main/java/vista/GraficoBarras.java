package vista;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComponent;

/** Gráfico de barras horizontales dibujado a mano, con animación de crecimiento. */
public class GraficoBarras extends JComponent {

    private Map<String, Integer> datos = new LinkedHashMap<>();
    private float progreso = 1f;

    public void setDatos(Map<String, Integer> nuevos) {
        this.datos = new LinkedHashMap<>(nuevos);
        this.progreso = 0f;
        Animador.animar(1000, x -> {
            progreso = (float) x;
            repaint();
        }, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.calidad(g);
        int w = getWidth();
        int h = getHeight();

        if (datos.isEmpty()) {
            g2.setFont(Tema.F_NORMAL);
            g2.setColor(Tema.SUAVE);
            String msg = "Todavía no hay datos. Importa los países primero.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        int izquierda = 120;
        int derecha = 70;
        int filas = datos.size();
        int altoFila = Math.max(26, Math.min(56, (h - 10) / filas));
        int altoBarra = Math.min(24, altoFila - 12);
        int max = 1;
        for (int v : datos.values()) {
            max = Math.max(max, v);
        }

        int i = 0;
        for (Map.Entry<String, Integer> e : datos.entrySet()) {
            int y = 5 + i * altoFila + (altoFila - altoBarra) / 2;
            Color c = Tema.colorRegion(e.getKey());

            g2.setFont(Tema.F_NORMAL);
            g2.setColor(Tema.TEXTO);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(e.getKey(), 6, y + (altoBarra + fm.getAscent() - fm.getDescent()) / 2);

            int pista = w - izquierda - derecha;
            g2.setColor(Tema.alfa(Tema.TARJETA_2, 200));
            g2.fill(new RoundRectangle2D.Double(izquierda, y, pista, altoBarra, altoBarra, altoBarra));

            double largo = pista * (e.getValue() / (double) max) * progreso;
            if (largo > 0.5) {
                double ancho = Math.max(altoBarra, largo);
                g2.setPaint(new GradientPaint(izquierda, 0, c, (float) (izquierda + ancho), 0, Tema.mezclar(c, Color.WHITE, 0.35f)));
                g2.fill(new RoundRectangle2D.Double(izquierda, y, ancho, altoBarra, altoBarra, altoBarra));
            }

            g2.setFont(Tema.fuente(java.awt.Font.BOLD, 14));
            g2.setColor(Tema.TEXTO);
            String valor = String.valueOf(Math.round(e.getValue() * progreso));
            g2.drawString(valor, izquierda + pista + 14, y + (altoBarra + fm.getAscent() - fm.getDescent()) / 2);
            i++;
        }
        g2.dispose();
    }
}
