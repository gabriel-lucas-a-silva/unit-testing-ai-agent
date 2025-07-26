package br.com.unicat.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

// Maven Invoker API imports
import org.apache.maven.shared.invoker.*;

// Apache Commons IO imports
import org.apache.commons.io.FileUtils;

/**
 * Ferramenta para execução de comandos Maven em workspace isolado.
 * Permite ao agente executar builds e testes em um sandbox isolado,
 * capturando resultados reais e validando antes de entregar os testes.
 * 
 * ARQUITETURA DO WORKSPACE ISOLADO:
 * 1. Cria workspace temporário único
 * 2. Copia projeto (removendo plugin UniCat)
 * 3. Gera testes no workspace
 * 4. Executa build/test REAL no workspace
 * 5. Se sucesso → Copia testes para projeto original
 * 6. Se falha → Reporta erro, não entrega nada
 * 7. Limpa workspace automaticamente
 */
public class BuildAndTestTool {
    
    private static final Logger LOGGER = Logger.getLogger(BuildAndTestTool.class.getName());
    private static final int DEFAULT_TIMEOUT_SECONDS = 300; // 5 minutos
    
    /**
     * Cria um workspace isolado copiando o projeto original, mas removendo o plugin UniCat.
     * 
     * @param projectPath Caminho do projeto original
     * @return Informações sobre o workspace criado
     * @throws IOException Se houver erro na criação do workspace
     */
    public WorkspaceInfo createIsolatedWorkspace(String projectPath) throws IOException {
        LOGGER.info("🏗️ Criando workspace isolado para: " + projectPath);
        
        try {
            // 1. Criar diretório no ~/Developer/unicat-workspaces
            String userHome = System.getProperty("user.home");
            Path developerDir = Paths.get(userHome, "Developer");
            if (!Files.exists(developerDir)) {
                Files.createDirectories(developerDir);
            }
            
            // Criar pasta específica da UniCat
            Path unicatWorkspacesDir = developerDir.resolve("unicat-workspaces");
            if (!Files.exists(unicatWorkspacesDir)) {
                Files.createDirectories(unicatWorkspacesDir);
            }
            
            String timestamp = String.valueOf(System.currentTimeMillis());
            Path tempDir = unicatWorkspacesDir.resolve("workspace-" + timestamp);
            Files.createDirectories(tempDir);
            
            Path workspacePath = tempDir.resolve("project");
            Path originalPath = Paths.get(projectPath);
            
            LOGGER.info("📁 Workspace temporário criado: " + tempDir);
            
            // 2. Copiar projeto para workspace
            FileUtils.copyDirectory(originalPath.toFile(), workspacePath.toFile());
            LOGGER.info("📋 Projeto copiado para workspace");
            
                    // 3. Remover plugin UniCat do pom.xml
        Path pomPath = workspacePath.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            removeUnicatPluginFromPom(pomPath);
        }
            
            // 4. Criar diretório de logs
            Path logsDir = tempDir.resolve("logs");
            Files.createDirectories(logsDir);
            
            WorkspaceInfo workspaceInfo = new WorkspaceInfo();
            workspaceInfo.workspacePath = workspacePath;
            workspaceInfo.originalProjectPath = originalPath;
            workspaceInfo.success = true;
            workspaceInfo.errorMessage = null;
            
            LOGGER.info("✅ Workspace isolado criado com sucesso: " + workspacePath);
            return workspaceInfo;
            
        } catch (Exception e) {
            LOGGER.severe("❌ Erro ao criar workspace isolado: " + e.getMessage());
            WorkspaceInfo workspaceInfo = new WorkspaceInfo();
            workspaceInfo.success = false;
            workspaceInfo.errorMessage = "Erro ao criar workspace: " + e.getMessage();
            throw new IOException("Falha ao criar workspace isolado", e);
        }
    }
    
    /**
     * Executa um comando Maven real no workspace isolado.
     * 
     * @param workspacePath Caminho do workspace
     * @param command Comando Maven (ex: "compile", "test")
     * @return Resultado da execução do comando
     */
    public CommandResult executeMavenCommandInWorkspace(Path workspacePath, String command) {
        return executeMavenCommandInWorkspace(workspacePath, command, DEFAULT_TIMEOUT_SECONDS);
    }
    
    /**
     * Executa um comando Maven real no workspace isolado com timeout personalizado.
     * 
     * @param workspacePath Caminho do workspace
     * @param command Comando Maven (ex: "compile", "test")
     * @param timeoutSeconds Timeout em segundos
     * @return Resultado da execução do comando
     */
    public CommandResult executeMavenCommandInWorkspace(Path workspacePath, String command, int timeoutSeconds) {
        LOGGER.info("🚀 Executando comando Maven REAL: " + command + " em " + workspacePath);
        
        CommandResult result = new CommandResult();
        long startTime = System.currentTimeMillis();
        
        try {
            // Verifica se o workspace existe
            if (!Files.exists(workspacePath) || !Files.isDirectory(workspacePath)) {
                result.success = false;
                result.exitCode = -1;
                result.errorOutput = "Workspace não encontrado: " + workspacePath;
                LOGGER.warning("Workspace não encontrado: " + workspacePath);
                return result;
            }
            
            // Executa com Maven Invoker API (execução real)
            try {
                LOGGER.info("🔧 Executando com Maven Invoker API (execução REAL)...");
                return executeWithMavenInvoker(workspacePath, command, timeoutSeconds, startTime);
            } catch (Exception e) {
                LOGGER.warning("Maven Invoker API falhou, tentando com ProcessBuilder: " + e.getMessage());
                // Fallback para ProcessBuilder
                return executeWithProcessBuilder(workspacePath, command, timeoutSeconds, startTime);
            }
            
        } catch (Exception e) {
            result.success = false;
            result.exitCode = -1;
            result.errorOutput = "Erro inesperado: " + e.getMessage();
            result.executionTimeMs = System.currentTimeMillis() - startTime;
            LOGGER.log(Level.SEVERE, "Erro inesperado ao executar comando Maven", e);
        }
        
        return result;
    }
    
    /**
     * Executa comando Maven usando Maven Invoker API (execução REAL).
     */
    private CommandResult executeWithMavenInvoker(Path workspacePath, String command, int timeoutSeconds, long startTime) throws Exception {
        LOGGER.info("🔧 Executando com Maven Invoker API (EXECUÇÃO REAL)");
        
        CommandResult result = new CommandResult();
        
        // Capturadores de saída personalizados
        StringBuilder outputCapture = new StringBuilder();
        StringBuilder errorCapture = new StringBuilder();
        
        try {
            // Configura a requisição de invocação
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(workspacePath.resolve("pom.xml").toFile());
            
            // Usa goals específicos para evitar problemas
            List<String> goals = convertToSpecificGoals(command);
            request.setGoals(goals);
            
            // Configurações para execução segura
            request.setBatchMode(true);
            request.setQuiet(false);
            request.setTimeoutInSeconds(timeoutSeconds);
            request.setRecursive(false);
            
            // Configurações para evitar interferência
            Properties props = new Properties();
            props.setProperty("maven.terminal.output", "false");
            props.setProperty("maven.terminal.use.color", "false");
            props.setProperty("maven.wrapper.quiet", "true");
            request.setProperties(props);
            
            // Configura variáveis de ambiente
            request.setMavenOpts("-Xmx512m");
            
            // Configura capturadores de saída
            request.setOutputHandler(new InvocationOutputHandler() {
                @Override
                public void consumeLine(String line) {
                    outputCapture.append(line).append(System.lineSeparator());
                    // Log em tempo real para o usuário
                    if (line != null && !line.trim().isEmpty()) {
                        LOGGER.info("[MAVEN-REAL] " + line);
                    }
                }
            });
            
            request.setErrorHandler(new InvocationOutputHandler() {
                @Override
                public void consumeLine(String line) {
                    errorCapture.append(line).append(System.lineSeparator());
                    // Log em tempo real para o usuário
                    if (line != null && !line.trim().isEmpty()) {
                        // Só loga como warning se realmente for um erro
                        if (line.contains("ERROR") || line.contains("FAILURE") || line.contains("Exception")) {
                            LOGGER.warning("[MAVEN-ERROR-REAL] " + line);
                        } else {
                            LOGGER.info("[MAVEN-REAL] " + line);
                        }
                    }
                }
            });
            
            // Configura o invoker
            Invoker invoker = new DefaultInvoker();
            
            // Detecta e configura o Maven Home
            String mavenHome = detectMavenHome();
            if (mavenHome != null) {
                invoker.setMavenHome(new File(mavenHome));
                LOGGER.info("Maven Home detectado: " + mavenHome);
            } else {
                LOGGER.warning("Maven Home não detectado, usando padrão do sistema");
            }
            
            // Executa a invocação (EXECUÇÃO REAL)
            LOGGER.info("🚀 Iniciando execução REAL do comando Maven: " + String.join(" ", goals));
            InvocationResult invocationResult = invoker.execute(request);
            
            // Processa o resultado
            result.exitCode = invocationResult.getExitCode();
            result.success = (result.exitCode == 0);
            result.output = outputCapture.toString();
            result.errorOutput = errorCapture.toString();
            result.executionTimeMs = System.currentTimeMillis() - startTime;
            
            // Captura saída
            if (invocationResult.getExecutionException() != null) {
                result.errorOutput += "Erro de execução: " + invocationResult.getExecutionException().getMessage();
                LOGGER.warning("Erro de execução no Maven Invoker: " + invocationResult.getExecutionException().getMessage());
            }
            
            LOGGER.info("Maven Invoker executado com código de saída REAL: " + result.exitCode);
            
            // Log detalhado do resultado
            if (result.success) {
                LOGGER.info("✅ Comando Maven executado com SUCESSO REAL!");
            } else {
                LOGGER.warning("❌ Comando Maven falhou com código REAL: " + result.exitCode);
                if (!result.output.isEmpty()) {
                    LOGGER.info("📋 Saída do comando:");
                    LOGGER.info(result.output);
                }
                if (!result.errorOutput.isEmpty()) {
                    LOGGER.warning("📋 Erros do comando:");
                    LOGGER.warning(result.errorOutput);
                }
            }
            
        } catch (Exception e) {
            LOGGER.severe("Erro inesperado no Maven Invoker: " + e.getMessage());
            throw e;
        }
        
        return result;
    }
    
    /**
     * Executa comando Maven usando ProcessBuilder (fallback).
     */
    private CommandResult executeWithProcessBuilder(Path workspacePath, String command, int timeoutSeconds, long startTime) throws Exception {
        LOGGER.info("🔧 Executando com ProcessBuilder (fallback - EXECUÇÃO REAL)");
        
        CommandResult result = new CommandResult();
        
        // Detecta qual comando Maven usar
        String mavenCommand = detectMavenCommand(workspacePath.toFile());
        if (mavenCommand == null) {
            result.success = false;
            result.exitCode = -1;
            result.errorOutput = "Maven não encontrado no workspace.";
            result.executionTimeMs = System.currentTimeMillis() - startTime;
            LOGGER.severe("Maven não encontrado no workspace");
            return result;
        }
        
        try {
            // Prepara o comando Maven
            List<String> commandParts = new ArrayList<>();
            commandParts.add(mavenCommand);
            commandParts.add("-B"); // Modo batch
            commandParts.add("-Dmaven.terminal.output=false");
            commandParts.add("-Dmaven.terminal.use.color=false");
            commandParts.add("-Dmaven.wrapper.quiet=true");
            
            // Adiciona o comando original
            commandParts.addAll(Arrays.asList(command.split("\\s+")));
            
            ProcessBuilder processBuilder = new ProcessBuilder(commandParts);
            processBuilder.directory(workspacePath.toFile());
            
            // Configura variáveis de ambiente
            processBuilder.environment().put("MAVEN_OPTS", "-Xmx512m");
            processBuilder.environment().put("MAVEN_TERMINAL_OUTPUT", "false");
            processBuilder.environment().put("MAVEN_TERMINAL_USE_COLOR", "false");
            processBuilder.environment().put("MAVEN_WRAPPER_QUIET", "true");
            
            // Mescla streams para evitar deadlock
            processBuilder.redirectErrorStream(true);
            
            // Inicia o processo
            LOGGER.info("🚀 Iniciando execução REAL do comando Maven: " + String.join(" ", commandParts));
            Process process = processBuilder.start();
            
            // Captura saída em thread única
            Future<String> outputFuture = Executors.newSingleThreadExecutor().submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append(System.lineSeparator());
                        // Log em tempo real para o usuário
                        LOGGER.info("[MAVEN-REAL] " + line);
                    }
                    return output.toString();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Erro ao ler saída do processo", e);
                    return "";
                }
            });
            
            // Aguarda conclusão do processo com timeout
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!finished) {
                // Timeout - mata o processo
                process.destroyForcibly();
                result.success = false;
                result.exitCode = -1;
                result.errorOutput = "Timeout após " + timeoutSeconds + " segundos";
                result.output = outputFuture.get();
                result.executionTimeMs = System.currentTimeMillis() - startTime;
                LOGGER.warning("Timeout na execução do comando Maven: " + command);
                return result;
            }
            
            // Obtém o código de saída
            int exitCode = process.exitValue();
            
            // Obtém a saída
            String output = outputFuture.get();
            
            // Preenche o resultado
            result.exitCode = exitCode;
            result.output = output;
            result.errorOutput = ""; // Já está mesclado com output
            result.success = (exitCode == 0);
            result.executionTimeMs = System.currentTimeMillis() - startTime;
            
            LOGGER.info("ProcessBuilder executado com código de saída REAL: " + exitCode);
            
            // Log detalhado do resultado
            if (result.success) {
                LOGGER.info("✅ Comando Maven executado com SUCESSO REAL!");
            } else {
                LOGGER.warning("❌ Comando Maven falhou com código REAL: " + result.exitCode);
                if (!result.output.isEmpty()) {
                    LOGGER.info("📋 Saída completa do comando:");
                    LOGGER.info(result.output);
                }
            }
            
        } catch (Exception e) {
            LOGGER.severe("Erro inesperado no ProcessBuilder: " + e.getMessage());
            throw e;
        }
        
        return result;
    }
    
    /**
     * Valida build e testes no workspace isolado.
     * 
     * @param workspacePath Caminho do workspace
     * @return Resultado da validação
     */
    public ValidationResult validateBuildAndTests(Path workspacePath) {
        LOGGER.info("🔍 Iniciando validação de build e testes no workspace: " + workspacePath);
        
        ValidationResult validationResult = new ValidationResult();
        
        try {
            // 1. Executar build (compile)
            LOGGER.info("🔨 Executando build (mvn compile)...");
            CommandResult buildResult = executeMavenCommandInWorkspace(workspacePath, "compile");
            validationResult.buildSuccess = buildResult.success;
            validationResult.buildResult = buildResult;
            
            if (!buildResult.success) {
                validationResult.testsSuccess = false;
                validationResult.testResult = null;
                validationResult.overallSuccess = false;
                validationResult.summary = "❌ Build falhou: " + buildResult.errorOutput;
                LOGGER.warning("❌ Build falhou, pulando execução de testes");
                return validationResult;
            }
            
            LOGGER.info("✅ Build executado com sucesso!");
            
            // 2. Executar testes
            LOGGER.info("🧪 Executando testes (mvn test)...");
            CommandResult testResult = executeMavenCommandInWorkspace(workspacePath, "test");
            validationResult.testsSuccess = testResult.success;
            validationResult.testResult = testResult;
            
            // 3. Determinar sucesso geral
            validationResult.overallSuccess = buildResult.success && testResult.success;
            
            // 4. Gerar resumo
            StringBuilder summary = new StringBuilder();
            summary.append("Build: ").append(buildResult.success ? "✅ SUCESSO" : "❌ FALHA");
            summary.append(", Testes: ").append(testResult.success ? "✅ SUCESSO" : "❌ FALHA");
            
            if (validationResult.overallSuccess) {
                summary.append(" → ✅ VALIDAÇÃO COMPLETA");
            } else {
                summary.append(" → ❌ VALIDAÇÃO FALHOU");
            }
            
            validationResult.summary = summary.toString();
            
            LOGGER.info("📊 Resultado da validação: " + validationResult.summary);
            
        } catch (Exception e) {
            LOGGER.severe("❌ Erro durante validação: " + e.getMessage());
            validationResult.buildSuccess = false;
            validationResult.testsSuccess = false;
            validationResult.overallSuccess = false;
            validationResult.summary = "❌ Erro durante validação: " + e.getMessage();
        }
        
        return validationResult;
    }
    
    /**
     * Copia os testes gerados do workspace para o projeto original apenas se a validação for bem-sucedida.
     * 
     * @param workspacePath Caminho do workspace
     * @param originalPath Caminho do projeto original
     * @return Resultado da cópia
     */
    public CopyResult copyTestsToOriginalProject(Path workspacePath, Path originalPath) {
        LOGGER.info("📋 Copiando testes do workspace para projeto original...");
        
        CopyResult copyResult = new CopyResult();
        copyResult.copiedFiles = new ArrayList<>();
        
        try {
            // Caminhos dos diretórios de teste
            Path workspaceTestDir = workspacePath.resolve("src").resolve("test").resolve("java");
            Path originalTestDir = originalPath.resolve("src").resolve("test").resolve("java");
            
            if (!Files.exists(workspaceTestDir)) {
                copyResult.success = false;
                copyResult.errorMessage = "Diretório de testes não encontrado no workspace: " + workspaceTestDir;
                LOGGER.warning("Diretório de testes não encontrado no workspace");
                return copyResult;
            }
            
            // Garante que o diretório de destino existe
            if (!Files.exists(originalTestDir)) {
                Files.createDirectories(originalTestDir);
                LOGGER.info("Diretório de testes criado no projeto original: " + originalTestDir);
            }
            
            // Copia arquivos de teste
            copyTestFilesRecursively(workspaceTestDir, originalTestDir, copyResult.copiedFiles);
            
            copyResult.success = true;
            copyResult.filesCopied = copyResult.copiedFiles.size();
            
            LOGGER.info("✅ " + copyResult.filesCopied + " arquivos de teste copiados com sucesso");
            
        } catch (Exception e) {
            copyResult.success = false;
            copyResult.errorMessage = "Erro ao copiar testes: " + e.getMessage();
            LOGGER.severe("❌ Erro ao copiar testes: " + e.getMessage());
        }
        
        return copyResult;
    }
    
    /**
     * Limpa o workspace temporário após a conclusão da operação.
     * 
     * @param workspacePath Caminho do workspace
     */
    public void cleanupWorkspace(Path workspacePath) {
        if (workspacePath == null) {
            return;
        }
        
        try {
            // Remove o diretório pai (que contém o workspace e logs)
            Path parentDir = workspacePath.getParent();
            if (parentDir != null && Files.exists(parentDir)) {
                deleteDirectoryRecursively(parentDir);
                LOGGER.info("🧹 Workspace limpo: " + parentDir);
            }
        } catch (Exception e) {
            LOGGER.warning("⚠️ Erro ao limpar workspace: " + e.getMessage());
        }
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    

    
    /**
     * Remove o plugin UniCat do pom.xml.
     */
    private void removeUnicatPluginFromPom(Path pomPath) throws IOException {
        String content = new String(Files.readAllBytes(pomPath));
        String originalContent = content;
        
        // Remove o plugin UniCat da seção principal de plugins
        content = removePluginSection(content, "br.com.unicat", "unicat-maven-plugin");
        
        // Remove o plugin UniCat de todos os profiles
        content = removePluginFromProfiles(content);
        
        // Verifica se algo foi removido
        if (!content.equals(originalContent)) {
            Files.write(pomPath, content.getBytes());
            LOGGER.info("🔧 Plugin UniCat removido do pom.xml do workspace");
        } else {
            LOGGER.info("🔧 Plugin UniCat não encontrado no pom.xml do workspace");
        }
    }
    
    /**
     * Remove uma seção de plugin específica do XML.
     */
    private String removePluginSection(String content, String groupId, String artifactId) {
        // Padrão para encontrar a seção do plugin com DOTALL flag
        String pattern = String.format(
            "(?s)(\\s*<plugin>\\s*<groupId>%s</groupId>\\s*<artifactId>%s</artifactId>.*?</plugin>\\s*)",
            groupId, artifactId
        );
        
        // Remove a seção do plugin usando regex
        String result = content.replaceAll(pattern, "");
        
        // Se não encontrou o padrão exato, tenta um padrão mais flexível
        if (result.equals(content)) {
            pattern = String.format(
                "(?s)(\\s*<plugin>.*?<groupId>%s</groupId>.*?<artifactId>%s</artifactId>.*?</plugin>\\s*)",
                groupId, artifactId
            );
            result = content.replaceAll(pattern, "");
        }
        
        return result;
    }
    
    /**
     * Remove o plugin UniCat de todos os profiles.
     */
    private String removePluginFromProfiles(String content) {
        // Padrão para encontrar profiles que contêm o plugin UniCat com DOTALL flag
        String pattern = "(?s)(<profile>.*?<id>.*?</id>.*?<build>.*?<plugins>.*?<plugin>.*?<groupId>br\\.com\\.unicat</groupId>.*?<artifactId>unicat-maven-plugin</artifactId>.*?</plugin>.*?</plugins>.*?</build>.*?</profile>)";
        
        // Remove o plugin de dentro dos profiles
        String result = content.replaceAll(pattern, "");
        
        return result;
    }
    
    /**
     * Copia arquivos de teste recursivamente.
     */
    private void copyTestFilesRecursively(Path sourceDir, Path targetDir, List<String> copiedFiles) throws IOException {
        if (!Files.exists(sourceDir)) {
            return;
        }
        
        Files.list(sourceDir).forEach(sourcePath -> {
            try {
                Path targetPath = targetDir.resolve(sourcePath.getFileName());
                
                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectories(targetPath);
                    }
                    copyTestFilesRecursively(sourcePath, targetPath, copiedFiles);
                } else if (sourcePath.toString().endsWith(".java")) {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    copiedFiles.add(targetPath.toString());
                    LOGGER.info("📄 Copiado: " + sourcePath.getFileName());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Remove um diretório recursivamente.
     */
    private void deleteDirectoryRecursively(Path path) throws IOException {
        FileUtils.deleteDirectory(path.toFile());
    }
    
    /**
     * Converte comandos Maven genéricos para goals específicos.
     */
    private List<String> convertToSpecificGoals(String command) {
        String[] parts = command.split("\\s+");
        
        // Se já é um goal específico (contém :), retorna como está
        if (command.contains(":")) {
            return Arrays.asList(parts);
        }
        
        // Converte comandos de ciclo para goals específicos
        switch (parts[0].toLowerCase()) {
            case "compile":
                return Arrays.asList("compile");
            case "test":
                return Arrays.asList("test");
            case "clean":
                return Arrays.asList("clean");
            case "install":
                return Arrays.asList("install");
            case "package":
                return Arrays.asList("package");
            default:
                return Arrays.asList(parts);
        }
    }
    
    /**
     * Detecta o Maven Home do sistema.
     */
    private String detectMavenHome() {
        // Tenta variáveis de ambiente
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome != null && !mavenHome.trim().isEmpty()) {
            return mavenHome;
        }
        
        mavenHome = System.getenv("M2_HOME");
        if (mavenHome != null && !mavenHome.trim().isEmpty()) {
            return mavenHome;
        }
        
        return null;
    }
    
    /**
     * Detecta qual comando Maven usar.
     */
    private String detectMavenCommand(File projectDir) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        
        // Primeiro verifica se o Maven do sistema está disponível
        if (isCommandAvailable("mvn")) {
            LOGGER.info("Usando Maven do sistema (mvn)");
            return "mvn";
        }
        
        // Verifica se o comando 'mvn.cmd' está disponível (Windows)
        if (isWindows && isCommandAvailable("mvn.cmd")) {
            LOGGER.info("Usando Maven do sistema (mvn.cmd)");
            return "mvn.cmd";
        }
        
        // Se não encontrar Maven do sistema, tenta Maven Wrapper
        File mvnw = new File(projectDir, "mvnw");
        File mvnwCmd = new File(projectDir, "mvnw.cmd");
        
        if (isWindows && mvnwCmd.exists()) {
            LOGGER.warning("Maven do sistema não encontrado. Usando Maven Wrapper");
            return ".\\mvnw.cmd";
        } else if (!isWindows && mvnw.exists() && mvnw.canExecute()) {
            LOGGER.warning("Maven do sistema não encontrado. Usando Maven Wrapper");
            return "./mvnw";
        }
        
        LOGGER.warning("Nenhum comando Maven encontrado");
        return null;
    }
    
    /**
     * Verifica se um comando está disponível no sistema.
     */
    private boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.command("cmd", "/c", command, "--version");
            } else {
                processBuilder.command("sh", "-c", command + " --version");
            }
            
            Process process = processBuilder.start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            
            if (finished && process.exitValue() == 0) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.fine("Comando " + command + " não disponível: " + e.getMessage());
        }
        return false;
    }
    
    // ========== MÉTODOS DE CONVENIÊNCIA ==========
    
    /**
     * Executa um comando Maven de teste específico no workspace.
     */
    public CommandResult executeTests(Path workspacePath) {
        return executeMavenCommandInWorkspace(workspacePath, "test");
    }
    
    /**
     * Executa um comando Maven de compilação no workspace.
     */
    public CommandResult executeCompile(Path workspacePath) {
        return executeMavenCommandInWorkspace(workspacePath, "compile");
    }
    
    /**
     * Executa um comando Maven de clean e install no workspace.
     */
    public CommandResult executeCleanInstall(Path workspacePath) {
        return executeMavenCommandInWorkspace(workspacePath, "clean install");
    }
    
    // ========== CLASSES AUXILIARES ==========
    
    /**
     * Classe que representa informações sobre o workspace isolado.
     */
    public static class WorkspaceInfo {
        public Path workspacePath;
        public Path originalProjectPath;
        public boolean success;
        public String errorMessage;
        
        @Override
        public String toString() {
            return "WorkspaceInfo{" +
                    "workspacePath=" + workspacePath +
                    ", originalProjectPath=" + originalProjectPath +
                    ", success=" + success +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
    
    /**
     * Classe que representa o resultado da execução de um comando.
     */
    public static class CommandResult {
        public int exitCode;
        public String output;
        public String errorOutput;
        public boolean success;
        public long executionTimeMs;
        
        @Override
        public String toString() {
            return "CommandResult{" +
                    "exitCode=" + exitCode +
                    ", success=" + success +
                    ", executionTimeMs=" + executionTimeMs +
                    ", outputLength=" + (output != null ? output.length() : 0) +
                    ", errorOutputLength=" + (errorOutput != null ? errorOutput.length() : 0) +
                    '}';
        }
    }
    
    /**
     * Classe que representa o resultado da validação de build e testes.
     */
    public static class ValidationResult {
        public boolean buildSuccess;
        public boolean testsSuccess;
        public CommandResult buildResult;
        public CommandResult testResult;
        public String summary;
        public boolean overallSuccess;
        
        @Override
        public String toString() {
            return "ValidationResult{" +
                    "buildSuccess=" + buildSuccess +
                    ", testsSuccess=" + testsSuccess +
                    ", overallSuccess=" + overallSuccess +
                    ", summary='" + summary + '\'' +
                    '}';
        }
    }
    
    /**
     * Classe que representa o resultado da cópia de testes.
     */
    public static class CopyResult {
        public boolean success;
        public int filesCopied;
        public List<String> copiedFiles;
        public String errorMessage;
        
        @Override
        public String toString() {
            return "CopyResult{" +
                    "success=" + success +
                    ", filesCopied=" + filesCopied +
                    ", copiedFiles=" + copiedFiles +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
} 