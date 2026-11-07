



package com.sistemaloja.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;













public class TelaPrincipal extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(TelaPrincipal.class.getName());

    


    public TelaPrincipal() {
        initComponents();      
        posConstrucao();       
    }

    





    private void posConstrucao() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 400));

        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSaida();
            }
        });

        
        aplicarHover(btnClientes);
        aplicarHover(btnProdutos);
        aplicarHover(btnVendas);
    }

    



    private void aplicarHover(JButton btn) {
        Color base = btn.getBackground();
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(base.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(base);
            }
        });
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
                "Deseja realmente sair do sistema?",
                "Confirmar saída",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (resposta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    




    @SuppressWarnings("unchecked")
    
    private void initComponents() {

        painelCabecalho = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelBotoes = new javax.swing.JPanel();
        btnClientes = new javax.swing.JButton();
        btnProdutos = new javax.swing.JButton();
        btnVendas = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema de Gerenciamento de Loja");
        setMinimumSize(new java.awt.Dimension(600, 400));

        painelCabecalho.setBackground(new java.awt.Color(248, 249, 250));
        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 20, 20, 20));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); 
        lblTitulo.setForeground(new java.awt.Color(33, 37, 41));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Sistema de Gerenciamento de Loja");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); 
        lblSubtitulo.setForeground(new java.awt.Color(108, 117, 125));
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitulo.setText("Selecione um módulo para começar");

        javax.swing.GroupLayout painelCabecalhoLayout = new javax.swing.GroupLayout(painelCabecalho);
        painelCabecalho.setLayout(painelCabecalhoLayout);
        painelCabecalhoLayout.setHorizontalGroup(
            painelCabecalhoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCabecalhoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelCabecalhoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        painelCabecalhoLayout.setVerticalGroup(
            painelCabecalhoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCabecalhoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitulo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelBotoes.setBackground(new java.awt.Color(248, 249, 250));
        painelBotoes.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 40, 40));
        painelBotoes.setLayout(new java.awt.GridLayout(1, 3, 20, 20));

        btnClientes.setBackground(new java.awt.Color(13, 110, 253));
        btnClientes.setFont(new java.awt.Font("Segoe UI", 1, 15)); 
        btnClientes.setForeground(new java.awt.Color(255, 255, 255));
        btnClientes.setText("👥  Clientes");
        btnClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClientes.setFocusPainted(false);
        btnClientes.setBorderPainted(false);
        btnClientes.setOpaque(true);
        btnClientes.addActionListener(this::btnClientesActionPerformed);
        painelBotoes.add(btnClientes);

        btnProdutos.setBackground(new java.awt.Color(25, 135, 84));
        btnProdutos.setFont(new java.awt.Font("Segoe UI", 1, 15)); 
        btnProdutos.setForeground(new java.awt.Color(255, 255, 255));
        btnProdutos.setText("📦  Produtos");
        btnProdutos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnProdutos.setFocusPainted(false);
        btnProdutos.setBorderPainted(false);
        btnProdutos.setOpaque(true);
        btnProdutos.addActionListener(this::btnProdutosActionPerformed);
        painelBotoes.add(btnProdutos);

        btnVendas.setBackground(new java.awt.Color(255, 193, 7));
        btnVendas.setFont(new java.awt.Font("Segoe UI", 1, 15)); 
        btnVendas.setForeground(new java.awt.Color(255, 255, 255));
        btnVendas.setText("💰  Vendas");
        btnVendas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVendas.setFocusPainted(false);
        btnVendas.setBorderPainted(false);
        btnVendas.setOpaque(true);
        btnVendas.addActionListener(this::btnVendasActionPerformed);
        painelBotoes.add(btnVendas);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelCabecalho, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(painelBotoes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(painelCabecalho, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(painelBotoes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

    private void btnClientesActionPerformed(java.awt.event.ActionEvent evt) {
        abrirTela(TelaClientes::new, "Clientes");
    }

    private void btnProdutosActionPerformed(java.awt.event.ActionEvent evt) {
        abrirTela(TelaProdutos::new, "Produtos");
    }

    private void btnVendasActionPerformed(java.awt.event.ActionEvent evt) {
        abrirTela(TelaVendas::new, "Vendas");
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
        java.awt.EventQueue.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }

    
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnProdutos;
    private javax.swing.JButton btnVendas;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelCabecalho;
    
}