package solidexercicio10.model;

/**
 * Fronteiras do mapa, de -tamanho a +tamanho nos dois eixos.
 *
 * <p>Na versão original os quatro valores (minX, maxX, minY, maxY) eram
 * passados soltos para praticamente todo método de movimento e colisão.
 * Concentrá-los aqui remove a repetição e dá um único lugar para a regra
 * "esta casa existe no mapa?".</p>
 */
public final class LimitesMapa {

    public static final int TAMANHO_PADRAO = 5;

    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;

    public LimitesMapa(int tamanho) {
        int t = tamanho > 0 ? tamanho : TAMANHO_PADRAO;
        this.minX = -t;
        this.maxX = t;
        this.minY = -t;
        this.maxY = t;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getTamanho() {
        return maxX;
    }

    public boolean contem(int x, int y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
}
