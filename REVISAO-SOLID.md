# Revisão crítica da refatoração SOLID

**Projeto:** Missão Marte Unifor
**Versão analisada:** `src/solidexercicio10` (refatorada), comparada com `src/missao` (versão da equipe na atividade 1, preservada). O código-base oficial do professor está em `src/exercicio10`, também sem alterações; as regras de jogo são as mesmas, e os números da tabela abaixo se referem a `src/missao`.
**Equipe:** Levi Costa Queiroz (2510500), Pedro Nicolas (2513847), Leonardo Norões (2517418)
**Disciplina:** Projeto e Arquitetura de Sistemas — UNIFOR

Este documento é a revisão do nosso próprio código depois de concluída a refatoração. Ele não repete o que o tutorial manda fazer: aponta o que ficou bom, o que ficou discutível e o que faríamos diferente, com prioridade para cada item.

---

## 1. Panorama do que mudou

| | Versão original (`src/missao`) | Versão refatorada (`src/solidexercicio10`) |
|---|---|---|
| Arquivos `.java` | 12 | 28 (25 no pacote + 3 tipos auxiliares: `Direcao`, `LimitesMapa`, `CriadorPassageiro`) |
| Linhas na `Main` | 383 | 47 |
| Maior arquivo | `Main`, 383 linhas | `Missao`, 247 linhas (cerca de 90 são javadoc) |
| Responsabilidades da `Main` | menu, laço de jogo, sorteio de entidades, desenho do mapa, estatísticas, formatação do ranking, leitura do teclado | montar dependências e chamar `JogoService.executar()` |
| `instanceof` no código | 2 (decidindo símbolo do mapa) | 0 |
| Teste automatizado possível? | Não. Toda regra estava presa ao laço de `Scanner` | Sim. 72 verificações rodando sem teclado e sem arquivo |

A conta de arquivos subiu de 12 para 28, e isso merece justificativa em cada caso — é exatamente o risco que o enunciado aponta. As adições que consideramos pagas pelo benefício: `LimitesMapa` (eliminou 4 parâmetros repetidos em 6 assinaturas), `Direcao` (tirou o teclado de dentro da `Nave`), `Plataforma` (eliminou o caso especial `if (x == 0 && y == 0)` do desenho), `ResultadoMissao` (permitiu passar o desfecho sem expor a `Missao` inteira), `RankingRepository` + `RankingEmMemoria` (tornou o ranking testável) e as três classes de apresentação. As que consideramos discutíveis estão nos itens 4.2 e 4.6 abaixo.

---

## 2. Observações por princípio

### 2.1 SRP — símbolo de desenho morando no domínio

- **Local:** `model/EntidadeMapa.getSimbolo()` e `getDescricao()`, implementados em todas as subclasses
- **Princípio relacionado:** SRP (e, em menor grau, separação domínio/apresentação)
- **Observação:** para eliminar a cadeia de `instanceof` do renderizador, colocamos o caractere do mapa dentro da entidade de domínio. O efeito é que uma decisão de apresentação (`'@'`, `'#'`, `'L'`) passou a ser atributo de regra de negócio. Se amanhã o jogo tiver interface gráfica, `Nave` continuará afirmando que é desenhada como `@`, o que não será verdade.
- **Impacto para manutenção, testes ou evolução:** baixo hoje, relevante quando existir uma segunda forma de exibição. Nesse momento, duas apresentações diferentes competiriam pelo mesmo método.
- **Proposta:** manter `getSimbolo()` enquanto a única saída for console; ao surgir a segunda interface, mover o mapeamento para a camada de apresentação (`Map<Class<? extends EntidadeMapa>, Character>` no `MapaRenderer`) ou usar um Visitor. Registramos isto como dívida consciente: trocamos pureza de camada por OCP no renderizador, e o renderizador é o que mais mexemos durante os exercícios.
- **Prioridade:** média

### 2.2 SRP — `Missao` acumula estado, regras e cronômetro

