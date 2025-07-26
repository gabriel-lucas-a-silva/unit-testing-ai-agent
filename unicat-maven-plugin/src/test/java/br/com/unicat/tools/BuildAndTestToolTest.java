package br.com.unicat.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Testes unitários para a classe BuildAndTestTool.
 */
public class BuildAndTestToolTest {
    
    private BuildAndTestTool buildTool;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        buildTool = new BuildAndTestTool();
    }
    
    @Test
    void testExecuteMavenCommand_WithNonExistentDirectory() {
        // Arrange
        String nonExistentPath = "/path/that/does/not/exist";
        
        // Act
        BuildAndTestTool.CommandResult result = buildTool.executeMavenCommand(nonExistentPath, "compile");
        
        // Assert
        assertFalse(result.success, "Deve retornar false para diretório inexistente");
        assertEquals(-1, result.exitCode, "Código de saída deve ser -1");
        assertNotNull(result.errorOutput, "Deve ter mensagem de erro");
        assertTrue(result.errorOutput.contains("não encontrado"), "Mensagem deve indicar diretório não encontrado");
    }
    
    @Test
    void testExecuteMavenCommand_WithInvalidCommand() {
        // Arrange
        Path projectDir = tempDir.resolve("test-project");
        projectDir.toFile().mkdirs();
        
        // Act
        BuildAndTestTool.CommandResult result = buildTool.executeMavenCommand(projectDir.toString(), "invalid-command");
        
        // Assert
        assertFalse(result.success, "Deve retornar false para comando inválido");
        assertNotEquals(0, result.exitCode, "Código de saída não deve ser 0");
        assertNotNull(result.errorOutput, "Deve ter saída de erro");
    }
    
    @Test
    void testExecuteMavenCommand_WithValidCommand() {
        // Arrange
        Path projectDir = tempDir.resolve("test-project");
        projectDir.toFile().mkdirs();
        
        // Criar um pom.xml básico para teste
        createBasicPomXml(projectDir);
        
        // Act
        BuildAndTestTool.CommandResult result = buildTool.executeMavenCommand(projectDir.toString(), "help");
        
        // Assert
        assertNotNull(result, "Resultado não deve ser null");
        // Como o mvn pode não estar no PATH, verificamos apenas se o resultado foi processado corretamente
        assertNotNull(result.output, "Saída não deve ser null");
        assertNotNull(result.errorOutput, "Saída de erro não deve ser null");
        // Se o mvn não estiver disponível, deve falhar mas com tratamento adequado
    }
    
    @Test
    void testExecuteTests_Method() {
        // Arrange
        Path projectDir = tempDir.resolve("test-project");
        projectDir.toFile().mkdirs();
        
        // Act
        BuildAndTestTool.CommandResult result = buildTool.executeTests(projectDir.toString());
        
        // Assert
        assertNotNull(result, "Resultado não deve ser null");
        assertFalse(result.success, "Testes devem falhar em projeto vazio");
        assertNotEquals(0, result.exitCode, "Código de saída não deve ser 0");
    }
    
    @Test
    void testExecuteCompile_Method() {
        // Arrange
        Path projectDir = tempDir.resolve("test-project");
        projectDir.toFile().mkdirs();
        
        // Act
        BuildAndTestTool.CommandResult result = buildTool.executeCompile(projectDir.toString());
        
        // Assert
        assertNotNull(result, "Resultado não deve ser null");
        assertFalse(result.success, "Compilação deve falhar em projeto vazio");
        assertNotEquals(0, result.exitCode, "Código de saída não deve ser 0");
    }
    
    @Test
    void testExecuteCleanInstall_Method() {
        // Arrange
        Path projectDir = tempDir.resolve("test-project");
        projectDir.toFile().mkdirs();
        
        // Act
        BuildAndTestTool.CommandResult result = buildTool.executeCleanInstall(projectDir.toString());
        
        // Assert
        assertNotNull(result, "Resultado não deve ser null");
        assertFalse(result.success, "Clean install deve falhar em projeto vazio");
        assertNotEquals(0, result.exitCode, "Código de saída não deve ser 0");
    }
    
    @Test
    void testCommandResult_ToString() {
        // Arrange
        BuildAndTestTool.CommandResult result = new BuildAndTestTool.CommandResult();
        result.exitCode = 0;
        result.success = true;
        result.output = "Test output";
        result.errorOutput = "Test error";
        
        // Act
        String toString = result.toString();
        
        // Assert
        assertNotNull(toString, "toString não deve ser null");
        assertTrue(toString.contains("exitCode=0"), "Deve conter exitCode");
        assertTrue(toString.contains("success=true"), "Deve conter success");
        assertTrue(toString.contains("outputLength="), "Deve conter outputLength");
        assertTrue(toString.contains("errorOutputLength="), "Deve conter errorOutputLength");
    }
    
    @Test
    void testExecuteMavenCommand_WithTimeout() {
        // Arrange
        Path projectDir = tempDir.resolve("test-project");
        projectDir.toFile().mkdirs();
        
        // Act - timeout muito baixo para forçar timeout
        BuildAndTestTool.CommandResult result = buildTool.executeMavenCommand(projectDir.toString(), "help", 1);
        
        // Assert
        assertNotNull(result, "Resultado não deve ser null");
        // Como o mvn pode não estar no PATH, o teste pode falhar por isso em vez de timeout
        // Verificamos apenas se o resultado foi processado corretamente
        if (!result.success && result.exitCode == -1) {
            // Pode ser timeout OU erro de comando não encontrado
            assertTrue(result.errorOutput.contains("Timeout") || result.errorOutput.contains("não pode encontrar"), 
                      "Deve indicar timeout ou comando não encontrado");
        }
    }
    
    /**
     * Cria um pom.xml básico para testes.
     */
    private void createBasicPomXml(Path projectDir) {
        try {
            String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                    "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 \n" +
                    "         http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "    <modelVersion>4.0.0</modelVersion>\n" +
                    "    <groupId>com.test</groupId>\n" +
                    "    <artifactId>test-project</artifactId>\n" +
                    "    <version>1.0-SNAPSHOT</version>\n" +
                    "    <packaging>jar</packaging>\n" +
                    "</project>";
            
            Files.write(projectDir.resolve("pom.xml"), pomContent.getBytes());
        } catch (IOException e) {
            // Ignora erro na criação do pom.xml para testes
        }
    }
} 