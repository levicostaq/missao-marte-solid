package solidexercicio10.model;

/**
 * As quatro direções de movimento e a tecla que dispara cada uma.
 *
 * <p>Na versão original o {@code switch (comando)} com os deslocamentos
 * vivia dentro da Nave. Aqui a tradução tecla -> deslocamento é um conceito
 * próprio, o que deixa a Nave sem conhecer o teclado.</p>
 */
public enum Direcao {

    CIMA('w', 0, -1),
    BAIXO('s', 0, 1),
    ESQUERDA('a', -1, 0),
    DIREITA('d', 1, 0);

    private final char tecla;
    private final int deslocamentoX;
    private final int deslocamentoY;

    Direcao(char tecla, int deslocamentoX, int deslocamentoY) {
        this.tecla = tecla;
        this.deslocamentoX = deslocamentoX;
        this.deslocamentoY = deslocamentoY;
    }

    public char getTecla() {
        return tecla;
    }

    public int getDeslocamentoX() {
        return deslocamentoX;
    }

    public int getDeslocamentoY() {
        return deslocamentoY;
    }

    /** @return a direção correspondente à tecla, ou null se a tecla não for de movimento. */
    public static Direcao deTecla(char tecla) {
        for (Direcao direcao : values()) {
            if (direcao.tecla == tecla) {
                return direcao;
            }
        }
        return null;
    }
}
