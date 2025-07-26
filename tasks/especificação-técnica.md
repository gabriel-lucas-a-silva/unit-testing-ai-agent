# Especificações Técnicas Detalhadas: Fase 1 (MVP)

Este documento contém as especificações técnicas detalhadas para todas as User Stories da Fase 1 (MVP) do roadmap de implementação do sistema multi-agentes autônomo para geração de testes unitários. Cada especificação detalha como as tarefas e sub-tarefas devem ser implementadas para atender aos critérios de aceitação da respectiva User Story, com foco na entrega de um produto mínimo viável funcional.

---

# Especificação Técnica Detalhada: US6 - Escrever Arquivos de Teste Java

**User Story:** US6: Como agente de IA, quero escrever arquivos de teste Java para que eu possa persistir os testes unitários gerados no projeto.

**Épico Relacionado:** Implementar um Sistema Multi-Agentes 100% Autônomo para Geração Automática de Testes Unitários em Projetos Java.

**Fase do Roadmap:** FASE 1: MVP - CORE FUNCIONAL

## 1. Visão Geral

Esta especificação detalha a implementação da funcionalidade de escrita de arquivos de teste Java, que é um componente crítico da fase de Ação/Tool Calling do agente. O objetivo é permitir que o agente crie novos arquivos `.java` ou modifique arquivos existentes no diretório de testes do projeto, persistindo o código dos testes unitários gerados.

## 2. Critérios de Aceitação da User Story

Para que a US6 seja considerada concluída, o sistema deve atender aos seguintes critérios:

* O agente cria novos arquivos `.java` no diretório de testes.
* O agente sobrescreve o conteúdo de arquivos de teste existentes quando necessário.
* O agente pode anexar novos métodos de teste a arquivos existentes.

## 3. Detalhamento Técnico por Tarefa e Sub-tarefa

### Tarefa 6.1: Implementar `CodeWriterTool`

**Objetivo:** Criar uma classe de ferramenta (`Tool`) que encapsule a lógica de escrita de arquivos de código Java, permitindo a criação, sobrescrita e adição de conteúdo a arquivos.

**Nome da Classe:** `com.example.agent.action.CodeWriterTool`

**Dependências:** Nenhuma dependência externa específica além das APIs de I/O do Java.

#### Sub-tarefa 6.1.1: Criar método `writeCodeFile(path, content)` para criar/sobrescrever arquivos

**Descrição:** Este método será responsável por criar um novo arquivo no caminho especificado ou sobrescrever um arquivo existente com o conteúdo fornecido. É a funcionalidade primária para persistir um teste unitário recém-gerado ou uma versão corrigida.

**Assinatura do Método:**

```java
public boolean writeCodeFile(String filePath, String content) {
    // ... implementação ...
}
```

**Passos de Implementação:**

1. **Criação de Diretórios:** Antes de escrever o arquivo, verificar se o diretório pai do `filePath` existe. Se não existir, criá-lo recursivamente para evitar `FileNotFoundException`.
2. **Escrita do Arquivo:** Utilizar `java.nio.file.Files.writeString()` para escrever o `content` no `filePath`. Este método sobrescreve o arquivo se ele já existir.
3. **Tratamento de Exceções:** Implementar blocos `try-catch` para `IOException` que pode ocorrer durante a criação de diretórios ou escrita do arquivo. Em caso de erro, a ferramenta deve retornar `false` e logar o problema.

