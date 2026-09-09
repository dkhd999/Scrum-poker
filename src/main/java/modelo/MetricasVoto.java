package modelo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import controlador.ConexionBDD;

public class MetricasVoto {

    private final Double promedio;
    private final Double mediana;
    private final String consenso;

    public MetricasVoto(Double promedio, Double mediana, String consenso) {
        this.promedio = promedio;
        this.mediana = mediana;
        this.consenso = consenso;
    }

    public Double getPromedio() { return promedio; }
    public Double getMediana() { return mediana; }
    public String getConsenso() { return consenso; }

    public static MetricasVoto obtenerPorSala(int idSala) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_obtener_metricas_votos(?)}")) {
            cs.setInt(1, idSala);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    Number promedio = (Number) rs.getObject("promedio");
                    Number mediana = (Number) rs.getObject("mediana");
                    return new MetricasVoto(
                            promedio == null ? null : promedio.doubleValue(),
                            mediana == null ? null : mediana.doubleValue(),
                            rs.getString("consenso"));
                }
            }
        }
        return new MetricasVoto(null, null, null);
    }
}
