package com.sistemaloja.view;

import com.sistemaloja.dao.ClienteDAO;
import com.sistemaloja.dao.ProdutoDAO;
import com.sistemaloja.dao.VendaDAO;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class TelaPrincipal extends javax.swing.JFrame {
    private static final Logger logger = Logger.getLogger(TelaPrincipal.class.getName());
    private static final Locale BR = Locale.of("pt", "BR");

    public TelaPrincipal() {
        initComponents();
        posConstrucao();
    }

    private void posConstrucao() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSaida();
            }
        });

        Estilo.aplicarHover(btnClientes, btnProdutos, btnVendas, btnSair);
        btnClientes.setMnemonic(KeyEvent.VK_C);
        btnProdutos.setMnemonic(KeyEvent.VK_P);
        btnVendas.setMnemonic(KeyEvent.VK_V);

        String hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", BR));
        lblData.setText(Character.toUpperCase(hoje.charAt(0)) + hoje.substring(1));

        carregarResumo();
        setLocationRelativeTo(null);
    }

    private void carregarResumo() {
        try {
            lblClientes.setText(String.valueOf(new ClienteDAO().listarAtivos().size()));
            lblProdutos.setText(String.valueOf(new ProdutoDAO().listarAtivos().size()));
            double[] vendas = new VendaDAO().resumo();
            lblVendas.setText(NumberFormat.getCurrencyInstance(BR).format(vendas[1]));
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Não foi possível carregar o resumo", e);
        }
    }

    private void abrirTela(Supplier<JFrame> fabrica, String nomeTela) {
        try {
            JFrame tela = fabrica.get();
            tela.setLocationRelativeTo(null);
            tela.setVisible(true);
            dispose();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Erro ao abrir tela: " + nomeTela, ex);
            JOptionPane.showMessageDialog(this,
                    "Não foi possível abrir a tela de " + nomeTela + ".\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmarSaida() {
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja realmente sair do OctoMax?",
                "Confirmar saída",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (resposta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlCabecalho = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlCorpo = new javax.swing.JPanel();
        pnlResumo = new javax.swing.JPanel();
        pnlIndClientes = new javax.swing.JPanel();
        lblClientesTitulo = new javax.swing.JLabel();
        lblClientes = new javax.swing.JLabel();
        pnlIndProdutos = new javax.swing.JPanel();
        lblProdutosTitulo = new javax.swing.JLabel();
        lblProdutos = new javax.swing.JLabel();
        pnlIndVendas = new javax.swing.JPanel();
        lblVendasTitulo = new javax.swing.JLabel();
        lblVendas = new javax.swing.JLabel();
        pnlModulosBloco = new javax.swing.JPanel();
        lblModulos = new javax.swing.JLabel();
        pnlModulos = new javax.swing.JPanel();
        btnClientes = new javax.swing.JButton();
        btnProdutos = new javax.swing.JButton();
        btnVendas = new javax.swing.JButton();
        pnlRodape = new javax.swing.JPanel();
        lblData = new javax.swing.JLabel();
        btnSair = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("OctoMax - Sistema de Gestão");
        setMinimumSize(new java.awt.Dimension(820, 540));
        getContentPane().setLayout(new java.awt.BorderLayout());

        pnlCabecalho.setBackground(new java.awt.Color(24, 36, 56));
        pnlCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 32, 28, 32));
        pnlCabecalho.setLayout(new java.awt.GridLayout(2, 1, 0, 4));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 34));
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("OctoMax");

        pnlCabecalho.add(lblTitulo);
        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 15));
        lblSubtitulo.setForeground(new java.awt.Color(173, 181, 189));
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitulo.setText("Sistema de gestão da loja");

        pnlCabecalho.add(lblSubtitulo);

        getContentPane().add(pnlCabecalho, java.awt.BorderLayout.NORTH);
        pnlCorpo.setBackground(new java.awt.Color(244, 246, 249));
        pnlCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 32, 8, 32));
        pnlCorpo.setLayout(new java.awt.BorderLayout());
        pnlResumo.setOpaque(false);
        pnlResumo.setLayout(new java.awt.GridLayout(1, 3, 16, 0));
        pnlIndClientes.setBackground(new java.awt.Color(255, 255, 255));
        pnlIndClientes.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)), javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        pnlIndClientes.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblClientesTitulo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblClientesTitulo.setForeground(new java.awt.Color(108, 117, 125));
        lblClientesTitulo.setText("Clientes ativos");

        pnlIndClientes.add(lblClientesTitulo);
        lblClientes.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblClientes.setForeground(new java.awt.Color(33, 37, 41));
        lblClientes.setText("-");

        pnlIndClientes.add(lblClientes);

        pnlResumo.add(pnlIndClientes);
        pnlIndProdutos.setBackground(new java.awt.Color(255, 255, 255));
        pnlIndProdutos.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)), javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        pnlIndProdutos.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblProdutosTitulo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblProdutosTitulo.setForeground(new java.awt.Color(108, 117, 125));
        lblProdutosTitulo.setText("Produtos cadastrados");

        pnlIndProdutos.add(lblProdutosTitulo);
        lblProdutos.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblProdutos.setForeground(new java.awt.Color(33, 37, 41));
        lblProdutos.setText("-");

        pnlIndProdutos.add(lblProdutos);

        pnlResumo.add(pnlIndProdutos);
        pnlIndVendas.setBackground(new java.awt.Color(255, 255, 255));
        pnlIndVendas.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)), javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        pnlIndVendas.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblVendasTitulo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblVendasTitulo.setForeground(new java.awt.Color(108, 117, 125));
        lblVendasTitulo.setText("Total em vendas");

        pnlIndVendas.add(lblVendasTitulo);
        lblVendas.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblVendas.setForeground(new java.awt.Color(33, 37, 41));
        lblVendas.setText("-");

        pnlIndVendas.add(lblVendas);

        pnlResumo.add(pnlIndVendas);

        pnlCorpo.add(pnlResumo, java.awt.BorderLayout.NORTH);
        pnlModulosBloco.setOpaque(false);
        pnlModulosBloco.setLayout(new java.awt.BorderLayout());
        lblModulos.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblModulos.setForeground(new java.awt.Color(108, 117, 125));
        lblModulos.setText("Selecione um módulo");
        lblModulos.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 2, 10, 0));

        pnlModulosBloco.add(lblModulos, java.awt.BorderLayout.NORTH);
        pnlModulos.setOpaque(false);
        pnlModulos.setLayout(new java.awt.GridLayout(1, 3, 16, 0));
        btnClientes.setBackground(new java.awt.Color(13, 110, 253));
        btnClientes.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnClientes.setForeground(new java.awt.Color(255, 255, 255));
        btnClientes.setText("<html><div style='text-align:center'><span style='font-size:16pt'><b>Clientes</b></span><br><br>Cadastrar e consultar clientes</div></html>");
        btnClientes.setToolTipText("Atalho: Alt+C");
        btnClientes.setBorderPainted(false);
        btnClientes.setFocusPainted(false);
        btnClientes.setOpaque(true);
        btnClientes.setPreferredSize(new java.awt.Dimension(220, 150));
        btnClientes.addActionListener(this::btnClientesActionPerformed);

        pnlModulos.add(btnClientes);
        btnProdutos.setBackground(new java.awt.Color(25, 135, 84));
        btnProdutos.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnProdutos.setForeground(new java.awt.Color(255, 255, 255));
        btnProdutos.setText("<html><div style='text-align:center'><span style='font-size:16pt'><b>Produtos</b></span><br><br>Gerenciar produtos e estoque</div></html>");
        btnProdutos.setToolTipText("Atalho: Alt+P");
        btnProdutos.setBorderPainted(false);
        btnProdutos.setFocusPainted(false);
        btnProdutos.setOpaque(true);
        btnProdutos.setPreferredSize(new java.awt.Dimension(220, 150));
        btnProdutos.addActionListener(this::btnProdutosActionPerformed);

        pnlModulos.add(btnProdutos);
        btnVendas.setBackground(new java.awt.Color(214, 92, 0));
        btnVendas.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnVendas.setForeground(new java.awt.Color(255, 255, 255));
        btnVendas.setText("<html><div style='text-align:center'><span style='font-size:16pt'><b>Vendas</b></span><br><br>Registrar vendas e ver histórico</div></html>");
        btnVendas.setToolTipText("Atalho: Alt+V");
        btnVendas.setBorderPainted(false);
        btnVendas.setFocusPainted(false);
        btnVendas.setOpaque(true);
        btnVendas.setPreferredSize(new java.awt.Dimension(220, 150));
        btnVendas.addActionListener(this::btnVendasActionPerformed);

        pnlModulos.add(btnVendas);

        pnlModulosBloco.add(pnlModulos, java.awt.BorderLayout.CENTER);

        pnlCorpo.add(pnlModulosBloco, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlCorpo, java.awt.BorderLayout.CENTER);
        pnlRodape.setBackground(new java.awt.Color(244, 246, 249));
        pnlRodape.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 32, 20, 32));
        pnlRodape.setLayout(new java.awt.BorderLayout());
        lblData.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblData.setForeground(new java.awt.Color(108, 117, 125));

        pnlRodape.add(lblData, java.awt.BorderLayout.WEST);
        btnSair.setBackground(new java.awt.Color(108, 117, 125));
        btnSair.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnSair.setForeground(new java.awt.Color(255, 255, 255));
        btnSair.setText("Sair");
        btnSair.setBorderPainted(false);
        btnSair.setFocusPainted(false);
        btnSair.setOpaque(true);
        btnSair.setPreferredSize(new java.awt.Dimension(100, 34));
        btnSair.addActionListener(this::btnSairActionPerformed);

        pnlRodape.add(btnSair, java.awt.BorderLayout.EAST);

        getContentPane().add(pnlRodape, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClientesActionPerformed
        abrirTela(TelaClientes::new, "Clientes");
    }//GEN-LAST:event_btnClientesActionPerformed

    private void btnProdutosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProdutosActionPerformed
        abrirTela(TelaProdutos::new, "Produtos");
    }//GEN-LAST:event_btnProdutosActionPerformed

    private void btnVendasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVendasActionPerformed
        abrirTela(TelaVendas::new, "Vendas");
    }//GEN-LAST:event_btnVendasActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        confirmarSaida();
    }//GEN-LAST:event_btnSairActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnProdutos;
    private javax.swing.JButton btnSair;
    private javax.swing.JButton btnVendas;
    private javax.swing.JLabel lblClientes;
    private javax.swing.JLabel lblClientesTitulo;
    private javax.swing.JLabel lblData;
    private javax.swing.JLabel lblModulos;
    private javax.swing.JLabel lblProdutos;
    private javax.swing.JLabel lblProdutosTitulo;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblVendas;
    private javax.swing.JLabel lblVendasTitulo;
    private javax.swing.JPanel pnlCabecalho;
    private javax.swing.JPanel pnlCorpo;
    private javax.swing.JPanel pnlIndClientes;
    private javax.swing.JPanel pnlIndProdutos;
    private javax.swing.JPanel pnlIndVendas;
    private javax.swing.JPanel pnlModulos;
    private javax.swing.JPanel pnlModulosBloco;
    private javax.swing.JPanel pnlResumo;
    private javax.swing.JPanel pnlRodape;
    // End of variables declaration//GEN-END:variables
}
