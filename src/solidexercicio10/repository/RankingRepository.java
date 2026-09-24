package solidexercicio10.repository;

import java.util.List;

import solidexercicio10.model.RankingEntry;

/**
 * Contrato de armazenamento do ranking.
 *
 * <p>Três operações, todas usadas pelo único cliente da interface
 * (o serviço de ranking): ler tudo, gravar tudo, apagar tudo (ISP).
 * A regra de Top 5 e a ordenação <b>não</b> estão aqui de propósito: são
 * regra de negócio e mudariam junto com o jogo, não com a forma de
 * guardar os dados.</p>
 *
 * <p>É a abstração do DIP: o serviço depende deste contrato e não sabe se
 * por trás existe um arquivo JSON, um banco ou uma lista em memória.</p>
 */
public interface RankingRepository {

    /** @return as entradas persistidas, ou lista vazia se não houver nenhuma. */
    List<RankingEntry> carregar();

    /** Substitui o conteúdo armazenado pela lista informada. */
    void salvar(List<RankingEntry> ranking);

    /** Remove todo o histórico. */
    void limpar();
}
