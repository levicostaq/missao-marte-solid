package solidexercicio10.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.RankingEntry;

/**
 * Persistência do ranking em arquivo JSON, sem bibliotecas externas.
 *
 * <p>Única responsabilidade: traduzir entre {@link RankingEntry} e texto em
 * disco. Ordenação, limite de posições e critério de classificação ficam
 * no serviço de ranking, então mudar o formato do arquivo não mexe em
 * regra de jogo e mudar a regra de jogo não mexe neste arquivo.</p>
 *
 * <p>Falha de leitura ou escrita não derruba a partida: o método informa o
 * problema e devolve o melhor resultado possível.</p>
 */
public class RankingArquivoJson implements RankingRepository {

    private final Path arquivo;

    public RankingArquivoJson(Path arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public List<RankingEntry> carregar() {
        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }
        try {
            String conteudo = new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8).trim();
            return lerJson(conteudo);
        } catch (IOException e) {
            System.out.println("Não foi possível ler o ranking (" + e.getMessage() + "). Começando do zero.");
            return new ArrayList<>();
        }
    }

    @Override
    public void salvar(List<RankingEntry> ranking) {
        try {
            Files.write(arquivo, escreverJson(ranking).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Não foi possível salvar o ranking: " + e.getMessage());
        }
    }

    @Override
    public void limpar() {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            System.out.println("Erro ao resetar o ranking: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Serialização
    // ------------------------------------------------------------------

    private String escreverJson(List<RankingEntry> ranking) {
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
        return json.append("]").toString();
    }

    /** Parser suficiente para o formato fixo que esta classe grava. */
    private List<RankingEntry> lerJson(String json) {
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
            RankingEntry entrada = lerObjeto(corpo.substring(inicio + 1, fim));
            if (entrada != null) {
                ranking.add(entrada);
            }
            cursor = fim + 1;
        }
        return ranking;
    }

    private RankingEntry lerObjeto(String objeto) {
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
                    piloto = desembrulhar(valor);
                    break;
                case "pontuacao":
                    pontuacao = tentarInteiro(valor);
                    break;
                case "dificuldade":
                    dificuldade = Dificuldade.deTexto(desembrulhar(valor));
                    break;
                case "passageirosResgatados":
                    Integer resgatados = tentarInteiro(valor);
                    passageirosResgatados = resgatados == null ? 0 : resgatados;
                    break;
                case "dataHora":
                    dataHora = desembrulhar(valor);
                    break;
                case "duracaoSegundos":
                    Integer duracao = tentarInteiro(valor);
                    duracaoSegundos = duracao == null ? 0 : duracao;
                    break;
                default:
                    break;
            }
        }

        if (piloto == null || pontuacao == null) {
            return null;
        }
        return new RankingEntry(piloto, pontuacao, dificuldade, passageirosResgatados,
                dataHora, duracaoSegundos);
    }

    private String desembrulhar(String valorComAspas) {
        if (valorComAspas.length() >= 2 && valorComAspas.startsWith("\"") && valorComAspas.endsWith("\"")) {
            return valorComAspas.substring(1, valorComAspas.length() - 1).replace("\\\"", "\"");
        }
        return valorComAspas;
    }

    private Integer tentarInteiro(String valor) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("\"", "\\\"");
    }
}
