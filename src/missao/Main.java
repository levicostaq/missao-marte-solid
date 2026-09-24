package missao;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static final Path CAMINHO_RANKING = Paths.get("ranking.json");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Scanner scanner = new Scanner(System.in);
    private final Random random = new Random();
    private final RankingRepositorio rankingRepositorio = new RankingRepositorio(CAMINHO_RANKING);

    public static void main(String[] args) {
        new Main().executar();
    }

    private void executar() {
        exibirBoasVindas();
        List<RankingEntry> ranking = rankingRepositorio.carregar();

        boolean rodando = true;
        while (rodando) {
            exibirMenuPrincipal();
            if (!scanner.hasNextLine()) {
                System.out.println("\nEntrada encerrada. Até a próxima!");
                break;
            }
            String opcao = lerLinha("Escolha uma opção: ");

            switch (opcao) {
                case "1":
                    jogarPartida(ranking);
                    ranking = rankingRepositorio.carregar();
                    break;
                case "2":
                    exibirRanking(ranking);
                    break;
                case "3":
                    ranking = confirmarResetRanking();
                    break;
                case "4":
                    rodando = false;
                    System.out.println("\nObrigado por jogar Missão Marte Unifor!");
                    break;
                default:
                    System.out.println("Opção inválida.\n");
            }
        }
        scanner.close();
    }

    private void exibirBoasVindas() {
        System.out.println("========================================");
        System.out.println("        MISSAO MARTE UNIFOR");
        System.out.println("========================================");
    }

    private void exibirMenuPrincipal() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Iniciar Nova Missão");
        System.out.println("2. Visualizar Ranking Top 5");
        System.out.println("3. Resetar Histórico de Ranking");
        System.out.println("4. Sair do Jogo");
    }

    // ----------------------------------------------------------------
    // Fluxo de uma partida
    // ----------------------------------------------------------------

    private void jogarPartida(List<RankingEntry> ranking) {
        String piloto = lerLinha("\nNome do piloto: ");
        if (piloto.isEmpty()) {
            piloto = "Piloto Anônimo";
        }

        Dificuldade dificuldade = escolherDificuldade();
        int tamanho = escolherTamanhoDoMapa();
        int minX = -tamanho, maxX = tamanho, minY = -tamanho, maxY = tamanho;

        Missao missao = criarMissao(dificuldade, minX, maxX, minY, maxY);
        Nave nave = missao.getNave();
        int pontuacao = dificuldade.getPontuacaoInicial();
        int movimentos = 0;
        long inicio = System.currentTimeMillis();

        boolean emAndamento = true;
        boolean venceu = false;

        while (emAndamento) {
            desenharMapa(missao, minX, maxX, minY, maxY, pontuacao, piloto);
            System.out.println("Comandos: w/a/s/d mover | c embarcar | q abortar");
            if (!scanner.hasNextLine()) {
                System.out.println("Entrada encerrada. Abortando missão.");
                break;
            }
            String entrada = lerLinha("Comando: ").toLowerCase();
            if (entrada.isEmpty()) {
                continue;
            }
            char comando = entrada.charAt(0);

            if (comando == 'q') {
                System.out.println("Missão abortada pelo piloto.");
                break;
            }

            if (comando == 'c') {
                Passageiro embarcado = missao.embarcarPassageiroNaPosicaoAtual();
                if (embarcado != null) {
                    pontuacao += embarcado.getPontuacao();
                    System.out.println(embarcado.getNome() + " embarcado! +" + embarcado.getPontuacao() + " pontos.");
                } else {
                    System.out.println("Nada para embarcar aqui (ou nave cheia).");
                }
            } else if (comando == 'w' || comando == 'a' || comando == 's' || comando == 'd') {
                boolean moveu = nave.moverComLimites(comando, minX, maxX, minY, maxY);
                if (moveu) {
                    pontuacao--;
                    movimentos++;
                } else {
                    System.out.println("Limite do mapa alcançado.");
                }
            } else {
                System.out.println("Comando desconhecido.");
                continue;
            }

            missao.moverInimigos(random, minX, maxX, minY, maxY);

            if (missao.verificaColisaoAsteroide() || missao.verificaColisaoInimigo()) {
                nave.perderVida();
                if (nave.getVidas() <= 0) {
                    System.out.println("GAME OVER: a nave foi destruída.");
                    emAndamento = false;
                    continue;
                }
                System.out.println("Colisão! Vidas restantes: " + nave.getVidas());
            }

            if (pontuacao <= 0) {
                System.out.println("Combustível esgotado. Missão fracassada.");
                emAndamento = false;
                continue;
            }

            if (missao.todosEmbarcados()) {
                if (nave.getX() == 0 && nave.getY() == 0) {
                    System.out.println("\nPouso confirmado na plataforma (0,0)! Missão cumprida!");
                    venceu = true;
                    emAndamento = false;
                } else {
                    System.out.println("Todos os passageiros a bordo! Retorne à plataforma 'L' em (0,0).");
                }
            }
        }

        long duracaoSegundos = (System.currentTimeMillis() - inicio) / 1000;
        exibirEstatisticas(pontuacao, movimentos, duracaoSegundos, nave.getPassageiros().size(), ranking);

        if (venceu && pontuacao > 0 && rankingRepositorio.isTopScore(ranking, pontuacao)) {
            RankingEntry novaEntrada = new RankingEntry(
                    piloto,
                    pontuacao,
                    dificuldade,
                    nave.getPassageiros().size(),
                    LocalDateTime.now().format(FORMATO_DATA),
                    duracaoSegundos
            );
            rankingRepositorio.registrarResultado(ranking, novaEntrada);
            System.out.println("Novo registro salvo no ranking!");
        }
    }

    private Dificuldade escolherDificuldade() {
        System.out.println("Dificuldade: [1] Fácil  [2] Médio  [3] Difícil");
        String escolha = lerLinha("Escolha (1-3, padrão Médio): ");
        return Dificuldade.fromTexto(escolha);
    }

    private int escolherTamanhoDoMapa() {
        String entrada = lerLinha("Tamanho do mapa, de -N a +N (padrão 5): ");
        if (entrada.isEmpty()) {
            return 5;
        }
        try {
            int tamanho = Integer.parseInt(entrada.trim());
            return tamanho > 0 ? tamanho : 5;
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido, usando 5.");
            return 5;
        }
    }

    private Missao criarMissao(Dificuldade dificuldade, int minX, int maxX, int minY, int maxY) {
        Nave nave = new Nave("A-1", 5);
        Missao missao = new Missao(nave);

        int qtdPassageiros = dificuldade.getQuantidadePassageiros();
        int qtdAsteroides = dificuldade.getQuantidadeAsteroides();
        int qtdInimigos = dificuldade.getQuantidadeInimigos();

        int criados = 0;
        while (criados < qtdPassageiros) {
            int x = posicaoAleatoria(minX, maxX);
            int y = posicaoAleatoria(minY, maxY);
            if (posicaoOcupada(missao, x, y) || (x == 0 && y == 0)) {
                continue;
            }
            missao.addPassageiro(criarPassageiro(criados, x, y));
            criados++;
        }

        int asteroidesCriados = 0;
        while (asteroidesCriados < qtdAsteroides) {
            int x = posicaoAleatoria(minX, maxX);
            int y = posicaoAleatoria(minY, maxY);
            if (posicaoOcupada(missao, x, y) || (x == 0 && y == 0)) {
                continue;
            }
            missao.addAsteroide(new Asteroide(x, y));
            asteroidesCriados++;
        }

        int inimigosCriados = 0;
        while (inimigosCriados < qtdInimigos) {
            int x = posicaoAleatoria(minX, maxX);
            int y = posicaoAleatoria(minY, maxY);
            if (posicaoOcupada(missao, x, y) || (x == 0 && y == 0)) {
                continue;
            }
            missao.addInimigo(new Inimigo(x, y));
            inimigosCriados++;
        }

        return missao;
    }

    private Passageiro criarPassageiro(int indice, int x, int y) {
        int tipo = indice % 3;
        if (tipo == 0) {
            return new Professor("Dr. Silva " + indice, x, y);
        }
        if (tipo == 1) {
            return new Engenheiro("Eng. Rosa " + indice, x, y);
        }
        return new Astronauta("Ast. Lima " + indice, x, y);
    }

    private int posicaoAleatoria(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    private boolean posicaoOcupada(Missao missao, int x, int y) {
        if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
            return true;
        }
        for (Passageiro p : missao.getPassageiros()) {
            if (p.getX() == x && p.getY() == y) {
                return true;
            }
        }
        for (Asteroide a : missao.getAsteroides()) {
            if (a.getX() == x && a.getY() == y) {
                return true;
            }
        }
        for (Inimigo i : missao.getInimigos()) {
            if (i.getX() == x && i.getY() == y) {
                return true;
            }
        }
        return false;
    }

    // ----------------------------------------------------------------
    // Interface: mapa, ranking, estatísticas
    // ----------------------------------------------------------------

    private void desenharMapa(Missao missao, int minX, int maxX, int minY, int maxY, int pontuacao, String piloto) {
        System.out.println();
        System.out.printf("Pontos: %d | Vidas: %d | Piloto: %s%n",
                pontuacao, missao.getNave().getVidas(), piloto);

        for (int y = minY; y <= maxY; y++) {
            StringBuilder linha = new StringBuilder();
            for (int x = minX; x <= maxX; x++) {
                linha.append(' ').append(simboloNaPosicao(missao, x, y));
            }
            System.out.println(linha);
        }
        System.out.println("Legenda: @ nave | P professor | E engenheiro | T astronauta | # asteroide | X inimigo | L plataforma de pouso");
    }

    private char simboloNaPosicao(Missao missao, int x, int y) {
        Nave nave = missao.getNave();
        if (nave.getX() == x && nave.getY() == y) {
            return '@';
        }
        for (Passageiro p : missao.getPassageiros()) {
            if (p.getX() == x && p.getY() == y) {
                if (p instanceof Engenheiro) {
                    return 'E';
                }
                if (p instanceof Astronauta) {
                    return 'T';
                }
                return 'P';
            }
        }
        for (Asteroide a : missao.getAsteroides()) {
            if (a.getX() == x && a.getY() == y) {
                return '#';
            }
        }
        for (Inimigo i : missao.getInimigos()) {
            if (i.getX() == x && i.getY() == y) {
                return 'X';
            }
        }
        if (x == 0 && y == 0) {
            return 'L';
        }
        return '.';
    }

    private void exibirEstatisticas(int pontuacao, int movimentos, long duracaoSegundos,
                                     int passageirosResgatados, List<RankingEntry> ranking) {
        System.out.println("\n--- Estatísticas da partida ---");
        System.out.println("Pontuação final: " + pontuacao);
        System.out.println("Movimentos realizados: " + movimentos);
        System.out.println("Duração: " + duracaoSegundos + "s");
        System.out.println("Passageiros resgatados: " + passageirosResgatados);

        if (!ranking.isEmpty()) {
            RankingEntry recorde = ranking.get(0);
            if (pontuacao > recorde.getPontuacao()) {
                System.out.println("Novo recorde do servidor!");
            } else {
                System.out.println("Recorde atual: " + recorde.getPontuacao() + " pts (" + recorde.getPiloto() + ")");
            }
        }
    }

    private void exibirRanking(List<RankingEntry> ranking) {
        System.out.println("\n--- RANKING TOP 5 ---");
        if (ranking.isEmpty()) {
            System.out.println("Nenhum registro ainda. Seja o primeiro!");
            return;
        }
        int posicao = 1;
        for (RankingEntry entrada : ranking) {
            System.out.printf("%d. %s - %d pts | %s | %d passageiros | %ds | %s%n",
                    posicao++, entrada.getPiloto(), entrada.getPontuacao(), entrada.getDificuldade(),
                    entrada.getPassageirosResgatados(), entrada.getDuracaoSegundos(), entrada.getDataHora());
        }
    }

    private List<RankingEntry> confirmarResetRanking() {
        String resposta = lerLinha("Tem certeza que deseja apagar o ranking? (s/n): ").toLowerCase();
        if (resposta.equals("s") || resposta.equals("sim")) {
            rankingRepositorio.resetar();
            System.out.println("Ranking resetado.");
            return rankingRepositorio.carregar();
        }
        System.out.println("Operação cancelada.");
        return rankingRepositorio.carregar();
    }

    private String lerLinha(String prompt) {
        System.out.print(prompt);
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "";
    }
}
