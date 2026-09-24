package missao;

/**
 * Representa um passageiro a ser resgatado durante a missão.
 * Serve de classe-base para os tipos especializados (Professor, Engenheiro, Astronauta).
 */
public class Passageiro {

    private final String nome;
    private final String tipo;
    private int x;
    private int y;

    public Passageiro(String nome, String tipo, int x, int y) {
        this.nome = nome;
        this.tipo = tipo;
        this.x = x;
        this.y = y;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Pontuação concedida ao embarcar este passageiro.
     * Cada subclasse sobrescreve com seu próprio valor (Exercício 4).
     */
    public int getPontuacao() {
        return 10;
    }
}
