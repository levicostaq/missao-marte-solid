package solidexercicio10.model;

import java.util.Random;

/**
 * Entidade hostil que muda de casa a cada turno.
 *
 * <p>Recebe o {@link Random} por parâmetro em vez de criar o seu próprio:
 * assim um teste pode fixar a semente e reproduzir exatamente a mesma
 * partida.</p>
 */
public class Inimigo extends EntidadeMapa implements Movel {

    public Inimigo(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean moverPara(int novoX, int novoY, LimitesMapa limites) {
        if (!limites.contem(novoX, novoY)) {
            return false;
        }
        definirPosicao(novoX, novoY);
        return true;
    }

    /** Sorteia uma das quatro direções e tenta seguir por ela. */
    public void moverAleatoriamente(Random random, LimitesMapa limites) {
        Direcao[] direcoes = Direcao.values();
        Direcao escolhida = direcoes[random.nextInt(direcoes.length)];
        moverPara(getX() + escolhida.getDeslocamentoX(),
                getY() + escolhida.getDeslocamentoY(),
                limites);
    }

    @Override
    public char getSimbolo() {
        return 'X';
    }

    @Override
    public String getDescricao() {
        return "inimigo";
    }
}
