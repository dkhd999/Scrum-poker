-- =====================================================
-- Creación de la Base de Datos
-- =====================================================
CREATE DATABASE IF NOT EXISTS scrum_poker_db;
USE scrum_poker_db;

-- Limpieza preventiva
DROP TABLE IF EXISTS votos;
DROP TABLE IF EXISTS historias_usuario;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS salas;

-- =====================================================
-- Estructura de Tablas (DDL)
-- =====================================================

-- 1. Tabla de Salas
CREATE TABLE salas (
    id_sala INT AUTO_INCREMENT PRIMARY KEY,
    codigo_sala VARCHAR(10) UNIQUE NOT NULL,
    cartas_reveladas BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla de Usuarios (Solo 2 Roles) - nickname unico por sala y solo un Product Owner por sala
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_sala INT NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    rol ENUM('PRODUCT_OWNER_MODERADOR', 'VOTANTE') NOT NULL,
    -- Estado del usuario: permite al moderador habilitar/inhabilitar Desarrolladores
    estado ENUM('ACTIVO', 'INHABILITADO') NOT NULL DEFAULT 'ACTIVO',
    -- Columna generada: 1 solo si el rol es Product Owner, NULL en caso contrario.
    -- El indice unico (id_sala, es_po) garantiza maximo un Product Owner por sala.
    es_po TINYINT GENERATED ALWAYS AS (CASE WHEN rol = 'PRODUCT_OWNER_MODERADOR' THEN 1 ELSE NULL END) STORED,
    FOREIGN KEY (id_sala) REFERENCES salas(id_sala) ON DELETE CASCADE,
    UNIQUE KEY uq_sala_nickname (id_sala, nickname),
    UNIQUE KEY uq_sala_po (id_sala, es_po)
);

-- 3. Tabla Historia de Usuario (Historial de historias por moderador)
CREATE TABLE historias_usuario (
    id_historia INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    prioridad VARCHAR(20),
    puntos_estimados VARCHAR(10),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    historia_activa_key INT GENERATED ALWAYS AS (CASE WHEN activa THEN id_usuario ELSE NULL END) STORED,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    UNIQUE KEY uq_historia_activa_usuario (historia_activa_key),
    INDEX idx_historia_usuario_fecha (id_usuario, fecha_creacion)
);

-- 4. Tabla de Votos
CREATE TABLE votos (
    id_voto INT AUTO_INCREMENT PRIMARY KEY,
    id_sala INT NOT NULL,
    id_usuario INT NOT NULL,
    carta VARCHAR(10),
    FOREIGN KEY (id_sala) REFERENCES salas(id_sala) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    UNIQUE KEY voto_unico_usuario (id_sala, id_usuario)
);

-- =====================================================
-- Procedimientos Almacenados (SPs)
-- =====================================================
DELIMITER //

-- SP 1: Crear una Sala
CREATE PROCEDURE sp_crear_sala(
    IN p_codigo VARCHAR(10),
    OUT p_id_sala INT
)
BEGIN
    INSERT INTO salas (codigo_sala) VALUES (p_codigo);
    SET p_id_sala = LAST_INSERT_ID();
END //

-- SP 2: Unirse a una Sala y registrar usuario/historia inicial (Relación 1 a 1)
CREATE PROCEDURE sp_unirse_sala(
    IN p_codigo VARCHAR(10),
    IN p_nickname VARCHAR(50),
    IN p_rol VARCHAR(30),
    IN p_titulo VARCHAR(255),
    IN p_descripcion TEXT,
    IN p_prioridad VARCHAR(20),
    IN p_puntos_estimados VARCHAR(10),
    OUT p_id_usuario INT,
    OUT p_id_sala INT
)
BEGIN
    DECLARE v_estado VARCHAR(20);

    -- Bloque global: si el nickname esta inhabilitado en alguna sala, no podra ingresar a ninguna
    SELECT estado INTO v_estado FROM usuarios WHERE nickname = p_nickname AND estado = 'INHABILITADO' LIMIT 1;

    IF v_estado = 'INHABILITADO' THEN
        SET p_id_usuario = -5; -- Indicador de usuario inhabilitado
        SET p_id_sala = NULL;
    ELSE
        SELECT id_sala INTO p_id_sala FROM salas WHERE codigo_sala = p_codigo;

        IF p_id_sala IS NOT NULL THEN
            -- Re-ingreso: si el nickname ya existe, se devuelve el usuario existente
            SELECT id_usuario INTO p_id_usuario FROM usuarios WHERE id_sala = p_id_sala AND nickname = p_nickname;

            IF p_id_usuario IS NULL THEN
                -- Validacion: solo un Product Owner / Moderador por sala
                IF p_rol = 'PRODUCT_OWNER_MODERADOR' AND EXISTS (
                    SELECT 1 FROM usuarios WHERE id_sala = p_id_sala AND rol = 'PRODUCT_OWNER_MODERADOR'
                ) THEN
                    SET p_id_usuario = -3; -- Indicador de que ya existe un Product Owner en la sala
                ELSE
                    INSERT INTO usuarios (id_sala, nickname, rol) VALUES (p_id_sala, p_nickname, p_rol);
                    SET p_id_usuario = LAST_INSERT_ID();

                    -- Si ingresa un Product Owner / Moderador, se registra la Historia inicial
                    IF p_rol = 'PRODUCT_OWNER_MODERADOR' AND p_titulo IS NOT NULL AND CHAR_LENGTH(TRIM(p_titulo)) > 0 THEN
                        INSERT INTO historias_usuario (id_usuario, titulo, descripcion, prioridad, puntos_estimados)
                        VALUES (p_id_usuario, p_titulo, p_descripcion, p_prioridad, p_puntos_estimados)
                        ;
                    END IF;
                END IF;
            END IF;
        ELSE
            SET p_id_usuario = -1; -- Indicador de que la sala no existe
        END IF;
    END IF;
