package com.sistemaloja.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

final class Estilo {
    private static final Color TEXTO = new Color(33, 37, 41);
    private static final Color BORDA = new Color(222, 226, 230);
    private static final Color CABECALHO_TABELA = new Color(248, 249, 250);
    private static final Color SELECAO = new Color(207, 226, 255);

    private Estilo() {
    }

    static void aplicarHover(JButton... botoes) {
        for (JButton btn : botoes) {
            Color base = btn.getBackground();
            Color desativado = clarear(base);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBackground(btn.isEnabled() ? base : desativado);
            btn.addPropertyChangeListener("enabled",
                    e -> btn.setBackground(btn.isEnabled() ? base : desativado));
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (btn.isEnabled()) btn.setBackground(base.darker());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(btn.isEnabled() ? base : desativado);
                }
            });
        }
    }

    static void estilizarTabela(JTable tabela) {
        tabela.setRowHeight(28);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabela.setForeground(TEXTO);
        tabela.setGridColor(BORDA);
        tabela.setShowVerticalLines(false);
        tabela.setSelectionBackground(SELECAO);
        tabela.setSelectionForeground(TEXTO);
        tabela.setFillsViewportHeight(true);

        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cabecalho.setBackground(CABECALHO_TABELA);
        cabecalho.setForeground(TEXTO);
        cabecalho.setReorderingAllowed(false);

        if (tabela.getColumnCount() > 0 && "Código".equals(tabela.getColumnName(0))) {
            TableColumn codigo = tabela.getColumnModel().getColumn(0);
            codigo.setMaxWidth(70);
            codigo.setPreferredWidth(60);
            DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
            centro.setHorizontalAlignment(SwingConstants.CENTER);
            codigo.setCellRenderer(centro);
        }
    }

    static void alinharDireita(JTable tabela, int... colunas) {
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
        direita.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : colunas) {
            tabela.getColumnModel().getColumn(c).setCellRenderer(direita);
        }
    }

    private static Color clarear(Color c) {
        return new Color((c.getRed() + 2 * 255) / 3, (c.getGreen() + 2 * 255) / 3, (c.getBlue() + 2 * 255) / 3);
    }
}
