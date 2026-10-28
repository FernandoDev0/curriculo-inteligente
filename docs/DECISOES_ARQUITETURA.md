# Decisões de arquitetura

Registro das escolhas feitas na inspeção de 2026-10-27. O prompt mestre chegou cortado no meio da Fase 3 (“Valid”). As seções 1 a 27 estavam completas e são a especificação seguida. A lista de fases depois da Fase 3 não veio.

## Onde o código mora

A pasta do workspace já é a raiz do produto e só continha o PDF de referência. Os diretórios `frontend/`, `backend/`, `database/`, `docs/` e `scripts/` ficam nessa raiz. Não foi criada uma pasta extra `curriculo-inteligente/`, para não aninhar o projeto sem necessidade. O PDF original permanece na raiz.

## Versões

| Peça | Escolha | Motivo |
|---|---|---|
| Node.js | 24.22.1, já instalado | Angular 22 aceita `^24.15.0` |
| Angular | 22, CLI via `npx @angular/cli@22` | Versão estável em suporte ativo. A CLI global da máquina é 21.2.27 e não será a base do projeto |
| Java | 21 LTS, Temurin, em `%USERPROFILE%\.jdks` | Não havia `java` no PATH. Java 21 é LTS e está na faixa do Spring Boot 4.1 (17 a 26). Evita instalar um JDK de sistema sem necessidade |
| Spring Boot | 4.1.2 | Linha com suporte aberto. Boot 3.5 já encerrou o suporte aberto |
| springdoc | 3.2.0 | Linha 3.x é a compatível com Boot 4 |
| PostgreSQL | 17 no Docker Compose | Não havia `psql` nem Docker no PATH. O compose é o caminho previsto. Sem H2 |
| PDF | `com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10` | Última versão publicada pelo projeto oficial. O grupo `io.github.openhtmltopdf` 1.1.x não é a linha usada |
| Gemini | modelo padrão `gemini-3.5-flash`, configurável | É o modelo do quickstart oficial. A documentação recente também cita `gemini-3.5-flash-lite` e `gemini-3.8-flash`. O teste de conexão descobre se o modelo responde. Não há chamada ao Gemini na subida do processo |

## Banco

A migração canônica é `database/migrations/V1__init.sql`. O build do backend copia esse diretório para `classpath:db/migration`. Não há segunda cópia manual dentro de `src/main/resources`.

Credenciais só por variável de ambiente. O exemplo está em `.env.example`. `.env` não entra no Git.

O servidor HTTP escuta em `127.0.0.1`. Nada é publicado na rede.

## Conta local

Não há cadastro aberto. Na primeira execução, se não existir usuário, `POST /api/v1/auth/bootstrap` cria o dono local. Depois disso o endpoint responde conflito. Sessão por cookie, senha com BCrypt. Todo currículo, entrevista e vaga é filtrado pelo `user_id` da sessão.

## Chave do Gemini

1. `GEMINI_API_KEY` no ambiente, se existir.
2. Senão, arquivo `%USERPROFILE%\.curriculo-inteligente\gemini-api-key`, gravado pelo assistente, com ACL só do usuário atual.
3. A chave não vai para o banco, para o Git, para localStorage nem para as respostas de status. Os logs não imprimem a chave nem o texto integral do currículo.

## IA

`LanguageModel` é a porta do domínio. `GeminiLanguageModel` é o adaptador HTTP (`RestClient`) para a API oficial. Testes usam um falso. A suíte comum não chama a rede.

Instrução de sistema e texto da pessoa viajam em campos separados. Texto do usuário não é concatenado na instrução de sistema.

Saída usada para alterar o currículo passa por DTO e por uma guarda: número que não estava no texto original e empresa que a pessoa não informou são rejeitados. O texto original permanece.

Edição de foto por IA só é oferecida se `GEMINI_IMAGE_MODEL` estiver definido e o `models.get` indicar saída de imagem. Caso contrário a foto enviada pela pessoa é usada como está, depois de remover metadados. O sistema não gera outra pessoa.

## PDF

Exportação definitiva só depois de aprovação e só se o PDF tiver uma página. Se estourar, a API informa a contagem e não declara sucesso. A prévia pode ser gerada antes, para a pessoa medir o documento. O HTML do modelo segue `docs/REFERENCIA_CURRICULO.md`.

## O que ficou de fora de propósito

- Deploy, domínio público, serviço pago de nuvem.
- H2.
- Chamada real ao Gemini dentro dos testes automatizados.
- Inventar experiência, data, certificado ou competência.
