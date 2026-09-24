package solidexercicio10.model;

/**
 * Obstáculo fixo. Tem posição, logo é {@link Posicionavel}, mas não é
 * {@link Movel}: nunca sai do lugar.
 */
public class Asteroide extends EntidadeMapa {

    public Asteroide(int x, int y) {
        super(x, y);
    }

    @Override
    public char getSimbolo() {
        return '#';
    }

    @Override
    public String getDescricao() {
        return "asteroide";
    }
}
