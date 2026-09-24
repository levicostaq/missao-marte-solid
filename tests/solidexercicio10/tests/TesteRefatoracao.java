package solidexercicio10.tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Astronauta;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Direcao;
import solidexercicio10.model.Engenheiro;
import solidexercicio10.model.EntidadeMapa;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.LimitesMapa;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Passageiro;
import solidexercicio10.model.Professor;
import solidexercicio10.model.RankingEntry;
import solidexercicio10.model.ResultadoMissao;
import solidexercicio10.repository.RankingArquivoJson;
import solidexercicio10.repository.RankingEmMemoria;
import solidexercicio10.repository.RankingRepository;
import solidexercicio10.service.FabricaDeMissao;
import solidexercicio10.service.RankingService;

/**
 * Testes das regras da versão refatorada, sem dependência de biblioteca
 * externa e sem entrada do teclado.
 *
 * <p>Estes testes só são possíveis por causa da refatoração: na versão
 * original a pontuação, a colisão e o ranking estavam presos ao laço de
 * console da {@code Main}, e a única forma de verificar qualquer regra era
 * jogar à mão. Aqui as regras estão em {@code Missao} e nos serviços, e o
 * ranking usa {@code RankingEmMemoria} em vez de mexer em arquivo.</p>
 *
 * <p>Compilar e rodar (a partir da raiz do repositório):</p>
 * <pre>
 * javac -encoding UTF-8 -d out $(find src/solidexercicio10 tests -name "*.java")
 * java -cp out solidexercicio10.tests.TesteRefatoracao
 * </pre>
 */
public class TesteRefatoracao {

    private static int executados = 0;
    private static int falhas = 0;

    public static void main(String[] args) throws Exception {
        vitoriaExigePousoNaPlataforma();
        pontuacaoUsaPolimorfismoDoPassageiro();
        movimentoCobraCombustivel();
        movimentoForaDoMapaNaoAlteraEstado();
        colisaoConsomeVidasAteDestruir();
        capacidadeDaNaveEhRespeitada();
        listaDePassageirosEhSomenteLeitura();
        fabricaPovoaSemSobreposicaoENaoOcupaPlataforma();
        inimigoNuncaSaiDosLimites();
        renderizacaoNaoDependeDeTipoConcreto();
        rankingMantemTop5Ordenado();
        rankingIgnoraPartidaPerdida();
        rankingResetLimpaHistorico();
        repositorioJsonGravaELeDeVolta();
        conversaoDeTextoEmDificuldade();
        conversaoDeTeclaEmDirecao();

        System.out.println();
        System.out.println("-------------------------------------------");
        System.out.printf("%d verificações, %d falha(s).%n", executados, falhas);
        if (falhas > 0) {
            System.exit(1);
        }
        System.out.println("TODOS OS TESTES PASSARAM.");
    }

    // ------------------------------------------------------------------
    // Regras da missão
    // ------------------------------------------------------------------

    private static void vitoriaExigePousoNaPlataforma() {
        Missao missao = missaoDeTeste(new Professor("Dr. Teste", 1, 0));

        checar("nave começa na plataforma", missao.naveNaPlataforma());
        checar("missão não está cumprida no início", !missao.missaoCumprida());

        missao.moverNave(Direcao.DIREITA);
        Passageiro embarcado = missao.embarcarNaPosicaoAtual();

        checar("passageiro embarcado na casa correta", embarcado != null);
        checar("todos embarcados após o resgate", missao.todosEmbarcados());
        checar("resgatar todos não basta para vencer", !missao.missaoCumprida());

        missao.moverNave(Direcao.ESQUERDA);
        checar("vitória após pousar em (0,0)", missao.missaoCumprida());
    }

    private static void pontuacaoUsaPolimorfismoDoPassageiro() {
        verificarPontuacaoDe(new Professor("P", 1, 0), 10);
        verificarPontuacaoDe(new Engenheiro("E", 1, 0), 15);
        verificarPontuacaoDe(new Astronauta("A", 1, 0), 20);
    }

    private static void verificarPontuacaoDe(Passageiro passageiro, int pontosEsperados) {
        Missao missao = missaoDeTeste(passageiro);
        int inicial = missao.getPontuacao();

        missao.moverNave(Direcao.DIREITA);
        missao.embarcarNaPosicaoAtual();

        int esperado = inicial - Missao.CUSTO_POR_MOVIMENTO + pontosEsperados;
        checar(passageiro.getTipo() + " concede " + pontosEsperados + " pontos",
                missao.getPontuacao() == esperado);
    }

