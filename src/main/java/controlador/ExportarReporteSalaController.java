package controlador;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import modelo.ReporteSala;

public class ExportarReporteSalaController {

    private final int idSala;

    public ExportarReporteSalaController(int idSala) {
        this.idSala = idSala;
    }

    public void exportar(javax.swing.JFrame padre) {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("reporte_sala_" + idSala + ".pdf"));
        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) return;
        try {
            ReportePDF.generar(ReporteSala.obtener(idSala), selector.getSelectedFile());
            JOptionPane.showMessageDialog(padre, "Reporte exportado correctamente.",
                    "Exportacion", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(padre, "No se pudo exportar el reporte:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
