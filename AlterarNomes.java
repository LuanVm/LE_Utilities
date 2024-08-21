import com.formdev.flatlaf.FlatDarculaLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class AlterarNomes {

    private static JTextArea textAreaArquivos;

    public static void main(String[] args) {

        // Tema
        FlatDarculaLaf.setup();

        // Tela de loading
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
        frame.setPreferredSize(new Dimension(1280,
                720));

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                frame.revalidate();
                frame.repaint();
            }
        });

        frame.pack();

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

    private static JButton criarBotao(String texto) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(getBackground().darker());
                } else {
                    g.setColor(getBackground());
                }
                g.fillRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 4, 4);

                super.paintComponent(g);
            }
        };

        botao.setBackground(new Color(0xEB5E28));
        botao.setForeground(Color.WHITE);
        botao.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        botao.setFocusPainted(false); // Remove a borda de foco padrão
        botao.setContentAreaFilled(false); // Remove o preenchimento padrão do botão

        return botao;
    }

    // Função auxiliar para obter a extensão de um arquivo
    private static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot > 0) {
            return fileName.substring(lastIndexOfDot);
        } else {
            return ""; // Sem extensão
        }
    }

    private static void configurarAbaSubstituicaoSimples(JPanel panel) {
        // Painel para os campos de entrada
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder("Configurações de Renomeação"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;


        // Pasta
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel labelPasta = new JLabel("Pasta:");
        inputPanel.add(labelPasta, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField textPasta = new JTextField(20);
        inputPanel.add(textPasta, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0; // Impede que o botão se expanda
        JButton buttonSelecionar = criarBotao("Selecionar Pasta"); // Usa o método criarBotao
        inputPanel.add(buttonSelecionar, gbc);

        // Nome original
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel labelOriginal = new JLabel("Nome original:");
        inputPanel.add(labelOriginal, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField textOriginal = new JTextField(20);
        inputPanel.add(textOriginal, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        inputPanel.add(new JLabel(""), gbc);

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

        // Botão Renomear
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JButton buttonRenomear = criarBotao("Renomear"); // Usa o método criarBotao
        inputPanel.add(buttonRenomear, gbc);

        // Adicionar área de visualização de arquivos
        gbc.gridx = 0;
        gbc.gridy = 3;
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
        // Painel principal da aba, usando BorderLayout para organizar os componentes
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Painel para os campos de entrada, usando GridBagLayout para um layout flexível
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder("Configurações de Renomeação"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;


        // Pasta
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel labelPasta = new JLabel("Pasta:");
        inputPanel.add(labelPasta, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField textPasta = new JTextField(20);
        inputPanel.add(textPasta, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton buttonSelecionar = criarBotao("Selecionar Pasta"); // Usa o método criarBotao
        inputPanel.add(buttonSelecionar, gbc);

        // Novo nome base para os arquivos
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel labelNovoNome = new JLabel("Novo nome base:");
        inputPanel.add(labelNovoNome, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JTextField textNovoNome = new JTextField(20);
        inputPanel.add(textNovoNome, gbc);

        // Opções de ordenação e intervalo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3; // Ocupa todas as colunas
        JPanel panelOrdenar = new JPanel(new FlowLayout(FlowLayout.LEFT));

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

        inputPanel.add(panelOrdenar);

        // Inicialmente desabilitar intervalo
        labelDefinirIntervalo.setEnabled(false);
        textIntervaloInicial.setEnabled(false);
        labelAte.setEnabled(false);
        textIntervaloFinal.setEnabled(false);

        // Habilitar/desabilitar intervalo com base no checkbox
        checkDefinirIntervalo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean habilitar = checkDefinirIntervalo.isSelected();
                labelDefinirIntervalo.setEnabled(habilitar);
                textIntervaloInicial.setEnabled(habilitar);
                labelAte.setEnabled(habilitar);
                textIntervaloFinal.setEnabled(habilitar);
            }
        });

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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton buttonRenomear = criarBotao("Renomear");
        buttonPanel.add(buttonRenomear);

        // Variáveis para controlar o arrasto do mouse
        int startRow = -1;
        int startValue = -1;

        // MouseListener para detectar o início do arrasto
        tabelaArquivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tabelaArquivos.rowAtPoint(e.getPoint());
                int column = tabelaArquivos.columnAtPoint(e.getPoint());

                if (column == 1 && SwingUtilities.isLeftMouseButton(e)) { // Segunda coluna e botão esquerdo do mouse
                    startRow = row;
                    try {
                        startValue = Integer.parseInt((String) modeloTabela.getValueAt(row, column));
                    } catch (NumberFormatException ex) {
                        startValue = -1; // Valor inválido, ignora o arrasto
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                startRow = -1;
                startValue = -1;
            }
        });

        // MouseMotionListener para detectar o arrasto do mouse
        tabelaArquivos.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (startRow != -1 && startValue != -1) {
                    int row = tabelaArquivos.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < modeloTabela.getRowCount()) {
                        int incremento = (row > startRow) ? 1 : -1;
                        int valor = startValue;
                        for (int i = startRow; i != row + incremento; i += incremento) {
                            modeloTabela.setValueAt(String.valueOf(valor), i, 1);
                            valor += incremento;
                        }
                    }
                }
            }
        });

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

        // Configurar a segunda coluna da tabela para permitir edição e preenchimento sequencial
        tabelaArquivos.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
        tabelaArquivos.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

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
                    String novoNomeBase = textNovoNome.getText();

                    File directory = new File(pasta);
                    File[] files = directory.listFiles();

                    if (files != null) {
                        Arrays.sort(files, Comparator.comparing(File::getName));
                        int counter = intervaloInicial;
                        for (int i = 0; i < files.length && counter <= intervaloFinal; i++) {
                            if (files[i].isFile()) {
                                String nomeArquivo = files[i].getName();

                                // Ajusta o formato de numeração com base no valor do contador
                                String formatoNumeracao = (counter < 100) ? "%d_" : "%03d_";
                                String novoNome = String.format(formatoNumeracao + "%s%s", counter, novoNomeBase, getFileExtension(nomeArquivo));
                                counter++;

                                File novoArquivo = new File(directory, novoNome);
                                if (!files[i].renameTo(novoArquivo)) {
                                    JOptionPane.showMessageDialog(panel, "Erro ao renomear: " + nomeArquivo);
                                } else {
                                    modeloTabela.setValueAt(novoNome, i, 0);
                                }
                            }
                        }
                        JOptionPane.showMessageDialog(panel, "Renomeação concluída!");
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
        modeloTabela.setRowCount(0);

        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                if
                (file.isFile()) {
                    modeloTabela.addRow(new Object[]{file.getName(), ""});
                }
            }
        }
    }
}