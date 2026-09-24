package missao;

public class Asteroide {

    private final int x;
    private final int y;

    public Asteroide(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean colideCom(Nave nave) {
        return nave.getX() == x && nave.getY() == y;
    }
}
