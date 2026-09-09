package modelo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import controlador.ConexionBDD;

public class Voto {

    private final String nickname;
    private final String rol;
    private final String carta;
    private final boolean cartasReveladas;

    public Voto(String nickname, String rol, String carta, boolean cartasReveladas) {
        this.nickname = nickname;
        this.rol = rol;
        this.carta = carta;
        this.cartasReveladas = cartasReveladas;
    }

    public String getNickname() { return nickname; }
    public String getRol() { return rol; }
    public String getCarta() { return carta; }
    public boolean isCartasReveladas() { return cartasReveladas; }

    public static void emitir(int idSala, int idUsuario, String carta) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_emitir_voto(?, ?, ?)}")) {
            cs.setInt(1, idSala);
            cs.setInt(2, idUsuario);
            cs.setString(3, carta);
            cs.execute();
        }
    }

    public static List<Voto> obtenerPorSala(int idSala) throws Exception {
        List<Voto> votos = new ArrayList<>();
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_obtener_votos(?)}")) {
            cs.setInt(1, idSala);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    votos.add(new Voto(rs.getString("nickname"), rs.getString("rol"),
                            rs.getString("carta"), rs.getBoolean("cartas_reveladas")));
                }
            }
        }
        return votos;
    }

}
