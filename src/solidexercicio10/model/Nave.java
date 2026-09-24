package solidexercicio10.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A nave do piloto: posição, vidas e passageiros a bordo.
 *
 * <p>Responsabilidade única: manter o estado interno da nave consistente.
 * Ela não sabe ler teclado (isso é {@link Direcao} e camada de
 * apresentação), não calcula pontuação (isso é {@link Missao}) e não
 * decide se a partida acabou.</p>
 */
public class Nave extends EntidadeMapa implements Movel {

    public static final int VIDAS_INICIAIS = 3;
    public static final int CAPACIDADE_PADRAO = 5;

    private final String id;
    private final int capacidade;
    private int vidas;
    private final List<Passageiro> passageiros = new ArrayList<>();

    public Nave(String id, int capacidade) {
        super(0, 0);
        this.id = id;
        this.capacidade = capacidade > 0 ? capacidade : CAPACIDADE_PADRAO;
        this.vidas = VIDAS_INICIAIS;
    }

    public String getId() {
        return id;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getVidas() {
        return vidas;
    }

    /**
     * Lista somente-leitura. A versão original devolvia a lista interna, o
     * que permitia a qualquer classe embarcar passageiros por fora,
     * furando a checagem de capacidade.
     */
    public List<Passageiro> getPassageiros() {
        return Collections.unmodifiableList(passageiros);
    }

    public int getQuantidadeEmbarcada() {
        return passageiros.size();
    }

    public boolean estaCheia() {
        return passageiros.size() >= capacidade;
    }

    public boolean estaDestruida() {
        return vidas <= 0;
    }

    @Override
    public boolean moverPara(int novoX, int novoY, LimitesMapa limites) {
        if (!limites.contem(novoX, novoY)) {
            return false;
        }
        definirPosicao(novoX, novoY);
        return true;
    }

    public boolean embarcar(Passageiro passageiro) {
        if (passageiro == null || estaCheia()) {
            return false;
        }
        passageiros.add(passageiro);
        return true;
    }

    public void perderVida() {
        if (vidas > 0) {
            vidas--;
        }
    }

    @Override
    public char getSimbolo() {
        return '@';
    }

    @Override
    public String getDescricao() {
        return "nave " + id;
    }
}
