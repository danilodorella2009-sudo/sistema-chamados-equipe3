-- =====================================================================
-- Sistema de Gestão de Chamados - EQUIPE 3
-- DML - Carga inicial (seed) - MySQL 8+
-- Senha de TODOS os usuários: 123456   (hash BCrypt abaixo)
-- Datas relativas a NOW() para que os prazos de SLA façam sentido
-- no momento da demonstração.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Perfis
-- ---------------------------------------------------------------------
INSERT INTO perfil (id, nome, descricao) VALUES
(1, 'SOLICITANTE',              'Usuário do cliente: abre, acompanha e valida chamados'),
(2, 'TECNICO',                  'Analista de suporte (N1, N2 ou N3)'),
(3, 'ADMINISTRADOR_SUPERVISOR', 'Supervisor: visão geral, dashboard, pode atuar em qualquer nível e encerrar chamados');

-- ---------------------------------------------------------------------
-- Grupos / Níveis de suporte
-- ---------------------------------------------------------------------
INSERT INTO grupo (id, sigla, nome, descricao, nivel) VALUES
(1, 'N1', 'Service Desk - Triagem',          'Triagem, dúvidas, acessos, procedimentos da base de conhecimento', 1),
(2, 'N2', 'Suporte Técnico / Infraestrutura', 'Análise técnica intermediária: logs, banco de dados, servidores, integrações', 2),
(3, 'N3', 'Especialistas / Desenvolvimento', 'Correção de bugs em código, melhorias e novas funcionalidades', 3);

-- ---------------------------------------------------------------------
-- Categorias
-- ---------------------------------------------------------------------
INSERT INTO categoria (id, nome, descricao) VALUES
(1, 'Aplicação web',                'Erros e dúvidas nas aplicações web dos clientes'),
(2, 'API',                          'APIs REST disponibilizadas aos clientes'),
(3, 'Banco de dados',               'Desempenho, consistência e disponibilidade de bancos de dados'),
(4, 'Acesso',                       'Login, senha, bloqueio e permissões'),
(5, 'Bug',                          'Defeito de software confirmado'),
(6, 'Solicitação de melhoria',      'Nova funcionalidade ou alteração de comportamento'),
(7, 'Integrações',                  'Integração com ERPs, gateways e sistemas de terceiros'),
(8, 'Infraestrutura de aplicação',  'Servidores, deploy, agentes e componentes de execução');

-- ---------------------------------------------------------------------
-- SLA (prazo de solução por prioridade)
-- ---------------------------------------------------------------------
INSERT INTO sla (prioridade, horas_solucao) VALUES
('BAIXA',   48),
('MEDIA',   24),
('ALTA',     8),
('CRITICA',  2);

-- ---------------------------------------------------------------------
-- Usuários (senha 123456)
-- 1 supervisor + 6 técnicos (N1, N1, N2, N2, N3, N3) + 9 solicitantes
-- ---------------------------------------------------------------------
INSERT INTO usuario (id, nome, email, senha, empresa, perfil_id, grupo_id, ativo) VALUES
(1,  'Fernanda Lima',    'supervisor@softhouse.com',       '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Software House', 3, NULL, TRUE),
(2,  'Ana Souza',        'ana.n1@softhouse.com',           '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Software House', 2, 1, TRUE),
(3,  'Bruno Costa',      'bruno.n1@softhouse.com',         '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Software House', 2, 1, TRUE),
(4,  'Carla Mendes',     'carla.n2@softhouse.com',         '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Software House', 2, 2, TRUE),
(5,  'Daniel Rocha',     'daniel.n3@softhouse.com',        '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Software House', 2, 3, TRUE),
(6,  'Eduardo Alves',    'eduardo.n3@softhouse.com',       '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Software House', 2, 3, TRUE),
(7,  'João Pereira',     'joao@alfavarejo.com.br',         '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Alfa Varejo',      1, NULL, TRUE),
(8,  'Maria Oliveira',   'maria@alfavarejo.com.br',        '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Alfa Varejo',      1, NULL, TRUE),
(9,  'Pedro Santos',     'pedro@betalogistica.com.br',     '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Beta Logística',   1, NULL, TRUE),
(10, 'Juliana Ferreira', 'juliana@betalogistica.com.br',   '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Beta Logística',   1, NULL, TRUE),
(11, 'Lucas Martins',    'lucas@gamasaude.com.br',         '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Gama Saúde',       1, NULL, TRUE),
(12, 'Patrícia Gomes',   'patricia@gamasaude.com.br',      '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Gama Saúde',       1, NULL, TRUE),
(13, 'Rafael Ribeiro',   'rafael@deltaeducacao.com.br',    '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Delta Educação',   1, NULL, TRUE),
(14, 'Camila Araújo',    'camila@deltaeducacao.com.br',    '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Delta Educação',   1, NULL, TRUE),
(15, 'Thiago Barbosa',   'thiago@epsilonfinanceira.com.br','$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Epsilon Financeira', 1, NULL, TRUE),
(16, 'Gustavo Nunes',    'gustavo.n2@softhouse.com',       '$2a$10$PQq8fUM3cn67nfbVLip2g.NNqk2gjUNHaSahgw0QHSG9P5HHjqlu.', 'Software House', 2, 2, TRUE);

