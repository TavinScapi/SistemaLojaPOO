package com.sistemaloja.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VendaDAO {
    public void registrarVenda(int clienteId, Map<Integer, Integer> itens) throws SQLException {
        if (itens.isEmpty()) {
            throw new SQLException("A venda não tem itens.");
        }

        String sqlCliente = "SELECT id FROM clientes WHERE id = ? AND ativo = TRUE";
        String sqlProduto = "SELECT nome, preco, estoque FROM produtos WHERE id = ? AND ativo = TRUE";
        String sqlVenda = "INSERT INTO vendas (cliente_id, produto_id, quantidade, total) VALUES (?, ?, ?, ?)";
        String sqlEstoque = "UPDATE produtos SET estoque = estoque - ? WHERE id = ? AND estoque >= ?";

        try (Connection con = Conexao.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlCliente)) {
                    ps.setInt(1, clienteId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Cliente não encontrado ou inativo.");
                    }
                }

                for (Map.Entry<Integer, Integer> item : itens.entrySet()) {
                    int produtoId = item.getKey();
                    int quantidade = item.getValue();
                    if (quantidade <= 0) throw new SQLException("A quantidade deve ser maior que zero.");

                    String nome;
                    double preco;
                    try (PreparedStatement ps = con.prepareStatement(sqlProduto)) {
                        ps.setInt(1, produtoId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Produto não encontrado ou inativo.");
                            nome = rs.getString("nome");
                            preco = rs.getDouble("preco");
                        }
                    }

                    try (PreparedStatement ps = con.prepareStatement(sqlEstoque)) {
                        ps.setInt(1, quantidade);
                        ps.setInt(2, produtoId);
                        ps.setInt(3, quantidade);
                        if (ps.executeUpdate() == 0) {
                            throw new SQLException("Estoque insuficiente para " + nome + ".");
                        }
                    }

                    try (PreparedStatement ps = con.prepareStatement(sqlVenda)) {
                        ps.setInt(1, clienteId);
                        ps.setInt(2, produtoId);
                        ps.setInt(3, quantidade);
                        ps.setDouble(4, preco * quantidade);
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Object[]> listarTodas() throws SQLException {
        List<Object[]> vendas = new ArrayList<>();
        String sql = """
            SELECT v.id, c.nome AS cliente, p.nome AS produto,
                   v.quantidade, v.total, v.data_venda
            FROM vendas v
            INNER JOIN clientes c ON c.id = v.cliente_id
            INNER JOIN produtos p ON p.id = v.produto_id
            ORDER BY v.id DESC
            """;
        try (Connection con = Conexao.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vendas.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("cliente"),
                    rs.getString("produto"),
                    rs.getInt("quantidade"),
                    rs.getDouble("total"),
                    rs.getTimestamp("data_venda").toLocalDateTime()
                });
            }
        }
        return vendas;
    }

    public double[] resumo() throws SQLException {
        String sql = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM vendas";
        try (Connection con = Conexao.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new double[]{rs.getInt(1), rs.getDouble(2)};
        }
    }
}
