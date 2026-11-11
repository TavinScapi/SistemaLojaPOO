package com.sistemaloja.dao;

import com.sistemaloja.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    public int salvar(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes " +
                "(nome, cpf, email, telefone, endereco, observacoes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getNome());
            stmt.setString(2, vazioParaNull(c.getCpf()));
            stmt.setString(3, c.getEmail());
            stmt.setString(4, c.getTelefone());
            stmt.setString(5, vazioParaNull(c.getEndereco()));
            stmt.setString(6, vazioParaNull(c.getObservacoes()));

            int linhas = stmt.executeUpdate();
            if (linhas == 0) throw new SQLException("Nenhum cliente foi inserido.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        }
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET nome=?, cpf=?, email=?, telefone=?, " +
                "endereco=?, observacoes=? WHERE id=? AND ativo=TRUE";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getNome());
            stmt.setString(2, vazioParaNull(c.getCpf()));
            stmt.setString(3, c.getEmail());
            stmt.setString(4, c.getTelefone());
            stmt.setString(5, vazioParaNull(c.getEndereco()));
            stmt.setString(6, vazioParaNull(c.getObservacoes()));
            stmt.setInt(7, c.getId());

            int linhas = stmt.executeUpdate();
            if (linhas == 0)
                throw new SQLException("Cliente não encontrado ou já inativo (id=" + c.getId() + ").");
        }
    }

    public void inativar(int id) throws SQLException {
        String sql = "UPDATE clientes SET ativo=FALSE WHERE id=? AND ativo=TRUE";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new SQLException("Cliente não encontrado ou já inativo (id=" + id + ").");
        }
    }

    public List<Cliente> listarAtivos() throws SQLException {
        return listar("SELECT * FROM clientes WHERE ativo=TRUE ORDER BY nome");
    }

    public List<Cliente> buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE ativo=TRUE AND nome LIKE ? ORDER BY nome";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");
            return mapear(stmt.executeQuery());
        }
    }

    public List<Cliente> buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE ativo=TRUE AND email LIKE ? ORDER BY nome";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + email + "%");
            return mapear(stmt.executeQuery());
        }
    }

    public List<Cliente> buscarPorTelefone(String telefone) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE ativo=TRUE AND telefone LIKE ? ORDER BY nome";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + telefone + "%");
            return mapear(stmt.executeQuery());
        }
    }

    public List<Cliente> buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE ativo=TRUE AND cpf LIKE ? ORDER BY nome";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + cpf + "%");
            return mapear(stmt.executeQuery());
        }
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE id=?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearLinha(rs) : null;
            }
        }
    }

    public boolean existeEmail(String email, int idIgnorar) throws SQLException {
        String sql = "SELECT 1 FROM clientes WHERE email=? AND id<>? LIMIT 1";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, idIgnorar);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeCpf(String cpf, int idIgnorar) throws SQLException {
        if (cpf == null || cpf.isBlank()) return false;
        String sql = "SELECT 1 FROM clientes WHERE cpf=? AND id<>? LIMIT 1";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.setInt(2, idIgnorar);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<Cliente> listar(String sql) throws SQLException {
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapear(rs);
        }
    }

    private List<Cliente> mapear(ResultSet rs) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        while (rs.next()) lista.add(mapearLinha(rs));
        return lista;
    }

    private Cliente mapearLinha(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("email"),
                rs.getString("telefone"),
                rs.getString("endereco"),
                rs.getString("observacoes"),
                rs.getBoolean("ativo"),
                rs.getTimestamp("data_cadastro") != null
                        ? rs.getTimestamp("data_cadastro").toLocalDateTime() : null,
                rs.getTimestamp("data_atualizacao") != null
                        ? rs.getTimestamp("data_atualizacao").toLocalDateTime() : null
        );
    }

    private String vazioParaNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
