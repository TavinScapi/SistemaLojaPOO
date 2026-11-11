package com.sistemaloja.view;

import com.sistemaloja.dao.ClienteDAO;
import com.sistemaloja.dao.ProdutoDAO;
import com.sistemaloja.dao.VendaDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TelaPrincipal extends JFrame {
    private static final Logger logger = Logger.getLogger(TelaPrincipal.class.getName());

    public static final String NOME_LOJA = "OctoMax";

    private static final Locale BR = Locale.of("pt", "BR");
    private static final Color FUNDO = new Color(244, 246, 249);
    private static final Color AZUL_ESCURO = new Color(24, 36, 56);
    private static final Color TEXTO = new Color(33, 37, 41);
    private static final Color TEXTO_SUAVE = new Color(108, 117, 125);
    private static final Color BORDA = new Color(222, 226, 230);
    private static final String FONTE = "Segoe UI";

    private final JLabel lblClientes = new JLabel("-");
    private final JLabel lblProdutos = new JLabel("-");
    private final JLabel lblVendas = new JLabel("-");

    public TelaPrincipal() {
        setTitle(NOME_LOJA + " - Sistema de Gestão");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSaida();
            }
        });

        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setBackground(FUNDO);
        conteudo.add(criarCabecalho(), BorderLayout.NORTH);
        conteudo.add(criarCentro(), BorderLayout.CENTER);
        conteudo.add(criarRodape(), BorderLayout.SOUTH);
        setContentPane(conteudo);

        carregarResumo();

        setMinimumSize(new Dimension(820, 540));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel criarCabecalho() {
        JPanel painel = new JPanel(new GridLayout(2, 1, 0, 4));
        painel.setBackground(AZUL_ESCURO);
        painel.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel titulo = new JLabel(NOME_LOJA, SwingConstants.CENTER);
        titulo.setFont(new Font(FONTE, Font.BOLD, 34));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Sistema de gestão da loja", SwingConstants.CENTER);
        subtitulo.setFont(new Font(FONTE, Font.PLAIN, 15));
        subtitulo.setForeground(new Color(173, 181, 189));

        painel.add(titulo);
        painel.add(subtitulo);
        return painel;
    }

    private JPanel criarCentro() {
        JPanel resumo = new JPanel(new GridLayout(1, 3, 16, 0));
        resumo.setOpaque(false);
        resumo.add(criarIndicador("Clientes ativos", lblClientes));
        resumo.add(criarIndicador("Produtos cadastrados", lblProdutos));
        resumo.add(criarIndicador("Total em vendas", lblVendas));

        JPanel modulos = new JPanel(new GridLayout(1, 3, 16, 0));
        modulos.setOpaque(false);
        modulos.add(criarBotaoModulo("Clientes", "Cadastrar e consultar clientes",
                new Color(13, 110, 253), KeyEvent.VK_C, () -> abrirTela(TelaClientes::new, "Clientes")));
        modulos.add(criarBotaoModulo("Produtos", "Gerenciar produtos e estoque",
                new Color(25, 135, 84), KeyEvent.VK_P, () -> abrirTela(TelaProdutos::new, "Produtos")));
        modulos.add(criarBotaoModulo("Vendas", "Registrar vendas e ver histórico",
                new Color(214, 92, 0), KeyEvent.VK_V, () -> abrirTela(TelaVendas::new, "Vendas")));

        JLabel lblModulos = new JLabel("Selecione um módulo");
        lblModulos.setFont(new Font(FONTE, Font.BOLD, 14));
        lblModulos.setForeground(TEXTO_SUAVE);
        lblModulos.setBorder(BorderFactory.createEmptyBorder(24, 2, 10, 0));

        JPanel blocoModulos = new JPanel(new BorderLayout());
        blocoModulos.setOpaque(false);
        blocoModulos.add(lblModulos, BorderLayout.NORTH);
        blocoModulos.add(modulos, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(24, 32, 8, 32));
        centro.add(resumo, BorderLayout.NORTH);
        centro.add(blocoModulos, BorderLayout.CENTER);
        return centro;
    }

    private JPanel criarIndicador(String titulo, JLabel valor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font(FONTE, Font.PLAIN, 13));
        lblTitulo.setForeground(TEXTO_SUAVE);

        valor.setFont(new Font(FONTE, Font.BOLD, 22));
        valor.setForeground(TEXTO);

        card.add(lblTitulo);
        card.add(valor);
        return card;
    }

    private JButton criarBotaoModulo(String titulo, String descricao, Color cor,
                                     int atalho, Runnable acao) {
        JButton btn = new JButton("<html><div style='text-align:center'>"
                + "<span style='font-size:16pt'><b>" + titulo + "</b></span><br><br>"
                + descricao + "</div></html>");
        btn.setFont(new Font(FONTE, Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(cor);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 150));
        btn.setMnemonic(atalho);
        btn.setToolTipText("Atalho: Alt+" + KeyEvent.getKeyText(atalho));
        btn.addActionListener(e -> acao.run());
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(cor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(cor);
            }
        });
        return btn;
    }

    private JPanel criarRodape() {
        String hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", BR));
        JLabel lblData = new JLabel(Character.toUpperCase(hoje.charAt(0)) + hoje.substring(1));
        lblData.setFont(new Font(FONTE, Font.PLAIN, 12));
        lblData.setForeground(TEXTO_SUAVE);

        JButton btnSair = new JButton("Sair");
        btnSair.setFont(new Font(FONTE, Font.PLAIN, 13));
        btnSair.addActionListener(e -> confirmarSaida());

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createEmptyBorder(12, 32, 20, 32));
        rodape.add(lblData, BorderLayout.WEST);
        rodape.add(btnSair, BorderLayout.EAST);
        return rodape;
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
                "Deseja realmente sair do " + NOME_LOJA + "?",
                "Confirmar saída",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (resposta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
