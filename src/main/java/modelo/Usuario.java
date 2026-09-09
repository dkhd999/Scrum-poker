package modelo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import controlador.ConexionBDD;

public abstract class Usuario implements IVotable {

    private int idUsuario;
    private String nickname;
    private String rol;
    private int idSala;
    private String cartaSeleccionada;
    private String estado;

    public Usuario(int idUsuario, String nickname, String rol, int idSala) {
        this.idUsuario = idUsuario;
        this.nickname = nickname;
        this.rol = rol;
        this.idSala = idSala;
        this.cartaSeleccionada = null;
        this.estado = "ACTIVO";
    }

    @Override
    public void seleccionarCarta(String carta) {
        this.cartaSeleccionada = carta;
    }

    @Override
    public String getCartaSeleccionada() {
        return cartaSeleccionada;
    }

    public abstract boolean esModerador();

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public static String obtenerRol(int idUsuario) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             PreparedStatement ps = conn.prepareStatement("SELECT rol FROM usuarios WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("rol") : null;
            }
        }
    }

    public static int[] crearDesarrollador(int idSala, String nickname) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_crear_dev(?, ?, ?, ?)}")) {
            cs.setInt(1, idSala);
            cs.setString(2, nickname);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            return new int[]{cs.getInt(3), cs.getInt(4)};
        }
    }

    public static int actualizarDesarrollador(int idUsuario, String nickname) throws Exception {
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_actualizar_dev(?, ?, ?)}")) {
            cs.setInt(1, idUsuario);
            cs.setString(2, nickname);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            return cs.getInt(3);
        }
    }

    public static void cambiarEstadoDesarrollador(int idUsuario, boolean habilitar) throws Exception {
        String procedimiento = habilitar ? "sp_habilitar_dev" : "sp_inhabilitar_dev";
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL " + procedimiento + "(?)}")) {
            cs.setInt(1, idUsuario);
            cs.execute();
        }
    }

    public static List<Usuario> listarDesarrolladores(int idSala) throws Exception {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = new ConexionBDD().conectar();
             CallableStatement cs = conn.prepareCall("{CALL sp_listar_devs(?)}")) {
            cs.setInt(1, idSala);
            cs.setInt(1, idSala);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Usuario usuario = new Votante(rs.getInt("id_usuario"), rs.getString("nickname"), idSala);
                    usuario.setEstado(rs.getString("estado"));
                    usuarios.add(usuario);
                }
            }
        }
        return usuarios;
    }
}
