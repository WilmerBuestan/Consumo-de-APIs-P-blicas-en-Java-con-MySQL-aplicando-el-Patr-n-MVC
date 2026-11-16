package vista;

import controlador.PaisController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import modelo.Pais;

/** Actividad 5: reportes estadísticos (por región, país más poblado, región con más países). */
public class PanelReportes extends PanelAnimado {

    private final JFrame ventana;
    private final PaisController controller;
    private final TarjetaStat statTotal;
    private final TarjetaStat statPoblacion;
    private final TarjetaStat statMasPoblado;
    private final TarjetaStat statRegionTop;
    private final GraficoBarras grafico = new GraficoBarras();

    public PanelReportes(JFrame ventana, PaisController controller) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        BotonModerno btnActualizar = new BotonModerno("Actualizar", Iconos.de(Iconos.Tipo.REFRESCAR, 18, Color.WHITE),
                BotonModerno.Estilo.SECUNDARIO);
        btnActualizar.addActionListener(e -> cargar());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        acciones.setOpaque(false);
        acciones.add(btnActualizar);
        add(Cabecera.crear("Reportes estadísticos",
                "Resumen calculado con consultas SQL sobre los países guardados.", acciones), BorderLayout.NORTH);

        JPanel fila = new JPanel(new GridLayout(1, 4, 16, 0));
        fila.setOpaque(false);
        statTotal = new TarjetaStat("Países registrados", Tema.ACENTO, Iconos.de(Iconos.Tipo.MUNDO, 20, Tema.ACENTO));
        statPoblacion = new TarjetaStat("Población total", Tema.ACENTO2, Iconos.de(Iconos.Tipo.TABLA, 20, Tema.ACENTO2));
        statMasPoblado = new TarjetaStat("País más poblado", Tema.AVISO, Iconos.de(Iconos.Tipo.GRAFICO, 20, Tema.AVISO));
        statRegionTop = new TarjetaStat("Región líder", Tema.OK, Iconos.de(Iconos.Tipo.CHECK, 20, Tema.OK));
        fila.add(statTotal);
        fila.add(statPoblacion);
        fila.add(statMasPoblado);
        fila.add(statRegionTop);

        Tarjeta tarjetaGrafico = new Tarjeta(new BorderLayout(0, 8));
        tarjetaGrafico.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));
        JLabel titulo = new JLabel("Número de países por región");
        titulo.setFont(Tema.F_SECCION);
        titulo.setForeground(Tema.TEXTO);
        tarjetaGrafico.add(titulo, BorderLayout.NORTH);
        tarjetaGrafico.add(grafico, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 18));
        centro.setOpaque(false);
        centro.add(fila, BorderLayout.NORTH);
        centro.add(tarjetaGrafico, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    @Override
    public void entrar() {
        super.entrar();
        cargar();
    }

    private void cargar() {
        Async.ejecutar(controller::estadisticas, est -> {
            statTotal.setNumero(est.totalPaises(), Tema::miles, "en la base de datos");
            statPoblacion.setNumero(est.poblacionTotal(), Tema::miles, "habitantes");
            Pais top = est.masPoblado();
            if (top == null) {
                statMasPoblado.setTexto("—", "sin datos");
            } else {
                statMasPoblado.setTexto(top.getNombre(),
                        Tema.miles(top.getPoblacion()) + " habitantes");
            }
            statRegionTop.setTexto(est.regionTop(), est.regionTopCantidad() + " países");
            grafico.setDatos(est.porRegion());
        }, ex -> Toast.mostrar(ventana, Async.mensaje(ex), Toast.Tipo.ERROR));
    }
}
