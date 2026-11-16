package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import modelo.Pais;
import modelo.Regiones;

/** Formulario modal para crear o editar un país. */
public class DialogoPais extends JDialog {

    private final JTextField txtNombre = new JTextField(24);
    private final JTextField txtCapital = new JTextField(24);
    private final JComboBox<String> cmbRegion = new JComboBox<>(Regiones.LISTA);
    private final JTextField txtPoblacion = new JTextField(24);
    private final JTextField txtMoneda = new JTextField(24);
    private final Pais pais;
    private boolean aceptado = false;

    public DialogoPais(JFrame padre, Pais existente) {
        super(padre, existente == null ? "Nuevo país" : "Editar país", true);
        this.pais = existente == null ? new Pais() : existente;

        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setBackground(Tema.TARJETA);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));

        JLabel titulo = new JLabel(existente == null ? "Registrar un país" : "Modificar datos del país");
        titulo.setFont(Tema.F_SECCION);
        titulo.setForeground(Tema.TEXTO);
        contenido.add(titulo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        agregar(form, 0, "Nombre", txtNombre);
        agregar(form, 1, "Capital", txtCapital);
        agregar(form, 2, "Región", cmbRegion);
        agregar(form, 3, "Población", txtPoblacion);
        agregar(form, 4, "Moneda", txtMoneda);
        contenido.add(form, BorderLayout.CENTER);

        BotonModerno btnGuardar = new BotonModerno("Guardar", Iconos.de(Iconos.Tipo.GUARDAR, 18, Color.WHITE),
                BotonModerno.Estilo.PRIMARIO);
        BotonModerno btnCancelar = new BotonModerno("Cancelar", BotonModerno.Estilo.SECUNDARIO);
        btnGuardar.addActionListener(e -> aceptar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnCancelar);
        botones.add(btnGuardar);
        contenido.add(botones, BorderLayout.SOUTH);

        if (existente != null) {
            txtNombre.setText(pais.getNombre());
            txtCapital.setText(pais.getCapital());
            cmbRegion.setSelectedItem(pais.getRegion());
            txtPoblacion.setText(String.valueOf(pais.getPoblacion()));
            txtMoneda.setText(pais.getMoneda());
        } else {
            txtPoblacion.setText("0");
        }

        setContentPane(contenido);
        getRootPane().setDefaultButton(btnGuardar);
        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private void agregar(JPanel form, int fila, String etiqueta, java.awt.Component campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(6, 0, 6, 14);
        c.anchor = GridBagConstraints.WEST;
        JLabel l = new JLabel(etiqueta);
        l.setFont(Tema.F_NORMAL);
        l.setForeground(Tema.SUAVE);
        c.gridx = 0;
        form.add(l, c);
        c.gridx = 1;
        c.insets = new Insets(6, 0, 6, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(campo, c);
    }

    private void aceptar() {
        long poblacion;
        try {
            String t = txtPoblacion.getText().trim().replace(".", "").replace(",", "");
            poblacion = t.isEmpty() ? 0 : Long.parseLong(t);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "La población debe ser un número entero.",
                    "Dato no válido", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        pais.setNombre(txtNombre.getText());
        pais.setCapital(txtCapital.getText());
        pais.setRegion((String) cmbRegion.getSelectedItem());
        pais.setPoblacion(poblacion);
        pais.setMoneda(txtMoneda.getText());
        aceptado = true;
        dispose();
    }

    /** Devuelve el país llenado, o null si el usuario canceló. */
    public Pais mostrar() {
        setVisible(true);
        return aceptado ? pais : null;
    }
}
