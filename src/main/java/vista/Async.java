package vista;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;
import modelo.ApiException;

/** Ejecuta tareas lentas (API, base de datos) fuera del hilo de la interfaz para que nunca se congele. */
public final class Async {

    private Async() {
    }

    public static <T> void ejecutar(Callable<T> tarea, Consumer<T> alTerminar, Consumer<Exception> alFallar) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            @Override
            protected void done() {
                try {
                    alTerminar.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    alFallar.accept(causa instanceof Exception ? (Exception) causa : new Exception(causa));
                }
            }
        }.execute();
    }

    /** Mensaje entendible para el usuario según el tipo de error. */
    public static String mensaje(Exception e) {
        if (e instanceof ApiException || e instanceof IllegalArgumentException) {
            return e.getMessage();
        }
        if (e instanceof SQLException) {
            return "Base de datos: " + e.getMessage();
        }
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
