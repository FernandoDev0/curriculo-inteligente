# Backend

Spring Boot 4.1.2, Java 21, PostgreSQL e Flyway. A API está descrita em `docs/API.md`.

O servidor escuta só em `127.0.0.1:8080`. A chave do Gemini fica na variável `GEMINI_API_KEY` ou no arquivo `%USERPROFILE%\.curriculo-inteligente\gemini-api-key`. Ela não entra no banco, no log nem no JSON de status.

Os testes não chamam o Gemini. O teste de conexão da aplicação é real e só roda quando alguém pede na tela.

```text
powershell -NoProfile -ExecutionPolicy Bypass -File ..\scripts\test-backend.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File ..\scripts\start-backend.ps1
```

O PostgreSQL precisa estar no ar antes do `start`. Sem o banco, o processo não sobe e não troca para H2.
