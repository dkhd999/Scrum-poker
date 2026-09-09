package modelo;

public class Votante extends Usuario {

    public Votante(int idUsuario, String nickname, int idSala) {
        super(idUsuario, nickname, "VOTANTE", idSala);
    }

    @Override
    public boolean esModerador() {
        return false;
    }
}