END //

-- SP 3: Crear una nueva historia y conservar las anteriores
CREATE PROCEDURE sp_crear_historia(
    IN p_id_usuario INT,
    IN p_titulo VARCHAR(255),
    IN p_descripcion TEXT,
    IN p_prioridad VARCHAR(20),
    IN p_puntos_estimados VARCHAR(10)
)
BEGIN
    UPDATE historias_usuario SET activa = FALSE WHERE id_usuario = p_id_usuario AND activa = TRUE;
    INSERT INTO historias_usuario (id_usuario, titulo, descripcion, prioridad, puntos_estimados)
    VALUES (p_id_usuario, p_titulo, p_descripcion, p_prioridad, p_puntos_estimados);
END //

-- SP 4: Actualizar la historia activa
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

-- SP 5: Emitir o Actualizar Voto
CREATE PROCEDURE sp_emitir_voto(
    IN p_id_sala INT,
    IN p_id_usuario INT,
    IN p_carta VARCHAR(10)
)
BEGIN
    INSERT INTO votos (id_sala, id_usuario, carta)
    VALUES (p_id_sala, p_id_usuario, p_carta)
    ON DUPLICATE KEY UPDATE carta = p_carta;
END //

-- SP 6: Revelar Cartas de la Sala
CREATE PROCEDURE sp_revelar_cartas(
    IN p_id_sala INT
)
BEGIN
    UPDATE salas SET cartas_reveladas = TRUE WHERE id_sala = p_id_sala;
END //

-- SP 7: Obtener Estado de la Sala (Votos, Participantes e Historia Activa)
CREATE PROCEDURE sp_obtener_votos(
    IN p_id_sala INT
)
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
    LEFT JOIN usuarios u_po ON u_po.id_sala = s.id_sala AND u_po.rol = 'PRODUCT_OWNER_MODERADOR' AND u_po.estado = 'ACTIVO'
    LEFT JOIN historias_usuario h ON h.id_usuario = u_po.id_usuario AND h.activa = TRUE
    WHERE u.id_sala = p_id_sala AND u.estado = 'ACTIVO';
END //

-- SP 8: Obtener el historial completo de historias de la sala
CREATE PROCEDURE sp_obtener_historial_historias(IN p_id_sala INT)
BEGIN
    SELECT h.id_historia, h.titulo, h.descripcion, h.prioridad,
           h.puntos_estimados, h.activa, h.fecha_creacion
    FROM historias_usuario h
    JOIN usuarios u ON u.id_usuario = h.id_usuario
    WHERE u.id_sala = p_id_sala
    ORDER BY h.id_historia;
END //

-- SP 9: Calcular promedio, mediana y consenso en la base de datos
CREATE PROCEDURE sp_obtener_metricas_votos(IN p_id_sala INT)
BEGIN
    WITH cartas AS (
        SELECT CAST(v.carta AS DECIMAL(10, 2)) AS valor
        FROM votos v
        WHERE v.id_sala = p_id_sala
          AND v.carta REGEXP '^[0-9]+([.][0-9]+)?$'
    ), ordenadas AS (
        SELECT valor,
               ROW_NUMBER() OVER (ORDER BY valor) AS posicion,
               COUNT(*) OVER () AS cantidad
        FROM cartas
    ), metricas AS (
        SELECT AVG(valor) AS promedio,
               AVG(CASE
                   WHEN posicion IN (FLOOR((cantidad + 1) / 2), CEIL((cantidad + 1) / 2))
                   THEN valor
               END) AS mediana,
               COUNT(*) AS cantidad,
               MIN(valor) AS minimo,
               MAX(valor) AS maximo
        FROM ordenadas
    )
        SELECT CASE WHEN CAST(s.cartas_reveladas AS UNSIGNED) = 1 THEN m.promedio ELSE NULL END AS promedio,
            CASE WHEN CAST(s.cartas_reveladas AS UNSIGNED) = 1 THEN m.mediana ELSE NULL END AS mediana,
            CASE WHEN CAST(s.cartas_reveladas AS UNSIGNED) = 1 AND m.cantidad > 0
                THEN IF(m.minimo = m.maximo, 'SI', 'NO')
                ELSE NULL
           END AS consenso
    FROM salas s
    LEFT JOIN metricas m ON TRUE
    WHERE s.id_sala = p_id_sala;
