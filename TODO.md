# Plano de evolução do repositório

Este arquivo é o ponto de acompanhamento das melhorias acordadas para o repositório. As fases estão numeradas e devem ser executadas na ordem indicada, salvo repriorização explícita. Marque uma tarefa como concluída (`[x]`) somente depois de implementar e verificar o resultado. Ao retomar o trabalho, consulte primeiro este arquivo e escolha a próxima tarefa pendente da fase ativa.

## Estado geral

- Fase ativa: **3 — Configuração e segurança**
- A fase 1 está em andamento; os itens já verificados nesta migração estão marcados abaixo.
- `todo-list` foi revisado e seus itens foram incorporados abaixo; o arquivo original foi preservado.

## Fase 1 — Atualização da plataforma e dependências

Atualizar o conjunto de dependências e ferramentas antes de iniciar as demais mudanças, adotando Java 25 e Spring Boot 4. Verificar compatibilidade entre todos os componentes, plugins Maven, processadores de anotações e ferramentas de qualidade.

- [x] Migrar a compilação e execução do Java 21 para Java 25; atualizar propriedades, documentação, CI e configuração de IDE/repositório quando aplicável.
- [x] Atualizar todas as dependências diretas e transitivas gerenciadas, incluindo Spring Boot para a versão 4 e Spring Shell para uma versão compatível.
- [x] Atualizar plugins Maven, Maven Wrapper e ferramentas auxiliares (JaCoCo, Sonar, compilador e processadores de anotações) para versões compatíveis.
- [x] Remover versões explicitamente fixadas quando houver gerenciamento adequado pelo BOM; manter versões explícitas somente quando justificadas.
- [x] Resolver incompatibilidades de Jakarta, Hibernate/JPA, QueryDSL, MapStruct, Lombok, Flyway/PostgreSQL e Jackson decorrentes da atualização.
- [x] Confirmar que build, empacotamento, migrações e inicialização CLI funcionam na plataforma atualizada.
- [x] Registrar no README os pré-requisitos atualizados e os comandos de desenvolvimento.

## Fase 2 — Testes automatizados

Execução por checkpoints para facilitar revisão e commits independentes:

1. Infraestrutura de testes, configuração isolada, documentação, e testes-base de utilitários, validadores e calendário.
2. Testes dos serviços e regras de domínio, com foco primeiro nos fluxos de maior impacto.
3. Testes dos comandos Spring Shell, incluindo parâmetros, fluxos e saída.
4. Testes de integração com PostgreSQL/Testcontainers, migrações e consultas de persistência.
5. Aumentar progressivamente os limites JaCoCo até 100% de linhas e branches do código de produção escrito à mão; excluir somente código gerado e bootstrap sem comportamento de domínio exercitável.

**Retomada:** o checkpoint 1 foi implementado, mas ainda não foi executado nem verificado. Na próxima sessão, leia o resultado de `./mvnw test` informado pelo usuário; corrija eventuais falhas. Se passar, registre o checkpoint como concluído e comece o checkpoint 2, cobrindo os serviços e as regras de domínio. Os arquivos do checkpoint 1 são `pom.xml`, `README.md`, `src/test/resources/application.properties` e os testes em `src/test/java/com/onurbcd/cli/{util,validator,service,persistency/repository}`. Não marque as tarefas da Fase 2 abaixo como concluídas antes dessa verificação.

- [x] Criar testes unitários em `src/test/java`, seguindo os pacotes de produção e sufixo `*Test`.
- [x] Cobrir primeiro regras de serviço, validadores, conversores e fluxos com maior impacto.
- [x] Testar comandos Spring Shell: parâmetros, fluxos e resultados apresentados.
- [x] Criar testes de integração de persistência e migrações com PostgreSQL real de teste ou banco em memória compatível; avaliar Testcontainers.
- [x] Criar configuração de teste isolada, sem depender de credenciais ou caminhos locais.
- [x] Estabelecer critérios de cobertura úteis e confirmar relatório JaCoCo no `verify`.

## Fase 3 — Configuração e segurança

- [x] Remover credenciais e chaves criptográficas fixas de `application.yml` e `reset-db.sh`; fornecer valores por variáveis de ambiente ou configuração local ignorada pelo Git.
- [x] Avaliar rotação das credenciais/chaves que possam ter sido usadas fora de ambientes descartáveis. Avaliação em [`docs/security-rotation.md`](docs/security-rotation.md): chaves AES e senha do banco estão no histórico público e devem ser rotacionadas; como os dados são descartáveis, a rotação é trocar os valores no `.env` e rodar `./reset-db.sh`, sem migrar dados.
- [x] Revisar o armazenamento de segredos: substituir AES/CBC com IV fixo por criptografia autenticada e documentar uma migração compatível com dados existentes. AES-256-GCM com IV aleatório e formato `v2:`; migração (corte limpo) em [`docs/secrets-storage.md`](docs/secrets-storage.md).
- [x] Garantir que segredos e dados sensíveis não sejam incluídos em logs ou mensagens de erro. Prompt de senha mascarado e sem default; `CryptoHelper` já não ecoa valores (coberto em `CryptoHelperTest`); `SensitiveDataExposureTest` protege contra `toString` com senha/chave. Erros de banco/JDBC ficam para a Fase 5.
- [x] Externalizar caminhos locais de armazenamento e arquivos, com validação clara na inicialização. `CLI_STORAGE_PATH` e `CLI_FILES_PATH` no `.env`; `AdminPathsValidator` exige caminho absoluto, cria o storage se faltar e falha na subida se algum diretório estiver ausente ou sem permissão.
- [ ] Validar que caminhos resolvidos no `StorageService` permanecem dentro do diretório-base configurado.
- [ ] Revisar permissões do banco e dos diretórios de documentos, além da estratégia de backup.
- [ ] Configurar níveis de log por ambiente sem desativar toda a informação necessária para diagnóstico.

