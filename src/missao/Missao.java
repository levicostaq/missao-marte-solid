package missao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Missao {

    private final Nave nave;
    private final List<Passageiro> passageiros = new ArrayList<>();
    private final List<Asteroide> asteroides = new ArrayList<>();
    private final List<Inimigo> inimigos = new ArrayList<>();

    public Missao(Nave nave) {
        this.nave = nave;
    }

    public Nave getNave() {
        return nave;
    }

    public List<Passageiro> getPassageiros() {
        return passageiros;
    }

    public List<Asteroide> getAsteroides() {
        return asteroides;
    }

    public List<Inimigo> getInimigos() {
        return inimigos;
    }

    public void addPassageiro(Passageiro p) {
        passageiros.add(p);
    }

    public void addAsteroide(Asteroide a) {
        asteroides.add(a);
    }

    public void addInimigo(Inimigo i) {
        inimigos.add(i);
    }

    public boolean verificaColisaoAsteroide() {
        for (Asteroide a : asteroides) {
            if (a.colideCom(nave)) {
                return true;
            }
        }
        return false;
    }

    public boolean verificaColisaoInimigo() {
        for (Inimigo i : inimigos) {
            if (i.colideCom(nave)) {
                return true;
            }
        }
        return false;
    }

    public Passageiro passageiroNaPosicaoAtual() {
        for (Passageiro p : passageiros) {
            if (p.getX() == nave.getX() && p.getY() == nave.getY()) {
                return p;
            }
        }
        return null;
    }

    public Passageiro embarcarPassageiroNaPosicaoAtual() {
        Iterator<Passageiro> it = passageiros.iterator();
        while (it.hasNext()) {
            Passageiro p = it.next();
            if (p.getX() == nave.getX() && p.getY() == nave.getY()) {
                if (nave.embarcar(p)) {
                    it.remove();
                    return p;
                }
                return null;
            }
        }
        return null;
    }

    public boolean todosEmbarcados() {
        return passageiros.isEmpty();
    }

    /**
     * Move todos os inimigos aleatoriamente (Exercício 7), chamado a cada turno.
     */
    public void moverInimigos(Random random, int minX, int maxX, int minY, int maxY) {
        for (Inimigo inimigo : inimigos) {
            inimigo.moverAleatoriamente(random, minX, maxX, minY, maxY);
        }
    }
}
