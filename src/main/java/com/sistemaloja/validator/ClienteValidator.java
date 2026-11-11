package com.sistemaloja.validator;

import java.util.ArrayList;
import java.util.List;

public final class ClienteValidator {
    private ClienteValidator() {}

    public static List<String> validar(String nome, String cpf, String email,
                                       String telefone, String endereco,
                                       String observacoes) {
        List<String> erros = new ArrayList<>();

        if (nome == null || nome.trim().isEmpty()) {
            erros.add("Nome é obrigatório.");
        } else if (nome.trim().length() < 2 || nome.trim().length() > 120) {
            erros.add("Nome deve ter entre 2 e 120 caracteres.");
        }

        if (cpf != null && !cpf.isBlank() && !cpfValido(cpf)) {
            erros.add("CPF deve ter 11 números.");
        }

        if (email == null || email.trim().isEmpty()) {
            erros.add("E-mail é obrigatório.");
        } else if (email.length() > 150) {
            erros.add("E-mail não pode exceder 150 caracteres.");
        } else if (!emailValido(email)) {
            erros.add("Formato de e-mail inválido.");
        }

        String telLimpo = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (telLimpo.isEmpty()) {
            erros.add("Telefone é obrigatório.");
        } else if (!telefoneValido(telLimpo)) {
            erros.add("Telefone inválido (deve ter DDD + 8 ou 9 dígitos).");
        }

        if (endereco != null && endereco.length() > 255) {
            erros.add("Endereço não pode exceder 255 caracteres.");
        }
        if (observacoes != null && observacoes.length() > 1000) {
            erros.add("Observações não podem exceder 1000 caracteres.");
        }

        return erros;
    }

    public static boolean emailValido(String email) {
        if (email == null) return false;
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(regex);
    }

    public static boolean telefoneValido(String tel) {
        String d = tel.replaceAll("\\D", "");
        if (d.length() < 10 || d.length() > 11) return false;
        int ddd = Integer.parseInt(d.substring(0, 2));
        return ddd >= 11 && ddd <= 99;
    }

    public static boolean cpfValido(String cpf) {
        return cpf.replaceAll("\\D", "").length() == 11;
    }
}
