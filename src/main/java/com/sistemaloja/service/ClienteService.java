package com.sistemaloja.service;

import com.sistemaloja.dao.ClienteDAO;
import com.sistemaloja.model.Cliente;
import com.sistemaloja.validator.ClienteValidator;
import java.sql.SQLException;
import java.util.List;

public class ClienteService {
    private final ClienteDAO dao = new ClienteDAO();

    public int cadastrar(Cliente c) throws SQLException, RegraNegocioException {
        validar(c);
        if (dao.existeEmail(c.getEmail(), 0))
            throw new RegraNegocioException("Já existe um cliente com este e-mail.");
        if (dao.existeCpf(c.getCpf(), 0))
            throw new RegraNegocioException("Já existe um cliente com este CPF.");
        return dao.salvar(c);
    }

    public void atualizar(Cliente c) throws SQLException, RegraNegocioException {
        validar(c);
        if (dao.existeEmail(c.getEmail(), c.getId()))
            throw new RegraNegocioException("Já existe outro cliente com este e-mail.");
        if (dao.existeCpf(c.getCpf(), c.getId()))
            throw new RegraNegocioException("Já existe outro cliente com este CPF.");
        dao.atualizar(c);
    }

    public void inativar(int id) throws SQLException, RegraNegocioException {
        dao.inativar(id);
    }

    public List<Cliente> listarAtivos()         throws SQLException { return dao.listarAtivos(); }
    public Cliente buscarPorId(int id)          throws SQLException { return dao.buscarPorId(id); }

    public List<Cliente> buscar(String termo) throws SQLException {
        if (termo == null || termo.trim().isEmpty()) return dao.listarAtivos();

        List<Cliente> porNome = dao.buscarPorNome(termo);
        if (!porNome.isEmpty()) return porNome;
        List<Cliente> porEmail = dao.buscarPorEmail(termo);
        if (!porEmail.isEmpty()) return porEmail;
        List<Cliente> porCpf = dao.buscarPorCpf(termo);
        if (!porCpf.isEmpty()) return porCpf;
        return dao.buscarPorTelefone(termo);
    }

    private void validar(Cliente c) throws RegraNegocioException {
        List<String> erros = ClienteValidator.validar(
                c.getNome(), c.getCpf(), c.getEmail(),
                c.getTelefone(), c.getEndereco(), c.getObservacoes());
        if (!erros.isEmpty()) {
            throw new RegraNegocioException(String.join("\n", erros));
        }
    }

    public static class RegraNegocioException extends Exception {
        public RegraNegocioException(String msg) { super(msg); }
    }
}
