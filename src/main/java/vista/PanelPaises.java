package vista;

import controlador.PaisController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import modelo.Pais;
import modelo.Regiones;

/**
 * Actividades 2, 3 y 4: CRUD completo (crear, leer, actualizar, eliminar),
 * países en un JTable y búsqueda por nombre / filtro por región.
 */
public class PanelPaises extends PanelAnimado {

    private final JFrame ventana;
    private final PaisController controller;
    private final TablaPaises tabla = new TablaPaises();
    private final JTextField txtBuscar = new JTextField();
    private final JLabel lblConteo = new JLabel(" ");
    private final BotonModerno btnEditar;
    private final BotonModerno btnEliminar;
    private final Timer debounce;
    private String regionActiva = null;           // null = todas

    public PanelPaises(JFrame ventana, PaisController controller) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        BotonModerno btnNuevo = new BotonModerno("Nuevo", Iconos.de(Iconos.Tipo.MAS, 18, Color.WHITE),
                BotonModerno.Estilo.PRIMARIO);
        btnEditar = new BotonModerno("Editar", Iconos.de(Iconos.Tipo.EDITAR, 18, Color.WHITE),
                BotonModerno.Estilo.SECUNDARIO);
        btnEliminar = new BotonModerno("Eliminar", Iconos.de(Iconos.Tipo.BORRAR, 18, Color.WHITE),
                BotonModerno.Estilo.PELIGRO);
        BotonModerno btnRefrescar = new BotonModerno("", Iconos.de(Iconos.Tipo.REFRESCAR, 18, Color.WHITE),
                BotonModerno.Estilo.SECUNDARIO);
        btnRefrescar.setToolTipText("Recargar la lista");
        btnNuevo.addActionListener(e -> nuevo());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnRefrescar.addActionListener(e -> cargar());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(btnRefrescar);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);
        acciones.add(btnNuevo);
        add(Cabecera.crear("Países",
                "Administra los países guardados: crea, consulta, actualiza y elimina. Filtra por región o busca por nombre.",
                acciones), BorderLayout.NORTH);

        // ---- filtros
        Tarjeta filtros = new Tarjeta(new BorderLayout(16, 0));
        filtros.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por nombre…");
        txtBuscar.putClientProperty("JTextField.leadingIcon", Iconos.de(Iconos.Tipo.BUSCAR, 16, Tema.SUAVE));
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        txtBuscar.setColumns(18);
        debounce = new Timer(280, e -> cargar());
        debounce.setRepeats(false);
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                debounce.restart();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                debounce.restart();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                debounce.restart();
            }
        });

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        ChipRegion todas = new ChipRegion("Todas", Tema.ACENTO2);
        todas.setSelected(true);
        todas.addActionListener(e -> filtrarRegion(null));
        grupo.add(todas);
        chips.add(todas);
        for (String region : Regiones.LISTA) {
            ChipRegion chip = new ChipRegion(region, Tema.colorRegion(region));
            chip.addActionListener(e -> filtrarRegion(region));
            grupo.add(chip);
            chips.add(chip);
        }
        filtros.add(txtBuscar, BorderLayout.WEST);
        filtros.add(chips, BorderLayout.CENTER);

        // ---- tabla
        Tarjeta contenedor = new Tarjeta(new BorderLayout());
        contenedor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenedor.add(tabla.envolver(), BorderLayout.CENTER);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editar();
                }
            }
        });

        lblConteo.setFont(Tema.F_NORMAL);
        lblConteo.setForeground(Tema.SUAVE);
        lblConteo.setBorder(BorderFactory.createEmptyBorder(12, 4, 0, 0));

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(contenedor, BorderLayout.CENTER);
        centro.add(lblConteo, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
    }

    @Override
    public void entrar() {
        super.entrar();
        cargar();
    }

    private void filtrarRegion(String region) {
        regionActiva = region;
        cargar();
    }

    private void cargar() {
        final String texto = txtBuscar.getText();
        final String region = regionActiva;
        Async.ejecutar(() -> controller.listar(texto, region),
                lista -> {
                    tabla.setPaises(lista);
                    String filtro = region == null ? "" : " en " + region;
                    lblConteo.setText("Mostrando " + lista.size() + (lista.size() == 1 ? " país" : " países") + filtro
                            + ". Doble clic en una fila para editarla.");
                },
                ex -> {
                    lblConteo.setText(" ");
                    Toast.mostrar(ventana, Async.mensaje(ex), Toast.Tipo.ERROR);
                });
    }

    // ---------------------------------------------------------------- CRUD
    private void nuevo() {
        Pais p = new DialogoPais(ventana, null).mostrar();
        if (p == null) {
            return;
        }
        Async.ejecutar(() -> {
            controller.crear(p);
            return p;
        }, creado -> {
            Toast.mostrar(ventana, "País \"" + creado.getNombre() + "\" creado.", Toast.Tipo.OK);
            cargar();
        }, ex -> Toast.mostrar(ventana, Async.mensaje(ex), Toast.Tipo.ERROR));
    }

    private void editar() {
        Pais seleccionado = tabla.paisSeleccionado();
        if (seleccionado == null) {
            Toast.mostrar(ventana, "Selecciona un país de la tabla para editarlo.", Toast.Tipo.AVISO);
            return;
        }
        Pais copia = new Pais(seleccionado.getNombre(), seleccionado.getCapital(), seleccionado.getRegion(),
                seleccionado.getPoblacion(), seleccionado.getMoneda());
        copia.setId(seleccionado.getId());
        Pais editado = new DialogoPais(ventana, copia).mostrar();
        if (editado == null) {
            return;
        }
        Async.ejecutar(() -> {
            controller.actualizar(editado);
            return editado;
        }, ok -> {
            Toast.mostrar(ventana, "País \"" + ok.getNombre() + "\" actualizado.", Toast.Tipo.OK);
            cargar();
        }, ex -> Toast.mostrar(ventana, Async.mensaje(ex), Toast.Tipo.ERROR));
    }

    private void eliminar() {
        Pais seleccionado = tabla.paisSeleccionado();
        if (seleccionado == null) {
            Toast.mostrar(ventana, "Selecciona un país de la tabla para eliminarlo.", Toast.Tipo.AVISO);
            return;
        }
        int r = JOptionPane.showConfirmDialog(ventana,
                "¿Eliminar \"" + seleccionado.getNombre() + "\" de la base de datos?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        Async.ejecutar(() -> {
            controller.eliminar(seleccionado.getId());
            return seleccionado;
        }, ok -> {
            Toast.mostrar(ventana, "País \"" + ok.getNombre() + "\" eliminado.", Toast.Tipo.OK);
            cargar();
        }, ex -> Toast.mostrar(ventana, Async.mensaje(ex), Toast.Tipo.ERROR));
    }
}
