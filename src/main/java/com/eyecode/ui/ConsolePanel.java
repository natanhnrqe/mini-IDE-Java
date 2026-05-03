package com.eyecode.ui;

import javax.swing.*;
import java.awt.*;

public class ConsolePanel extends JPanel {

    private JTextArea consoleArea;

    public ConsolePanel() {

        setLayout(new BorderLayout());

        consoleArea = new JTextArea();
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        consoleArea.setEditable(false);

        consoleArea.setBackground(new Color(43, 43, 43));
        consoleArea.setForeground(new Color(169, 183, 198));
        consoleArea.setCaretColor(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(consoleArea);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void print(String text){
        consoleArea.append(text + "\n");
    }

}
