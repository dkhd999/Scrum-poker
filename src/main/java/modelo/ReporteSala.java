package modelo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import controlador.ConexionBDD;

public class ReporteSala {

    private final int idSala;
    private final String codigoSala;
    private final boolean cartasReveladas;
    private final List<HistoriaUsuario> historias;
    private final List<Voto> votos;
    private final MetricasVoto metricas;

    private ReporteSala(int idSala, String codigoSala, boolean cartasReveladas,
            List<HistoriaUsuario> historias, List<Voto> votos, MetricasVoto metricas) {
        this.idSala = idSala;
        this.codigoSala = codigoSala;
        this.cartasReveladas = cartasReveladas;
        this.historias = historias;
        this.votos = votos;
        this.metricas = metricas;
    }

    public int getIdSala() { return idSala; }
    public String getCodigoSala() { return codigoSala; }
    public boolean isCartasReveladas() { return cartasReveladas; }
    public List<HistoriaUsuario> getHistorias() { return historias; }
    public List<Voto> getVotos() { return votos; }
    public MetricasVoto getMetricas() { return metricas; }

    public static ReporteSala obtener(int idSala) throws Exception {
        String codigo = "";
        boolean reveladas = false;
        try (Connection conn = new ConexionBDD().conectar();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT codigo_sala, cartas_reveladas FROM salas WHERE id_sala = ?")) {
            ps.setInt(1, idSala);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("La sala no existe: " + idSala);
                codigo = rs.getString("codigo_sala");
                reveladas = rs.getBoolean("cartas_reveladas");
            }
        }

        List<HistoriaUsuario> historias = HistoriaUsuario.obtenerHistorial(idSala);
        List<Voto> votos = obtenerVotos(idSala);
        MetricasVoto metricas = MetricasVoto.obtenerPorSala(idSala);
        return new ReporteSala(idSala, codigo, reveladas, historias, votos, metricas);
    }

    private static List<Voto> obtenerVotos(int idSala) throws Exception {
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