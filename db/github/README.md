# Base de datos de Scrum Poker

Este directorio contiene los archivos SQL listos para publicar y ejecutar en GitHub.

## Instalacion nueva

Ejecuta `01_instalar_base_completa.sql` desde MySQL Workbench o desde el cliente `mysql`.
Ese archivo carga `../scrum_poker_db.sql`, que contiene:

- La base de datos `scrum_poker_db`.
- Las tablas `salas`, `usuarios`, `historias_usuario` y `votos`.
- Claves primarias, foraneas, indices y restricciones.
- Los 16 procedimientos almacenados utilizados por la aplicacion.

El script de instalacion elimina y recrea las tablas de la base indicada. Usalo solo para una instalacion nueva o despues de respaldar los datos existentes.

## Migraciones de una base existente

Si ya tienes datos, no ejecutes la instalacion completa. Ejecuta desde la carpeta `db/`, segun corresponda:

1. `migrar_historias_existentes.sql`
2. `migrar_resultados_votos.sql`
3. `actualizar_metricas_mysql.sql`

Las migraciones deben probarse primero sobre una copia de la base de datos.

## Verificacion

Despues de instalar o migrar, ejecuta `02_verificar_base.sql`. El resultado esperado incluye:

- Las cuatro tablas principales.
- 16 procedimientos almacenados.
- Cero historias activas duplicadas por usuario.

## Conexion de la aplicacion

La aplicacion Java se conecta por defecto a `scrum_poker_db` en `localhost` con el usuario `root`. Revisa las credenciales de `ConexionBDD.java` antes de ejecutar la aplicacion.

## Requisitos

- MySQL 8.0 o superior.
- Permisos para crear la base de datos, tablas y procedimientos almacenados.
- Ejecutar el script con un cliente que soporte `DELIMITER`.