-- ---------------------------------------------------------------------
-- Base de conhecimento (6 artigos)
-- ---------------------------------------------------------------------
INSERT INTO artigo_conhecimento (id, titulo, problema, solucao, palavras_chave, categoria_id, autor_id, data_criacao) VALUES
(1, 'Desbloqueio de usuário e redefinição de senha',
 'Usuário não consegue acessar o sistema: "usuário bloqueado" após 5 tentativas ou senha expirada (90 dias).',
 '1) Confirme a identidade do usuário (e-mail corporativo + empresa).\n2) No Painel Administrativo do cliente, acesse Usuários > localizar > "Desbloquear".\n3) Clique em "Enviar link de redefinição" – o link expira em 30 minutos.\n4) Oriente o usuário a criar senha com 8+ caracteres, maiúscula, número e símbolo.\n5) Peça para o usuário testar o login com o chamado ainda aberto.',
 'senha, bloqueio, login, acesso, expirada', 4, 2, TIMESTAMPADD(DAY, -60, NOW())),
(2, 'Liberação de acesso a novo módulo / perfil',
 'Usuário acessa o sistema mas não visualiza um módulo (ex.: Financeiro, Relatórios).',
 '1) Solicitar aprovação do gestor do usuário no próprio chamado (comentário).\n2) Com a aprovação registrada, acessar Painel Administrativo > Perfis > adicionar o perfil do módulo.\n3) Solicitar que o usuário faça logout/login para recarregar as permissões.',
 'permissão, perfil, módulo, acesso, autorização', 4, 3, TIMESTAMPADD(DAY, -55, NOW())),
(3, 'Como exportar relatórios em Excel ou PDF',
 'Dúvida: como exportar relatórios do portal web para Excel/PDF e por que a exportação vem vazia.',
 '1) Acesse Relatórios > escolha o relatório > aplique os filtros.\n2) Clique em "Pesquisar" ANTES de exportar (a exportação usa o resultado da pesquisa).\n3) Use o botão "Exportar" > Excel (.xlsx) ou PDF.\n4) Relatórios com mais de 50 mil linhas são enviados por e-mail em até 15 minutos.',
 'relatório, exportar, excel, pdf, dúvida', 1, 2, TIMESTAMPADD(DAY, -50, NOW())),
(4, 'Erro 500 após sessão expirada (cache/cookies)',
 'Usuário recebe "Erro 500 - Internal Server Error" ao voltar para uma aba aberta há muito tempo.',
 '1) Solicite que o usuário limpe cache e cookies do domínio (Ctrl+Shift+Del) ou use aba anônima.\n2) Faça novo login.\n3) Se o erro persistir para vários usuários ou em telas específicas, NÃO é este caso: coletar horário, URL e print, e escalar para o N2 analisar os logs do servidor.',
 'erro 500, sessão, cookie, cache, internal server error', 1, 3, TIMESTAMPADD(DAY, -45, NOW())),
(5, 'Geração e renovação de token de acesso à API',
 'Integração do cliente recebe HTTP 401 (Unauthorized) ao chamar a API.',
 '1) Verifique a validade do token no Portal do Desenvolvedor > Credenciais.\n2) Tokens expiram em 24h: gere um novo com POST /oauth/token (client_id + client_secret).\n3) Confirme que o header enviado é "Authorization: Bearer <token>".\n4) Se o token é válido e o erro persiste, escalar para o N2 com o request-id da resposta.',
 'api, token, 401, oauth, credencial, integração', 2, 4, TIMESTAMPADD(DAY, -40, NOW())),
