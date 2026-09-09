package controlador;

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import modelo.Usuario;
import vista.AgregarDevVista;
import vista.MenuModerador;

public class AgregarDevControlador {

    private AgregarDevVista vista;
    private final int idSala;
    private final int idUsuario;
    private final String codigo;
    private final String nickname;
    private final MenuModerador menuPadre;
    private Timer timerRefresco;

    public AgregarDevControlador(AgregarDevVista vista, int idSala, String codigo, int idUsuario, String nickname, MenuModerador menuPadre) {
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

    private void crearDev() {
        String devNickname = vista.getTxtNickname().getText().trim();
        if (devNickname.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Ingrese un nickname para el desarrollador.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int[] resultado;
        try { resultado = Usuario.crearDesarrollador(idSala, devNickname); }
        catch (Exception e) { System.out.println("Error al crear dev: " + e.getMessage()); resultado = new int[]{0, -2}; }
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
        int resultado;
        try { resultado = Usuario.actualizarDesarrollador(idDev, nuevoNickname); }
        catch (Exception e) { System.out.println("Error al actualizar dev: " + e.getMessage()); resultado = -3; }
        if (resultado == -1) {
            JOptionPane.showMessageDialog(vista, "El nickname '" + nuevoNickname + "' ya existe en la sala.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (resultado == -2) {
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
        try { Usuario.cambiarEstadoDesarrollador(idDev, false); }
        catch (Exception e) { System.out.println("Error al inhabilitar dev: " + e.getMessage()); }
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
        try { Usuario.cambiarEstadoDesarrollador(idDev, true); }
        catch (Exception e) { System.out.println("Error al habilitar dev: " + e.getMessage()); }
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

    private void refrescarDevs() {
        DefaultTableModel modelDevs = (DefaultTableModel) vista.getTblDev().getModel();
        modelDevs.setRowCount(0);
        try {
            List<Usuario> usuarios = Usuario.listarDesarrolladores(idSala);
            for (Usuario usuario : usuarios) {
                modelDevs.addRow(new Object[]{
                    usuario.getIdUsuario(), usuario.getNickname(), usuario.getEstado()
                });
            }
        } catch (Exception e) {
            System.out.println("Error al listar devs: " + e.getMessage());
        }
    }
}