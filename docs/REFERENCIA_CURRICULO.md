# Referência do currículo

Arquivo original, preservado na raiz do projeto:

`Curriculo_Fernando_Aparecido_Calassio_Rodrigues.pdf`

Há um único modelo na pasta. Nenhum outro PDF ou documento de currículo foi encontrado. Este arquivo não foi alterado e não é copiado para dentro do código da aplicação. Os dados pessoais dele permanecem só no original. O modelo visual é que será reproduzido.

## O que o arquivo é

| Item | Medida |
|---|---|
| Páginas | 1 |
| Tamanho | A4, 595,28 × 841,89 pt |
| Produtor | ReportLab (PDF de texto selecionável, não um scan) |
| Metadados | título e autor preenchidos; creator não informado |
| Fontes embutidas | Arial Bold (`Arial-BoldMT`), Arial (`ArialMT`), Arial Italic (`Arial-ItalicMT`), mais Helvetica base |
| Foto embutida | imagem 800 × 960 px, no canto superior direito |
| Margem esquerda do texto | 48 pt (16,9 mm) |
| Faixa superior até a origem do nome | cerca de 66 pt a partir do topo da página |

## Tipografia medida

| Elemento | Tamanho |
|---|---|
| Nome | 16,5 pt, negrito |
| Cargo desejado | 10,5 pt, negrito |
| Cidade, disponibilidade e contatos | 10 pt |
| Rótulo e lista de tecnologias | 9 pt |
| Título de seção | 12 pt, negrito |
| Corpo do resumo e dos itens | 10,5 pt, entrelinha de cerca de 14,2 pt |
| Linha de projeto | 9,5 pt, itálico |
| Marcador de lista | recuo interno de 14 pt |

Os títulos de seção são azul-marinho, com um fio horizontal logo abaixo. A cor exata não está isolada no texto extraído. A reprodução usa `#1F4E79` até uma comparação visual lado a lado confirmar o RGB. Contatos que são URL aparecem como links sublinhados. O corpo é preto sobre fundo branco.

## Ordem das seções

1. Cabeçalho: nome, cargo desejado, cidade/estado, disponibilidade e contatos (e-mail, telefone, LinkedIn, GitHub), com a foto à direita.
2. Linha de tecnologias, separada por fios horizontais.
3. Resumo profissional.
4. Experiência profissional: empresa, cargo, período mês/ano, linha de projeto em itálico e itens com marcador.
5. Competências técnicas, agrupadas por rótulo em negrito (back-end, front-end, mensageria, observabilidade, CI/CD, automação), uma linha por grupo, com fios entre elas.
6. Formação acadêmica: curso, instituição e situação.
7. Idiomas: nome em negrito e nível.

Não há tabelas de dados, barras de porcentagem, ícones, gráfico de competências nem segunda coluna de texto. A leitura automática encontra texto selecionável e títulos convencionais. O exemplo tem uma experiência detalhada e sobra espaço em branco no fim da página.

## O que a aplicação reproduz

O PDF gerado pelo backend segue este modelo (`template_code = REFERENCIA_A4`):

- A4, uma página, margens próximas de 17 mm, Arial quando a fonte existir na máquina e Helvetica como reserva.
- Mesma ordem de seções, fios sob os títulos, foto opcional no canto superior direito, links clicáveis.
- Texto selecionável, sem elemento decorativo que atrapalhe um ATS.
- Idade só entra se a pessoa pedir. No PDF de referência a idade aparece. No produto ela fica de fora por padrão, como pedido na especificação.

## O que não será copiado

O conteúdo profissional do arquivo original não é semente do banco, não vira fixture de teste com dados reais e não é enviado à IA. Os testes de PDF usam um currículo fictício no mesmo formato.
