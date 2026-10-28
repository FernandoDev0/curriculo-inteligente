# Inicialização

O banco e o usuário nascem das variáveis `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` do Docker Compose.

Não há script SQL aqui de propósito. Tabela, índice e restrição ficam nas migrações Flyway em `../migrations`.
