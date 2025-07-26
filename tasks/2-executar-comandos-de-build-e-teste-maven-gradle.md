# Especificações Técnicas Detalhadas: Fase 1 (MVP)

Este documento contém as especificações técnicas detalhadas para todas as User Stories da Fase 1 (MVP) do roadmap de implementação do sistema multi-agentes autônomo para geração de testes unitários. Cada especificação detalha como as tarefas e sub-tarefas devem ser implementadas para atender aos critérios de aceitação da respectiva User Story, com foco na entrega de um produto mínimo viável funcional.

# Especificação Técnica Detalhada: US7 - Executar Comandos de Build e Teste Maven/Gradle

**User Story:** US7: Como agente de IA, quero executar comandos de build e teste Maven/Gradle em um workspace isolado para que eu possa compilar o projeto e validar os testes gerados sem interferência do plugin.

**Épico Relacionado:** Implementar um Sistema Multi-Agentes 100% Autônomo para Geração Automática de Testes Unitários em Projetos Java.

**Fase do Roadmap:** FASE 1: MVP - CORE FUNCIONAL

## 1. Visão Geral

Esta especificação detalha a implementação da funcionalidade de execução de comandos de build e teste (Maven/Gradle) em um **workspace isolado**, que é um componente crítico da fase de Ação/Tool Calling do agente. O objetivo é permitir que o agente compile o código gerado (incluindo os testes unitários) e execute os testes em um ambiente controlado, capturando os resultados reais para que o agente possa avaliar o sucesso ou falha e o coverage.

## 2. Critérios de Aceitação da User Story

Para que a US7 seja considerada concluída, o sistema deve atender aos seguintes critérios:

* O agente cria um workspace isolado (sandbox) para execução segura.
* O agente executa comandos Maven reais (ex: `mvn test`, `mvn clean install`) no sandbox.
* O agente captura a saída real e o código de saída dos comandos.
* O agente identifica se o build e os testes foram bem-sucedidos ou falharam.
* O agente só entrega os testes gerados se o build e testes passarem com sucesso.

## 3. Arquitetura do Workspace Isolado

### 3.1 Fluxo de Execução

```
1. Plugin detecta classes para gerar testes
2. Cria workspace isolado (sandbox)
3. Copia projeto para sandbox (REMOVENDO plugin UniCat)
4. Gera testes no sandbox
5. Executa: mvn compile (REAL)
6. Executa: mvn test (REAL)
7. Se TUDO passar → Copia testes para projeto original
8. Se FALHAR → Reporta erro, não entrega nada
```

### 3.2 Estrutura do Sandbox

```
/tmp/unicat-sandbox-{timestamp}/
├── src/
│   ├── main/java/          # Código fonte original
│   └── test/java/          # Testes gerados + existentes
├── pom.xml                 # POM sem plugin UniCat
├── target/                 # Build artifacts
└── logs/                   # Logs de execução
```

## 4. Detalhamento Técnico por Tarefa e Sub-tarefa

### Tarefa 7.1: Implementar `BuildAndTestTool` com Workspace Isolado

**Objetivo:** Criar uma classe de ferramenta (`Tool`) que encapsule a lógica de execução de comandos de sistema em um workspace isolado, especificamente para Maven/Gradle, e retorne a saída real e o status de execução.

**Nome da Classe:** `br.com.unicat.tools.BuildAndTestTool`

**Dependências:** 
- APIs de Process do Java
- Apache Commons IO (para cópia de arquivos)
- Maven Invoker API (para execução segura)

#### Sub-tarefa 7.1.1: Criar método `createIsolatedWorkspace(projectPath)`

**Descrição:** Este método cria um workspace isolado copiando o projeto original, mas removendo o plugin UniCat para evitar recursão.

**Assinatura do Método:**

```java
public WorkspaceInfo createIsolatedWorkspace(String projectPath) throws IOException {
    // ... implementação ...
}

// Classe auxiliar para informações do workspace
public static class WorkspaceInfo {
    public Path workspacePath;
    public Path originalProjectPath;
    public boolean success;
    public String errorMessage;
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método cria um diretório temporário único.
* O método copia todo o projeto para o workspace.
* O método remove o plugin UniCat do pom.xml do workspace.
* O método retorna informações sobre o workspace criado.

#### Sub-tarefa 7.1.2: Implementar método `executeMavenCommandInWorkspace(workspacePath, command)`

**Descrição:** Este método executa comandos Maven reais no workspace isolado, capturando resultados reais.

**Assinatura do Método:**

```java
public CommandResult executeMavenCommandInWorkspace(Path workspacePath, String command) {
    // ... implementação ...
}

