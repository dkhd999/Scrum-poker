package vista;

import controlador.LoginControlador;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        java.awt.EventQueue.invokeLater(() -> {
            LoginVista loginVista = new LoginVista();
            LoginControlador loginController = new LoginControlador();
            loginController.setVista(loginVista);
            loginVista.setVisible(true);
        });
    }
}