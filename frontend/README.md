# Interface

Aplicação Angular da pasta `frontend`. O navegador fala somente com a API Spring em `http://127.0.0.1:8080`, pelo proxy de `/api`. Não há outro backend.

A chave do Gemini não fica no navegador. O programa não grava chave, senha nem texto de currículo em `localStorage` ou `sessionStorage`. O campo de chave envia o valor uma vez para `POST /api/v1/setup/api-key` e em seguida é limpo. A resposta dessa chamada não é mostrada como chave.

## Instalar

Na pasta `frontend`:

```text
npm install
```

É preciso Node.js 24. O projeto foi gerado com o Angular CLI 22 local (`npx`), não com o CLI global.

## Subir

A API precisa estar em `127.0.0.1:8080`.

```text
npm start
```

Isso executa `ng serve` com `proxy.conf.json`, que encaminha `/api` para `http://127.0.0.1:8080`. A interface abre em `http://localhost:4200`.

## Testes

```text
npm test -- --watch=false
```

O runner é o Vitest que o Angular CLI 22 configura, com jsdom. As requisições dos testes são simuladas. Nenhuma chamada real sai da máquina.

O script `scripts/test-frontend.ps1` acrescenta `--browsers=ChromeHeadless`. Esse modo pede um pacote de browser do Vitest (`@vitest/browser-playwright` ou equivalente), que não está instalado. Sem esse pacote, use o comando acima.

## O que a tela faz

Conta local, configuração e teste da chave, entrevista uma pergunta por vez, correção do histórico, edição do currículo, prévia A4, aprovação, PDF e comparação com uma vaga. Sem chave continuam a entrevista, o cadastro, a prévia e o PDF. Reescrita, resumo gerado, aprofundamento adaptativo e a formulação da comparação com a vaga pedem a chave e o consentimento.
