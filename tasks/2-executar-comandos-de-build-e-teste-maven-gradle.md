# Especificações Técnicas Detalhadas: Fase 1 (MVP)

Este documento contém as especificações técnicas detalhadas para todas as User Stories da Fase 1 (MVP) do roadmap de implementação do sistema multi-agentes autônomo para geração de testes unitários. Cada especificação detalha como as tarefas e sub-tarefas devem ser implementadas para atender aos critérios de aceitação da respectiva User Story, com foco na entrega de um produto mínimo viável funcional.

# Especificação Técnica Detalhada: US7 - Executar Comandos de Build e Teste Maven/Gradle

**User Story:** US7: Como agente de IA, quero executar comandos de build e teste Maven/Gradle para que eu possa compilar o projeto e validar os testes gerados.

**Épico Relacionado:** Implementar um Sistema Multi-Agentes 100% Autônomo para Geração Automática de Testes Unitários em Projetos Java.

**Fase do Roadmap:** FASE 1: MVP - CORE FUNCIONAL

## 1. Visão Geral

Esta especificação detalha a implementação da funcionalidade de execução de comandos de build e teste (Maven/Gradle), que é um componente crítico da fase de Ação/Tool Calling do agente. O objetivo é permitir que o agente compile o código gerado (incluindo os testes unitários) e execute os testes, capturando os resultados para que o agente possa avaliar o sucesso ou falha e o coverage.

## 2. Critérios de Aceitação da User Story

Para que a US7 seja considerada concluída, o sistema deve atender aos seguintes critérios:

* O agente executa comandos Maven (ex: `mvn test`, `mvn clean install`).
* O agente captura a saída e o código de saída dos comandos.
* O agente identifica se o build e os testes foram bem-sucedidos ou falharam.

## 3. Detalhamento Técnico por Tarefa e Sub-tarefa

### Tarefa 7.1: Implementar `BuildAndTestTool`

**Objetivo:** Criar uma classe de ferramenta (`Tool`) que encapsule a lógica de execução de comandos de sistema, especificamente para Maven/Gradle, e retorne a saída e o status de execução.

**Nome da Classe:** `com.example.agent.action.BuildAndTestTool`

**Dependências:** Nenhuma dependência externa específica além das APIs de Process do Java.

#### Sub-tarefa 7.1.1: Criar método `executeMavenCommand(projectPath, command)`

**Descrição:** Este método será a interface principal para o agente invocar comandos Maven. Ele receberá o caminho do projeto onde o comando deve ser executado e o comando Maven específico (ex: "clean install", "test").

**Assinatura do Método:**

