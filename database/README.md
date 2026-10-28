# Banco de dados

PostgreSQL 17. Não há H2.

A migração versionada fica em `migrations/` e é a fonte única. O `pom.xml` do backend copia essa pasta para o classpath do Flyway durante o build. Não duplique o SQL dentro de `backend/src`.

`init/` não cria tabela. O Docker cria o usuário e o banco pelas variáveis do `.env`. O Flyway cria o esquema quando o backend sobe.

## Subir

Na raiz, com Docker instalado e `.env` preenchido:

```text
docker compose up -d
```

A porta publicada é `127.0.0.1`, não `0.0.0.0`. O volume `curriculo_pg` guarda os dados entre reinícios.

## Backup local

```text
docker exec curriculo-postgres pg_dump -U curriculo -d curriculo -Fc -f /tmp/curriculo.dump
docker cp curriculo-postgres:/tmp/curriculo.dump .\backup-curriculo.dump
```

## Restaurar

```text
docker cp .\backup-curriculo.dump curriculo-postgres:/tmp/curriculo.dump
docker exec curriculo-postgres pg_restore -U curriculo -d curriculo --clean --if-exists /tmp/curriculo.dump
```

Nesta máquina, na inspeção de 2026-10-27, `docker` e `psql` não estavam no PATH. Sem um PostgreSQL local o backend não inicia. Instalar o Docker ou o PostgreSQL é passo seu, ou de um script futuro, e não faz parte do código.
