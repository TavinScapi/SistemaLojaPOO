package com.sistemaloja.view;

import com.sistemaloja.dao.ClienteDAO;
import com.sistemaloja.dao.ProdutoDAO;
import com.sistemaloja.dao.VendaDAO;
import com.sistemaloja.model.Cliente;
import com.sistemaloja.model.Produto;
import java.awt.Dimension;
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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmarSaida()) voltarParaPrincipal();
            }
        });

        Estilo.aplicarHover(btnVoltar, btnNovoCliente, btnAdicionar, btnRemover, btnLimpar, btnFinalizar);
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
        Estilo.estilizarTabela(tabelaProdutos);
        tabelaProdutos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaProdutos.setAutoCreateRowSorter(true);
        Estilo.alinharDireita(tabelaProdutos, 2, 3);
    }

    private void configurarTabelaCarrinho() {
        modeloCarrinho = new DefaultTableModel(
                new Object[]{"Código", "Produto", "Qtd", "Preço Unit.", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaCarrinho.setModel(modeloCarrinho);
        Estilo.estilizarTabela(tabelaCarrinho);
        tabelaCarrinho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Estilo.alinharDireita(tabelaCarrinho, 2, 3, 4);
    }

    private void configurarTabelaHistorico() {
        modeloHistorico = new DefaultTableModel(
                new Object[]{"Código", "Cliente", "Produto", "Qtd", "Total", "Data"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaHistorico.setModel(modeloHistorico);
        Estilo.estilizarTabela(tabelaHistorico);
        tabelaHistorico.setAutoCreateRowSorter(true);
        Estilo.alinharDireita(tabelaHistorico, 3, 4);
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
        pnlEsquerdo = new javax.swing.JPanel();
        pnlProdutos = new javax.swing.JPanel();
        pnlProdutosTopo = new javax.swing.JPanel();
        lblProdutosTitulo = new javax.swing.JLabel();
        pnlBuscaProduto = new javax.swing.JPanel();
        lblBusca = new javax.swing.JLabel();
        txtBuscaProduto = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        scrollProdutos = new javax.swing.JScrollPane();
        tabelaProdutos = new javax.swing.JTable();
        pnlHistorico = new javax.swing.JPanel();
        lblHistoricoTitulo = new javax.swing.JLabel();
        scrollHistorico = new javax.swing.JScrollPane();
        tabelaHistorico = new javax.swing.JTable();
        pnlDireito = new javax.swing.JPanel();
        pnlClienteBarra = new javax.swing.JPanel();
        lblCliente = new javax.swing.JLabel();
        cbCliente = new javax.swing.JComboBox<>();
        btnNovoCliente = new javax.swing.JButton();
        pnlCarrinho = new javax.swing.JPanel();
        pnlCarrinhoTopo = new javax.swing.JPanel();
        lblCarrinho = new javax.swing.JLabel();
        lblCarrinhoDica = new javax.swing.JLabel();
        scrollCarrinho = new javax.swing.JScrollPane();
        tabelaCarrinho = new javax.swing.JTable();
        pnlCarrinhoRodape = new javax.swing.JPanel();
        pnlTotal = new javax.swing.JPanel();
        lblTotalTitulo = new javax.swing.JLabel();
        lblTotalValor = new javax.swing.JLabel();
        pnlCarrinhoBotoes = new javax.swing.JPanel();
        btnRemover = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        btnFinalizar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Vendas - OctoMax");
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
        lblTitulo.setText("Registrar venda");

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
        pnlEsquerdo.setOpaque(false);
        pnlEsquerdo.setLayout(new java.awt.BorderLayout(0, 16));
        pnlProdutos.setBackground(new java.awt.Color(255, 255, 255));
        pnlProdutos.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)), javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        pnlProdutos.setLayout(new java.awt.BorderLayout(0, 10));
        pnlProdutosTopo.setOpaque(false);
        pnlProdutosTopo.setLayout(new java.awt.BorderLayout(0, 8));
        lblProdutosTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15));
        lblProdutosTitulo.setForeground(new java.awt.Color(33, 37, 41));
        lblProdutosTitulo.setText("Produtos disponíveis");

        pnlProdutosTopo.add(lblProdutosTitulo, java.awt.BorderLayout.NORTH);
        pnlBuscaProduto.setOpaque(false);
        pnlBuscaProduto.setLayout(new java.awt.BorderLayout(10, 0));
        lblBusca.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblBusca.setForeground(new java.awt.Color(108, 117, 125));
        lblBusca.setText("Buscar produto (Ctrl+F):");

        pnlBuscaProduto.add(lblBusca, java.awt.BorderLayout.WEST);
        txtBuscaProduto.setFont(new java.awt.Font("Segoe UI", 0, 14));

        pnlBuscaProduto.add(txtBuscaProduto, java.awt.BorderLayout.CENTER);
        btnAdicionar.setBackground(new java.awt.Color(25, 135, 84));
        btnAdicionar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnAdicionar.setForeground(new java.awt.Color(255, 255, 255));
        btnAdicionar.setText("Adicionar ao carrinho");
        btnAdicionar.setBorderPainted(false);
        btnAdicionar.setFocusPainted(false);
        btnAdicionar.setOpaque(true);
        btnAdicionar.setPreferredSize(new java.awt.Dimension(200, 34));
        btnAdicionar.addActionListener(this::btnAdicionarActionPerformed);

        pnlBuscaProduto.add(btnAdicionar, java.awt.BorderLayout.EAST);

        pnlProdutosTopo.add(pnlBuscaProduto, java.awt.BorderLayout.CENTER);

        pnlProdutos.add(pnlProdutosTopo, java.awt.BorderLayout.NORTH);

        scrollProdutos.setViewportView(tabelaProdutos);

        pnlProdutos.add(scrollProdutos, java.awt.BorderLayout.CENTER);

        pnlEsquerdo.add(pnlProdutos, java.awt.BorderLayout.CENTER);
        pnlHistorico.setBackground(new java.awt.Color(255, 255, 255));
        pnlHistorico.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)), javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        pnlHistorico.setPreferredSize(new java.awt.Dimension(400, 210));
        pnlHistorico.setLayout(new java.awt.BorderLayout(0, 10));
        lblHistoricoTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15));
        lblHistoricoTitulo.setForeground(new java.awt.Color(33, 37, 41));
        lblHistoricoTitulo.setText("Histórico de vendas");

        pnlHistorico.add(lblHistoricoTitulo, java.awt.BorderLayout.NORTH);

        scrollHistorico.setViewportView(tabelaHistorico);

        pnlHistorico.add(scrollHistorico, java.awt.BorderLayout.CENTER);

        pnlEsquerdo.add(pnlHistorico, java.awt.BorderLayout.SOUTH);

        pnlCorpo.add(pnlEsquerdo, java.awt.BorderLayout.CENTER);
        pnlDireito.setOpaque(false);
        pnlDireito.setPreferredSize(new java.awt.Dimension(430, 500));
        pnlDireito.setLayout(new java.awt.BorderLayout(0, 16));
        pnlClienteBarra.setBackground(new java.awt.Color(255, 255, 255));
        pnlClienteBarra.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)), javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        pnlClienteBarra.setLayout(new java.awt.BorderLayout(8, 8));
        lblCliente.setFont(new java.awt.Font("Segoe UI", 1, 15));
        lblCliente.setForeground(new java.awt.Color(33, 37, 41));
        lblCliente.setText("Cliente");

        pnlClienteBarra.add(lblCliente, java.awt.BorderLayout.NORTH);
        cbCliente.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cbCliente.setPreferredSize(new java.awt.Dimension(240, 34));

        pnlClienteBarra.add(cbCliente, java.awt.BorderLayout.CENTER);
        btnNovoCliente.setBackground(new java.awt.Color(13, 110, 253));
        btnNovoCliente.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnNovoCliente.setForeground(new java.awt.Color(255, 255, 255));
        btnNovoCliente.setText("+ Novo");
        btnNovoCliente.setBorderPainted(false);
        btnNovoCliente.setFocusPainted(false);
        btnNovoCliente.setOpaque(true);
        btnNovoCliente.setPreferredSize(new java.awt.Dimension(90, 34));
        btnNovoCliente.addActionListener(this::btnNovoClienteActionPerformed);

        pnlClienteBarra.add(btnNovoCliente, java.awt.BorderLayout.EAST);

        pnlDireito.add(pnlClienteBarra, java.awt.BorderLayout.NORTH);
        pnlCarrinho.setBackground(new java.awt.Color(255, 255, 255));
        pnlCarrinho.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)), javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        pnlCarrinho.setLayout(new java.awt.BorderLayout(0, 10));
        pnlCarrinhoTopo.setOpaque(false);
        pnlCarrinhoTopo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblCarrinho.setFont(new java.awt.Font("Segoe UI", 1, 15));
        lblCarrinho.setForeground(new java.awt.Color(33, 37, 41));
        lblCarrinho.setText("Carrinho");

        pnlCarrinhoTopo.add(lblCarrinho);
        lblCarrinhoDica.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblCarrinhoDica.setForeground(new java.awt.Color(108, 117, 125));
        lblCarrinhoDica.setText("Dê um duplo clique em um produto para adicioná-lo");

        pnlCarrinhoTopo.add(lblCarrinhoDica);

        pnlCarrinho.add(pnlCarrinhoTopo, java.awt.BorderLayout.NORTH);

        scrollCarrinho.setViewportView(tabelaCarrinho);

        pnlCarrinho.add(scrollCarrinho, java.awt.BorderLayout.CENTER);
        pnlCarrinhoRodape.setOpaque(false);
        pnlCarrinhoRodape.setLayout(new java.awt.BorderLayout(0, 10));
        pnlTotal.setOpaque(false);
        pnlTotal.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        lblTotalTitulo.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblTotalTitulo.setForeground(new java.awt.Color(108, 117, 125));
        lblTotalTitulo.setText("TOTAL:");

        pnlTotal.add(lblTotalTitulo);
        lblTotalValor.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblTotalValor.setForeground(new java.awt.Color(33, 37, 41));
        lblTotalValor.setText("R$ 0,00");

        pnlTotal.add(lblTotalValor);

        pnlCarrinhoRodape.add(pnlTotal, java.awt.BorderLayout.NORTH);
        pnlCarrinhoBotoes.setOpaque(false);
        pnlCarrinhoBotoes.setLayout(new java.awt.GridLayout(1, 2, 8, 0));
        btnRemover.setBackground(new java.awt.Color(108, 117, 125));
        btnRemover.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnRemover.setForeground(new java.awt.Color(255, 255, 255));
        btnRemover.setText("Remover item");
        btnRemover.setBorderPainted(false);
        btnRemover.setFocusPainted(false);
        btnRemover.setOpaque(true);
        btnRemover.setPreferredSize(new java.awt.Dimension(120, 36));
        btnRemover.addActionListener(this::btnRemoverActionPerformed);

        pnlCarrinhoBotoes.add(btnRemover);
        btnLimpar.setBackground(new java.awt.Color(220, 53, 69));
        btnLimpar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnLimpar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpar.setText("Limpar carrinho");
        btnLimpar.setBorderPainted(false);
        btnLimpar.setFocusPainted(false);
        btnLimpar.setOpaque(true);
        btnLimpar.setPreferredSize(new java.awt.Dimension(120, 36));
        btnLimpar.addActionListener(this::btnLimparActionPerformed);

        pnlCarrinhoBotoes.add(btnLimpar);

        pnlCarrinhoRodape.add(pnlCarrinhoBotoes, java.awt.BorderLayout.CENTER);
        btnFinalizar.setBackground(new java.awt.Color(214, 92, 0));
        btnFinalizar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnFinalizar.setForeground(new java.awt.Color(255, 255, 255));
        btnFinalizar.setText("Finalizar venda (Ctrl+Enter)");
        btnFinalizar.setBorderPainted(false);
        btnFinalizar.setFocusPainted(false);
        btnFinalizar.setOpaque(true);
        btnFinalizar.setPreferredSize(new java.awt.Dimension(250, 44));
        btnFinalizar.addActionListener(this::btnFinalizarActionPerformed);

        pnlCarrinhoRodape.add(btnFinalizar, java.awt.BorderLayout.SOUTH);

        pnlCarrinho.add(pnlCarrinhoRodape, java.awt.BorderLayout.SOUTH);

        pnlDireito.add(pnlCarrinho, java.awt.BorderLayout.CENTER);

        pnlCorpo.add(pnlDireito, java.awt.BorderLayout.EAST);

        getContentPane().add(pnlCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        if (confirmarSaida()) voltarParaPrincipal();
    }//GEN-LAST:event_btnVoltarActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionarAoCarrinho();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnRemoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverActionPerformed
        removerDoCarrinho();
    }//GEN-LAST:event_btnRemoverActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        limparCarrinho();
    }//GEN-LAST:event_btnLimparActionPerformed

    private void btnFinalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizarActionPerformed
        finalizarVenda();
    }//GEN-LAST:event_btnFinalizarActionPerformed

    private void btnNovoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovoClienteActionPerformed
        if (confirmarSaida()) {
            new TelaClientes().setVisible(true);
            dispose();
        }
    }//GEN-LAST:event_btnNovoClienteActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnFinalizar;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnNovoCliente;
    private javax.swing.JButton btnRemover;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JComboBox<Cliente> cbCliente;
    private javax.swing.JLabel lblBusca;
    private javax.swing.JLabel lblCarrinho;
    private javax.swing.JLabel lblCarrinhoDica;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblHistoricoTitulo;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblProdutosTitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotalTitulo;
    private javax.swing.JLabel lblTotalValor;
    private javax.swing.JPanel pnlAcoesCabecalho;
    private javax.swing.JPanel pnlBuscaProduto;
    private javax.swing.JPanel pnlCabecalho;
    private javax.swing.JPanel pnlCarrinho;
    private javax.swing.JPanel pnlCarrinhoBotoes;
    private javax.swing.JPanel pnlCarrinhoRodape;
    private javax.swing.JPanel pnlCarrinhoTopo;
    private javax.swing.JPanel pnlClienteBarra;
    private javax.swing.JPanel pnlCorpo;
    private javax.swing.JPanel pnlDireito;
    private javax.swing.JPanel pnlEsquerdo;
    private javax.swing.JPanel pnlHistorico;
    private javax.swing.JPanel pnlProdutos;
    private javax.swing.JPanel pnlProdutosTopo;
    private javax.swing.JPanel pnlTitulos;
    private javax.swing.JPanel pnlTotal;
    private javax.swing.JScrollPane scrollCarrinho;
    private javax.swing.JScrollPane scrollHistorico;
    private javax.swing.JScrollPane scrollProdutos;
    private javax.swing.JTable tabelaCarrinho;
    private javax.swing.JTable tabelaHistorico;
    private javax.swing.JTable tabelaProdutos;
    private javax.swing.JTextField txtBuscaProduto;
    // End of variables declaration//GEN-END:variables

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
