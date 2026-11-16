import javax.swing.SwingUtilities;
import vista.FrmPais;
import vista.Tema;

public class Main {

    public static void main(String[] args) {
        Tema.instalar();                                   // tema oscuro moderno (FlatLaf)
        SwingUtilities.invokeLater(() -> {
            FrmPais frm = new FrmPais();
            frm.setVisible(true);
        });
    }
}
