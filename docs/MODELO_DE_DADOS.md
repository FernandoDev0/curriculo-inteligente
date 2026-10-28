# Modelo de dados

PostgreSQL 17. Identificadores `uuid`. Horários `timestamptz`. A migração é `database/migrations/V1__init.sql`.

O dono de tudo é `app_user`. Apagar a conta remove o que depende dele.

```text
app_user
  └── candidate_profile          1:1
  └── experience                 1:N
  └── education                  1:N
  └── certification              1:N
  └── skill                      1:N
  └── language_skill             1:N
  └── project_item               1:N
  └── interview                  1:N
        └── interview_message    1:N
  └── resume                     1:N
        └── resume_version       1:N   foto JSON aprovada
  └── job_posting                1:N
  └── job_match                  1:N
```

`audit_event` e `ai_invocation` referenciam o usuário sem cascata obrigatória de conteúdo: a invocação não carrega texto. No delete da conta, as linhas desses dois registros daquele usuário também são removidas pelo serviço, não por um log eterno.

## Regras que o banco ajuda a garantir

- E-mail do usuário é único.
- Perfil é único por usuário.
- Mês, quando informado, está entre 1 e 12.
- Ano de experiência está entre 1950 e 2100.
- Idade, se informada, está entre 14 e 100. `include_age` padrão é falso.
- Competência única por usuário, categoria e nome.
- Idioma único por usuário e nome.
- Versão do currículo única por currículo e número.
- `current_job` verdadeiro convive com fim nulo. Data desconhecida permanece nula. A aplicação não completa data faltante.

## O que não fica no banco

- Chave de API.
- CPF, RG, endereço residencial, dados bancários.
- Pacote bruto enviado ao Gemini.
- Metadados EXIF da foto. A imagem guardada já foi regravada.

## Retenção

- Entrevista e mensagens: enquanto a pessoa não excluir a entrevista ou a conta.
- Versão aprovada: enquanto o currículo existir. É o documento que a exportação usa.
- `ai_invocation`: 90 dias.
- Foto em disco: removida com a foto, com o perfil ou com a conta.

## Seleção para uma página

Não é uma coluna mágica. `ExperienceSelectionPolicy` escolhe, em código testado:

- até duas experiências mais recentes e relevantes como detalhadas;
- as demais entram resumidas se mostrarem continuidade;
- em transição de carreira, projetos, formação e habilidades transferíveis sobem na ordem;
- nada é apagado sem a pessoa confirmar. A política só sugere o que cabe na página.
