# Avaliação de rotação de credenciais e chaves

Avaliação feita na Fase 3 do `TODO.md`. Nenhum valor secreto é registrado aqui.

## Situação

- O repositório `onurbcd/cli` é **público** no GitHub.
- O commit `a9c85e2` (2023-11-24) colocou `admin.crypto.key1` (chave AES) e `admin.crypto.key2` (IV) em `application.yml`. Credenciais do banco apareceram em `application.yml` e `reset-db.sh` até o commit `1262c38`, que as moveu para `.env`.
- Remover os valores do código não os remove do histórico. Qualquer pessoa com acesso ao repositório consegue recuperá-los.
- Em 2026-11-27, o `.env` local ainda usava a **mesma chave e o mesmo IV** presentes no histórico. A senha do banco não pôde ser comparada com confiança.

## Premissa

Os dados da base podem ser apagados. Por isso a rotação não precisa preservar nem migrar registros: troca-se o valor no `.env` e recria-se o banco.

## Decisões

| Item | Decisão | Motivo |
|------|---------|--------|
| `CLI_DB_PASSWORD` | **Rotacionar** | O valor antigo é público. Basta trocar no `.env` e recriar o banco com `./reset-db.sh`. |
| `CLI_CRYPTO_KEY1` / `CLI_CRYPTO_KEY2` | **Rotacionar** | Comprometidos: decifram tudo que está em `secret`. Como os dados são descartáveis, não há re-criptografia; os registros antigos são apagados com o reset. |
| Segredos guardados na tabela `secret` | **Trocar nos serviços de origem** | Se o banco ou um dump tiver saído da máquina, o conteúdo é legível com as chaves públicas. |
| Reescrever o histórico (`git filter-repo`) | **Não fazer** | Valores já expostos podem ter sido copiados; só a rotação resolve. Reescrever quebraria clones sem ganho real. |

## Procedimento

1. Gere valores novos: senha do banco, chave AES (`openssl rand -base64 32`).
2. Atualize `CLI_DB_PASSWORD` e `CLI_CRYPTO_KEY` (Base64 de 32 bytes; ver [`secrets-storage.md`](secrets-storage.md)) no `.env`.
3. Rode `./reset-db.sh`. Isso remove o container `postgres-cli` e cria outro com a senha nova. Como o script não usa volume, **todos os dados são perdidos**, inclusive os registros cifrados com as chaves antigas.
4. Inicie a aplicação; o Flyway recria o esquema.
5. Troque nos serviços de origem os segredos reais que passaram pelo app.

A migração para criptografia autenticada (próximo item da Fase 3) não precisa converter dados antigos. Basta que a versão nova use a chave nova. Se algum dia houver dados que precisem sobreviver a uma troca de algoritmo, será preciso decifrar com a chave antiga e cifrar de novo numa única transação.
