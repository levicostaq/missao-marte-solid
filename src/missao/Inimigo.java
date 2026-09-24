package missao;

import java.util.Random;

/**
 * Entidade hostil que se desloca aleatoriamente pelo mapa a cada turno.
 */
public class Inimigo {

    private int x;
    private int y;

    public Inimigo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Move o inimigo em uma direção aleatória, respeitando os limites do mapa.
     */
    public void moverAleatoriamente(Random random, int minX, int maxX, int minY, int maxY) {
        int direcao = random.nextInt(4);
        switch (direcao) {
            case 0:
                if (y > minY) y--;
                break;
            case 1:
                if (y < maxY) y++;
                break;
            case 2:
                if (x > minX) x--;
                break;
            case 3:
                if (x < maxX) x++;
                break;
            default:
                break;
        }
    }

    public boolean colideCom(Nave nave) {
        return nave.getX() == x && nave.getY() == y;
    }
}