    private static void movimentoCobraCombustivel() {
        Missao missao = missaoDeTeste();
        int inicial = missao.getPontuacao();

        missao.moverNave(Direcao.DIREITA);
        missao.moverNave(Direcao.BAIXO);

        checar("dois movimentos custam dois pontos",
                missao.getPontuacao() == inicial - 2);
        checar("contador de movimentos atualizado", missao.getMovimentos() == 2);
    }

    private static void movimentoForaDoMapaNaoAlteraEstado() {
        Nave nave = new Nave("T-1", 5);
        Missao missao = new Missao("Piloto", Dificuldade.MEDIO, new LimitesMapa(1), nave);

        checar("primeiro movimento dentro do mapa", missao.moverNave(Direcao.DIREITA));
        int pontuacao = missao.getPontuacao();
        int movimentos = missao.getMovimentos();

        checar("movimento além da borda é recusado", !missao.moverNave(Direcao.DIREITA));
        checar("nave permanece na borda", nave.getX() == 1);
        checar("movimento recusado não cobra combustível", missao.getPontuacao() == pontuacao);
        checar("movimento recusado não conta como movimento", missao.getMovimentos() == movimentos);
    }

    private static void colisaoConsomeVidasAteDestruir() {
        Nave nave = new Nave("T-1", 5);
        Missao missao = new Missao("Piloto", Dificuldade.MEDIO, new LimitesMapa(5), nave);
        missao.adicionarAsteroide(new Asteroide(1, 0));

        checar("sem colisão antes de entrar na casa do asteroide", !missao.houveColisao());
        missao.moverNave(Direcao.DIREITA);
        checar("colisão detectada na casa do asteroide", missao.houveColisao());

        int vidas = nave.getVidas();
        missao.registrarColisao();
        checar("colisão remove uma vida", nave.getVidas() == vidas - 1);

        while (!nave.estaDestruida()) {
            missao.registrarColisao();
        }
        checar("nave destruída quando as vidas acabam", missao.naveDestruida());
        checar("vidas não ficam negativas", nave.getVidas() == 0);
    }

    private static void capacidadeDaNaveEhRespeitada() {
        Nave nave = new Nave("T-1", 1);
        Missao missao = new Missao("Piloto", Dificuldade.MEDIO, new LimitesMapa(5), nave);
        missao.adicionarPassageiro(new Professor("Primeiro", 1, 0));
        missao.adicionarPassageiro(new Engenheiro("Segundo", 1, 0));

        missao.moverNave(Direcao.DIREITA);
        checar("primeiro passageiro embarca", missao.embarcarNaPosicaoAtual() != null);
        checar("nave cheia recusa o segundo", missao.embarcarNaPosicaoAtual() == null);
        checar("o segundo continua no mapa", missao.getPassageirosRestantes() == 1);
    }

    private static void listaDePassageirosEhSomenteLeitura() {
        Nave nave = new Nave("T-1", 2);
        try {
            nave.getPassageiros().add(new Professor("Clandestino", 0, 0));
            checar("lista de passageiros da nave é imutável", false);
        } catch (UnsupportedOperationException esperado) {
            checar("lista de passageiros da nave é imutável", true);
        }
    }

    // ------------------------------------------------------------------
    // Fábrica e entidades
    // ------------------------------------------------------------------

    private static void fabricaPovoaSemSobreposicaoENaoOcupaPlataforma() {
        FabricaDeMissao fabrica = new FabricaDeMissao(new Random(42));
        Missao missao = fabrica.criar("Piloto", Dificuldade.DIFICIL, new LimitesMapa(5));

        checar("quantidade de passageiros vem da dificuldade",
                missao.getPassageirosRestantes() == Dificuldade.DIFICIL.getQuantidadePassageiros());
        checar("quantidade de asteroides vem da dificuldade",
                missao.getAsteroides().size() == Dificuldade.DIFICIL.getQuantidadeAsteroides());
        checar("quantidade de inimigos vem da dificuldade",
                missao.getInimigos().size() == Dificuldade.DIFICIL.getQuantidadeInimigos());
        checar("capacidade da nave comporta todos os passageiros",
                missao.getNave().getCapacidade() >= missao.getPassageirosRestantes());

        Set<String> casas = new HashSet<>();
        boolean sobreposicao = false;
        for (EntidadeMapa entidade : missao.getEntidades()) {
            if (entidade == missao.getNave() || entidade == missao.getPlataforma()) {
                continue;
            }
            if (!casas.add(entidade.getX() + ":" + entidade.getY())) {
                sobreposicao = true;
            }
            if (entidade.getX() == 0 && entidade.getY() == 0) {
                sobreposicao = true;
            }
        }
        checar("nenhuma entidade sorteada se sobrepõe ou ocupa (0,0)", !sobreposicao);

        List<String> tipos = new ArrayList<>();
        for (Passageiro passageiro : missao.getPassageirosNoMapa()) {
            if (!tipos.contains(passageiro.getTipo())) {
                tipos.add(passageiro.getTipo());
            }
        }
        checar("sorteio alterna os tipos de passageiro", tipos.size() >= 2);
    }

