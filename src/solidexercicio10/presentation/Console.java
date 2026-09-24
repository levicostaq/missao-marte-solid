package solidexercicio10.presentation;

import java.util.Scanner;

/**
 * Único ponto do programa que fala com o terminal.
 *
 * <p>Responsabilidade única: ler e escrever texto. Nenhuma outra classe
 * cria {@link Scanner} ou chama {@code System.out}, então trocar console
 * por interface gráfica no futuro é substituir esta camada, sem tocar em
 * regra de jogo.</p>
 */
public class Console {

    private final Scanner scanner = new Scanner(System.in);

    public void escrever(String mensagem) {
        System.out.println(mensagem);
    }

    public void escreverLinhaVazia() {
        System.out.println();
    }

    public boolean temEntrada() {
        return scanner.hasNextLine();
    }

    public String lerLinha(String prompt) {
        System.out.print(prompt);
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "";
    }

    /** Primeiro caractere digitado em minúsculo, ou '\0' se a linha vier vazia. */
    public char lerTecla(String prompt) {
        String entrada = lerLinha(prompt).toLowerCase();
        return entrada.isEmpty() ? '\0' : entrada.charAt(0);
    }

    public int lerInteiro(String prompt, int padrao) {
        String entrada = lerLinha(prompt);
        if (entrada.isEmpty()) {
            return padrao;
        }
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            escrever("Valor inválido, usando " + padrao + ".");
            return padrao;
        }
    }

    public boolean confirmar(String prompt) {
        String resposta = lerLinha(prompt).toLowerCase();
        return resposta.equals("s") || resposta.equals("sim");
    }

    public void fechar() {
        scanner.close();
    }
}
