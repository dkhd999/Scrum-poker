package controlador;

import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import modelo.EstadoSala;
import modelo.MetricasVoto;
import modelo.Voto;
import vista.RegistrarVotoVista;

public class RegistrarVotoControlador {

    private RegistrarVotoVista vista;
    private final int idSala;
    private final int idUsuario;
    private final String codigo;
    private final String nickname;
    private final String rol;
    private Timer timerRefresco;
    private boolean cartasReveladas = false;

    public RegistrarVotoControlador(RegistrarVotoVista vista, int idSala, String codigo, String nickname, String rol, int idUsuario) {
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

    private void emitirVoto() {
        Object carta = vista.getCmbMiCarta().getSelectedItem();
        if (carta == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione una carta.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Voto.emitir(idSala, idUsuario, carta.toString());
        } catch (Exception ex) {
            System.out.println("Error al registrar voto: " + ex.getMessage());
        }
        refrescarEstado();
    }

    // Los controles de "Revelar Cartas" y "Nueva Ronda" pertenecen unicamente al
    // moderador y se gestionan desde la vista GestionHistoriaUsuarioVIsta.

    private void refrescarEstado() {
        EstadoSala estado;
        try {
            estado = EstadoSala.obtener(idSala);
            cartasReveladas = estado.isCartasReveladas();
        } catch (Exception e) {
            System.out.println("Error al obtener estado: " + e.getMessage());
            return;
        }

        DefaultTableModel modelVotantes = (DefaultTableModel) vista.getTblVotante().getModel();
        modelVotantes.setRowCount(0);
        for (Voto v : estado.getVotos()) {
            String rolMostrar = "PRODUCT_OWNER_MODERADOR".equals(v.getRol()) ? "Moderador" : "Votante";
            String cartaMostrar;
            String estadoMostrar;
            if (v.isCartasReveladas()) {
                cartaMostrar = v.getCarta() != null && !v.getCarta().isEmpty() ? v.getCarta() : "-";
                estadoMostrar = "Revelado";
            } else {
                if (v.getCarta() != null && !v.getCarta().isEmpty()) {
                    cartaMostrar = "-";
                    estadoMostrar = "\u2713 Listo";
                } else {
                    cartaMostrar = "-";
                    estadoMostrar = "Pendiente";
                }
            }
            modelVotantes.addRow(new Object[]{v.getNickname(), rolMostrar, cartaMostrar, estadoMostrar});
        }

        if (estado.getHistoriaActiva() != null) {
            vista.getTxtTitulo().setText(estado.getHistoriaActiva().getTitulo());
            vista.getTxtDescripcion().setText(estado.getHistoriaActiva().getDescripcion() != null ? estado.getHistoriaActiva().getDescripcion() : "");
            seleccionarCombo(vista.getCmbPrioridad(), estado.getHistoriaActiva().getPrioridad());
            seleccionarCombo(vista.getCmbPuntosEstimados(), estado.getHistoriaActiva().getPuntosEstimados());
        } else {
            vista.getTxtTitulo().setText("");
            vista.getTxtDescripcion().setText("");
        }

        actualizarMetricas();
    }

    private void actualizarMetricas() {
        try {
            MetricasVoto metricas = MetricasVoto.obtenerPorSala(idSala);
            vista.getLblPromedio().setText(formatearMetrica(metricas.getPromedio()));
            vista.getLblMediana().setText(formatearMetrica(metricas.getMediana()));
            vista.getLblConsenso().setText(metricas.getConsenso() != null ? metricas.getConsenso() : "-");
        } catch (Exception e) {
            vista.getLblPromedio().setText("Error");
            vista.getLblMediana().setText("Error");
            vista.getLblConsenso().setText("Error");
            System.out.println("Error al obtener metricas: " + e.getMessage());
        }
    }

    private String formatearMetrica(Double valor) {
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

}