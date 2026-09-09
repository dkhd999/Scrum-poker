package controlador;

import vista.AgregarDevVista;
import vista.GestionHistoriaUsuarioVIsta;
import vista.MenuModerador;

public class MenuModeradorController {

    private MenuModerador vista;
    private final int idSala;
    private final int idUsuario;
    private final String codigo;
    private final String nickname;

    public MenuModeradorController(MenuModerador vista, int idSala, String codigo, int idUsuario, String nickname) {
        this.vista = vista;
        this.idSala = idSala;
        this.codigo = codigo;
        this.idUsuario = idUsuario;
        this.nickname = nickname;
        configurarEventos();
    }

    private void configurarEventos() {
        vista.getBtnAgregarDev().addActionListener(e -> abrirAgregarDev());
        vista.getBrtnGestionHisatoriaUsuario().addActionListener(e -> abrirGestionHistoria());
    }

    private void abrirAgregarDev() {
        AgregarDevVista devVista = new AgregarDevVista();
        new AgregarDevController(devVista, idSala, codigo, idUsuario, nickname, vista);
        devVista.setVisible(true);
    }

    private void abrirGestionHistoria() {
        GestionHistoriaUsuarioVIsta historiaVista = new GestionHistoriaUsuarioVIsta();
        new GestionHistoriaUsuarioController(historiaVista, idSala, codigo, idUsuario, nickname, vista);
        historiaVista.setVisible(true);
    }
}