(6, 'Procedimento de atendimento via suporte remoto',
 'Problemas que só podem ser diagnosticados na estação do usuário (agentes locais, impressão, certificados).',
 '1) Técnico solicita o acesso remoto pelo chamado (botão "Solicitar acesso remoto").\n2) O SOLICITANTE precisa AUTORIZAR no chamado – sem autorização registrada a sessão não pode ser iniciada.\n3) Usuário informa o ID do AnyDesk; técnico conecta e realiza o diagnóstico.\n4) Registrar no chamado o log do diagnóstico e as ações realizadas.\n5) Encerrar a sessão e pedir que o usuário teste.',
 'remoto, anydesk, acesso remoto, diagnóstico, estação', 8, 2, TIMESTAMPADD(DAY, -35, NOW()));

-- =====================================================================
-- OS 10 CHAMADOS DE TESTE
-- Matriz: soma dos pesos (1..4) de Impacto + Urgência
--   2-3 = BAIXA (48h) | 4 = MEDIA (24h) | 5-6 = ALTA (8h) | 7-8 = CRITICA (2h)
-- =====================================================================
INSERT INTO chamado (id, titulo, descricao, tipo, categoria_id, impacto, urgencia, prioridade, status,
                     solicitante_id, grupo_atual_id, tecnico_id, data_abertura, prazo_sla, data_solucao, data_fechamento,
                     solucao, referencia_versao, artigo_id,
                     suporte_remoto, remoto_autorizado, remoto_autorizado_em, remoto_ferramenta, remoto_diagnostico) VALUES
-- 1) Erro 500 na aplicação web -> Incidente / Alta / N1 (em atendimento)
(1, 'Erro 500 na aplicação web',
 'Ao finalizar o pedido na loja virtual, todos os vendedores da filial recebem "Erro 500 - Internal Server Error". Começou hoje às 09h.',
 'INCIDENTE', 1, 'ALTO', 'ALTA', 'ALTA', 'EM_ATENDIMENTO',
 7, 1, 2, TIMESTAMPADD(MINUTE, -120, NOW()), TIMESTAMPADD(MINUTE, 360, NOW()), NULL, NULL,
 NULL, NULL, NULL, FALSE, FALSE, NULL, NULL, NULL),

-- 2) API retornando dados incorretos -> Bug / escalado N1 -> N3
(2, 'API retornando dados incorretos',
 'GET /api/v2/pedidos está retornando o campo valor_total sem aplicar o desconto do cupom. Nosso ERP está faturando valores errados.',
 'BUG', 2, 'MEDIO', 'MEDIA', 'MEDIA', 'EM_ATENDIMENTO',
 9, 3, 5, TIMESTAMPADD(MINUTE, -1200, NOW()), TIMESTAMPADD(MINUTE, 240, NOW()), NULL, NULL,
 NULL, NULL, NULL, FALSE, FALSE, NULL, NULL, NULL),

-- 3) Usuário sem acesso -> Solicitação de Serviço / N1 via Base de Conhecimento (fechado)
(3, 'Usuário sem acesso ao sistema',
 'Não consigo entrar no sistema, aparece "usuário bloqueado". Preciso emitir as notas do dia.',
 'SOLICITACAO_DE_SERVICO', 4, 'MEDIO', 'MEDIA', 'MEDIA', 'FECHADO',
 8, 1, 2, TIMESTAMPADD(MINUTE, -4320, NOW()), TIMESTAMPADD(MINUTE, -2880, NOW()), TIMESTAMPADD(MINUTE, -4280, NOW()), TIMESTAMPADD(MINUTE, -4200, NOW()),
 'Usuário desbloqueado e link de redefinição de senha enviado conforme artigo KB #1. Usuário confirmou acesso.', NULL, 1,
 FALSE, FALSE, NULL, NULL, NULL),

-- 4) Consulta lenta no banco -> Incidente / escalado N1 -> N2 (solucionado, aguardando validação)
(4, 'Consulta lenta no banco de dados',
 'O relatório de pedidos por cliente está demorando mais de 30 segundos e às vezes dá timeout.',
 'INCIDENTE', 3, 'ALTO', 'MEDIA', 'ALTA', 'SOLUCIONADO',
 11, 2, 4, TIMESTAMPADD(MINUTE, -360, NOW()), TIMESTAMPADD(MINUTE, 120, NOW()), TIMESTAMPADD(MINUTE, -60, NOW()), NULL,
 'Plano de execução mostrava full scan na tabela pedidos (2,3 milhões de linhas). Criado índice idx_pedidos_cliente_data (cliente_id, data_emissao). Consulta caiu de 38s para 0,4s.', NULL, NULL,
 FALSE, FALSE, NULL, NULL, NULL),

