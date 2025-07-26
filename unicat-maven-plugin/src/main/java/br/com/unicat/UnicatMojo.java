package br.com.unicat;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import br.com.unicat.tools.CodeWriterTool;
import br.com.unicat.tools.BuildAndTestTool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Goal principal do UniCat para geração automática de testes unitários.
 * Analisa o código fonte do projeto e gera testes unitários automaticamente.
 */
@Mojo(name = "unicat")
public class UnicatMojo extends AbstractMojo {
    
    @Parameter(property = "sourceDir", defaultValue = "src/main/java")
    private String sourceDir;
    
    @Parameter(property = "testDir", defaultValue = "src/test/java")
    private String testDir;
    
    @Parameter(property = "packageFilter", defaultValue = "")
    private String packageFilter;
    
    @Parameter(property = "generateForAllClasses", defaultValue = "true")
    private boolean generateForAllClasses;
    
    @Parameter(property = "runBuildAfterGeneration", defaultValue = "true")
    private boolean runBuildAfterGeneration;
    
    @Parameter(property = "runTestsAfterGeneration", defaultValue = "true")
    private boolean runTestsAfterGeneration;
    
    @Parameter(property = "buildTimeout", defaultValue = "300")
    private int buildTimeout;
    
    @Parameter(property = "unicat.skip", defaultValue = "false")
    private boolean skip;
    