END //

-- SP 10: Preparar una historia nueva conservando el historial
CREATE PROCEDURE sp_preparar_nueva_historia(IN p_id_sala INT, IN p_id_usuario INT)
BEGIN
    DELETE FROM votos WHERE id_sala = p_id_sala;
    UPDATE salas SET cartas_reveladas = FALSE WHERE id_sala = p_id_sala;
    UPDATE historias_usuario SET activa = FALSE
    WHERE id_usuario = p_id_usuario AND activa = TRUE;
END //

-- SP 11: Crear un Desarrollador (rol VOTANTE) dentro de la sala
CREATE PROCEDURE sp_crear_dev(
    IN p_id_sala INT,
    IN p_nickname VARCHAR(50),
    OUT p_id_usuario INT,
    OUT p_resultado INT
)
BEGIN
    DECLARE v_existe INT DEFAULT 0;
    SET p_resultado = 0;
    SELECT COUNT(*) INTO v_existe FROM usuarios WHERE id_sala = p_id_sala AND nickname = p_nickname;
    IF v_existe > 0 THEN
        SET p_id_usuario = 0;
        SET p_resultado = -1; -- El nickname ya existe
    ELSE
        INSERT INTO usuarios (id_sala, nickname, rol) VALUES (p_id_sala, p_nickname, 'VOTANTE');
        SET p_id_usuario = LAST_INSERT_ID();
    END IF;
END //

-- SP 12: Listar Desarrolladores de la sala (id, nickname, estado)
CREATE PROCEDURE sp_listar_devs(IN p_id_sala INT)
BEGIN
    SELECT id_usuario, nickname, rol, estado
    FROM usuarios
    WHERE id_sala = p_id_sala AND rol = 'VOTANTE'
    ORDER BY id_usuario;
END //

-- SP 13: Actualizar Desarrollador (cambiar nickname)
CREATE PROCEDURE sp_actualizar_dev(
    IN p_id_usuario INT,
    IN p_nickname VARCHAR(50),
    OUT p_resultado INT
)
BEGIN
    DECLARE v_sala INT DEFAULT NULL;
    DECLARE v_existe INT DEFAULT 0;
    SET p_resultado = 0;
    SELECT id_sala INTO v_sala FROM usuarios WHERE id_usuario = p_id_usuario AND rol = 'VOTANTE';
    IF v_sala IS NULL THEN
        SET p_resultado = -2; -- No es un Desarrollador
    ELSE
        SELECT COUNT(*) INTO v_existe FROM usuarios WHERE id_sala = v_sala AND nickname = p_nickname AND id_usuario <> p_id_usuario;
        IF v_existe > 0 THEN
            SET p_resultado = -1; -- El nickname ya existe
        ELSE
            UPDATE usuarios SET nickname = p_nickname WHERE id_usuario = p_id_usuario;
        END IF;
    END IF;
END //

-- SP 14: Inhabilitar Desarrollador
CREATE PROCEDURE sp_inhabilitar_dev(IN p_id_usuario INT)
BEGIN
    UPDATE usuarios SET estado = 'INHABILITADO' WHERE id_usuario = p_id_usuario AND rol = 'VOTANTE';
END //

-- SP 15: Habilitar Desarrollador
CREATE PROCEDURE sp_habilitar_dev(IN p_id_usuario INT)
BEGIN
    UPDATE usuarios SET estado = 'ACTIVO' WHERE id_usuario = p_id_usuario AND rol = 'VOTANTE';
END //

-- SP 16: Reiniciar Votación (Nueva Ronda)
CREATE PROCEDURE sp_reiniciar_votacion(
    IN p_id_sala INT,
    IN p_id_usuario_po INT,
    IN p_titulo VARCHAR(255),
    IN p_descripcion TEXT,
    IN p_prioridad VARCHAR(20),
    IN p_puntos_estimados VARCHAR(10)
)
BEGIN
    -- Borra los votos de la ronda anterior
    DELETE FROM votos WHERE id_sala = p_id_sala;

    -- Vuelve a ocultar las cartas
    UPDATE salas SET cartas_reveladas = FALSE WHERE id_sala = p_id_sala;

    -- Actualiza la historia activa, sin crear una nueva entrada de historial
    IF p_id_usuario_po IS NOT NULL AND p_titulo IS NOT NULL THEN
        UPDATE historias_usuario
        SET titulo = p_titulo,
            descripcion = p_descripcion,
            prioridad = p_prioridad,
            puntos_estimados = p_puntos_estimados
        WHERE id_usuario = p_id_usuario_po AND activa = TRUE;
    END IF;
END //

DELIMITER ;