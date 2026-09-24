package solidexercicio10.service;

import java.util.Random;

import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Direcao;
import solidexercicio10.model.LimitesMapa;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Passageiro;
import solidexercicio10.model.ResultadoMissao;
import solidexercicio10.presentation.TelaJogo;

/**
 * Fluxo da aplicação: menu principal e o laço de uma partida.
 *
 * <p>Orquestra, não executa. Não desenha mapa (é o renderizador), não sabe
 * o texto das mensagens (é a tela), não calcula pontuação nem colisão (é a
 * missão) e não sabe onde o ranking é guardado (é o repositório, atrás do
 * serviço de ranking).</p>
 *
 * <p>Tudo o que ele usa chega pelo construtor, então dá para rodar este
 * serviço com outra tela ou outro ranking sem tocar no código.</p>
 */
public class JogoService {

    private static final String PILOTO_PADRAO = "Piloto Anônimo";

    private static final char COMANDO_EMBARCAR = 'c';
    private static final char COMANDO_ABORTAR = 'q';
    private static final char SEM_COMANDO = '\0';

    private final TelaJogo tela;
    private final RankingService rankingService;
    private final FabricaDeMissao fabricaDeMissao;
    private final Random random;

    public JogoService(TelaJogo tela, RankingService rankingService,
                       FabricaDeMissao fabricaDeMissao, Random random) {
        this.tela = tela;
        this.rankingService = rankingService;
        this.fabricaDeMissao = fabricaDeMissao;
        this.random = random;
    }

    // ------------------------------------------------------------------
    // Menu principal
    // ------------------------------------------------------------------

    public void executar() {
        tela.exibirBoasVindas();

        boolean rodando = true;
        while (rodando) {
            tela.exibirMenuPrincipal();
            if (!tela.temEntrada()) {
                tela.anunciarEntradaEncerrada();
                break;
            }

            String opcao = tela.lerOpcaoMenu();
            switch (opcao) {
                case "1":
                    jogarPartida();
                    break;
                case "2":
                    tela.exibirRanking(rankingService.listar());
                    break;
                case "3":
                    resetarRanking();
                    break;
                case "4":
                    rodando = false;
                    tela.exibirDespedida();
                    break;
                default:
                    tela.anunciarOpcaoInvalida();
            }
        }
        tela.fechar();
    }

    private void resetarRanking() {
        if (tela.confirmarReset()) {
            rankingService.resetar();
            tela.anunciarRankingResetado();
        } else {
            tela.anunciarOperacaoCancelada();
        }
    }

    // ------------------------------------------------------------------
    // Partida
    // ------------------------------------------------------------------

    private void jogarPartida() {
        Missao missao = fabricaDeMissao.criar(lerPiloto(), tela.pedirDificuldade(),
                new LimitesMapa(tela.pedirTamanhoMapa()));

        boolean venceu = executarTurnos(missao);

        ResultadoMissao resultado = missao.gerarResultado(venceu);
        tela.exibirEstatisticas(resultado, rankingService.recorde());
        if (rankingService.registrar(resultado)) {
            tela.anunciarRegistroNoRanking();
        }
    }

    private String lerPiloto() {
        String piloto = tela.pedirNomePiloto();
        return piloto.isEmpty() ? PILOTO_PADRAO : piloto;
    }

    /** @return true se a missão foi cumprida. */
    private boolean executarTurnos(Missao missao) {
        while (true) {
            tela.exibirMapa(missao);

            if (!tela.temEntrada()) {
                tela.anunciarEntradaEncerrada();
                return false;
            }

            char comando = tela.lerComando();
            if (comando == SEM_COMANDO) {
                continue;
            }
            if (comando == COMANDO_ABORTAR) {
                tela.anunciarMissaoAbortada();
                return false;
            }
            if (!aplicarComando(missao, comando)) {
                continue;
            }

            missao.moverInimigos(random);

            if (missao.houveColisao()) {
                missao.registrarColisao();
                if (missao.naveDestruida()) {
                    tela.anunciarNaveDestruida();
                    return false;
                }
                tela.anunciarColisao(missao.getNave().getVidas());
            }

            if (missao.combustivelEsgotado()) {
                tela.anunciarCombustivelEsgotado();
                return false;
            }

            if (missao.todosEmbarcados()) {
                if (missao.naveNaPlataforma()) {
                    tela.anunciarVitoria();
                    return true;
                }
                tela.anunciarRetornoParaPlataforma();
            }
        }
    }

    /**
     * Executa a ação do jogador.
     *
     * @return true se o turno deve seguir (inimigos se movem, colisões são
     *         checadas); false se a tecla era inválida e nada aconteceu.
     */
    private boolean aplicarComando(Missao missao, char comando) {
        if (comando == COMANDO_EMBARCAR) {
            Passageiro embarcado = missao.embarcarNaPosicaoAtual();
            if (embarcado != null) {
                tela.anunciarEmbarque(embarcado);
            } else {
                tela.anunciarNadaParaEmbarcar();
            }
            return true;
        }

        Direcao direcao = Direcao.deTecla(comando);
        if (direcao == null) {
            tela.anunciarComandoDesconhecido();
            return false;
        }
        if (!missao.moverNave(direcao)) {
            tela.anunciarLimiteDoMapa();
        }
        return true;
    }
}