**Exemplo de Código:**

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CodeWriterTool {

    public boolean writeCodeFile(String filePath, String content) {
        Path path = Paths.get(filePath);
        try {
            // Garante que o diretório pai exista
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Arquivo " + filePath + " escrito com sucesso.");
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao escrever o arquivo " + filePath + ": " + e.getMessage());
            return false;
        }
    }
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `writeCodeFile` cria um novo arquivo se ele não existir.
* O método `writeCodeFile` sobrescreve o conteúdo de um arquivo existente.
* O método retorna `true` em caso de sucesso e `false` em caso de falha, com logging apropriado.
* O método cria os diretórios necessários no caminho especificado.

#### Sub-tarefa 6.1.2: Criar método `appendCodeToFile(path, content)` para adicionar conteúdo

**Descrição:** Este método será utilizado para adicionar conteúdo ao final de um arquivo existente. Isso é útil quando o agente precisa adicionar um novo método de teste a uma classe de teste já existente, sem sobrescrever todo o seu conteúdo.

**Assinatura do Método:**

```java
public boolean appendCodeToFile(String filePath, String content) {
    // ... implementação ...
}
```

**Passos de Implementação:**

1. **Verificação de Existência:** Verificar se o arquivo `filePath` existe. Se não existir, o método deve se comportar como `writeCodeFile` ou retornar `false` (decisão de design: para este contexto, é mais seguro que `append` falhe se o arquivo não existir, forçando o agente a primeiro criar o arquivo com `writeCodeFile`).
2. **Adição de Conteúdo:** Utilizar `java.nio.file.Files.writeString()` com a opção `StandardOpenOption.APPEND`.
3. **Tratamento de Exceções:** Implementar blocos `try-catch` para `IOException`.

**Exemplo de Código:**

```java
// ... dentro da classe CodeWriterTool

public boolean appendCodeToFile(String filePath, String content) {
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
        System.err.println("Erro: Arquivo " + filePath + " não existe para append.");
        return false; // Ou chamar writeCodeFile se o comportamento desejado for criar
    }
    try {
        Files.writeString(path, content, StandardOpenOption.APPEND);
        System.out.println("Conteúdo adicionado ao arquivo " + filePath + " com sucesso.");
        return true;
    } catch (IOException e) {
        System.err.println("Erro ao adicionar conteúdo ao arquivo " + filePath + ": " + e.getMessage());
        return false;
    }
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `appendCodeToFile` adiciona o `content` ao final de um arquivo existente.
* O método retorna `true` em caso de sucesso e `false` em caso de falha (ex: arquivo não existe), com logging apropriado.
* O método não sobrescreve o conteúdo existente do arquivo.

#### Sub-tarefa 6.1.3: Garantir que o diretório de destino exista (criar se necessário)

**Descrição:** Esta sub-tarefa é uma pré-condição para as operações de escrita e adição de conteúdo. Garante que a estrutura de diretórios necessária para o `filePath` esteja presente antes de qualquer tentativa de I/O no arquivo. Esta lógica já foi incorporada na `writeCodeFile` na sub-tarefa 6.1.1, mas é importante destacá-la como um requisito.

**Passos de Implementação:**

1. **Utilização de `Files.createDirectories()`:** O método `Files.createDirectories(path.getParent())` é a forma recomendada de garantir que todos os diretórios pai de um determinado caminho existam. Ele cria todos os diretórios intermediários se eles não existirem.

**Critérios de Aceitação da Sub-tarefa:**

* Qualquer tentativa de escrita ou adição de conteúdo a um arquivo em um diretório inexistente resulta na criação automática dos diretórios necessários.
* Nenhuma exceção de diretório não encontrado é lançada durante as operações de escrita/append.

#### Sub-tarefa 6.1.4: Lidar com exceções de I/O

**Descrição:** Todas as operações de I/O são suscetíveis a exceções (ex: permissão negada, disco cheio, arquivo corrompido). É crucial que a `CodeWriterTool` trate essas exceções de forma robusta, informando o agente sobre a falha sem interromper o fluxo principal do sistema.

**Passos de Implementação:**

1. **Blocos `try-catch`:** Envolver todas as operações de I/O em blocos `try-catch` para capturar `IOException`.
2. **Logging:** Registrar mensagens de erro detalhadas utilizando um sistema de logging (ex: `java.util.logging` ou SLF4J/Logback) para auxiliar na depuração.
3. **Retorno de Status:** Retornar um valor booleano (`true` para sucesso, `false` para falha) ou um objeto de resultado que contenha o status e uma mensagem de erro, permitindo que o agente tome decisões com base no sucesso ou falha da operação de escrita.

**Critérios de Aceitação da Sub-tarefa:**

* Nenhuma `IOException` não tratada é propagada para fora da `CodeWriterTool`.
* Mensagens de erro claras são logadas quando uma operação de I/O falha.
* Os métodos de escrita/append retornam um status que indica o sucesso ou falha da operação.

## 4. Integração com Google ADK (Conceitual)

Para integrar a `CodeWriterTool` com o Google ADK, ela precisará ser registrada como uma `Tool` que o agente pode invocar. O Google ADK permitirá que o LLM ou o `TestPlanner` chame os métodos `writeCodeFile` ou `appendCodeToFile` com o caminho e o conteúdo do arquivo, e receba o status de sucesso/falha como resultado.

**Exemplo de Registro (Conceitual - a ser detalhado na fase de Integração):**

```java
// Exemplo conceitual de como o Google ADK registraria a ferramenta
// AgentBuilder.forAgent("TestGenerationAgent")
//     .addTool(new CodeWriterTool())
//     .build();
```

## 5. Testes Unitários (para `CodeWriterTool`)

Testes unitários devem ser escritos para a classe `CodeWriterTool` para garantir que cada sub-tarefa funcione conforme o esperado. Casos de teste devem incluir:

* Criação de um novo arquivo em um diretório existente.
* Criação de um novo arquivo em um diretório inexistente (verificar criação de diretórios).
* Sobrescrita de um arquivo existente.
* Adição de conteúdo a um arquivo existente.
* Tentativa de adicionar conteúdo a um arquivo inexistente (verificar comportamento esperado).
* Testes de exceção (simular permissão negada, por exemplo, se possível).
* Verificação do conteúdo do arquivo após as operações de escrita/append.

## 6. Referências

* **Java NIO.2 (Files API):** [https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html)

---

**Autor:** Manus AI
**Data:** 25 de Julho de 2025

---

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

1. **Criação do `ProcessBuilder`:** Instanciar `ProcessBuilder` com o comando completo. O comando Maven geralmente é `mvn` seguido dos argumentos. Ex: `new ProcessBuilder(

mvn", "clean", "install").
2.  **Configuração do Diretório:** Utilizar `directory(new File(projectPath))` para definir o diretório de trabalho do processo.
3.  **Início do Processo:** Chamar `start()` para iniciar a execução do comando.

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

**Exemplo de Registro (Conceitual - a ser detalhado na fase de Integração):**

```java
// Exemplo conceitual de como o Google ADK registraria a ferramenta
// AgentBuilder.forAgent("TestGenerationAgent")
//     .addTool(new BuildAndTestTool())
//     .build();
```

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

# Especificação Técnica Detalhada: US8 - Armazenar e Recuperar Informações de Curto Prazo

**User Story:** US8: Como agente de IA, quero armazenar e recuperar informações de curto prazo para manter o contexto das minhas ações e percepções recentes.

**Épico Relacionado:** Implementar um Sistema Multi-Agentes 100% Autônomo para Geração Automática de Testes Unitários em Projetos Java.

**Fase do Roadmap:** FASE 1: MVP - CORE FUNCIONAL

## 1. Visão Geral

Esta especificação detalha a implementação da funcionalidade de memória de curto prazo, que é um componente crítico da fase de Memória do agente. O objetivo é permitir que o agente mantenha um registro das ações e percepções recentes dentro de uma única sessão de execução. Isso é essencial para que o agente possa tomar decisões informadas com base no contexto imediato, como evitar a repetição de ações ou entender a sequência de eventos que levaram a um determinado estado.

## 2. Critérios de Aceitação da User Story

Para que a US8 seja considerada concluída, o sistema deve atender aos seguintes critérios:

* O agente registra eventos como "arquivo lido", "código analisado", "teste executado".
* A memória de curto prazo tem um limite de tamanho e remove os eventos mais antigos.
* O agente pode consultar os eventos recentes para tomar decisões imediatas.

## 3. Detalhamento Técnico por Tarefa e Sub-tarefa

### Tarefa 8.1: Implementar `MemoryTool` (Memória de Curto Prazo)

**Objetivo:** Criar uma classe de ferramenta (`Tool`) que encapsule a lógica de armazenamento e recuperação de informações de curto prazo, funcionando como um "scratchpad" para o agente durante sua execução.

**Nome da Classe:** `com.example.agent.memory.MemoryTool`

**Dependências:** Nenhuma dependência externa específica.

#### Sub-tarefa 8.1.1: Criar estrutura de dados para memória de curto prazo (ex: `List<String>` com limite)

**Descrição:** A estrutura de dados para a memória de curto prazo deve ser simples e eficiente. Uma `LinkedList` ou uma `ArrayDeque` são boas opções, pois permitem a adição e remoção eficientes de elementos no início e no final, o que é útil para manter uma janela de eventos recentes.

**Passos de Implementação:**

1. **Escolha da Estrutura:** Utilizar uma `java.util.LinkedList<String>` para armazenar os eventos como strings.
2. **Definição do Limite:** Definir uma constante para o tamanho máximo da memória (ex: `private static final int MAX_MEMORY_SIZE = 100;`).

**Exemplo de Código:**

```java
import java.util.LinkedList;
import java.util.List;

public class MemoryTool {

    private static final int MAX_MEMORY_SIZE = 100;
    private LinkedList<String> shortTermMemory = new LinkedList<>();

    // ... métodos ...
}
```

**Critérios de Aceitação da Sub-tarefa:**

* A classe `MemoryTool` contém uma estrutura de dados para armazenar os eventos de curto prazo.
* Um limite de tamanho para a memória é definido.

#### Sub-tarefa 8.1.2: Implementar `addShortTermEvent(event)` para adicionar eventos

**Descrição:** Este método será utilizado pelo agente para registrar eventos na memória de curto prazo. Ele deve adicionar o novo evento e garantir que o tamanho da memória não exceda o limite definido, removendo o evento mais antigo se necessário.

**Assinatura do Método:**

```java
public void addShortTermEvent(String event) {
    // ... implementação ...
}
```

**Passos de Implementação:**

1. **Adição do Evento:** Adicionar o novo `event` ao final da lista (`shortTermMemory.addLast(event)`).
2. **Verificação do Limite:** Verificar se o tamanho da lista excede `MAX_MEMORY_SIZE`. Se sim, remover o primeiro elemento (`shortTermMemory.removeFirst()`).

**Exemplo de Código:**

```java
// ... dentro da classe MemoryTool

public void addShortTermEvent(String event) {
    shortTermMemory.addLast(event);
    if (shortTermMemory.size() > MAX_MEMORY_SIZE) {
        shortTermMemory.removeFirst();
    }
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `addShortTermEvent` adiciona um novo evento à memória.
* O método remove o evento mais antigo quando o limite de tamanho da memória é excedido.

#### Sub-tarefa 8.1.3: Implementar `getShortTermEvents()` para recuperar eventos

**Descrição:** Este método permitirá que o agente consulte o conteúdo atual da memória de curto prazo. Isso é útil para fornecer contexto ao LLM ou para a lógica de planejamento do agente.

**Assinatura do Método:**

```java
public List<String> getShortTermEvents() {
    // ... implementação ...
}
```

**Passos de Implementação:**

1. **Retorno da Lista:** Retornar uma cópia da lista de eventos para evitar modificações externas (`new ArrayList<>(shortTermMemory)`).

**Exemplo de Código:**

```java
import java.util.ArrayList;

// ... dentro da classe MemoryTool

public List<String> getShortTermEvents() {
    return new ArrayList<>(shortTermMemory);
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `getShortTermEvents` retorna uma lista de todos os eventos atualmente na memória de curto prazo.
* A lista retornada é uma cópia, protegendo a integridade da memória interna.

#### Sub-tarefa 8.1.4: Implementar `clearShortTermMemory()`

**Descrição:** Este método permitirá que o agente limpe a memória de curto prazo, o que pode ser útil no início de uma nova sessão de geração de testes ou para resetar o contexto.

**Assinatura do Método:**

```java
public void clearShortTermMemory() {
    // ... implementação ...
}
```

**Passos de Implementação:**

1. **Limpeza da Lista:** Chamar o método `clear()` na lista de eventos (`shortTermMemory.clear()`).

**Exemplo de Código:**

```java
// ... dentro da classe MemoryTool

public void clearShortTermMemory() {
    shortTermMemory.clear();
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `clearShortTermMemory` remove todos os eventos da memória de curto prazo.

## 4. Integração com Google ADK (Conceitual)

Para integrar a `MemoryTool` com o Google ADK, ela precisará ser registrada como uma `Tool` que o agente pode invocar. O Google ADK permitirá que o LLM ou o `TestPlanner` chame os métodos `addShortTermEvent`, `getShortTermEvents` e `clearShortTermMemory` para gerenciar o contexto da sessão.

**Exemplo de Registro (Conceitual - a ser detalhado na fase de Integração):**

```java
// Exemplo conceitual de como o Google ADK registraria a ferramenta
// AgentBuilder.forAgent("TestGenerationAgent")
//     .addTool(new MemoryTool())
//     .build();
```

## 5. Testes Unitários (para `MemoryTool`)

Testes unitários devem ser escritos para a classe `MemoryTool` para garantir que cada sub-tarefa funcione conforme o esperado. Casos de teste devem incluir:

* Adição de eventos à memória.
* Verificação de que o limite de tamanho da memória é respeitado e os eventos mais antigos são removidos.
* Recuperação de eventos da memória.
* Limpeza da memória.
* Verificação de que a lista retornada por `getShortTermEvents` é uma cópia e não a referência original.

## 6. Referências

* **Java LinkedList:** [https://docs.oracle.com/javase/8/docs/api/java/util/LinkedList.html](https://docs.oracle.com/javase/8/docs/api/java/util/LinkedList.html)

---

**Autor:** Manus AI
**Data:** 25 de Julho de 2025

---

# Especificação Técnica Detalhada: US5 - Planejar a Geração de Testes Unitários

**User Story:** US5: Como agente de IA, quero planejar a geração de testes unitários para que eu possa definir uma sequência de ações para criar testes eficazes para os alvos identificados.

**Épico Relacionado:** Implementar um Sistema Multi-Agentes 100% Autônomo para Geração Automática de Testes Unitários em Projetos Java.

**Fase do Roadmap:** FASE 1: MVP - CORE FUNCIONAL

## 1. Visão Geral

Esta especificação detalha a implementação da funcionalidade de planejamento, que é o coração do raciocínio do agente. O objetivo é criar um `TestPlanner` que, com base nas informações coletadas pela fase de Percepção (análise de código, cobertura), gere um plano de ação detalhado para o agente executar. Este plano consistirá em uma sequência de chamadas de ferramentas (`Tool Calling`) para gerar, executar e validar os testes unitários.

## 2. Critérios de Aceitação da User Story

Para que a US5 seja considerada concluída, o sistema deve atender aos seguintes critérios:

* O agente gera um plano que inclui o nome do arquivo de teste, a classe/método a ser testado e o tipo de teste (ex: teste de comportamento, teste de exceção).
* O plano especifica as ferramentas a serem utilizadas (ex: ferramenta de escrita de código, ferramenta de execução de build).
* O plano é adaptável e pode ser ajustado com base no feedback da execução.

## 3. Detalhamento Técnico por Tarefa e Sub-tarefa

### Tarefa 5.1: Implementar `TestPlanner`

**Objetivo:** Criar uma classe que encapsule a lógica de planejamento, recebendo o estado atual do ambiente (código, cobertura) e gerando um plano de ação executável pelo agente.

**Nome da Classe:** `com.example.agent.reasoning.TestPlanner`

**Dependências:** Modelos de dados da fase de Percepção (ex: `ClassAnalysisResult`), `MemoryTool`.

#### Sub-tarefa 5.1.1: Receber um "alvo de teste" (classe/método) e informações de contexto (código-fonte, dependências, cobertura atual)

**Descrição:** O método principal do `TestPlanner` receberá todas as informações necessárias para tomar uma decisão informada sobre como gerar o teste. Isso inclui a classe/método alvo, sua análise estrutural, e o estado atual da cobertura de testes.

**Assinatura do Método:**

```java
public Plan generateTestPlan(TestTarget target, ClassAnalysisResult analysis, CoverageResult coverage) {
    // ... implementação ...
}

// Classes auxiliares para o plano e o alvo
public static class TestTarget {
    public String className;
    public String methodName; // Opcional, para focar em um método específico
}

public static class Plan {
    public List<Action> actions = new ArrayList<>();
}

public static class Action {
    public String toolName;
    public Map<String, String> parameters = new HashMap<>();
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `generateTestPlan` aceita o alvo de teste e os resultados da análise de código e cobertura como parâmetros.
* As estruturas de dados para `TestTarget`, `Plan` e `Action` são definidas para representar o plano de forma clara.

#### Sub-tarefa 5.1.2: Definir estratégias de geração de teste (ex: teste de unidade básico, teste com mocks, teste de exceção)

**Descrição:** O `TestPlanner` deve ser capaz de decidir qual tipo de teste é mais apropriado para o alvo. Inicialmente, isso pode ser baseado em regras simples, mas pode evoluir para uma lógica mais complexa.

**Passos de Implementação:**

1. **Análise do Alvo:** Analisar as características do método alvo (ex: se ele tem dependências externas, se lança exceções, se é um método simples).
2. **Seleção da Estratégia:** Com base na análise, selecionar uma ou mais estratégias de teste:
    * **Teste de Unidade Básico:** Para métodos simples sem dependências externas.
    * **Teste com Mocks:** Para métodos que dependem de outras classes (requer uma biblioteca de mocking como Mockito).
    * **Teste de Exceção:** Para métodos que declaram `throws` em sua assinatura.

**Exemplo de Lógica (dentro de `generateTestPlan`):**

```java
// Lógica simplificada para seleção de estratégia
if (analysis.hasExternalDependencies(target.methodName)) {
    // Planejar teste com mocks
} else if (analysis.throwsException(target.methodName)) {
    // Planejar teste de exceção
} else {
    // Planejar teste de unidade básico
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O `TestPlanner` implementa uma lógica para selecionar diferentes estratégias de geração de teste.
* A seleção da estratégia é baseada na análise do código-fonte do método alvo.

#### Sub-tarefa 5.1.3: Gerar um plano de execução que inclua: Nome do arquivo de teste a ser criado/modificado, Classe/método a ser testado, Tipo de teste a ser gerado, Sequência de chamadas de `Tool Calling`

**Descrição:** Com base na estratégia selecionada, o `TestPlanner` deve gerar uma sequência de ações (`Action`) que o agente executará. Cada ação representa uma chamada a uma das ferramentas (`Tool`) disponíveis.

**Passos de Implementação:**

1. **Geração do Código do Teste (Conceitual):** O `TestPlanner` (ou um componente invocado por ele, como um `TestCodeGenerator`) gerará o código do teste como uma `String`. No MVP, isso pode ser baseado em templates. Em fases futuras, pode ser gerado por um LLM.
2. **Criação das Ações:** Criar uma lista de objetos `Action` que representem o fluxo de trabalho:
    * **Ação 1:** Chamar `WorkspaceManagerTool.setupSandbox()`.
    * **Ação 2:** Chamar `CodeWriterTool.writeCodeFile()` para escrever o código do teste no sandbox.
    * **Ação 3:** Chamar `BuildAndTestTool.executeMavenCommand()` para compilar e executar os testes no sandbox.
    * **Ação 4:** Chamar `CoverageReportReaderTool.readReport()` para analisar a cobertura no sandbox.
    * **Ação 5:** (Condicional) Se a cobertura não for 100%, gerar um novo plano para adicionar testes faltantes (iteração).
    * **Ação 6:** (Final) Se tudo estiver OK, chamar `CodeWriterTool.writeCodeFile()` para escrever o teste no projeto principal do usuário.

**Exemplo de Código (dentro de `generateTestPlan`):**

```java
Plan plan = new Plan();

// Ação para escrever o teste no sandbox
Action writeTestAction = new Action();
writeTestAction.toolName = "CodeWriterTool";
writeTestAction.parameters.put("filePath", "/path/to/sandbox/src/test/java/com/example/MyClassTest.java");
writeTestAction.parameters.put("content", "// ... código do teste gerado ...");
plan.actions.add(writeTestAction);

// Ação para executar os testes no sandbox
Action executeTestAction = new Action();
executeTestAction.toolName = "BuildAndTestTool";
executeTestAction.parameters.put("projectPath", "/path/to/sandbox");
executeTestAction.parameters.put("command", "test");
plan.actions.add(executeTestAction);

// ... outras ações ...

return plan;
```

**Critérios de Aceitação da Sub-tarefa:**

* O `TestPlanner` gera uma lista de ações que representam um fluxo de trabalho completo para a geração e validação de um teste.
* Cada ação especifica a ferramenta a ser chamada e os parâmetros necessários.

#### Sub-tarefa 5.1.4: Retornar o plano como uma estrutura de dados (ex: lista de objetos `Action`)

**Descrição:** O resultado final do `TestPlanner` é o objeto `Plan` contendo a lista de `Action`. Esta estrutura de dados deve ser clara e facilmente interpretável pelo loop de execução do agente.

**Passos de Implementação:**

1. **Retorno do Objeto `Plan`:** O método `generateTestPlan` retorna a instância do objeto `Plan` criada e preenchida nas sub-tarefas anteriores.

**Critérios de Aceitação da Sub-tarefa:**

* O método `generateTestPlan` retorna um objeto `Plan` não nulo.
* A lista de ações dentro do `Plan` reflete a sequência correta de operações para a geração e validação do teste.

#### Sub-tarefa 5.1.5 (CRÍTICA PARA MVP): Implementar lógica no `TestPlanner` para, com base no relatório de cobertura (`CoverageReportReaderTool`), identificar linhas/branches não cobertas e gerar um plano de ação para adicionar testes faltantes (ou modificar testes existentes) para cobrir essas lacunas. Este plano deve ser capaz de ser executado iterativamente no sandbox

**Descrição:** Esta é a sub-tarefa central para a iteração do agente. Após a primeira execução de testes no sandbox, o `TestPlanner` receberá o relatório de cobertura e, se a cobertura não for 100%, ele deve gerar um novo plano para adicionar os testes que faltam.

**Passos de Implementação:**

1. **Análise do Relatório de Cobertura:** O `TestPlanner` recebe o `CoverageResult` e identifica as linhas ou branches não cobertos.
2. **Geração de Novos Testes:** O `TestPlanner` (ou `TestCodeGenerator`) gera o código para os novos testes que visam cobrir as lacunas identificadas.
3. **Geração de um Novo Plano:** O `TestPlanner` gera um novo `Plan` que inclui:
    * Uma ação para **adicionar** o novo código de teste ao arquivo de teste existente no sandbox (usando `CodeWriterTool.appendCodeToFile`).
    * Ações para re-executar os testes e a análise de cobertura.

**Critérios de Aceitação da Sub-tarefa:**

* O `TestPlanner` pode ser invocado com um relatório de cobertura como entrada.
* O `TestPlanner` gera um plano de ação para adicionar testes faltantes com base no relatório de cobertura.
* O plano gerado é projetado para ser executado iterativamente até que a cobertura desejada seja atingida.

## 4. Integração com Google ADK (Conceitual)

Para integrar o `TestPlanner` com o Google ADK, ele não será necessariamente uma `Tool` que o LLM invoca diretamente, mas sim um componente central do raciocínio do agente. O loop principal do agente, orquestrado pelo ADK, invocará o `TestPlanner` para obter o plano de ação. Em fases futuras, o próprio LLM, facilitado pelo ADK, poderá assumir o papel do `TestPlanner`.

## 5. Testes Unitários (para `TestPlanner`)

Testes unitários devem ser escritos para a classe `TestPlanner` para garantir que a lógica de planejamento funcione conforme o esperado. Casos de teste devem incluir:

* Geração de um plano para um método simples sem dependências.
* Geração de um plano para um método com dependências (verificar se a estratégia de mocking é considerada).
* Geração de um plano para um método que lança exceções.
* Geração de um plano de correção com base em um relatório de cobertura que indica lacunas.
* Verificação da estrutura e conteúdo do objeto `Plan` e dos objetos `Action` gerados.

## 6. Referências

* Nenhuma referência externa específica além das classes e ferramentas definidas no próprio projeto.

---

**Autor:** Manus AI
**Data:** 25 de Julho de 2025

---

# Especificação Técnica Detalhada: US10 - Operar de Forma 100% Autônoma

**User Story:** US10: Como sistema, quero operar de forma 100% autônoma para que a geração de testes ocorra sem intervenção humana contínua.

**Épico Relacionado:** Implementar um Sistema Multi-Agentes 100% Autônomo para Geração Automática de Testes Unitários em Projetos Java.

**Fase do Roadmap:** FASE 1: MVP - CORE FUNCIONAL

## 1. Visão Geral

Esta especificação detalha a implementação do loop de execução autônomo, que é o coração do agente e o principal componente da sua autonomia. O objetivo é criar um fluxo de trabalho contínuo que orquestre as chamadas aos módulos de Percepção, Raciocínio, Ação e Memória, permitindo que o agente execute sua tarefa de geração de testes de forma independente, desde a análise inicial até a entrega do resultado final.

## 2. Critérios de Aceitação da User Story

Para que a US10 seja considerada concluída, o sistema deve atender aos seguintes critérios:

* O sistema é acionado automaticamente por eventos (ex: push no repositório) - **(Fase Pós-MVP)**.
* O sistema executa o ciclo completo (percepção, raciocínio, planejamento, ação, memória) sem prompts de usuário.
* O sistema lida com erros e falhas de forma autônoma, tentando se recuperar ou registrando para análise.

## 3. Detalhamento Técnico por Tarefa e Sub-tarefa

### Tarefa 10.1: Implementar loop de execução autônomo

**Objetivo:** Criar a classe principal do agente (`TestGenerationAgent`) que contém o loop de execução autônomo, orquestrando todo o fluxo de trabalho.

**Nome da Classe:** `com.example.agent.TestGenerationAgent`

**Dependências:** `CodeAnalyzerTool`, `CodeWriterTool`, `BuildAndTestTool`, `CoverageReportReaderTool`, `MemoryTool`, `WorkspaceManagerTool`, `TestPlanner`.

#### Sub-tarefa 10.1.1: Criar um loop principal no `TestGenerationAgent` que orquestra as chamadas aos módulos de Percepção, Raciocínio, Ação e Memória, com foco na iteração dentro do sandbox

**Descrição:** O loop principal será o ponto de entrada para a execução do agente. Ele receberá um alvo de teste (a classe a ser testada) e iniciará o ciclo de geração de testes, iterando dentro do sandbox até que os critérios de qualidade sejam atendidos.

**Passos de Implementação:**

1. **Inicialização:** O agente inicializa todas as ferramentas necessárias (`CodeAnalyzerTool`, `CodeWriterTool`, etc.).
2. **Setup do Sandbox:** O agente chama `WorkspaceManagerTool.setupSandbox()` para criar um ambiente de trabalho limpo.
3. **Análise Inicial:** O agente chama `CodeAnalyzerTool.analyzeCode()` para obter a estrutura da classe alvo.
4. **Geração do Plano Inicial:** O agente chama `TestPlanner.generateTestPlan()` com a análise inicial para obter o primeiro plano de ação.
5. **Execução do Plano:** O agente itera sobre as ações do plano, invocando as ferramentas correspondentes (`CodeWriterTool`, `BuildAndTestTool`, etc.).
6. **Loop de Iteração:** O agente entra em um loop de iteração onde:
    * Executa os testes no sandbox.
    * Analisa a cobertura.
    * Se a cobertura não for 100% ou os testes falharem, ele invoca o `TestPlanner` novamente com o novo estado para obter um plano de correção.
    * Executa o novo plano.
    * Repete até que os critérios de sucesso sejam atendidos.
7. **Entrega Final:** Após o sucesso, o agente executa a ação final de escrever o teste no projeto principal do usuário.

**Exemplo de Lógica (Pseudocódigo):**

```java
public class TestGenerationAgent {

    // ... inicialização das ferramentas ...

    public void run(TestTarget target) {
        workspaceManager.setupSandbox(target.projectPath);
        ClassAnalysisResult analysis = codeAnalyzer.analyzeCode(target.classContent);
        CoverageResult coverage = coverageReader.readInitialReport(workspaceManager.getSandboxPath());

        Plan currentPlan = testPlanner.generateTestPlan(target, analysis, coverage);
        boolean isSuccess = false;

        while (!isSuccess) {
            executePlan(currentPlan);
            CommandResult testResult = buildAndTestTool.executeMavenCommand(workspaceManager.getSandboxPath(), "test");
            coverage = coverageReader.readReport(workspaceManager.getSandboxPath());

            if (testResult.success && coverage.is100Percent()) {
                isSuccess = true;
            } else {
                // Obter novo plano de correção
                currentPlan = testPlanner.generateTestPlan(target, analysis, coverage);
            }
        }

        // Entregar teste final
        codeWriter.writeCodeFile(target.finalTestPath, getFinalTestContentFromSandbox());
    }

    private void executePlan(Plan plan) {
        for (Action action : plan.actions) {
            // Invocar a ferramenta correspondente com os parâmetros
        }
    }
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O `TestGenerationAgent` implementa um loop de execução que orquestra as chamadas às ferramentas.
* O loop é capaz de iterar no sandbox, re-invocando o `TestPlanner` para correção.

#### Sub-tarefa 10.1.2: Adicionar um mecanismo de "sleep" ou espera por eventos para evitar consumo excessivo de CPU

**Descrição:** Em um cenário de monitoramento contínuo (pós-MVP), o agente não deve consumir recursos da CPU constantemente. Um mecanismo de espera (sleep) ou, preferencialmente, um modelo baseado em eventos, deve ser implementado.

**Passos de Implementação (para Pós-MVP):**

1. **Modo de Polling:** Em sua forma mais simples, o loop principal pode incluir um `Thread.sleep()` para pausar a execução por um período configurável entre as verificações de alterações no projeto.
2. **Modo Baseado em Eventos:** Uma abordagem mais avançada seria utilizar um sistema de fila de mensagens (ex: RabbitMQ, Kafka) ou um listener de webhook. O agente ficaria em estado de espera até que um evento (ex: novo commit) o acionasse.

**Critérios de Aceitação da Sub-tarefa:**

* (Pós-MVP) O agente não consome 100% da CPU em um loop infinito.
* (Pós-MVP) O agente pode ser configurado para operar em modo de polling ou ser acionado por eventos.

#### Sub-tarefa 10.1.3 (CRÍTICA PARA MVP): Implementar a condição de saída do loop de iteração no sandbox: o agente só finaliza a geração e entrega o teste quando todos os testes passam e 100% de cobertura é atingida para a classe alvo

**Descrição:** Esta é a condição de sucesso que determina quando o agente pode parar de iterar no sandbox e entregar o resultado final. É a garantia de qualidade do MVP.

**Passos de Implementação:**

1. **Verificação do Resultado do Teste:** Após cada execução de `BuildAndTestTool.executeMavenCommand()`, verificar se `testResult.success` é `true`.
2. **Verificação da Cobertura:** Após cada execução de `CoverageReportReaderTool.readReport()`, verificar se o resultado da cobertura para a classe alvo é de 100%.
3. **Condição de Saída:** A condição de saída do loop `while` deve ser `testResult.success && coverage.is100Percent()`.

**Critérios de Aceitação da Sub-tarefa:**

* O loop de iteração do agente tem uma condição de saída clara e correta.
* O agente só sai do loop quando os testes passam e a cobertura de 100% é atingida.

#### Sub-tarefa 10.1.4 (CRÍTICA PARA MVP): Adicionar mecanismo de feedback interno no loop para que o agente possa re-invocar o `TestPlanner` com o novo estado de cobertura para gerar o próximo conjunto de ações de correção

**Descrição:** Este é o mecanismo de feedback que permite ao agente aprender e se corrigir. Após cada iteração malsucedida, o agente deve usar os resultados (testes falhos, cobertura incompleta) como entrada para a próxima rodada de planejamento.

**Passos de Implementação:**

1. **Coleta de Feedback:** Coletar o `CommandResult` e o `CoverageResult` da iteração atual.
2. **Re-invocação do `TestPlanner`:** Chamar `testPlanner.generateTestPlan()` novamente, passando o `TestTarget`, a `ClassAnalysisResult` (que permanece a mesma) e o novo `CoverageResult`.
3. **Atualização do Plano:** O `currentPlan` do agente é atualizado com o novo plano de correção gerado pelo `TestPlanner`.

**Critérios de Aceitação da Sub-tarefa:**

* O agente utiliza os resultados da iteração atual como entrada para a próxima rodada de planejamento.
* O `TestPlanner` é re-invocado dentro do loop de iteração para gerar planos de correção.

## 4. Integração com Google ADK (Conceitual)

O `TestGenerationAgent` e seu loop de execução autônomo são a implementação central do que o Google ADK orquestra. O ADK fornecerá o framework para definir o agente, suas ferramentas e o fluxo de raciocínio (que invoca o `TestPlanner` e executa o plano). O ADK ajudará a estruturar este loop de forma mais robusta e a integrá-lo com o LLM para decisões mais inteligentes.

## 5. Testes Unitários e de Integração (para `TestGenerationAgent`)

Testar o `TestGenerationAgent` é um teste de integração de alto nível. Requer:

* **Mocking de Ferramentas:** Mockar todas as ferramentas (`CodeAnalyzerTool`, `CodeWriterTool`, etc.) para simular diferentes cenários e verificar se o agente orquestra as chamadas corretamente.
* **Teste de Integração de Ponta a Ponta:** Criar um projeto de exemplo completo e executar o agente contra ele, verificando se ele gera um teste final que atende aos critérios de qualidade.
* **Casos de Teste:**
  * Verificar se o agente itera corretamente quando a cobertura não é 100%.
  * Verificar se o agente itera corretamente quando os testes falham.
  * Verificar se o agente sai do loop e entrega o teste quando os critérios de sucesso são atendidos.

## 6. Referências

* Nenhuma referência externa específica além das classes e ferramentas definidas no próprio projeto.

---

**Autor:** Manus AI
**Data:** 25 de Julho de 2025

---

# Especificação Técnica Detalhada: US11 - Comunicação e Colaboração Eficiente de Agentes

**User Story:** US11: Como sistema, quero que meus agentes se comuniquem e colaborem eficientemente para garantir um fluxo de trabalho coeso e escalável.

**Épico Relacionado:** Implementar um Sistema Multi-Agentes 100% Autônomo para Geração Automática de Testes Unitários em Projetos Java.

**Fase do Roadmap:** FASE 1: MVP - CORE FUNCIONAL

## 1. Visão Geral

Esta especificação detalha a implementação das interfaces de comunicação entre os diferentes componentes (módulos/agentes) do sistema e a integração conceitual com o Google ADK. O objetivo é garantir que os dados fluam de forma consistente e que o sistema seja projetado de forma modular e escalável desde o início.

## 2. Critérios de Aceitação da User Story

Para que a US11 seja considerada concluída, o sistema deve atender aos seguintes critérios:

* Os agentes trocam informações e eventos de forma assíncrona (ex: via fila de mensagens) - **(Fase Pós-MVP)**.
* As interfaces entre os agentes e as ferramentas são bem definidas (APIs, contratos de dados).
* O Google ADK é utilizado para orquestrar as chamadas de ferramentas e o raciocínio dos agentes.

## 3. Detalhamento Técnico por Tarefa e Sub-tarefa

### Tarefa 11.1: Definir interfaces de comunicação entre módulos/agentes

**Objetivo:** Padronizar os formatos de dados de entrada e saída para cada ferramenta e módulo, garantindo uma comunicação consistente e desacoplada.

#### Sub-tarefa 11.1.1: Padronizar formatos de dados de entrada/saída para cada ferramenta/módulo (ex: JSON)

**Descrição:** Para garantir que os diferentes componentes do sistema possam se comunicar de forma eficaz, é essencial definir contratos de dados claros. O formato JSON é ideal para isso, pois é leve, legível por humanos e facilmente serializável/desserializável em Java.

**Passos de Implementação:**

1. **Definição de POJOs:** Para cada estrutura de dados complexa (ex: `ClassAnalysisResult`, `CoverageResult`, `CommandResult`, `Plan`), definir uma classe Java (POJO) com os campos correspondentes. Isso já foi iniciado nas especificações técnicas anteriores.
2. **Utilização de Jackson/Gson:** Utilizar uma biblioteca de serialização JSON (como Jackson) para converter os objetos Java em `String` JSON e vice-versa. Isso será especialmente útil na saída das ferramentas, que podem retornar uma `String` JSON para o agente.
3. **Documentação dos Contratos:** Documentar a estrutura JSON esperada para a entrada e saída de cada ferramenta. Isso será crucial para a depuração e para a integração com o LLM.

**Exemplo de Contrato de Dados (para `CodeAnalyzerTool`):**

```json
{
  "className": "MyClass",
  "methods": [
    {
      "name": "myMethod",
      "visibility": "public",
      "returnType": "void",
      "signature": "public void myMethod(String arg1)",
      "parameters": ["String arg1"]
    }
  ],
  "calledMethods": ["someOtherMethod"]
}
```

**Critérios de Aceitação da Sub-tarefa:**

* Todas as ferramentas que retornam dados complexos o fazem em um formato JSON padronizado.
* Classes POJO são definidas para representar os contratos de dados, melhorando a manutenibilidade.
* A documentação do projeto inclui a especificação dos formatos JSON utilizados.

### Tarefa 11.2: Integrar com Google ADK

**Objetivo:** Mapear a arquitetura do nosso sistema para os conceitos do Google ADK, garantindo que o framework seja utilizado como a base para a orquestração do agente.

#### Sub-tarefa 11.2.1: Entender como registrar as ferramentas Java (`CodeReaderTool`, `CodeAnalyzerTool`, etc.) como `Tools` no Google ADK

**Descrição:** O Google ADK permite que funções Java sejam registradas como `Tools` que o agente pode invocar. É necessário entender o mecanismo de registro para expor nossas ferramentas ao agente.

**Passos de Implementação (Conceitual):**

1. **Anotações do ADK:** O ADK provavelmente utiliza anotações (ex: `@Tool`) para marcar métodos que devem ser expostos como ferramentas. É necessário pesquisar a documentação do ADK para identificar as anotações corretas.
2. **Registro das Ferramentas:** O `AgentBuilder` do ADK será utilizado para registrar instâncias das nossas classes de ferramentas (`CodeAnalyzerTool`, `CodeWriterTool`, etc.) com o agente.

**Exemplo de Código (Conceitual):**

```java
import com.google.adk.annotations.Tool;

public class CodeAnalyzerTool {

    @Tool(description = "Analisa o código-fonte de um arquivo Java e retorna sua estrutura em JSON.")
    public String analyzeCode(String fileContent) {
        // ... implementação ...
    }
}

// ... no TestGenerationAgent

Agent agent = AgentBuilder.forAgent("TestGenerationAgent")
                         .addTool(new CodeAnalyzerTool())
                         .addTool(new CodeWriterTool())
                         // ... registrar todas as outras ferramentas
                         .build();
```

**Critérios de Aceitação da Sub-tarefa:**

* Um entendimento claro de como registrar as ferramentas Java no Google ADK é alcançado.
* O código do agente é estruturado para facilitar o registro das ferramentas.

#### Sub-tarefa 11.2.2: Mapear o fluxo de Percepção-Raciocínio-Ação-Memória para a orquestração do LLM no ADK

**Descrição:** O fluxo de trabalho que definimos (percepção -> raciocínio -> planejamento -> ação -> feedback) precisa ser mapeado para a forma como o Google ADK orquestra as interações com o LLM. O ADK gerencia o envio do prompt (incluindo o histórico da conversa e as ferramentas disponíveis) para o LLM e interpreta a resposta (que pode ser uma mensagem de texto ou uma chamada de ferramenta).

**Passos de Implementação (Conceitual):**

1. **Construção do Prompt:** O loop principal do agente será responsável por construir o prompt para o LLM em cada etapa. O prompt incluirá:
    * O objetivo principal (ex: "Gere um teste unitário para esta classe com 100% de cobertura").
    * O contexto atual (o código da classe, o relatório de cobertura, o histórico de ações recentes da memória de curto prazo).
    * A lista de ferramentas disponíveis (fornecida pelo ADK).
2. **Invocação do Agente:** O agente do ADK será invocado com este prompt.
3. **Processamento da Resposta:** O ADK retornará a resposta do LLM. Se for uma chamada de ferramenta, o ADK a executará e retornará o resultado. O loop principal do nosso agente processará este resultado e construirá o próximo prompt para a iteração seguinte.

**Critérios de Aceitação da Sub-tarefa:**

* Um mapeamento claro entre o nosso fluxo de trabalho e a orquestração do ADK é definido.
* A lógica para construir os prompts e processar as respostas do agente do ADK é implementada no loop principal.

#### Sub-tarefa 11.2.3: (Opcional) Explorar a criação de múltiplos agentes no ADK para cada fase (ex: Agente de Percepção, Agente de Planejamento, Agente Executor)

**Descrição:** Para uma arquitetura mais avançada (pós-MVP), o ADK permite a criação de múltiplos agentes que podem se comunicar entre si. Poderíamos ter um agente principal (`OrchestratorAgent`) que delega tarefas a agentes especializados.

**Passos de Implementação (Pós-MVP):**

1. **Definição de Agentes Especializados:** Definir um `PerceptionAgent` (responsável por analisar código e cobertura), um `PlanningAgent` (responsável por gerar o plano) e um `ExecutionAgent` (responsável por executar as ações no sandbox).
2. **Comunicação entre Agentes:** Utilizar os mecanismos de comunicação entre agentes do ADK para que o `OrchestratorAgent` possa enviar tarefas e receber resultados dos agentes especializados.

**Critérios de Aceitação da Sub-tarefa:**

* (Pós-MVP) A arquitetura do sistema é refatorada para utilizar múltiplos agentes especializados.
* (Pós-MVP) A comunicação entre os agentes é implementada utilizando os recursos do ADK.

## 4. Testes Unitários e de Integração

Testar a integração com o ADK envolverá:

* **Testes de Integração:** Verificar se as ferramentas são registradas corretamente e podem ser invocadas pelo agente do ADK.
* **Testes de Ponta a Ponta:** Executar o agente completo e verificar se o fluxo de trabalho é orquestrado corretamente pelo ADK.
* **Mocking do LLM:** Para testes isolados, mockar a resposta do LLM para simular diferentes cenários (ex: o LLM decide chamar uma ferramenta específica).

## 5. Referências

* **Documentação do Google Agent Development Kit (ADK):** (URL a ser pesquisada e adicionada)

---

**Autor:** Manus AI
**Data:** 25 de Julho de 2025

# Especificação Técnica Detalhada: US3 - Analisar Cobertura de Código

**User Story:** US3: Como desenvolvedor, quero que o sistema analise a cobertura de código para que eu possa garantir que os testes unitários gerados cubram todas as linhas e branches do código-fonte.

**Épico Relacionado:** Implementar um Sistema Multi-Agentes 100% Autônomo para Geração Automática de Testes Unitários em Projetos Java.

**Fase do Roadmap:** FASE 1: MVP - CORE FUNCIONAL

## 1. Visão Geral

Esta especificação detalha a implementação da funcionalidade de análise de cobertura de código, que é um componente crítico da fase de Percepção e do ciclo de feedback do agente. O objetivo é permitir que o agente execute os testes gerados, colete relatórios de cobertura (utilizando JaCoCo) e interprete esses relatórios para determinar se a classe alvo está 100% coberta por testes. Esta capacidade é fundamental para a iteração do agente no sandbox, garantindo a qualidade dos testes antes da entrega.

## 2. Critérios de Aceitação da User Story

Para que a US3 seja considerada concluída, o sistema deve atender aos seguintes critérios:

* O sistema configura o JaCoCo para gerar relatórios de cobertura.
* O sistema lê e interpreta relatórios de cobertura gerados pelo JaCoCo.
* O sistema identifica a porcentagem de cobertura de linha e branch para a classe alvo.
* O sistema pode identificar linhas/branches não cobertas.

## 3. Detalhamento Técnico por Tarefa e Sub-tarefa

### Tarefa 3.1: Configurar geração de relatórios de cobertura JaCoCo

**Objetivo:** Integrar o plugin JaCoCo ao projeto Maven/Gradle do sandbox do agente para que ele possa gerar relatórios de cobertura durante a execução dos testes.

#### Sub-tarefa 3.1.1: Adicionar plugin JaCoCo ao `pom.xml` (Maven) ou `build.gradle` (Gradle) do projeto do sandbox

**Descrição:** O JaCoCo (Java Code Coverage) é uma ferramenta padrão da indústria para medir a cobertura de código. Ele será configurado no projeto Maven/Gradle do sandbox para gerar relatórios em formato XML, que serão posteriormente lidos e interpretados pelo agente.

**Passos de Implementação:**

1. **Identificação do Plugin:** A versão mais recente e estável do plugin JaCoCo Maven (<https://mvnrepository.com/artifact/org.jacoco/jacoco-maven-plugin>) ou Gradle (<https://docs.gradle.org/current/userguide/jacoco_plugin.html>) deve ser identificada.
2. **Adição ao `pom.xml` (Maven):** No arquivo `pom.xml` do projeto Maven do sandbox, o plugin JaCoCo deve ser adicionado dentro da tag `<build><plugins>`:

    ```xml
    <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.11</version> <!-- Usar a versão mais recente e estável -->
        <executions>
            <execution>
                <goals>
                    <goal>prepare-agent</goal>
                </goals>
            </execution>
            <execution>
                <id>report</id>
                <phase>test</phase>
                <goals>
                    <goal>report</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    ```

    *Nota:* A versão `0.8.11` é um exemplo; verificar a versão mais recente no momento da implementação.

3. **Adição ao `build.gradle` (Gradle - se aplicável):** Se o projeto do sandbox utilizar Gradle, o plugin equivalente seria adicionado no arquivo `build.gradle`:

    ```gradle
    plugins {
        id 'jacoco'
    }

    jacoco {
        toolVersion = 



'0.8.11' // Usar a versão mais recente e estável
    }

    test {
        finalizedBy jacocoTestReport
    }

    jacocoTestReport {
        dependsOn test
        reports {
            xml.required = true
            html.required = false
        }
    }
    ```

4.  **Verificação da Configuração:** Após adicionar o plugin, executar um `mvn clean install` (Maven) ou `gradle build` (Gradle) no projeto do sandbox e verificar se o diretório `target/site/jacoco` (Maven) ou `build/reports/jacoco` (Gradle) é criado com o arquivo `jacoco.xml` após a execução dos testes.

**Critérios de Aceitação da Sub-tarefa:**

* O plugin JaCoCo é configurado corretamente no projeto do sandbox.
* Um relatório `jacoco.xml` é gerado no diretório esperado após a execução dos testes.

### Tarefa 3.2: Implementar `CoverageReportReaderTool`

**Objetivo:** Criar uma classe de ferramenta (`Tool`) que seja capaz de ler e interpretar o relatório `jacoco.xml`, extraindo informações relevantes sobre a cobertura de código.

**Nome da Classe:** `com.example.agent.perception.CoverageReportReaderTool`

**Dependências:** Biblioteca para parsing de XML (ex: `javax.xml.parsers` ou `org.w3c.dom` do Java padrão, ou uma biblioteca externa como JDOM/DOM4J se necessário para maior facilidade).

#### Sub-tarefa 3.2.1: Criar método `readReport(reportPath)` para ler o arquivo `jacoco.xml`

**Descrição:** Este método receberá o caminho para o arquivo `jacoco.xml` gerado pelo JaCoCo e será responsável por carregar o conteúdo XML para processamento.

**Assinatura do Método:**

```java
public CoverageResult readReport(String reportPath) {
    // ... implementação ...
}

// Classe auxiliar para o resultado da cobertura
public static class CoverageResult {
    public String className;
    public double lineCoveragePercentage;
    public double branchCoveragePercentage;
    public List<String> uncoveredLines;
    public List<String> uncoveredBranches;
    public boolean is100PercentCovered;
}
```

**Passos de Implementação:**

1. **Leitura do Arquivo:** Utilizar `java.nio.file.Files.readAllBytes()` para ler o conteúdo do arquivo XML.
2. **Parsing XML:** Utilizar `DocumentBuilderFactory` e `DocumentBuilder` para parsear o XML e obter um objeto `Document` (DOM).
3. **Tratamento de Exceções:** Implementar blocos `try-catch` para `IOException` e `ParserConfigurationException`/`SAXException`.

**Exemplo de Código (Conceitual):**

```java
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import java.io.File;

public class CoverageReportReaderTool {

    public CoverageResult readReport(String reportPath) {
        try {
            File inputFile = new File(reportPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            // ... continuar com a extração de dados ...
            return new CoverageResult(); // Placeholder
        } catch (Exception e) {
            System.err.println("Erro ao ler ou parsear o relatório JaCoCo: " + e.getMessage());
            return null; // Ou um objeto CoverageResult indicando erro
        }
    }
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `readReport` aceita o caminho para o arquivo `jacoco.xml`.
* O método carrega e parseia o conteúdo XML, retornando um objeto `Document` (ou similar).
* O tratamento de exceções para erros de I/O e parsing XML é implementado.

#### Sub-tarefa 3.2.2: Extrair porcentagens de cobertura de linha e branch para a classe alvo

**Descrição:** O relatório `jacoco.xml` contém métricas detalhadas de cobertura. O agente precisa extrair as porcentagens de cobertura de linha e branch para a classe específica que está sendo testada.

**Passos de Implementação:**

1. **Navegação no DOM:** Navegar na estrutura DOM do `Document` para encontrar os elementos `<package>`, `<class>` e `<counter>` relevantes.
2. **Identificação da Classe Alvo:** Localizar o elemento `<class>` correspondente à classe Java que está sendo analisada.
3. **Extração de Contadores:** Dentro do elemento `<class>`, encontrar os elementos `<counter>` com `type=

"LINE" e "BRANCH" e extrair seus atributos `missed` e `covered`.
4.  **Cálculo da Porcentagem:** Calcular a porcentagem de cobertura usando a fórmula `covered / (missed + covered) * 100`.

**Exemplo de Código (Conceitual):**

```java
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// ... dentro do readReport method, após obter doc

CoverageResult result = new CoverageResult();
result.className = "MyClass"; // Deve ser passado como parâmetro ou inferido

NodeList classNodes = doc.getElementsByTagName("class");
for (int i = 0; i < classNodes.getLength(); i++) {
    Element classElement = (Element) classNodes.item(i);
    if (classElement.getAttribute("name").equals(result.className.replace(".", "/"))) { // JaCoCo usa / para pacotes
        NodeList counterNodes = classElement.getElementsByTagName("counter");
        for (int j = 0; j < counterNodes.getLength(); j++) {
            Element counterElement = (Element) counterNodes.item(j);
            String type = counterElement.getAttribute("type");
            int missed = Integer.parseInt(counterElement.getAttribute("missed"));
            int covered = Integer.parseInt(counterElement.getAttribute("covered"));

            if (type.equals("LINE")) {
                result.lineCoveragePercentage = (double) covered / (missed + covered) * 100;
            } else if (type.equals("BRANCH")) {
                result.branchCoveragePercentage = (double) covered / (missed + covered) * 100;
            }
        }
        break; // Classe encontrada, pode sair do loop
    }
}

result.is100PercentCovered = (result.lineCoveragePercentage == 100.0 && result.branchCoveragePercentage == 100.0);

return result;
```

**Critérios de Aceitação da Sub-tarefa:**

* O método extrai corretamente as porcentagens de cobertura de linha e branch para a classe alvo.
* O cálculo da porcentagem é preciso.
* O campo `is100PercentCovered` é definido corretamente.

#### Sub-tarefa 3.2.3: Identificar linhas/branches não cobertas (para feedback ao `TestPlanner`)

**Descrição:** Além das porcentagens, é crucial que o agente saiba exatamente quais linhas ou branches não foram cobertas. Essa informação será usada pelo `TestPlanner` para gerar testes que cubram essas lacunas.

**Passos de Implementação:**

1. **Navegação nos Elementos `line`:** O relatório `jacoco.xml` contém elementos `<line>` dentro de `<method>` que indicam o status de cobertura de cada linha. Para branches, os atributos `mb` (missed branches) e `cb` (covered branches) são relevantes.
2. **Coleta de Linhas Não Cobertas:** Iterar sobre esses elementos e coletar os números das linhas que possuem `ci=

"0" (missed instructions) ou `mb="1"` (missed branches).

**Exemplo de Código (Conceitual):**

```java
// ... dentro do readReport method, após obter doc

List<String> uncoveredLines = new ArrayList<>();
List<String> uncoveredBranches = new ArrayList<>();

NodeList methodNodes = doc.getElementsByTagName("method");
for (int i = 0; i < methodNodes.getLength(); i++) {
    Element methodElement = (Element) methodNodes.item(i);
    // Filtrar por métodos da classe alvo, se necessário

    NodeList lineNodes = methodElement.getElementsByTagName("line");
    for (int j = 0; j < lineNodes.getLength(); j++) {
        Element lineElement = (Element) lineNodes.item(j);
        int lineNumber = Integer.parseInt(lineElement.getAttribute("nr"));
        int missedInstructions = Integer.parseInt(lineElement.getAttribute("ci")); // Covered Instructions
        int totalInstructions = Integer.parseInt(lineElement.getAttribute("mi")); // Missed Instructions
        int missedBranches = Integer.parseInt(lineElement.getAttribute("mb"));
        int coveredBranches = Integer.parseInt(lineElement.getAttribute("cb"));

        if (missedInstructions > 0) {
            uncoveredLines.add("Linha " + lineNumber + ": " + missedInstructions + " instruções não cobertas.");
        }
        if (missedBranches > 0) {
            uncoveredBranches.add("Linha " + lineNumber + ": " + missedBranches + " branches não cobertos.");
        }
    }
}

result.uncoveredLines = uncoveredLines;
result.uncoveredBranches = uncoveredBranches;

return result;
```

**Critérios de Aceitação da Sub-tarefa:**

* O método identifica e lista as linhas e branches que não foram cobertas pelos testes.
* A informação é granular o suficiente para ser utilizada pelo `TestPlanner` para gerar testes de correção.

#### Sub-tarefa 3.2.4: Lidar com arquivos `jacoco.xml` vazios ou inválidos

**Descrição:** A ferramenta deve ser robusta o suficiente para lidar com cenários onde o arquivo `jacoco.xml` não existe, está vazio ou contém XML inválido. Nesses casos, a ferramenta deve retornar um `CoverageResult` que indique a falha e/ou logar o erro, sem interromper o fluxo do agente.

**Passos de Implementação:**

1. **Verificação de Existência:** Antes de tentar ler o arquivo, verificar se ele existe (`Files.exists(Paths.get(reportPath))`).
2. **Tratamento de XML Inválido:** O `DocumentBuilder.parse()` já lança `SAXException` para XML malformado. Capturar essa exceção.
3. **Retorno de Objeto de Erro:** Em caso de erro, retornar um `CoverageResult` com valores padrão (ex: 0% de cobertura) e uma flag de erro, ou `null`, dependendo da estratégia de tratamento de erros do agente.

**Critérios de Aceitação da Sub-tarefa:**

* O método `readReport` não lança exceções não tratadas para arquivos `jacoco.xml` inexistentes, vazios ou inválidos.
* O método retorna um `CoverageResult` que reflete o estado de erro ou uma cobertura de 0% nesses cenários.

## 4. Integração com Google ADK (Conceitual)

Para integrar a `CoverageReportReaderTool` com o Google ADK, ela precisará ser registrada como uma `Tool` que o agente pode invocar. O Google ADK permitirá que o LLM ou o `TestPlanner` chame o método `readReport` com o caminho para o relatório `jacoco.xml` e receba o `CoverageResult` para avaliar a qualidade dos testes gerados.

**Exemplo de Registro (Conceitual - a ser detalhado na fase de Integração):**

```java
// Exemplo conceitual de como o Google ADK registraria a ferramenta
// AgentBuilder.forAgent("TestGenerationAgent")
//     .addTool(new CoverageReportReaderTool())
//     .build();
```

## 5. Testes Unitários (para `CoverageReportReaderTool`)

Testes unitários devem ser escritos para a classe `CoverageReportReaderTool` para garantir que cada sub-tarefa funcione conforme o esperado. Casos de teste devem incluir:

* Leitura e parsing de um arquivo `jacoco.xml` válido com diferentes cenários de cobertura (100%, parcial, 0%).
* Extração correta das porcentagens de cobertura de linha e branch.
* Identificação precisa das linhas e branches não cobertas.
* Tratamento de arquivos `jacoco.xml` vazios, inexistentes ou malformados.
* Verificação da estrutura e conteúdo do objeto `CoverageResult` retornado.

## 6. Referências

* **JaCoCo:** [https://www.jacoco.org/jacoco/](https://www.jacoco.org/jacoco/)
* **JaCoCo XML Report Structure:** [https://www.jacoco.org/jacoco/trunk/coverage/report.html](https://www.jacoco.org/jacoco/trunk/coverage/report.html)
* **Java XML Parsing (DOM):** [https://docs.oracle.com/javase/tutorial/jaxp/dom/index.html](https://docs.oracle.com/javase/tutorial/jaxp/dom/index.html)

---

**Autor:** Manus AI
**Data:** 25 de Julho de 2025

---

# Especificação Técnica Detalhada: US9 - Gerenciar Workspace/Sandbox

**User Story:** US9: Como agente de IA, quero gerenciar um workspace/sandbox para que eu possa criar um ambiente isolado para gerar, compilar e executar testes unitários sem afetar o projeto original.

**Épico Relacionado:** Implementar um Sistema Multi-Agentes 100% Autônomo para Geração Automática de Testes Unitários em Projetos Java.

**Fase do Roadmap:** FASE 1: MVP - CORE FUNCIONAL

## 1. Visão Geral

Esta especificação detalha a implementação da funcionalidade de gerenciamento de workspace/sandbox, que é um componente crítico para a autonomia e segurança do agente. O objetivo é permitir que o agente crie um ambiente de trabalho isolado (uma cópia do projeto do usuário ou uma estrutura mínima) onde ele possa gerar, compilar e executar testes unitários de forma iterativa, sem impactar o projeto original. Este sandbox é essencial para o ciclo de feedback e correção do agente, garantindo que apenas testes funcionais e com cobertura total sejam

entregues ao projeto original.

## 2. Critérios de Aceitação da User Story

Para que a US9 seja considerada concluída, o sistema deve atender aos seguintes critérios:

* O agente cria um diretório de sandbox isolado.
* O agente copia os arquivos necessários do projeto original para o sandbox.
* O agente pode limpar o conteúdo do sandbox para uma nova iteração.
* O agente pode obter o caminho para o diretório do sandbox.

## 3. Detalhamento Técnico por Tarefa e Sub-tarefa

### Tarefa 9.1: Implementar `WorkspaceManagerTool`

**Objetivo:** Criar uma classe de ferramenta (`Tool`) que encapsule a lógica de criação, gerenciamento e limpeza do ambiente de sandbox.

**Nome da Classe:** `com.example.agent.action.WorkspaceManagerTool`

**Dependências:** Nenhuma dependência externa específica além das APIs de I/O do Java.

#### Sub-tarefa 9.1.1: Criar método `setupSandbox(originalProjectPath, targetClassName)` para inicializar o ambiente

**Descrição:** Este método será responsável por criar um diretório de sandbox temporário e copiar os arquivos essenciais do projeto original para este ambiente. No MVP, o foco é em uma única classe, então a cópia pode ser mais seletiva.

**Assinatura do Método:**

```java
public String setupSandbox(String originalProjectPath, String targetClassName) {
    // ... implementação ...
}
```

**Passos de Implementação:**

1. **Definição do Caminho do Sandbox:** Criar um diretório temporário ou um diretório com um nome previsível (ex: `temp/agent-sandbox-<timestamp>`).
2. **Criação do Diretório:** Utilizar `Files.createDirectories()` para criar o diretório do sandbox.
3. **Cópia de Arquivos Essenciais:**
    * Copiar o arquivo `.java` da `targetClassName` do `originalProjectPath` para o sandbox.
    * Copiar o `pom.xml` (Maven) ou `build.gradle` (Gradle) do projeto original para o sandbox, pois ele contém as dependências necessárias para compilação e execução de testes.
    * Copar quaisquer outras classes ou recursos que a `targetClassName` dependa diretamente (para o MVP, podemos começar com dependências mínimas e expandir conforme necessário).
4. **Retorno do Caminho:** Retornar o caminho absoluto para o diretório raiz do sandbox.

**Exemplo de Código (Conceitual):**

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class WorkspaceManagerTool {

    private String sandboxPath;

    public String setupSandbox(String originalProjectPath, String targetClassName) {
        try {
            Path originalRoot = Paths.get(originalProjectPath);
            Path tempDir = Files.createTempDirectory("agent-sandbox-");
            this.sandboxPath = tempDir.toAbsolutePath().toString();

            // Exemplo: Copiar pom.xml
            Files.copy(originalRoot.resolve("pom.xml"), tempDir.resolve("pom.xml"), StandardCopyOption.REPLACE_EXISTING);

            // Exemplo: Copiar a classe alvo (assumindo estrutura src/main/java)
            Path originalClassPath = originalRoot.resolve("src/main/java/")
                                                .resolve(targetClassName.replace(".", File.separator) + ".java");
            Path sandboxClassDir = tempDir.resolve("src/main/java/")
                                        .resolve(targetClassName.substring(0, targetClassName.lastIndexOf(".")).replace(".", File.separator));
            Files.createDirectories(sandboxClassDir);
            Files.copy(originalClassPath, sandboxClassDir.resolve(targetClassName.substring(targetClassName.lastIndexOf(".") + 1) + ".java"), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Sandbox criado em: " + sandboxPath);
            return sandboxPath;
        } catch (IOException e) {
            System.err.println("Erro ao configurar o sandbox: " + e.getMessage());
            return null;
        }
    }

    public String getSandboxPath() {
        return sandboxPath;
    }

    // ... outros métodos ...
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `setupSandbox` cria um diretório de sandbox único e isolado.
* O `pom.xml` (ou `build.gradle`) e o arquivo `.java` da classe alvo são copiados para o sandbox.
* O método retorna o caminho absoluto para o diretório raiz do sandbox.

#### Sub-tarefa 9.1.2: Implementar `cleanSandbox()` para remover o ambiente

**Descrição:** Após a conclusão da geração de testes para uma classe (seja com sucesso ou falha irrecuperável), o ambiente de sandbox deve ser limpo para liberar recursos e garantir que não haja resíduos de execuções anteriores. Isso é crucial para a próxima iteração do agente.

**Assinatura do Método:**

```java
public boolean cleanSandbox() {
    // ... implementação ...
}
```

**Passos de Implementação:**

1. **Verificação de Existência:** Verificar se o `sandboxPath` existe e é um diretório.
2. **Exclusão Recursiva:** Utilizar `Files.walk()` e `Files.delete()` para excluir recursivamente todos os arquivos e subdiretórios dentro do sandbox.
3. **Tratamento de Exceções:** Implementar blocos `try-catch` para `IOException`.

**Exemplo de Código:**

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

// ... dentro da classe WorkspaceManagerTool

public boolean cleanSandbox() {
    if (sandboxPath == null) {
        System.out.println("Nenhum sandbox para limpar.");
        return true;
    }
    Path path = Paths.get(sandboxPath);
    if (!Files.exists(path)) {
        System.out.println("Sandbox já limpo ou não existe: " + sandboxPath);
        return true;
    }
    try {
        Files.walk(path)
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);
        System.out.println("Sandbox " + sandboxPath + " limpo com sucesso.");
        sandboxPath = null; // Resetar o caminho após a limpeza
        return true;
    } catch (IOException e) {
        System.err.println("Erro ao limpar o sandbox " + sandboxPath + ": " + e.getMessage());
        return false;
    }
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método `cleanSandbox` remove todos os arquivos e diretórios dentro do sandbox.
* O método retorna `true` em caso de sucesso e `false` em caso de falha, com logging apropriado.

#### Sub-tarefa 9.1.3: Implementar `getSandboxPath()` para obter o caminho do sandbox atual

**Descrição:** Este método permitirá que outros componentes do agente (como `BuildAndTestTool` ou `CodeWriterTool`) obtenham o caminho para o diretório de trabalho atual do sandbox.

**Assinatura do Método:**

```java
public String getSandboxPath() {
    // ... implementação ...
}
```

**Passos de Implementação:**

1. **Retorno da Variável:** Simplesmente retornar a variável de instância que armazena o caminho do sandbox.

**Critérios de Aceitação da Sub-tarefa:**

* O método `getSandboxPath` retorna o caminho absoluto para o diretório raiz do sandbox.
* Retorna `null` ou uma string vazia se nenhum sandbox estiver ativo.

## 4. Integração com Google ADK (Conceitual)

Para integrar a `WorkspaceManagerTool` com o Google ADK, ela precisará ser registrada como uma `Tool` que o agente pode invocar. O Google ADK permitirá que o LLM ou o `TestPlanner` chame os métodos `setupSandbox` e `cleanSandbox` para gerenciar o ambiente de trabalho do agente.

**Exemplo de Registro (Conceitual - a ser detalhado na fase de Integração):**

```java
// Exemplo conceitual de como o Google ADK registraria a ferramenta
// AgentBuilder.forAgent("TestGenerationAgent")
//     .addTool(new WorkspaceManagerTool())
//     .build();
```

## 5. Testes Unitários (para `WorkspaceManagerTool`)

Testes unitários devem ser escritos para a classe `WorkspaceManagerTool` para garantir que cada sub-tarefa funcione conforme o esperado. Casos de teste devem incluir:

* Criação de um sandbox com sucesso, verificando a existência do diretório e dos arquivos copiados.
* Limpeza de um sandbox com sucesso, verificando a remoção de todos os arquivos.
* Tentativa de limpar um sandbox inexistente.
* Verificação do caminho retornado por `getSandboxPath`.
* Testes de exceção (simular permissão negada, por exemplo, se possível).

## 6. Referências

* **Java NIO.2 (Files API):** [https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html)

---

**Autor:** Manus AI
**Data:** 25 de Julho de 2025
