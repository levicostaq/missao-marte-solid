package solidexercicio10.model;

/**
 * Um registro do ranking: piloto, pontuação e metadados da partida.
 *
 * <p>Fica em {@code model} e não em {@code repository} porque é um dado de
 * domínio: o ranking continuaria existindo se as pontuações fossem para um
 * banco, para memória ou para a rede. É o repositório que depende deste
 * tipo, nunca o contrário.</p>
 */
public final class RankingEntry {

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