-- 5) Bug que afeta um cliente -> Bug / Médio-Alta => ALTA (aberto, aguardando triagem)
(5, 'Bug no boletim escolar de um cliente',
 'No portal do aluno, a média final está sendo arredondada para baixo (7,95 aparece como 7,9). Afeta apenas a nossa escola.',
 'BUG', 5, 'MEDIO', 'ALTA', 'ALTA', 'ABERTO',
 13, 1, NULL, TIMESTAMPADD(MINUTE, -30, NOW()), TIMESTAMPADD(MINUTE, 450, NOW()), NULL, NULL,
 NULL, NULL, NULL, FALSE, FALSE, NULL, NULL, NULL),

-- 6) Bug crítico que afeta vários clientes -> Crítica / entra direto no N2 pelo cálculo -> N3 / encerrado com release
(6, 'Bug crítico: cálculo de frete zerado para todos os clientes',
 'Após a atualização de ontem, o checkout de TODOS os clientes está calculando frete R$ 0,00. Prejuízo em andamento.',
 'BUG', 5, 'CRITICO', 'CRITICA', 'CRITICA', 'FECHADO',
 15, 3, 5, TIMESTAMPADD(MINUTE, -7200, NOW()), TIMESTAMPADD(MINUTE, -7080, NOW()), TIMESTAMPADD(MINUTE, -7090, NOW()), TIMESTAMPADD(MINUTE, -7000, NOW()),
 'Defeito introduzido na v3.4.0: conversão de peso de gramas para kg retornava inteiro (divisão inteira), zerando o frete. Corrigido o cálculo, criado teste automatizado de regressão e publicado hotfix em produção.',
 'Hotfix v3.4.1 - commit 8f2c1e9 (deploy em produção)', NULL,
 FALSE, FALSE, NULL, NULL, NULL),

-- 7) Dúvida funcional -> Dúvida / resolvida no N1 (fechado)
(7, 'Dúvida: como exportar relatório para Excel',
 'Quando clico em Exportar o arquivo vem vazio. Como faço para exportar o relatório de entregas?',
 'DUVIDA', 1, 'BAIXO', 'BAIXA', 'BAIXA', 'FECHADO',
 10, 1, 3, TIMESTAMPADD(MINUTE, -2880, NOW()), NOW(), TIMESTAMPADD(MINUTE, -2850, NOW()), TIMESTAMPADD(MINUTE, -2700, NOW()),
 'Orientado o usuário a clicar em "Pesquisar" antes de "Exportar" (artigo KB #3). Exportação funcionou.', NULL, 3,
 FALSE, FALSE, NULL, NULL, NULL),

-- 8) Chamado com suporte remoto -> sessão autorizada com log de diagnóstico (fechado)
(8, 'Impressão de etiquetas parou na estação do faturamento',
 'O módulo de impressão de etiquetas não imprime mais na estação FAT-02 desde a atualização do Windows.',
 'INCIDENTE', 8, 'MEDIO', 'MEDIA', 'MEDIA', 'FECHADO',
 12, 1, 2, TIMESTAMPADD(MINUTE, -1440, NOW()), NOW(), TIMESTAMPADD(MINUTE, -1380, NOW()), TIMESTAMPADD(MINUTE, -1300, NOW()),
 'Serviço PrintAgent reiniciado e configurado como automático; criada regra de firewall liberando a porta 9100. Etiqueta de teste impressa com sucesso.', NULL, 6,
 TRUE, TRUE, TIMESTAMPADD(MINUTE, -1415, NOW()), 'AnyDesk',
 'Sessão AnyDesk autorizada pela solicitante Patrícia Gomes.\nServiço "PrintAgent" encontrado PARADO (startup = manual após update KB5034441).\nTeste de porta: 9100/TCP bloqueada pelo Windows Firewall.\nAções: serviço iniciado e definido como automático; regra de entrada criada para a porta 9100.\nTeste: etiqueta impressa com sucesso. Sessão encerrada.'),

