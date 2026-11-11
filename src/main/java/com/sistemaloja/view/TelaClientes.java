package com.sistemaloja.view;

import com.sistemaloja.model.Cliente;
import com.sistemaloja.service.ClienteService;
import com.sistemaloja.service.ClienteService.RegraNegocioException;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;
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

    private String snapshot = "";

    public TelaClientes() {
        initComponents();
        posConstrucao();
    }

    private void posConstrucao() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1050, 700));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmarDescarte()) voltarParaPrincipal();
            }
        });

        Estilo.aplicarHover(btnVoltar, btnCadastrar, btnEditar, btnExcluir, btnLimpar);
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
        Estilo.estilizarTabela(tabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
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

        if (!confirmarDescarte()) return;

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

    private String estadoFormulario() {
        return String.join("|", txtNome.getText(), txtCpf.getText().replaceAll("\\D", ""),
                txtEmail.getText(), txtTelefone.getText().replaceAll("\\D", ""),
                txtEndereco.getText(), txtObservacoes.getText());
    }

    private void atualizarSnapshot() {
        snapshot = estadoFormulario();
    }

    private boolean formularioAlterado() {
        return !estadoFormulario().equals(snapshot);
    }

    private boolean confirmarDescarte() {
        boolean visualizando = idClienteSelecionado != -1 && !modoEdicao;
        if (visualizando || !formularioAlterado()) return true;
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
        Throwable causa = ex instanceof ExecutionException && ex.getCause() != null
                ? ex.getCause() : ex;
        if (causa instanceof RegraNegocioException) {
            JOptionPane.showMessageDialog(this, causa.getMessage(),
                    "Verifique os dados", JOptionPane.WARNING_MESSAGE);
            return;
        }
        logger.log(Level.SEVERE, contexto, causa);
        JOptionPane.showMessageDialog(this, contexto + ":\n" + causa.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void voltarParaPrincipal() {
        new TelaPrincipal().setVisible(true);
        dispose();
    }

    private void btnCadastrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCadastrarActionPerformed
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
    }//GEN-LAST:event_btnCadastrarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        if (idClienteSelecionado == -1) return;
        entrarModoEdicao();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
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
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        if ((modoEdicao || formularioAlterado()) && !confirmarDescarte()) return;
        entrarModoNovo();
    }//GEN-LAST:event_btnLimparActionPerformed

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        if (confirmarDescarte()) voltarParaPrincipal();
    }//GEN-LAST:event_btnVoltarActionPerformed

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
        lblCpf = new javax.swing.JLabel();
        txtCpf = new javax.swing.JFormattedTextField();
        lblTelefone = new javax.swing.JLabel();
        txtTelefone = new javax.swing.JFormattedTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblEndereco = new javax.swing.JLabel();
        txtEndereco = new javax.swing.JTextField();
        lblObservacoes = new javax.swing.JLabel();
        scrollObservacoes = new javax.swing.JScrollPane();
        txtObservacoes = new javax.swing.JTextArea();
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
        setTitle("Clientes - OctoMax");
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
        lblTitulo.setText("Cadastro de clientes");

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
        pnlFormulario.setPreferredSize(new java.awt.Dimension(420, 560));
        pnlFormulario.setLayout(new java.awt.GridBagLayout());
        lblFormTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15));
        lblFormTitulo.setForeground(new java.awt.Color(33, 37, 41));
        lblFormTitulo.setText("Dados do cliente");

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
        lblNome.setText("Nome completo *");

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
        lblCpf.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblCpf.setForeground(new java.awt.Color(108, 117, 125));
        lblCpf.setText("CPF (opcional)");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 12);
        pnlFormulario.add(lblCpf, gridBagConstraints);
        txtCpf.setFont(new java.awt.Font("Segoe UI", 0, 14));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 12);
        pnlFormulario.add(txtCpf, gridBagConstraints);
        lblTelefone.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblTelefone.setForeground(new java.awt.Color(108, 117, 125));
        lblTelefone.setText("Telefone *");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        pnlFormulario.add(lblTelefone, gridBagConstraints);
        txtTelefone.setFont(new java.awt.Font("Segoe UI", 0, 14));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        pnlFormulario.add(txtTelefone, gridBagConstraints);
        lblEmail.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblEmail.setForeground(new java.awt.Color(108, 117, 125));
        lblEmail.setText("E-mail *");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        pnlFormulario.add(lblEmail, gridBagConstraints);
        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        pnlFormulario.add(txtEmail, gridBagConstraints);
        lblEndereco.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblEndereco.setForeground(new java.awt.Color(108, 117, 125));
        lblEndereco.setText("Endereço");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        pnlFormulario.add(lblEndereco, gridBagConstraints);
        txtEndereco.setFont(new java.awt.Font("Segoe UI", 0, 14));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        pnlFormulario.add(txtEndereco, gridBagConstraints);
        lblObservacoes.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblObservacoes.setForeground(new java.awt.Color(108, 117, 125));
        lblObservacoes.setText("Observações");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        pnlFormulario.add(lblObservacoes, gridBagConstraints);
        scrollObservacoes.setMinimumSize(new java.awt.Dimension(100, 58));
        scrollObservacoes.setPreferredSize(new java.awt.Dimension(200, 58));
        txtObservacoes.setColumns(20);
        txtObservacoes.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setRows(3);
        txtObservacoes.setWrapStyleWord(true);

        scrollObservacoes.setViewportView(txtObservacoes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        pnlFormulario.add(scrollObservacoes, gridBagConstraints);
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
        btnExcluir.setText("Inativar");
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
        gridBagConstraints.gridy = 13;
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
        lblTabelaTitulo.setText("Clientes cadastrados");

        pnlTabelaTopo.add(lblTabelaTitulo, java.awt.BorderLayout.NORTH);
        lblBusca.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblBusca.setForeground(new java.awt.Color(108, 117, 125));
        lblBusca.setText("Buscar por nome, e-mail, telefone ou CPF:");

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JLabel lblFormTitulo;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblObservacoes;
    private javax.swing.JLabel lblTabelaTitulo;
    private javax.swing.JLabel lblTelefone;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlAcoesCabecalho;
    private javax.swing.JPanel pnlBotoes;
    private javax.swing.JPanel pnlCabecalho;
    private javax.swing.JPanel pnlCorpo;
    private javax.swing.JPanel pnlFormulario;
    private javax.swing.JPanel pnlTabela;
    private javax.swing.JPanel pnlTabelaTopo;
    private javax.swing.JPanel pnlTitulos;
    private javax.swing.JScrollPane scrollObservacoes;
    private javax.swing.JScrollPane scrollTabela;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtBusca;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JFormattedTextField txtCpf;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtEndereco;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextArea txtObservacoes;
    private javax.swing.JFormattedTextField txtTelefone;
    // End of variables declaration//GEN-END:variables

    private javax.swing.table.DefaultTableModel modeloTabela;
}
