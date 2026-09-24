package solidexercicio10.model;

/**
 * Níveis de dificuldade e os recursos que cada um concede.
 *
 * <p>Diferença em relação à versão original: os valores viraram campos do
 * enum em vez de quatro {@code switch (this)} separados. Ajustar o
 * balanceamento agora é editar uma linha, e criar um novo nível não exige
 * tocar em nenhum método.</p>
 */
public enum Dificuldade {

    FACIL("Fácil", 30, 4, 1, 1),
    MEDIO("Médio", 20, 3, 2, 2),
    DIFICIL("Difícil", 15, 5, 3, 3);

    private final String rotulo;
    private final int pontuacaoInicial;
    private final int quantidadePassageiros;
    private final int quantidadeAsteroides;
    private final int quantidadeInimigos;

    Dificuldade(String rotulo, int pontuacaoInicial, int quantidadePassageiros,
                int quantidadeAsteroides, int quantidadeInimigos) {
        this.rotulo = rotulo;
        this.pontuacaoInicial = pontuacaoInicial;
        this.quantidadePassageiros = quantidadePassageiros;
        this.quantidadeAsteroides = quantidadeAsteroides;
        this.quantidadeInimigos = quantidadeInimigos;
    }

    public int getPontuacaoInicial() {
        return pontuacaoInicial;
    }

    public int getQuantidadePassageiros() {
        return quantidadePassageiros;
    }

    public int getQuantidadeAsteroides() {
        return quantidadeAsteroides;
    }

    public int getQuantidadeInimigos() {
        return quantidadeInimigos;
    }

    /** Aceita o número do menu, o nome do enum ou o rótulo com acento. */
    public static Dificuldade deTexto(String texto) {
        if (texto == null) {
            return MEDIO;
        }
        String normalizado = texto.trim().toLowerCase();
        if (normalizado.isEmpty()) {
            return MEDIO;
        }
        if (normalizado.equals("1") || normalizado.equals("facil") || normalizado.equals("fácil")) {
            return FACIL;
        }
        if (normalizado.equals("3") || normalizado.equals("dificil") || normalizado.equals("difícil")) {
            return DIFICIL;
        }
        return MEDIO;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
