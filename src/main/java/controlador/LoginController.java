package controlador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import vista.RegistrarVotoVista;
import vista.LoginVista;
import vista.MenuModerador;

public class LoginController {

    private LoginVista vista;
    private final ButtonGroup grupoRoles;
    private String ultimoError = null;

    public LoginController() {
        this.grupoRoles = new ButtonGroup();
    }

    public void setVista(LoginVista vista) {
        this.vista = vista;
        grupoRoles.add(vista.getRbtCrearSala());
        grupoRoles.add(vista.getRbtUnirse());
        configurarEventos();
    }

    private void configurarEventos() {
        vista.getBtnIngresar().addActionListener(e -> ingresar());
    }

    private void ingresar() {
        String nickname = vista.getTxtNickname().getText().trim();
        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Debe ingresar un nickname.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (vista.getRbtCrearSala().isSelected()) {
            crearNuevaSala(nickname);
        } else if (vista.getRbtUnirse().isSelected()) {
            unirseSalaExistente(nickname);
        } else {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar una accion.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void crearNuevaSala(String nickname) {
        String codigo = vista.getTxtCodigoSala().getText().trim();
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Debe ingresar un codigo de sala.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int[] resultado = crearSala(codigo, nickname, null, null, null, null);
        int idUsuario = resultado[0];
        int idSala = resultado[1];
        if (idUsuario > 0 && idSala > 0) {
            abrirMenuModerador(idSala, codigo, nickname, idUsuario);
        } else if (idUsuario == -2 || idSala == -2) {
            // El codigo de sala ya existe: el moderador (unico en la sala) ingresa a su sala creada
            ingresarComoModerador(codigo, nickname);
        } else if (idUsuario == -3) {
            JOptionPane.showMessageDialog(vista, "Ya existe un Product Owner en la sala. Solo puede haber uno.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (idUsuario == -5) {
            JOptionPane.showMessageDialog(vista, "Su usuario se encuentra inhabilitado por el moderador.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vista, "Error al crear la sala.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ingresarComoModerador(String codigo, String nickname) {
        int[] resultado = unirseASala(codigo, nickname, "PRODUCT_OWNER_MODERADOR");
        int idUsuario = resultado[0];
        int idSala = resultado[1];
        if (idUsuario > 0 && idSala > 0) {
            abrirMenuModerador(idSala, codigo, nickname, idUsuario);
        } else if (idUsuario == -3) {
            JOptionPane.showMessageDialog(vista, "Ya existe un Product Owner en la sala. Solo puede haber uno.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (idUsuario == -5) {
            JOptionPane.showMessageDialog(vista, "Su usuario se encuentra inhabilitado por el moderador.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vista, "No fue posible ingresar a la sala.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirMenuModerador(int idSala, String codigo, String nickname, int idUsuario) {
        MenuModerador menuModerador = new MenuModerador();
        MenuModeradorController controllerMenu = new MenuModeradorController(menuModerador, idSala, codigo, idUsuario, nickname);
        menuModerador.setVisible(true);
        vista.dispose();
    }

    private void unirseSalaExistente(String nickname) {
        String codigo = vista.getTxtCodigoSala().getText().trim();
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Debe ingresar el codigo de sala.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int[] resultado = unirseASala(codigo, nickname, "VOTANTE");
        int idUsuario = resultado[0];
        int idSala = resultado[1];
        if (idUsuario > 0 && idSala > 0) {
            String rolReal = obtenerRolBD(idUsuario);
            if ("PRODUCT_OWNER_MODERADOR".equals(rolReal)) {
                abrirMenuModerador(idSala, codigo, nickname, idUsuario);
            } else {
                RegistrarVotoVista vistaVotante = new RegistrarVotoVista();
                RegistrarVotoController controllerVotante = new RegistrarVotoController(vistaVotante, idSala, codigo, nickname, "VOTANTE", idUsuario);
                vistaVotante.setVisible(true);
                vista.dispose();
            }
        } else if (idUsuario == -3) {
            JOptionPane.showMessageDialog(vista, "Ya existe un Product Owner en la sala. Solo puede haber uno.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (idUsuario == -5) {
            JOptionPane.showMessageDialog(vista, "Su usuario se encuentra inhabilitado por el moderador.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String mensaje = "La sala no existe o el codigo es incorrecto.";
            if (ultimoError != null) {
                mensaje += "\nDetalle: " + ultimoError;
            }
            JOptionPane.showMessageDialog(vista, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerRolBD(int idUsuario) {
        try (Connection conn = obtenerConexion();
             PreparedStatement ps = conn.prepareStatement("SELECT rol FROM usuarios WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al obtener rol: " + e.getMessage());
        }
        return null;
    }

    private Connection obtenerConexion() throws Exception {
        ConexionBDD db = new ConexionBDD();
        return db.conectar();
    }

    public int[] crearSala(String codigo, String nickname, String titulo, String descripcion, String prioridad, String puntosEstimados) {
        int[] resultado = new int[2];
        resultado[0] = -1;
        resultado[1] = -1;
        try (Connection conn = obtenerConexion()) {
            try (CallableStatement cs = conn.prepareCall("{CALL sp_crear_sala(?, ?)}")) {
                cs.setString(1, codigo);
                cs.registerOutParameter(2, Types.INTEGER);
                cs.execute();
                resultado[1] = cs.getInt(2);
            }
            if (resultado[1] > 0) {
                try (CallableStatement cs = conn.prepareCall("{CALL sp_unirse_sala(?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
                    cs.setString(1, codigo);
                    cs.setString(2, nickname);
                    cs.setString(3, "PRODUCT_OWNER_MODERADOR");
                    cs.setString(4, titulo);
                    cs.setString(5, descripcion);
                    cs.setString(6, prioridad);
                    cs.setString(7, puntosEstimados);
                    cs.registerOutParameter(8, Types.INTEGER);
                    cs.registerOutParameter(9, Types.INTEGER);
                    cs.execute();
                    resultado[0] = cs.getInt(8);
                    resultado[1] = cs.getInt(9);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al crear sala: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                resultado[0] = -2;
                resultado[1] = -2;
            }
        }
        return resultado;
    }

    public int[] unirseASala(String codigo, String nickname, String rol) {
        int[] resultado = new int[2];
        ultimoError = null;
        resultado[0] = -1;
        resultado[1] = -1;
        try (Connection conn = obtenerConexion()) {
            CallableStatement cs = conn.prepareCall("{CALL sp_unirse_sala(?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, codigo);
            cs.setString(2, nickname);
            cs.setString(3, rol);
            cs.setString(4, null);
            cs.setString(5, null);
            cs.setString(6, null);
            cs.setString(7, null);
            cs.registerOutParameter(8, Types.INTEGER);
            cs.registerOutParameter(9, Types.INTEGER);
            cs.execute();
            resultado[0] = cs.getInt(8);
            resultado[1] = cs.getInt(9);
        } catch (Exception e) {
            ultimoError = e.getMessage();
            System.out.println("Error al unirse a sala: " + e.getMessage());
        }
        return resultado;
    }
}