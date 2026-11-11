
package com.sistemaloja.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    public void registrarVenda(int clienteId, int produtoId, int quantidade)
            throws SQLException {

        if (quantidade <= 0) {
            throw new SQLException(
                "A quantidade deve ser maior que zero."
            );
        }

        String sqlCliente =
            "SELECT id FROM clientes WHERE id = ? AND ativo = TRUE";

        String sqlProduto =
            "SELECT preco, estoque FROM produtos "
            + "WHERE id = ? AND ativo = TRUE";

        String sqlVenda =
            "INSERT INTO vendas "
            + "(cliente_id, produto_id, quantidade, total) "
            + "VALUES (?, ?, ?, ?)";

        String sqlEstoque =
            "UPDATE produtos SET estoque = estoque - ? "
            + "WHERE id = ? AND ativo = TRUE AND estoque >= ?";

        try (Connection con = Conexao.conectar()) {

            con.setAutoCommit(false);

            try {
                
                try (PreparedStatement ps =
                         con.prepareStatement(sqlCliente)) {

                    ps.setInt(1, clienteId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                "Cliente não encontrado ou inativo."
                            );
                        }
                    }
                }

                
                double preco;
                int estoque;

                try (PreparedStatement ps =
                         con.prepareStatement(sqlProduto)) {

                    ps.setInt(1, produtoId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                "Produto não encontrado ou inativo."
                            );
                        }

                        preco = rs.getDouble("preco");
                        estoque = rs.getInt("estoque");
                    }
                }

                
                if (estoque < quantidade) {
                    throw new SQLException(
                        "Estoque insuficiente. Disponível: " + estoque
                    );
                }

                
                double total = preco * quantidade;

                
                try (PreparedStatement ps =
                         con.prepareStatement(sqlVenda)) {

                    ps.setInt(1, clienteId);
                    ps.setInt(2, produtoId);
                    ps.setInt(3, quantidade);
                    ps.setDouble(4, total);

                    ps.executeUpdate();
                }

                
                try (PreparedStatement ps =
                         con.prepareStatement(sqlEstoque)) {

                    ps.setInt(1, quantidade);
                    ps.setInt(2, produtoId);
                    ps.setInt(3, quantidade);

                    int linhas = ps.executeUpdate();

                    if (linhas == 0) {
                        throw new SQLException(
                            "Não foi possível atualizar o estoque."
                        );
                    }
                }

                
                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;

            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<Object[]> listarTodas() throws SQLException {

        List<Object[]> vendas = new ArrayList<>();

        String sql = """
            SELECT v.id,
                   c.nome AS cliente,
                   p.nome AS produto,
                   v.quantidade,
                   v.total,
                   v.data_venda
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
                    rs.getTimestamp("data_venda")
                });
            }
        }

        return vendas;
    }
}
