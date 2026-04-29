package ide.java.ui.editor;

import javax.swing.*;
import java.awt.*;

public class SuggestionRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus
        );

        Suggestion s = (Suggestion) value;

        String icon = switch (s.getType()) {
            case "METHOD" -> "ƒ";
            case "KEYWORD" -> "K";
            case "VARIABLE" -> "V";
            case "CLASS" -> "C";
            default -> "•";
        };

        label.setText(String.format("%-3s %s", icon, s.getText()));

        label.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        return label;

        }

    }