-- 9) Escalonamento N1 -> N2 -> N3 com rastreabilidade completa (fechado)
(9, 'Notas fiscais não sincronizam com o ERP',
 'Desde ontem as notas emitidas no portal não aparecem no ERP. A integração não mostra erro na tela.',
 'INCIDENTE', 7, 'ALTO', 'ALTA', 'ALTA', 'FECHADO',
 14, 3, 6, TIMESTAMPADD(MINUTE, -5760, NOW()), TIMESTAMPADD(MINUTE, -5280, NOW()), TIMESTAMPADD(MINUTE, -5300, NOW()), TIMESTAMPADD(MINUTE, -5100, NOW()),
 'Conector de integração ajustado para o novo layout XML do ERP (campo cMunFG) e reprocessadas as 312 notas pendentes. Sincronização normalizada.',
 'Release v2.8.3 - commit 4b7d2a1', NULL,
 FALSE, FALSE, NULL, NULL, NULL),

-- 10) Solicitação de nova funcionalidade -> Melhoria / encaminhada direto ao N3 (aberto)
(10, 'Nova funcionalidade: exportar pedidos em CSV pela API',
 'Gostaríamos de um endpoint que retorne os pedidos do mês em CSV para importação automática na nossa planilha de BI.',
 'SOLICITACAO_DE_MELHORIA', 6, 'BAIXO', 'MEDIA', 'BAIXA', 'ABERTO',
 9, 3, NULL, TIMESTAMPADD(MINUTE, -60, NOW()), TIMESTAMPADD(MINUTE, 2820, NOW()), NULL, NULL,
 NULL, NULL, NULL, FALSE, FALSE, NULL, NULL, NULL);