- **Local:** `model/Missao`, campo `instanteInicio` e método `getDuracaoSegundos()`
- **Princípio relacionado:** SRP
- **Observação:** a missão é dona do mapa, das regras do turno, da pontuação **e** do relógio. O relógio é o intruso: ele lê `System.currentTimeMillis()` diretamente, então a duração da partida não é controlável de fora.
- **Impacto:** nosso teste de persistência do ranking teve que construir `ResultadoMissao` à mão para poder afirmar uma duração fixa (77s). Se a duração passar a valer pontos, essa regra vira intestável sem esperar tempo real passar.
- **Proposta:** receber um `java.time.Clock` (ou uma interface `Cronometro`) no construtor da `Missao`, com `Clock.systemDefaultZone()` sendo passado pela `Main`. São ~10 linhas e destrava o teste.
- **Prioridade:** média

### 2.3 OCP — extensão real de passageiros, proporção ainda fixa

- **Local:** `service/FabricaDeMissao.CRIADORES` e `criarPassageiro(...)`
- **Princípio relacionado:** OCP
- **Observação:** o ganho é concreto — criar um novo tipo de passageiro é escrever a subclasse e acrescentar **uma** linha na lista `CRIADORES`; nem o laço de sorteio, nem a `Missao`, nem o `MapaRenderer`, nem o `JogoService` mudam. O limite é que a distribuição dos tipos é `indice % CRIADORES.size()`, ou seja, rigorosamente igualitária. Incluir um quarto tipo altera silenciosamente a proporção dos três já existentes e, com ela, o balanceamento da pontuação.
- **Impacto:** balanceamento muda sem que ninguém tenha pedido; o efeito só aparece jogando.
- **Proposta:** dar peso a cada criador (por exemplo `new Peso(Professor::new, 3)`) ou deixar a `Dificuldade` informar a composição desejada. Só vale a pena quando existir um quarto tipo.
- **Prioridade:** baixa

### 2.4 LSP — o contrato da subclasse de `Passageiro` não está verificado

- **Local:** `model/Passageiro.getPontuacao()`
- **Princípio relacionado:** LSP
- **Observação:** tornar `Passageiro` abstrata resolveu o problema da versão original, em que a classe-base era concreta e devolvia 10 pontos por padrão — ali, esquecer um `@Override` produzia um passageiro silenciosamente errado em vez de erro de compilação. Ainda assim, o contrato implícito "embarcar um passageiro nunca reduz a pontuação" não está escrito em lugar algum: nada impede uma subclasse futura devolver `-5` e inverter a regra do jogo sem quebrar nenhuma assinatura.
- **Impacto:** uma substituição inválida passaria pelo compilador e só apareceria como bug de jogabilidade.
- **Proposta:** validar no construtor de `Passageiro` (`if (getPontuacao() <= 0) throw ...` não serve em construtor por causa da ordem de inicialização; o caminho é um teste de contrato que percorre `FabricaDeMissao.tiposDisponiveis()` e exige pontuação positiva de cada tipo). Já deixamos `tiposDisponiveis()` público exatamente para isso, mas o teste ainda não foi escrito.
- **Prioridade:** média

### 2.5 LSP/duplicação — `moverPara` repetido em `Nave` e `Inimigo`

- **Local:** `model/Nave.moverPara(...)` e `model/Inimigo.moverPara(...)`
- **Princípio relacionado:** LSP (implementações coerentes do mesmo contrato) e DRY
- **Observação:** os dois corpos são idênticos: checar `limites.contem(...)` e chamar `definirPosicao(...)`. Se um dia alguém corrigir a regra de borda em uma das classes e esquecer a outra, `Nave` e `Inimigo` deixarão de respeitar o mesmo contrato de `Movel` — exatamente o tipo de divergência que o LSP quer evitar.
- **Impacto:** risco de comportamento divergente entre duas entidades que deveriam se mover igual.
- **Proposta:** criar `abstract class EntidadeMovel extends EntidadeMapa implements Movel` com a implementação única de `moverPara`, e fazer `Nave` e `Inimigo` herdarem dela. Não fizemos ainda porque adiciona um nível de herança para economizar 6 linhas, e queríamos manter a hierarquia rasa.
- **Prioridade:** baixa

### 2.6 ISP — `RankingRepository` está no tamanho certo, e resistimos a fatiar mais

