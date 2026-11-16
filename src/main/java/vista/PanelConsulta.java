package vista;

import controlador.PaisController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import modelo.Pais;

/**
 * Actividad 1: consultar un país por nombre directamente en la API
 * (GET .../countries/v5/names.common?q=ecuador) y, si se desea, guardarlo en la base de datos.
 */
public class PanelConsulta extends PanelAnimado {

    private final JFrame ventana;
    private final PaisController controller;
    private final JTextField txtNombre = new JTextField();
    private final TablaPaises tabla = new TablaPaises();
    private final JLabel lblInfo = new JLabel("Escribe un nombre y presiona Consultar.");
    private final BotonModerno btnConsultar;
    private final BotonModerno btnGuardarSel;
    private final BotonModerno btnGuardarTodos;

    public PanelConsulta(JFrame ventana, PaisController controller) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        add(Cabecera.crear("Consultar país en la API",
                "Busca por nombre en REST Countries (por ejemplo: ecuador, peru, ger). La búsqueda encuentra todos los países que contengan el texto.",
                null), BorderLayout.NORTH);

        // ---- barra de búsqueda
        Tarjeta barra = new Tarjeta(new BorderLayout(12, 0));
        barra.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        txtNombre.putClientProperty("JTextField.placeholderText", "Nombre del país…  (ej. ecuador)");
        txtNombre.putClientProperty("JTextField.leadingIcon", Iconos.de(Iconos.Tipo.BUSCAR, 16, Tema.SUAVE));
        txtNombre.putClientProperty("JTextField.showClearButton", true);
        txtNombre.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
        txtNombre.addActionListener(e -> consultar());
        btnConsultar = new BotonModerno("Consultar API", Iconos.de(Iconos.Tipo.BUSCAR, 18, Color.WHITE),
                BotonModerno.Estilo.PRIMARIO);
        btnConsultar.addActionListener(e -> consultar());
        barra.add(txtNombre, BorderLayout.CENTER);
        barra.add(btnConsultar, BorderLayout.EAST);

        // ---- resultados
        Tarjeta resultados = new Tarjeta(new BorderLayout());
        resultados.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultados.add(tabla.envolver(), BorderLayout.CENTER);

        // ---- pie con acciones
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        lblInfo.setFont(Tema.F_NORMAL);
        lblInfo.setForeground(Tema.SUAVE);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        btnGuardarSel = new BotonModerno("Guardar seleccionados", Iconos.de(Iconos.Tipo.GUARDAR, 18, Color.WHITE),
                BotonModerno.Estilo.SECUNDARIO);
        btnGuardarTodos = new BotonModerno("Guardar todos", Iconos.de(Iconos.Tipo.GUARDAR, 18, Color.WHITE),
                BotonModerno.Estilo.EXITO);
        btnGuardarSel.setEnabled(false);
        btnGuardarTodos.setEnabled(false);
        btnGuardarSel.addActionListener(e -> guardar(tabla.paisesSeleccionados()));
        btnGuardarTodos.addActionListener(e -> guardar(tabla.todos()));
        botones.add(btnGuardarSel);
        botones.add(btnGuardarTodos);
        pie.add(lblInfo, BorderLayout.WEST);
        pie.add(botones, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        centro.add(barra, BorderLayout.NORTH);
        centro.add(resultados, BorderLayout.CENTER);
        centro.add(pie, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
    }

    @Override
    public void entrar() {
        super.entrar();
        txtNombre.requestFocusInWindow();
    }

    private void consultar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            Toast.mostrar(ventana, "Escribe el nombre de un país.", Toast.Tipo.AVISO);
            return;
        }
        btnConsultar.setEnabled(false);
        lblInfo.setForeground(Tema.SUAVE);
        lblInfo.setText("Consultando la API…");
        Async.ejecutar(() -> controller.consultarPorNombre(nombre),
                lista -> {
                    btnConsultar.setEnabled(true);
                    tabla.setPaises(lista);
                    btnGuardarTodos.setEnabled(!lista.isEmpty());
                    btnGuardarSel.setEnabled(!lista.isEmpty());
                    if (lista.isEmpty()) {
                        lblInfo.setText("Sin resultados para \"" + nombre + "\".");
                        Toast.mostrar(ventana, "No se encontró ningún país con ese nombre.", Toast.Tipo.INFO);
                    } else {
                        lblInfo.setText(lista.size() + (lista.size() == 1 ? " país encontrado" : " países encontrados")
                                + " para \"" + nombre + "\".");
                        if (lista.size() == 1) {
                            tabla.setRowSelectionInterval(0, 0);
                        }
                    }
                },
                ex -> {
                    btnConsultar.setEnabled(true);
                    lblInfo.setForeground(Tema.ERROR);
                    lblInfo.setText("No se pudo consultar la API.");
                    Toast.mostrar(ventana, Async.mensaje(ex), Toast.Tipo.ERROR);
                });
    }

    private void guardar(List<Pais> paises) {
        if (paises.isEmpty()) {
            Toast.mostrar(ventana, "Selecciona al menos una fila.", Toast.Tipo.AVISO);
            return;
        }
        Async.ejecutar(() -> controller.guardarDesdeApi(paises),
                r -> Toast.mostrar(ventana, "Guardado en la base de datos: " + r[0] + " nuevo(s), "
                        + r[1] + " actualizado(s).", Toast.Tipo.OK),
                ex -> Toast.mostrar(ventana, Async.mensaje(ex), Toast.Tipo.ERROR));
    }
}
