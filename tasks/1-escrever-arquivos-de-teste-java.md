# US6 - Escrever Arquivos de Teste Java

**User Story:** US6: Como agente de IA, quero escrever arquivos de teste Java para que eu possa persistir os testes unitários gerados no projeto.

## 1. Visão Geral

Esta especificação detalha a implementação da funcionalidade de escrita de arquivos de teste Java, que é um componente crítico da fase de Ação/Tool Calling do agente. O objetivo é permitir que o agente crie novos arquivos .java ou modifique arquivos existentes no diretório de testes do projeto, persistindo o código dos testes unitários gerados.

## 2. Critérios de Aceitação da User Story

Para que a US seja considerada concluída, o sistema deve atender aos seguintes
critérios:

- O agente cria novos arquivos .java no diretório de testes.
- O agente sobrescreve o conteúdo de arquivos de teste existentes quando necessário.
- O agente pode anexar novos métodos de teste a arquivos existentes.

## 3. Detalhamento Técnico

### Tarefa 6.1 : Implementar `CodeWriterTool`

Objetivo: Criar uma classe de ferramenta (`Tool`) que encapsule a lógica de escrita de arquivos de código Java, permitindo a criação, sobrescrita e adição de conteúdo a arquivos.

Nome da Classe: `com.example.agent.action.CodeWriterTool`

Dependências: Nenhuma dependência externa específica além das APIs de I/O do Java.

#### Sub-tarefa 6.1.1: Criar método `writeCodeFile(path, content)` para criar/sobrescrever arquivos

**Descrição:** Este método será responsável por criar um novo arquivo no caminho especificado ou sobrescrever um arquivo existente com o conteúdo fornecido. É a funcionalidade primária para persistir um teste unitário recém-gerado ou uma versão corrigida.

**Assinatura do Método:**

```java
public boolean writeCodeFile(String filePath, String content) {
  // Implementação
}
```

**Passos de Implementação**:

1. **Criação de Diretórios:** Antes de escrever o arquivo, verificar se o diretório pai do `filePath` existe. Se não existir, criá-lo recursivamente para evitar `FileNotFoundException`.
2. **Escrita do Arquivo:** Utilizar `java.nio.file.Files.writeString()` para escrever o `content` no `filePath`. Este método sobrescreve o arquivo se ele já existir.
3. **Tratamento de Exceções:** Implementar blocos `try-catch` para IOException que pode ocorrer durante a criação de diretórios ou escrita do arquivo. Em caso de erro, a erramenta deve retornar `false` e logar o problema.

**Exemplo de Código:**

```java
import java.io.IOException;
import java.nio.files.Files;
import java.nio.files.Path;
import java.nio.files.Paths;
import java.nio.files.StandardOpenOption;

public class CodeWriterTool {
  
  public boolean writeCodeFile(String filePath, String content) {
    Path path = Paths.get(filePath);

    try {
      // Garante que o diretório do pai exista
      Files.createDirectories(path.getParent());
      Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

      System.out.println("Arquivo " + filePath + " escrito com sucesso.");
      return true;

    } catch (IOException e) {
      System.out.println("Erro ao escrever o arquivo " + filePath + ": " + e.getMessage);
      return false;
    }
  }

}
```

**Critérios de Aceitação da Sub-tarefa:**

- O método `writeCodeFile` cria um novo arquivo se ele não existir.
- O método `writeCodeFile` sobrescreve o conteúdo de um arquivo existente.
- O método retorna `true` em caso de sucesso `false` em caso de falha, com e logging apropriado.
- O método cria os diretórios necessários no caminho especificado.

#### Sub-tarefa 6.1.2: Criar método `appendCodeToFile(path, content)` para adicionar conteúdo

**Descrição:** Este método será utilizado para adicionar conteúdo ao final de um arquivo existente. Isso é útil quando o agente precisa adicionar um novo método de teste a uma classe de teste já existente, sem sobrescrever todo o seu conteúdo.

**Assinatura do Método:**

```java
public boolean appendCodeToFile(String filePath, String content) {
  // Implementação ...
}
```

**Passos de Implementação:**

1. **Verificação de Existência:** Verificar se o arquivo `filePath` existe. Se não existir, o método deve se comportar como `writeCodeFile` ou retornar `false` (decisão de design: para este contexto, é mais seguro que `append` falhe se o arquivo não existir, forçando o agente a primeir criar o arquivo com `writeCodeFile`).
2. **Adição de Conteúdo:** Utilizar `java.nio.file.Files.writeString()` com a opção `StandardOpenOption.APPEND`.
3. **Tratamento de Exceções:** Implementar blocos `try-catch` para `IOException`.

