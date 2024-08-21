import com.formdev.flatlaf.FlatDarculaLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.event.ComponentAdapter;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ComponentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Arrays;
import java.util.Comparator;


public class AlterarNomes {

    private static JTextArea textAreaArquivos;

    public static void main(String[] args) {

        // tema
        FlatDarculaLaf.setup();

        // tela de loading
        JWindow splashScreen = new JWindow();
        splashScreen.getContentPane().add(new JLabel("Carregando...", SwingConstants.CENTER));
        splashScreen.setSize(150, 100);
        splashScreen.setLocationRelativeTo(null);
        splashScreen.setVisible(true);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Livre Escolha - Utilities");

        try {
            BufferedImage originalImage = ImageIO.read(new File("src/mini.png"));
            int newWidth = 16;
            int newHeight = 16;
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

            resizedImage.getGraphics().drawImage(scaledImage,
                    0, 0, null);
            frame.setIconImage(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                frame.revalidate();
                frame.repaint();
            }
        });

        frame.pack(); // valida o layout e tamanho da janela

        splashScreen.dispose();
        frame.setVisible(true);

        // Cria um JTabbedPane para as abas
        JTabbedPane tabbedPane = new JTabbedPane();
        UIManager.put("TabbedPane.selected", new Color(0xEB5E28));

        // Cria a primeira aba (Substituição simples)
        JPanel panelSubstituicaoSimples = criarAba("Substituição Simples", tabbedPane);

        // Cria a segunda aba (Renomear com ordenação)
        JPanel panelRenomeacao = criarAba("Renomear e ordenar", tabbedPane);

        // Adiciona o JTabbedPane ao frame
        frame.add(tabbedPane);

        // Configura a primeira aba (Substituição Simples)
        configurarAbaSubstituicaoSimples(panelSubstituicaoSimples);

        // Configura a segunda aba (Renomear com Ordenação)
        configurarAbaRenomeacao(panelRenomeacao);
    }

    private static JPanel criarAba(String titulo, JTabbedPane tabbedPane) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab(titulo, panel);
        return panel;
    }

    private static void configurarAbaSubstituicaoSimples(JPanel panel) {
        // Painel para os campos de entrada
        JPanel inputPanel = new JPanel(new GridBagLayout()); // Usa GridBagLayout para um layout mais flexível
        inputPanel.setBorder(new TitledBorder("Configurações de Renomeação"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaçamento entre os componentes
        gbc.fill = GridBagConstraints.HORIZONTAL; // Os componentes preenchem horizontalmente a célula
        gbc.anchor = GridBagConstraints.WEST; // Alinha os componentes à esquerda

        // Pasta
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel labelPasta = new JLabel("Pasta:");
        inputPanel.add(labelPasta, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0; // Permite que o campo de texto se expanda horizontalmente
        JTextField textPasta = new JTextField(20);
        inputPanel.add(textPasta, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0; // Impede que o botão se expanda
        JButton buttonSelecionar = new JButton("Selecionar Pasta"); // Muda o texto do botão
        buttonSelecionar.setBackground(new Color(0xEB5E28));
        buttonSelecionar.setForeground(Color.WHITE);
        inputPanel.add(buttonSelecionar, gbc);

        // Nome original
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0; // Redefine o peso para o label
        JLabel labelOriginal = new JLabel("Nome original:");
        inputPanel.add(labelOriginal, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0; // Permite que o campo de texto se expanda
        JTextField textOriginal = new JTextField(20);
        inputPanel.add(textOriginal, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0; // Redefine o peso para ocupar apenas uma célula
        inputPanel.add(new JLabel(""), gbc); // Adiciona um espaço vazio para manter o alinhamento

        // Alterar para
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel labelNova = new JLabel("Alterar para:");
        inputPanel.add(labelNova, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField textNova = new JTextField(20);
        inputPanel.add(textNova, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        inputPanel.add(new JLabel(""), gbc);

//        Botão Renomear
//        JButton buttonRenomear = new JButton("Renomear");
//        buttonRenomear.setBackground(new Color(0xEB5E28));
//        buttonRenomear.setForeground(Color.WHITE);
//        buttonRenomear.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Espaçamento interno
//        buttonRenomear.setPreferredSize(new Dimension(100, 20));

        JButton buttonRenomear = new JButton("Renomear");
        buttonRenomear.setBackground(new Color(0xEB5E28));
        buttonRenomear.setForeground(Color.WHITE);
        inputPanel.add(buttonRenomear, gbc);

        gbc.gridx = 2; // Mesma coluna do botão "Selecionar"
        gbc.gridy = 2; // Mesma linha do campo de texto "Alterar para"
        gbc.gridwidth = 1; // O botão ocupa apenas 1 coluna
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST; // Alinha o botão à direita da célula
        inputPanel.add(buttonRenomear, gbc);

        // Adicionar área de visualização de arquivos
        gbc.gridx = 0;
        gbc.gridy = 3; // Muda para a próxima linha
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        textAreaArquivos = new JTextArea(10, 40);
        textAreaArquivos.setEditable(false);
        JScrollPane scrollPaneArquivos = new JScrollPane(textAreaArquivos);
        scrollPaneArquivos.setBorder(new TitledBorder("Arquivos na Pasta"));

        // Adiciona os painéis à aba
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPaneArquivos, BorderLayout.CENTER);

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
        // Painel para os campos de entrada
        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        inputPanel.setBorder(new TitledBorder("Configurações de Renomeação"));

        // Pasta
        JLabel labelPasta = new JLabel("Pasta:");
        inputPanel.add(labelPasta);

        JTextField textPasta = new JTextField(20);
        inputPanel.add(textPasta);

        JButton buttonSelecionar = new JButton("Selecionar");
        buttonSelecionar.setBackground(new Color(0xEB5E28));
        buttonSelecionar.setForeground(Color.WHITE);
        inputPanel.add(buttonSelecionar);

        // Ordenar seleção e definir intervalo
        JPanel panelOrdenar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JCheckBox checkOrdenar = new JCheckBox("Ordenar seleção");
        panelOrdenar.add(checkOrdenar);

        JCheckBox checkDefinirIntervalo = new JCheckBox("Definir intervalo");
        panelOrdenar.add(checkDefinirIntervalo);

        JLabel labelDefinirIntervalo = new JLabel("Definir intervalo:");
        panelOrdenar.add(labelDefinirIntervalo);

        JTextField textIntervaloInicial = new JTextField(5);
        panelOrdenar.add(textIntervaloInicial);

        JLabel labelAte = new JLabel("até");
        panelOrdenar.add(labelAte);

        JTextField textIntervaloFinal = new JTextField(5);
        panelOrdenar.add(textIntervaloFinal);

        inputPanel.add(panelOrdenar); // Adiciona o painel de ordenação ao painel de entrada

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
        String[] colunas = {"Nome do Arquivo", "Ordem"};
        DefaultTableModel modeloTabela = new DefaultTableModel(colunas, 0);
        JTable tabelaArquivos = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaArquivos);
        scrollPane.setBorder(new TitledBorder("Arquivos na Pasta"));

        // Adiciona os botões abaixo da tabela
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton buttonRenomear = criarBotao();
        buttonPanel.add(buttonRenomear);

        // Adiciona os painéis à aba
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

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

    private static JButton criarBotao() {
        JButton botao = new JButton("Renomear");
        botao.setBackground(new Color(0xEB5E28)); // Cor de fundo
        botao.setForeground(Color.WHITE); // Cor do texto
        botao.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Espaçamento interno
        return botao;
    }
}