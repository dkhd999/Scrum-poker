package controlador;

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import modelo.EstadoSala;
import modelo.HistoriaUsuario;
import modelo.MetricasVoto;
import modelo.Sala;
import modelo.Voto;
import vista.GestionHistoriaUsuarioVIsta;
import vista.MenuModerador;

public class GestionHistoriaUsuarioControlador {

    private GestionHistoriaUsuarioVIsta vista;
    private final int idSala;
    private final int idUsuario;
    private final String codigo;
    private final String nickname;
    private final MenuModerador menuPadre;
    private Timer timerRefresco;
    private boolean actualizandoTabla = false;
    private boolean preparandoNuevaHistoria = false;
    private int historiaActivaId = -1;

    public GestionHistoriaUsuarioControlador(GestionHistoriaUsuarioVIsta vista, int idSala, String codigo, int idUsuario, String nickname, MenuModerador menuPadre) {
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
        vista.getTxtCodigoSala().setText(codigo);
        vista.getTxtCodigoSala().setEditable(false);
        vista.getTxtUsuario().setText(nickname);
        vista.getTxtUsuario().setEditable(false);
        vista.getTxtRol().setText("Moderador");
        vista.getTxtRol().setEditable(false);

        vista.getCmbPrioridad().setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Alta", "Media", "Baja"}));
        vista.getCmbPuntosEstimados().setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "0.5", "1", "2", "3", "5", "8", "13", "20", "40", "100", "?", "Cafe"}));
        vista.getCmbMiCarta().setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "0.5", "1", "2", "3", "5", "8", "13", "20", "40", "100", "?", "Cafe"}));

        DefaultTableModel modelHistoria = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Titulo", "Descripcion", "Prioridad", "Puntos Estimados"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vista.getTblHistoriaUsuario().setModel(modelHistoria);

        DefaultTableModel modelVotantes = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Nickname", "Rol", "Carta", "Estado"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vista.getTblVerVotante().setModel(modelVotantes);
    }

    private void configurarEventos() {
        vista.getGuardarHistoria().addActionListener(e -> guardarHistoria());
        vista.getGuardarActualizarHistoria().addActionListener(e -> actualizarHistoria());
        vista.getBtnVotar().addActionListener(e -> emitirVotoDelModerador());
        vista.getBtnRevelarCartas().addActionListener(e -> revelarCartas());
        vista.getBtnNuevaRonda().addActionListener(e -> nuevaRonda());
        vista.getBtnAgregarHistoria().addActionListener(e -> agregarOtraHistoria());
        vista.getBtnVolver().addActionListener(e -> volverAlMenu());
    }

    private void agregarOtraHistoria() {
        try {
            Sala.prepararNuevaHistoria(idSala, idUsuario);
            preparandoNuevaHistoria = true;
            historiaActivaId = -1;
            limpiarFormularioNuevaHistoria();
            refrescarEstado();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "No se pudo preparar la nueva historia.\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Error al agregar otra historia: " + ex.getMessage());
        }
    }

    private void limpiarFormularioNuevaHistoria() {
        vista.getTxtTitulo().setText("");
        vista.getTxtDescripcion().setText("");
        vista.getCmbPrioridad().setSelectedIndex(0);
        vista.getCmbPuntosEstimados().setSelectedIndex(0);
        vista.getJLabel10().setText("Promedio: -");
        vista.getJLabel11().setText("Mediana: -");
        vista.getJLabel12().setText("Consenso: -");
    }

    private void revelarCartas() {
        try {
            Sala.revelarCartas(idSala);
        } catch (Exception ex) {
            System.out.println("Error al revelar cartas: " + ex.getMessage());
        }
        refrescarEstado();
    }

    private void nuevaRonda() {
        String titulo = vista.getTxtTitulo().getText().trim();
        String descripcion = vista.getTxtDescripcion().getText().trim();
        String prioridad = (String) vista.getCmbPrioridad().getSelectedItem();
        String puntos = (String) vista.getCmbPuntosEstimados().getSelectedItem();
        try {
            Sala.reiniciarVotacion(idSala, idUsuario, titulo, descripcion, prioridad, puntos);
        } catch (Exception ex) {
            System.out.println("Error al iniciar nueva ronda: " + ex.getMessage());
        }
        refrescarEstado();
    }

    private void volverAlMenu() {
        timerRefresco.stop();
        vista.dispose();
        if (menuPadre != null) {
            menuPadre.setVisible(true);
        }
    }

    private void iniciarRefresco() {
        timerRefresco = new Timer(2000, e -> refrescarEstado());
        timerRefresco.start();
        refrescarEstado();
    }

    private void guardarHistoria() {
        String titulo = vista.getTxtTitulo().getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "El titulo es obligatorio.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String descripcion = vista.getTxtDescripcion().getText().trim();
        String prioridad = (String) vista.getCmbPrioridad().getSelectedItem();
        String puntos = (String) vista.getCmbPuntosEstimados().getSelectedItem();
        crearHistoriaDB(idUsuario, titulo, descripcion, prioridad, puntos);
        preparandoNuevaHistoria = false;
        JOptionPane.showMessageDialog(vista, "Historia guardada correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
        refrescarEstado();
    }

    private void actualizarHistoria() {
        String titulo = vista.getTxtTitulo().getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "El titulo es obligatorio.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String descripcion = vista.getTxtDescripcion().getText().trim();
        String prioridad = (String) vista.getCmbPrioridad().getSelectedItem();
        String puntos = (String) vista.getCmbPuntosEstimados().getSelectedItem();
        actualizarHistoriaDB(historiaActivaId, idUsuario, titulo, descripcion, prioridad, puntos);
        preparandoNuevaHistoria = false;
        JOptionPane.showMessageDialog(vista, "Historia actualizada correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
        refrescarEstado();
    }

    private void emitirVotoDelModerador() {
        Object carta = vista.getCmbMiCarta().getSelectedItem();
        if (carta == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione una carta.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Voto.emitir(idSala, idUsuario, carta.toString());
        } catch (Exception ex) {
            System.out.println("Error al votar (moderador): " + ex.getMessage());
        }
        refrescarEstado();
    }

    private void refrescarEstado() {
        try {
            EstadoSala estado = EstadoSala.obtener(idSala);
            HistoriaUsuario historia = estado.getHistoriaActiva();
            if (!preparandoNuevaHistoria && historia != null) {
                historiaActivaId = historia.getIdHistoria();
                vista.getTxtTitulo().setText(historia.getTitulo());
                vista.getTxtDescripcion().setText(historia.getDescripcion() != null ? historia.getDescripcion() : "");
                vista.getCmbPrioridad().setSelectedItem(historia.getPrioridad());
                vista.getCmbPuntosEstimados().setSelectedItem(historia.getPuntosEstimados());
            }
            refrescarHistorial();
            refrescarVotantes(estado);
            actualizarMetricas();
        } catch (Exception e) {
            System.out.println("Error al obtener estado: " + e.getMessage());
        }
    }

    private void refrescarVotantes(EstadoSala estadoSala) {
        DefaultTableModel modelVotantes = (DefaultTableModel) vista.getTblVerVotante().getModel();
        modelVotantes.setRowCount(0);
        for (Voto voto : estadoSala.getVotos()) {
            String nick = voto.getNickname();
            String rolUsu = voto.getRol();
            String carta = voto.getCarta();
            String rolMostrar = "PRODUCT_OWNER_MODERADOR".equals(rolUsu) ? "Moderador" : "Votante";
            String cartaMostrar;
            String estadoMostrar;
            if (estadoSala.isCartasReveladas()) {
                cartaMostrar = carta != null ? carta : "-";
                estadoMostrar = "Revelado";
            } else {
                if (carta != null && !carta.isEmpty()) {
                    cartaMostrar = "-";
                    estadoMostrar = "\u2713 Listo";
                } else {
                    cartaMostrar = "-";
                    estadoMostrar = "Pendiente";
                }
            }
            modelVotantes.addRow(new Object[]{nick, rolMostrar, cartaMostrar, estadoMostrar});
        }
    }

    private void refrescarHistorial() {
        List<HistoriaUsuario> historial;
        try {
            historial = HistoriaUsuario.obtenerHistorial(idSala);
        } catch (Exception e) {
            System.out.println("Error al obtener historial: " + e.getMessage());
            return;
        }
        DefaultTableModel modelHistoria = (DefaultTableModel) vista.getTblHistoriaUsuario().getModel();
        actualizandoTabla = true;
        try {
            modelHistoria.setRowCount(0);
            for (HistoriaUsuario historia : historial) {
                modelHistoria.addRow(new Object[]{historia.getTitulo(), historia.getDescripcion(),
                    historia.getPrioridad(), historia.getPuntosEstimados()});
            }
        } finally {
            actualizandoTabla = false;
        }
    }

    private void actualizarMetricas() {
        try {
            MetricasVoto metricas = MetricasVoto.obtenerPorSala(idSala);
            vista.getJLabel10().setText("Promedio: " + formatearMetrica(metricas.getPromedio()));
            vista.getJLabel11().setText("Mediana: " + formatearMetrica(metricas.getMediana()));
            vista.getJLabel12().setText("Consenso: " + (metricas.getConsenso() != null ? metricas.getConsenso() : "-"));
        } catch (Exception e) {
            vista.getJLabel10().setText("Promedio: Error");
            vista.getJLabel11().setText("Mediana: Error");
            vista.getJLabel12().setText("Consenso: Error");
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

    private void crearHistoriaDB(int idUsuario, String titulo, String descripcion, String prioridad, String puntosEstimados) {
        try {
            HistoriaUsuario.crear(idUsuario, titulo, descripcion, prioridad, puntosEstimados);
        } catch (Exception e) {
            System.out.println("Error al guardar historia: " + e.getMessage());
        }
    }

    private void actualizarHistoriaDB(int idHistoria, int idUsuario, String titulo, String descripcion, String prioridad, String puntosEstimados) {
        if (idHistoria < 0) {
            crearHistoriaDB(idUsuario, titulo, descripcion, prioridad, puntosEstimados);
            return;
        }
        try {
            HistoriaUsuario.actualizar(idHistoria, idUsuario, titulo, descripcion, prioridad, puntosEstimados);
        } catch (Exception e) {
            System.out.println("Error al actualizar historia: " + e.getMessage());
        }
    }
}