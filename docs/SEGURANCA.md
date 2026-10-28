# Segurança e privacidade

Currículo é dado pessoal. O desenho abaixo vale desde a primeira versão.

## Segredos

- `GEMINI_API_KEY` vem do ambiente ou de `%USERPROFILE%\.curriculo-inteligente\gemini-api-key`.
- O arquivo da chave fica fora do repositório. No Windows a ACL fica restrita ao usuário atual.
- Status, erro e log nunca incluem a chave. Um erro do provedor é reescrito antes de chegar ao cliente.
- `.env` está no `.gitignore`. `.env.example` só tem nomes de variáveis e valores fictícios.

## Acesso

- HTTP apenas em `127.0.0.1`.
- CORS apenas para `http://localhost:4200`, com credencial.
- Cookie de sessão `HttpOnly`. Em loopback o cookie não é marcado `Secure`, porque o ambiente local é HTTP.
- CSRF ativo. O Angular lê o cookie `XSRF-TOKEN` e devolve o cabeçalho `X-XSRF-TOKEN`.
- Bootstrap só funciona enquanto a tabela de usuários está vazia.
- Cada consulta de currículo, entrevista, experiência, vaga e foto inclui o id do usuário da sessão. Não existe busca por id sem esse filtro.
- PDF definitivo e prévia passam pela mesma checagem.

## Entrada

- Bean Validation nos DTOs.
- Texto livre limitado (respostas e descrições de vaga).
- Upload de foto: JPEG, PNG ou WebP, até 5 MB. A imagem é regravada para JPEG, o que descarta metadados EXIF. O arquivo não é servido como estático público.
- HTML do currículo é montado no servidor a partir de campos escapados. Texto da IA não é inserido como HTML cru.
- Resposta do modelo não é executada. Não há interpretador de instrução devolvida pela IA.

## IA e LGPD

- Antes da primeira chamada, a pessoa registra consentimento (`POST /api/v1/profile/consent`). O texto explica que o conteúdo profissional será enviado ao Google para gerar texto.
- A tabela `ai_invocation` guarda finalidade, modelo, status, latência e tamanhos. Não guarda prompt nem resposta.
- Mensagens da entrevista ficam enquanto a entrevista existir, porque são o histórico que a pessoa continua e corrige. Não é armazenado o pacote bruto enviado ao provedor.
- `DELETE /api/v1/account` apaga o usuário e, em cascata, perfil, experiências, currículo, entrevistas, vagas e arquivos de foto.
- Registros de `ai_invocation` com mais de 90 dias são apagados por uma tarefa diária.
- A IA não decide contratação nem rejeição.

## Limites

- Teste de conexão e geração têm limite em memória por usuário, para não queimar a cota por repetição.
- A aplicação não chama o Gemini ao iniciar.

## Logs

Nível de log padrão sem corpo de requisição. O adaptador do Gemini registra status HTTP e o código de erro público, não o corpo quando ele puder conter texto do currículo.
