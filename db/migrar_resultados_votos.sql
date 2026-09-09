USE scrum_poker_db;

-- Migracion no destructiva para guardar el resultado de cada votacion.
-- No elimina votos ni historias existentes.

SET @col_id_historia = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'votos' AND column_name = 'id_historia'
);
SET @sql_id_historia = IF(@col_id_historia = 0,
    'ALTER TABLE votos ADD COLUMN id_historia INT NULL AFTER id_usuario', 'SELECT 1');
PREPARE s_id_historia FROM @sql_id_historia;
EXECUTE s_id_historia;
DEALLOCATE PREPARE s_id_historia;

SET @cantidad_metricas = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'votos'
      AND column_name IN ('promedio', 'mediana', 'consenso')
);
SET @sql_metricas = IF(@cantidad_metricas = 3, 'SELECT 1',
    'ALTER TABLE votos ADD COLUMN promedio DECIMAL(10,2) NULL, ADD COLUMN mediana DECIMAL(10,2) NULL, ADD COLUMN consenso VARCHAR(2) NULL');
PREPARE s_metricas FROM @sql_metricas;
EXECUTE s_metricas;
DEALLOCATE PREPARE s_metricas;

-- Asociar votos antiguos con la historia activa de su sala.
UPDATE votos v
JOIN usuarios u ON u.id_usuario = v.id_usuario
JOIN usuarios moderador ON moderador.id_sala = v.id_sala
  AND moderador.rol = 'PRODUCT_OWNER_MODERADOR'
JOIN historias_usuario h ON h.id_usuario = moderador.id_usuario
  AND h.activa = TRUE
SET v.id_historia = h.id_historia
WHERE v.id_historia IS NULL;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_emitir_voto //
CREATE PROCEDURE sp_emitir_voto(IN p_id_sala INT, IN p_id_usuario INT, IN p_carta VARCHAR(10))
BEGIN
    DECLARE v_id_historia INT DEFAULT NULL;
    SELECT h.id_historia INTO v_id_historia
    FROM historias_usuario h
    JOIN usuarios u ON u.id_usuario = h.id_usuario
    WHERE u.id_sala = p_id_sala
      AND u.rol = 'PRODUCT_OWNER_MODERADOR'
      AND h.activa = TRUE
    LIMIT 1;

    INSERT INTO votos (id_sala, id_usuario, id_historia, carta, promedio, mediana, consenso)
    VALUES (p_id_sala, p_id_usuario, v_id_historia, p_carta, NULL, NULL, NULL)
    ON DUPLICATE KEY UPDATE
        id_historia = v_id_historia,
        carta = p_carta,
        promedio = NULL,
        mediana = NULL,
        consenso = NULL;
END //

DROP PROCEDURE IF EXISTS sp_revelar_cartas //
CREATE PROCEDURE sp_revelar_cartas(IN p_id_sala INT)
BEGIN
    DECLARE v_id_historia INT DEFAULT NULL;
    DECLARE v_cantidad INT DEFAULT 0;
    DECLARE v_inicio INT DEFAULT 0;
    DECLARE v_promedio DECIMAL(10,2) DEFAULT NULL;
    DECLARE v_mediana DECIMAL(10,2) DEFAULT NULL;
    DECLARE v_minimo DECIMAL(10,2) DEFAULT NULL;
    DECLARE v_maximo DECIMAL(10,2) DEFAULT NULL;

    SELECT h.id_historia INTO v_id_historia
    FROM historias_usuario h
    JOIN usuarios u ON u.id_usuario = h.id_usuario
    WHERE u.id_sala = p_id_sala
      AND u.rol = 'PRODUCT_OWNER_MODERADOR'
      AND h.activa = TRUE
    LIMIT 1;

    UPDATE salas SET cartas_reveladas = TRUE WHERE id_sala = p_id_sala;

    CREATE TEMPORARY TABLE tmp_revelar_votos (valor DECIMAL(10,2));
    INSERT INTO tmp_revelar_votos (valor)
    SELECT CAST(carta AS DECIMAL(10,2))
    FROM votos
    WHERE id_sala = p_id_sala
      AND id_historia = v_id_historia
      AND carta REGEXP '^[0-9]+([.][0-9]+)?$';

    SELECT COUNT(*), AVG(valor), MIN(valor), MAX(valor)
    INTO v_cantidad, v_promedio, v_minimo, v_maximo
    FROM tmp_revelar_votos;

    IF v_cantidad > 0 THEN
        SET v_inicio = FLOOR((v_cantidad - 1) / 2);
        SELECT AVG(valor) INTO v_mediana
        FROM (SELECT valor FROM tmp_revelar_votos ORDER BY valor LIMIT v_inicio, 2) AS centrales;
    END IF;

    UPDATE votos
    SET promedio = v_promedio,
        mediana = v_mediana,
        consenso = IF(v_cantidad > 0 AND v_minimo = v_maximo, 'SI', 'NO')
    WHERE id_sala = p_id_sala AND id_historia = v_id_historia;

    DROP TEMPORARY TABLE tmp_revelar_votos;
END //

DELIMITER ;

-- Verificacion: reemplaza 100 por el id_sala que quieras revisar.
SET @id_sala = 100;
SELECT id_voto, id_sala, id_usuario, id_historia, carta, promedio, mediana, consenso
FROM votos
WHERE id_sala = @id_sala
ORDER BY id_voto;