    private static void inimigoNuncaSaiDosLimites() {
        LimitesMapa limites = new LimitesMapa(3);
        Inimigo inimigo = new Inimigo(1, 1);
        Random random = new Random(7);

        boolean dentro = true;
        for (int turno = 0; turno < 500; turno++) {
            inimigo.moverAleatoriamente(random, limites);
            if (!limites.contem(inimigo.getX(), inimigo.getY())) {
                dentro = false;
                break;
            }
        }
        checar("inimigo respeita os limites em 500 turnos", dentro);
    }

    private static void renderizacaoNaoDependeDeTipoConcreto() {
        Missao missao = missaoDeTeste(new Astronauta("Ast", 2, 2));
        missao.adicionarAsteroide(new Asteroide(1, 1));
        missao.adicionarInimigo(new Inimigo(2, 1));

        Set<Character> simbolos = new HashSet<>();
        for (EntidadeMapa entidade : missao.getEntidades()) {
            simbolos.add(entidade.getSimbolo());
        }

        checar("cada entidade fornece o próprio símbolo",
                simbolos.contains('@') && simbolos.contains('T')
                        && simbolos.contains('#') && simbolos.contains('X')
                        && simbolos.contains('L'));
        checar("nave aparece antes das outras entidades na ordem de desenho",
                missao.getEntidades().get(0) == missao.getNave());
    }

    // ------------------------------------------------------------------
    // Ranking
    // ------------------------------------------------------------------

    private static void rankingMantemTop5Ordenado() {
        RankingService servico = new RankingService(new RankingEmMemoria());

        int[] pontuacoes = {12, 40, 25, 8, 33, 19};
        for (int pontuacao : pontuacoes) {
            servico.registrar(vitoriaCom(pontuacao));
        }

        List<RankingEntry> ranking = servico.listar();
        checar("ranking guarda no máximo 5 posições",
                ranking.size() == RankingService.TAMANHO_MAXIMO);
        checar("melhor pontuação em primeiro", ranking.get(0).getPontuacao() == 40);
        checar("pior pontuação foi descartada", ranking.get(4).getPontuacao() == 12);
        checar("recorde é a primeira posição", servico.recorde().getPontuacao() == 40);

        boolean ordenado = true;
        for (int i = 1; i < ranking.size(); i++) {
            if (ranking.get(i - 1).getPontuacao() < ranking.get(i).getPontuacao()) {
                ordenado = false;
            }
        }
        checar("ranking está em ordem decrescente", ordenado);
        checar("pontuação menor que a última não classifica", !servico.classificaNoTop(5));
        checar("pontuação maior que a última classifica", servico.classificaNoTop(30));
    }

    private static void rankingIgnoraPartidaPerdida() {
        RankingService servico = new RankingService(new RankingEmMemoria());

        ResultadoMissao derrota = new ResultadoMissao("Piloto", 50, 10, 2, 30,
                Dificuldade.MEDIO, false);
        checar("derrota não entra no ranking", !servico.registrar(derrota));
        checar("ranking segue vazio após derrota", servico.listar().isEmpty());
        checar("ranking vazio não tem recorde", servico.recorde() == null);

        ResultadoMissao semPontos = new ResultadoMissao("Piloto", 0, 10, 2, 30,
                Dificuldade.MEDIO, true);
        checar("vitória com zero ponto não entra no ranking", !servico.registrar(semPontos));
    }

    private static void rankingResetLimpaHistorico() {
        RankingRepository repositorio = new RankingEmMemoria();
        RankingService servico = new RankingService(repositorio);

        servico.registrar(vitoriaCom(30));
        checar("registro salvo antes do reset", servico.listar().size() == 1);

        servico.resetar();
        checar("reset limpa o ranking", servico.listar().isEmpty());
        checar("reset atinge o repositório", repositorio.carregar().isEmpty());
    }

