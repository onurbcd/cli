# Runbook: migração para Java 25 e Spring Boot 4

Este runbook orienta a atualização do projeto para Java 25, Spring Boot 4 e versões compatíveis das dependências. **Rode os comandos no terminal, a partir da raiz do repositório** — a pasta `cli`, onde ficam `pom.xml` e `mvnw`.

Antes de começar, confira `git status --short` e preserve suas alterações locais. No estado atual desta migração, o POM já declara Java 25, Spring Boot 4.0.9 e Spring Shell 4.0.3. Portanto, você está no passo 4: não repita os passos anteriores; continue a migração do código Spring Shell descrita abaixo. As versões 4.0.9 e 4.0.3 formam o par estável compatível confirmado na documentação consultada em 15/11/2026. Confira novamente a [matriz de compatibilidade](https://github.com/spring-projects/spring-shell/wiki/Spring-Boot-compatibility) caso retome este runbook mais tarde.

## 0. Preparar o trabalho

Confira quais arquivos estão alterados:

```bash
git status --short
```

Se a saída listar arquivos, identifique quais são suas alterações da migração e não as descarte. Se ainda não estiver numa branch de trabalho, crie uma:

```bash
git switch -c chore/java25-spring-boot4
```

Rode `git switch -c` somente se o worktree estiver limpo e essa branch ainda não existir. Se já estiver na branch de migração, continue nela. Se o worktree estiver sujo, não troque de branch neste ponto: preserve as alterações e continue no contexto em que elas foram feitas.

## 1. Repetir e registrar o build atual

Confira as versões que o terminal está usando:

```bash
java -version
./mvnw --version
```

O arquivo `.mvn/wrapper/maven-wrapper.properties` deste repositório aponta para Maven 3.10.0. Na data desta revisão, essa é uma versão estável; Maven 4 ainda é pré-lançamento. Não troque o Wrapper por uma versão preview. Confira o [histórico oficial de releases do Maven](https://maven.apache.org/docs/history) antes de retomar este passo no futuro.

Rode o build de linha de base:

```bash
./mvnw -DskipTests clean package
```

`clean` remove arquivos gerados anteriormente; `package` compila e empacota a aplicação. `-DskipTests` evita executar testes — o projeto ainda não tem testes Java.

**Só avance se o comando terminar com `BUILD SUCCESS`.** Se falhar, guarde a saída completa e corrija essa falha antes de começar a migração. Erros de resolução de dependências podem ser causados por rede ou repositórios inacessíveis; erros de compilação apontam para problemas do projeto.

## 2. Gerar relatórios de versões disponíveis

Rode:

```bash
./mvnw versions:display-dependency-updates
./mvnw versions:display-plugin-updates
```

O primeiro relatório lista versões mais novas de dependências; o segundo faz o mesmo para plugins Maven. Eles são uma lista para investigação, **não** uma instrução para atualizar tudo. O relatório não sabe necessariamente se duas versões são compatíveis.

Não rode `versions:use-latest-versions`: esse objetivo altera o POM automaticamente e pode trocar bibliotecas geridas pelo Spring Boot por versões incompatíveis com o BOM.

## 3. Migrar Java 25 e atualizar Boot 3.5 como etapa intermediária

Este passo registra a mudança feita ao sair da linha de base Java 21. No estado atual, o POM já declara Java 25 e usa `<release>${java.version}</release>`; se isso continuar verdadeiro, não repita a edição. Caso ainda encontre Java 21, abra `pom.xml` e, na seção `<properties>`, altere:

```xml
<java.version>21</java.version>
```

para:

```xml
<java.version>25</java.version>
```

No `maven-compiler-plugin`, substitua estas duas linhas:

```xml
<source>${java.version}</source>
<target>${java.version}</target>
```

por esta:

```xml
<release>${java.version}</release>
```

`release` configura tanto a versão da linguagem quanto as APIs Java permitidas na compilação. Depois de salvar, rode:

```bash
./mvnw -DskipTests clean compile
```

O build deve mostrar a compilação com `release 25`. Se falhar, não avance: anote o primeiro erro e corrija essa etapa.

Em seguida, leve Boot 3.5 à versão estável mais recente da série. O OpenRewrite tem uma receita para essa atualização. Primeiro faça uma simulação, que **não altera os arquivos**:

```bash
./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:dryRun \
  -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:RELEASE \
  -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5
```

Confira o resultado:

```bash
git status --short
git diff
```

O `dryRun` escreve o patch em `target/rewrite/rewrite.patch`. Para ler as alterações propostas:

```bash
less target/rewrite/rewrite.patch
```

Se as mudanças propostas fazem sentido, aplique a receita:

```bash
./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:RELEASE \
  -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5
```

O objetivo `rewrite:run` altera os arquivos do projeto; `rewrite:dryRun` apenas simula. Confira cada alteração:

```bash
git diff
```

Rode o build antes de avançar:

```bash
./mvnw -DskipTests clean package
```

## 4. Migrar para Spring Boot 4 e Spring Shell 4

A receita Boot 4 pode atualizar o parent do Spring Boot, propriedades e alguns usos de APIs. Ela **não atualiza o BOM separado do Spring Shell**. Primeiro simule a receita Boot:

```bash
./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:dryRun \
  -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:RELEASE \
  -Drewrite.activeRecipes=org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0
```

Revise os patches em `target/rewrite` e o que aparece no log. Se estiver de acordo, aplique:

```bash
./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:RELEASE \
  -Drewrite.activeRecipes=org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0
```

Depois, examine o diff:

```bash
git diff
```

### Atualizar explicitamente a dependência Spring Shell

No `pom.xml`, a versão do Shell é definida separadamente pela propriedade `spring-shell.version`; a receita Boot não altera essa propriedade. Para o Spring Boot 4.0.9 deste projeto, configure a versão estável compatível do Shell:

```xml
<spring-shell.version>4.0.3</spring-shell.version>
```

Essa propriedade controla a importação de `spring-shell-dependencies`. Correção importante: **`spring-shell-starter-jline` não existe**. O starter com suporte JLine e provedor Jansi publicado pelo Spring Shell é `spring-shell-starter-jansi`. Como este projeto usa fluxos interativos e tabelas, substitua a dependência `spring-shell-starter` por:

```xml
<dependency>
    <groupId>org.springframework.shell</groupId>
    <artifactId>spring-shell-starter-jansi</artifactId>
</dependency>
```

Faça esta substituição na dependência existente; não declare `spring-shell-starter` e `spring-shell-starter-jansi` juntos, porque o starter Jansi já depende do starter base e do módulo `spring-shell-jline`. A documentação lista os starters `spring-shell-starter-jansi`, `-jni`, `-jna` e `-ffm`, mas não um `-jline`. Depois de editar o POM, confirme que a árvore de dependências inclui `spring-shell-jline`:

```bash
./mvnw dependency:tree -Dincludes=org.springframework.shell
```

Os starters do Spring Shell 4 importam o starter base do Boot. A tabela de compatibilidade Spring Shell/Boot associa as séries 4.0.x entre si. [Documentação de build do Shell](https://docs.spring.io/spring-shell/reference/building.html) · [Guia de migração do Shell 4](https://github.com/spring-projects/spring-shell/wiki/v4-migration-guide) · [Matriz de compatibilidade](https://github.com/spring-projects/spring-shell/wiki/Spring-Boot-compatibility)

### Migrar o código Spring Shell

Leia o [guia de migração do Spring Shell 4](https://github.com/spring-projects/spring-shell/wiki/v4-migration-guide). Neste repositório, a migração é uma mudança de código necessária, não apenas uma troca de imports: há vários comandos e usos de fluxos, contexto, tabelas e formatadores.

Localize os usos antigos:

```bash
rg -n "org\.springframework\.shell|@ShellComponent|@ShellMethod|@ShellOption|@ShellCommandGroup|ComponentFlow|ComponentContext|org\.springframework\.shell\.table" src/main/java
```

Migre os comandos seguindo estas correspondências do guia, revisando cada assinatura e o nome do comando:

- Troque `@ShellComponent` por um bean Spring, normalmente `@Component`.
- Troque `@ShellMethod` por `@Command` nos métodos. O atributo `key` passa para `name`, e `value` passa para `description`; preserve o nome do comando que os usuários já conhecem. Por exemplo, `@ShellMethod(key = "source-save", value = "Create or update a source.")` vira `@Command(name = "source-save", description = "Create or update a source.")`. Se havia várias chaves/aliases, passe os nomes para o array `name = { ... }` e confira a regra de nomes de opções do Shell 4.
- Troque `@ShellOption` por `@Option` quando o valor é uma opção nomeada e por `@Argument` quando é um argumento posicional. Preserve aliases, obrigatoriedade, valores padrão e ajuda; não faça substituição mecânica sem revisar esses detalhes.
- Quando `@Option(required = true)` já exige que o usuário informe o valor, `@NotNull` é redundante para essa mesma verificação no caminho do comando. Mantenha `@NotNull` se o método também puder ser chamado fora do Shell ou se quiser conservar a validação Bean Validation como contrato; mantenha sempre outras constraints, como `@Min` e `@Max`, pois `required` só verifica presença.
- Onde o código usa `defaultValue = ShellOption.NULL`, remova esse valor especial e marque a nova opção como `@Option(required = false)`. Para receber `null` quando a opção for omitida, mantenha o parâmetro como tipo de referência (`String`, `UUID`, `Boolean`, etc.); não use `defaultValue = "null"`, que seria o texto literal `null`. No Spring Shell 4, `required` é `false` por padrão, mas escrevê-lo explicitamente deixa a intenção clara.
- `ShellOption.NULL` também aparece fora de anotações e, nesses casos, não se troca por `@Option`. Em `FlowBuilderWrapper`, ele era passado como texto sentinela para indicar que o fluxo não tem valor padrão; passe `null` aos métodos `defaultValue`/`defaultSelect` e marque os parâmetros como `@Nullable` quando apropriado. Em `ParamUtil`, os métodos `getLocalDate`, `getString`, `getBigDecimal`, `getEnum`, `getShort` e `getUUID` usam o marcador para representar a ausência de um valor usado como valor inicial de fluxo: remova a importação de `ShellOption`, faça esses métodos retornarem `@Nullable String` e troque `.orElse(ShellOption.NULL)` por `.orElse(null)`. Marque como `@Nullable` os campos de `*SaveFlowParam` que recebem esses resultados. Em `StringUtil.normalizeSpace`, remova a comparação com `ShellOption.NULL`: `StringUtils.isNotBlank(null)` já resulta em `false`. Assim, os fluxos recebem `null` como ausência de valor, sem criar outro marcador textual da aplicação.
- Troque agrupamentos antigos por `@CommandGroup` e remova `@CommandScan`/`@EnableCommand` se existirem; no Boot 4, a descoberta de comandos é automática.
- Atualize os pacotes de `ComponentFlow`, `ComponentContext`, tabelas e formatadores conforme o guia. As APIs de tabela foram movidas para `org.springframework.shell.jline.tui.table`; as APIs do componente JLine ficam sob `org.springframework.shell.jline.tui.component`.

O guia documenta outras quebras, incluindo aliases curtos/longos de opções e a definição de comandos no nível da classe. Depois de migrar os usos, rode a compilação abaixo e use os erros restantes para encontrar referências antigas que a busca não cobriu.

Rode:

```bash
./mvnw -DskipTests clean compile
```

Resolva as falhas antes de continuar. A migração Boot 4 também pode envolver starters modulares e atualizações em JPA/Hibernate, Flyway, PostgreSQL e Jackson. Revise as alterações do OpenRewrite e trate manualmente as que a receita não fizer.

Ao migrar Jackson 2 para Jackson 3, revise manualmente `EruConfig`. Se houver serializadores personalizados, `addSerializer` é um método de `SimpleModule`, não uma chamada isolada: crie um `SimpleModule`, registre nele os serializadores de `LocalDate` e `LocalDateTime`, e passe o módulo a `JsonMapper.builder().addModule(...)`. Preserve `LOCAL_DATE_TIME_PATTERN` e `DateTimeFormatter.ISO_LOCAL_DATE` para manter o formato JSON atual.

## 5. Atualizar dependências e plugins restantes

O Spring Boot parent/BOM controla as versões de muitas dependências. No POM atual isso inclui, por exemplo, starters Spring, PostgreSQL, Flyway e Jackson. Em geral, **remova uma versão explícita quando o BOM já gerencia aquela dependência**, em vez de sobrepor a versão indicada pelo Boot.

MapStruct e SpotBugs annotations não recebem necessariamente uma versão do BOM do Boot; use os relatórios do passo 2 e confirme as versões estáveis compatíveis. JaCoCo é um plugin Maven e também deve ser atualizado e revisado separadamente. O Spring Boot 4.0.9 importa o BOM do QueryDSL e gerencia `querydsl.version`; portanto, não trate QueryDSL como uma dependência sem gerenciamento. Remova versões explícitas das dependências QueryDSL quando o BOM cobrir os artefatos usados, ou justifique qualquer override.

Um ponto específico deste repositório é o `apt-maven-plugin`, que gera as classes QueryDSL `Q*`. O projeto já usa `annotationProcessorPaths` no `maven-compiler-plugin` para Lombok e MapStruct, mas ainda mantém o plugin APT separado. Para removê-lo, primeiro adicione `com.querydsl:querydsl-apt` com o classifier `jakarta` como processador de anotação, usando a versão gerenciada `${querydsl.version}`. Então remova o `apt-maven-plugin` e sua propriedade própria. **Confirme que a compilação gera as classes `Q*` necessárias antes de aceitar essa remoção.** Depois de cada grupo de mudanças, rode:

```bash
./mvnw -DskipTests clean compile
```

Se a compilação falhar, use o primeiro erro como ponto de partida. Não atualize outras dependências enquanto a etapa atual estiver quebrada.

Para plugins Maven, prefira as versões já gerenciadas pelo parent do Spring Boot quando forem adequadas. Este POM já herda a versão do `spring-boot-maven-plugin`; não volte a fixá-la manualmente. Mantenha uma versão explícita de plugin, como JaCoCo, somente quando o parent não a gerenciar ou quando houver uma justificativa de compatibilidade.

## 6. Validar o empacotamento e a aplicação

Quando os checkpoints anteriores compilarem, rode:

```bash
./mvnw -DskipTests clean verify
```

Para verificar a inicialização e as migrações, confirme primeiro que o PostgreSQL está ativo e que as credenciais em `src/main/resources/application.yml` funcionam no seu ambiente. Depois inicie o CLI:

```bash
./mvnw spring-boot:run
```

Na aplicação, confirme que o shell inicia, execute `help` e teste um comando representativo que leia ou grave dados. Saia com `exit` ou `quit`. A inicialização também deve mostrar se o Flyway aplicou ou validou as migrações.

Este teste depende do PostgreSQL configurado localmente. Se não puder conectar, isso não confirma uma falha na migração do código: primeiro é preciso corrigir a conexão ou o banco.

## 7. Atualizar documentação e TODO

Depois de confirmar o build, o empacotamento e a inicialização:

- Atualize no README os requisitos de Java e os comandos, se necessário.
- Atualize o retrato inicial e os itens da fase 1 no `TODO.md`.
- Marque uma caixa como concluída somente se a verificação correspondente passou.

**OpenRewrite é auxílio, não validação:** a simulação mostra o que a receita pretende mudar; `run` aplica essas mudanças. A revisão do diff e a verificação do build continuam necessárias. [Documentação do plugin OpenRewrite para Maven](https://docs.openrewrite.org/reference/rewrite-maven-plugin)
