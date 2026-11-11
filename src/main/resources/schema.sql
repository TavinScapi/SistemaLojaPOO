-- Tabelas do Sistema Loja (SQLite).
-- Executado AUTOMATICAMENTE pelo sistema ao conectar (ver Conexao.java).
-- Usa IF NOT EXISTS, então pode rodar quantas vezes for.

CREATE TABLE IF NOT EXISTS clientes (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    nome             VARCHAR(120) NOT NULL,
    cpf              VARCHAR(14)  NULL UNIQUE,
    email            VARCHAR(150) NOT NULL UNIQUE,
    telefone         VARCHAR(20)  NOT NULL,
    endereco         VARCHAR(255) NULL,
    observacoes      TEXT         NULL,
    ativo            BOOLEAN      NOT NULL DEFAULT 1,
    data_cadastro    TIMESTAMP    NOT NULL DEFAULT (datetime('now', 'localtime')),
    data_atualizacao TIMESTAMP    NULL DEFAULT NULL
);

-- Substitui o "ON UPDATE CURRENT_TIMESTAMP" do MySQL
CREATE TRIGGER IF NOT EXISTS trg_clientes_atualizacao
AFTER UPDATE ON clientes
FOR EACH ROW WHEN NEW.data_atualizacao IS OLD.data_atualizacao
BEGIN
    UPDATE clientes SET data_atualizacao = datetime('now', 'localtime')
    WHERE id = NEW.id;
END;

CREATE TABLE IF NOT EXISTS produtos (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    nome    VARCHAR(100)  NOT NULL,
    preco   DECIMAL(10,2) NOT NULL,
    estoque INTEGER       NOT NULL DEFAULT 0,
    ativo   BOOLEAN       NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS vendas (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id INTEGER       NOT NULL REFERENCES clientes(id),
    produto_id INTEGER       NOT NULL REFERENCES produtos(id),
    quantidade INTEGER       NOT NULL,
    total      DECIMAL(10,2) NOT NULL,
    data_venda TIMESTAMP     NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- Produtos de exemplo (só entram se a tabela estiver vazia)
INSERT INTO produtos (nome, preco, estoque)
SELECT 'Notebook Dell Inspiron 15', 3499.00, 10 WHERE NOT EXISTS (SELECT 1 FROM produtos)
UNION ALL SELECT 'Monitor LG 24"',              799.90, 20 WHERE NOT EXISTS (SELECT 1 FROM produtos)
UNION ALL SELECT 'Teclado Mecânico Redragon',   249.90, 30 WHERE NOT EXISTS (SELECT 1 FROM produtos)
UNION ALL SELECT 'Mouse Logitech MX Master 3S', 599.90, 15 WHERE NOT EXISTS (SELECT 1 FROM produtos)
UNION ALL SELECT 'SSD Kingston 1TB',            429.90, 40 WHERE NOT EXISTS (SELECT 1 FROM produtos);
