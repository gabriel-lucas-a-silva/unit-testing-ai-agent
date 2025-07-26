package br.com.unicat.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Ferramenta para execução de comandos Maven e captura de resultados.
 * Permite ao agente executar builds e testes, capturando saída e códigos de saída.
 */
public class BuildAndTestTool {
    
    private static final Logger LOGGER = Logger.getLogger(BuildAndTestTool.class.getName());
    private static final int DEFAULT_TIMEOUT_SECONDS = 300; // 5 minutos
    
    /**
     * Executa um comando Maven no diretório especificado.
     * 
     * @param projectPath Caminho do projeto onde executar o comando
     * @param command Comando Maven (ex: "clean install", "test")
     * @return Resultado da execução do comando
     */
    public CommandResult executeMavenCommand(String projectPath, String command) {
        return executeMavenCommand(projectPath, command, DEFAULT_TIMEOUT_SECONDS);
    }
    
    /**
     * Executa um comando Maven no diretório especificado com timeout personalizado.
     * 
     * @param projectPath Caminho do projeto onde executar o comando
     * @param command Comando Maven (ex: "clean install", "test")
     * @param timeoutSeconds Timeout em segundos
     * @return Resultado da execução do comando
     */
    public CommandResult executeMavenCommand(String projectPath, String command, int timeoutSeconds) {
        LOGGER.info("Executando comando Maven: " + command + " em " + projectPath);
        
        CommandResult result = new CommandResult();
        
        try {
            // Verifica se o diretório existe
            File projectDir = new File(projectPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.success = false;
                result.exitCode = -1;
                result.errorOutput = "Diretório do projeto não encontrado: " + projectPath;
                LOGGER.warning("Diretório do projeto não encontrado: " + projectPath);
                return result;
            }
            
            // Detecta qual comando Maven usar
            String mavenCommand = detectMavenCommand(projectDir);
            if (mavenCommand == null) {
                result.success = false;
                result.exitCode = -1;
                result.errorOutput = "Maven não encontrado. Verifique se o Maven está instalado e configurado no PATH, ou se o Maven Wrapper (mvnw) está disponível no projeto.";
                LOGGER.severe("Maven não encontrado no sistema");
                return result;
            }
            
            // Prepara o comando Maven
            String[] commandParts = (mavenCommand + " " + command).split("\\s+");
            ProcessBuilder processBuilder = new ProcessBuilder(commandParts);
            processBuilder.directory(projectDir);
            
            // Configura variáveis de ambiente para evitar travamentos
            processBuilder.environment().put("MAVEN_OPTS", "-Xmx512m");
            
            // Para Maven Wrapper, adiciona variáveis para evitar interação
            if (mavenCommand.contains("mvnw")) {
                processBuilder.environment().put("MAVEN_TERMINAL_OUTPUT", "false");
                processBuilder.environment().put("MAVEN_TERMINAL_USE_COLOR", "false");
                // Força download automático sem interação
                processBuilder.environment().put("MAVEN_WRAPPER_QUIET", "true");
            }
            
            // Inicia o processo
            Process process = processBuilder.start();
            
            // Captura saída padrão e de erro em threads separadas
            Future<String> outputFuture = Executors.newSingleThreadExecutor().submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    return reader.lines().collect(Collectors.joining(System.lineSeparator()));
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Erro ao ler saída padrão", e);
                    return "";
                }
            });
            
            Future<String> errorFuture = Executors.newSingleThreadExecutor().submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    return reader.lines().collect(Collectors.joining(System.lineSeparator()));
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Erro ao ler saída de erro", e);
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
                LOGGER.warning("Timeout na execução do comando Maven: " + command);
                return result;
            }
            
            // Obtém o código de saída
            int exitCode = process.exitValue();
            
            // Obtém as saídas
            String output = outputFuture.get();
            String errorOutput = errorFuture.get();
            
            // Preenche o resultado
            result.exitCode = exitCode;
            result.output = output;
            result.errorOutput = errorOutput;
            result.success = (exitCode == 0);
            
            LOGGER.info("Comando Maven executado com código de saída: " + exitCode);
            
        } catch (IOException e) {
            result.success = false;
            result.exitCode = -1;
            result.errorOutput = "Erro de I/O: " + e.getMessage();
            LOGGER.log(Level.SEVERE, "Erro de I/O ao executar comando Maven", e);
        } catch (InterruptedException e) {
            result.success = false;
            result.exitCode = -1;
            result.errorOutput = "Processo interrompido: " + e.getMessage();
            LOGGER.log(Level.SEVERE, "Processo Maven interrompido", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            result.success = false;
            result.exitCode = -1;
            result.errorOutput = "Erro inesperado: " + e.getMessage();
            LOGGER.log(Level.SEVERE, "Erro inesperado ao executar comando Maven", e);
        }
        
        return result;
    }
    
    /**
     * Detecta qual comando Maven usar, priorizando o Maven do sistema se disponível.
     * 
     * @param projectDir Diretório do projeto
     * @return Comando Maven a ser usado (mvn, mvn.cmd, mvnw, ou mvnw.cmd) ou null se não encontrado
     */
    private String detectMavenCommand(File projectDir) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        
        // Primeiro verifica se o Maven do sistema está disponível (prioridade máxima)
        if (isCommandAvailable("mvn")) {
            LOGGER.info("Usando Maven do sistema (mvn)");
            return "mvn";
        }
        
        // Verifica se o comando 'mvn.cmd' está disponível (Windows)
        if (isWindows && isCommandAvailable("mvn.cmd")) {
            LOGGER.info("Usando Maven do sistema (mvn.cmd)");
            return "mvn.cmd";
        }
        
        // Se não encontrar Maven do sistema, tenta Maven Wrapper (com aviso)
        File mvnw = new File(projectDir, "mvnw");
        File mvnwCmd = new File(projectDir, "mvnw.cmd");
        
        if (isWindows && mvnwCmd.exists()) {
            LOGGER.warning("Maven do sistema não encontrado. Usando Maven Wrapper (pode demorar na primeira execução)");
            return ".\\mvnw.cmd";
        } else if (!isWindows && mvnw.exists() && mvnw.canExecute()) {
            LOGGER.warning("Maven do sistema não encontrado. Usando Maven Wrapper (pode demorar na primeira execução)");
            return "./mvnw";
        } else if (mvnw.exists() && mvnw.canExecute()) {
            LOGGER.warning("Maven do sistema não encontrado. Usando Maven Wrapper (pode demorar na primeira execução)");
            return "./mvnw";
        }
        
        LOGGER.warning("Nenhum comando Maven encontrado");
        return null;
    }
    
    /**
     * Verifica se um comando está disponível no sistema.
     * 
     * @param command Comando a verificar
     * @return true se o comando estiver disponível, false caso contrário
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
    
    /**
     * Executa um comando Maven de teste específico.
     * 
     * @param projectPath Caminho do projeto
     * @return Resultado da execução dos testes
     */
    public CommandResult executeTests(String projectPath) {
        return executeMavenCommand(projectPath, "test");
    }
    
    /**
     * Executa um comando Maven de compilação.
     * 
     * @param projectPath Caminho do projeto
     * @return Resultado da compilação
     */
    public CommandResult executeCompile(String projectPath) {
        return executeMavenCommand(projectPath, "compile");
    }
    
    /**
     * Executa um comando Maven de clean e install.
     * 
     * @param projectPath Caminho do projeto
     * @return Resultado do clean install
     */
    public CommandResult executeCleanInstall(String projectPath) {
        return executeMavenCommand(projectPath, "clean install");
    }
    
    /**
     * Classe que representa o resultado da execução de um comando.
     */
    public static class CommandResult {
        public int exitCode;
        public String output;
        public String errorOutput;
        public boolean success;
        
        @Override
        public String toString() {
            return "CommandResult{" +
                    "exitCode=" + exitCode +
                    ", success=" + success +
                    ", outputLength=" + (output != null ? output.length() : 0) +
                    ", errorOutputLength=" + (errorOutput != null ? errorOutput.length() : 0) +
                    '}';
        }
    }
} 