// Classe auxiliar para o resultado do comando
public static class CommandResult {
    public int exitCode;
    public String output;
    public String errorOutput;
    public boolean success;
    public long executionTimeMs;
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método executa comandos Maven reais (não simulados).
* O método captura saída padrão e erro reais.
* O método retorna código de saída real do Maven.
* O método mede tempo de execução.

#### Sub-tarefa 7.1.3: Implementar método `validateBuildAndTests(workspacePath)`

**Descrição:** Este método executa a sequência completa de build e testes, validando cada etapa.

**Assinatura do Método:**

```java
public ValidationResult validateBuildAndTests(Path workspacePath) {
    // ... implementação ...
}

// Classe auxiliar para resultado da validação
public static class ValidationResult {
    public boolean buildSuccess;
    public boolean testsSuccess;
    public CommandResult buildResult;
    public CommandResult testResult;
    public String summary;
    public boolean overallSuccess;
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método executa `mvn compile` e valida sucesso.
* O método executa `mvn test` e valida sucesso.
* O método retorna resultado detalhado de cada etapa.
* O método só retorna sucesso se AMBAS etapas passarem.

#### Sub-tarefa 7.1.4: Implementar método `copyTestsToOriginalProject(workspacePath, originalPath)`

**Descrição:** Este método copia os testes gerados do workspace para o projeto original apenas se a validação for bem-sucedida.

**Assinatura do Método:**

```java
public CopyResult copyTestsToOriginalProject(Path workspacePath, Path originalPath) {
    // ... implementação ...
}

// Classe auxiliar para resultado da cópia
public static class CopyResult {
    public boolean success;
    public int filesCopied;
    public List<String> copiedFiles;
    public String errorMessage;
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método só copia se validação anterior foi bem-sucedida.
* O método copia apenas arquivos de teste gerados.
* O método preserva estrutura de diretórios.
* O método retorna lista de arquivos copiados.

#### Sub-tarefa 7.1.5: Implementar limpeza automática do workspace

**Descrição:** Este método limpa o workspace temporário após a conclusão da operação.

**Assinatura do Método:**

```java
public void cleanupWorkspace(Path workspacePath) {
    // ... implementação ...
}
```

**Critérios de Aceitação da Sub-tarefa:**

* O método remove completamente o workspace temporário.
* O método trata erros de limpeza graciosamente.
* O método é chamado automaticamente após operações.

### Tarefa 7.2: Integrar Workspace Isolado no UnicatMojo

**Objetivo:** Modificar o `UnicatMojo` para usar o workspace isolado em vez de execução direta.

#### Sub-tarefa 7.2.1: Modificar fluxo de execução

**Descrição:** Alterar o fluxo para usar workspace isolado.

**Fluxo Atual:**
```
1. Gera testes no projeto original
2. Tenta executar build/test (falha por recursão)
3. Simula sucesso
```

**Fluxo Novo:**
```
1. Cria workspace isolado
2. Copia projeto (sem plugin UniCat)
3. Gera testes no workspace
4. Executa build/test REAL no workspace
5. Se sucesso → Copia testes para original
6. Se falha → Reporta erro real
7. Limpa workspace
```

#### Sub-tarefa 7.2.2: Implementar validação condicional

**Descrição:** Só entregar testes se build e testes passarem.

**Critérios de Aceitação:**

* Plugin só entrega testes se `mvn compile` passar.
* Plugin só entrega testes se `mvn test` passar.
* Se qualquer etapa falhar, não entrega nada.
* Reporta erro detalhado para o usuário.

## 5. Implementação Detalhada

### 5.1 Criação do Workspace Isolado

```java
public WorkspaceInfo createIsolatedWorkspace(String projectPath) throws IOException {
    // 1. Criar diretório temporário único
    Path tempDir = Files.createTempDirectory("unicat-sandbox-");
    
    // 2. Copiar projeto para workspace
    Path originalPath = Paths.get(projectPath);
    Path workspacePath = tempDir.resolve("project");
    FileUtils.copyDirectory(originalPath.toFile(), workspacePath.toFile());
    
    // 3. Remover plugin UniCat do pom.xml
    removeUnicatPluginFromPom(workspacePath.resolve("pom.xml"));
    
    return new WorkspaceInfo(workspacePath, originalPath, true, null);
}
```

### 5.2 Execução Real do Maven

```java
public CommandResult executeMavenCommandInWorkspace(Path workspacePath, String command) {
    // Usar Maven Invoker API para execução segura
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(workspacePath.resolve("pom.xml").toFile());
    request.setGoals(Arrays.asList(command.split("\\s+")));
    request.setBatchMode(true);
    request.setTimeoutInSeconds(300);
    
    // Configurar para evitar recursão
    Properties props = new Properties();
    props.setProperty("maven.terminal.output", "false");
    request.setProperties(props);
    
    // Executar e capturar resultado real
    Invoker invoker = new DefaultInvoker();
    InvocationResult result = invoker.execute(request);
    
    return new CommandResult(
        result.getExitCode(),
        captureOutput(result),
        captureError(result),
        result.getExitCode() == 0,
        System.currentTimeMillis()
    );
}
```

### 5.3 Validação Completa

```java
public ValidationResult validateBuildAndTests(Path workspacePath) {
    // 1. Executar build
    CommandResult buildResult = executeMavenCommandInWorkspace(workspacePath, "compile");
    
    if (!buildResult.success) {
        return new ValidationResult(false, false, buildResult, null, 
            "Build falhou: " + buildResult.errorOutput, false);
    }
    
    // 2. Executar testes
    CommandResult testResult = executeMavenCommandInWorkspace(workspacePath, "test");
    
    boolean overallSuccess = buildResult.success && testResult.success;
    
    return new ValidationResult(
        buildResult.success,
        testResult.success,
        buildResult,
        testResult,
        "Build: " + (buildResult.success ? "SUCESSO" : "FALHA") + 
        ", Testes: " + (testResult.success ? "SUCESSO" : "FALHA"),
        overallSuccess
    );
}
```

## 6. Integração com Google ADK (Conceitual)

Para integrar a `BuildAndTestTool` com o Google ADK, ela precisará ser registrada como uma `Tool` que o agente pode invocar. O Google ADK permitirá que o LLM ou o `TestPlanner` chame os métodos do workspace isolado e receba resultados reais para avaliar o sucesso do build e dos testes.

## 7. Testes Unitários

### 7.1 Testes para Workspace Isolado

* **Teste de Criação:** Verificar se workspace é criado corretamente.
* **Teste de Cópia:** Verificar se projeto é copiado sem plugin UniCat.
* **Teste de Execução:** Verificar se Maven executa realmente no workspace.
* **Teste de Validação:** Verificar se build e testes são validados corretamente.
* **Teste de Cópia Condicional:** Verificar se testes só são copiados se validação passar.
* **Teste de Limpeza:** Verificar se workspace é limpo após uso.

### 7.2 Casos de Teste

* **Cenário de Sucesso:** Build e testes passam → testes são entregues.
* **Cenário de Falha no Build:** Build falha → testes não são entregues.
* **Cenário de Falha nos Testes:** Build passa, testes falham → testes não são entregues.
* **Cenário de Timeout:** Execução demora muito → timeout e erro.
* **Cenário de Erro de Cópia:** Erro ao copiar projeto → erro reportado.

## 8. Benefícios da Implementação

### 8.1 Vantagens do Workspace Isolado

* **✅ Execução Real:** Maven executa de verdade, não simulado.
* **✅ Sem Recursão:** Plugin UniCat não interfere na execução.
* **✅ Validação Real:** Build e testes são validados realmente.
* **✅ Entrega Segura:** Só entrega testes se tudo passar.
* **✅ Isolamento:** Não afeta projeto original durante validação.
* **✅ Limpeza Automática:** Workspace é removido após uso.

### 8.2 Melhorias na Qualidade

* **Detecção Real de Erros:** Identifica problemas reais de compilação.
* **Validação de Testes:** Garante que testes gerados funcionam.
* **Feedback Preciso:** Reporta erros reais, não simulados.
* **Confiabilidade:** Entrega só testes que realmente funcionam.

## 9. Referências

* **Java ProcessBuilder:** [https://docs.oracle.com/javase/8/docs/api/java/lang/ProcessBuilder.html](https://docs.oracle.com/javase/8/docs/api/java/lang/ProcessBuilder.html)
* **Maven Invoker API:** [https://maven.apache.org/shared/maven-invoker/](https://maven.apache.org/shared/maven-invoker/)
* **Apache Commons IO:** [https://commons.apache.org/proper/commons-io/](https://commons.apache.org/proper/commons-io/)

---

**Autor:** Manus AI
**Data:** 25 de Julho de 2025
**Versão:** 2.0 - Workspace Isolado

---
