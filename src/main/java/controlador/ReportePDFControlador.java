package controlador;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import modelo.ReporteSala;
import vista.ReportePDFVista;

public class ReportePDFControlador {

    private final ReportePDFVista vista;
    private final int idSala;
    private final String codigoSala;

    public ReportePDFControlador(ReportePDFVista vista, int idSala, String codigoSala) {
        this.vista = vista;
        this.idSala = idSala;
        this.codigoSala = codigoSala;
        configurarVista();
        configurarEventos();
    }

    private void configurarVista() {
        vista.getLblSala().setText(String.valueOf(idSala));
        vista.getLblCodigoSala().setText(codigoSala == null ? "-" : codigoSala);
        vista.getTxtUbicacion().setText(new File(".").getAbsolutePath());
        vista.getTxtUbicacion().setEditable(false);
    }

    private void configurarEventos() {
        vista.getBtnExaminar().addActionListener(e -> seleccionarUbicacion());
        vista.getBtnExportar().addActionListener(e -> exportar());
        vista.getBtnCancelar().addActionListener(e -> vista.dispose());
    }

    private void seleccionarUbicacion() {
        JFileChooser selector = new JFileChooser();
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        selector.setDialogTitle("Seleccione la carpeta del reporte");
        if (selector.showOpenDialog(vista) == JFileChooser.APPROVE_OPTION) {
            vista.getTxtUbicacion().setText(selector.getSelectedFile().getAbsolutePath());
        }
    }

    private void exportar() {
        String nombre = vista.getTxtNombreArchivo().getText().trim();
        String ubicacion = vista.getTxtUbicacion().getText().trim();
        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Ingrese el nombre del archivo y seleccione una ubicacion.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!nombre.toLowerCase().endsWith(".pdf")) nombre += ".pdf";

        try {
            File archivo = new File(ubicacion, nombre);
            ReportePDF.generar(ReporteSala.obtener(idSala), archivo);
            JOptionPane.showMessageDialog(vista,
                    "Reporte exportado correctamente en:\n" + archivo.getAbsolutePath(),
                    "Exportacion", JOptionPane.INFORMATION_MESSAGE);
            vista.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "No se pudo exportar el reporte:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
