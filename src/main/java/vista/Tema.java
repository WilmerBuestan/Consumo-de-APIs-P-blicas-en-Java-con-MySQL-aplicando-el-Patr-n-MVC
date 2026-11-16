package vista;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

/** Paleta, fuentes y configuración global del tema oscuro moderno. */
public final class Tema {

    public static final Color FONDO = new Color(0x0B1020);
    public static final Color SIDEBAR = new Color(0x0E1428);
    public static final Color TARJETA = new Color(0x151C33);
    public static final Color TARJETA_2 = new Color(0x1C2646);
    public static final Color BORDE = new Color(0x2A3560);
    public static final Color TEXTO = new Color(0xE8EBF7);
    public static final Color SUAVE = new Color(0x8E97BA);
    public static final Color ACENTO = new Color(0x7C5CFF);
    public static final Color ACENTO2 = new Color(0x22D3EE);
    public static final Color OK = new Color(0x34D399);
    public static final Color ERROR = new Color(0xFB7185);
    public static final Color AVISO = new Color(0xFBBF24);

    public static final String FAMILIA = "Segoe UI";

    public static final Font F_TITULO = fuente(Font.BOLD, 26);
    public static final Font F_SUBTITULO = fuente(Font.PLAIN, 14);
    public static final Font F_SECCION = fuente(Font.BOLD, 16);
    public static final Font F_NORMAL = fuente(Font.PLAIN, 14);
    public static final Font F_PEQUENA = fuente(Font.PLAIN, 12);
    public static final Font F_BOTON = fuente(Font.BOLD, 13);
    public static final Font F_NUMERO = fuente(Font.BOLD, 30);

    private Tema() {
    }

    public static Font fuente(int estilo, int tamano) {
        return new Font(FAMILIA, estilo, tamano);
    }

    public static void instalar() {
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#7C5CFF"));
        FlatDarkLaf.setup();
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        UIManager.put("defaultFont", F_NORMAL);
        UIManager.put("Component.arc", 14);
        UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", 14);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", TARJETA);
        UIManager.put("OptionPane.messageForeground", TEXTO);
        UIManager.put("Dialog.background", TARJETA);
        UIManager.put("TextField.background", new Color(0x0F1630));
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("ComboBox.background", new Color(0x0F1630));
        UIManager.put("ComboBox.foreground", TEXTO);
        UIManager.put("Table.background", TARJETA);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("ToolTip.background", TARJETA_2);
        UIManager.put("ToolTip.foreground", TEXTO);
    }

    public static Color colorRegion(String region) {
        if (region == null) {
            return SUAVE;
        }
        switch (region) {
            case "América":
                return new Color(0x7C5CFF);
            case "Europa":
                return new Color(0x22D3EE);
            case "Asia":
                return new Color(0xFBBF24);
            case "África":
                return new Color(0x34D399);
            case "Oceanía":
                return new Color(0xFB7185);
            case "Antártida":
                return new Color(0x94A3B8);
            default:
                return SUAVE;
        }
    }

    /** 18135478 -> "18.135.478" (siempre con punto, sin depender del idioma del equipo). */
    public static String miles(long n) {
        return String.format(java.util.Locale.US, "%,d", n).replace(',', '.');
    }

    public static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    public static Color mezclar(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    public static Graphics2D calidad(java.awt.Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }
}