- **Local:** `repository/RankingRepository`
- **Princípio relacionado:** ISP
- **Observação:** a interface tem três métodos (`carregar`, `salvar`, `limpar`) e o único cliente, `RankingService`, usa todos os três. Chegamos a considerar separar em `LeitorDeRanking` e `EscritorDeRanking`, e decidimos **não** fazer: não existe cliente que só leia. Criar as duas interfaces agora produziria arquivos sem cliente, que é o defeito que o enunciado pede para evitar. O que de fato deixamos fora da interface foi a regra de Top 5: ela não é operação de armazenamento e a incluiríamos à força em toda futura implementação.
- **Impacto:** nenhum problema aberto. É a decisão que revisaríamos se aparecesse uma tela pública de ranking somente-leitura.
- **Proposta:** manter como está; fatiar apenas quando existir um cliente exclusivamente leitor.
- **Prioridade:** baixa (nenhuma ação agora)

### 2.7 DIP — a persistência ainda escreve no console (o furo mais sério)

- **Local:** `repository/RankingArquivoJson`, métodos `carregar()`, `salvar()` e `limpar()` — as chamadas `System.out.println` nos blocos `catch`
- **Princípio relacionado:** DIP (e SRP)
- **Observação:** o `RankingService` depende corretamente da abstração `RankingRepository`, e a `Main` é o único lugar do projeto que instancia implementação concreta — isso funciona. Mas a implementação de baixo nível fala diretamente com o console: em caso de falha de I/O ela imprime a mensagem ela mesma. Ou seja, a camada mais baixa depende de um detalhe de apresentação, e a inversão que fizemos no nível de cima é desfeita no nível de baixo. Herdamos isso da versão original sem questionar.
- **Impacto:** alto para evolução. Uma implementação em banco de dados vai querer registrar log, não imprimir na tela do jogador; e uma interface gráfica teria mensagens de erro saindo pelo terminal, invisíveis para o usuário. Além disso, teste que provoque falha de escrita suja a saída do teste.
- **Proposta:** o repositório lança uma exceção própria (`RankingIndisponivelException`) ou recebe um `Notificador` no construtor; quem decide o que o jogador lê continua sendo a camada de apresentação. É a mudança que implementaríamos primeiro.
- **Prioridade:** **alta**

---

## 3. Melhoria adicional identificada

- **Local:** `service/RankingService.registrar(...)` e `listar()`
- **Princípio relacionado:** SRP / eficiência
- **Observação:** `registrar` chama `listar()` duas vezes (direto e via `classificaNoTop`) e cada `listar()` chama `repositorio.carregar()`, o que significa reabrir e reparsear o `ranking.json` três vezes ao fim de uma partida vencida. Funciona e é barato para cinco linhas de ranking, mas é trabalho repetido por motivo nenhum e, com duas instâncias do jogo abertas, abre janela para leituras inconsistentes.
- **Impacto:** desempenho irrelevante hoje; a inconsistência com acesso concorrente é real, ainda que improvável em jogo de console.
- **Proposta:** carregar uma vez dentro de `registrar`, trabalhar sobre essa lista e salvar ao final — três linhas de mudança, sem alterar a interface pública do serviço.
- **Prioridade:** média

---

## 4. Decisões do tutorial: onde concordamos e onde não

### 4.1 Concordamos: `RankingRepository` como interface separada da implementação

**Benefício comprovado na prática, não só no papel.** Escrevemos `RankingEmMemoria` em pouco mais de 30 linhas e a bateria de testes do ranking (Top 5, ordenação, reset, recusa de derrota) roda inteira sem criar um único arquivo em disco — 14 verificações que, na versão original, seriam impossíveis sem fabricar `ranking.json` no meio do teste. Trocar a persistência do jogo é uma linha na `Main`:

```java
RankingRepository repositorio = new RankingArquivoJson(ARQUIVO_RANKING);
// RankingRepository repositorio = new RankingEmMemoria();
```

Nenhuma outra classe é recompilada por causa dessa troca. Responde diretamente à pergunta orientadora "o que seria necessário para trocar o arquivo por um banco": criar a classe que implementa a interface e mudar essa linha.

### 4.2 Discordamos: `RankingService` como a classe que acessa o arquivo

O tutorial propõe `RankingRepository` (interface), `RankingService` (implementação que lê o JSON) e `RankingEntry`, todos em `repository`. Implementamos diferente e sustentamos a diferença:

