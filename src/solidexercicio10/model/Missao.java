package solidexercicio10.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Estado e regras de uma partida: quem está no mapa, onde, quantos pontos
 * o piloto tem e quando a missão termina.
 *
 * <p>Concentra o que a versão original espalhava entre variáveis locais da
 * {@code Main} (pontuação, movimentos, cronômetro) e ifs soltos no laço de
 * jogo. O serviço agora pergunta à missão o que aconteceu em vez de
 * calcular ele mesmo.</p>
 *
 * <p>Composição: a missão <b>é dona</b> da nave, da plataforma e das listas
 * de entidades. Quando a missão é descartada, tudo isso deixa de existir.</p>
 */
public class Missao {

    public static final int CUSTO_POR_MOVIMENTO = 1;

    private final String piloto;
    private final Dificuldade dificuldade;
    private final LimitesMapa limites;
    private final Nave nave;
    private final Plataforma plataforma = new Plataforma();

    private final List<Passageiro> passageiros = new ArrayList<>();
    private final List<Asteroide> asteroides = new ArrayList<>();
    private final List<Inimigo> inimigos = new ArrayList<>();

    private int pontuacao;
    private int movimentos;
    private final long instanteInicio = System.currentTimeMillis();

    public Missao(String piloto, Dificuldade dificuldade, LimitesMapa limites, Nave nave) {
        this.piloto = piloto;
        this.dificuldade = dificuldade;
        this.limites = limites;
        this.nave = nave;
        this.pontuacao = dificuldade.getPontuacaoInicial();
    }

    // ------------------------------------------------------------------
    // Estado
    // ------------------------------------------------------------------

    public String getPiloto() {
        return piloto;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public LimitesMapa getLimites() {
        return limites;
    }

    public Nave getNave() {
        return nave;
    }

    public Plataforma getPlataforma() {
        return plataforma;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getMovimentos() {
        return movimentos;
    }

    public int getPassageirosResgatados() {
        return nave.getQuantidadeEmbarcada();
    }

    public int getPassageirosRestantes() {
        return passageiros.size();
    }

    public long getDuracaoSegundos() {
        return (System.currentTimeMillis() - instanteInicio) / 1000;
    }

    public List<Passageiro> getPassageirosNoMapa() {
        return Collections.unmodifiableList(passageiros);
    }

    public List<Asteroide> getAsteroides() {
        return Collections.unmodifiableList(asteroides);
    }

    public List<Inimigo> getInimigos() {
        return Collections.unmodifiableList(inimigos);
    }

    /**
     * Todas as entidades do mapa em ordem de prioridade de desenho.
     *
     * <p>É o que permite ao renderizador não conhecer nenhum tipo concreto:
     * ele percorre esta lista e pede o símbolo. Um novo tipo de entidade
     * aparece no mapa sem alterar uma linha da apresentação (OCP).</p>
     */
    public List<EntidadeMapa> getEntidades() {
        List<EntidadeMapa> entidades = new ArrayList<>();
        entidades.add(nave);
        entidades.addAll(passageiros);
        entidades.addAll(asteroides);
        entidades.addAll(inimigos);
        entidades.add(plataforma);
        return Collections.unmodifiableList(entidades);
    }

    // ------------------------------------------------------------------
    // Montagem (usada pela fábrica de missões)
    // ------------------------------------------------------------------

    public void adicionarPassageiro(Passageiro passageiro) {
        passageiros.add(passageiro);
    }

    public void adicionarAsteroide(Asteroide asteroide) {
        asteroides.add(asteroide);
    }

    public void adicionarInimigo(Inimigo inimigo) {
        inimigos.add(inimigo);
    }

    public boolean posicaoOcupada(int x, int y) {
        for (EntidadeMapa entidade : getEntidades()) {
            if (entidade.estaEm(x, y)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Regras do turno
    // ------------------------------------------------------------------

    /**
     * Move a nave e cobra o combustível do movimento.
     *
     * @return true se a nave saiu do lugar.
     */
    public boolean moverNave(Direcao direcao) {
        if (direcao == null) {
            return false;
        }
        boolean moveu = nave.moverPara(
                nave.getX() + direcao.getDeslocamentoX(),
                nave.getY() + direcao.getDeslocamentoY(),
                limites);
        if (moveu) {
            movimentos++;
            pontuacao -= CUSTO_POR_MOVIMENTO;
        }
        return moveu;
    }

    /**
     * Embarca o passageiro que estiver na casa da nave e credita a
     * pontuação do tipo dele (polimorfismo).
     *
     * @return o passageiro embarcado, ou null se não havia ninguém ali ou a
     *         nave estava cheia.
     */
    public Passageiro embarcarNaPosicaoAtual() {
        Iterator<Passageiro> it = passageiros.iterator();
        while (it.hasNext()) {
            Passageiro passageiro = it.next();
            if (!passageiro.estaNaMesmaPosicaoDe(nave)) {
                continue;
            }
            if (!nave.embarcar(passageiro)) {
                return null;
            }
            it.remove();
            pontuacao += passageiro.getPontuacao();
            return passageiro;
        }
        return null;
    }

    public void moverInimigos(Random random) {
        for (Inimigo inimigo : inimigos) {
            inimigo.moverAleatoriamente(random, limites);
        }
    }

    public boolean houveColisao() {
        for (Asteroide asteroide : asteroides) {
            if (asteroide.estaNaMesmaPosicaoDe(nave)) {
                return true;
            }
        }
        for (Inimigo inimigo : inimigos) {
            if (inimigo.estaNaMesmaPosicaoDe(nave)) {
                return true;
            }
        }
        return false;
    }

    public void registrarColisao() {
        nave.perderVida();
    }

    // ------------------------------------------------------------------
    // Condições de fim de partida
    // ------------------------------------------------------------------

    public boolean todosEmbarcados() {
        return passageiros.isEmpty();
    }

    public boolean naveNaPlataforma() {
        return nave.estaNaMesmaPosicaoDe(plataforma);
    }

    /** Vitória: resgatar todos e pousar de volta em (0,0). */
    public boolean missaoCumprida() {
        return todosEmbarcados() && naveNaPlataforma();
    }

    public boolean naveDestruida() {
        return nave.estaDestruida();
    }

    public boolean combustivelEsgotado() {
        return pontuacao <= 0;
    }

    /** Fotografia imutável do desfecho, para estatísticas e ranking. */
    public ResultadoMissao gerarResultado(boolean venceu) {
        return new ResultadoMissao(piloto, pontuacao, movimentos,
                getPassageirosResgatados(), getDuracaoSegundos(), dificuldade, venceu);
    }
}