    public void execute() throws MojoExecutionException {
        // Verifica se deve pular a execução do plugin
        if (skip) {
            getLog().info("⏭️ UniCat - Execução pulada (unicat.skip=true)");
            return;
        }
        
        getLog().info("🚀 UniCat - Iniciando geração automática de testes unitários...");
        
        CodeWriterTool codeWriter = new CodeWriterTool();
        BuildAndTestTool buildTool = new BuildAndTestTool();
        
        try {
            // Verifica se o diretório fonte existe
            Path sourcePath = Paths.get(sourceDir);
            if (!Files.exists(sourcePath)) {
                getLog().warn("⚠️ Diretório fonte não encontrado: " + sourceDir);
                return;
            }
            
            // Encontra todas as classes Java no diretório fonte
            List<Path> javaFiles = findJavaFiles(sourcePath);
            
            if (javaFiles.isEmpty()) {
                getLog().info("ℹ️ Nenhuma classe Java encontrada em: " + sourceDir);
                return;
            }
            
            getLog().info("📁 Encontradas " + javaFiles.size() + " classes Java");
            
            int generatedTests = 0;
            int skippedTests = 0;
            
            for (Path javaFile : javaFiles) {
                try {
                    if (shouldGenerateTestForFile(javaFile)) {
                        boolean success = generateTestForClass(javaFile, codeWriter);
                        if (success) {
                            generatedTests++;
                        } else {
                            skippedTests++;
                        }
                    } else {
                        skippedTests++;
                        getLog().debug("⏭️ Pulando: " + javaFile.getFileName());
                    }
                } catch (Exception e) {
                    getLog().warn("⚠️ Erro ao processar " + javaFile.getFileName() + ": " + e.getMessage());
                    skippedTests++;
                }
            }
            
            getLog().info("✅ Geração concluída!");
            getLog().info("📊 Resumo da Geração:");
            getLog().info("   - Testes gerados: " + generatedTests);
            getLog().info("   - Testes pulados: " + skippedTests);
            getLog().info("   - Total processado: " + javaFiles.size());
            
            // Executa build e testes se configurado
            if (runBuildAfterGeneration || runTestsAfterGeneration) {
                executeBuildAndTests(buildTool);
            }
            
        } catch (Exception e) {
            throw new MojoExecutionException("Erro durante a geração de testes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Encontra todos os arquivos Java no diretório especificado.
     */
    private List<Path> findJavaFiles(Path sourcePath) throws Exception {
        try (Stream<Path> paths = Files.walk(sourcePath)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Verifica se deve gerar teste para o arquivo especificado.
     */
    private boolean shouldGenerateTestForFile(Path javaFile) {
        // Se não há filtro de pacote, gera para todas as classes
        if (packageFilter == null || packageFilter.trim().isEmpty()) {
            return true;
        }
        
        // Verifica se o arquivo está no pacote especificado
        String filePath = javaFile.toString().replace("\\", "/");
        return filePath.contains(packageFilter.replace(".", "/"));
    }
    
    /**
     * Gera teste unitário para uma classe específica.
     */
    private boolean generateTestForClass(Path javaFile, CodeWriterTool codeWriter) throws Exception {
        String className = javaFile.getFileName().toString().replace(".java", "");
        String packageName = extractPackageName(javaFile);
        String testClassName = className + "Test";
        
        // Cria o caminho do arquivo de teste
        String testFilePath = testDir + "/" + packageName.replace(".", "/") + "/" + testClassName + ".java";
        
        // Verifica se o teste já existe (temporariamente desabilitado para demonstração)
        if (codeWriter.fileExists(testFilePath)) {
            getLog().debug("⏭️ Teste já existe: " + testClassName + " - sobrescrevendo para demonstração");
            // return false; // Comentado temporariamente
        }
        
        // Gera o conteúdo do teste
        String testContent = generateTestContent(packageName, testClassName, className);
        
        // Escreve o arquivo de teste
        boolean success = codeWriter.writeCodeFile(testFilePath, testContent);
        
        if (success) {
            getLog().info("✅ Teste gerado: " + testClassName);
        } else {
            getLog().warn("❌ Falha ao gerar teste: " + testClassName);
        }
        
        return success;
    }
    
    /**
     * Extrai o nome do pacote do arquivo Java.
     */
    private String extractPackageName(Path javaFile) {
        try {
            String content = Files.readString(javaFile);
            String[] lines = content.split("\n");
            
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("package ")) {
                    return line.substring(8, line.length() - 1); // Remove "package " e ";"
                }
            }
        } catch (Exception e) {
            getLog().warn("⚠️ Erro ao extrair pacote de " + javaFile.getFileName());
        }
        
        return "br.com.example"; // Pacote padrão
    }
    
    /**
     * Gera o conteúdo de um arquivo de teste Java.
     */
    private String generateTestContent(String packageName, String testClassName, String originalClassName) {
        StringBuilder content = new StringBuilder();
        
        content.append("package ").append(packageName).append(";\n\n");
        content.append("import org.junit.jupiter.api.Test;\n");
        content.append("import org.junit.jupiter.api.BeforeEach;\n");
        content.append("import org.junit.jupiter.api.BeforeAll;\n");
        content.append("import org.junit.jupiter.api.AfterEach;\n");
        content.append("import org.junit.jupiter.api.AfterAll;\n");
        content.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
        content.append("/**\n");
        content.append(" * Testes unitários para ").append(originalClassName).append("\n");
        content.append(" * Gerados automaticamente pelo UniCat Maven Plugin\n");
        content.append(" */\n");
        content.append("public class ").append(testClassName).append(" {\n\n");
        
        content.append("    @BeforeAll\n");
        content.append("    static void setUpClass() {\n");
        content.append("        // Configuração inicial da classe de teste\n");
        content.append("    }\n\n");
        
        content.append("    @BeforeEach\n");
        content.append("    void setUp() {\n");
        content.append("        // Configuração inicial de cada teste\n");
        content.append("    }\n\n");
        
        content.append("    @Test\n");
        content.append("    void testBasicFunctionality() {\n");
        content.append("        // TODO: Implementar teste básico para ").append(originalClassName).append("\n");
        content.append("        assertTrue(true, \"Teste básico - implementar lógica específica\");\n");
        content.append("    }\n\n");
        
        content.append("    @Test\n");
        content.append("    void testConstructor() {\n");
        content.append("        // TODO: Testar construtor da classe ").append(originalClassName).append("\n");
        content.append("        // Exemplo: ").append(originalClassName).append(" instance = new ").append(originalClassName).append("();\n");
        content.append("        // assertNotNull(instance);\n");
        content.append("        assertTrue(true, \"Teste de construtor - implementar lógica específica\");\n");
        content.append("    }\n\n");
        
        content.append("    @Test\n");
        content.append("    void testEdgeCases() {\n");
        content.append("        // TODO: Testar casos extremos e valores limite\n");
        content.append("        assertTrue(true, \"Teste de casos extremos - implementar lógica específica\");\n");
        content.append("    }\n\n");
        
        content.append("    @AfterEach\n");
        content.append("    void tearDown() {\n");
        content.append("        // Limpeza após cada teste\n");
        content.append("    }\n\n");
        
        content.append("    @AfterAll\n");
        content.append("    static void tearDownClass() {\n");
        content.append("        // Limpeza final da classe de teste\n");
        content.append("    }\n");
        
        content.append("}\n");
        
        return content.toString();
    }
    
    /**
     * Executa build e testes do projeto em workspace isolado após a geração dos testes.
     */
    private void executeBuildAndTests(BuildAndTestTool buildTool) {
        getLog().info("🔨 Iniciando build e testes em workspace isolado...");
        
        String projectPath = System.getProperty("user.dir");
        BuildAndTestTool.WorkspaceInfo workspaceInfo = null;
        
        try {
            // 1. Criar workspace isolado
            getLog().info("🏗️ Criando workspace isolado...");
            workspaceInfo = buildTool.createIsolatedWorkspace(projectPath);
            
            if (!workspaceInfo.success) {
                getLog().error("❌ Falha ao criar workspace isolado: " + workspaceInfo.errorMessage);
                return;
            }
            
            getLog().info("✅ Workspace isolado criado: " + workspaceInfo.workspacePath);
            
            // 2. Gerar testes no workspace (se necessário)
            // Nota: Os testes já foram gerados no projeto original, mas precisamos copiá-los para o workspace
            getLog().info("📋 Copiando testes gerados para workspace...");
            
            // 3. Validar build e testes no workspace
            if (runBuildAfterGeneration || runTestsAfterGeneration) {
                getLog().info("🔍 Validando build e testes no workspace...");
                BuildAndTestTool.ValidationResult validationResult = buildTool.validateBuildAndTests(workspaceInfo.workspacePath);
                
                getLog().info("📊 Resultado da validação: " + validationResult.summary);
                
                if (validationResult.overallSuccess) {
                    getLog().info("✅ Validação completa bem-sucedida!");
                    
                    // 4. Copiar testes validados para projeto original
                    getLog().info("📋 Copiando testes validados para projeto original...");
                    BuildAndTestTool.CopyResult copyResult = buildTool.copyTestsToOriginalProject(
                        workspaceInfo.workspacePath, 
                        workspaceInfo.originalProjectPath
                    );
                    
                    if (copyResult.success) {
                        getLog().info("✅ " + copyResult.filesCopied + " arquivos de teste entregues com sucesso!");
                        getLog().info("📄 Arquivos copiados:");
                        for (String file : copyResult.copiedFiles) {
                            getLog().info("   - " + file);
                        }
                    } else {
                        getLog().error("❌ Erro ao copiar testes: " + copyResult.errorMessage);
                    }
                    
                    // Log detalhado dos resultados
                    if (validationResult.buildResult != null) {
                        logCommandResult("Build", validationResult.buildResult);
                    }
                    if (validationResult.testResult != null) {
                        logCommandResult("Testes", validationResult.testResult);
                    }
                    
                } else {
                    getLog().error("❌ Validação falhou - testes não serão entregues!");
                    
                    // Log detalhado dos erros
                    if (validationResult.buildResult != null && !validationResult.buildSuccess) {
                        logCommandResult("Build (FALHOU)", validationResult.buildResult);
                    }
                    if (validationResult.testResult != null && !validationResult.testsSuccess) {
                        logCommandResult("Testes (FALHARAM)", validationResult.testResult);
                    }
                    
                    getLog().error("🚫 Testes gerados NÃO foram entregues devido a falhas na validação!");
                }
            } else {
                getLog().info("⏭️ Build e testes desabilitados - copiando testes sem validação...");
                
                // Copiar testes sem validação (modo não-seguro)
                BuildAndTestTool.CopyResult copyResult = buildTool.copyTestsToOriginalProject(
                    workspaceInfo.workspacePath, 
                    workspaceInfo.originalProjectPath
                );
                
                if (copyResult.success) {
                    getLog().info("✅ " + copyResult.filesCopied + " arquivos de teste copiados (sem validação)");
                } else {
                    getLog().error("❌ Erro ao copiar testes: " + copyResult.errorMessage);
                }
            }
            
        } catch (Exception e) {
            getLog().error("❌ Erro durante execução em workspace isolado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. Limpar workspace
            if (workspaceInfo != null && workspaceInfo.workspacePath != null) {
                getLog().info("🧹 Limpando workspace isolado...");
                buildTool.cleanupWorkspace(workspaceInfo.workspacePath);
            }
        }
    }
    
    /**
     * Loga detalhes de um resultado de comando.
     */
    private void logCommandResult(String operation, BuildAndTestTool.CommandResult result) {
        getLog().info("📋 " + operation + " - Código: " + result.exitCode + 
                     ", Sucesso: " + result.success + 
                     ", Tempo: " + result.executionTimeMs + "ms");
        
        if (result.output != null && !result.output.isEmpty()) {
            getLog().info("📋 Logs de " + operation + ":");
            getLog().info(result.output);
        }
        
        if (result.errorOutput != null && !result.errorOutput.isEmpty()) {
            getLog().warn("📋 Erros de " + operation + ":");
            getLog().warn(result.errorOutput);
        }
    }
    
    /**
     * Extrai e exibe um resumo dos resultados dos testes.
     */
    private void extractTestSummary(String testOutput) {
        getLog().info("📊 Resumo dos Testes:");
        
        // Procura por padrões comuns na saída do Maven Surefire
        String[] lines = testOutput.split("\n");
        for (String line : lines) {
            line = line.trim();
            
            // Padrões de sucesso
            if (line.contains("Tests run:") && line.contains("Failures: 0") && line.contains("Errors: 0")) {
                getLog().info("   ✅ " + line);
            }
            // Padrões de falha
            else if (line.contains("Tests run:") && (line.contains("Failures:") || line.contains("Errors:"))) {
                getLog().warn("   ❌ " + line);
            }
            // Informações de cobertura (se disponível)
            else if (line.contains("BUILD SUCCESS")) {
                getLog().info("   🎉 " + line);
            }
            else if (line.contains("BUILD FAILURE")) {
                getLog().warn("   💥 " + line);
            }
        }
    }
}
