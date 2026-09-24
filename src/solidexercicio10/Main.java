package solidexercicio10;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import solidexercicio10.presentation.Console;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.presentation.TelaJogo;
import solidexercicio10.repository.RankingArquivoJson;
import solidexercicio10.repository.RankingRepository;
import solidexercicio10.service.FabricaDeMissao;
import solidexercicio10.service.JogoService;
import solidexercicio10.service.RankingService;

/**
 * Ponto de entrada. Faz uma coisa só: montar as dependências e mandar o
 * jogo rodar.
 *
 * <p>É aqui, e somente aqui, que o programa escolhe implementações
 * concretas. Note a linha do repositório: trocar
 * {@link RankingArquivoJson} por
 * {@code new RankingEmMemoria()} faz o jogo rodar sem gravar nada em disco,
 * e nenhuma outra classe precisa ser recompilada por causa disso. Esse é o
 * ganho prático do DIP nesta refatoração.</p>
 *
 * <p>Comparação: na versão original esta classe tinha 383 linhas e
 * acumulava menu, laço de jogo, sorteio de entidades, desenho do mapa,
 * estatísticas e formatação do ranking.</p>
 */
public class Main {

    private static final Path ARQUIVO_RANKING = Paths.get("ranking.json");

    public static void main(String[] args) {
        Console console = new Console();
        TelaJogo tela = new TelaJogo(console, new MapaRenderer(console));

        RankingRepository repositorio = new RankingArquivoJson(ARQUIVO_RANKING);
        RankingService rankingService = new RankingService(repositorio);

        Random random = new Random();
        FabricaDeMissao fabricaDeMissao = new FabricaDeMissao(random);

        new JogoService(tela, rankingService, fabricaDeMissao, random).executar();
    }
}
