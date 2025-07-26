package br.com.unicat.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Ferramenta para escrita de arquivos de código Java.
 * Permite criar, sobrescrever e adicionar conteúdo a arquivos de teste.
 */
public class CodeWriterTool {
    
    private static final Logger LOGGER = Logger.getLogger(CodeWriterTool.class.getName());
    
    /**
     * Cria um novo arquivo ou sobrescreve um arquivo existente com o conteúdo fornecido.
     * 
     * @param filePath Caminho do arquivo a ser criado/sobrescrito
     * @param content Conteúdo a ser escrito no arquivo
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean writeCodeFile(String filePath, String content) {
        Path path = Paths.get(filePath);
        
        try {
            // Garante que o diretório pai exista
            Files.createDirectories(path.getParent());
            
            // Escreve o conteúdo no arquivo (cria se não existir, sobrescreve se existir)
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            LOGGER.info("Arquivo " + filePath + " escrito com sucesso.");
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao escrever o arquivo " + filePath + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Adiciona conteúdo ao final de um arquivo existente.
     * 
     * @param filePath Caminho do arquivo ao qual adicionar conteúdo
     * @param content Conteúdo a ser adicionado ao final do arquivo
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean appendCodeToFile(String filePath, String content) {
        Path path = Paths.get(filePath);
        
        // Verifica se o arquivo existe
        if (!Files.exists(path)) {
            LOGGER.warning("Erro: Arquivo " + filePath + " não existe para append.");
            return false;
        }
        
        try {
            // Adiciona o conteúdo ao final do arquivo
            Files.writeString(path, content, StandardOpenOption.APPEND);
            
            LOGGER.info("Conteúdo adicionado ao arquivo " + filePath + " com sucesso.");
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar conteúdo ao arquivo " + filePath + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Adiciona conteúdo dentro de uma classe Java, antes da chave de fechamento.
     * 
     * @param filePath Caminho do arquivo Java ao qual adicionar conteúdo
     * @param content Conteúdo a ser adicionado dentro da classe
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean appendToJavaClass(String filePath, String content) {
        Path path = Paths.get(filePath);
        
        // Verifica se o arquivo existe
        if (!Files.exists(path)) {
            LOGGER.warning("Erro: Arquivo " + filePath + " não existe para append.");
            return false;
        }
        
        try {
            // Lê o conteúdo atual do arquivo
            String fileContent = Files.readString(path);
            
            // Encontra a última chave de fechamento da classe
            int lastClosingBraceIndex = findLastClassClosingBrace(fileContent);
            
            if (lastClosingBraceIndex == -1) {
                LOGGER.warning("Erro: Não foi possível encontrar a chave de fechamento da classe no arquivo " + filePath);
                return false;
            }
            
            // Insere o conteúdo antes da chave de fechamento
            String newContent = fileContent.substring(0, lastClosingBraceIndex) + 
                               content + 
                               fileContent.substring(lastClosingBraceIndex);
            
            // Escreve o novo conteúdo no arquivo
            Files.writeString(path, newContent, StandardOpenOption.TRUNCATE_EXISTING);
            
            LOGGER.info("Conteúdo adicionado à classe Java no arquivo " + filePath + " com sucesso.");
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar conteúdo à classe Java no arquivo " + filePath + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Encontra a posição da última chave de fechamento de classe no código Java.
     * 
     * @param fileContent Conteúdo do arquivo Java
     * @return Índice da última chave de fechamento de classe, ou -1 se não encontrada
     */
    private int findLastClassClosingBrace(String fileContent) {
        // Divide o conteúdo em linhas para análise mais precisa
        String[] lines = fileContent.split("\n");
        int braceCount = 0;
        boolean insideClass = false;
        int lastClassClosingBrace = -1;
        int currentPosition = 0;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Verifica se a linha contém declaração de classe
            if (trimmedLine.matches(".*\\b(public\\s+)?class\\s+\\w+.*\\{.*")) {
                insideClass = true;
                braceCount = 1; // Começa a contar chaves
            } else if (insideClass) {
                // Conta chaves de abertura e fechamento
                for (char c : line.toCharArray()) {
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            // Encontrou o fim da classe
                            lastClassClosingBrace = currentPosition + line.indexOf('}');
                            break;
                        }
                    }
                }
            }
            
            currentPosition += line.length() + 1; // +1 para a quebra de linha
        }
        
        return lastClassClosingBrace;
    }
    
    /**
     * Verifica se um arquivo existe.
     * 
     * @param filePath Caminho do arquivo a ser verificado
     * @return true se o arquivo existe, false caso contrário
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Cria um diretório e todos os diretórios pai necessários.
     * 
     * @param directoryPath Caminho do diretório a ser criado
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean createDirectory(String directoryPath) {
        try {
            Files.createDirectories(Paths.get(directoryPath));
            LOGGER.info("Diretório " + directoryPath + " criado com sucesso.");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar diretório " + directoryPath + ": " + e.getMessage(), e);
            return false;
        }
    }
} 