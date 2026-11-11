package com.sistemaloja.view;

import com.sistemaloja.dao.ProdutoDAO;
import com.sistemaloja.model.Produto;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class TelaProdutos extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(TelaProdutos.class.getName());

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    private List<Produto> produtos = List.of();

    private boolean modoEdicao = false;
    private int idProdutoSelecionado = -1;

    public TelaProdutos() {
        initComponents();
        posConstrucao();
    }

    private void posConstrucao() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(950, 560));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmarDescarte()) {
                    voltarParaPrincipal();
                }
            }
        });

        Estilo.aplicarHover(btnVoltar, btnCadastrar, btnEditar, btnExcluir, btnLimpar);

        configurarTabela();

        txtBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabela();
            }
        });

        SwingUtilities.invokeLater(() -> txtNome.requestFocusInWindow());

        entrarModoNovo();
        carregarProdutos();
    }

    private void configurarTabela() {
        modeloTabela = new DefaultTableModel(
                new Object[]{"Código", "Nome", "Preço", "Estoque"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabela.setModel(modeloTabela);
        Estilo.estilizarTabela(tabela);
        Estilo.alinharDireita(tabela, 2, 3);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() != -1) {
                selecionarProduto();
            }
        });
    }

    private void carregarProdutos() {
        try {
            produtos = new ProdutoDAO().listarAtivos();
            filtrarTabela();
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar produtos", e);
        }
    }

    private void filtrarTabela() {
        String filtro = txtBusca.getText().trim().toLowerCase();
        modeloTabela.setRowCount(0);
        for (Produto p : produtos) {
            if (p.getNome().toLowerCase().contains(filtro)) {
                modeloTabela.addRow(new Object[]{
                    p.getId(), p.getNome(), MOEDA.format(p.getPreco()), p.getEstoque()
                });
            }
        }
    }

    private Produto buscarProduto(int id) {
        for (Produto p : produtos) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private void selecionarProduto() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return;
        if (modoEdicao && !confirmarDescarte()) return;

        Produto p = buscarProduto((Integer) tabela.getValueAt(linha, 0));
        if (p == null) return;

        idProdutoSelecionado = p.getId();
        txtCodigo.setText(String.valueOf(p.getId()));
        txtNome.setText(p.getNome());
        txtPreco.setText(String.format(Locale.ROOT, "%.2f", p.getPreco()).replace('.', ','));
        txtEstoque.setText(String.valueOf(p.getEstoque()));
        entrarModoVisualizacao();
    }

    private void entrarModoNovo() {
        modoEdicao = false;
        idProdutoSelecionado = -1;

        txtCodigo.setText("");
        txtNome.setText("");
        txtPreco.setText("");
        txtEstoque.setText("");
        txtCodigo.setEditable(false);
        txtNome.setEditable(true);
        txtPreco.setEditable(true);
        txtEstoque.setEditable(true);

        btnCadastrar.setText("Cadastrar");
        btnCadastrar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnLimpar.setText("Limpar");
        lblTitulo.setText("Cadastro de produtos");
        tabela.clearSelection();
        txtNome.requestFocusInWindow();
    }

    private void entrarModoVisualizacao() {
        modoEdicao = false;
        txtNome.setEditable(false);
        txtPreco.setEditable(false);
        txtEstoque.setEditable(false);

        btnCadastrar.setText("Cadastrar");
        btnCadastrar.setEnabled(false);
        btnEditar.setEnabled(true);
        btnExcluir.setEnabled(true);
        btnLimpar.setText("Limpar seleção");
        lblTitulo.setText("Produto selecionado");
    }

    private void entrarModoEdicao() {
        modoEdicao = true;
        txtNome.setEditable(true);
        txtPreco.setEditable(true);
        txtEstoque.setEditable(true);

        btnCadastrar.setText("Salvar");
        btnCadastrar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnLimpar.setText("Cancelar edição");
        lblTitulo.setText("Editar produto");
        txtNome.requestFocusInWindow();
    }

    private void mostrarSucesso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarErro(String titulo, Exception ex) {
        logger.log(Level.SEVERE, titulo, ex);
        JOptionPane.showMessageDialog(this,
                titulo + ":\n" + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private boolean confirmarDescarte() {
        if (!modoEdicao) return true;
        int r = JOptionPane.showConfirmDialog(this,
                "Há alterações não salvas. Descartar?",
                "Alterações pendentes",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }

    private void voltarParaPrincipal() {
        new TelaPrincipal().setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlCabecalho = new javax.swing.JPanel();
        pnlTitulos = new javax.swing.JPanel();
        lblMarca = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        pnlAcoesCabecalho = new javax.swing.JPanel();
        btnVoltar = new javax.swing.JButton();
        pnlCorpo = new javax.swing.JPanel();
        pnlFormulario = new javax.swing.JPanel();
        lblFormTitulo = new javax.swing.JLabel();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblNome = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        lblPreco = new javax.swing.JLabel();
        txtPreco = new javax.swing.JTextField();
        lblEstoque = new javax.swing.JLabel();
        txtEstoque = new javax.swing.JTextField();
        pnlBotoes = new javax.swing.JPanel();
        btnCadastrar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        pnlTabela = new javax.swing.JPanel();
        pnlTabelaTopo = new javax.swing.JPanel();
        lblTabelaTitulo = new javax.swing.JLabel();
        lblBusca = new javax.swing.JLabel();
        txtBusca = new javax.swing.JTextField();
        scrollTabela = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Produtos - OctoMax");
        getContentPane().setLayout(new java.awt.BorderLayout());

        pnlCabecalho.setBackground(new java.awt.Color(24, 36, 56));
        pnlCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 32, 18, 32));
        pnlCabecalho.setLayout(new java.awt.BorderLayout());
        pnlTitulos.setOpaque(false);
        pnlTitulos.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblMarca.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblMarca.setForeground(new java.awt.Color(173, 181, 189));
        lblMarca.setText("OctoMax");

        pnlTitulos.add(lblMarca);
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setText("Cadastro de produtos");

        pnlTitulos.add(lblTitulo);

        pnlCabecalho.add(pnlTitulos, java.awt.BorderLayout.WEST);
        pnlAcoesCabecalho.setOpaque(false);
        pnlAcoesCabecalho.setLayout(new java.awt.GridBagLayout());
        btnVoltar.setBackground(new java.awt.Color(52, 68, 94));
        btnVoltar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnVoltar.setForeground(new java.awt.Color(255, 255, 255));
        btnVoltar.setText("← Voltar");
        btnVoltar.setBorderPainted(false);
        btnVoltar.setFocusPainted(false);
        btnVoltar.setOpaque(true);
        btnVoltar.setPreferredSize(new java.awt.Dimension(110, 36));
        btnVoltar.addActionListener(this::btnVoltarActionPerformed);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        pnlAcoesCabecalho.add(btnVoltar, gridBagConstraints);

        pnlCabecalho.add(pnlAcoesCabecalho, java.awt.BorderLayout.EAST);

        getContentPane().add(pnlCabecalho, java.awt.BorderLayout.NORTH);
        pnlCorpo.setBackground(new java.awt.Color(244, 246, 249));
        pnlCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 32, 24, 32));
        pnlCorpo.setLayout(new java.awt.BorderLayout(20, 0));
        pnlFormulario.setBackground(new java.awt.Color(255, 255, 255));
        pnlFormulario.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)), javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        pnlFormulario.setPreferredSize(new java.awt.Dimension(340, 560));
        pnlFormulario.setLayout(new java.awt.GridBagLayout());
        lblFormTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15));
        lblFormTitulo.setForeground(new java.awt.Color(33, 37, 41));
        lblFormTitulo.setText("Dados do produto");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        pnlFormulario.add(lblFormTitulo, gridBagConstraints);
        lblCodigo.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblCodigo.setForeground(new java.awt.Color(108, 117, 125));
        lblCodigo.setText("Código");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        pnlFormulario.add(lblCodigo, gridBagConstraints);
        txtCodigo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtCodigo.setEditable(false);
        txtCodigo.setBackground(new java.awt.Color(233, 236, 239));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        pnlFormulario.add(txtCodigo, gridBagConstraints);
        lblNome.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblNome.setForeground(new java.awt.Color(108, 117, 125));
        lblNome.setText("Nome do produto *");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        pnlFormulario.add(lblNome, gridBagConstraints);
        txtNome.setFont(new java.awt.Font("Segoe UI", 0, 14));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        pnlFormulario.add(txtNome, gridBagConstraints);
        lblPreco.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblPreco.setForeground(new java.awt.Color(108, 117, 125));
        lblPreco.setText("Preço (R$) *");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 12);
        pnlFormulario.add(lblPreco, gridBagConstraints);
        txtPreco.setFont(new java.awt.Font("Segoe UI", 0, 14));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 12);
        pnlFormulario.add(txtPreco, gridBagConstraints);
        lblEstoque.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblEstoque.setForeground(new java.awt.Color(108, 117, 125));
        lblEstoque.setText("Estoque *");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        pnlFormulario.add(lblEstoque, gridBagConstraints);
        txtEstoque.setFont(new java.awt.Font("Segoe UI", 0, 14));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        pnlFormulario.add(txtEstoque, gridBagConstraints);
        pnlBotoes.setOpaque(false);
        pnlBotoes.setLayout(new java.awt.GridLayout(2, 2, 8, 8));
        btnCadastrar.setBackground(new java.awt.Color(13, 110, 253));
        btnCadastrar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnCadastrar.setForeground(new java.awt.Color(255, 255, 255));
        btnCadastrar.setText("Cadastrar");
        btnCadastrar.setBorderPainted(false);
        btnCadastrar.setFocusPainted(false);
        btnCadastrar.setOpaque(true);
        btnCadastrar.setPreferredSize(new java.awt.Dimension(120, 36));
        btnCadastrar.addActionListener(this::btnCadastrarActionPerformed);

        pnlBotoes.add(btnCadastrar);
        btnEditar.setBackground(new java.awt.Color(25, 135, 84));
        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnEditar.setForeground(new java.awt.Color(255, 255, 255));
        btnEditar.setText("Editar");
        btnEditar.setBorderPainted(false);
        btnEditar.setFocusPainted(false);
        btnEditar.setOpaque(true);
        btnEditar.setPreferredSize(new java.awt.Dimension(120, 36));
        btnEditar.addActionListener(this::btnEditarActionPerformed);

        pnlBotoes.add(btnEditar);
        btnExcluir.setBackground(new java.awt.Color(220, 53, 69));
        btnExcluir.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnExcluir.setForeground(new java.awt.Color(255, 255, 255));
        btnExcluir.setText("Excluir");
        btnExcluir.setBorderPainted(false);
        btnExcluir.setFocusPainted(false);
        btnExcluir.setOpaque(true);
        btnExcluir.setPreferredSize(new java.awt.Dimension(120, 36));
        btnExcluir.addActionListener(this::btnExcluirActionPerformed);

        pnlBotoes.add(btnExcluir);
        btnLimpar.setBackground(new java.awt.Color(108, 117, 125));
        btnLimpar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnLimpar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpar.setText("Limpar");
        btnLimpar.setBorderPainted(false);
        btnLimpar.setFocusPainted(false);
        btnLimpar.setOpaque(true);
        btnLimpar.setPreferredSize(new java.awt.Dimension(120, 36));
        btnLimpar.addActionListener(this::btnLimparActionPerformed);

        pnlBotoes.add(btnLimpar);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlFormulario.add(pnlBotoes, gridBagConstraints);

        pnlCorpo.add(pnlFormulario, java.awt.BorderLayout.WEST);
        pnlTabela.setBackground(new java.awt.Color(255, 255, 255));
        pnlTabela.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)), javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        pnlTabela.setLayout(new java.awt.BorderLayout(0, 12));
        pnlTabelaTopo.setOpaque(false);
        pnlTabelaTopo.setLayout(new java.awt.BorderLayout(0, 6));
        lblTabelaTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15));
        lblTabelaTitulo.setForeground(new java.awt.Color(33, 37, 41));
        lblTabelaTitulo.setText("Produtos cadastrados");

        pnlTabelaTopo.add(lblTabelaTitulo, java.awt.BorderLayout.NORTH);
        lblBusca.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblBusca.setForeground(new java.awt.Color(108, 117, 125));
        lblBusca.setText("Buscar por nome:");

        pnlTabelaTopo.add(lblBusca, java.awt.BorderLayout.CENTER);
        txtBusca.setFont(new java.awt.Font("Segoe UI", 0, 14));

        pnlTabelaTopo.add(txtBusca, java.awt.BorderLayout.SOUTH);

        pnlTabela.add(pnlTabelaTopo, java.awt.BorderLayout.NORTH);

        scrollTabela.setViewportView(tabela);

        pnlTabela.add(scrollTabela, java.awt.BorderLayout.CENTER);

        pnlCorpo.add(pnlTabela, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCadastrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCadastrarActionPerformed
        String nome = txtNome.getText().trim();
        String precoTexto = txtPreco.getText().trim().replace(",", ".");
        String estoqueTexto = txtEstoque.getText().trim();

        StringBuilder erros = new StringBuilder();
        if (nome.isEmpty()) erros.append("• Nome é obrigatório.\n");

        double preco = 0;
        if (precoTexto.isEmpty()) {
            erros.append("• Preço é obrigatório.\n");
        } else {
            try {
                preco = Double.parseDouble(precoTexto);
                if (!Double.isFinite(preco) || preco < 0)
                    erros.append("• Preço inválido (não pode ser negativo).\n");
            } catch (NumberFormatException ex) {
                erros.append("• Preço inválido.\n");
            }
        }

        int estoque = 0;
        if (estoqueTexto.isEmpty()) {
            erros.append("• Estoque é obrigatório.\n");
        } else {
            try {
                estoque = Integer.parseInt(estoqueTexto);
                if (estoque < 0)
                    erros.append("• Estoque não pode ser negativo.\n");
            } catch (NumberFormatException ex) {
                erros.append("• Estoque deve ser um número inteiro.\n");
            }
        }

        if (erros.length() > 0) {
            JOptionPane.showMessageDialog(this, erros.toString(),
                    "Campos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ProdutoDAO dao = new ProdutoDAO();
            if (modoEdicao) {
                dao.atualizar(new Produto(idProdutoSelecionado, nome, preco, estoque));
                mostrarSucesso("Produto atualizado com sucesso!");
            } else {
                dao.salvar(new Produto(nome, preco, estoque));
                mostrarSucesso("Produto cadastrado com sucesso!");
            }
            entrarModoNovo();
            carregarProdutos();
        } catch (SQLException ex) {
            mostrarErro("Erro ao salvar produto", ex);
        }
    }//GEN-LAST:event_btnCadastrarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        if (idProdutoSelecionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um produto na tabela primeiro.",
                    "Nenhum produto selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        entrarModoEdicao();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        if (idProdutoSelecionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um produto para excluir.",
                    "Nenhum produto selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir este produto?\nO histórico de vendas será preservado.",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;

        try {
            new ProdutoDAO().excluir(idProdutoSelecionado);
            mostrarSucesso("Produto excluído com sucesso!");
            entrarModoNovo();
            carregarProdutos();
        } catch (SQLException ex) {
            mostrarErro("Erro ao excluir produto", ex);
        }
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        if (modoEdicao && !confirmarDescarte()) return;
        entrarModoNovo();
    }//GEN-LAST:event_btnLimparActionPerformed

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        if (confirmarDescarte()) {
            voltarParaPrincipal();
        }
    }//GEN-LAST:event_btnVoltarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCadastrar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JLabel lblBusca;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblEstoque;
    private javax.swing.JLabel lblFormTitulo;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblPreco;
    private javax.swing.JLabel lblTabelaTitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlAcoesCabecalho;
    private javax.swing.JPanel pnlBotoes;
    private javax.swing.JPanel pnlCabecalho;
    private javax.swing.JPanel pnlCorpo;
    private javax.swing.JPanel pnlFormulario;
    private javax.swing.JPanel pnlTabela;
    private javax.swing.JPanel pnlTabelaTopo;
    private javax.swing.JPanel pnlTitulos;
    private javax.swing.JScrollPane scrollTabela;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtBusca;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtEstoque;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtPreco;
    // End of variables declaration//GEN-END:variables

    private javax.swing.table.DefaultTableModel modeloTabela;
}
