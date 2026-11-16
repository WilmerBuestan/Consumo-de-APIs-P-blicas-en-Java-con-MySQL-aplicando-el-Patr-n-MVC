package modelo;

/**
 * Regiones que usa la aplicación. La API devuelve los nombres en inglés
 * (Americas, Europe, Asia, Africa, Oceania); aquí se guardan en español,
 * como pide la Actividad 4 del taller.
 */
public final class Regiones {

    public static final String[] LISTA = {"América", "Europa", "Asia", "África", "Oceanía"};

    private Regiones() {
    }

    public static String aEspanol(String regionApi) {
        if (regionApi == null || regionApi.isBlank()) {
            return "Sin región";
        }
        switch (regionApi.trim().toLowerCase()) {
            case "americas":
            case "america":
                return "América";
            case "europe":
                return "Europa";
            case "asia":
                return "Asia";
            case "africa":
                return "África";
            case "oceania":
                return "Oceanía";
            case "antarctic":
            case "antarctica":
                return "Antártida";
            default:
                return regionApi.trim();
        }
    }
}
