package modelo;

/** Error al consumir la API pública. Lleva el código HTTP (0 = no hubo respuesta). */
public class ApiException extends Exception {

    private final int estado;

    public ApiException(int estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public ApiException(int estado, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.estado = estado;
    }

    public int getEstado() {
        return estado;
    }
}
