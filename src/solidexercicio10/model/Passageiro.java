package solidexercicio10.model;

/**
 * Pessoa a ser resgatada. Classe abstrata porque "passageiro genérico" não
 * existe no jogo: todo passageiro é de um tipo, com pontuação e símbolo
 * próprios.
 *
 * <p>Na versão original esta classe era concreta e devolvia 10 pontos por
 * padrão, o que permitia instanciar um passageiro sem tipo definido e
 * escondia o esquecimento de um {@code @Override}. Tornando-a abstrata, o
 * compilador passa a cobrar o contrato de cada subclasse (LSP).</p>
 */
public abstract class Passageiro extends EntidadeMapa {

    private final String nome;

    protected Passageiro(String nome, int x, int y) {
        super(x, y);
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    /** Pontos concedidos ao embarcar. Positivo em qualquer subclasse. */
    public abstract int getPontuacao();

    /** Tipo exibido nos relatórios da partida. */
    public abstract String getTipo();

    @Override
    public String getDescricao() {
        return getTipo();
    }

    @Override
    public String toString() {
        return getTipo() + " " + nome;
    }
}
