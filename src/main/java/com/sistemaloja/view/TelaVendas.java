package com.sistemaloja.view;

import com.sistemaloja.dao.ClienteDAO;
import com.sistemaloja.dao.ProdutoDAO;
import com.sistemaloja.dao.VendaDAO;
import com.sistemaloja.model.Cliente;
import com.sistemaloja.model.Produto;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TelaVendas extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(TelaVendas.class.getName());

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private List<Produto> produtos;

    private final List<ItemCarrinho> carrinho = new ArrayList<>();

    private DefaultTableModel modeloProdutos;
    private DefaultTableModel modeloCarrinho;
    private DefaultTableModel modeloHistorico;

    public TelaVendas() {
        initComponents();
        posConstrucao();
    }

    private void posConstrucao() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1150, 680));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmarSaida()) voltarParaPrincipal();
            }
        });

        configurarTabelaProdutos();
        configurarTabelaCarrinho();
        configurarTabelaHistorico();

        txtBuscaProduto.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filtrarProdutos(); }
            @Override public void removeUpdate(DocumentEvent e)  { filtrarProdutos(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrarProdutos(); }
        });

        tabelaProdutos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabelaProdutos.getSelectedRow() != -1) {
                    adicionarAoCarrinho();
                }
            }
        });

        txtBuscaProduto.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && tabelaProdutos.getRowCount() > 0) {
                    tabelaProdutos.setRowSelectionInterval(0, 0);
                    adicionarAoCarrinho();
                }
            }
        });

        tabelaCarrinho.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) atualizarEstadoBotoes();
        });

        configurarAtalhos();

        carregarClientes();
        carregarProdutos();
        carregarHistorico();
        atualizarTabelaCarrinho();
        txtBuscaProduto.requestFocusInWindow();
    }

    private void configurarTabelaProdutos() {
        modeloProdutos = new DefaultTableModel(
                new Object[]{"Código", "Produto", "Preço", "Estoque"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaProdutos.setModel(modeloProdutos);
        tabelaProdutos.setRowHeight(26);
        tabelaProdutos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaProdutos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabelaProdutos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaProdutos.setAutoCreateRowSorter(true);
        tabelaProdutos.setFillsViewportHeight(true);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        tabelaProdutos.getColumnModel().getColumn(2).setCellRenderer(right);
        tabelaProdutos.getColumnModel().getColumn(3).setCellRenderer(right);
    }

    private void configurarTabelaCarrinho() {
        modeloCarrinho = new DefaultTableModel(
                new Object[]{"Código", "Produto", "Qtd", "Preço Unit.", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaCarrinho.setModel(modeloCarrinho);
        tabelaCarrinho.setRowHeight(26);
        tabelaCarrinho.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaCarrinho.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabelaCarrinho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCarrinho.setFillsViewportHeight(true);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        tabelaCarrinho.getColumnModel().getColumn(2).setCellRenderer(right);
        tabelaCarrinho.getColumnModel().getColumn(3).setCellRenderer(right);
        tabelaCarrinho.getColumnModel().getColumn(4).setCellRenderer(right);
    }

    private void configurarTabelaHistorico() {
        modeloHistorico = new DefaultTableModel(
                new Object[]{"Código", "Cliente", "Produto", "Qtd", "Total", "Data"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaHistorico.setModel(modeloHistorico);
        tabelaHistorico.setRowHeight(24);
        tabelaHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaHistorico.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaHistorico.setAutoCreateRowSorter(true);
        tabelaHistorico.setFillsViewportHeight(true);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        tabelaHistorico.getColumnModel().getColumn(3).setCellRenderer(right);
        tabelaHistorico.getColumnModel().getColumn(4).setCellRenderer(right);
    }

    private void configurarAtalhos() {
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F,
                        java.awt.event.InputEvent.CTRL_DOWN_MASK), "focusBusca");
        getRootPane().getActionMap().put("focusBusca", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                txtBuscaProduto.requestFocusInWindow();
            }
        });

        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                        java.awt.event.InputEvent.CTRL_DOWN_MASK), "finalizar");
        getRootPane().getActionMap().put("finalizar", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                finalizarVenda();
            }
        });
    }

    private void carregarClientes() {
        try {
            cbCliente.removeAllItems();
            for (Cliente c : new ClienteDAO().listarAtivos()) {
                cbCliente.addItem(c);
            }
        } catch (SQLException e) {
            mostrarErro("Não foi possível carregar os clientes.", e);
        }
    }

    private void carregarProdutos() {
        try {
            produtos = new ProdutoDAO().listarAtivos();
            filtrarProdutos();
        } catch (SQLException e) {
            mostrarErro("Não foi possível carregar os produtos.", e);
        }
    }

    private void filtrarProdutos() {
        if (produtos == null) return;
        String filtro = txtBuscaProduto.getText().trim().toLowerCase();
        modeloProdutos.setRowCount(0);
        for (Produto p : produtos) {
            if (p.getEstoque() <= 0) continue;
            if (!filtro.isEmpty() && !p.getNome().toLowerCase().contains(filtro)) continue;
            modeloProdutos.addRow(new Object[]{
                p.getId(), p.getNome(), MOEDA.format(p.getPreco()), p.getEstoque()
            });
        }
    }

    private void carregarHistorico() {
        try {
            modeloHistorico.setRowCount(0);
            for (Object[] linha : new VendaDAO().listarTodas()) {
                linha[4] = MOEDA.format((Double) linha[4]);
                linha[5] = ((LocalDateTime) linha[5]).format(FMT_DATA);
                modeloHistorico.addRow(linha);
            }
        } catch (SQLException e) {
            mostrarErro("Não foi possível carregar o histórico.", e);
        }
    }

    private void adicionarAoCarrinho() {
        int linha = tabelaProdutos.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um produto na lista antes de adicionar.",
                    "Nenhum produto selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(tabelaProdutos.getValueAt(linha, 0).toString());
        Produto produto = buscarProduto(id);
        if (produto == null) return;

        ItemCarrinho existente = null;
        for (ItemCarrinho item : carrinho) {
            if (item.produto.getId() == id) { existente = item; break; }
        }

        int qtdAtual = existente != null ? existente.quantidade : 0;
        if (qtdAtual + 1 > produto.getEstoque()) {
            JOptionPane.showMessageDialog(this,
                    "Estoque insuficiente. Disponível: " + produto.getEstoque(),
                    "Estoque insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (existente != null) {
            existente.quantidade++;
        } else {
            carrinho.add(new ItemCarrinho(produto, 1));
        }

        atualizarTabelaCarrinho();
        txtBuscaProduto.setText("");
        txtBuscaProduto.requestFocusInWindow();
    }

    private void removerDoCarrinho() {
        int linha = tabelaCarrinho.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um item do carrinho para remover.",
                    "Nenhum item selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(tabelaCarrinho.getValueAt(linha, 0).toString());
        carrinho.removeIf(item -> item.produto.getId() == id);
        atualizarTabelaCarrinho();
    }

    private void atualizarTabelaCarrinho() {
        modeloCarrinho.setRowCount(0);
        for (ItemCarrinho item : carrinho) {
            modeloCarrinho.addRow(new Object[]{
                item.produto.getId(),
                item.produto.getNome(),
                item.quantidade,
                MOEDA.format(item.produto.getPreco()),
                MOEDA.format(item.subtotal())
            });
        }
        atualizarTotalCarrinho();
    }

    private void atualizarTotalCarrinho() {
        double total = 0;
        for (ItemCarrinho item : carrinho) total += item.subtotal();
        lblTotalValor.setText(MOEDA.format(total));
        atualizarEstadoBotoes();
    }

    private void atualizarEstadoBotoes() {
        boolean temItens = !carrinho.isEmpty();
        boolean temSelecao = tabelaCarrinho.getSelectedRow() != -1;
        btnFinalizar.setEnabled(temItens);
        btnLimpar.setEnabled(temItens);
        btnRemover.setEnabled(temSelecao);
    }

    private Produto buscarProduto(int id) {
        if (produtos == null) return null;
        for (Produto p : produtos) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private void finalizarVenda() {
        if (carrinho.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Adicione pelo menos um item ao carrinho.",
                    "Carrinho vazio", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cliente cliente = (Cliente) cbCliente.getSelectedItem();
        if (cliente == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um cliente antes de finalizar.",
                    "Cliente não selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder resumo = new StringBuilder();
        resumo.append("Cliente: ").append(cliente.getNome()).append("\n\n");
        double total = 0;
        for (ItemCarrinho item : carrinho) {
            resumo.append(String.format("• %s × %d = %s%n",
                    item.produto.getNome(), item.quantidade,
                    MOEDA.format(item.subtotal())));
            total += item.subtotal();
        }
        resumo.append("\nTotal: ").append(MOEDA.format(total));
        resumo.append("\n\nConfirmar venda?");

        int confirm = JOptionPane.showConfirmDialog(this, resumo.toString(),
                "Confirmar venda", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        Map<Integer, Integer> itens = new LinkedHashMap<>();
        for (ItemCarrinho item : carrinho) itens.put(item.produto.getId(), item.quantidade);

        try {
            new VendaDAO().registrarVenda(cliente.getId(), itens);
            mostrarSucesso("Venda registrada com sucesso!\nTotal: " + MOEDA.format(total));
            carrinho.clear();
            atualizarTabelaCarrinho();
            carregarProdutos();
            carregarHistorico();
            txtBuscaProduto.requestFocusInWindow();
        } catch (SQLException e) {
            mostrarErro("A venda não foi registrada", e);
        }
    }

    private void limparCarrinho() {
        if (carrinho.isEmpty()) return;
        int r = JOptionPane.showConfirmDialog(this,
                "Descartar todos os itens do carrinho?",
                "Limpar carrinho", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            carrinho.clear();
            atualizarTabelaCarrinho();
        }
    }

    private boolean confirmarSaida() {
        if (carrinho.isEmpty()) return true;
        int r = JOptionPane.showConfirmDialog(this,
                "Há itens no carrinho. Descartar e sair?",
                "Itens não finalizados", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }

    private void voltarParaPrincipal() {
        new TelaPrincipal().setVisible(true);
        dispose();
    }

    private void mostrarSucesso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarErro(String mensagem, Exception e) {
        logger.log(Level.SEVERE, mensagem, e);
        JOptionPane.showMessageDialog(this, mensagem + ":\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        btnVoltar = new javax.swing.JButton();
        lblCliente = new javax.swing.JLabel();
        cbCliente = new javax.swing.JComboBox<>();
        btnNovoCliente = new javax.swing.JButton();
        pnlEsquerdo = new javax.swing.JPanel();
        pnlProdutos = new javax.swing.JPanel();
        lblBusca = new javax.swing.JLabel();
        txtBuscaProduto = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        scrollProdutos = new javax.swing.JScrollPane();
        tabelaProdutos = new javax.swing.JTable();
        pnlCarrinho = new javax.swing.JPanel();
        lblCarrinho = new javax.swing.JLabel();
        scrollCarrinho = new javax.swing.JScrollPane();
        tabelaCarrinho = new javax.swing.JTable();
        btnRemover = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        lblTotalTitulo = new javax.swing.JLabel();
        lblTotalValor = new javax.swing.JLabel();
        btnFinalizar = new javax.swing.JButton();
        pnlHistorico = new javax.swing.JPanel();
        scrollHistorico = new javax.swing.JScrollPane();
        tabelaHistorico = new javax.swing.JTable();

        setTitle("Vendas - " + TelaPrincipal.NOME_LOJA);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitulo.setText("Registrar venda");

        btnVoltar.setText("← Voltar");
        btnVoltar.addActionListener(this::btnVoltarActionPerformed);

        lblCliente.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblCliente.setText("Cliente:");

        cbCliente.setFont(new java.awt.Font("Segoe UI", 0, 13));

        btnNovoCliente.setText("+ Novo cliente");
        btnNovoCliente.addActionListener(this::btnNovoClienteActionPerformed);

        pnlEsquerdo.setLayout(new java.awt.BorderLayout(0, 10));

        pnlProdutos.setBorder(javax.swing.BorderFactory.createTitledBorder("Produtos disponíveis"));

        lblBusca.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblBusca.setText("Buscar produto (Ctrl+F):");

        txtBuscaProduto.setFont(new java.awt.Font("Segoe UI", 0, 14));

        btnAdicionar.setText("Adicionar ao carrinho");
        btnAdicionar.addActionListener(this::btnAdicionarActionPerformed);

        tabelaProdutos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Código", "Produto", "Preço", "Estoque"}
        ));
        scrollProdutos.setViewportView(tabelaProdutos);

        javax.swing.GroupLayout pnlProdutosLayout = new javax.swing.GroupLayout(pnlProdutos);
        pnlProdutos.setLayout(pnlProdutosLayout);
        pnlProdutosLayout.setHorizontalGroup(
            pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollProdutos)
                    .addGroup(pnlProdutosLayout.createSequentialGroup()
                        .addGroup(pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblBusca)
                            .addGroup(pnlProdutosLayout.createSequentialGroup()
                                .addComponent(txtBuscaProduto, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlProdutosLayout.setVerticalGroup(
            pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBusca)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscaProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrollProdutos, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlCarrinho.setBorder(javax.swing.BorderFactory.createTitledBorder("Carrinho"));

        lblCarrinho.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblCarrinho.setText("Itens adicionados (duplo clique na lista de produtos adiciona automaticamente)");

        tabelaCarrinho.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Código", "Produto", "Qtd", "Preço Unit.", "Subtotal"}
        ));
        scrollCarrinho.setViewportView(tabelaCarrinho);

        btnRemover.setText("Remover item");
        btnRemover.addActionListener(this::btnRemoverActionPerformed);

        btnLimpar.setText("Limpar carrinho");
        btnLimpar.addActionListener(this::btnLimparActionPerformed);

        lblTotalTitulo.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblTotalTitulo.setText("TOTAL:");

        lblTotalValor.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTotalValor.setText("R$ 0,00");

        btnFinalizar.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnFinalizar.setText("Finalizar venda (Ctrl+Enter)");
        btnFinalizar.addActionListener(this::btnFinalizarActionPerformed);

        javax.swing.GroupLayout pnlCarrinhoLayout = new javax.swing.GroupLayout(pnlCarrinho);
        pnlCarrinho.setLayout(pnlCarrinhoLayout);
        pnlCarrinhoLayout.setHorizontalGroup(
            pnlCarrinhoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCarrinhoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCarrinhoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollCarrinho)
                    .addComponent(lblCarrinho, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCarrinhoLayout.createSequentialGroup()
                        .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTotalTitulo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblTotalValor))
                    .addComponent(btnFinalizar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlCarrinhoLayout.setVerticalGroup(
            pnlCarrinhoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCarrinhoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCarrinho)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollCarrinho, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlCarrinhoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalTitulo)
                    .addComponent(lblTotalValor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnFinalizar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlEsquerdo.add(pnlProdutos, java.awt.BorderLayout.CENTER);
        pnlEsquerdo.add(pnlCarrinho, java.awt.BorderLayout.SOUTH);

        pnlHistorico.setBorder(javax.swing.BorderFactory.createTitledBorder("Histórico de vendas"));

        tabelaHistorico.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Código", "Cliente", "Produto", "Qtd", "Total", "Data"}
        ));
        scrollHistorico.setViewportView(tabelaHistorico);

        javax.swing.GroupLayout pnlHistoricoLayout = new javax.swing.GroupLayout(pnlHistorico);
        pnlHistorico.setLayout(pnlHistoricoLayout);
        pnlHistoricoLayout.setHorizontalGroup(
            pnlHistoricoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHistoricoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollHistorico, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlHistoricoLayout.setVerticalGroup(
            pnlHistoricoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHistoricoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollHistorico)
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
                        .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblCliente)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNovoCliente))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlEsquerdo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlHistorico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVoltar)
                    .addComponent(lblTitulo)
                    .addComponent(lblCliente)
                    .addComponent(cbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNovoCliente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlEsquerdo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlHistorico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {
        if (confirmarSaida()) voltarParaPrincipal();
    }

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {
        adicionarAoCarrinho();
    }

    private void btnRemoverActionPerformed(java.awt.event.ActionEvent evt) {
        removerDoCarrinho();
    }

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {
        limparCarrinho();
    }

    private void btnFinalizarActionPerformed(java.awt.event.ActionEvent evt) {
        finalizarVenda();
    }

    private void btnNovoClienteActionPerformed(java.awt.event.ActionEvent evt) {
        if (confirmarSaida()) {
            new TelaClientes().setVisible(true);
            dispose();
        }
    }

    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnFinalizar;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnNovoCliente;
    private javax.swing.JButton btnRemover;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JComboBox<Cliente> cbCliente;
    private javax.swing.JLabel lblBusca;
    private javax.swing.JLabel lblCarrinho;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotalTitulo;
    private javax.swing.JLabel lblTotalValor;
    private javax.swing.JPanel pnlCarrinho;
    private javax.swing.JPanel pnlEsquerdo;
    private javax.swing.JPanel pnlHistorico;
    private javax.swing.JPanel pnlProdutos;
    private javax.swing.JScrollPane scrollCarrinho;
    private javax.swing.JScrollPane scrollHistorico;
    private javax.swing.JScrollPane scrollProdutos;
    private javax.swing.JTable tabelaCarrinho;
    private javax.swing.JTable tabelaHistorico;
    private javax.swing.JTable tabelaProdutos;
    private javax.swing.JTextField txtBuscaProduto;

    private static class ItemCarrinho {
        final Produto produto;
        int quantidade;

        ItemCarrinho(Produto produto, int quantidade) {
            this.produto = produto;
            this.quantidade = quantidade;
        }
        double subtotal() { return produto.getPreco() * quantidade; }
    }
}
