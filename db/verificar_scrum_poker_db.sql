USE scrum_poker_db;

-- 1. Tablas principales
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('salas', 'usuarios', 'historias_usuario', 'votos')
ORDER BY table_name;

-- 2. Estructura de historias: historial y una historia activa por usuario
SELECT column_name, column_type, is_nullable, column_key
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'historias_usuario'
ORDER BY ordinal_position;

SELECT index_name, column_name, non_unique
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'historias_usuario'
ORDER BY index_name, seq_in_index;

-- 3. Debe devolver 16 procedimientos almacenados
SELECT COUNT(*) AS cantidad_procedimientos
FROM information_schema.routines
WHERE routine_schema = DATABASE()
  AND routine_type = 'PROCEDURE';

SELECT routine_name
FROM information_schema.routines
WHERE routine_schema = DATABASE()
  AND routine_type = 'PROCEDURE'
ORDER BY routine_name;

-- 3b. Debe mostrar la version actual del SP de metricas
SELECT routine_name, routine_definition
FROM information_schema.routines
WHERE routine_schema = DATABASE()
  AND routine_name = 'sp_obtener_metricas_votos';

-- 3c. La sala debe estar revelada y tener votos numericos para obtener metricas
SELECT s.id_sala, s.cartas_reveladas,
       COUNT(v.id_voto) AS votos,
       SUM(v.carta REGEXP '^[0-9]+([.][0-9]+)?$') AS votos_numericos
FROM salas s
LEFT JOIN votos v ON v.id_sala = s.id_sala
GROUP BY s.id_sala, s.cartas_reveladas
ORDER BY s.id_sala;

-- 4. No debe devolver filas: solo puede existir una historia activa por moderador
SELECT id_usuario, COUNT(*) AS historias_activas
FROM historias_usuario
WHERE activa = TRUE
GROUP BY id_usuario
HAVING COUNT(*) > 1;

-- 5. Resumen persistido de historias y votos
SELECT
    (SELECT COUNT(*) FROM salas) AS salas,
    (SELECT COUNT(*) FROM usuarios) AS usuarios,
    (SELECT COUNT(*) FROM historias_usuario) AS historias,
    (SELECT COUNT(*) FROM historias_usuario WHERE activa = TRUE) AS historias_activas,
    (SELECT COUNT(*) FROM votos) AS votos;

-- 6. Ejecutar los SP de lectura sobre la primera sala disponible.
-- Si no existen salas, estos CALL no devolveran datos.
SET @id_sala_verificacion = (SELECT MIN(id_sala) FROM salas);

CALL sp_obtener_historial_historias(@id_sala_verificacion);
CALL sp_obtener_metricas_votos(@id_sala_verificacion);
CALL sp_obtener_votos(@id_sala_verificacion);
