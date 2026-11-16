# 🌍 Consumo de APIs Públicas en Java con MySQL · Patrón MVC

Aplicación de escritorio (Java Swing + FlatLaf) que **consume la API REST Countries**, convierte el JSON a objetos Java
con **Gson**, guarda los países en **MySQL / MariaDB** mediante un **DAO** y organiza todo con el patrón **MVC**.

```
+------------------+      +------------------+      +----------------------------+
|      VISTA       | ---> |   CONTROLADOR    | ---> |           MODELO           |
|  FrmPais + paneles|      |  PaisController |      | Pais · ConsumoAPI · PaisDAO |
|  (Swing/FlatLaf) | <--- |  valida y coordina| <--- |  Conexion  ──> MySQL        |
+------------------+      +------------------+      +----------------------------+
                                                            │
                                                            └──> API REST Countries (JSON)
```

## ✅ Qué cumple del taller

| Actividad | Dónde está |
|-----------|-----------|
| Caso principal: botón **IMPORTAR PAÍSES** (API → JSON → objetos → MySQL → mensaje) | Pantalla *Importar países* |
| 1. Consultar un país por nombre | Pantalla *Consultar API* |
| 2. CRUD completo sobre la tabla `pais` | Pantalla *Países (CRUD)* |
| 3. Mostrar los países en un `JTable` | Pantalla *Países (CRUD)* |
| 4. Búsqueda por región (América, Europa, Asia, África, Oceanía) | Chips de la pantalla *Países* |
| 5. Reportes: países por región, país más poblado, región con más países | Pantalla *Reportes* |

Extras: barra de progreso animada, importación sin duplicados (si el país existe se actualiza), tareas en segundo plano
(la interfaz nunca se congela), respaldo con los datos guardados si la API falla, notificaciones y gráfico animado.

## 📁 Estructura

```
src/main/java
├── Main.java
├── modelo        Pais · Conexion · ConsumoAPI · PaisDAO · Regiones · Configuracion · ApiException
├── controlador   PaisController
└── vista         FrmPais · PanelImportar · PanelConsulta · PanelPaises · PanelReportes · DialogoPais · componentes
sql/api_paises.sql          script de la base de datos (también se ejecuta solo al iniciar)
config/*.properties.example plantillas de configuración
```

## ⚙️ Requisitos

JDK 17+ · Maven · NetBeans 21+ (opcional) · MySQL Server 8+ o MariaDB · una API key de <https://restcountries.com>

## 🔑 Configuración (los secretos NO se suben a GitHub)

1. Copia `config/api.properties.example` → `config/api.properties` y pon tu API key.
2. Copia `config/db.properties.example` → `config/db.properties` y ajusta usuario, clave y URL:
   - MySQL: `jdbc:mysql://localhost:3306/api_paises`
   - MariaDB: `jdbc:mariadb://localhost:3307/api_paises`

Ambos archivos están en `.gitignore`. La base `api_paises` y la tabla `pais` se crean solas al abrir la aplicación
(o ejecuta `sql/api_paises.sql` a mano).

## ▶️ Ejecutar

- **NetBeans:** *File → Open Project* → botón derecho → *Run*.
- **Maven:** `mvn compile exec:java`
- **JAR ejecutable:** `mvn package` y luego `java -jar target/consumo-apis-paises-1.0.0.jar` (ejecútalo desde la carpeta del proyecto para que encuentre `config/`).

## 🔌 Nota sobre la API

El PDF del taller usa `https://restcountries.com/v3.1/all`. REST Countries pasó a la **versión 5**, que exige API key
(`Authorization: Bearer <key>`), pagina de 100 en 100 y anida los campos:

| Dato | v3.1 (PDF) | v5 (esta app) |
|------|-----------|---------------|
| Nombre | `name.common` | `names.common` |
| Capital | `capital[0]` | `capitals[0].name` |
| Moneda | `currencies{USD:{name}}` | `currencies[{code,name}]` |
| Todos | `/v3.1/all` | `/countries/v5?limit=100&offset=N` |
| Por nombre | `/v3.1/name/ecuador` | `/countries/v5/names.common?q=ecuador` |

El controlador entiende también el formato antiguo, por si cambia el servicio.

## 👤 Autor

Wilmer Buestán · Ingeniería de Software · Universidad de las Fuerzas Armadas ESPE
