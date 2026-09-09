USE scrum_poker_db;

-- Tablas principales.
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('salas', 'usuarios', 'historias_usuario', 'votos')
ORDER BY table_name;

-- Cantidad y listado de procedimientos almacenados.
SELECT COUNT(*) AS cantidad_procedimientos
FROM information_schema.routines
WHERE routine_schema = DATABASE()
  AND routine_type = 'PROCEDURE';

SELECT routine_name
FROM information_schema.routines
WHERE routine_schema = DATABASE()
  AND routine_type = 'PROCEDURE'
ORDER BY routine_name;

-- No debe devolver filas: una sola historia activa por usuario.
SELECT id_usuario, COUNT(*) AS historias_activas
FROM historias_usuario
WHERE activa = TRUE
GROUP BY id_usuario
HAVING COUNT(*) > 1;

-- Resumen de los datos existentes.
SELECT
    (SELECT COUNT(*) FROM salas) AS salas,
    (SELECT COUNT(*) FROM usuarios) AS usuarios,
    (SELECT COUNT(*) FROM historias_usuario) AS historias,
    (SELECT COUNT(*) FROM historias_usuario WHERE activa = TRUE) AS historias_activas,
    (SELECT COUNT(*) FROM votos) AS votos;
