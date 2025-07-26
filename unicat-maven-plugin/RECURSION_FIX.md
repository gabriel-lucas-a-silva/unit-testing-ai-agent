# Correção do Problema de Recursão - UniCat Maven Plugin

## Problema Identificado

O plugin UniCat estava enfrentando um problema de recursão infinita quando tentava executar comandos Maven dentro do próprio projeto. Isso acontecia porque:

1. **Goals de Ciclo Completo**: Ao invocar `mvn compile` ou `mvn test`, o Maven executava todo o ciclo de vida, incluindo o próprio plugin UniCat
2. **Auto-Invocação**: O plugin se auto-dispareva, criando um loop infinito
3. **Deadlock de I/O**: O ProcessBuilder também enfrentava problemas de buffer cheio

## Soluções Implementadas

### 1. Goals Pontuais do Maven

**Problema**: Usar `compile`, `test`, `install` invoca todo o ciclo de vida
**Solução**: Converter para goals específicos de plugins

```java
// Antes
request.setGoals(Arrays.asList("compile"));

// Depois  
request.setGoals(Collections.singletonList("org.apache.maven.plugins:maven-compiler-plugin:3.14.0:compile"));
```

**Mapeamento de Comandos**:
- `compile` → `compiler:compile`
- `test` → `compiler:compile` + `compiler:testCompile` + `surefire:test`
- `clean` → `clean:clean`
- `install` → Sequência completa de goals específicos

### 2. Propriedade `unicat.skip`

**Implementação no UnicatMojo.java**:
```java
@Parameter(property = "unicat.skip", defaultValue = "false")
private boolean skip;

public void execute() throws MojoExecutionException {
    if (skip) {
        getLog().info("⏭️ UniCat - Execução pulada (unicat.skip=true)");
        return;
    }
    // ... resto da execução
}
```

**Uso no BuildAndTestTool.java**:
```java
Properties props = new Properties();
props.setProperty("unicat.skip", "true");
request.setProperties(props);
```

### 3. Desabilitação de Recursão

**Maven Invoker API**:
```java
request.setRecursive(false); // CRÍTICO: Desabilita recursão de reactor
```

### 4. Perfil Maven para Desabilitação

**Adicionado ao pom.xml do projeto consumidor**:
```xml
<profiles>
  <profile>
    <id>invoker-skip-unicat</id>
    <activation>
      <property>
        <name>skipUnicatProfile</name>
      </property>
    </activation>
    <build>
      <plugins>
        <plugin>
          <groupId>br.com.unicat</groupId>
          <artifactId>unicat-maven-plugin</artifactId>
          <executions>
            <execution>
              <id>default-cli</id>
              <phase>none</phase>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```

### 5. Detecção Melhorada de Recursão

**Múltiplas verificações**:
```java
private boolean isRunningInsideMavenPlugin() {
    // 1. Verifica propriedade unicat.skip
    if (Boolean.getBoolean("unicat.skip")) {
        return true;
    }
    
    // 2. Verifica variável de ambiente
    String unicatSkip = System.getenv("UNICAT_SKIP");
    if (unicatSkip != null && (unicatSkip.equals("true") || unicatSkip.equals("1"))) {
        return true;
    }
    
    // 3. Verifica processos Maven em execução
    // 4. Verifica variáveis de ambiente do Maven
    // 5. Verifica stack trace para execução dentro do plugin
}
```

## Como Usar

### Opção 1: Propriedade de Sistema
```bash
mvn compile -Dunicat.skip=true
```

### Opção 2: Variável de Ambiente
```bash
export UNICAT_SKIP=true
mvn compile
```

### Opção 3: Perfil Maven
```bash
mvn compile -Pinvoker-skip-unicat
```

### Opção 4: Perfil com Propriedade
```bash
mvn compile -DskipUnicatProfile
```

## Benefícios das Correções

1. **Eliminação da Recursão**: O plugin não se auto-invoca mais
2. **Execução Mais Rápida**: Goals pontuais são mais eficientes
3. **Controle Granular**: Múltiplas formas de desabilitar o plugin
4. **Compatibilidade**: Mantém funcionalidade original quando necessário
5. **Debugging Melhorado**: Logs detalhados para identificar problemas

## Testes Recomendados

1. **Teste de Compilação**:
   ```bash
   mvn compile -Dunicat.skip=true
   ```

2. **Teste de Execução de Testes**:
   ```bash
   mvn test -Dunicat.skip=true
   ```

3. **Teste de Build Completo**:
   ```bash
   mvn clean install -Dunicat.skip=true
   ```

4. **Teste com Perfil**:
   ```bash
   mvn compile -Pinvoker-skip-unicat
   ```

## Monitoramento

O plugin agora fornece logs detalhados:
- ✅ Sucesso na execução
- ❌ Falhas com códigos de saída
- 📋 Logs completos para debugging
- ⚠️ Avisos sobre execução recursiva

## Conclusão

As correções implementadas resolvem completamente o problema de recursão, mantendo a funcionalidade do plugin e adicionando controles para evitar execuções indesejadas. O sistema agora é mais robusto e oferece múltiplas opções de configuração. 