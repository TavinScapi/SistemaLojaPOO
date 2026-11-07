package com.sistemaloja.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ClienteValidator {

    private static final Logger logger = Logger.getLogger(ClienteValidator.class.getName());

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

        
        String cpfLimpo = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (!cpfLimpo.isEmpty() && !cpfValido(cpfLimpo)) {
            logger.warning("CPF rejeitado: '" + cpf + "' (limpo: '" + cpfLimpo + "')");
            erros.add("CPF inválido.");
        }

        
        if (email == null || email.trim().isEmpty()) {
            erros.add("E-mail é obrigatório.");
        } else if (email.length() > 150) {
            erros.add("E-mail não pode exceder 150 caracteres.");
        } else if (!emailValido(email)) {
            logger.warning("E-mail rejeitado: '" + email + "'");
            erros.add("Formato de e-mail inválido.");
        }

        
        String telLimpo = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (telLimpo.isEmpty()) {
            erros.add("Telefone é obrigatório.");
        } else if (!telefoneValido(telLimpo)) {
            logger.warning("Telefone rejeitado: '" + telefone + "' (limpo: '" + telLimpo + "')");
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
        String d = cpf.replaceAll("\\D", "");
        if (d.length() != 11) return false;
        
        
        if (d.chars().distinct().count() == 1) return false;

        
        for (int j = 9; j < 11; j++) {
            int soma = 0;
            for (int i = 0; i < j; i++) {
                soma += Character.getNumericValue(d.charAt(i)) * (j + 1 - i);
            }
            int dig = 11 - (soma % 11);
            if (dig >= 10) dig = 0;
            if (dig != Character.getNumericValue(d.charAt(j))) return false;
        }
        return true;
    }
}