- **`RankingService` foi para `service`** e virou regra de negócio: ordenação, limite de cinco posições e o critério de classificação (só entra quem venceu e terminou com pontuação positiva). Quem lê e grava arquivo é `RankingArquivoJson`. O motivo é o teste do SRP, "quais motivos diferentes fazem esta classe mudar?": mudar o Top 5 para Top 10 é decisão de jogo; mudar JSON para CSV é decisão de infraestrutura. Na estrutura do tutorial, as duas mudanças caem na mesma classe.
- **`RankingEntry` foi para `model`**, porque é dado de domínio: continuaria existindo se o ranking fosse para banco, memória ou rede. Deixá-lo em `repository` forçaria `model` a depender de `repository` ou o serviço a importar tipo de persistência para falar de pontuação.
- **Consequência honesta:** nosso projeto tem uma classe a mais que o do tutorial, e quem seguir o tutorial à risca não vai achar `RankingService` no mesmo pacote. Os diagramas em `docs/uml/` refletem a estrutura real implementada, como o enunciado exige.

### 4.3 Discordamos em parte: o tamanho da camada de apresentação

O tutorial sugere um único `MapaRenderer` para a apresentação. Nós chegamos a três classes: `Console` (só I/O de texto), `MapaRenderer` (só o mapa) e `TelaJogo` (menus, perguntas, mensagens de turno, estatísticas, ranking). A divisão vale pela consequência prática — nenhuma classe de `service` ou `model` contém uma única chamada a `System.out` ou cria um `Scanner`, e o `JogoService` chama `tela.anunciarColisao(2)` sem saber em que idioma a mensagem sai.

O custo, que assumimos: `TelaJogo` ficou com cerca de 30 métodos curtos e faz entrada **e** saída, o que é uma responsabilidade e meia. Dividir em `EntradaDoJogador` e `SaidaDoJogo` deixaria mais puro e também mais burocrático, com dois campos a mais em todo lugar. Para um jogo de console de uma tela só, achamos que não se paga — e essa é justamente a pergunta orientadora sobre complexidade desnecessária. Prioridade baixa, revisável se a tela crescer.

---

## 5. Testes realizados e resultados

### 5.1 Testes automatizados

Arquivo: `tests/solidexercicio10/tests/TesteRefatoracao.java`. Sem biblioteca externa, sem teclado, sem depender de arquivo (exceto o teste que verifica justamente o arquivo, que usa arquivo temporário do sistema).

```bash
javac -encoding UTF-8 -d out $(find src/solidexercicio10 tests -name "*.java")
java -cp out solidexercicio10.tests.TesteRefatoracao
```

**Resultado: 72 verificações, 0 falhas.** Saída completa em `docs/evidencias/testes-automatizados.txt`.

| Grupo | O que foi verificado | Resultado |
|---|---|---|
| Vitória | resgatar todos **não** basta; só vence pousando em (0,0) | ok |
| Pontuação | Professor +10, Engenheiro +15, Astronauta +20 pela chamada polimórfica | ok |
| Combustível | cada movimento custa 1 ponto; movimento recusado não cobra nada | ok |
| Limites | movimento além da borda é recusado e não altera posição, pontuação nem contador | ok |
| Colisão | detecção na casa do obstáculo, perda de vida, destruição em 3 colisões, vidas nunca negativas | ok |
| Capacidade | nave cheia recusa embarque e o passageiro continua no mapa | ok |
| Encapsulamento | `nave.getPassageiros().add(...)` lança `UnsupportedOperationException` | ok |
| Fábrica | quantidades vêm da `Dificuldade`, nada se sobrepõe, nada ocupa (0,0), capacidade comporta todos | ok |
| Inimigo | 500 turnos aleatórios sem sair dos limites (semente fixa 7) | ok |
| Renderização | todas as entidades fornecem símbolo próprio; nave tem prioridade de desenho | ok |
| Ranking | Top 5, ordem decrescente, descarte do pior, recorde, `classificaNoTop` | ok |
| Ranking | derrota e vitória com 0 ponto **não** entram | ok |
| Reset | limpa serviço e repositório | ok |
| JSON | grava, lê de volta, preserva aspas no nome do piloto, `limpar()` remove o arquivo | ok |
| Entrada | `Dificuldade.deTexto` com número, nome, acento, vazio, nulo e lixo; `Direcao.deTecla` nas 4 teclas e em teclas inválidas | ok |

