CREATE DATABASE  IF NOT EXISTS `scrum_poker_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `scrum_poker_db`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: scrum_poker_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `historias_usuario`
--

DROP TABLE IF EXISTS `historias_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historias_usuario` (
  `id_historia` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `titulo` varchar(255) NOT NULL,
  `descripcion` text,
  `prioridad` varchar(20) DEFAULT NULL,
  `puntos_estimados` varchar(10) DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_historia`),
  KEY `fk_historias_usuario_usuario` (`id_usuario`),
  CONSTRAINT `fk_historias_usuario_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historias_usuario`
--

LOCK TABLES `historias_usuario` WRITE;
/*!40000 ALTER TABLE `historias_usuario` DISABLE KEYS */;
INSERT INTO `historias_usuario` VALUES (7,17,'Crear la Vistas','Vista del comprador','Alta','0','2026-09-09 18:49:41',0),(9,17,'Crear BDD','CREAR LA BASE DE DATOS DE UNA EMPRESA','Alta','0','2026-09-09 19:16:46',0),(10,22,'Inicio de Sesión','Ingresar al sistema con correo y contraseña, recuperar contraseña','Media','0','2026-09-09 19:29:45',0),(11,22,'Actualizar BDD','Actualizar la base de datos y cambios','Alta','0','2026-09-09 20:16:54',0),(12,22,'Crear Vista','Crear una vista para el usuario','Alta','0','2026-09-09 20:30:24',1);
/*!40000 ALTER TABLE `historias_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `salas`
--

DROP TABLE IF EXISTS `salas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salas` (
  `id_sala` int NOT NULL AUTO_INCREMENT,
  `codigo_sala` varchar(10) NOT NULL,
  `cartas_reveladas` tinyint(1) DEFAULT '0',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_sala`),
  UNIQUE KEY `codigo_sala` (`codigo_sala`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `salas`
--

LOCK TABLES `salas` WRITE;
/*!40000 ALTER TABLE `salas` DISABLE KEYS */;
INSERT INTO `salas` VALUES (10,'SALA4',0,'2026-09-09 18:17:18'),(13,'100',1,'2026-09-09 19:27:22');
/*!40000 ALTER TABLE `salas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `id_sala` int NOT NULL,
  `nickname` varchar(50) NOT NULL,
  `rol` enum('PRODUCT_OWNER_MODERADOR','VOTANTE') NOT NULL,
  `estado` enum('ACTIVO','INHABILITADO') NOT NULL DEFAULT 'ACTIVO',
  `es_po` tinyint GENERATED ALWAYS AS ((case when (`rol` = _utf8mb4'PRODUCT_OWNER_MODERADOR') then 1 else NULL end)) STORED,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uq_sala_nickname` (`id_sala`,`nickname`),
  UNIQUE KEY `uq_sala_po` (`id_sala`,`es_po`),
  CONSTRAINT `usuarios_ibfk_1` FOREIGN KEY (`id_sala`) REFERENCES `salas` (`id_sala`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` (`id_usuario`, `id_sala`, `nickname`, `rol`, `estado`) VALUES (17,10,'Ana','PRODUCT_OWNER_MODERADOR','ACTIVO'),(18,10,'Justin','VOTANTE','ACTIVO'),(22,13,'Gaby','PRODUCT_OWNER_MODERADOR','ACTIVO'),(23,13,'Justin','VOTANTE','ACTIVO'),(24,13,'Ana','VOTANTE','ACTIVO'),(25,13,'Chris','VOTANTE','ACTIVO');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `votos`
--

DROP TABLE IF EXISTS `votos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `votos` (
  `id_voto` int NOT NULL AUTO_INCREMENT,
  `id_sala` int NOT NULL,
  `id_usuario` int NOT NULL,
  `id_historia` int DEFAULT NULL,
  `carta` varchar(10) DEFAULT NULL,
  `promedio` decimal(10,2) DEFAULT NULL,
  `mediana` decimal(10,2) DEFAULT NULL,
  `consenso` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`id_voto`),
  UNIQUE KEY `voto_unico_usuario` (`id_sala`,`id_usuario`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `votos_ibfk_1` FOREIGN KEY (`id_sala`) REFERENCES `salas` (`id_sala`) ON DELETE CASCADE,
  CONSTRAINT `votos_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `votos`
--

LOCK TABLES `votos` WRITE;
/*!40000 ALTER TABLE `votos` DISABLE KEYS */;
INSERT INTO `votos` VALUES (22,13,22,12,'5',3.50,3.50,'NO'),(23,13,24,12,'5',3.50,3.50,'NO'),(24,13,25,12,'2',3.50,3.50,'NO'),(25,13,23,12,'2',3.50,3.50,'NO');
/*!40000 ALTER TABLE `votos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'scrum_poker_db'
--

--
-- Dumping routines for database 'scrum_poker_db'
--
/*!50003 DROP PROCEDURE IF EXISTS `sp_actualizar_dev` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_dev`(
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
        SET p_resultado = -2; 
    ELSE
        SELECT COUNT(*) INTO v_existe FROM usuarios WHERE id_sala = v_sala AND nickname = p_nickname AND id_usuario <> p_id_usuario;
        IF v_existe > 0 THEN
            SET p_resultado = -1; 
        ELSE
            UPDATE usuarios SET nickname = p_nickname WHERE id_usuario = p_id_usuario;
        END IF;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_actualizar_historia` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_historia`(
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
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_crear_dev` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_dev`(
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
        SET p_resultado = -1; 
    ELSE
        INSERT INTO usuarios (id_sala, nickname, rol) VALUES (p_id_sala, p_nickname, 'VOTANTE');
        SET p_id_usuario = LAST_INSERT_ID();
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_crear_historia` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_historia`(
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
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_crear_sala` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_sala`(
    IN p_codigo VARCHAR(10),
    OUT p_id_sala INT
)
BEGIN
    INSERT INTO salas (codigo_sala) VALUES (p_codigo);
    SET p_id_sala = LAST_INSERT_ID();
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_emitir_voto` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_emitir_voto`(IN p_id_sala INT, IN p_id_usuario INT, IN p_carta VARCHAR(10))
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
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_guardar_actualizar_historia` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_guardar_actualizar_historia`(
    IN p_id_usuario INT,
    IN p_titulo VARCHAR(255),
    IN p_descripcion TEXT,
    IN p_prioridad VARCHAR(20),
    IN p_puntos_estimados VARCHAR(10)
)
BEGIN
    INSERT INTO historias_usuario (id_usuario, titulo, descripcion, prioridad, puntos_estimados)
    VALUES (p_id_usuario, p_titulo, p_descripcion, p_prioridad, p_puntos_estimados)
    ON DUPLICATE KEY UPDATE
        titulo = p_titulo,
        descripcion = p_descripcion,
        prioridad = p_prioridad,
        puntos_estimados = p_puntos_estimados;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_habilitar_dev` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_habilitar_dev`(IN p_id_usuario INT)
BEGIN
    UPDATE usuarios SET estado = 'ACTIVO' WHERE id_usuario = p_id_usuario AND rol = 'VOTANTE';
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_inhabilitar_dev` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_inhabilitar_dev`(IN p_id_usuario INT)
BEGIN
    UPDATE usuarios SET estado = 'INHABILITADO' WHERE id_usuario = p_id_usuario AND rol = 'VOTANTE';
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_listar_devs` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_devs`(IN p_id_sala INT)
BEGIN
    SELECT id_usuario, nickname, rol, estado
    FROM usuarios
    WHERE id_sala = p_id_sala AND rol = 'VOTANTE'
    ORDER BY id_usuario;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_obtener_historial_historias` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_historial_historias`(IN p_id_sala INT)
BEGIN
    SELECT h.id_historia, h.titulo, h.descripcion, h.prioridad,
           h.puntos_estimados, h.activa, h.fecha_creacion
    FROM historias_usuario h
    JOIN usuarios u ON u.id_usuario = h.id_usuario
    WHERE u.id_sala = p_id_sala
    ORDER BY h.id_historia;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_obtener_metricas_votos` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_metricas_votos`(IN p_id_sala INT)
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
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_obtener_votos` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_votos`(IN p_id_sala INT)
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
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_preparar_nueva_historia` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_preparar_nueva_historia`(IN p_id_sala INT, IN p_id_usuario INT)
BEGIN
    DELETE FROM votos WHERE id_sala = p_id_sala;
    UPDATE salas SET cartas_reveladas = FALSE WHERE id_sala = p_id_sala;
    UPDATE historias_usuario SET activa = FALSE
    WHERE id_usuario = p_id_usuario AND activa = TRUE;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_reiniciar_votacion` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_reiniciar_votacion`(
    IN p_id_sala INT,
    IN p_id_usuario_po INT,
    IN p_titulo VARCHAR(255),
    IN p_descripcion TEXT,
    IN p_prioridad VARCHAR(20),
    IN p_puntos_estimados VARCHAR(10)
)
BEGIN
    
    DELETE FROM votos WHERE id_sala = p_id_sala;

    
    UPDATE salas SET cartas_reveladas = FALSE WHERE id_sala = p_id_sala;

    
    IF p_id_usuario_po IS NOT NULL AND p_titulo IS NOT NULL THEN
        INSERT INTO historias_usuario (id_usuario, titulo, descripcion, prioridad, puntos_estimados)
        VALUES (p_id_usuario_po, p_titulo, p_descripcion, p_prioridad, p_puntos_estimados)
        ON DUPLICATE KEY UPDATE
            titulo = p_titulo,
            descripcion = p_descripcion,
            prioridad = p_prioridad,
            puntos_estimados = p_puntos_estimados;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_revelar_cartas` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_revelar_cartas`(IN p_id_sala INT)
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
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_unirse_sala` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_unirse_sala`(
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

    
    SELECT estado INTO v_estado FROM usuarios WHERE nickname = p_nickname AND estado = 'INHABILITADO' LIMIT 1;

    IF v_estado = 'INHABILITADO' THEN
        SET p_id_usuario = -5; 
        SET p_id_sala = NULL;
    ELSE
        SELECT id_sala INTO p_id_sala FROM salas WHERE codigo_sala = p_codigo;

        IF p_id_sala IS NOT NULL THEN
            
            SELECT id_usuario INTO p_id_usuario FROM usuarios WHERE id_sala = p_id_sala AND nickname = p_nickname;

            IF p_id_usuario IS NULL THEN
                
                IF p_rol = 'PRODUCT_OWNER_MODERADOR' AND EXISTS (
                    SELECT 1 FROM usuarios WHERE id_sala = p_id_sala AND rol = 'PRODUCT_OWNER_MODERADOR'
                ) THEN
                    SET p_id_usuario = -3; 
                ELSE
                    INSERT INTO usuarios (id_sala, nickname, rol) VALUES (p_id_sala, p_nickname, p_rol);
                    SET p_id_usuario = LAST_INSERT_ID();

                    IF p_rol = 'PRODUCT_OWNER_MODERADOR' AND p_titulo IS NOT NULL AND CHAR_LENGTH(TRIM(p_titulo)) > 0 THEN
                        INSERT INTO historias_usuario (id_usuario, titulo, descripcion, prioridad, puntos_estimados)
                        VALUES (p_id_usuario, p_titulo, p_descripcion, p_prioridad, p_puntos_estimados)
                        ON DUPLICATE KEY UPDATE
                            titulo = p_titulo,
                            descripcion = p_descripcion,
                            prioridad = p_prioridad,
                            puntos_estimados = p_puntos_estimados;
                    END IF;
                END IF;
            END IF;
        ELSE
            SET p_id_usuario = -1; 
        END IF;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-09 16:23:55
