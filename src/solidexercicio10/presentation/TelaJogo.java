package solidexercicio10.presentation;

import java.util.List;

import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.LimitesMapa;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Passageiro;
import solidexercicio10.model.RankingEntry;
import solidexercicio10.model.ResultadoMissao;

/**
 * Toda a conversa com o jogador: menus, perguntas, mensagens de turno,
 * estatísticas e ranking.
 *
 * <p>Motivo de existir: o serviço de jogo não deve conhecer texto de
 * interface. Ele chama {@code anunciarColisao(2)} e quem decide as
 * palavras, o idioma e o formato é esta classe. Trocar o console por uma
 * janela gráfica passa a ser reescrever esta camada.</p>
 */
public class TelaJogo {

    private final Console console;
    private final MapaRenderer mapaRenderer;

    public TelaJogo(Console console, MapaRenderer mapaRenderer) {
        this.console = console;
        this.mapaRenderer = mapaRenderer;
    }

    // ------------------------------------------------------------------
    // Menu principal
    // ------------------------------------------------------------------

    public void exibirBoasVindas() {
        console.escrever("========================================");
        console.escrever("        MISSAO MARTE UNIFOR");
        console.escrever("      versao refatorada com SOLID");
        console.escrever("========================================");
    }

    public void exibirMenuPrincipal() {
        console.escreverLinhaVazia();
        console.escrever("--- MENU PRINCIPAL ---");
        console.escrever("1. Iniciar Nova Missão");
        console.escrever("2. Visualizar Ranking Top 5");
        console.escrever("3. Resetar Histórico de Ranking");
        console.escrever("4. Sair do Jogo");
    }

    public String lerOpcaoMenu() {
        return console.lerLinha("Escolha uma opção: ");
    }

    public boolean temEntrada() {
        return console.temEntrada();
    }

    // ------------------------------------------------------------------
    // Configuração da partida
    // ------------------------------------------------------------------

    public String pedirNomePiloto() {
        console.escreverLinhaVazia();
        return console.lerLinha("Nome do piloto: ");
    }

    public Dificuldade pedirDificuldade() {
        console.escrever("Dificuldade: [1] Fácil  [2] Médio  [3] Difícil");
        return Dificuldade.deTexto(console.lerLinha("Escolha (1-3, padrão Médio): "));
    }

    public int pedirTamanhoMapa() {
        return console.lerInteiro(
                "Tamanho do mapa, de -N a +N (padrão " + LimitesMapa.TAMANHO_PADRAO + "): ",
                LimitesMapa.TAMANHO_PADRAO);
    }

    // ------------------------------------------------------------------
    // Turno
    // ------------------------------------------------------------------

    public void exibirMapa(Missao missao) {
        mapaRenderer.renderizar(missao);
    }

    public char lerComando() {
        console.escrever("Comandos: w/a/s/d mover | c embarcar | q abortar");
        return console.lerTecla("Comando: ");
    }

    public void anunciarEmbarque(Passageiro passageiro) {
        console.escrever(passageiro.getNome() + " (" + passageiro.getTipo() + ") embarcado! +"
                + passageiro.getPontuacao() + " pontos.");
    }

    public void anunciarNadaParaEmbarcar() {
        console.escrever("Nada para embarcar aqui (ou nave cheia).");
    }

    public void anunciarLimiteDoMapa() {
        console.escrever("Limite do mapa alcançado.");
    }

    public void anunciarComandoDesconhecido() {
        console.escrever("Comando desconhecido.");
    }

    public void anunciarColisao(int vidasRestantes) {
        console.escrever("Colisão! Vidas restantes: " + vidasRestantes);
    }

    public void anunciarNaveDestruida() {
        console.escrever("GAME OVER: a nave foi destruída.");
    }

    public void anunciarCombustivelEsgotado() {
        console.escrever("Combustível esgotado. Missão fracassada.");
    }

    public void anunciarRetornoParaPlataforma() {
        console.escrever("Todos os passageiros a bordo! Retorne à plataforma 'L' em (0,0).");
    }

    public void anunciarVitoria() {
        console.escreverLinhaVazia();
        console.escrever("Pouso confirmado na plataforma (0,0)! Missão cumprida!");
    }

    public void anunciarMissaoAbortada() {
        console.escrever("Missão abortada pelo piloto.");
    }

    public void anunciarEntradaEncerrada() {
        console.escreverLinhaVazia();
        console.escrever("Entrada encerrada. Até a próxima!");
    }

    // ------------------------------------------------------------------
    // Relatórios
    // ------------------------------------------------------------------

    public void exibirEstatisticas(ResultadoMissao resultado, RankingEntry recorde) {
        console.escreverLinhaVazia();
        console.escrever("--- Estatísticas da partida ---");
        console.escrever("Piloto: " + resultado.getPiloto());
        console.escrever("Dificuldade: " + resultado.getDificuldade());
        console.escrever("Pontuação final: " + resultado.getPontuacao());
        console.escrever("Movimentos realizados: " + resultado.getMovimentos());
        console.escrever("Duração: " + resultado.getDuracaoSegundos() + "s");
        console.escrever("Passageiros resgatados: " + resultado.getPassageirosResgatados());
        console.escrever("Desfecho: " + (resultado.venceu() ? "missão cumprida" : "missão não concluída"));

        if (recorde == null) {
            return;
        }
        if (resultado.getPontuacao() > recorde.getPontuacao()) {
            console.escrever("Novo recorde do servidor!");
        } else {
            console.escrever("Recorde atual: " + recorde.getPontuacao() + " pts (" + recorde.getPiloto() + ")");
        }
    }

    public void anunciarRegistroNoRanking() {
        console.escrever("Novo registro salvo no ranking!");
    }

    public void exibirRanking(List<RankingEntry> ranking) {
        console.escreverLinhaVazia();
        console.escrever("--- RANKING TOP 5 ---");
        if (ranking.isEmpty()) {
            console.escrever("Nenhum registro ainda. Seja o primeiro!");
            return;
        }
        int posicao = 1;
        for (RankingEntry entrada : ranking) {
            console.escrever(String.format("%d. %s - %d pts | %s | %d passageiros | %ds | %s",
                    posicao++,
                    entrada.getPiloto(),
                    entrada.getPontuacao(),
                    entrada.getDificuldade(),
                    entrada.getPassageirosResgatados(),
                    entrada.getDuracaoSegundos(),
                    entrada.getDataHora()));
        }
    }

    public boolean confirmarReset() {
        return console.confirmar("Tem certeza que deseja apagar o ranking? (s/n): ");
    }

    public void anunciarRankingResetado() {
        console.escrever("Ranking resetado.");
    }

    public void anunciarOperacaoCancelada() {
        console.escrever("Operação cancelada.");
    }

    public void anunciarOpcaoInvalida() {
        console.escrever("Opção inválida.");
    }

    public void exibirDespedida() {
        console.escreverLinhaVazia();
        console.escrever("Obrigado por jogar Missão Marte Unifor!");
    }

    public void fechar() {
        console.fechar();
    }
}
