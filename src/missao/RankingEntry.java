package missao;

/**
 * Um registro do ranking: piloto, pontuação e metadados da partida (Exercício 9).
 */
public class RankingEntry {

    private final String piloto;
    private final int pontuacao;
    private final Dificuldade dificuldade;
    private final int passageirosResgatados;
    private final String dataHora;
    private final long duracaoSegundos;

    public RankingEntry(String piloto, int pontuacao, Dificuldade dificuldade,
                         int passageirosResgatados, String dataHora, long duracaoSegundos) {
        this.piloto = piloto;
        this.pontuacao = pontuacao;
        this.dificuldade = dificuldade;
        this.passageirosResgatados = passageirosResgatados;
        this.dataHora = dataHora;
        this.duracaoSegundos = duracaoSegundos;
    }

    public String getPiloto() {
        return piloto;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public int getPassageirosResgatados() {
        return passageirosResgatados;
    }

    public String getDataHora() {
        return dataHora;
    }

    public long getDuracaoSegundos() {
        return duracaoSegundos;
    }
}
