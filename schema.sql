-- Script de criação das tabelas (rodar uma vez no banco do Aiven: defaultdb)

CREATE TABLE IF NOT EXISTS clientes (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    nome             VARCHAR(120) NOT NULL,
    cpf              VARCHAR(14)  NULL,
    email            VARCHAR(150) NOT NULL,
    telefone         VARCHAR(20)  NOT NULL,
    endereco         VARCHAR(255) NULL,
    observacoes      TEXT         NULL,
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    data_cadastro    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_clientes_email (email),
    UNIQUE KEY uk_clientes_cpf (cpf)
);

CREATE TABLE IF NOT EXISTS produtos (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nome    VARCHAR(100)  NOT NULL,
    preco   DECIMAL(10,2) NOT NULL,
    estoque INT           NOT NULL DEFAULT 0,
    ativo   BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS vendas (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT           NOT NULL,
    produto_id INT           NOT NULL,
    quantidade INT           NOT NULL,
    total      DECIMAL(10,2) NOT NULL,
    data_venda TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vendas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_vendas_produto FOREIGN KEY (produto_id) REFERENCES produtos(id)
);