**Exemplo de Código:**

```java
// ... dentro da classe CodeWriterTool
public boolean appendCodeToFile(String filePath, String content) {
  Path path = Paths.get(filePath);
  if (!Files.exists(path)) {
    System.out.println("Erro: Arquivo " + filePath + " não existe para append.");
    return false; // Ou chamar writeCodeFile se o comportamento desejado for criar.
  }

  try {
    Files.writeString(path, content, StandardOption.APPEND);
    System.out.println("Conteúdo adicionado ao arquivo " + filePath + " com sucesso.");
    return true;
  } catch (IOException e) {
    System.out.println("Erro ao adicionar conteúdo ao arquivo: " + filePath + ": " + e.getMessage());
    return false;
  }
}
```

**Critérios de Aceitação da Sub-tarefa:**

- O método `appendCodeToFile` adiciona o `content` ao final de um arquivo existente.
- O método retorna `true` em caso de sucesso e `false` em caso de falha (ex: o arquivo não existe), com logging apropriado.
- O método não sobrescreve o conteúdo existente do arquivo.

#### Sub-tarefa 6.1.3: Garantir que o diretório de destino exista (criar se necessário)

**Descrição:** Esta sub-tarefa é uma pré-condição para as operações de escrita e adição de conteúdo. Garante que a estrutura de diretórios necessária para o `filePath` esteja  presente antes de qualquer tentativa de I/O no arquivo. Esta lógica já foi incorporada na `writeCodeFile` na sub-tarefa 6.1.1, mas é importante destacá-la como um requisito.

**Passos de Implementação:**

1. **Utilização de `Files.createDirectories()`:** O método` Files.createDirectories(path.getParent())` é a forma recomendada de garantir que todos os diretórios pai de um determinado caminho existam. Ele cria todos os diretórios intermediários se eles não existirem.

**Critérios de Aceitação de Sub-tarefa:**

- Qualquer tentativa de escrita ou adição de conteúdo a um arquivo em um diretório inexistente resulta na criação automática dos diretórios necessários.
- Nenhuma exceção de diretório não encontrado é lançada durante as operações de escrita/append.

#### Sub-tarefa 6.1.4: Lidar com exceções de I/O

**Descrição:** Todas as operações de I/O são suscetíveis a exceções (ex: permissão negada, disco cheio, arquivo corrompido). É crucial que a `CodeWriterTool` trate essas exceções de forma robusta, informando o agente sobre a falha sem interromper o fluxo principal do sistema.

**Passos de Implementação:**

1.**Blocos `try-catch`:** Envolver todas as operações de I/O em blocos `try-catch` para caturar `IOException`.
2.**Logging:** Registrar mensagens de erro detalhadas utilizando um sistema de logging (ex: `j**va.util.logging` ou SLF J/Logback) para auxiliar na depuração.
3.**Retorno de Status:** Retornar um valor booleano (`true` para sucesso, `false` para falha) ou um objeto de resultado que contenha o status e uma mensagem de erro, permitindo que o agente tome decisões com base no sucesso ou falha da operação de escrita.

**Critérios de Aceitação da Sub-tarefa:**

- Nenhuma `IOException` não tratada é propagada para fora da `CodeWriterTool`.
- Mensagens de erro claras são logadas quando uma operação de I/O falha.
- Os métodos de escrita/append retornam um status que indica o sucesso ou falha da operação.

## 4. Integração com Google ADK (Conceitual)

Para integrar a `CodeWriterTool` com o Google ADK, ela precisará ser registrada como uma Tool que o agente pode invocar. O Google ADK permitirá que o LLM ou o `TestPlanner` chame os métodos `writeCodeFile` ou `appendCodeToFile` com o caminho e o conteúdo do arquivo, e receba o status de sucesso/falha como resultado.

**Exemplo de Registro (Conceitual - a ser detalhado na fase de Integração):

```java

```

## 5. Testes Unitários (para `CodeWriterTool`)

Testes unitários devem ser escritos para a classe `CodeWriterTool` para garantir que cada sub-tarefa funcione conforme o esperado. Casos de teste devem incluir:

- Criação de um novo arquivo em um diretório existente.
- Criação de um novo arquivo em um diretório inexistente (verificar criação de diretórios).
- Sobrescrita de um arquivo existente.
- Adição de conteúdo a um arquivo existente.
- Tentativa de adicionar conteúdo a um arquivo inexistente (verificar comportamento esperado).
- Testes de exceção (simular permissão negada, por exemplo, se possível).
- Verificação do conteúdo do arquivo após as operações de escrita/append.

## 6. Referências

- **Java NIO.2 (Files API):**
  <https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html>