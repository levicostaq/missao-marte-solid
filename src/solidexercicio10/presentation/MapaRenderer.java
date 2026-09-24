package solidexercicio10.presentation;

import java.util.LinkedHashMap;
import java.util.Map;

import solidexercicio10.model.EntidadeMapa;
import solidexercicio10.model.LimitesMapa;
import solidexercicio10.model.Missao;

/**
 * Desenha o estado da missão no console.
 *
 * <p>Não conhece nenhum tipo concreto de entidade: percorre
 * {@link Missao#getEntidades()} e pede a cada uma o seu símbolo. A versão
 * original decidia o caractere com uma cadeia de {@code instanceof}, de
 * modo que todo novo tipo de passageiro ou obstáculo obrigava a editar o
 * desenho do mapa. Aqui não obriga (OCP).</p>
 *
 * <p>A legenda também é montada a partir das entidades presentes, então
 * ela nunca fica dessincronizada do mapa.</p>
 */
public class MapaRenderer {

    private static final char CASA_VAZIA = '.';

    private final Console console;

    public MapaRenderer(Console console) {
        this.console = console;
    }

    public void renderizar(Missao missao) {
        LimitesMapa limites = missao.getLimites();

        console.escreverLinhaVazia();
        console.escrever(String.format("Pontos: %d | Vidas: %d | Piloto: %s | Passageiros a bordo: %d/%d",
                missao.getPontuacao(),
                missao.getNave().getVidas(),
                missao.getPiloto(),
                missao.getPassageirosResgatados(),
                missao.getNave().getCapacidade()));

        for (int y = limites.getMinY(); y <= limites.getMaxY(); y++) {
            StringBuilder linha = new StringBuilder();
            for (int x = limites.getMinX(); x <= limites.getMaxX(); x++) {
                linha.append(' ').append(simboloEm(missao, x, y));
            }
            console.escrever(linha.toString());
        }

        console.escrever(montarLegenda(missao));
    }

    private char simboloEm(Missao missao, int x, int y) {
        for (EntidadeMapa entidade : missao.getEntidades()) {
            if (entidade.estaEm(x, y)) {
                return entidade.getSimbolo();
            }
        }
        return CASA_VAZIA;
    }

    private String montarLegenda(Missao missao) {
        Map<Character, String> itens = new LinkedHashMap<>();
        for (EntidadeMapa entidade : missao.getEntidades()) {
            itens.putIfAbsent(entidade.getSimbolo(), entidade.getDescricao());
        }

        StringBuilder legenda = new StringBuilder("Legenda:");
        for (Map.Entry<Character, String> item : itens.entrySet()) {
            legenda.append(' ').append(item.getKey()).append(' ').append(item.getValue()).append(" |");
        }
        legenda.append(' ').append(CASA_VAZIA).append(" espaço livre");
        return legenda.toString();
    }
}