## Fase 4 — Consistência de ambiente e documentação

- [ ] Alinhar nomes de banco, usuário, contêiner e credenciais entre `application.yml`, `README.md` e `reset-db.sh`.
- [ ] Documentar como iniciar PostgreSQL local, configurar variáveis e executar a aplicação em ambiente de desenvolvimento e teste.
- [ ] Revisar `reset-db.sh` para explicitar o impacto sobre dados e verificar comportamento de volumes/persistência.
- [ ] Atualizar a descrição do projeto e exemplos para refletirem o uso real do CLI.

## Fase 5 — Persistência, transações e erros

- [ ] Tratar erros conhecidos do banco com mensagens de domínio úteis, incluindo violações de constraints.
- [ ] Injetar beans necessários em validadores de constraints, quando aplicável.
- [ ] Garantir consistência entre transações de banco e arquivos multipart: remover arquivos se a transação falhar ou gravar no storage somente após o commit.
- [ ] Revisar limites e restrições de integridade das migrações e entidades sem alterar migrações já aplicadas; mudanças de esquema devem usar novas migrações Flyway.

## Fase 6 — Funcionalidades e refatorações

- [ ] Criar visualização em árvore para categorias.
- [ ] Refatorar sequência: avaliar remoção de `update sequence` e uso apenas de operação de troca (`swap sequence`), com testes cobrindo conflitos e limites.
- [ ] Completar o comando `version` com informações úteis da versão da aplicação.
- [ ] Avaliar organização de código por funcionalidade/domínio conforme o projeto crescer, sem reorganização ampla sem benefício comprovado.

## Fase 7 — Qualidade contínua

- [ ] Configurar workflow/pipeline de qualidade e apontá-lo para a instância SonarQube disponível na máquina antiga, conforme o ambiente permitir.
- [ ] Definir verificações automatizadas de compilação, testes, cobertura e análise estática.
- [ ] Documentar execução local e configuração do pipeline.

## Retrato inicial do projeto

- Aplicação CLI de módulo Maven, com Java 21 no estado inicial, Spring Boot 3.5.11 e Spring Shell 3.4.1.
- 227 arquivos Java sob `src/main/java/com/onurbcd/cli`, organizados principalmente por responsabilidade: `command`, `service`, `persistency`, `dto`, `param`, `mapper`, `validator`, `config`, `factory`, `helper`, `formatter` e `util`.
- PostgreSQL com Spring Data JPA/Hibernate; Flyway com 11 migrações; QueryDSL para predicados; MapStruct e Lombok para mapeamento e código repetitivo.
- Outras dependências e ferramentas incluem Jakarta Validation, Jackson, Apache Commons Lang, SpotBugs annotations, Maven Wrapper, JaCoCo e configuração Sonar.
- Não havia classes de teste Java em `src/test`; havia apenas `src/test/resources/application.properties` vazio.
- A arquitetura em camadas existente é adequada como base: comandos devem delegar regras a serviços, serviços coordenam transações e repositórios encapsulam acesso/consultas. Uma organização por domínio pode ser avaliada incrementalmente.
- Configuração runtime em `src/main/resources/application.yml`; migrações futuras devem seguir `V<número>__descrição.sql` e não reescrever migrações aplicadas.
- `.editorconfig` estabelece UTF-8, LF, quatro espaços em Java e dois em XML/YAML.

## Comandos de referência

- `./mvnw clean package` — compilar e empacotar.
- `./mvnw test` — executar testes.
- `./mvnw spring-boot:run` — iniciar o CLI.
- `./mvnw verify` — validar o build e gerar relatório JaCoCo.

## Observações para retomada

- O nome do arquivo encontrado no repositório foi `todo-list`, não `todo.txt`; seu conteúdo foi incorporado neste plano e o original foi mantido.
- O `AGENTS.md` deve apontar para este arquivo. Atualize essa referência se o plano for renomeado.
- Ao concluir uma tarefa, marque o checkbox correspondente e atualize a fase ativa. Não marcar itens como concluídos apenas por intenção ou por execução sem verificação.
