-- =====================================================================
-- Sistema de Gestão de Chamados - EQUIPE 3
-- DDL - MySQL 8+
-- Executado automaticamente pelo Spring Boot (spring.sql.init.mode=always)
-- ou manualmente:  mysql -u root -p chamados_db < schema.sql
-- =====================================================================

DROP TABLE IF EXISTS historico_chamado;
DROP TABLE IF EXISTS chamado;
DROP TABLE IF EXISTS artigo_conhecimento;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS sla;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS grupo;
DROP TABLE IF EXISTS perfil;

-- Perfis de acesso (roles)
CREATE TABLE perfil (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(40)  NOT NULL,
    descricao   VARCHAR(200),
    PRIMARY KEY (id),
    CONSTRAINT uk_perfil_nome UNIQUE (nome)
);

-- Grupos / níveis de suporte
CREATE TABLE grupo (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    sigla       VARCHAR(5)   NOT NULL,
    nome        VARCHAR(80)  NOT NULL,
    descricao   VARCHAR(255),
    nivel       INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_grupo_sigla UNIQUE (sigla)
);

CREATE TABLE categoria (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(80)  NOT NULL,
    descricao   VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_categoria_nome UNIQUE (nome)
);

-- Prazo de solução por prioridade
CREATE TABLE sla (
    prioridade     VARCHAR(20) NOT NULL,
    horas_solucao  INT         NOT NULL,
    PRIMARY KEY (prioridade)
);

CREATE TABLE usuario (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(120) NOT NULL,
    senha       VARCHAR(100) NOT NULL,
    empresa     VARCHAR(120),
    perfil_id   BIGINT       NOT NULL,
    grupo_id    BIGINT,
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT fk_usuario_perfil FOREIGN KEY (perfil_id) REFERENCES perfil (id),
    CONSTRAINT fk_usuario_grupo  FOREIGN KEY (grupo_id)  REFERENCES grupo (id)
);

CREATE TABLE artigo_conhecimento (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    titulo           VARCHAR(150) NOT NULL,
    problema         TEXT         NOT NULL,
    solucao          TEXT         NOT NULL,
    palavras_chave   VARCHAR(255),
    categoria_id     BIGINT,
    autor_id         BIGINT,
    data_criacao     DATETIME     NOT NULL,
    data_atualizacao DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_artigo_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT fk_artigo_autor     FOREIGN KEY (autor_id)     REFERENCES usuario (id)
);

CREATE TABLE chamado (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    titulo                VARCHAR(150) NOT NULL,
    descricao             TEXT         NOT NULL,
    -- classificação
    tipo                  VARCHAR(30)  NOT NULL,   -- INCIDENTE | BUG | SOLICITACAO_DE_MELHORIA | SOLICITACAO_DE_SERVICO | DUVIDA
    categoria_id          BIGINT       NOT NULL,
    impacto               VARCHAR(10)  NOT NULL,   -- BAIXO | MEDIO | ALTO | CRITICO
    urgencia              VARCHAR(10)  NOT NULL,   -- BAIXA | MEDIA | ALTA | CRITICA
    prioridade            VARCHAR(10)  NOT NULL,   -- BAIXA | MEDIA | ALTA | CRITICA
    -- atendimento
    status                VARCHAR(20)  NOT NULL,   -- ABERTO | EM_ATENDIMENTO | SOLUCIONADO | FECHADO
    solicitante_id        BIGINT       NOT NULL,
    grupo_atual_id        BIGINT       NOT NULL,
    tecnico_id            BIGINT,
    tecnico_designado_id  BIGINT,                  -- técnico escolhido na transferência (só ele aceita)
    data_abertura         DATETIME     NOT NULL,
    prazo_sla             DATETIME     NOT NULL,
    data_solucao          DATETIME,
    sla_vencido_notificado_em DATETIME,            -- quando o alerta de SLA vencido foi enviado a toda a equipe
    data_fechamento       DATETIME,
    -- solução
    solucao               TEXT,
    referencia_versao     VARCHAR(150),            -- release / versão / commit / hotfix (N3)
    artigo_id             BIGINT,                  -- artigo da base de conhecimento utilizado
    -- suporte remoto (simulação)
    suporte_remoto        BOOLEAN      NOT NULL DEFAULT FALSE,
    remoto_autorizado     BOOLEAN      NOT NULL DEFAULT FALSE,
    remoto_autorizado_em  DATETIME,
    remoto_ferramenta     VARCHAR(60),
    remoto_diagnostico    TEXT,
    -- controle de concorrência: impede que dois técnicos assumam o mesmo chamado ao mesmo tempo
    versao                BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_chamado_categoria   FOREIGN KEY (categoria_id)   REFERENCES categoria (id),
    CONSTRAINT fk_chamado_solicitante FOREIGN KEY (solicitante_id) REFERENCES usuario (id),
    CONSTRAINT fk_chamado_grupo       FOREIGN KEY (grupo_atual_id) REFERENCES grupo (id),
    CONSTRAINT fk_chamado_tecnico     FOREIGN KEY (tecnico_id)     REFERENCES usuario (id),
    CONSTRAINT fk_chamado_designado   FOREIGN KEY (tecnico_designado_id) REFERENCES usuario (id),
    CONSTRAINT fk_chamado_artigo      FOREIGN KEY (artigo_id)      REFERENCES artigo_conhecimento (id)
);

CREATE INDEX idx_chamado_status ON chamado (status);
CREATE INDEX idx_chamado_grupo_status ON chamado (grupo_atual_id, status);

-- Histórico / tramitações (somente inserção - nunca é alterado ou excluído pela aplicação)
CREATE TABLE historico_chamado (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    chamado_id        BIGINT      NOT NULL,
    usuario_id        BIGINT,                      -- NULL = ação automática do sistema
    data_hora         DATETIME    NOT NULL,
    acao              VARCHAR(30) NOT NULL,
    grupo_origem_id   BIGINT,
    grupo_destino_id  BIGINT,
    status_anterior   VARCHAR(20),
    status_novo       VARCHAR(20),
    descricao         TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_hist_chamado FOREIGN KEY (chamado_id)       REFERENCES chamado (id),
    CONSTRAINT fk_hist_usuario FOREIGN KEY (usuario_id)       REFERENCES usuario (id),
    CONSTRAINT fk_hist_origem  FOREIGN KEY (grupo_origem_id)  REFERENCES grupo (id),
    CONSTRAINT fk_hist_destino FOREIGN KEY (grupo_destino_id) REFERENCES grupo (id)
);

CREATE INDEX idx_hist_chamado ON historico_chamado (chamado_id, data_hora);
