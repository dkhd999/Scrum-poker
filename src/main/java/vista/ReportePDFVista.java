package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ReportePDFVista extends JFrame {

    private final JLabel lblSala = new JLabel();
    private final JLabel lblCodigoSala = new JLabel();
    private final JTextField txtNombreArchivo = new JTextField("reporte_sala.pdf", 24);
    private final JTextField txtUbicacion = new JTextField(24);
    private final JButton btnExaminar = new JButton("Examinar...");
    private final JButton btnExportar = new JButton("Exportar PDF");
    private final JButton btnCancelar = new JButton("Cancelar");

    public ReportePDFVista() {
        setTitle("Exportar reporte PDF");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        construirVista();
        pack();
        setLocationRelativeTo(null);
    }

    private void construirVista() {
        JLabel titulo = new JLabel("Exportar reporte de la sala");
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(6, 6, 6, 6);
        restricciones.anchor = GridBagConstraints.WEST;

        agregarCampo(formulario, restricciones, 0, "Sala:", lblSala);
        agregarCampo(formulario, restricciones, 1, "Codigo:", lblCodigoSala);
        agregarCampo(formulario, restricciones, 2, "Nombre del archivo:", txtNombreArchivo);
        agregarCampo(formulario, restricciones, 3, "Ubicacion:", txtUbicacion);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.add(btnCancelar);
        acciones.add(btnExportar);

        JPanel inferior = new JPanel(new BorderLayout(8, 0));
        inferior.add(btnExaminar, BorderLayout.WEST);
        inferior.add(acciones, BorderLayout.EAST);

        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
        contenido.add(titulo, BorderLayout.NORTH);
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(inferior, BorderLayout.SOUTH);
        setContentPane(contenido);
    }

    private void agregarCampo(JPanel panel, GridBagConstraints restricciones, int fila,
            String etiqueta, java.awt.Component componente) {
        restricciones.gridx = 0;
        restricciones.gridy = fila;
        restricciones.weightx = 0;
        restricciones.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(etiqueta), restricciones);
        restricciones.gridx = 1;
        restricciones.weightx = 1;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        panel.add(componente, restricciones);
    }

    public JLabel getLblSala() { return lblSala; }
    public JLabel getLblCodigoSala() { return lblCodigoSala; }
    public JTextField getTxtNombreArchivo() { return txtNombreArchivo; }
    public JTextField getTxtUbicacion() { return txtUbicacion; }
    public JButton getBtnExaminar() { return btnExaminar; }
    public JButton getBtnExportar() { return btnExportar; }
    public JButton getBtnCancelar() { return btnCancelar; }
}
