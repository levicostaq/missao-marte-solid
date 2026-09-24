package solidexercicio10.model;

/**
 * Contrato mínimo de qualquer coisa que ocupa uma casa do mapa.
 *
 * <p>Interface pequena de propósito (ISP): quem só precisa saber "onde isto
 * está" depende apenas destes dois métodos, sem herdar operações de
 * movimento, pontuação ou renderização.</p>
 */
public interface Posicionavel {

    int getX();

    int getY();

    default boolean estaEm(int x, int y) {
        return getX() == x && getY() == y;
    }

    default boolean estaNaMesmaPosicaoDe(Posicionavel outro) {
        return outro != null && estaEm(outro.getX(), outro.getY());
    }
}