-- =====================================================================
-- HISTÓRICO / TRAMITAÇÕES dos 10 chamados
-- (usuario_id NULL = ação automática do sistema)
-- =====================================================================
INSERT INTO historico_chamado (chamado_id, usuario_id, data_hora, acao, grupo_origem_id, grupo_destino_id, status_anterior, status_novo, descricao) VALUES
-- #1
(1, 7, TIMESTAMPADD(MINUTE, -120, NOW()), 'ABERTURA',      NULL, 1, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(1, NULL, TIMESTAMPADD(MINUTE, -120, NOW()), 'CLASSIFICACAO', NULL, 1, NULL, NULL, 'Classificação automática: tipo Incidente, categoria Aplicação web, impacto Alto, urgência Alta => prioridade Alta (SLA 8h). Encaminhado para N1.'),
(1, 2, TIMESTAMPADD(MINUTE, -110, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Ana Souza (N1).'),
(1, 2, TIMESTAMPADD(MINUTE, -90,  NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'Artigo KB #4 (cache/cookies) aplicado sem sucesso; erro ocorre para todos os usuários. Coletando horário, URL e prints para análise.'),

-- #2
(2, 9, TIMESTAMPADD(MINUTE, -1200, NOW()), 'ABERTURA',      NULL, 1, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(2, NULL, TIMESTAMPADD(MINUTE, -1200, NOW()), 'CLASSIFICACAO', NULL, 1, NULL, NULL, 'Classificação automática: tipo Bug, categoria API, impacto Médio, urgência Média => prioridade Média (SLA 24h). Encaminhado para N1.'),
(2, 3, TIMESTAMPADD(MINUTE, -1185, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Bruno Costa (N1).'),
(2, 3, TIMESTAMPADD(MINUTE, -1170, NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'Reproduzido em homologação: pedido 98231 com cupom de 10% retorna valor_total sem desconto. Token e parâmetros corretos.'),
(2, 3, TIMESTAMPADD(MINUTE, -1160, NOW()), 'ESCALONAMENTO', 1, 3, 'EM_ATENDIMENTO', 'ABERTO', 'Escalonado de N1 para N3. Justificativa: defeito de código confirmado e reproduzível (evidência anexada) – enviado direto ao desenvolvimento.'),
(2, 5, TIMESTAMPADD(MINUTE, -1100, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Daniel Rocha (N3).'),
(2, 5, TIMESTAMPADD(MINUTE, -600,  NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'Causa identificada: PedidoDTO calcula o total antes de aplicar o cupom. Correção em code review (PR #412).'),

-- #3
(3, 8, TIMESTAMPADD(MINUTE, -4320, NOW()), 'ABERTURA',      NULL, 1, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(3, NULL, TIMESTAMPADD(MINUTE, -4320, NOW()), 'CLASSIFICACAO', NULL, 1, NULL, NULL, 'Classificação automática: tipo Solicitação de Serviço, categoria Acesso, impacto Médio, urgência Média => prioridade Média (SLA 24h). Encaminhado para N1.'),
(3, 2, TIMESTAMPADD(MINUTE, -4310, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Ana Souza (N1).'),
(3, 2, TIMESTAMPADD(MINUTE, -4280, NOW()), 'SOLUCAO',       NULL, NULL, 'EM_ATENDIMENTO', 'SOLUCIONADO', 'Solução registrada: Usuário desbloqueado e link de redefinição enviado | Artigo KB #1. Aguardando validação do solicitante.'),
(3, 8, TIMESTAMPADD(MINUTE, -4200, NOW()), 'VALIDACAO_APROVADA', NULL, NULL, NULL, NULL, 'Solicitante confirmou a solução. Comentário: Consegui acessar, obrigada!'),
(3, 8, TIMESTAMPADD(MINUTE, -4200, NOW()), 'ENCERRAMENTO',  NULL, NULL, 'SOLUCIONADO', 'FECHADO', 'Chamado encerrado após validação do solicitante.'),

-- #4
(4, 11, TIMESTAMPADD(MINUTE, -360, NOW()), 'ABERTURA',      NULL, 1, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(4, NULL, TIMESTAMPADD(MINUTE, -360, NOW()), 'CLASSIFICACAO', NULL, 1, NULL, NULL, 'Classificação automática: tipo Incidente, categoria Banco de dados, impacto Alto, urgência Média => prioridade Alta (SLA 8h). Encaminhado para N1.'),
(4, 3, TIMESTAMPADD(MINUTE, -350, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Bruno Costa (N1).'),
(4, 3, TIMESTAMPADD(MINUTE, -340, NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'Lentidão confirmada apenas no relatório de pedidos por cliente; demais telas normais. Não há artigo na base para o caso.'),
(4, 3, TIMESTAMPADD(MINUTE, -330, NOW()), 'ESCALONAMENTO', 1, 2, 'EM_ATENDIMENTO', 'ABERTO', 'Escalonado de N1 para N2. Justificativa: requer análise de plano de execução e acesso ao banco de dados – fora da alçada do N1.'),
(4, 4, TIMESTAMPADD(MINUTE, -300, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Carla Mendes (N2).'),
(4, 4, TIMESTAMPADD(MINUTE, -200, NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'EXPLAIN mostra full scan na tabela pedidos (2,3 mi linhas) por ausência de índice em (cliente_id, data_emissao).'),
(4, 4, TIMESTAMPADD(MINUTE, -60,  NOW()), 'SOLUCAO',       NULL, NULL, 'EM_ATENDIMENTO', 'SOLUCIONADO', 'Solução registrada: criado índice idx_pedidos_cliente_data; consulta caiu de 38s para 0,4s. Aguardando validação do solicitante.'),

-- #5
(5, 13, TIMESTAMPADD(MINUTE, -30, NOW()), 'ABERTURA',      NULL, 1, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(5, NULL, TIMESTAMPADD(MINUTE, -30, NOW()), 'CLASSIFICACAO', NULL, 1, NULL, NULL, 'Classificação automática: tipo Bug, categoria Bug, impacto Médio, urgência Alta => prioridade Alta (SLA 8h). Encaminhado para N1.'),

-- #6
(6, 15, TIMESTAMPADD(MINUTE, -7200, NOW()), 'ABERTURA',      NULL, 2, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(6, NULL, TIMESTAMPADD(MINUTE, -7200, NOW()), 'CLASSIFICACAO', NULL, 2, NULL, NULL, 'Classificação automática: tipo Bug, categoria Bug, impacto Crítico, urgência Crítica => prioridade Crítica (SLA 2h). Encaminhado para N2: 8 pontos (4 impacto + 4 urgência) => N2.'),
(6, 4, TIMESTAMPADD(MINUTE, -7180, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Carla Mendes (N2).'),
(6, 4, TIMESTAMPADD(MINUTE, -7165, NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'Infraestrutura normal (CPU, memória, banco). Erro começou exatamente após o deploy da v3.4.0; logs mostram frete = 0 no serviço de cálculo.'),
(6, 4, TIMESTAMPADD(MINUTE, -7160, NOW()), 'ESCALONAMENTO', 2, 3, 'EM_ATENDIMENTO', 'ABERTO', 'Escalonado de N2 para N3. Justificativa: defeito de código introduzido na release v3.4.0 – necessária correção pelo desenvolvimento.'),
(6, 5, TIMESTAMPADD(MINUTE, -7155, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Daniel Rocha (N3).'),
(6, 5, TIMESTAMPADD(MINUTE, -7090, NOW()), 'SOLUCAO',       NULL, NULL, 'EM_ATENDIMENTO', 'SOLUCIONADO', 'Solução registrada: corrigida divisão inteira na conversão de peso; teste de regressão criado | Versão/Release: Hotfix v3.4.1 - commit 8f2c1e9 (deploy em produção). Aguardando validação do solicitante.'),
(6, 15, TIMESTAMPADD(MINUTE, -7000, NOW()), 'VALIDACAO_APROVADA', NULL, NULL, NULL, NULL, 'Solicitante confirmou a solução. Comentário: Frete voltou a ser calculado corretamente.'),
(6, 15, TIMESTAMPADD(MINUTE, -7000, NOW()), 'ENCERRAMENTO',  NULL, NULL, 'SOLUCIONADO', 'FECHADO', 'Chamado encerrado após validação do solicitante.'),

-- #7
(7, 10, TIMESTAMPADD(MINUTE, -2880, NOW()), 'ABERTURA',      NULL, 1, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(7, NULL, TIMESTAMPADD(MINUTE, -2880, NOW()), 'CLASSIFICACAO', NULL, 1, NULL, NULL, 'Classificação automática: tipo Dúvida, categoria Aplicação web, impacto Baixo, urgência Baixa => prioridade Baixa (SLA 48h). Encaminhado para N1.'),
(7, 3, TIMESTAMPADD(MINUTE, -2870, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Bruno Costa (N1).'),
(7, 3, TIMESTAMPADD(MINUTE, -2850, NOW()), 'SOLUCAO',       NULL, NULL, 'EM_ATENDIMENTO', 'SOLUCIONADO', 'Solução registrada: orientado a pesquisar antes de exportar | Artigo KB #3. Aguardando validação do solicitante.'),
(7, 10, TIMESTAMPADD(MINUTE, -2700, NOW()), 'VALIDACAO_APROVADA', NULL, NULL, NULL, NULL, 'Solicitante confirmou a solução.'),
(7, 10, TIMESTAMPADD(MINUTE, -2700, NOW()), 'ENCERRAMENTO',  NULL, NULL, 'SOLUCIONADO', 'FECHADO', 'Chamado encerrado após validação do solicitante.'),

-- #8
(8, 12, TIMESTAMPADD(MINUTE, -1440, NOW()), 'ABERTURA',      NULL, 1, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(8, NULL, TIMESTAMPADD(MINUTE, -1440, NOW()), 'CLASSIFICACAO', NULL, 1, NULL, NULL, 'Classificação automática: tipo Incidente, categoria Infraestrutura de aplicação, impacto Médio, urgência Média => prioridade Média (SLA 24h). Encaminhado para N1.'),
(8, 2, TIMESTAMPADD(MINUTE, -1430, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Ana Souza (N1).'),
(8, 2, TIMESTAMPADD(MINUTE, -1420, NOW()), 'SUPORTE_REMOTO', NULL, NULL, NULL, NULL, 'Técnico solicitou acesso remoto via AnyDesk. Aguardando autorização do solicitante.'),
(8, 12, TIMESTAMPADD(MINUTE, -1415, NOW()), 'SUPORTE_REMOTO', NULL, NULL, NULL, NULL, 'Solicitante AUTORIZOU a sessão de suporte remoto (AnyDesk).'),
(8, 2, TIMESTAMPADD(MINUTE, -1390, NOW()), 'SUPORTE_REMOTO', NULL, NULL, NULL, NULL, 'Diagnóstico da sessão remota: serviço PrintAgent parado após update do Windows e porta 9100 bloqueada no firewall.'),
(8, 2, TIMESTAMPADD(MINUTE, -1380, NOW()), 'SOLUCAO',       NULL, NULL, 'EM_ATENDIMENTO', 'SOLUCIONADO', 'Solução registrada: PrintAgent reiniciado/automático e porta 9100 liberada | Artigo KB #6. Aguardando validação do solicitante.'),
(8, 12, TIMESTAMPADD(MINUTE, -1300, NOW()), 'VALIDACAO_APROVADA', NULL, NULL, NULL, NULL, 'Solicitante confirmou a solução. Comentário: Etiquetas imprimindo normalmente.'),
(8, 12, TIMESTAMPADD(MINUTE, -1300, NOW()), 'ENCERRAMENTO',  NULL, NULL, 'SOLUCIONADO', 'FECHADO', 'Chamado encerrado após validação do solicitante.'),

-- #9  (rastreabilidade completa N1 -> N2 -> N3, inclusive uma reprovação na validação)
(9, 14, TIMESTAMPADD(MINUTE, -5760, NOW()), 'ABERTURA',      NULL, 1, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(9, NULL, TIMESTAMPADD(MINUTE, -5760, NOW()), 'CLASSIFICACAO', NULL, 1, NULL, NULL, 'Classificação automática: tipo Incidente, categoria Integrações, impacto Alto, urgência Alta => prioridade Alta (SLA 8h). Encaminhado para N1.'),
(9, 3, TIMESTAMPADD(MINUTE, -5750, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Bruno Costa (N1).'),
(9, 3, TIMESTAMPADD(MINUTE, -5730, NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'Aplicado artigo KB #5 (token/credenciais): credenciais válidas, problema persiste.'),
(9, 3, TIMESTAMPADD(MINUTE, -5720, NOW()), 'ESCALONAMENTO', 1, 2, 'EM_ATENDIMENTO', 'ABERTO', 'Escalonado de N1 para N2. Justificativa: procedimentos do N1 esgotados; necessária análise dos logs do servidor de integração.'),
(9, 4, TIMESTAMPADD(MINUTE, -5700, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Carla Mendes (N2).'),
(9, 4, TIMESTAMPADD(MINUTE, -5640, NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'Fila de mensagens e conectividade OK. Logs mostram erro de parsing do XML: o ERP alterou o layout (novo campo cMunFG).'),
(9, 4, TIMESTAMPADD(MINUTE, -5630, NOW()), 'ESCALONAMENTO', 2, 3, 'EM_ATENDIMENTO', 'ABERTO', 'Escalonado de N2 para N3. Justificativa: necessária alteração de código no conector de integração.'),
(9, 6, TIMESTAMPADD(MINUTE, -5600, NOW()), 'ATRIBUICAO',    NULL, NULL, 'ABERTO', 'EM_ATENDIMENTO', 'Chamado assumido por Eduardo Alves (N3).'),
(9, 6, TIMESTAMPADD(MINUTE, -5480, NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'Parser ajustado para o novo layout; testes de regressão OK em homologação.'),
(9, 6, TIMESTAMPADD(MINUTE, -5420, NOW()), 'SOLUCAO',       NULL, NULL, 'EM_ATENDIMENTO', 'SOLUCIONADO', 'Solução registrada: conector ajustado ao novo layout | Versão/Release: Release v2.8.3 - commit 4b7d2a1. Aguardando validação do solicitante.'),
(9, 14, TIMESTAMPADD(MINUTE, -5380, NOW()), 'VALIDACAO_REJEITADA', NULL, NULL, 'SOLUCIONADO', 'EM_ATENDIMENTO', 'Solicitante reprovou a solução. Comentário: notas novas sincronizam, mas as pendentes de ontem não apareceram no ERP.'),
(9, 6, TIMESTAMPADD(MINUTE, -5330, NOW()), 'COMENTARIO',    NULL, NULL, NULL, NULL, 'Executado reprocessamento das 312 notas pendentes.'),
(9, 6, TIMESTAMPADD(MINUTE, -5300, NOW()), 'SOLUCAO',       NULL, NULL, 'EM_ATENDIMENTO', 'SOLUCIONADO', 'Solução registrada: conector ajustado e 312 notas reprocessadas | Versão/Release: Release v2.8.3 - commit 4b7d2a1. Aguardando validação do solicitante.'),
(9, 14, TIMESTAMPADD(MINUTE, -5100, NOW()), 'VALIDACAO_APROVADA', NULL, NULL, NULL, NULL, 'Solicitante confirmou a solução. Comentário: todas as notas estão no ERP.'),
(9, 14, TIMESTAMPADD(MINUTE, -5100, NOW()), 'ENCERRAMENTO',  NULL, NULL, 'SOLUCIONADO', 'FECHADO', 'Chamado encerrado após validação do solicitante.'),

-- #10
(10, 9, TIMESTAMPADD(MINUTE, -60, NOW()), 'ABERTURA',      NULL, 3, NULL, 'ABERTO', 'Chamado aberto pelo solicitante.'),
(10, NULL, TIMESTAMPADD(MINUTE, -60, NOW()), 'CLASSIFICACAO', NULL, 3, NULL, NULL, 'Classificação automática: tipo Solicitação de Melhoria, categoria Solicitação de melhoria, impacto Baixo, urgência Média => prioridade Baixa (SLA 48h). Encaminhado para N3.');
