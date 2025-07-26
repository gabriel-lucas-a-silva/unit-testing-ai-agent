package br.com.unicat.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Testes unitários para a classe CodeWriterTool.
 */
public class CodeWriterToolTest {
    
    private CodeWriterTool codeWriter;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        codeWriter = new CodeWriterTool();
    }
    
    @Test
    void testWriteCodeFile_CreatesNewFile() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.java");
        String content = "public class Test {}";
        
        // Act
        boolean result = codeWriter.writeCodeFile(testFile.toString(), content);
        
        // Assert
        assertTrue(result, "Deve retornar true quando o arquivo é criado com sucesso");
        assertTrue(Files.exists(testFile), "Arquivo deve ser criado");
        assertEquals(content, Files.readString(testFile), "Conteúdo deve ser escrito corretamente");
    }
    
    @Test
    void testWriteCodeFile_OverwritesExistingFile() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.java");
        String originalContent = "public class Original {}";
        String newContent = "public class New {}";
        
        // Criar arquivo original
        Files.writeString(testFile, originalContent);
        
        // Act
        boolean result = codeWriter.writeCodeFile(testFile.toString(), newContent);
        
        // Assert
        assertTrue(result, "Deve retornar true quando o arquivo é sobrescrito com sucesso");
        assertEquals(newContent, Files.readString(testFile), "Conteúdo deve ser sobrescrito");
    }
    
    @Test
    void testWriteCodeFile_CreatesDirectories() throws IOException {
        // Arrange
        Path nestedDir = tempDir.resolve("nested").resolve("deep").resolve("directory");
        Path testFile = nestedDir.resolve("test.java");
        String content = "public class Test {}";
        
        // Act
        boolean result = codeWriter.writeCodeFile(testFile.toString(), content);
        
        // Assert
        assertTrue(result, "Deve retornar true quando os diretórios são criados e o arquivo é escrito");
        assertTrue(Files.exists(nestedDir), "Diretórios aninhados devem ser criados");
        assertTrue(Files.exists(testFile), "Arquivo deve ser criado no diretório aninhado");
    }
    
    @Test
    void testAppendCodeToFile_AppendsToExistingFile() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.java");
        String originalContent = "public class Test {\n";
        String appendContent = "    public void method() {}\n}";
        
        // Criar arquivo original
        Files.writeString(testFile, originalContent);
        
        // Act
        boolean result = codeWriter.appendCodeToFile(testFile.toString(), appendContent);
        
        // Assert
        assertTrue(result, "Deve retornar true quando o conteúdo é adicionado com sucesso");
        String expectedContent = originalContent + appendContent;
        assertEquals(expectedContent, Files.readString(testFile), "Conteúdo deve ser anexado corretamente");
    }
    
    @Test
    void testAppendCodeToFile_ReturnsFalseForNonExistentFile() {
        // Arrange
        Path nonExistentFile = tempDir.resolve("nonexistent.java");
        
        // Act
        boolean result = codeWriter.appendCodeToFile(nonExistentFile.toString(), "content");
        
        // Assert
        assertFalse(result, "Deve retornar false quando o arquivo não existe");
    }
    
    @Test
    void testFileExists_ReturnsTrueForExistingFile() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.java");
        Files.writeString(testFile, "content");
        
        // Act
        boolean result = codeWriter.fileExists(testFile.toString());
        
        // Assert
        assertTrue(result, "Deve retornar true para arquivo existente");
    }
    
    @Test
    void testFileExists_ReturnsFalseForNonExistentFile() {
        // Arrange
        Path nonExistentFile = tempDir.resolve("nonexistent.java");
        
        // Act
        boolean result = codeWriter.fileExists(nonExistentFile.toString());
        
        // Assert
        assertFalse(result, "Deve retornar false para arquivo inexistente");
    }
    
    @Test
    void testCreateDirectory_CreatesSingleDirectory() {
        // Arrange
        Path newDir = tempDir.resolve("newdirectory");
        
        // Act
        boolean result = codeWriter.createDirectory(newDir.toString());
        
        // Assert
        assertTrue(result, "Deve retornar true quando o diretório é criado com sucesso");
        assertTrue(Files.exists(newDir), "Diretório deve ser criado");
        assertTrue(Files.isDirectory(newDir), "Caminho deve ser um diretório");
    }
    
    @Test
    void testCreateDirectory_CreatesNestedDirectories() {
        // Arrange
        Path nestedDir = tempDir.resolve("level1").resolve("level2").resolve("level3");
        
        // Act
        boolean result = codeWriter.createDirectory(nestedDir.toString());
        
        // Assert
        assertTrue(result, "Deve retornar true quando os diretórios aninhados são criados com sucesso");
        assertTrue(Files.exists(nestedDir), "Diretórios aninhados devem ser criados");
        assertTrue(Files.isDirectory(nestedDir), "Caminho deve ser um diretório");
    }
    
    @Test
    void testAppendToJavaClass_AddsContentInsideClass() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("TestClass.java");
        String originalContent = "package com.example;\n\n" +
                               "public class TestClass {\n" +
                               "    public void existingMethod() {\n" +
                               "        // existing code\n" +
                               "    }\n" +
                               "}\n";
        String newMethod = "\n" +
                          "    @Test\n" +
                          "    void testNewMethod() {\n" +
                          "        assertTrue(true);\n" +
                          "    }\n";
        
        // Criar arquivo original
        Files.writeString(testFile, originalContent);
        
        // Act
        boolean result = codeWriter.appendToJavaClass(testFile.toString(), newMethod);
        
        // Assert
        assertTrue(result, "Deve retornar true quando o conteúdo é adicionado à classe com sucesso");
        String finalContent = Files.readString(testFile);
        assertTrue(finalContent.contains("testNewMethod"), "Novo método deve estar presente");
        assertTrue(finalContent.contains("public class TestClass"), "Classe original deve ser preservada");
        assertTrue(finalContent.contains("existingMethod"), "Método existente deve ser preservado");
        // Verifica se o arquivo termina com chave de fechamento (pode ter espaços em branco)
        assertTrue(finalContent.trim().endsWith("}"), "Arquivo deve terminar com chave de fechamento");
    }
    
    @Test
    void testAppendToJavaClass_ReturnsFalseForNonExistentFile() {
        // Arrange
        Path nonExistentFile = tempDir.resolve("nonexistent.java");
        
        // Act
        boolean result = codeWriter.appendToJavaClass(nonExistentFile.toString(), "content");
        
        // Assert
        assertFalse(result, "Deve retornar false quando o arquivo não existe");
    }
    
    @Test
    void testAppendToJavaClass_HandlesComplexClassStructure() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("ComplexClass.java");
        String originalContent = "package com.example;\n\n" +
                               "public class ComplexClass {\n" +
                               "    private String field;\n" +
                               "    \n" +
                               "    public ComplexClass() {\n" +
                               "        this.field = \"test\";\n" +
                               "    }\n" +
                               "    \n" +
                               "    public void method1() {\n" +
                               "        if (true) {\n" +
                               "            // nested block\n" +
                               "        }\n" +
                               "    }\n" +
                               "    \n" +
                               "    public void method2() {\n" +
                               "        // another method\n" +
                               "    }\n" +
                               "}\n";
        String newMethod = "\n" +
                          "    @Test\n" +
                          "    void testComplexMethod() {\n" +
                          "        ComplexClass obj = new ComplexClass();\n" +
                          "        assertNotNull(obj);\n" +
                          "    }\n";
        
        // Criar arquivo original
        Files.writeString(testFile, originalContent);
        
        // Act
        boolean result = codeWriter.appendToJavaClass(testFile.toString(), newMethod);
        
        // Assert
        assertTrue(result, "Deve retornar true para estrutura de classe complexa");
        String finalContent = Files.readString(testFile);
        assertTrue(finalContent.contains("testComplexMethod"), "Novo método deve estar presente");
        assertTrue(finalContent.contains("method1"), "Métodos existentes devem ser preservados");
        assertTrue(finalContent.contains("method2"), "Métodos existentes devem ser preservados");
    }
} 