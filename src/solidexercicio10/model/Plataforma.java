package solidexercicio10.model;

/**
 * Plataforma de pouso, sempre na coordenada (0,0).
 *
 * <p>Na versão original o 'L' era um {@code if (x == 0 && y == 0)} solto no
 * meio do desenho do mapa. Transformar a plataforma em entidade elimina
 * esse caso especial: ela é desenhada e comparada como qualquer outra
 * coisa do mapa.</p>
 */
public class Plataforma extends EntidadeMapa {

    public Plataforma() {
        super(0, 0);
    }

    @Override
    public char getSimbolo() {
        return 'L';
    }

    @Override
    public String getDescricao() {
        return "plataforma de pouso";
    }
}