### 5.2 Testes manuais do jogo completo

Executados pelo terminal, com a saída registrada em `docs/evidencias/`:

| Cenário | Comandos | Esperado | Resultado |
|---|---|---|---|
| Partida vencida | menu 1, piloto, dificuldade Fácil, mapa 1, varrer o mapa embarcando, voltar a (0,0) | vitória, estatísticas, gravação no ranking | ok — 73 pontos, 4 passageiros, 12 movimentos, registro salvo (`docs/evidencias/partida-vitoria.txt`) |
| Conta da pontuação | a mesma partida | 30 inicial − 12 movimentos + (10+15+20+10) = 73 | ok, bateu exatamente |
| Ranking vazio | menu 2 num repositório limpo | "Nenhum registro ainda" | ok |
| Ranking com dados | menu 2 após a vitória | posição, pontos, dificuldade, passageiros, duração e data/hora | ok |
| Opção inválida | menu 9 | "Opção inválida" e menu de novo, sem quebrar | ok (`docs/evidencias/menu-e-ranking.txt`) |
| Reset cancelado | menu 3, responder `n` | "Operação cancelada", ranking intacto | ok |
| Reset confirmado | menu 3, responder `s` | "Ranking resetado" e `ranking.json` removido | ok |
| Abortar missão | tecla `q` no meio da partida | encerra, mostra estatísticas, não grava no ranking | ok |
| Comando inválido | tecla `z` durante a partida | "Comando desconhecido", turno não avança | ok |
| Entrada encerrada | fim do stdin | mensagem de encerramento em vez de exceção | ok |
| Mapa configurável | tamanhos 1, 3 e 5 | mapa redesenhado na dimensão pedida | ok |
| Compilação | `javac -Xlint:all` em todo o pacote | sem erros e sem avisos | ok |

### 5.3 Comparação com a versão original

Rodamos `src/missao` (original) e `src/solidexercicio10` (refatorada) lado a lado nos cenários acima. Todos os comportamentos observáveis coincidem: mesmo menu, mesmas teclas, mesma pontuação inicial por dificuldade, mesmo custo de movimento, mesmas três vidas, mesma regra de pouso em (0,0), mesmo formato de `ranking.json` — os arquivos gerados pelas duas versões são intercambiáveis. As únicas diferenças visíveis são adições: o cabeçalho informa "versão refatorada com SOLID", a linha de status mostra `Passageiros a bordo: n/capacidade`, a legenda do mapa é montada a partir das entidades presentes e as estatísticas finais também exibem piloto, dificuldade e desfecho.

---

## 6. Limitações que permanecem

1. A mensagem de erro de I/O sai pelo console de dentro do repositório (item 2.7). É a primeira coisa que mudaríamos.
2. A duração da partida usa o relógio do sistema diretamente, o que a torna intestável (item 2.2).
3. Não há teste de contrato para subclasses de `Passageiro` (item 2.4).
4. O símbolo do mapa vive no domínio (item 2.1) — dívida assumida em troca de um renderizador fechado a mudanças.
5. Em mapa muito pequeno com dificuldade Difícil (11 entidades em 8 casas livres), a fábrica para após 500 tentativas e a missão começa com menos obstáculos que o previsto, sem avisar o jogador. Preferimos isso a um laço infinito — que é o que a versão original faz nessa situação —, mas o certo seria validar o tamanho mínimo do mapa ao perguntá-lo.
6. Não há tratamento para `ranking.json` corrompido por edição manual: o parser simplesmente ignora o que não entende e devolve as entradas que conseguiu ler.

---

## 7. Qual melhoria implementaríamos primeiro

Tirar o `System.out` do `RankingArquivoJson` (item 2.7). É a única observação de prioridade alta, é pequena, e é a que de fato completa a inversão de dependência que o resto da refatoração já montou: hoje temos uma arquitetura em que a regra não conhece a persistência, mas a persistência conhece a tela.
