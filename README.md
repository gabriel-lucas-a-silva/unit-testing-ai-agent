# UniCat Maven Plugin - Projeto de Demonstração

Este projeto demonstra como criar e testar um plugin Maven customizado. O projeto consiste em dois módulos principais:

- **`unicat-maven-plugin`**: O plugin Maven customizado
- **`test-plugin-consumer`**: Projeto de teste que consome o plugin

## 📋 Pré-requisitos

- Java 8 ou superior
- Maven 3.6 ou superior

## 🚀 Como Executar

### 1. Compilar o Plugin

Primeiro, você precisa compilar e instalar o plugin no repositório local do Maven:

```bash
cd unicat-maven-plugin
mvn clean install
```

### 2. Executar o Plugin

#### **Método Principal (Recomendado)**

Execute o plugin diretamente usando o comando completo:

```bash
cd test-plugin-consumer
mvn br.com.unicat:unicat-maven-plugin:1.0-SNAPSHOT:hello
```

#### **Método Alternativo**

Execute o plugin através da fase de compilação do projeto consumidor:

```bash
cd test-plugin-consumer
mvn compile
```

## 📁 Estrutura do Projeto

```
unit-testing-ai-agent/
├── unicat-maven-plugin/          # Plugin Maven customizado
│   ├── pom.xml                   # Configuração do plugin
│   └── src/main/java/br/com/unicat/
│       └── HelloMojo.java        # Implementação do goal 'hello'
└── test-plugin-consumer/         # Projeto que consome o plugin
    └── pom.xml                   # Configuração com o plugin
```

## 🔧 Como Funciona

### Plugin (`unicat-maven-plugin`)

O plugin contém um goal chamado `hello` que imprime "Olá, Mundo!" no log do Maven:

```java
@Mojo(name = "hello")
public class HelloMojo extends AbstractMojo {
    public void execute() throws MojoExecutionException {
        getLog().info("Olá, Mundo!");
    }
}
```

### Projeto Consumidor (`test-plugin-consumer`)

O projeto consumidor está configurado para executar o plugin durante a fase `compile`:

```xml
<plugin>
    <groupId>br.com.unicat</groupId>
    <artifactId>unicat-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>hello</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 📝 Saída Esperada

Quando executado com sucesso, você verá:

```
[INFO] --- unicat:1.0-SNAPSHOT:hello (default-cli) @ test-plugin-consumer ---
[INFO] Olá, Mundo!
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

## 🛠️ Desenvolvimento

### Adicionando Novos Goals

Para adicionar novos goals ao plugin:

1. Crie uma nova classe que estenda `AbstractMojo`
2. Anote com `@Mojo(name = "nome-do-goal")`
3. Implemente o método `execute()`
4. Recompile o plugin com `mvn clean install`

### Modificando o Plugin

Após qualquer modificação no plugin, sempre recompile:

```bash
cd unicat-maven-plugin
mvn clean install
```

## 🐛 Solução de Problemas

### Plugin não encontrado
Certifique-se de que o plugin foi compilado e instalado:
```bash
cd unicat-maven-plugin
mvn clean install
```

### Erro de encoding
Se houver problemas com caracteres especiais, verifique se o encoding está configurado como UTF-8.

## 📚 Recursos Adicionais

- [Maven Plugin Development Guide](https://maven.apache.org/guides/plugin/guide-java-plugin-development.html)
- [Maven Plugin API Documentation](https://maven.apache.org/ref/current/maven-plugin-api/)
- [Maven Plugin Annotations](https://maven.apache.org/plugin-tools/maven-plugin-tools-annotations/) 