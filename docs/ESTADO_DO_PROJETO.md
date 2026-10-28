# Estado do projeto

Atualizado em 2026-10-28, depois da primeira implementação.

## Feito

- O PDF original `Curriculo_Fernando_Aparecido_Calassio_Rodrigues.pdf` foi medido e permanece intacto. O modelo está em `docs/REFERENCIA_CURRICULO.md`.
- Documentos de arquitetura, dados, conversa, segurança, API e decisões escritos.
- Migração `database/migrations/V1__init.sql` copiada para o classpath do Flyway no build.
- Backend Spring Boot 4.1.2 em `backend/`. Java 21. API da `docs/API.md`, sessão, chave do Gemini fora do banco, entrevista por etapas, rascunho, revisão com guarda contra número novo, PDF A4 e comparação lexical com a vaga.
- Frontend Angular 22 em `frontend/`. Telas de conta, configuração, diagnóstico, entrevista, prévia, edição, aprovação e lista.
- Temurin 21.0.12.1 extraído em `%USERPROFILE%\.jdks\jdk-21.0.12.1+1`. Não está no PATH do sistema. Os scripts em `scripts/` apontam para ele.

## Testes executados nesta máquina

- `mvnw test` no backend: 11 testes, todos passaram. Inclui PDF de uma página, PDF que estoura a página, guarda de números e avanço da entrevista. Não chama o Gemini nem o PostgreSQL.
- `npm test -- --watch=false` no frontend: 8 testes, todos passaram (Vitest e jsdom).

## Ainda não verificado

- Subir o backend. Não há Docker nem `psql` no PATH, então o PostgreSQL não está rodando. Sem o banco o Spring não inicia e não usa H2.
- Fluxo no navegador. O servidor não chegou a responder.
- Chamada real ao Gemini. O teste de conexão existe e não é simulado, mas ninguém executou com uma chave.
- A cor `#1F4E79` é a aproximação visual do azul do PDF original. Falta comparar o PDF gerado com o arquivo lado a lado.
- Edição de foto por IA fica desligada de propósito. O teste de conexão só marca se o modelo anuncia saída de imagem. O endpoint de edição não gera outra pessoa.

## Ambiente

- Node.js 24.22.1, npm 11.19.1, Angular CLI do projeto 22.
- Git 2.55. `java` e `mvn` globais ausentes. Maven Wrapper 3.9.16 baixa o Maven.
- `winget` não atualizou a fonte. WSL não está instalado.

## Próximo passo

Instalar Docker ou o PostgreSQL 17, copiar `.env.example` para `.env`, subir o banco e só então abrir a interface em http://localhost:4200.
