package solidexercicio10.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import solidexercicio10.model.RankingEntry;
import solidexercicio10.model.ResultadoMissao;
import solidexercicio10.repository.RankingRepository;

/**
 * Regras do ranking: quem entra, em que ordem e quantas posições existem.
 *
 * <p>Depende de {@link RankingRepository}, não do arquivo JSON (DIP).
 * Trocar a persistência por banco ou memória não muda nada aqui.</p>
 *
 * <p>Divergência consciente do tutorial: nele a classe {@code RankingService}
 * era a própria implementação que lia o arquivo. Ordenar, limitar a cinco
 * posições e decidir se uma partida classifica são regras de negócio, que
 * mudam por motivos diferentes do formato do arquivo; por isso ficaram
 * neste serviço e a leitura/escrita foi para o pacote {@code repository}.</p>
 */
public class RankingService {

    public static final int TAMANHO_MAXIMO = 5;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RankingRepository repositorio;

    public RankingService(RankingRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Top 5 em ordem decrescente de pontuação. */
    public List<RankingEntry> listar() {
        List<RankingEntry> ranking = new ArrayList<>(repositorio.carregar());
        ranking.sort(Comparator.comparingInt(RankingEntry::getPontuacao).reversed());
        if (ranking.size() > TAMANHO_MAXIMO) {
            return new ArrayList<>(ranking.subList(0, TAMANHO_MAXIMO));
        }
        return ranking;
    }

    /** @return a melhor pontuação já registrada, ou null se o ranking está vazio. */
    public RankingEntry recorde() {
        List<RankingEntry> ranking = listar();
        return ranking.isEmpty() ? null : ranking.get(0);
    }

    public boolean classificaNoTop(int pontuacao) {
        List<RankingEntry> ranking = listar();
        if (ranking.size() < TAMANHO_MAXIMO) {
            return true;
        }
        return pontuacao > ranking.get(ranking.size() - 1).getPontuacao();
    }

    /**
     * Registra o resultado se ele merecer entrar no Top 5.
     *
     * @return true se o ranking foi alterado.
     */
    public boolean registrar(ResultadoMissao resultado) {
        if (!resultado.venceu() || resultado.getPontuacao() <= 0) {
            return false;
        }
        if (!classificaNoTop(resultado.getPontuacao())) {
            return false;
        }

        List<RankingEntry> ranking = listar();
        ranking.add(paraEntrada(resultado));
        ranking.sort(Comparator.comparingInt(RankingEntry::getPontuacao).reversed());
        if (ranking.size() > TAMANHO_MAXIMO) {
            ranking = new ArrayList<>(ranking.subList(0, TAMANHO_MAXIMO));
        }
        repositorio.salvar(ranking);
        return true;
    }

    public void resetar() {
        repositorio.limpar();
    }

    private RankingEntry paraEntrada(ResultadoMissao resultado) {
        return new RankingEntry(
                resultado.getPiloto(),
                resultado.getPontuacao(),
                resultado.getDificuldade(),
                resultado.getPassageirosResgatados(),
                LocalDateTime.now().format(FORMATO_DATA),
                resultado.getDuracaoSegundos());
    }
}
