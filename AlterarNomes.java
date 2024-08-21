import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class AlterarNomes {

    public static void main(String[] args) {
        // Define o tema moderno
        FlatDarculaLaf.setup();

        JFrame frame = new JFrame("LEAN");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);

        // Cria um JTabbedPane para as abas
        JTabbedPane tabbedPane = new JTabbedPane();

        // Cria a primeira aba (Substituição simples)
        JPanel panelSubstituicaoSimples = new JPanel();
        panelSubstituicaoSimples.setLayout(new BoxLayout(panelSubstituicaoSimples, BoxLayout.Y_AXIS));
        panelSubstituicaoSimples.setBorder(new EmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab("Substituição Simples", panelSubstituicaoSimples);

        // Cria a segunda aba (Renomear com ordenação)
        JPanel panelRenomeacao = new JPanel();
        panelRenomeacao.setLayout(new BoxLayout(panelRenomeacao, BoxLayout.Y_AXIS));
        panelRenomeacao.setBorder(new EmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab("Renomear com ordenação", panelRenomeacao);

        // Adiciona o JTabbedPane ao frame
        frame.add(tabbedPane);
        frame.setVisible(true);

        // Configura a primeira aba (Substituição Simples)
        configurarAbaSubstituicaoSimples(panelSubstituicaoSimples);

        // Configura a segunda aba (Renomear com Ordenação)
        configurarAbaRenomeacao(panelRenomeacao, tabbedPane);
    }

    private static void configurarAbaSubstituicaoSimples(JPanel panel) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Pasta
        JLabel labelPasta = new JLabel("Pasta:");
        labelPasta.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelPasta);

        JTextField textPasta = new JTextField(20);
        textPasta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        textPasta.setBorder(BorderFactory.createCompoundBorder(
                textPasta.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textPasta);

        JButton buttonSelecionar = new JButton("Selecionar");
        buttonSelecionar.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonSelecionar.setMaximumSize(new Dimension(150, 30));
        buttonSelecionar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(buttonSelecionar);

        // Nome original
        JLabel labelOriginal = new JLabel("Nome original:");
        labelOriginal.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(labelOriginal);

        JTextField textOriginal = new JTextField(20);
        textOriginal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        textOriginal.setBorder(BorderFactory.createCompoundBorder(
                textOriginal.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textOriginal);

        // Alterar para
        JLabel labelNova = new JLabel("Alterar para:");
        labelNova.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(labelNova);

        JTextField textNova = new JTextField(20);
        textNova.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        textNova.setBorder(BorderFactory.createCompoundBorder(
                textNova.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textNova);

        JButton buttonRenomear = new JButton("Renomear");
        buttonRenomear.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonRenomear.setMaximumSize(new Dimension(150, 30));
        buttonRenomear.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(buttonRenomear);

        // Ação do botão Selecionar
        buttonSelecionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    textPasta.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        // Ação do botão Renomear
        buttonRenomear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pasta = textPasta.getText();
                String palavraNova = textNova.getText();
                String palavraAntiga = textOriginal.getText();

                File directory = new File(pasta);
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            String nomeArquivo = file.getName();
                            String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf('.'));
                            String nomeSemExtensao = nomeArquivo.substring(0, nomeArquivo.lastIndexOf('.'));
                            String novoNome = nomeSemExtensao.replace(palavraAntiga, palavraNova) + extensao;

                            File novoArquivo = new File(directory, novoNome);
                            if (!file.renameTo(novoArquivo)) {
                                JOptionPane.showMessageDialog(panel, "Erro ao renomear: " + nomeArquivo);
                            }
                        }
                    }
                    JOptionPane.showMessageDialog(panel, "Renomeação concluída!");
                } else {
                    JOptionPane.showMessageDialog(panel, "Pasta não encontrada ou vazia.");
                }
            }
        });
    }

    private static void configurarAbaRenomeacao(JPanel panel, JTabbedPane tabbedPane) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Pasta
        JLabel labelPasta = new JLabel("Pasta:");
        labelPasta.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelPasta);

        JTextField textPasta = new JTextField(20);
        textPasta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        textPasta.setBorder(BorderFactory.createCompoundBorder(
                textPasta.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textPasta);

        JButton buttonSelecionar = new JButton("Selecionar");
        buttonSelecionar.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonSelecionar.setMaximumSize(new Dimension(150, 30));
        buttonSelecionar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(buttonSelecionar);

        // Ordenar seleção
        JCheckBox checkOrdenar = new JCheckBox("Ordenar seleção");
        checkOrdenar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(checkOrdenar);

        // Nome para alterar
        JLabel labelNova = new JLabel("Alterar para:");
        labelNova.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(labelNova);

        JTextField textNova = new JTextField(20);
        textNova.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        textNova.setBorder(BorderFactory.createCompoundBorder(
                textNova.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textNova);

        // Lista de Arquivos
        JLabel labelArquivos = new JLabel("Arquivos encontrados:");
        labelArquivos.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelArquivos);

        JPanel painelArquivos = new JPanel();
        painelArquivos.setLayout(new BoxLayout(painelArquivos, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(painelArquivos);
        scrollPane.setPreferredSize(new Dimension(250, 300));
        panel.add(scrollPane);

        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT)); // Alinhar à direita
        panel.add(panelButtons);

        JButton buttonSelecionarTodos = new JButton("Selecionar todos");
        panelButtons.add(buttonSelecionarTodos);

        JButton buttonRetirarSelecao = new JButton("Retirar seleção");
        panelButtons.add(buttonRetirarSelecao);

        JButton buttonRenomear = new JButton("Renomear");
        buttonRenomear.setBackground(tabbedPane.getBackgroundAt(tabbedPane.getSelectedIndex())); // Cor da aba selecionada
        buttonRenomear.setForeground(Color.WHITE);
        panelButtons.add(buttonRenomear);

        JButton buttonRefresh = new JButton("Refresh");
        panelButtons.add(buttonRefresh);

        // Ação do botão Selecionar
        buttonSelecionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    textPasta.setText(selectedFile.getAbsolutePath());
                    atualizarListaArquivos(painelArquivos, selectedFile, checkOrdenar.isSelected());
                }
            }
        });

        // Ação do botão Refresh
        buttonRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File pastaSelecionada = new File(textPasta.getText());
                atualizarListaArquivos(painelArquivos, pastaSelecionada, checkOrdenar.isSelected());
            }
        });

        // Ação do botão Renomear
        buttonRenomear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pasta = textPasta.getText();
                String palavraNova = textNova.getText();

                File directory = new File(pasta);
                File[] files = directory.listFiles();
                if (files != null) {
                    int contador = 1;
                    for (Component comp : painelArquivos.getComponents()) {
                        if (comp instanceof JCheckBox) {
                            JCheckBox checkBox = (JCheckBox) comp;
                            if (checkBox.isSelected()) {
                                File file = new File(pasta + File.separator + checkBox.getText());
                                String extensao = file.getName().substring(file.getName().lastIndexOf('.'));
                                String novoNome = palavraNova + "_" + String.format("%03d", contador) + extensao; // Formato 3 dígitos
                                File novoArquivo = new File(directory, novoNome);
                                if (!file.renameTo(novoArquivo)) {
                                    JOptionPane.showMessageDialog(panel, "Erro ao renomear: " + file.getName());
                                }
                                contador++;
                            }
                        }
                    }
                    JOptionPane.showMessageDialog(panel, "Renomeação concluída!");
                    atualizarListaArquivos(painelArquivos, directory, checkOrdenar.isSelected());
                } else {
                    JOptionPane.showMessageDialog(panel, "Pasta não encontrada ou vazia.");
                }
            }
        });

        // Ação do botão Selecionar Todos
        buttonSelecionarTodos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Component comp : painelArquivos.getComponents()) {
                    if (comp instanceof JCheckBox) {
                        JCheckBox checkBox = (JCheckBox) comp;
                        checkBox.setSelected(true);
                    }
                }
            }
        });

        // Ação do botão Retirar Seleção
        buttonRetirarSelecao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Component comp : painelArquivos.getComponents()) {
                    if (comp instanceof JCheckBox) {
                        JCheckBox checkBox = (JCheckBox) comp;
                        checkBox.setSelected(false);
                    }
                }
            }
        });
    }

    private static void atualizarListaArquivos(JPanel painelArquivos, File directory, boolean ordenar) {
        painelArquivos.removeAll();

        File[] files = directory.listFiles();
        if (files != null) {
            if (ordenar) {
                Arrays.sort(files, Comparator.comparing(File::getName));
            }
            for (File file : files) {
                if (file.isFile()) {
                    JCheckBox checkBox = new JCheckBox(file.getName());
                    painelArquivos.add(checkBox);
                }
            }
        }

        painelArquivos.revalidate();
        painelArquivos.repaint();
    }
}
