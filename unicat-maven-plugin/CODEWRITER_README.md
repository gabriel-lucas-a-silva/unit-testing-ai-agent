# CodeWriterTool - Implementação da US6

Este documento descreve a implementação da funcionalidade de escrita de arquivos de teste Java conforme especificado na **US6**.

## 📋 Visão Geral

A `CodeWriterTool` é uma classe utilitária que encapsula a lógica de escrita de arquivos de código Java, permitindo a criação, sobrescrita e adição de conteúdo a arquivos de teste. Esta implementação atende aos critérios de aceitação da US6.

## 🏗️ Estrutura da Implementação

```
unicat-maven-plugin/
└── src/main/java/br/com/unicat/
    ├── tools/
    │   └── CodeWriterTool.java          # Implementação principal da ferramenta
    ├── goals/
    │   ├── TestGeneratorMojo.java       # Goal para gerar arquivos de teste
    │   └── TestAppenderMojo.java        # Goal para adicionar métodos a testes existentes
    └── HelloMojo.java                   # Goal existente
```

## 🔧 Funcionalidades Implementadas

### 1. CodeWriterTool

A classe `CodeWriterTool` implementa todas as sub-tarefas especificadas na US6:

#### Sub-tarefa 6.1.1: `writeCodeFile(path, content)`
- ✅ Cria novos arquivos se não existirem
- ✅ Sobrescreve arquivos existentes
- ✅ Retorna `true`/`false` com logging apropriado
- ✅ Cria diretórios necessários automaticamente

#### Sub-tarefa 6.1.2: `appendCodeToFile(path, content)`
- ✅ Adiciona conteúdo ao final de arquivos existentes
- ✅ Retorna `false` se o arquivo não existir
- ✅ Não sobrescreve conteúdo existente

#### Sub-tarefa 6.1.2.1: `appendToJavaClass(path, content)` (Nova funcionalidade)
- ✅ Adiciona conteúdo **dentro de classes Java**, antes da chave de fechamento
- ✅ Analisa a estrutura do arquivo Java para encontrar o local correto
- ✅ Preserva a sintaxe e estrutura da classe
- ✅ Retorna `false` se o arquivo não existir ou não for uma classe Java válida

#### Sub-tarefa 6.1.3: Criação de diretórios
- ✅ Cria diretórios automaticamente usando `Files.createDirectories()`
- ✅ Suporte a diretórios aninhados

#### Sub-tarefa 6.1.4: Tratamento de exceções
- ✅ Blocos `try-catch` para todas as operações de I/O
- ✅ Logging detalhado usando `java.util.logging`
- ✅ Retorno de status booleano

### 2. Goals do Maven

#### TestGeneratorMojo (`generate-test`)
Gera arquivos de teste Java completos:

```bash
# Gerar teste com parâmetros padrão
mvn br.com.unicat:unicat-maven-plugin:1.0-SNAPSHOT:generate-test

# Gerar teste com parâmetros customizados
mvn br.com.unicat:unicat-maven-plugin:1.0-SNAPSHOT:generate-test \
    -DtestClass=MyCustomTest \
    -DpackageName=br.com.mycompany \
    -DoutputDir=src/test/java
```

#### TestAppenderMojo (`append-test`)
Adiciona métodos de teste a arquivos existentes:

```bash
# Adicionar método de teste
mvn br.com.unicat:unicat-maven-plugin:1.0-SNAPSHOT:append-test \
    -DtestFile=src/test/java/br/com/example/ExampleTest.java \
    -DtestMethodName=testNewFeature
```

## 🧪 Testes Unitários

A implementação inclui testes unitários abrangentes em `CodeWriterToolTest.java` que cobrem:

- ✅ Criação de novos arquivos
- ✅ Sobrescrita de arquivos existentes
- ✅ Criação de diretórios aninhados
- ✅ Adição de conteúdo a arquivos existentes
- ✅ Verificação de existência de arquivos
- ✅ Criação de diretórios
- ✅ Tratamento de arquivos inexistentes

## 📝 Exemplos de Uso

### Exemplo 1: Criar um arquivo de teste

