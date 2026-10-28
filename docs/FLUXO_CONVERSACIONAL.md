# Fluxo conversacional

A entrevista mostra uma pergunta por vez. O progresso, o histórico e a prévia ficam visíveis. Dá para corrigir uma resposta anterior, pular o que é opcional, pausar e voltar. Descartar pede confirmação.

Sem chave do Gemini, o roteiro fixo abaixo ainda funciona. Com chave e consentimento, uma resposta curta ou genérica pode gerar no máximo duas perguntas de aprofundamento. A IA para quando já há empresa, cargo, período, atividades e ferramentas, ou quando a pessoa pula.

## Etapa A — identificação

| Chave | Pergunta | Obrigatória |
|---|---|---|
| `full_name` | Qual é o seu nome completo? | sim |
| `target_role` | Qual cargo você deseja ocupar? | sim |
| `current_profession` | Qual é sua profissão ou área de atuação atual? | não |
| `city_state` | Em qual cidade e estado você mora? | não |
| `email` | Qual é seu e-mail profissional? | não |
| `phone` | Qual é seu telefone ou WhatsApp profissional? | não |
| `linkedin` | Você possui LinkedIn? | não |
| `other_links` | Deseja incluir GitHub, portfólio ou outras redes profissionais? | não |
| `work_mode` | Você está disponível para trabalho remoto, híbrido ou presencial? | não |
| `target_opportunity` | Já existe uma vaga ou empresa de interesse? | não |
| `age_choice` | Deseja informar a idade? Por padrão ela não entra no currículo. | não |
| `career_change` | Você está mudando de profissão? | não |

Não se pede CPF, RG, endereço completo nem dados bancários. Rede pessoal só é aceita se a pessoa incluir em `other_links`. A interface recomenda deixar de fora quando não ajudar a vaga.

## Etapa B e C — experiências

Para a experiência mais recente, e depois para cada outra que a pessoa quiser contar:

`company`, `role_title`, `period` (mês e ano; dia não é exigido), `day_in_the_life`, `activities`, `tools`, `problems`, `people`, `responsibility`, `autonomy`, `results`.

Se a resposta de atividades tiver menos de 40 caracteres ou for uma frase genérica conhecida (“ajudava a equipe”, “mexia com planilha”, “fazia de tudo”), o sistema faz uma pergunta de concretude. Com IA, a pergunta usa o cargo já informado. Sem IA, usa o texto fixo: pedir tarefas reais, sem completar o que a pessoa não fez.

A pessoa diz quando não há mais empregos. A política de uma página marca o destaque. Nada é inventado para tapar lacuna.

## Etapa D — formação e idiomas

Escolaridade, ensino médio, curso, instituição, situação (concluído, em andamento, interrompido), ano ou previsão, cursos e certificações, idiomas com compreensão, leitura, escrita e conversação separados. O sistema não promove ninguém a fluente.

## Etapa E — competências

O sistema lista candidatos a competência só a partir do que já foi dito (ferramentas citadas, não sinônimos ambiciosos). Cada item entra como `confirmed = false` até a pessoa aceitar. “Trabalhava em equipe” não vira liderança.

## Etapa F e G — resumo e reescrita

O resumo e a reescrita dos itens só rodam com IA e consentimento. A resposta traz `original` e `revised`. A pessoa edita, pede outra versão ou aprova. Sem IA, o texto dela segue para a prévia, com aviso de que a revisão automática não rodou.

Se houver descrição de vaga, a comparação devolve quatro listas: requisitos já demonstrados, competências relacionadas, requisitos sem comprovação e palavras que já estão sustentadas pelo histórico. Não há promessa de aprovação em ATS.

## Etapa H — transição

Se `career_change` for sim: profissão desejada, motivo, habilidades transferíveis, cursos, projetos pessoais, portfólio, voluntariado. Projeto pessoal não é gravado como emprego. Vai para `project_item`.

## Etapa I — foto

Opcional. Envio da própria foto, revisão e aprovação. Tratamento por IA só se o modelo configurado realmente anunciar edição de imagem.

## Etapa J — prévia

A prévia A4 usa o mesmo HTML do PDF. Exportar o arquivo definitivo exige aprovação e uma página. Se não couber, a API pede corte de conteúdo. Não reduz fonte abaixo de 9 pt.
