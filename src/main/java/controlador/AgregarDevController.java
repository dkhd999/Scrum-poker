package controlador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import vista.AgregarDevVista;
import vista.MenuModerador;

public class AgregarDevController {

    private AgregarDevVista vista;
    private final int idSala;
    private final int idUsuario;
    private final String codigo;
    private final String nickname;
    private final MenuModerador menuPadre;
    private Timer timerRefresco;

    public AgregarDevController(AgregarDevVista vista, int idSala, String codigo, int idUsuario, String nickname, MenuModerador menuPadre) {
        this.vista = vista;
        this.idSala = idSala;
        this.codigo = codigo;
        this.idUsuario = idUsuario;
        this.nickname = nickname;
        this.menuPadre = menuPadre;
        configurarVista();
        configurarEventos();
        iniciarRefresco();
    }

    private void configurarVista() {
        vista.setTitle("Agregar Dev - Sala " + codigo);

        DefaultTableModel modelDevs = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Id", "Nickname", "Estado"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vista.getTblDev().setModel(modelDevs);
    }

    private void configurarEventos() {
        vista.getBtnCrear().addActionListener(e -> crearDev());
        vista.getBtnInhabilitar().addActionListener(e -> inhabilitarDev());
        vista.getBtnActualizar().addActionListener(e -> actualizarDev());
        vista.getBtnHabilitar().addActionListener(e -> habilitarDev());
        vista.getBtnVolver().addActionListener(e -> volverAlMenu());
        vista.getTblDev().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                copiarSeleccionATexto();
            }
        });
    }

    private void volverAlMenu() {
        timerRefresco.stop();
        vista.dispose();
        if (menuPadre != null) {
            menuPadre.setVisible(true);
        }
    }

    private void iniciarRefresco() {
        timerRefresco = new Timer(2000, e -> refrescarDevs());
        timerRefresco.start();
        refrescarDevs();
    }

    private Connection obtenerConexion() throws Exception {
        ConexionBDD db = new ConexionBDD();
        return db.conectar();
    }

    private void crearDev() {
        String devNickname = vista.getTxtNickname().getText().trim();
        if (devNickname.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Ingrese un nickname para el desarrollador.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int[] resultado = crearDevDB(idSala, devNickname);
        if (resultado[1] == 0) {
            JOptionPane.showMessageDialog(vista, "Desarrollador '" + devNickname + "' creado correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
            vista.getTxtNickname().setText("");
            refrescarDevs();
        } else if (resultado[1] == -1) {
            JOptionPane.showMessageDialog(vista, "El nickname '" + devNickname + "' ya existe en la sala.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vista, "Error al crear el desarrollador.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarDev() {
        int fila = devSeleccionado();
        if (fila < 0) {
            JOptionPane.showMessageDialog(vista, "Seleccione un desarrollador en la tabla.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idDev = (Integer) vista.getTblDev().getValueAt(fila, 0);
        String nuevoNickname = vista.getTxtNickname().getText().trim();
        if (nuevoNickname.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Ingrese el nuevo nickname en el campo.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int[] resultado = actualizarDevDB(idDev, nuevoNickname);
        if (resultado[0] == -1) {
            JOptionPane.showMessageDialog(vista, "El nickname '" + nuevoNickname + "' ya existe en la sala.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (resultado[0] == -2) {
            JOptionPane.showMessageDialog(vista, "El usuario seleccionado no es un desarrollador.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vista, "Desarrollador actualizado correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
            refrescarDevs();
        }
    }

    private void inhabilitarDev() {
        int fila = devSeleccionado();
        if (fila < 0) {
            JOptionPane.showMessageDialog(vista, "Seleccione un desarrollador en la tabla.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idDev = (Integer) vista.getTblDev().getValueAt(fila, 0);
        inhabilitarDevDB(idDev);
        JOptionPane.showMessageDialog(vista, "Desarrollador inhabilitado correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
        refrescarDevs();
    }

    private void habilitarDev() {
        int fila = devSeleccionado();
        if (fila < 0) {
            JOptionPane.showMessageDialog(vista, "Seleccione un desarrollador en la tabla.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idDev = (Integer) vista.getTblDev().getValueAt(fila, 0);
        habilitarDevDB(idDev);
        JOptionPane.showMessageDialog(vista, "Desarrollador habilitado correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
        refrescarDevs();
    }

    private int devSeleccionado() {
        return vista.getTblDev().getSelectedRow();
    }

    private void copiarSeleccionATexto() {
        int fila = devSeleccionado();
        if (fila >= 0) {
            vista.getTxtNickname().setText(vista.getTblDev().getValueAt(fila, 1).toString());
        }
    }

    public int[] crearDevDB(int idSala, String devNickname) {
        int[] resultado = new int[2];
        try (Connection conn = obtenerConexion()) {
            CallableStatement cs = conn.prepareCall("{CALL sp_crear_dev(?, ?, ?, ?)}");
            cs.setInt(1, idSala);
            cs.setString(2, devNickname);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            resultado[0] = cs.getInt(3);
            resultado[1] = cs.getInt(4);
        } catch (Exception e) {
            System.out.println("Error al crear dev: " + e.getMessage());
        }
        return resultado;
    }

    public int[] actualizarDevDB(int idDev, String nuevoNickname) {
        int[] resultado = new int[1];
        try (Connection conn = obtenerConexion()) {
            CallableStatement cs = conn.prepareCall("{CALL sp_actualizar_dev(?, ?, ?)}");
            cs.setInt(1, idDev);
            cs.setString(2, nuevoNickname);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            resultado[0] = cs.getInt(3);
        } catch (Exception e) {
            System.out.println("Error al actualizar dev: " + e.getMessage());
        }
        return resultado;
    }

    public void inhabilitarDevDB(int idDev) {
        try (Connection conn = obtenerConexion()) {
            CallableStatement cs = conn.prepareCall("{CALL sp_inhabilitar_dev(?)}");
            cs.setInt(1, idDev);
            cs.execute();
        } catch (Exception e) {
            System.out.println("Error al inhabilitar dev: " + e.getMessage());
        }
    }

    public void habilitarDevDB(int idDev) {
        try (Connection conn = obtenerConexion()) {
            CallableStatement cs = conn.prepareCall("{CALL sp_habilitar_dev(?)}");
            cs.setInt(1, idDev);
            cs.execute();
        } catch (Exception e) {
            System.out.println("Error al habilitar dev: " + e.getMessage());
        }
    }

    private void refrescarDevs() {
        DefaultTableModel modelDevs = (DefaultTableModel) vista.getTblDev().getModel();
        modelDevs.setRowCount(0);
        try (Connection conn = obtenerConexion()) {
            CallableStatement cs = conn.prepareCall("{CALL sp_listar_devs(?)}");
            cs.setInt(1, idSala);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                modelDevs.addRow(new Object[]{
                    rs.getInt("id_usuario"),
                    rs.getString("nickname"),
                    rs.getString("estado")
                });
            }
        } catch (Exception e) {
            System.out.println("Error al listar devs: " + e.getMessage());
        }
    }
}