```java
CodeWriterTool codeWriter = new CodeWriterTool();

String testContent = """
package br.com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {
    @Test
    void testAddition() {
        assertEquals(4, 2 + 2);
    }
}
""";

boolean success = codeWriter.writeCodeFile("src/test/java/br/com/example/CalculatorTest.java", testContent);
```

### Exemplo 2: Adicionar método a teste existente

```java
CodeWriterTool codeWriter = new CodeWriterTool();

String newMethod = """
    
    @Test
    void testSubtraction() {
        assertEquals(2, 4 - 2);
    }
""";

// Método antigo (adiciona ao final do arquivo)
boolean success1 = codeWriter.appendCodeToFile("src/test/java/br/com/example/CalculatorTest.java", newMethod);

// Método novo (adiciona dentro da classe Java)
boolean success2 = codeWriter.appendToJavaClass("src/test/java/br/com/example/CalculatorTest.java", newMethod);
```

## 🚀 Como Executar

### 1. Compilar o Plugin

```bash
cd unicat-maven-plugin
mvn clean install
```

### 2. Executar os Goals

```bash
# No projeto consumidor
cd test-plugin-consumer

# Gerar arquivo de teste
mvn br.com.unicat:unicat-maven-plugin:1.0-SNAPSHOT:generate-test

# Adicionar método a teste existente
mvn br.com.unicat:unicat-maven-plugin:1.0-SNAPSHOT:append-test \
    -DtestFile=src/test/java/br/com/example/ExampleTest.java
```

## ✅ Critérios de Aceitação Atendidos

- ✅ O agente cria novos arquivos .java no diretório de testes
- ✅ O agente sobrescreve o conteúdo de arquivos de teste existentes quando necessário
- ✅ O agente pode anexar novos métodos de teste a arquivos existentes
- ✅ **CORRIGIDO**: Métodos são inseridos **dentro da classe Java**, não fora dela
- ✅ Tratamento robusto de exceções de I/O
- ✅ Criação automática de diretórios
- ✅ Logging apropriado para depuração
- ✅ Retorno de status para tomada de decisão

## 🔄 Integração com Agente de IA

A `CodeWriterTool` está pronta para ser integrada com um agente de IA que pode:

1. **Analisar código fonte** e gerar testes unitários
2. **Usar `writeCodeFile()`** para criar novos arquivos de teste
3. **Usar `appendToJavaClass()`** para adicionar métodos **dentro de classes Java**
4. **Usar `appendCodeToFile()`** para adicionar conteúdo ao final de arquivos
5. **Verificar existência** de arquivos antes de operações
6. **Criar estrutura de diretórios** automaticamente

## 🐛 Correção Implementada

### Problema Original
O método `appendCodeToFile` adicionava conteúdo ao final do arquivo, o que resultava em métodos sendo inseridos **fora da classe Java**, tornando o código inválido.

### Solução Implementada
- **Novo método `appendToJavaClass()`**: Analisa a estrutura do arquivo Java e insere o conteúdo antes da chave de fechamento da classe
- **Algoritmo robusto**: Detecta a declaração de classe e conta chaves para encontrar o local correto de inserção
- **Preservação da sintaxe**: Mantém a estrutura e formatação da classe intacta

### Exemplo de Correção

**Antes (Inválido):**
```java
public class ExampleTest {
    // métodos existentes
}
    @Test  // ← Método fora da classe!
    void testNewMethod() {
        // ...
    }
```

**Depois (Válido):**
```java
public class ExampleTest {
    // métodos existentes
    
    @Test  // ← Método dentro da classe!
    void testNewMethod() {
        // ...
    }
}
```

## 📚 Dependências

- Java 8+
- Maven 3.6+
- JUnit 5 (para testes unitários)

## 🐛 Solução de Problemas

### Erro de permissão
Certifique-se de que o diretório de destino tenha permissões de escrita.

### Arquivo não encontrado para append
Use `fileExists()` para verificar se o arquivo existe antes de usar `appendCodeToFile()`.

### Encoding de caracteres
O plugin usa UTF-8 por padrão. Para outros encodings, modifique a implementação conforme necessário. 