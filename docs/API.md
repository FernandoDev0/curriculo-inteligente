# API HTTP

Base: `http://127.0.0.1:8080`. Prefixo `/api/v1`. JSON. Sessão por cookie. OpenAPI em `/swagger-ui.html`.

Erro:

```json
{
  "code": "VALIDATION",
  "message": "Revise os campos.",
  "fields": { "email": "Informe um e-mail válido." }
}
```

Códigos usados: `VALIDATION`, `UNAUTHENTICATED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `KEY_MISSING`, `CONSENT_REQUIRED`, `AI_UNAVAILABLE`, `AI_INVALID_OUTPUT`, `QUOTA`, `PAGE_OVERFLOW`, `NOT_APPROVED`, `PAYLOAD_TOO_LARGE`.

Lista paginada:

```json
{ "content": [], "page": 0, "size": 20, "totalElements": 0, "totalPages": 0 }
```

## Sessão

`GET /api/v1/auth/session` público.

```json
{ "authenticated": false, "needsBootstrap": true, "email": null }
```

`POST /api/v1/auth/bootstrap` público só com tabela vazia. Senha mínima de 10 caracteres.

```json
{ "email": "voce@exemplo.com", "password": "uma frase longa", "fullName": "Nome" }
```

`POST /api/v1/auth/login` `{ "email", "password" }`.

`POST /api/v1/auth/logout`.

`DELETE /api/v1/account` apaga a conta, os dados e as fotos.

## Configuração do Gemini

`GET /api/v1/setup/status` público. Não devolve a chave.

```json
{
  "keyConfigured": false,
  "keySource": "NONE",
  "model": "gemini-3.5-flash",
  "imageEditingAvailable": false,
  "state": "KEY_MISSING",
  "lastTestAt": null,
  "lastTestMessage": "Nenhuma chave foi configurada."
}
```

`state`: `KEY_MISSING`, `UNTESTED`, `TESTING` não é persistido, `VALIDATED`, `QUOTA`, `INVALID_CREDENTIALS`, `CONNECTIVITY`, `UNAVAILABLE`.

`POST /api/v1/setup/api-key` autenticado. Corpo `{ "apiKey": "..." }`. Grava fora do banco. Resposta é o status, sem a chave.

`DELETE /api/v1/setup/api-key` remove só o arquivo. Não apaga variável de ambiente.

`POST /api/v1/setup/test-connection` autenticado. Faz `models.get` e um `generateContent` curto de verdade. Não simula sucesso.

## Perfil

`GET /api/v1/profile` e `PUT /api/v1/profile`.

```json
{
  "fullName": "",
  "targetRole": "",
  "currentProfession": null,
  "city": null,
  "state": null,
  "professionalEmail": null,
  "phone": null,
  "linkedinUrl": null,
  "githubUrl": null,
  "portfolioUrl": null,
  "otherProfessionalLinks": null,
  "workMode": "REMOTE",
  "targetOpportunity": null,
  "includeAge": false,
  "age": null,
  "careerChange": false,
  "desiredProfession": null,
  "careerChangeReason": null,
  "photoApproved": false,
  "hasPhoto": false,
  "aiConsentAt": null
}
```

`workMode`: `REMOTE`, `HYBRID`, `ONSITE`, `FLEXIBLE` ou null.

`POST /api/v1/profile/consent` registra o consentimento para envio ao provedor de IA.

`POST /api/v1/profile/photo` multipart `file`. `DELETE /api/v1/profile/photo`. `POST /api/v1/profile/photo/approve`. `GET /api/v1/profile/photo` devolve a imagem autenticada.

`POST /api/v1/profile/photo/edit` só se a edição existir. Corpo `{ "background": "LIGHT" | "DARK" | "OTHER" }`. Se o modelo não edita imagem, `409` com `AI_UNAVAILABLE` e a foto original permanece.

## Coleções do perfil

O mesmo desenho para experiências, formação, certificações, competências, idiomas e projetos.

- `GET /api/v1/experiences`
- `POST /api/v1/experiences`
- `PUT /api/v1/experiences/{id}`
- `DELETE /api/v1/experiences/{id}`
- `PUT /api/v1/experiences/order` corpo `{ "ids": ["uuid"] }`

Experiência:

```json
{
  "company": "Padaria Sol",
  "roleTitle": "Padeiro",
  "startYear": 2022,
  "startMonth": 3,
  "endYear": null,
  "endMonth": null,
  "currentJob": true,
  "projectLabel": null,
  "responsibilities": "Preparava pães e controlava o forno.",
  "tools": "Forno, balança",
  "results": null,
  "notes": null,
  "sortOrder": 0,
  "detailPreference": "AUTO"
}
```

`detailPreference`: `AUTO`, `DETAILED`, `SUMMARY`, `HIDDEN`.

Formação: `level`, `course`, `institution`, `status` (`COMPLETED`, `IN_PROGRESS`, `INTERRUPTED`), `startYear`, `endYear`, `sortOrder`.

Certificação: `name`, `issuer`, `year`, `sortOrder`.

Competência: `name`, `category` (`TECHNICAL`, `TOOL`, `DOMAIN`, `BEHAVIORAL`), `confirmed`, `origin` (`USER`, `SUGGESTED`), `sortOrder`. Sugestão nasce com `confirmed: false`.

Idioma: `name`, `comprehension`, `reading`, `writing`, `conversation`. Níveis `BASIC`, `INTERMEDIATE`, `ADVANCED`, `FLUENT`, `NATIVE` ou null.

Projeto: `name`, `description`, `url`, `personal` (padrão true), `sortOrder`.

Rotas irmãs: `/api/v1/education`, `/api/v1/certifications`, `/api/v1/skills`, `/api/v1/languages`, `/api/v1/projects`.

`POST /api/v1/skills/suggest` olha o histórico e devolve sugestões não confirmadas. Sem IA, a sugestão é só a lista de ferramentas já escritas pela pessoa.

## Entrevista

`POST /api/v1/interviews` cria e devolve a primeira pergunta.

`GET /api/v1/interviews?page=0&size=20`

`GET /api/v1/interviews/{id}`

```json
{
  "id": "uuid",
  "status": "IN_PROGRESS",
  "stage": "IDENTIFICATION",
  "questionKey": "full_name",
  "progressPercent": 4,
  "currentQuestion": {
    "key": "full_name",
    "text": "Qual é o seu nome completo?",
    "required": true,
    "canSkip": false
  },
  "messages": [
    {
      "id": "uuid",
      "role": "ASSISTANT",
      "stage": "IDENTIFICATION",
      "questionKey": "full_name",
      "content": "Qual é o seu nome completo?",
      "skipped": false,
      "createdAt": "2026-10-27T12:00:00Z"
    }
  ]
}
```

`status`: `IN_PROGRESS`, `PAUSED`, `COMPLETED`.

`POST /api/v1/interviews/{id}/messages`

```json
{ "content": "Ana Souza", "skip": false }
```

Resposta: a entrevista atualizada, já com a próxima pergunta. `skip: true` só em pergunta opcional.

`PUT /api/v1/interviews/{id}/messages/{messageId}` corrige uma resposta da pessoa e recalcula o estado a partir dali.

`POST /api/v1/interviews/{id}/pause` e `POST /api/v1/interviews/{id}/resume`.

`DELETE /api/v1/interviews/{id}` pede que o cliente tenha confirmado. O servidor apaga.

`POST /api/v1/interviews/{id}/resume-draft` grava perfil, experiências e um currículo `DRAFT`. Devolve o currículo. Não chama exportação.

## Currículo

`POST /api/v1/resumes` `{ "title": "Currículo" }`.

`GET /api/v1/resumes?page=0&size=20`

`GET /api/v1/resumes/{id}` inclui o snapshot montado para a prévia: cabeçalho, resumo, experiências com `detailLevel` `DETAILED` ou `SUMMARY`, competências confirmadas, formação, idiomas, projetos.

`PUT /api/v1/resumes/{id}` altera título, `targetRole`, `summaryDraft`, `templateCode`, `includePhoto`.

`POST /api/v1/resumes/{id}/summary` gera outra versão do resumo. Exige consentimento e chave. Devolve `{ "original", "revised" }` e grava `summaryDraft` com o texto revisado, sem aprovar.

`POST /api/v1/resumes/{id}/review`

```json
{ "section": "EXPERIENCE", "experienceId": "uuid", "text": "Eu ficava vendo os pedidos." }
```

Resposta `{ "original", "revised" }`. Não substitui o texto até `PUT` na experiência. `section` também aceita `SUMMARY`.

`POST /api/v1/resumes/{id}/approve` grava `resume_version.snapshot` com os dados atuais, copia o resumo para `summary_approved`, status `APPROVED`. Exige que os obrigatórios do cabeçalho existam (nome e cargo).

`POST /api/v1/resumes/{id}/preview/pdf` devolve `application/pdf` e o cabeçalho `X-Page-Count`. Pode ser usado antes da aprovação.

`POST /api/v1/resumes/{id}/export/pdf` só com status `APPROVED` e `X-Page-Count = 1`. Senão `409` `NOT_APPROVED` ou `PAGE_OVERFLOW`. O corpo do conflito inclui `{ "pageCount", "hints": ["..."] }`. O nome do arquivo é `curriculo.pdf`, sem o nome da pessoa.

## Vaga

`POST /api/v1/jobs` `{ "title", "company", "description" }`.

`GET /api/v1/jobs/{id}`

`POST /api/v1/resumes/{id}/job-match` `{ "jobId" }`.

```json
{
  "demonstrated": ["Java"],
  "related": ["Spring Boot"],
  "missing": ["Kubernetes"],
  "keywords": ["API REST"],
  "suggestions": "O texto já cita API. Dá para deixar isso explícito no item em que você descreveu o serviço."
}
```

Sem IA, a comparação é lexical: termos da vaga que já aparecem no currículo entram em `demonstrated` ou `keywords`. O que não aparece entra em `missing`. Nada é inventado para preencher `missing`.
