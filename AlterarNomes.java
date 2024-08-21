import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class AlterarNomes {

    private static JPanel panelRenomeacao;
    private static JTextArea textAreaArquivos;

    public static void main(String[] args) {
        // Define o tema moderno
        FlatDarculaLaf.setup();

        JFrame frame = new JFrame("LEAN");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Cria um JTabbedPane para as abas
        JTabbedPane tabbedPane = new JTabbedPane();

        // Cria a primeira aba (Substituição simples)
        JPanel panelSubstituicaoSimples = new JPanel();
        panelSubstituicaoSimples.setLayout(new GridBagLayout());
        panelSubstituicaoSimples.setBorder(new EmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab("Substituição Simples", panelSubstituicaoSimples);

        // Cria a segunda aba (Renomear com ordenação)
        panelRenomeacao = new JPanel();
        panelRenomeacao.setLayout(new GridBagLayout());
        panelRenomeacao.setBorder(new EmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab("Renomear e ordenar", panelRenomeacao);

        // Adiciona o JTabbedPane ao frame
        frame.add(tabbedPane);
        frame.setVisible(true);

        // Configura a primeira aba (Substituição Simples)
        configurarAbaSubstituicaoSimples(panelSubstituicaoSimples);

        // Configura a segunda aba (Renomear com Ordenação)
        configurarAbaRenomeacao(panelRenomeacao);
    }

    private static void configurarAbaSubstituicaoSimples(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Pasta
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel labelPasta = new JLabel("Pasta:");
        panel.add(labelPasta, gbc);

        gbc.gridx = 1;
        JTextField textPasta = new JTextField(20);
        textPasta.setMaximumSize(new Dimension(200, 30));
        textPasta.setBorder(BorderFactory.createCompoundBorder(
                textPasta.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textPasta, gbc);

        gbc.gridx = 2;
        JButton buttonSelecionar = new JButton("Selecionar");
        buttonSelecionar.setBackground(new Color(0xEB5E28));
        buttonSelecionar.setForeground(Color.WHITE);
        buttonSelecionar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonSelecionar.setPreferredSize(new Dimension(50, 30));
        panel.add(buttonSelecionar, gbc);

        // Nome original
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel labelOriginal = new JLabel("Nome original:");
        panel.add(labelOriginal, gbc);

        gbc.gridx = 1;
        JTextField textOriginal = new JTextField(20);
        textOriginal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        textOriginal.setBorder(BorderFactory.createCompoundBorder(
                textOriginal.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textOriginal, gbc);

        // Alterar para
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel labelNova = new JLabel("Alterar para:");
        panel.add(labelNova, gbc);

        gbc.gridx = 1;
        JTextField textNova = new JTextField(20);
        textNova.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        textNova.setBorder(BorderFactory.createCompoundBorder(
                textNova.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textNova, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JButton buttonRenomear = new JButton("Renomear");
        buttonRenomear.setBackground(new Color(0xEB5E28));
        buttonRenomear.setForeground(Color.WHITE);
        buttonRenomear.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(buttonRenomear, gbc);

        // Adicionar área de visualização de arquivos
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        textAreaArquivos = new JTextArea(5, 40);
        textAreaArquivos.setEditable(false);
        JScrollPane scrollPaneArquivos = new JScrollPane(textAreaArquivos);
        panel.add(scrollPaneArquivos, gbc);

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
                    atualizarVisualizacaoArquivos(selectedFile);
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
                            String novoNome = nomeArquivo.replace(palavraAntiga, palavraNova);

                            File novoArquivo = new File(directory, novoNome);
                            if (!file.renameTo(novoArquivo)) {
                                JOptionPane.showMessageDialog(panel, "Erro ao renomear: " + nomeArquivo);
                            }
                        }
                    }
                    JOptionPane.showMessageDialog(panel, "Renomeação concluída!");
                    atualizarVisualizacaoArquivos(directory);
                } else {
                    JOptionPane.showMessageDialog(panel, "Pasta não encontrada ou vazia.");
                }
            }
        });
    }

    private static void configurarAbaRenomeacao(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Pasta
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel labelPasta = new JLabel("Pasta:");
        panel.add(labelPasta, gbc);

        gbc.gridx = 1;
        JTextField textPasta = new JTextField(20);
        textPasta.setMaximumSize(new Dimension(200, 30));
        textPasta.setBorder(BorderFactory.createCompoundBorder(
                textPasta.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textPasta, gbc);

        gbc.gridx = 2;
        JButton buttonSelecionar = new JButton("Selecionar");
        buttonSelecionar.setBackground(new Color(0xEB5E28));
        buttonSelecionar.setForeground(Color.WHITE);
        buttonSelecionar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(buttonSelecionar, gbc);

        // Ordenar seleção e definir intervalo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel panelOrdenar = new JPanel();
        panelOrdenar.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(panelOrdenar, gbc);

        JCheckBox checkOrdenar = new JCheckBox("Ordenar seleção");
        panelOrdenar.add(checkOrdenar);

        JCheckBox checkDefinirIntervalo = new JCheckBox("Definir intervalo");
        panelOrdenar.add(checkDefinirIntervalo);

        JLabel labelDefinirIntervalo = new JLabel("Definir intervalo:");
        panelOrdenar.add(labelDefinirIntervalo);

        JTextField textIntervaloInicial = new JTextField(5);
        textIntervaloInicial.setMaximumSize(new Dimension(60, 30));
        textIntervaloInicial.setBorder(BorderFactory.createCompoundBorder(
                textIntervaloInicial.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelOrdenar.add(textIntervaloInicial);

        JLabel labelAte = new JLabel("até");
        panelOrdenar.add(labelAte);

        JTextField textIntervaloFinal = new JTextField(5);
        textIntervaloFinal.setMaximumSize(new Dimension(60, 30));
        textIntervaloFinal.setBorder(BorderFactory.createCompoundBorder(
                textIntervaloFinal.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelOrdenar.add(textIntervaloFinal);

        // Inicialmente desabilitar intervalo
        checkDefinirIntervalo.setEnabled(false);

        // Adicionar DocumentListener para validar inputs
        DocumentListener intervalValidation = new DocumentListener() {
            private void validate() {
                try {
                    int intervaloInicial = Integer.parseInt(textIntervaloInicial.getText());
                    int intervaloFinal = Integer.parseInt(textIntervaloFinal.getText());
                    checkDefinirIntervalo.setEnabled(intervaloFinal > intervaloInicial);
                } catch (NumberFormatException e) {
                    checkDefinirIntervalo.setEnabled(false);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validate();
            }
        };

        textIntervaloInicial.getDocument().addDocumentListener(intervalValidation);
        textIntervaloFinal.getDocument().addDocumentListener(intervalValidation);

        // Tabela de arquivos
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[] colunas = {"Nome do Arquivo", "Ordem"};
        DefaultTableModel modeloTabela = new DefaultTableModel(colunas, 0);
        JTable tabelaArquivos = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaArquivos);
        panel.add(scrollPane, gbc);

        // Adiciona os botões abaixo da tabela
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton buttonRenomear = criarBotao("Renomear");
        panel.add(buttonRenomear, gbc);

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
                    atualizarTabelaArquivos(selectedFile, modeloTabela);
                }
            }
        });

        // Ação do botão Renomear
        buttonRenomear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int intervaloInicial = Integer.parseInt(textIntervaloInicial.getText());
                    int intervaloFinal = Integer.parseInt(textIntervaloFinal.getText());

                    if (intervaloFinal < intervaloInicial) {
                        JOptionPane.showMessageDialog(panel, "O intervalo final deve ser maior que o inicial.");
                        return;
                    }

                    String pasta = textPasta.getText();
                    File directory = new File(pasta);
                    File[] files = directory.listFiles();

                    if (files != null) {
                        Arrays.sort(files, Comparator.comparing(File::getName));
                        int counter = intervaloInicial;
                        for (int i = 0; i < files.length && counter <= intervaloFinal; i++) {
                            if (files[i].isFile()) {
                                String nomeArquivo = files[i].getName();
                                String novoNome = String.format("%03d_%s", counter++, nomeArquivo);

                                File novoArquivo = new File(directory, novoNome);
                                if (!files[i].renameTo(novoArquivo)) {
                                    JOptionPane.showMessageDialog(panel, "Erro ao renomear: " + nomeArquivo);
                                }
                            }
                        }
                        JOptionPane.showMessageDialog(panel, "Renomeação concluída!");
                        atualizarTabelaArquivos(directory, modeloTabela);
                    } else {
                        JOptionPane.showMessageDialog(panel, "Pasta não encontrada ou vazia.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Por favor, insira valores numéricos válidos para o intervalo.");
                }
            }
        });
    }

    private static void atualizarVisualizacaoArquivos(File directory) {
        File[] files = directory.listFiles();
        textAreaArquivos.setText("");
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                if (file.isFile()) {
                    textAreaArquivos.append(file.getName() + "\n");
                }
            }
        }
    }

    private static void atualizarTabelaArquivos(File directory, DefaultTableModel modeloTabela) {
        modeloTabela.setRowCount(0); // Limpar a tabela

        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                if (file.isFile()) {
                    modeloTabela.addRow(new Object[]{file.getName(), ""});
                }
            }
        }
    }

    private static JButton criarBotao(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(new Color(0xEB5E28));
        botao.setForeground(Color.WHITE);
        botao.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return botao;
    }
}
