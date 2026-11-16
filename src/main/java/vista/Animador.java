package vista;

import java.util.function.DoubleConsumer;
import javax.swing.Timer;

/** Animaciones suaves (easing) sobre el hilo de Swing. */
public final class Animador {

    private Animador() {
    }

    /** Llama a frame(x) con x de 0 a 1 (con aceleración suave) durante 'ms' milisegundos. */
    public static Timer animar(int ms, DoubleConsumer frame, Runnable fin) {
        final long inicio = System.nanoTime();
        final Timer timer = new Timer(15, null);
        timer.addActionListener(e -> {
            double x = Math.min(1.0, (System.nanoTime() - inicio) / (ms * 1_000_000.0));
            frame.accept(suave(x));
            if (x >= 1.0) {
                timer.stop();
                if (fin != null) {
                    fin.run();
                }
            }
        });
        timer.setCoalesce(true);
        timer.start();
        return timer;
    }

    /** easeOutCubic */
    public static double suave(double x) {
        return 1 - Math.pow(1 - x, 3);
    }
}
