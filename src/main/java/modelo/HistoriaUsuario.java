package modelo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import controlador.ConexionBDD;

public class HistoriaUsuario {

    private int idHistoria;
    private int idUsuario;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private String puntosEstimados;
    private boolean activa;

    public HistoriaUsuario(int idHistoria, int idUsuario, String titulo, String descripcion, String prioridad, String puntosEstimados) {
        this.idHistoria = idHistoria;
        this.idUsuario = idUsuario;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.puntosEstimados = puntosEstimados;
    }

    public HistoriaUsuario(String titulo, String descripcion, String prioridad, String puntosEstimados) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.puntosEstimados = puntosEstimados;
    }

    public int getIdHistoria() {
        return idHistoria;
    }

    public void setIdHistoria(int idHistoria) {
        this.idHistoria = idHistoria;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getPuntosEstimados() {
        return puntosEstimados;
    }

    public void setPuntosEstimados(String puntosEstimados) {
        this.puntosEstimados = puntosEstimados;
    }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public static void crear(int idUsuario, String titulo, String descripcion,
            String prioridad, String puntos) throws Exception {
        ejecutarHistoria("{CALL sp_crear_historia(?, ?, ?, ?, ?)}", -1, idUsuario,
                titulo, descripcion, prioridad, puntos);
    }

    public static void actualizar(int idHistoria, int idUsuario, String titulo,
            String descripcion, String prioridad, String puntos) throws Exception {
        ejecutarHistoria("{CALL sp_actualizar_historia(?, ?, ?, ?, ?, ?)}", idHistoria,
                idUsuario, titulo, descripcion, prioridad, puntos);
    }

    public static List<HistoriaUsuario> obtenerHistorial(int idSala) throws Exception {
        List<HistoriaUsuario> historias = new ArrayList<>();
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_obtener_historial_historias(?)}")) {
            cs.setInt(1, idSala);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    HistoriaUsuario historia = new HistoriaUsuario(rs.getInt("id_historia"), 0,
                            rs.getString("titulo"), rs.getString("descripcion"),
                            rs.getString("prioridad"), rs.getString("puntos_estimados"));
                    historia.setActiva(rs.getBoolean("activa"));
                    historias.add(historia);
                }
            }
        }
        return historias;
    }

    private static void ejecutarHistoria(String sql, int idHistoria, int idUsuario,
            String titulo, String descripcion, String prioridad, String puntos) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall(sql)) {
            int indice = 1;
            if (idHistoria >= 0) cs.setInt(indice++, idHistoria);
            cs.setInt(indice++, idUsuario);
            cs.setString(indice++, titulo);
            cs.setString(indice++, descripcion);
            cs.setString(indice++, prioridad);
            cs.setString(indice, puntos);
            cs.execute();
        }
    }
}
