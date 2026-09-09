package modelo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import controlador.ConexionBDD;

public class Sala {

    private int idSala;
    private String codigoSala;
    private boolean cartasReveladas;

    public Sala(int idSala, String codigoSala, boolean cartasReveladas) {
        this.idSala = idSala;
        this.codigoSala = codigoSala;
        this.cartasReveladas = cartasReveladas;
    }

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public String getCodigoSala() {
        return codigoSala;
    }

    public void setCodigoSala(String codigoSala) {
        this.codigoSala = codigoSala;
    }

    public boolean isCartasReveladas() {
        return cartasReveladas;
    }

    public void setCartasReveladas(boolean cartasReveladas) {
        this.cartasReveladas = cartasReveladas;
    }

    public static int crear(String codigo) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_crear_sala(?, ?)}")) {
            cs.setString(1, codigo);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            return cs.getInt(2);
        }
    }

    public static void revelarCartas(int idSala) throws Exception {
        ejecutarUnaEntrada("{CALL sp_revelar_cartas(?)}", idSala);
    }

    public static void prepararNuevaHistoria(int idSala, int idUsuario) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_preparar_nueva_historia(?, ?)}")) {
            cs.setInt(1, idSala);
            cs.setInt(2, idUsuario);
            cs.execute();
        }
    }

    public static void reiniciarVotacion(int idSala, int idUsuario, String titulo,
            String descripcion, String prioridad, String puntos) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_reiniciar_votacion(?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, idSala);
            cs.setInt(2, idUsuario);
            cs.setString(3, titulo);
            cs.setString(4, descripcion);
            cs.setString(5, prioridad);
            cs.setString(6, puntos);
            cs.execute();
        }
    }

    private static void ejecutarUnaEntrada(String sql, int idSala) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, idSala);
            cs.execute();
        }
    }
}
