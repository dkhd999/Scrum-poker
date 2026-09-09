package modelo;

public class ProductOwnerModerador extends Usuario {

    public ProductOwnerModerador(int idUsuario, String nickname, int idSala) {
        super(idUsuario, nickname, "PRODUCT_OWNER_MODERADOR", idSala);
    }

    @Override
    public boolean esModerador() {
        return true;
    }
}
