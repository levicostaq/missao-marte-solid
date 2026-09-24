package solidexercicio10.model;

/**
 * Base de tudo que aparece no mapa.
 *
 * <p>Guarda a posição (única responsabilidade) e obriga cada subclasse a
 * dizer como é desenhada. É esse {@link #getSimbolo()} que permite ao
 * renderizador desenhar qualquer entidade sem um único
 * {@code instanceof} — ponto central do OCP nesta refatoração.</p>
 */
public abstract class EntidadeMapa implements Posicionavel {

    private int x;
    private int y;

    protected EntidadeMapa(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    protected void definirPosicao(int novoX, int novoY) {
        this.x = novoX;
        this.y = novoY;
    }

    /** Caractere usado para representar a entidade no mapa em console. */
    public abstract char getSimbolo();

    /** Nome curto exibido na legenda do mapa. */
    public abstract String getDescricao();
}
