



package com.sistemaloja.view;

import com.sistemaloja.model.Cliente;
import com.sistemaloja.service.ClienteService;
import com.sistemaloja.service.ClienteService.RegraNegocioException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
















public class TelaClientes extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(TelaClientes.class.getName());

    private static final DateTimeFormatter FMT_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClienteService service = new ClienteService();

    private boolean modoEdicao = false;
    private int idClienteSelecionado = -1;

    
    private String snapshotNome = "";
    private String snapshotEmail = "";
    private String snapshotTelefone = "";

    public TelaClientes() {
        initComponents();
        posConstrucao();
    }

    private void posConstrucao() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 640));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmarDescarte()) voltarParaPrincipal();
            }
        });

        configurarTabela();
        configurarMascaras();
        configurarBusca();

        
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() != -1) {
                selecionarCliente();
            }
        });

        SwingUtilities.invokeLater(() -> txtNome.requestFocusInWindow());
        entrarModoNovo();
        carregarClientesAsync();
    }

    
    private void configurarTabela() {
        modeloTabela = new DefaultTableModel(
                new Object[]{"Código", "Nome", "E-mail", "Telefone"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela.setModel(modeloTabela);
        tabela.setRowHeight(26);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        tabela.setFillsViewportHeight(true);
    }

    
    private void configurarMascaras() {
        try {
            MaskFormatter telMask = new MaskFormatter("(##) #####-####");
            telMask.setPlaceholderCharacter('_');
            telMask.install(txtTelefone);

            MaskFormatter cpfMask = new MaskFormatter("###.###.###-##");
            cpfMask.setPlaceholderCharacter('_');
            cpfMask.install(txtCpf);
        } catch (java.text.ParseException ex) {
            logger.log(Level.WARNING, "Falha ao instalar máscara", ex);
        }
    }

    
    private void configurarBusca() {
        javax.swing.Timer timer = new javax.swing.Timer(300, e -> buscarAsync());
        timer.setRepeats(false);

        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { timer.restart(); }
            @Override public void removeUpdate(DocumentEvent e)  { timer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { timer.restart(); }
        });
    }

    
    private void carregarClientesAsync() {
        new SwingWorker<List<Cliente>, Void>() {
            @Override protected List<Cliente> doInBackground() throws Exception {
                return service.listarAtivos();
            }
            @Override protected void done() {
                try {
                    preencherTabela(get());
                } catch (Exception ex) {
                    mostrarErro("Erro ao carregar clientes", ex);
                }
            }
        }.execute();
    }

    private void buscarAsync() {
        String termo = txtBusca.getText().trim();
        new SwingWorker<List<Cliente>, Void>() {
            @Override protected List<Cliente> doInBackground() throws Exception {
                return service.buscar(termo);
            }
            @Override protected void done() {
                try {
                    preencherTabela(get());
                } catch (Exception ex) {
                    mostrarErro("Erro ao buscar clientes", ex);
                }
            }
        }.execute();
    }

    private void preencherTabela(List<Cliente> clientes) {
        modeloTabela.setRowCount(0);
        for (Cliente c : clientes) {
            modeloTabela.addRow(new Object[]{
                c.getId(), c.getNome(), c.getEmail(), c.getTelefone()
            });
        }
    }

    
    private void selecionarCliente() {
        int linhaVisual = tabela.getSelectedRow();
        if (linhaVisual == -1) return;

        
        int linhaModelo = tabela.convertRowIndexToModel(linhaVisual);

        if (modoEdicao && !confirmarDescarte()) return;

        idClienteSelecionado = Integer.parseInt(
                tabela.getModel().getValueAt(linhaModelo, 0).toString());
        txtCodigo.setText(String.valueOf(idClienteSelecionado));
        txtNome.setText(tabela.getModel().getValueAt(linhaModelo, 1).toString());
        txtEmail.setText(tabela.getModel().getValueAt(linhaModelo, 2).toString());
        txtTelefone.setText(tabela.getModel().getValueAt(linhaModelo, 3).toString());

        
        carregarDetalhesAsync(idClienteSelecionado);

        entrarModoVisualizacao();
    }

    private void carregarDetalhesAsync(int id) {
        new SwingWorker<Cliente, Void>() {
            @Override protected Cliente doInBackground() throws Exception {
                return service.buscarPorId(id);
            }
            @Override protected void done() {
                try {
                    Cliente c = get();
                    if (c != null) {
                        txtCpf.setText(c.getCpf() == null ? "" : c.getCpf());
                        txtEndereco.setText(c.getEndereco() == null ? "" : c.getEndereco());
                        txtObservacoes.setText(c.getObservacoes() == null ? "" : c.getObservacoes());
                    }
                } catch (Exception ex) {
                    mostrarErro("Erro ao carregar detalhes do cliente", ex);
                }
            }
        }.execute();
    }

    
    private void entrarModoNovo() {
        modoEdicao = false;
        idClienteSelecionado = -1;

        txtCodigo.setText("");
        txtNome.setText("");
        txtCpf.setText("");
        txtEmail.setText("");
        txtTelefone.setText("");
        txtEndereco.setText("");
        txtObservacoes.setText("");
        txtCodigo.setEditable(false);
        txtNome.setEditable(true);
        txtCpf.setEditable(true);
        txtEmail.setEditable(true);
        txtTelefone.setEditable(true);
        txtEndereco.setEditable(true);
        txtObservacoes.setEditable(true);

        btnCadastrar.setText("Cadastrar");
        btnCadastrar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnLimpar.setText("Limpar");
        lblTitulo.setText("Cadastro de clientes");
        tabela.clearSelection();
        atualizarSnapshot();
        txtNome.requestFocusInWindow();
    }

    private void entrarModoVisualizacao() {
        modoEdicao = false;
        txtNome.setEditable(false);
        txtCpf.setEditable(false);
        txtEmail.setEditable(false);
        txtTelefone.setEditable(false);
        txtEndereco.setEditable(false);
        txtObservacoes.setEditable(false);

        btnCadastrar.setText("Cadastrar");
        btnCadastrar.setEnabled(false);
        btnEditar.setEnabled(true);
        btnExcluir.setEnabled(true);
        btnLimpar.setText("Limpar seleção");
        lblTitulo.setText("Cliente selecionado");
    }

    private void entrarModoEdicao() {
        modoEdicao = true;
        txtNome.setEditable(true);
        txtCpf.setEditable(true);
        txtEmail.setEditable(true);
        txtTelefone.setEditable(true);
        txtEndereco.setEditable(true);
        txtObservacoes.setEditable(true);

        btnCadastrar.setText("Salvar alterações");
        btnCadastrar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnLimpar.setText("Cancelar edição");
        lblTitulo.setText("Editar cliente");
        atualizarSnapshot();
        txtNome.requestFocusInWindow();
    }

    
    private void atualizarSnapshot() {
        snapshotNome = txtNome.getText();
        snapshotEmail = txtEmail.getText();
        snapshotTelefone = txtTelefone.getText();
    }

    private boolean formularioAlterado() {
        return !txtNome.getText().equals(snapshotNome)
            || !txtEmail.getText().equals(snapshotEmail)
            || !txtTelefone.getText().equals(snapshotTelefone);
    }

    private boolean confirmarDescarte() {
        
        if (!modoEdicao && !formularioAlterado()) return true;
        if (modoEdicao && !formularioAlterado()) {
            
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Há dados não salvos. Descartar?",
                "Alterações pendentes",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }

    
    private void mostrarSucesso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    



    private void mostrarErro(String contexto, Exception ex) {
        logger.log(Level.SEVERE, contexto, ex);
        String msg = contexto;
        if (ex instanceof RegraNegocioException) {
            msg = ex.getMessage();
        } else if (ex instanceof SQLException) {
            msg = "Erro de banco de dados. Contate o suporte.";
        } else if (ex.getMessage() != null) {
            msg += "\n" + ex.getMessage();
        }
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void voltarParaPrincipal() {
        new TelaPrincipal().setVisible(true);
        dispose();
    }

    
    private void btnCadastrarActionPerformed(java.awt.event.ActionEvent evt) {
        String nome = txtNome.getText().trim();
        
        String cpfTexto = txtCpf.getText().replaceAll("\\D", "");
        String cpf = cpfTexto.isEmpty() ? null : cpfTexto; 
        
        String email = txtEmail.getText().trim();
        
        String telTexto = txtTelefone.getText().replaceAll("\\D", "");
        String telefone = telTexto.isEmpty() ? null : telTexto; 
        
        String endereco = txtEndereco.getText().trim();
        String observacoes = txtObservacoes.getText().trim();

        Cliente cliente = modoEdicao
                ? new Cliente(idClienteSelecionado, nome, cpf, email, telefone, endereco, observacoes)
                : new Cliente(nome, cpf, email, telefone, endereco, observacoes);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                if (modoEdicao) service.atualizar(cliente);
                else service.cadastrar(cliente);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    mostrarSucesso(modoEdicao
                            ? "Cliente atualizado com sucesso!"
                            : "Cliente cadastrado com sucesso!");
                    entrarModoNovo();
                    carregarClientesAsync();
                } catch (Exception ex) {
                    mostrarErro("Erro ao salvar cliente", ex);
                }
            }
        }.execute();
    }

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {
        if (idClienteSelecionado == -1) return;
        entrarModoEdicao();
    }

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {
        if (idClienteSelecionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um cliente na tabela antes de inativar.",
                    "Nenhum cliente selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Deseja realmente inativar este cliente?\nO histórico de vendas será preservado.",
                "Confirmar inativação",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                service.inativar(idClienteSelecionado);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    mostrarSucesso("Cliente inativado com sucesso!");
                    entrarModoNovo();
                    carregarClientesAsync();
                } catch (Exception ex) {
                    mostrarErro("Erro ao inativar cliente", ex);
                }
            }
        }.execute();
    }

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {
        if ((modoEdicao || formularioAlterado()) && !confirmarDescarte()) return;
        entrarModoNovo();
    }

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {
        if (confirmarDescarte()) voltarParaPrincipal();
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
        lblCpf = new javax.swing.JLabel();
        txtCpf = new javax.swing.JFormattedTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblTelefone = new javax.swing.JLabel();
        txtTelefone = new javax.swing.JFormattedTextField();
        lblEndereco = new javax.swing.JLabel();
        txtEndereco = new javax.swing.JTextField();
        lblObservacoes = new javax.swing.JLabel();
        scrollObservacoes = new javax.swing.JScrollPane();
        txtObservacoes = new javax.swing.JTextArea();
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
        setTitle("Clientes - Sistema de Loja");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); 
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Cadastro de clientes");

        btnVoltar.setText("← Voltar");
        btnVoltar.addActionListener(this::btnVoltarActionPerformed);

        pnlFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder("Dados do cliente"));

        lblCodigo.setFont(new java.awt.Font("Segoe UI", 0, 12)); 
        lblCodigo.setText("Código");

        txtCodigo.setEditable(false);
        txtCodigo.setFont(new java.awt.Font("Segoe UI", 0, 14)); 

        lblNome.setFont(new java.awt.Font("Segoe UI", 0, 12)); 
        lblNome.setText("Nome completo *");

        txtNome.setFont(new java.awt.Font("Segoe UI", 0, 14)); 

        lblCpf.setFont(new java.awt.Font("Segoe UI", 0, 12)); 
        lblCpf.setText("CPF (opcional)");

        txtCpf.setFont(new java.awt.Font("Segoe UI", 0, 14)); 

        lblEmail.setFont(new java.awt.Font("Segoe UI", 0, 12)); 
        lblEmail.setText("E-mail *");

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); 

        lblTelefone.setFont(new java.awt.Font("Segoe UI", 0, 12)); 
        lblTelefone.setText("Telefone *");

        txtTelefone.setFont(new java.awt.Font("Segoe UI", 0, 14)); 

        lblEndereco.setFont(new java.awt.Font("Segoe UI", 0, 12)); 
        lblEndereco.setText("Endereço");

        txtEndereco.setFont(new java.awt.Font("Segoe UI", 0, 14)); 

        lblObservacoes.setFont(new java.awt.Font("Segoe UI", 0, 12)); 
        lblObservacoes.setText("Observações");

        txtObservacoes.setColumns(20);
        txtObservacoes.setRows(3);
        txtObservacoes.setFont(new java.awt.Font("Segoe UI", 0, 13)); 
        scrollObservacoes.setViewportView(txtObservacoes);

        btnCadastrar.setText("Cadastrar");
        btnCadastrar.addActionListener(this::btnCadastrarActionPerformed);

        btnEditar.setText("Editar");
        btnEditar.addActionListener(this::btnEditarActionPerformed);

        btnExcluir.setText("Inativar");
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
                    .addComponent(txtEmail)
                    .addComponent(txtTelefone)
                    .addComponent(txtEndereco)
                    .addComponent(scrollObservacoes)
                    .addComponent(lblNome)
                    .addComponent(lblEmail)
                    .addComponent(lblTelefone)
                    .addComponent(lblEndereco)
                    .addComponent(lblObservacoes)
                    .addGroup(pnlFormularioLayout.createSequentialGroup()
                        .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCodigo)
                            .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCpf)
                            .addComponent(txtCpf)))
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
                .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCodigo)
                    .addComponent(lblCpf))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCpf, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblNome)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblEmail)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTelefone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblEndereco)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblObservacoes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollObservacoes, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCadastrar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTabela.setBorder(javax.swing.BorderFactory.createTitledBorder("Clientes cadastrados"));

        lblBusca.setFont(new java.awt.Font("Segoe UI", 0, 12)); 
        lblBusca.setText("Buscar por nome, e-mail, telefone ou CPF:");

        txtBusca.setFont(new java.awt.Font("Segoe UI", 0, 13)); 

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Código", "Nome", "E-mail", "Telefone"}
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

    public static void main(String args[]) {
        System.setProperty("file.encoding", "UTF-8");

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
        java.awt.EventQueue.invokeLater(() -> new TelaClientes().setVisible(true));
    }

    
    private javax.swing.JButton btnCadastrar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JLabel lblBusca;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblCpf;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblEndereco;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblObservacoes;
    private javax.swing.JLabel lblTelefone;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFormulario;
    private javax.swing.JPanel pnlTabela;
    private javax.swing.JScrollPane scrollObservacoes;
    private javax.swing.JScrollPane scrollTabela;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtBusca;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JFormattedTextField txtCpf;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtEndereco;
    private javax.swing.JTextArea txtObservacoes;
    private javax.swing.JFormattedTextField txtTelefone;
    private javax.swing.JTextField txtNome;
    

    private javax.swing.table.DefaultTableModel modeloTabela;
}