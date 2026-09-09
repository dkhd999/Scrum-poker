package controlador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import vista.RegistrarVotoVista;

public class RegistrarVotoController {

    private RegistrarVotoVista vista;
    private final int idSala;
    private final int idUsuario;
    private final String codigo;
    private final String nickname;
    private final String rol;
    private Timer timerRefresco;
    private boolean cartasReveladas = false;

    public RegistrarVotoController(RegistrarVotoVista vista, int idSala, String codigo, String nickname, String rol, int idUsuario) {
        this.vista = vista;
        this.idSala = idSala;
        this.codigo = codigo;
        this.nickname = nickname;
        this.rol = rol;
        this.idUsuario = idUsuario;
        configurarVista();
        configurarEventos();
        iniciarRefresco();
    }

    private void configurarVista() {
        vista.setTitle("Sala: " + codigo + " - " + nickname + " (" + (esModerador() ? "Moderador" : "Votante") + ")");

        String[] mazo = {"0", "0.5", "1", "2", "3", "5", "8", "13", "20", "40", "100", "?", "Cafe"};
        vista.getCmbMiCarta().setModel(new javax.swing.DefaultComboBoxModel<>(mazo));
        vista.getCmbPrioridad().setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Alta", "Media", "Baja"}));
        vista.getCmbPuntosEstimados().setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "0.5", "1", "2", "3", "5", "8", "13", "20", "40", "100", "?", "Cafe"}));

        vista.getTxtTitulo().setEditable(false);
        vista.getTxtDescripcion().setEditable(false);
        vista.getCmbPrioridad().setEnabled(false);
        vista.getCmbPuntosEstimados().setEnabled(false);

        DefaultTableModel modelVotantes = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Nickname", "Rol", "Carta", "Estado"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vista.getTblVotante().setModel(modelVotantes);
    }

    private void configurarEventos() {
        vista.getBtnEmitirVoto().addActionListener(e -> emitirVoto());
    }

    private void iniciarRefresco() {
        timerRefresco = new Timer(2000, e -> refrescarEstado());
        timerRefresco.start();
        refrescarEstado();
    }

    private boolean esModerador() {
        return "PRODUCT_OWNER_MODERADOR".equals(rol);
    }

    private Connection obtenerConexion() throws Exception {
        ConexionBDD db = new ConexionBDD();
        return db.conectar();
    }

    private void emitirVoto() {
        Object carta = vista.getCmbMiCarta().getSelectedItem();
        if (carta == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione una carta.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = obtenerConexion()) {
            CallableStatement cs = conn.prepareCall("{CALL sp_emitir_voto(?, ?, ?)}");
            cs.setInt(1, idSala);
            cs.setInt(2, idUsuario);
            cs.setString(3, carta.toString());
            cs.execute();
        } catch (Exception ex) {
            System.out.println("Error al registrar voto: " + ex.getMessage());
        }
        refrescarEstado();
    }

    // Los controles de "Revelar Cartas" y "Nueva Ronda" pertenecen unicamente al
    // moderador y se gestionan desde la vista GestionHistoriaUsuarioVIsta.

    private void refrescarEstado() {
        List<VotoItem> votos = new ArrayList<>();
        String histTitulo = null, histDescripcion = null, histPrioridad = null, histPuntos = null;

        try (Connection conn = obtenerConexion()) {
            CallableStatement cs = conn.prepareCall("{CALL sp_obtener_votos(?)}");
            cs.setInt(1, idSala);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                boolean reveladas = rs.getBoolean("cartas_reveladas");
                cartasReveladas = reveladas;
                VotoItem v = new VotoItem(
                    rs.getString("nickname"),
                    rs.getString("rol"),
                    rs.getString("carta"),
                    reveladas
                );
                votos.add(v);

                if (rs.getString("historia_titulo") != null && histTitulo == null) {
                    histTitulo = rs.getString("historia_titulo");
                    histDescripcion = rs.getString("historia_descripcion");
                    histPrioridad = rs.getString("historia_prioridad");
                    histPuntos = rs.getString("historia_puntos");
                }
            }
        } catch (Exception e) {
            System.out.println("Error al obtener estado: " + e.getMessage());
        }

        DefaultTableModel modelVotantes = (DefaultTableModel) vista.getTblVotante().getModel();
        modelVotantes.setRowCount(0);
        for (VotoItem v : votos) {
            String rolMostrar = "PRODUCT_OWNER_MODERADOR".equals(v.rol) ? "Moderador" : "Votante";
            String cartaMostrar;
            String estado;
            if (v.reveladas) {
                cartaMostrar = v.carta != null && !v.carta.isEmpty() ? v.carta : "-";
                estado = "Revelado";
            } else {
                if (v.carta != null && !v.carta.isEmpty()) {
                    cartaMostrar = "-";
                    estado = "\u2713 Listo";
                } else {
                    cartaMostrar = "-";
                    estado = "Pendiente";
                }
            }
            modelVotantes.addRow(new Object[]{v.nickname, rolMostrar, cartaMostrar, estado});
        }

        if (histTitulo != null) {
            vista.getTxtTitulo().setText(histTitulo);
            vista.getTxtDescripcion().setText(histDescripcion != null ? histDescripcion : "");
            seleccionarCombo(vista.getCmbPrioridad(), histPrioridad);
            seleccionarCombo(vista.getCmbPuntosEstimados(), histPuntos);
        } else {
            vista.getTxtTitulo().setText("");
            vista.getTxtDescripcion().setText("");
        }

        actualizarMetricas();
    }

    private void actualizarMetricas() {
        try (Connection conn = obtenerConexion();
             CallableStatement cs = conn.prepareCall("{CALL sp_obtener_metricas_votos(?)}")) {
            cs.setInt(1, idSala);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    Object promedio = rs.getObject("promedio");
                    Object mediana = rs.getObject("mediana");
                    String consenso = rs.getString("consenso");
                    vista.getLblPromedio().setText(formatearMetrica(promedio));
                    vista.getLblMediana().setText(formatearMetrica(mediana));
                    vista.getLblConsenso().setText(consenso != null ? consenso : "-");
                }
            }
        } catch (Exception e) {
            vista.getLblPromedio().setText("Error");
            vista.getLblMediana().setText("Error");
            vista.getLblConsenso().setText("Error");
            System.out.println("Error al obtener metricas: " + e.getMessage());
        }
    }

    private String formatearMetrica(Object valor) {
        return valor == null ? "-" : String.format("%.2f", ((Number) valor).doubleValue());
    }

    private void seleccionarCombo(javax.swing.JComboBox<String> combo, String valor) {
        if (valor == null || valor.isEmpty()) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equals(valor)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static class VotoItem {
        String nickname;
        String rol;
        String carta;
        boolean reveladas;

        VotoItem(String nickname, String rol, String carta, boolean reveladas) {
            this.nickname = nickname;
            this.rol = rol;
            this.carta = carta;
            this.reveladas = reveladas;
        }
    }
}