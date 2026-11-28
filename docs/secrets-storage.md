# Armazenamento de segredos

Fase 3 do `TODO.md`. Nenhum valor secreto é registrado aqui.

## Formato atual

`secret.password` guarda `v2:` + Base64(IV ‖ texto cifrado ‖ tag), cifrado com **AES-256-GCM**:

- chave: `CLI_CRYPTO_KEY`, Base64 de 32 bytes aleatórios (`openssl rand -base64 32`); a aplicação não inicia se estiver ausente ou inválida;
- IV de 12 bytes **aleatório a cada cifragem** (o mesmo texto gera valores diferentes);
- tag de 128 bits: valor adulterado ou chave errada falha na decifragem com `ApiException(CRYPTO)`, sem expor conteúdo;
- limite: a coluna é `varchar(255)`, o que comporta senhas de até ~160 bytes UTF-8.

## Por que mudou

O formato anterior era AES/CBC/PKCS5 com IV fixo (`CLI_CRYPTO_KEY2`): determinístico (revela senhas iguais), sem verificação de integridade e com a chave publicada no histórico (ver [`security-rotation.md`](security-rotation.md)).

## Migração

Decisão: **corte limpo**. Os dados eram descartáveis e a base já foi recriada, então não há leitura do formato antigo; valores sem o prefixo `v2:` são rejeitados com `Crypto error: unsupported secret format`.

Passos para quem tiver uma base antiga:

1. Gere a nova chave e defina `CLI_CRYPTO_KEY` no `.env`; remova `CLI_CRYPTO_KEY1` e `CLI_CRYPTO_KEY2`.
2. Rode `./reset-db.sh` (apaga todos os dados) e cadastre os segredos novamente.

Se algum dia for preciso preservar dados antigos: decifrar cada linha com a chave/IV antigos (AES/CBC/PKCS5) e regravar com a versão nova numa única transação, a partir de um utilitário temporário, e depois removê-lo. O prefixo `v2:` permite distinguir linhas já migradas e retomar o processo com segurança. Faça backup antes.
