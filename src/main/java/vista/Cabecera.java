package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Título + subtítulo que se repite en cada pantalla, con un hueco a la derecha para acciones. */
public final class Cabecera {

    private Cabecera() {
    }

    public static JPanel crear(String titulo, String subtitulo, Component acciones) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(titulo);
        t.setFont(Tema.F_TITULO);
        t.setForeground(Tema.TEXTO);
        JLabel s = new JLabel("<html><body style='width:640px'>" + subtitulo + "</body></html>");
        s.setFont(Tema.F_SUBTITULO);
        s.setForeground(Tema.SUAVE);
        s.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        textos.add(t);
        textos.add(s);
        p.add(textos, BorderLayout.WEST);
        if (acciones != null) {
            p.add(acciones, BorderLayout.EAST);
        }
        return p;
    }
}
