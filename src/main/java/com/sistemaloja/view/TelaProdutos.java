package com.sistemaloja.view;

import com.sistemaloja.dao.ProdutoDAO;
import com.sistemaloja.model.Produto;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class TelaProdutos extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(TelaProdutos.class.getName());

    private static final Locale BR = new Locale("pt", "BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(BR);

    private boolean modoEdicao = false;
    private int idProdutoSelecionado = -1;

    public TelaProdutos() {
        initComponents();
        posConstrucao();
    }

    private void posConstrucao() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(950, 560));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmarDescarte()) {
                    voltarParaPrincipal();
                }
            }
        });

        aplicarHover(btnCadastrar);
        aplicarHover(btnEditar);
        aplicarHover(btnExcluir);
        aplicarHover(btnLimpar);
        aplicarHover(btnVoltar);

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

    private void aplicarHover(JButton btn) {
        Color base = btn.getBackground();
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(base.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(base);
            }
        });
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
        tabela.setRowHeight(26);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setShowGrid(false);

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() != -1) {
                    selecionarProduto();
                }
            }
        });

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() != -1) {
                selecionarProduto();
            }
        });
    }

    private void carregarProdutos() {
        try {
            List<Produto> produtos = new ProdutoDAO().listarTodos();
            modeloTabela.setRowCount(0);
            for (Produto p : produtos) {
                modeloTabela.addRow(new Object[]{
                    p.getId(), p.getNome(), MOEDA.format(p.getPreco()), p.getEstoque()
                });
            }
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar produtos", e);
        }
    }

    private void filtrarTabela() {
        String filtro = txtBusca.getText().trim().toLowerCase();
        if (filtro.isEmpty()) {
            carregarProdutos();
            return;
        }
        try {
            List<Produto> produtos = new ProdutoDAO().listarTodos();
            modeloTabela.setRowCount(0);
            for (Produto p : produtos) {
                if (p.getNome().toLowerCase().contains(filtro)) {
                    modeloTabela.addRow(new Object[]{
                        p.getId(), p.getNome(), MOEDA.format(p.getPreco()), p.getEstoque()
                    });
                }
            }
        } catch (SQLException e) {
            mostrarErro("Erro ao filtrar produtos", e);
        }
    }

    private void selecionarProduto() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return;
        if (modoEdicao && !confirmarDescarte()) return;

        idProdutoSelecionado = Integer.parseInt(tabela.getValueAt(linha, 0).toString());
        txtCodigo.setText(String.valueOf(idProdutoSelecionado));
        txtNome.setText(tabela.getValueAt(linha, 1).toString());

        String precoFormatado = tabela.getValueAt(linha, 2).toString();
        String precoBruto = precoFormatado
                .replace("R$", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        txtPreco.setText(precoBruto);
        txtEstoque.setText(tabela.getValueAt(linha, 3).toString());
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
        lblTitulo.setText("Cadastro de Produtos");
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

        btnCadastrar.setText("Salvar alterações");
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

    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        btnVoltar = new javax.swing.JButton();
        pnlFormulario = new javax.swing.JPanel();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblNome = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        lblPreco = new javax.swing.JLabel();
        txtPreco = new javax.swing.JTextField();
        lblEstoque = new javax.swing.JLabel();
        txtEstoque = new javax.swing.JTextField();
        btnCadastrar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        pnlTabela = new javax.swing.JPanel();
        lblBusca = new javax.swing.JLabel();
        txtBusca = new javax.swing.JTextField();
        scrollTabela = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Produtos - Sistema de Loja");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Cadastro de Produtos");

        btnVoltar.setText("← Voltar");
        btnVoltar.addActionListener(this::btnVoltarActionPerformed);

        pnlFormulario.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)),
                javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        lblCodigo.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblCodigo.setText("Código");

        txtCodigo.setEditable(false);
        txtCodigo.setBackground(new java.awt.Color(233, 236, 239));
        txtCodigo.setFont(new java.awt.Font("Segoe UI", 0, 14));

        lblNome.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblNome.setText("Nome do Produto");

        txtNome.setFont(new java.awt.Font("Segoe UI", 0, 14));

        lblPreco.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblPreco.setText("Preço (R$)");

        txtPreco.setFont(new java.awt.Font("Segoe UI", 0, 14));

        lblEstoque.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblEstoque.setText("Estoque");

        txtEstoque.setFont(new java.awt.Font("Segoe UI", 0, 14));

        btnCadastrar.setText("Cadastrar");
        btnCadastrar.addActionListener(this::btnCadastrarActionPerformed);

        btnEditar.setText("Editar");
        btnEditar.addActionListener(this::btnEditarActionPerformed);

        btnExcluir.setText("Excluir");
        btnExcluir.addActionListener(this::btnExcluirActionPerformed);

        btnLimpar.setText("Limpar");
        btnLimpar.addActionListener(this::btnLimparActionPerformed);

        javax.swing.GroupLayout pnlFormularioLayout = new javax.swing.GroupLayout(pnlFormulario);
        pnlFormulario.setLayout(pnlFormularioLayout);
        pnlFormularioLayout.setHorizontalGroup(
            pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNome)
                    .addComponent(txtPreco)
                    .addComponent(txtEstoque)
                    .addGroup(pnlFormularioLayout.createSequentialGroup()
                        .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCodigo)
                            .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(lblNome)
                    .addComponent(lblPreco)
                    .addComponent(lblEstoque)
                    .addGroup(pnlFormularioLayout.createSequentialGroup()
                        .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnCadastrar, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                            .addComponent(btnEditar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnLimpar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnExcluir, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlFormularioLayout.setVerticalGroup(
            pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCodigo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblNome)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblPreco)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPreco, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblEstoque)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCadastrar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTabela.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)),
                javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        lblBusca.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblBusca.setText("Buscar por nome:");

        txtBusca.setFont(new java.awt.Font("Segoe UI", 0, 13));

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Código", "Nome", "Preço", "Estoque"
            }
        ));
        scrollTabela.setViewportView(tabela);

        javax.swing.GroupLayout pnlTabelaLayout = new javax.swing.GroupLayout(pnlTabela);
        pnlTabela.setLayout(pnlTabelaLayout);
        pnlTabelaLayout.setHorizontalGroup(
            pnlTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTabelaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollTabela, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                    .addGroup(pnlTabelaLayout.createSequentialGroup()
                        .addComponent(lblBusca)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBusca)))
                .addContainerGap())
        );
        pnlTabelaLayout.setVerticalGroup(
            pnlTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTabelaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBusca)
                    .addComponent(txtBusca, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrollTabela, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnVoltar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlFormulario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlTabela, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVoltar)
                    .addComponent(lblTitulo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFormulario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTabela, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }

    private void btnCadastrarActionPerformed(java.awt.event.ActionEvent evt) {
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
    }

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {
        if (idProdutoSelecionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um produto na tabela primeiro.",
                    "Nenhum produto selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        entrarModoEdicao();
    }

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {
        if (idProdutoSelecionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um produto para excluir.",
                    "Nenhum produto selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir este produto?",
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
    }

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {
        if (modoEdicao && !confirmarDescarte()) return;
        entrarModoNovo();
    }

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {
        if (confirmarDescarte()) {
            voltarParaPrincipal();
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new TelaProdutos().setVisible(true));
    }

    private javax.swing.JButton btnCadastrar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JLabel lblBusca;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblEstoque;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblPreco;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFormulario;
    private javax.swing.JPanel pnlTabela;
    private javax.swing.JScrollPane scrollTabela;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtBusca;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtEstoque;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtPreco;

    private javax.swing.table.DefaultTableModel modeloTabela;
}
