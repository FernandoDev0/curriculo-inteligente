# Arquitetura

Aplicação local. O navegador fala só com o Spring Boot. O Spring Boot fala com o PostgreSQL e, quando a pessoa autoriza, com a API Gemini. A chave não sai do backend.

```text
Angular (localhost:4200)
        |  cookie de sessão, HTTPS não exigido em loopback
        v
Spring Boot 4.1  127.0.0.1:8080
        |                \
        v                 v
   PostgreSQL 17      Gemini REST
   localhost:5432     generativelanguage.googleapis.com
```

Pacote base: `com.curriculo.inteligente`.

| Pacote | Responsabilidade |
|---|---|
| `config` | OpenAPI, Jackson, propriedades |
| `security` | filtro de sessão, dono do recurso, CSRF, CORS local |
| `setup` | estado da chave, teste real de conexão, arquivo restrito |
| `user` | bootstrap e sessão do dono local |
| `candidate` | perfil, consentimento, foto |
| `interview` | entrevista por etapas e mensagens |
| `experience` | experiências e reordenação |
| `education` | formação e certificações |
| `skill` | competências confirmadas e idiomas |
| `resume` | currículo, versão aprovada, resumo |
| `job` | vaga e comparação com o currículo |
| `ai` | porta `LanguageModel`, adaptador Gemini, guarda de saída |
| `export` | HTML do modelo de referência e PDF |
| `storage` | arquivos locais fora do repositório |
| `common` | erros, paginação |
| `audit` | evento sem conteúdo sensível e métrica de chamada de IA |

Não há classes vazias só para preencher essa lista. Cada pacote existe porque tem código de verdade.

Controllers devolvem DTO. Entidade JPA não aparece no JSON.

Transações ficam nos serviços que gravam mais de uma tabela (aprovação grava versão e status; rascunho da entrevista grava perfil, experiências e currículo).

## Fluxo de uma alteração vinda da IA

1. O serviço monta a instrução de sistema fixa e o conteúdo da pessoa em separado.
2. O adaptador chama o Gemini com timeout e, no máximo, uma nova tentativa para falha transitória.
3. O JSON volta para um DTO validado.
4. A guarda compara o resultado com os fatos já gravados.
5. Só então o serviço grava, e a tela pede aprovação da pessoa antes do PDF definitivo.

## Operação sem IA

Entrevista guiada, cadastro, prévia e PDF funcionam sem chave. Reescrita, resumo gerado, perguntas adaptativas e comparação com a vaga exigem a integração e a tela diz isso.

## Documentos irmãos

- Dados: `docs/MODELO_DE_DADOS.md`
- Conversa: `docs/FLUXO_CONVERSACIONAL.md`
- HTTP: `docs/API.md`
- Segurança: `docs/SEGURANCA.md`
- Modelo visual: `docs/REFERENCIA_CURRICULO.md`
- Estado: `docs/ESTADO_DO_PROJETO.md`
