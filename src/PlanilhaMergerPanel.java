import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;

class PlanilhaMergerPanel {

    private static final Logger LOGGER = Logger.getLogger(PlanilhaMergerPanel.class.getName());

    private JTextArea textAreaArquivos;
    private DefaultTableModel modeloTabela;
    private JProgressBar progressBar;
    private JTextField textPasta;
    private JTextField textNovoNome;

    public PlanilhaMergerPanel(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
    }

    public JPanel criarPainel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = criarInputPanel();
        JPanel tabelaPanel = criarTabelaArquivos();
        JPanel buttonPanel = criarButtonPanel();

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(tabelaPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Configurações de Mesclagem",
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Configuração do rótulo e campo de texto para a pasta
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel labelPasta = new JLabel("Pasta:");
        inputPanel.add(labelPasta, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        textPasta = new JTextField(20);
        inputPanel.add(textPasta, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton buttonSelecionar = new JButton("Selecionar Pasta");
        buttonSelecionar.setToolTipText("Clique para selecionar a pasta que deseja organizar");
        inputPanel.add(buttonSelecionar, gbc);

        // Configuração do rótulo e campo de texto para o novo nome do arquivo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel labelNovoNome = new JLabel("Nome do arquivo:");
        inputPanel.add(labelNovoNome, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        textNovoNome = new JTextField(20);
        inputPanel.add(textNovoNome, gbc);

        JLabel labelProgresso = new JLabel("Progresso:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(labelProgresso, gbc);

        // Configuração da barra de progresso
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMaximum(100);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        inputPanel.add(progressPanel, gbc);

        // Adiciona ação ao botão de seleção de pasta
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
                    atualizarVisualizacaoArquivos(selectedFile);
                }
            }
        });

        return inputPanel;
    }


    private JPanel criarTabelaArquivos() {
        String[] colunas = {"Nome do Arquivo", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0);
        JTable tabelaArquivos = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaArquivos);
        scrollPane.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Arquivos na pasta", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 12)));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel criarButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton buttonMesclar = new JButton("Mesclar");
        JButton buttonLimpar = new JButton("Limpar Status");
        buttonPanel.add(buttonMesclar);
        buttonPanel.add(buttonLimpar);

        adicionarAcaoBotaoMesclar(buttonMesclar);
        adicionarAcaoBotaoLimpar(buttonLimpar);

        return buttonPanel;
    }

    private void adicionarAcaoBotaoMesclar(JButton buttonMesclar) {
        buttonMesclar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pasta = textPasta.getText();
                String novoNomeBase = textNovoNome.getText();

                if (pasta.isEmpty() || novoNomeBase.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos.");
                    return;
                }

                File directory = new File(pasta);
                File[] files = directory.listFiles();

                if (files != null) {
                    progressBar.setMaximum(files.length);
                    progressBar.setValue(0);

                    new Thread(() -> {
                        try (Workbook workbook = new XSSFWorkbook()) {
                            Sheet sheet = workbook.createSheet("Mesclado");

                            int rowIndex = 0;
                            for (int i = 0; i < files.length; i++) {
                                if (files[i].isFile() && files[i].getName().endsWith(".xlsx")) {
                                    try (Workbook inputWorkbook = new XSSFWorkbook(files[i])) {
                                        Sheet inputSheet = inputWorkbook.getSheetAt(0);
                                        for (Row row : inputSheet) {
                                            Row newRow = sheet.createRow(rowIndex++);
                                            for (int col = 0; col < 28; col++) {
                                                Cell cell = row.getCell(col);
                                                if (cell != null) {
                                                    Cell newCell = newRow.createCell(col, cell.getCellType());
                                                    newCell.setCellValue(cell.toString());
                                                }
                                            }
                                        }
                                        modeloTabela.setValueAt("Mesclado", i, 1);
                                    } catch (IOException | InvalidFormatException ioException) {
                                        modeloTabela.setValueAt("Erro ao ler arquivo", i, 1);
                                    }

                                    int finalI = i;
                                    SwingUtilities.invokeLater(() -> progressBar.setValue(finalI + 1));
                                }
                            }

                            File outputFile = new File(directory, novoNomeBase + ".xlsx");
                            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                                workbook.write(out);
                            }

                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Mesclagem concluída. Arquivo salvo em: " + outputFile.getAbsolutePath()));
                        } catch (IOException ioException) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Erro ao criar o arquivo mesclado."));
                        }

                        SwingUtilities.invokeLater(() -> atualizarVisualizacaoArquivos(directory));
                    }).start();
                } else {
                    JOptionPane.showMessageDialog(null, "Pasta não encontrada ou vazia.");
                }
            }
        });
    }

    private void adicionarAcaoBotaoLimpar(JButton buttonLimpar) {
        buttonLimpar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowCount = modeloTabela.getRowCount();
                for (int i = 0; i < rowCount; i++) {
                    modeloTabela.setValueAt("", i, 1);
                }
            }
        });
    }

    private void atualizarTabelaArquivos(File directory, DefaultTableModel modeloTabela) {
        modeloTabela.setRowCount(0);
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".xlsx")) {
                    modeloTabela.addRow(new Object[]{file.getName(), "Não processado"});
                }
            }
        }
    }

    private void atualizarVisualizacaoArquivos(File directory) {
        StringBuilder sb = new StringBuilder();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".xlsx")) {
                    sb.append(file.getName()).append("\n");
                }
            }
        }
        textAreaArquivos.setText(sb.toString());
    }
}
