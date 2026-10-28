# Currículo inteligente

Plataforma local para montar um currículo de uma página a partir de uma conversa. O navegador fala com um backend Spring Boot. O backend fala com o PostgreSQL e, se você configurar, com a API Gemini. A chave do Google não vai para o JavaScript.

O visual do PDF segue o arquivo `Curriculo_Fernando_Aparecido_Calassio_Rodrigues.pdf`, que permanece na raiz e não é alterado. A medida desse modelo está em `docs/REFERENCIA_CURRICULO.md`.

## O que você precisa

- Node.js 24 (esta máquina tem 24.22.1) e npm.
- JDK 21. Não veio no PATH. Os scripts procuram `%USERPROFILE%\.jdks`.
- Docker, para o PostgreSQL 17 do `docker-compose.yml`. Sem Docker o banco não sobe. A aplicação não usa H2.
- Uma chave do [Google AI Studio](https://aistudio.google.com/) quando quiser a reescrita. Dá para percorrer a entrevista e exportar o PDF sem ela.

Modelos, limites e preços da API mudam. A documentação oficial está em https://ai.google.dev/gemini-api/docs. Nada aqui promete uso ilimitado.

## Configurar

```text
copy .env.example .env
```

Edite `.env` e troque `POSTGRES_PASSWORD`. Para a opção A da chave, preencha `GEMINI_API_KEY` nesse arquivo, que não entra no Git. A opção B é o assistente da tela de configuração: a chave vai para `%USERPROFILE%\.curriculo-inteligente\gemini-api-key`.

## Subir

Com Docker e o JDK no lugar:

```text
docker compose up -d
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-backend.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-frontend.ps1
```

- API: http://127.0.0.1:8080
- OpenAPI: http://127.0.0.1:8080/swagger-ui.html
- Interface: http://localhost:4200

Na primeira visita, crie a conta local. Ela só existe neste banco. Depois rode o teste de conexão na tela de configuração. Esse teste chama o Gemini de verdade e gasta uma requisição pequena.

## Testes

```text
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-backend.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-frontend.ps1
```

Os testes não chamam o Gemini. O teste de conexão da tela é a verificação externa, e você dispara quando quiser.

## Encerrar

Pare os dois processos do terminal com Ctrl+C. O banco para com `docker compose stop`. Os dados ficam no volume `curriculo_pg`.

## Logs e backup

O backend escreve no terminal em que foi iniciado. Backup e restauração estão em `database/README.md`.

## Mapa

| Pasta | Conteúdo |
|---|---|
| `frontend/` | Angular |
| `backend/` | Spring Boot |
| `database/migrations/` | Flyway |
| `docs/` | Arquitetura, API, segurança, estado |
| `scripts/` | Atalhos locais |

Decisões de versão e o que ainda falta: `docs/DECISOES_ARQUITETURA.md` e `docs/ESTADO_DO_PROJETO.md`.
