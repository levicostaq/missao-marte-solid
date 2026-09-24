package missao;

import java.util.ArrayList;
import java.util.List;

public class Nave {

    private static final int VIDAS_INICIAIS = 3;

    private final String id;
    private int x;
    private int y;
    private final int capacidade;
    private int vidas;
    private final List<Passageiro> passageiros = new ArrayList<>();

    public Nave(String id, int capacidade) {
        this.id = id;
        this.capacidade = capacidade;
        this.x = 0;
        this.y = 0;
        this.vidas = VIDAS_INICIAIS;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getVidas() {
        return vidas;
    }

    public List<Passageiro> getPassageiros() {
        return passageiros;
    }

    public void moveUp() {
        y--;
    }

    public void moveDown() {
        y++;
    }

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }

    /**
     * Move a nave respeitando os limites do mapa configurável (Exercício 6).
     * Retorna true se o movimento foi realmente aplicado.
     */
    public boolean moverComLimites(char comando, int minX, int maxX, int minY, int maxY) {
        int novoX = x;
        int novoY = y;

        switch (comando) {
            case 'w':
                novoY = y - 1;
                break;
            case 's':
                novoY = y + 1;
                break;
            case 'a':
                novoX = x - 1;
                break;
            case 'd':
                novoX = x + 1;
                break;
            default:
                return false;
        }

        if (novoX < minX || novoX > maxX || novoY < minY || novoY > maxY) {
            return false;
        }

        x = novoX;
        y = novoY;
        return true;
    }

    public boolean embarcar(Passageiro p) {
        if (passageiros.size() < capacidade) {
            passageiros.add(p);
            return true;
        }
        return false;
    }

    /**
     * Reduz uma vida ao colidir com um obstáculo (Exercício 5).
     */
    public void perderVida() {
        if (vidas > 0) {
            vidas--;
        }
    }
}
