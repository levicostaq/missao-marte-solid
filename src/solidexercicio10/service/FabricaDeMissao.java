package solidexercicio10.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Astronauta;
import solidexercicio10.model.CriadorPassageiro;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Engenheiro;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.LimitesMapa;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Professor;

/**
 * Monta uma missão: cria a nave e sorteia posições livres para passageiros,
 * asteroides e inimigos.
 *
 * <p>Na versão original isso eram cinco métodos privados dentro da
 * {@code Main}, misturados com menu e desenho de mapa. Isolar a montagem dá
 * um lugar único para mudar o povoamento do mapa e permite montar uma
 * missão determinística em teste, passando um {@code Random} com semente
 * fixa.</p>
 *
 * <p>A lista {@link #CRIADORES} é o ponto de extensão: um novo tipo de
 * passageiro entra no sorteio com uma linha, sem alterar o laço de
 * criação (OCP).</p>
 */
public class FabricaDeMissao {

    /** Evita laço infinito quando o mapa é pequeno demais para tanta entidade. */
    private static final int MAX_TENTATIVAS = 500;

    private static final List<CriadorPassageiro> CRIADORES = Arrays.asList(
            (indice, x, y) -> new Professor("Dr. Silva " + indice, x, y),
            (indice, x, y) -> new Engenheiro("Eng. Rosa " + indice, x, y),
            (indice, x, y) -> new Astronauta("Ast. Lima " + indice, x, y));

    private final Random random;

    public FabricaDeMissao(Random random) {
        this.random = random;
    }

    public Missao criar(String piloto, Dificuldade dificuldade, LimitesMapa limites) {
        int quantidadePassageiros = dificuldade.getQuantidadePassageiros();
        Nave nave = new Nave("A-1", Math.max(Nave.CAPACIDADE_PADRAO, quantidadePassageiros));
        Missao missao = new Missao(piloto, dificuldade, limites, nave);

        povoar(missao, limites, quantidadePassageiros, (indice, x, y) ->
                missao.adicionarPassageiro(criarPassageiro(indice, x, y)));

        povoar(missao, limites, dificuldade.getQuantidadeAsteroides(), (indice, x, y) ->
                missao.adicionarAsteroide(new Asteroide(x, y)));

        povoar(missao, limites, dificuldade.getQuantidadeInimigos(), (indice, x, y) ->
                missao.adicionarInimigo(new Inimigo(x, y)));

        return missao;
    }

    /**
     * Sorteia casas livres e chama o posicionador. A missão já considera a
     * plataforma de pouso como casa ocupada, então (0,0) nunca é sorteada.
     */
    private void povoar(Missao missao, LimitesMapa limites, int quantidade, Posicionador posicionador) {
        int criados = 0;
        int tentativas = 0;
        while (criados < quantidade && tentativas < MAX_TENTATIVAS) {
            tentativas++;
            int x = sortear(limites.getMinX(), limites.getMaxX());
            int y = sortear(limites.getMinY(), limites.getMaxY());
            if (missao.posicaoOcupada(x, y)) {
                continue;
            }
            posicionador.posicionar(criados, x, y);
            criados++;
        }
    }

    private solidexercicio10.model.Passageiro criarPassageiro(int indice, int x, int y) {
        CriadorPassageiro criador = CRIADORES.get(indice % CRIADORES.size());
        return criador.criar(indice, x, y);
    }

    private int sortear(int minimo, int maximo) {
        return random.nextInt(maximo - minimo + 1) + minimo;
    }

    /** Onde cada entidade recém-sorteada deve ser guardada. */
    @FunctionalInterface
    private interface Posicionador {
        void posicionar(int indice, int x, int y);
    }

    /** Apenas para deixar explícito que a lista de criadores não é vazia. */
    public static List<CriadorPassageiro> tiposDisponiveis() {
        return new ArrayList<>(CRIADORES);
    }
}
