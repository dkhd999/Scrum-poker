USE scrum_poker_db;

DROP PROCEDURE IF EXISTS sp_obtener_metricas_votos;

DELIMITER //

CREATE PROCEDURE sp_obtener_metricas_votos(IN p_id_sala INT)
BEGIN
    DECLARE v_reveladas BOOLEAN DEFAULT FALSE;
    DECLARE v_cantidad INT DEFAULT 0;
    DECLARE v_inicio INT DEFAULT 0;
    DECLARE v_promedio DECIMAL(10, 2) DEFAULT NULL;
    DECLARE v_mediana DECIMAL(10, 2) DEFAULT NULL;
    DECLARE v_minimo DECIMAL(10, 2) DEFAULT NULL;
    DECLARE v_maximo DECIMAL(10, 2) DEFAULT NULL;

    SELECT cartas_reveladas INTO v_reveladas FROM salas WHERE id_sala = p_id_sala;
    CREATE TEMPORARY TABLE tmp_metricas_votos (valor DECIMAL(10, 2));
    INSERT INTO tmp_metricas_votos (valor)
    SELECT CAST(carta AS DECIMAL(10, 2)) FROM votos
    WHERE id_sala = p_id_sala AND carta REGEXP '^[0-9]+([.][0-9]+)?$';
    SELECT COUNT(*), AVG(valor), MIN(valor), MAX(valor)
    INTO v_cantidad, v_promedio, v_minimo, v_maximo
    FROM tmp_metricas_votos;
    IF v_cantidad > 0 THEN
        SET v_inicio = FLOOR((v_cantidad - 1) / 2);
        SELECT AVG(valor) INTO v_mediana
        FROM (SELECT valor FROM tmp_metricas_votos ORDER BY valor LIMIT v_inicio, 2) AS valores_centrales;
    END IF;
    SELECT IF(v_reveladas, v_promedio, NULL) AS promedio,
           IF(v_reveladas, v_mediana, NULL) AS mediana,
           IF(v_reveladas AND v_cantidad > 0, IF(v_minimo = v_maximo, 'SI', 'NO'), NULL) AS consenso;
    DROP TEMPORARY TABLE tmp_metricas_votos;
END //

DELIMITER ;

-- Diagnostico: sustituye 1 por el id_sala que quieras probar.
SET @id_sala = 1;
SELECT id_sala, cartas_reveladas FROM salas WHERE id_sala = @id_sala;
SELECT id_usuario, carta FROM votos WHERE id_sala = @id_sala;
CALL sp_obtener_metricas_votos(@id_sala);
