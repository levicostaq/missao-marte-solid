package solidexercicio10.model;

/**
 * Desfecho de uma partida, em objeto imutável.
 *
 * <p>Existe para que a tela de estatísticas e o serviço de ranking recebam
 * o resultado sem precisar da {@link Missao} inteira: ninguém consegue
 * alterar o estado do jogo a partir de um relatório.</p>
 */
public final class ResultadoMissao {

    private final String piloto;
    private final int pontuacao;
    private final int movimentos;
    private final int passageirosResgatados;
    private final long duracaoSegundos;
    private final Dificuldade dificuldade;
    private final boolean venceu;

    public ResultadoMissao(String piloto, int pontuacao, int movimentos, int passageirosResgatados,
                           long duracaoSegundos, Dificuldade dificuldade, boolean venceu) {
        this.piloto = piloto;
        this.pontuacao = pontuacao;
        this.movimentos = movimentos;
        this.passageirosResgatados = passageirosResgatados;
        this.duracaoSegundos = duracaoSegundos;
        this.dificuldade = dificuldade;
        this.venceu = venceu;
    }

    public String getPiloto() {
        return piloto;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getMovimentos() {
        return movimentos;
    }

    public int getPassageirosResgatados() {
        return passageirosResgatados;
    }

    public long getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public boolean venceu() {
        return venceu;
    }
}
