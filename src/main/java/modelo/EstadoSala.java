package modelo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import controlador.ConexionBDD;

public class EstadoSala {

    private final List<Voto> votos;
    private HistoriaUsuario historiaActiva;
    private boolean cartasReveladas;

    private EstadoSala() {
        votos = new ArrayList<>();
    }

    public List<Voto> getVotos() { return votos; }
    public HistoriaUsuario getHistoriaActiva() { return historiaActiva; }
    public boolean isCartasReveladas() { return cartasReveladas; }

    public static EstadoSala obtener(int idSala) throws Exception {
        EstadoSala estado = new EstadoSala();
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_obtener_votos(?)}")) {
            cs.setInt(1, idSala);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    estado.cartasReveladas = rs.getBoolean("cartas_reveladas");
                    estado.votos.add(new Voto(rs.getString("nickname"), rs.getString("rol"),
                            rs.getString("carta"), estado.cartasReveladas));
                    if (estado.historiaActiva == null && rs.getString("historia_titulo") != null) {
                        estado.historiaActiva = new HistoriaUsuario(rs.getInt("id_historia"), 0,
                                rs.getString("historia_titulo"), rs.getString("historia_descripcion"),
                                rs.getString("historia_prioridad"), rs.getString("historia_puntos"));
                        estado.historiaActiva.setActiva(true);
                    }
                }
            }
        }
        return estado;
    }
}
