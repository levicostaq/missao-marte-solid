package missao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Responsável por toda a persistência do ranking em ranking.json.
 * Mantido separado de Main para não sobrecarregar a classe principal
 * (item de refatoração exigido no Exercício 10).
 */
public class RankingRepositorio {

    private static final int TAMANHO_MAXIMO = 5;

    private final Path arquivo;

    public RankingRepositorio(Path arquivo) {
        this.arquivo = arquivo;
    }

    public List<RankingEntry> carregar() {
        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }
        try {
            String conteudo = new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8).trim();
            return parseJson(conteudo);
        } catch (IOException e) {
            System.out.println("Não foi possível ler o ranking (" + e.getMessage() + "). Começando do zero.");
            return new ArrayList<>();
        }
    }

    /**
     * Adiciona uma nova entrada, mantém só o Top 5 e persiste em disco.
     */
    public List<RankingEntry> registrarResultado(List<RankingEntry> rankingAtual, RankingEntry novaEntrada) {
        List<RankingEntry> atualizado = new ArrayList<>(rankingAtual);
        atualizado.add(novaEntrada);
        atualizado.sort(Comparator.comparingInt(RankingEntry::getPontuacao).reversed());
        if (atualizado.size() > TAMANHO_MAXIMO) {
            atualizado = new ArrayList<>(atualizado.subList(0, TAMANHO_MAXIMO));
        }
        salvar(atualizado);
        return atualizado;
    }

    public boolean isTopScore(List<RankingEntry> ranking, int pontuacao) {
        if (ranking.size() < TAMANHO_MAXIMO) {
            return true;
        }
        return pontuacao > ranking.get(ranking.size() - 1).getPontuacao();
    }

    public void resetar() {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            System.out.println("Erro ao resetar o ranking: " + e.getMessage());
        }
    }

    private void salvar(List<RankingEntry> ranking) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < ranking.size(); i++) {
            RankingEntry entrada = ranking.get(i);
            json.append("{")
                    .append("\"piloto\":\"").append(escapar(entrada.getPiloto())).append("\",")
                    .append("\"pontuacao\":").append(entrada.getPontuacao()).append(",")
                    .append("\"dificuldade\":\"").append(entrada.getDificuldade().name()).append("\",")
                    .append("\"passageirosResgatados\":").append(entrada.getPassageirosResgatados()).append(",")
                    .append("\"dataHora\":\"").append(escapar(entrada.getDataHora())).append("\",")
                    .append("\"duracaoSegundos\":").append(entrada.getDuracaoSegundos())
                    .append("}");
            if (i < ranking.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        try {
            Files.write(arquivo, json.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Não foi possível salvar o ranking: " + e.getMessage());
        }
    }

    /**
     * Parser manual e simples de JSON (sem bibliotecas externas), suficiente
     * para o formato fixo que este projeto grava.
     */
    private List<RankingEntry> parseJson(String json) {
        List<RankingEntry> ranking = new ArrayList<>();
        if (json.isEmpty() || json.equals("[]")) {
            return ranking;
        }

        String corpo = json;
        if (corpo.startsWith("[")) {
            corpo = corpo.substring(1);
        }
        if (corpo.endsWith("]")) {
            corpo = corpo.substring(0, corpo.length() - 1);
        }

        int cursor = 0;
        while (cursor < corpo.length()) {
            int inicio = corpo.indexOf('{', cursor);
            if (inicio < 0) {
                break;
            }
            int fim = corpo.indexOf('}', inicio);
            if (fim < 0) {
                break;
            }
            RankingEntry entrada = parseObjeto(corpo.substring(inicio + 1, fim));
            if (entrada != null) {
                ranking.add(entrada);
            }
            cursor = fim + 1;
        }

        ranking.sort(Comparator.comparingInt(RankingEntry::getPontuacao).reversed());
        return ranking;
    }

    private RankingEntry parseObjeto(String objeto) {
        String piloto = null;
        Integer pontuacao = null;
        Dificuldade dificuldade = Dificuldade.MEDIO;
        int passageirosResgatados = 0;
        String dataHora = "";
        long duracaoSegundos = 0;

        for (String campo : objeto.split(",")) {
            String[] partes = campo.split(":", 2);
            if (partes.length != 2) {
                continue;
            }
            String chave = partes[0].trim().replace("\"", "");
            String valor = partes[1].trim();

            switch (chave) {
                case "piloto":
                    piloto = despir(valor);
                    break;
                case "pontuacao":
                    pontuacao = tentarParseInt(valor);
                    break;
                case "dificuldade":
                    dificuldade = Dificuldade.fromTexto(despir(valor));
                    break;
                case "passageirosResgatados":
                    Integer p = tentarParseInt(valor);
                    passageirosResgatados = p == null ? 0 : p;
                    break;
                case "dataHora":
                    dataHora = despir(valor);
                    break;
                case "duracaoSegundos":
                    try {
                        duracaoSegundos = Long.parseLong(valor);
                    } catch (NumberFormatException ignored) {
                        duracaoSegundos = 0;
                    }
                    break;
                default:
                    break;
            }
        }

        if (piloto == null || pontuacao == null) {
            return null;
        }
        return new RankingEntry(piloto, pontuacao, dificuldade, passageirosResgatados, dataHora, duracaoSegundos);
    }

    private String despir(String valorComAspas) {
        if (valorComAspas.startsWith("\"") && valorComAspas.endsWith("\"") && valorComAspas.length() >= 2) {
            return valorComAspas.substring(1, valorComAspas.length() - 1).replace("\\\"", "\"");
        }
        return valorComAspas;
    }

    private Integer tentarParseInt(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("\"", "\\\"");
    }
}
