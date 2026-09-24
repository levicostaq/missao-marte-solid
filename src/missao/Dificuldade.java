package missao;

/**
 * Níveis de dificuldade da missão, cada um ajustando quantidade de
 * obstáculos, pontuação inicial e quantidade de passageiros.
 */
public enum Dificuldade {
    FACIL,
    MEDIO,
    DIFICIL;

    public static Dificuldade fromTexto(String texto) {
        if (texto == null) {
            return MEDIO;
        }
        String normalizado = texto.trim().toLowerCase();
        if (normalizado.equals("facil") || normalizado.equals("fácil") || normalizado.equals("1")) {
            return FACIL;
        }
        if (normalizado.equals("dificil") || normalizado.equals("difícil") || normalizado.equals("3")) {
            return DIFICIL;
        }
        return MEDIO;
    }

    public int getPontuacaoInicial() {
        switch (this) {
            case FACIL:
                return 30;
            case DIFICIL:
                return 15;
            default:
                return 20;
        }
    }

    public int getQuantidadeAsteroides() {
        switch (this) {
            case FACIL:
                return 1;
            case DIFICIL:
                return 3;
            default:
                return 2;
        }
    }

    public int getQuantidadePassageiros() {
        switch (this) {
            case FACIL:
                return 4;
            case DIFICIL:
                return 5;
            default:
                return 3;
        }
    }

    public int getQuantidadeInimigos() {
        switch (this) {
            case FACIL:
                return 1;
            case DIFICIL:
                return 3;
            default:
                return 2;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case FACIL:
                return "Fácil";
            case DIFICIL:
                return "Difícil";
            default:
                return "Médio";
        }
    }
}