    private static void repositorioJsonGravaELeDeVolta() throws Exception {
        Path arquivo = Files.createTempFile("ranking-teste", ".json");
        Files.deleteIfExists(arquivo);

        RankingRepository repositorio = new RankingArquivoJson(arquivo);
        checar("arquivo inexistente devolve lista vazia", repositorio.carregar().isEmpty());

        RankingService servico = new RankingService(repositorio);
        servico.registrar(new ResultadoMissao("Levi \"Ace\"", 42, 11, 3, 77,
                Dificuldade.DIFICIL, true));

        checar("arquivo foi criado", Files.exists(arquivo));

        List<RankingEntry> lido = new RankingService(new RankingArquivoJson(arquivo)).listar();
        checar("uma entrada foi persistida", lido.size() == 1);

        RankingEntry entrada = lido.get(0);
        checar("piloto com aspas sobrevive ao JSON", "Levi \"Ace\"".equals(entrada.getPiloto()));
        checar("pontuação persistida", entrada.getPontuacao() == 42);
        checar("dificuldade persistida", entrada.getDificuldade() == Dificuldade.DIFICIL);
        checar("passageiros resgatados persistidos", entrada.getPassageirosResgatados() == 3);
        checar("duração persistida", entrada.getDuracaoSegundos() == 77);
        checar("data e hora persistidas", !entrada.getDataHora().isEmpty());

        repositorio.limpar();
        checar("limpar remove o arquivo", !Files.exists(arquivo));
    }

    // ------------------------------------------------------------------
    // Conversões de entrada
    // ------------------------------------------------------------------

    private static void conversaoDeTextoEmDificuldade() {
        checar("'1' vira FACIL", Dificuldade.deTexto("1") == Dificuldade.FACIL);
        checar("'facil' vira FACIL", Dificuldade.deTexto("facil") == Dificuldade.FACIL);
        checar("'difícil' com acento vira DIFICIL", Dificuldade.deTexto("difícil") == Dificuldade.DIFICIL);
        checar("texto vazio cai no padrão MEDIO", Dificuldade.deTexto("") == Dificuldade.MEDIO);
        checar("nulo cai no padrão MEDIO", Dificuldade.deTexto(null) == Dificuldade.MEDIO);
        checar("lixo cai no padrão MEDIO", Dificuldade.deTexto("xyz") == Dificuldade.MEDIO);
        checar("FACIL dá mais combustível que DIFICIL",
                Dificuldade.FACIL.getPontuacaoInicial() > Dificuldade.DIFICIL.getPontuacaoInicial());
        checar("DIFICIL tem mais obstáculos que FACIL",
                Dificuldade.DIFICIL.getQuantidadeAsteroides() > Dificuldade.FACIL.getQuantidadeAsteroides());
    }

    private static void conversaoDeTeclaEmDirecao() {
        checar("w sobe", Direcao.deTecla('w') == Direcao.CIMA);
        checar("s desce", Direcao.deTecla('s') == Direcao.BAIXO);
        checar("a vai para a esquerda", Direcao.deTecla('a') == Direcao.ESQUERDA);
        checar("d vai para a direita", Direcao.deTecla('d') == Direcao.DIREITA);
        checar("tecla de ação não é direção", Direcao.deTecla('c') == null);
        checar("tecla desconhecida não é direção", Direcao.deTecla('z') == null);
    }

    // ------------------------------------------------------------------
    // Apoio
    // ------------------------------------------------------------------

    private static Missao missaoDeTeste(Passageiro... passageiros) {
        Missao missao = new Missao("Piloto de Teste", Dificuldade.MEDIO,
                new LimitesMapa(5), new Nave("T-1", 5));
        for (Passageiro passageiro : passageiros) {
            missao.adicionarPassageiro(passageiro);
        }
        return missao;
    }

    private static ResultadoMissao vitoriaCom(int pontuacao) {
        return new ResultadoMissao("Piloto " + pontuacao, pontuacao, 10, 3, 60,
                Dificuldade.MEDIO, true);
    }

    private static void checar(String descricao, boolean condicao) {
        executados++;
        if (condicao) {
            System.out.println("  ok   " + descricao);
        } else {
            falhas++;
            System.out.println("  FALHA " + descricao);
        }
    }
}
