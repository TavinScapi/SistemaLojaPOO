package com.sistemaloja.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.sqlite.SQLiteConfig;

/**
 * Conexão com o banco SQLite.
 *
 * Não precisa instalar nada: o banco é o arquivo "loja.db", criado
 * automaticamente na pasta do projeto na primeira execução, já com as
 * tabelas e alguns produtos de exemplo (schema.sql em src/main/resources).
 *
 * Para usar outro arquivo, defina a variável de ambiente DB_ARQUIVO.
 */
public class Conexao {

    private static final String ARQUIVO_PADRAO = "loja.db";
    private static volatile boolean schemaCriado = false;

    private static String arquivo() {
        String v = System.getenv("DB_ARQUIVO");
        return (v == null || v.isBlank()) ? ARQUIVO_PADRAO : v.trim();
    }

    public static Connection conectar() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);              // SQLite vem com FKs desligadas
        config.setBusyTimeout(5000);                  // espera em vez de dar "database is locked"
        config.setDateStringFormat("yyyy-MM-dd HH:mm:ss"); // formato das datas gravadas no schema

        Connection con = config.createConnection("jdbc:sqlite:" + arquivo());
        if (!schemaCriado) {
            criarTabelas(con);
        }
        return con;
    }

    /** Executa o schema.sql (CREATE ... IF NOT EXISTS) uma vez por execução. */
    private static synchronized void criarTabelas(Connection con) throws SQLException {
        if (schemaCriado) return;
        String script;
        try (InputStream in = Conexao.class.getResourceAsStream("/schema.sql")) {
            if (in == null) throw new SQLException("schema.sql não encontrado no classpath");
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("Erro ao ler schema.sql", e);
        }
        try (Statement st = con.createStatement()) {
            st.executeUpdate(script); // o driver do SQLite executa o script inteiro
        }
        schemaCriado = true;
    }
}
