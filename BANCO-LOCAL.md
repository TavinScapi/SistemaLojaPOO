# Banco de dados (SQLite)

O sistema usa **SQLite**: não precisa instalar MySQL nem nenhum servidor,
e não depende de internet nem de firewall.

## Como rodar

1. Abra o projeto no NetBeans.
2. Clique em **Run**.

Pronto. Na primeira execução:

- o NetBeans/Maven baixa o driver `sqlite-jdbc` (só nessa vez precisa de internet);
- o arquivo **`loja.db`** é criado na pasta do projeto, já com as tabelas
  e 5 produtos de exemplo (script em `src/main/resources/schema.sql`).

## Dicas

- **Zerar o banco:** feche o sistema e apague o `loja.db`. Ele é recriado
  na próxima execução.
- **Levar os dados para outro PC:** copie o `loja.db` junto com o projeto.
- **Ver os dados:** abra o `loja.db` no [DB Browser for SQLite](https://sqlitebrowser.org/).
- **Usar outro arquivo:** defina a variável de ambiente `DB_ARQUIVO`
  (ex.: `DB_ARQUIVO=C:\dados\loja.db`).
