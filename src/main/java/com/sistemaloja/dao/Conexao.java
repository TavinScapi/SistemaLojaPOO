package com.sistemaloja.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lê as credenciais do banco de:
 *   1) variáveis de ambiente DB_URL, DB_USER, DB_PASSWORD (se existirem), ou
 *   2) arquivo db.properties na raiz do projeto (fora do git).
 */
public class Conexao {

    private static final Properties CONFIG = carregarConfig();

    private static Properties carregarConfig() {
        Properties p = new Properties();
        Path arquivo = Path.of("db.properties");
        if (Files.exists(arquivo)) {
            try (InputStream in = Files.newInputStream(arquivo)) {
                p.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao ler db.properties", e);
            }
        }
        return p;
    }

    private static String valor(String env, String chave) {
        String v = System.getenv(env);
        if (v == null || v.isBlank()) v = CONFIG.getProperty(chave);
        if (v == null || v.isBlank())
            throw new IllegalStateException(
                "Configuração '" + chave + "' não encontrada. "
                + "Crie o arquivo db.properties (veja db.properties.example) "
                + "ou defina a variável de ambiente " + env + ".");
        return v.trim();
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(
                valor("DB_URL", "db.url"),
                valor("DB_USER", "db.user"),
                valor("DB_PASSWORD", "db.password"));
    }
}
