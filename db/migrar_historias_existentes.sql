USE scrum_poker_db;

-- Migracion no destructiva para bases creadas con la version anterior.
-- No elimina tablas, historias ni votos.

SET @columna_activa = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'historias_usuario'
      AND column_name = 'activa'
);
SET @sql_add_columna = IF(
    @columna_activa = 0,
    'ALTER TABLE historias_usuario ADD COLUMN activa BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE sentencia_add_columna FROM @sql_add_columna;
EXECUTE sentencia_add_columna;
DEALLOCATE PREPARE sentencia_add_columna;

-- La version anterior tenia id_usuario como UNIQUE. Se elimina solo ese indice
-- para permitir varias historias por moderador.
SET @nombre_fk = (
        SELECT kcu.constraint_name
        FROM information_schema.key_column_usage kcu
        WHERE kcu.table_schema = DATABASE()
            AND kcu.table_name = 'historias_usuario'
            AND kcu.column_name = 'id_usuario'
            AND kcu.referenced_table_name = 'usuarios'
        LIMIT 1
);
SET @sql_drop_fk = IF(
        @nombre_fk IS NULL,
        'SELECT 1',
        CONCAT('ALTER TABLE historias_usuario DROP FOREIGN KEY `', @nombre_fk, '`')
);
PREPARE sentencia_drop_fk FROM @sql_drop_fk;
EXECUTE sentencia_drop_fk;
DEALLOCATE PREPARE sentencia_drop_fk;

SET @indice_unico = (
    SELECT s.index_name
    FROM information_schema.statistics s
    WHERE s.table_schema = DATABASE()
      AND s.table_name = 'historias_usuario'
      AND s.column_name = 'id_usuario'
      AND s.non_unique = 0
      AND s.index_name <> 'PRIMARY'
    LIMIT 1
);
SET @sql_drop_indice = IF(
    @indice_unico IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE historias_usuario DROP INDEX `', @indice_unico, '`')
);
PREPARE sentencia_drop_indice FROM @sql_drop_indice;
EXECUTE sentencia_drop_indice;
DEALLOCATE PREPARE sentencia_drop_indice;

SET @fk_restaurada = (
    SELECT COUNT(*)
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'historias_usuario'
      AND column_name = 'id_usuario'
      AND referenced_table_name = 'usuarios'
);
SET @sql_add_fk = IF(
    @fk_restaurada = 0,
    'ALTER TABLE historias_usuario ADD CONSTRAINT fk_historias_usuario_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE',
    'SELECT 1'
);
PREPARE sentencia_add_fk FROM @sql_add_fk;
EXECUTE sentencia_add_fk;
DEALLOCATE PREPARE sentencia_add_fk;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_preparar_nueva_historia //
CREATE PROCEDURE sp_preparar_nueva_historia(IN p_id_sala INT, IN p_id_usuario INT)
BEGIN
    DELETE FROM votos WHERE id_sala = p_id_sala;
    UPDATE salas SET cartas_reveladas = FALSE WHERE id_sala = p_id_sala;
    UPDATE historias_usuario SET activa = FALSE
    WHERE id_usuario = p_id_usuario AND activa = TRUE;
END //

DROP PROCEDURE IF EXISTS sp_crear_historia //
CREATE PROCEDURE sp_crear_historia(
    IN p_id_usuario INT,
    IN p_titulo VARCHAR(255),
    IN p_descripcion TEXT,
    IN p_prioridad VARCHAR(20),
    IN p_puntos_estimados VARCHAR(10)
)
BEGIN
    UPDATE historias_usuario SET activa = FALSE
    WHERE id_usuario = p_id_usuario AND activa = TRUE;
    INSERT INTO historias_usuario (id_usuario, titulo, descripcion, prioridad, puntos_estimados, activa)
    VALUES (p_id_usuario, p_titulo, p_descripcion, p_prioridad, p_puntos_estimados, TRUE);
END //

DROP PROCEDURE IF EXISTS sp_actualizar_historia //
CREATE PROCEDURE sp_actualizar_historia(
    IN p_id_historia INT,
    IN p_id_usuario INT,
    IN p_titulo VARCHAR(255),
    IN p_descripcion TEXT,
    IN p_prioridad VARCHAR(20),
    IN p_puntos_estimados VARCHAR(10)
)
BEGIN
    UPDATE historias_usuario
    SET titulo = p_titulo,
        descripcion = p_descripcion,
        prioridad = p_prioridad,
        puntos_estimados = p_puntos_estimados
    WHERE id_historia = p_id_historia
      AND id_usuario = p_id_usuario
      AND activa = TRUE;
END //

DROP PROCEDURE IF EXISTS sp_obtener_historial_historias //
CREATE PROCEDURE sp_obtener_historial_historias(IN p_id_sala INT)
BEGIN
    SELECT h.id_historia, h.titulo, h.descripcion, h.prioridad,
           h.puntos_estimados, h.activa, h.fecha_creacion
    FROM historias_usuario h
    JOIN usuarios u ON u.id_usuario = h.id_usuario
    WHERE u.id_sala = p_id_sala
    ORDER BY h.id_historia;
END //

DROP PROCEDURE IF EXISTS sp_obtener_votos //
CREATE PROCEDURE sp_obtener_votos(IN p_id_sala INT)
BEGIN
    SELECT
        u.id_usuario,
        u.nickname,
        u.rol,
        v.carta,
        s.cartas_reveladas,
        h.id_historia,
        h.titulo AS historia_titulo,
        h.descripcion AS historia_descripcion,
        h.prioridad AS historia_prioridad,
        h.puntos_estimados AS historia_puntos
    FROM usuarios u
    JOIN salas s ON u.id_sala = s.id_sala
    LEFT JOIN votos v ON u.id_usuario = v.id_usuario AND v.id_sala = s.id_sala
    LEFT JOIN usuarios u_po ON u_po.id_sala = s.id_sala
        AND u_po.rol = 'PRODUCT_OWNER_MODERADOR'
        AND u_po.estado = 'ACTIVO'
    LEFT JOIN historias_usuario h ON h.id_usuario = u_po.id_usuario AND h.activa = TRUE
    WHERE u.id_sala = p_id_sala AND u.estado = 'ACTIVO';
END //

DROP PROCEDURE IF EXISTS sp_obtener_metricas_votos //
CREATE PROCEDURE sp_obtener_metricas_votos(IN p_id_sala INT)
BEGIN
    DECLARE v_reveladas BOOLEAN DEFAULT FALSE;
    DECLARE v_cantidad INT DEFAULT 0;
    DECLARE v_inicio INT DEFAULT 0;
    DECLARE v_promedio DECIMAL(10, 2) DEFAULT NULL;
    DECLARE v_mediana DECIMAL(10, 2) DEFAULT NULL;
    DECLARE v_minimo DECIMAL(10, 2) DEFAULT NULL;
    DECLARE v_maximo DECIMAL(10, 2) DEFAULT NULL;

    SELECT cartas_reveladas INTO v_reveladas
    FROM salas
    WHERE id_sala = p_id_sala;

    CREATE TEMPORARY TABLE tmp_metricas_votos (valor DECIMAL(10, 2));
    INSERT INTO tmp_metricas_votos (valor)
    SELECT CAST(carta AS DECIMAL(10, 2))
    FROM votos
    WHERE id_sala = p_id_sala
      AND carta REGEXP '^[0-9]+([.][0-9]+)?$';

    SELECT COUNT(*), AVG(valor), MIN(valor), MAX(valor)
    INTO v_cantidad, v_promedio, v_minimo, v_maximo
    FROM tmp_metricas_votos;

    IF v_cantidad > 0 THEN
        SET v_inicio = FLOOR((v_cantidad - 1) / 2);
        SELECT AVG(valor) INTO v_mediana
        FROM (
            SELECT valor
            FROM tmp_metricas_votos
            ORDER BY valor
            LIMIT v_inicio, 2
        ) AS valores_centrales;
    END IF;

    SELECT
        IF(v_reveladas, v_promedio, NULL) AS promedio,
        IF(v_reveladas, v_mediana, NULL) AS mediana,
        IF(v_reveladas AND v_cantidad > 0,
           IF(v_minimo = v_maximo, 'SI', 'NO'), NULL) AS consenso;

    DROP TEMPORARY TABLE tmp_metricas_votos;
END //

DELIMITER ;

-- Verificacion de la migracion.
SELECT routine_name
FROM information_schema.routines
WHERE routine_schema = DATABASE()
  AND routine_name IN (
      'sp_preparar_nueva_historia',
      'sp_crear_historia',
      'sp_actualizar_historia',
            'sp_obtener_historial_historias',
            'sp_obtener_votos',
            'sp_obtener_metricas_votos'
  )
ORDER BY routine_name;

SELECT id_historia, id_usuario, titulo, activa
FROM historias_usuario
ORDER BY id_historia;
