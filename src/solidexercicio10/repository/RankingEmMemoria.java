package solidexercicio10.repository;

import java.util.ArrayList;
import java.util.List;

import solidexercicio10.model.RankingEntry;

/**
 * Implementação volátil do ranking, sem tocar em disco.
 *
 * <p>Serve a dois propósitos concretos: provar na prática que trocar a
 * persistência não exige mudança no serviço nem no fluxo do jogo (DIP/OCP),
 * e permitir testar a regra de Top 5 sem criar arquivos. Basta uma linha
 * diferente na {@code Main} para o jogo rodar com ela.</p>
 */
public class RankingEmMemoria implements RankingRepository {

    private final List<RankingEntry> registros = new ArrayList<>();

    @Override
    public List<RankingEntry> carregar() {
        return new ArrayList<>(registros);
    }

    @Override
    public void salvar(List<RankingEntry> ranking) {
        registros.clear();
        if (ranking != null) {
            registros.addAll(ranking);
        }
    }

    @Override
    public void limpar() {
        registros.clear();
    }
}
