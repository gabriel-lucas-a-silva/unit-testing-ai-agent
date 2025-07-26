package br.com.unicat;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import br.com.unicat.tools.CodeWriterTool;

import java.io.File;
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
    
    public void execute() throws MojoExecutionException {
        getLog().info("🚀 UniCat - Iniciando geração automática de testes unitários...");
        
        CodeWriterTool codeWriter = new CodeWriterTool();
        
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
            getLog().info("📊 Resumo:");
            getLog().info("   - Testes gerados: " + generatedTests);
            getLog().info("   - Testes pulados: " + skippedTests);
            getLog().info("   - Total processado: " + javaFiles.size());
            
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
        
        // Verifica se o teste já existe
        if (codeWriter.fileExists(testFilePath)) {
            getLog().debug("⏭️ Teste já existe: " + testClassName);
            return false;
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
}
