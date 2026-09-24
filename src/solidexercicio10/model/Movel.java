package solidexercicio10.model;

/**
 * Entidade que pode trocar de casa durante a partida.
 *
 * <p>Separada de {@link Posicionavel} porque asteroides e a plataforma de
 * pouso têm posição mas nunca se movem: obrigá-los a implementar um método
 * de movimento seria violação de ISP.</p>
 */
public interface Movel extends Posicionavel {

    /**
     * Tenta ocupar a casa informada.
     *
     * @return true se o movimento foi aplicado, false se o destino está fora
     *         dos limites do mapa.
     */
    boolean moverPara(int novoX, int novoY, LimitesMapa limites);
}