```java
public CommandResult executeMavenCommand(String projectPath, String command) {
    // ... implementação ...
}

// Classe auxiliar para o resultado do comando
public static class CommandResult {
    public int exitCode;
    public String output;
    public String errorOutput;
    public boolean success;
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `executeMavenCommand` aceita o caminho do projeto e o comando Maven como `String`.
* O método retorna um objeto `CommandResult` contendo o código de saída, a saída padrão, a saída de erro e um indicador de sucesso.

#### Sub-tarefa 7.1.2: Utilizar `ProcessBuilder` para executar comandos Maven

**Descrição:** A classe `java.lang.ProcessBuilder` é a forma recomendada para executar processos externos em Java. Ela oferece controle sobre o diretório de trabalho, variáveis de ambiente e redirecionamento de I/O.

**Passos de Implementação:**

1. **Criação do `ProcessBuilder`:** Instanciar `ProcessBuilder` com o comando completo. O comando Maven geralmente é `mvn` seguido dos argumentos. Ex: `new ProcessBuilder(mvn", "clean", "install").
2. **Configuração do Diretório:** Utilizar `directory(new File(projectPath))` para definir o diretório de trabalho do processo.
3. **Início do Processo:** Chamar `start()` para iniciar a execução do comando.

**Exemplo de Código:**

```java
import java.io.File;
import java.io.IOException;

// ... dentro do executeMavenCommand method

ProcessBuilder processBuilder = new ProcessBuilder();
processBuilder.command("mvn", command.split("\\s+")); // Divide o comando em argumentos
processBuilder.directory(new File(projectPath));

try {
    Process process = processBuilder.start();
    // ... continuar com a captura da saída ...
} catch (IOException e) {
    // ... tratar erro de inicialização do processo ...
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método utiliza `ProcessBuilder` para configurar e iniciar o processo Maven.
* O diretório de trabalho do processo é definido corretamente para o `projectPath`.

#### Sub-tarefa 7.1.3: Capturar a saída padrão e de erro do processo

**Descrição:** É crucial capturar a saída padrão (`stdout`) e a saída de erro (`stderr`) do processo Maven. A `stdout` contém informações sobre o progresso do build, enquanto a `stderr` contém mensagens de erro que são essenciais para a depuração.

**Passos de Implementação:**

1. **Leitura dos Streams:** Obter os `InputStream` para `stdout` e `stderr` do objeto `Process` (`process.getInputStream()` e `process.getErrorStream()`).
2. **Consumo dos Streams:** Ler os streams em threads separadas para evitar deadlocks. Uma abordagem comum é usar `BufferedReader` para ler linha por linha e `StringBuilder` para acumular a saída.
3. **Aguardar Conclusão:** Chamar `process.waitFor()` para aguardar a conclusão do processo e obter o código de saída.

**Exemplo de Código:**

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

// ... dentro do try block após processBuilder.start()

StringBuilder output = new StringBuilder();
StringBuilder errorOutput = new StringBuilder();

// Leitura da saída padrão e de erro em threads separadas
Future<String> outputFuture = Executors.newSingleThreadExecutor().submit(() -> {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
});
Future<String> errorFuture = Executors.newSingleThreadExecutor().submit(() -> {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
});

int exitCode = process.waitFor();

result.output = outputFuture.get();
result.errorOutput = errorFuture.get();
result.exitCode = exitCode;
```

**Critérios de Aceitação da Sub-tarefa:**

* A saída padrão e a saída de erro do processo Maven são capturadas completamente.
* A captura da saída não causa deadlocks no processo.

#### Sub-tarefa 7.1.4: Retornar a saída completa e o código de saída

**Descrição:** O método deve retornar todas as informações coletadas (código de saída, `stdout`, `stderr`) em um objeto `CommandResult` estruturado. Isso permite que o agente analise o resultado da execução de forma programática.

**Passos de Implementação:**

1. **Criação do `CommandResult`:** Instanciar o objeto `CommandResult`.
2. **Preenchimento dos Dados:** Preencher o objeto com o `exitCode`, `output` e `errorOutput` coletados.
3. **Definição do Status de Sucesso:** Adicionar uma lógica para definir o campo `success` do `CommandResult`. Geralmente, um `exitCode` de 0 indica sucesso.

**Exemplo de Código:**

```java
// ... após obter exitCode, output e errorOutput

CommandResult result = new CommandResult();
result.exitCode = exitCode;
result.output = outputFuture.get();
result.errorOutput = errorFuture.get();
result.success = (exitCode == 0);

return result;
```

**Critérios de Aceitação da Sub-tarefa:**

* O método retorna um objeto `CommandResult` preenchido com todas as informações relevantes.
* O campo `success` do `CommandResult` reflete corretamente o código de saída do processo.

#### Sub-tarefa 7.1.5: (Opcional) Adicionar suporte para Gradle

**Descrição:** Para tornar a ferramenta mais versátil, o suporte para Gradle pode ser adicionado. Isso envolveria a criação de um método `executeGradleCommand` ou a adaptação do método existente para lidar com as diferenças entre os comandos Maven e Gradle.

**Passos de Implementação:**

1. **Criação de um Método `executeGradleCommand`:** Similar ao `executeMavenCommand`, mas utilizando o comando `gradlew` (ou `gradle`) e seus respectivos argumentos.
2. **Generalização do Método de Execução:** Criar um método privado genérico que receba o comando completo como uma lista de `String` e execute o `ProcessBuilder`, sendo chamado pelos métodos específicos de Maven e Gradle.

**Critérios de Aceitação da Sub-tarefa:**

* (Se implementado) O agente pode executar comandos Gradle com sucesso.
* (Se implementado) A captura de saída e o retorno de resultados funcionam da mesma forma que para os comandos Maven.

## 4. Integração com Google ADK (Conceitual)

Para integrar a `BuildAndTestTool` com o Google ADK, ela precisará ser registrada como uma `Tool` que o agente pode invocar. O Google ADK permitirá que o LLM ou o `TestPlanner` chame o método `executeMavenCommand` com o caminho do projeto e o comando desejado, e receba o `CommandResult` para avaliar o sucesso do build e dos testes.

## 5. Testes Unitários (para `BuildAndTestTool`)

Testar a `BuildAndTestTool` pode ser complexo, pois envolve a execução de processos externos. Estratégias de teste podem incluir:

* **Testes de Integração:** Criar um projeto Maven/Gradle de exemplo dentro do diretório de testes e executar comandos reais contra ele, verificando os resultados.
* **Mocks:** Mockar a classe `ProcessBuilder` e `Process` para simular diferentes cenários de execução (sucesso, falha, saídas específicas) sem a necessidade de executar processos reais. Isso é mais rápido e isolado, mas menos realista.
* **Casos de Teste:**
  * Execução de um comando Maven bem-sucedido (ex: `mvn compile`).
  * Execução de um comando Maven que falha (ex: `mvn test` com um teste que falha).
  * Verificação da captura correta de `stdout` e `stderr`.
  * Verificação do código de saída e do status de sucesso.

## 6. Referências

* **Java ProcessBuilder:** [https://docs.oracle.com/javase/8/docs/api/java/lang/ProcessBuilder.html](https://docs.oracle.com/javase/8/docs/api/java/lang/ProcessBuilder.html)

---

**Autor:** Manus AI
**Data:** 25 de Julho de 2025

---
