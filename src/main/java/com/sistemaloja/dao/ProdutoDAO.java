package com.sistemaloja.dao;

import com.sistemaloja.model.Produto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {
    public void salvar(Produto produto) throws SQLException {
        String sql = "INSERT INTO produtos (nome, preco, estoque) VALUES (?, ?, ?)";
        try (Connection con = Conexao.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setDouble(2, produto.getPreco());
            stmt.setInt(3, produto.getEstoque());
            stmt.executeUpdate();
        }
    }

    public void atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produtos SET nome=?, preco=?, estoque=? WHERE id=? AND ativo=TRUE";
        try (Connection con = Conexao.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setDouble(2, produto.getPreco());
            stmt.setInt(3, produto.getEstoque());
            stmt.setInt(4, produto.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "UPDATE produtos SET ativo=FALSE WHERE id=?";
        try (Connection con = Conexao.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Produto> listarAtivos() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT id, nome, preco, estoque FROM produtos WHERE ativo = TRUE ORDER BY nome";
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                produtos.add(new Produto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getInt("estoque")));
            }
        }
        return produtos;
    }
}
