package com.sistemaloja;

import com.sistemaloja.view.TelaPrincipal;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaPrincipal tela = new TelaPrincipal();
            tela.setLocationRelativeTo(null);
            tela.setVisible(true);
        });
    }
}
