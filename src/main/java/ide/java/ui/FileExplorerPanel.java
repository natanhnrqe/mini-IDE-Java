package ide.java.ui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;

public class FileExplorerPanel extends JPanel {

    private JTree jTree;
    private DefaultTreeModel treeModel;

    public FileExplorerPanel(File rootDirectory) {
        setLayout(new BorderLayout());

        DefaultMutableTreeNode rootNode = createNode(rootDirectory);

        treeModel = new DefaultTreeModel(rootNode);
        jTree = new JTree(treeModel);

        JScrollPane scrollPane = new JScrollPane(jTree);
        scrollPane.setBackground(Color.DARK_GRAY);

        add(scrollPane, BorderLayout.CENTER);
    }

    private DefaultMutableTreeNode createNode(File file){
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);

        if (file.isDirectory()){
            File[] files = file.listFiles();

            if (files != null){
                for (File child : files){
                    node.add(createNode(child));
                }
            }
        }
        return node;
    }

    public JTree getjTree() {
        return jTree;
    }
}
