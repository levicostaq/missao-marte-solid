package solidexercicio10.model;

/**
 * Fábrica de um tipo de passageiro.
 *
 * <p>É o gancho de OCP do sorteio de passageiros: a fábrica de missões
 * guarda uma lista destes criadores e nunca menciona {@code Professor},
 * {@code Engenheiro} ou {@code Astronauta} diretamente. Incluir um novo
 * tipo de passageiro no jogo é criar a subclasse e acrescentar uma linha
 * naquela lista.</p>
 */
@FunctionalInterface
public interface CriadorPassageiro {

    Passageiro criar(int indice, int x, int y);
}
