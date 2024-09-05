import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class PlanilhaMergerPanel {

    private static final Logger LOGGER = Logger.getLogger(PlanilhaMergerPanel.class.getName());

    private JTextArea textAreaArquivos;
    private DefaultTableModel modeloTabela;
    private JProgressBar progressBar;
    private JTextField textPasta;
    private JTextField textNovoNome;
    private JTextField textArquivoBase;
    private Set<Integer> colunasBase = new HashSet<>();
    private volatile boolean cancelar = false;

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

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel labelNovoNome = new JLabel("Nome do arquivo:");
        inputPanel.add(labelNovoNome, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        textNovoNome = new JTextField(20);
        inputPanel.add(textNovoNome, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel labelArquivoBase = new JLabel("Arquivo Base:");
        inputPanel.add(labelArquivoBase, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        textArquivoBase = new JTextField(20);
        inputPanel.add(textArquivoBase, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton buttonSelecionarBase = new JButton("Selecionar Arquivo Base");
        buttonSelecionarBase.setToolTipText("Clique para selecionar o arquivo base para definir as colunas.");
        inputPanel.add(buttonSelecionarBase, gbc);

        JLabel labelProgresso = new JLabel("Progresso:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(labelProgresso, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMaximum(100);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        inputPanel.add(progressPanel, gbc);

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

        buttonSelecionarBase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel", "xlsx"));

                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    textArquivoBase.setText(selectedFile.getAbsolutePath());
                    lerColunasBase(selectedFile);
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
        JButton buttonCancelar = new JButton("Cancelar");
        buttonPanel.add(buttonMesclar);
        buttonPanel.add(buttonCancelar);

        adicionarAcaoBotaoMesclar(buttonMesclar);
        adicionarAcaoBotaoCancelar(buttonCancelar);

        return buttonPanel;
    }

    private void adicionarAcaoBotaoMesclar(JButton buttonMesclar) {
        buttonMesclar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pasta = textPasta.getText();
                String novoNomeBase = textNovoNome.getText();

                if (pasta.isEmpty() || novoNomeBase.isEmpty() || colunasBase.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos e defina as colunas base.");
                    return;
                }

                File directory = new File(pasta);
                File[] files = directory.listFiles((dir, name) -> name.endsWith(".xlsx"));

                if (files != null && files.length > 0 && validarArquivos(files)) {
                    progressBar.setMaximum(files.length);
                    progressBar.setValue(0);

                    cancelar = false;

                    new Thread(() -> {
                        try {
                            int totalFiles = files.length;
                            int processedFiles = 0;

                            for (File file : files) {
                                if (cancelar) {
                                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Processo cancelado."));
                                    break;
                                }

                                int finalProcessedFiles = processedFiles;
                                SwingUtilities.invokeLater(() -> modeloTabela.setValueAt("Em Processamento", finalProcessedFiles, 1));

                                try {
                                    File outputFile = new File(directory, novoNomeBase + ".xlsx");
                                    gerarArquivoXLSX(outputFile, files);
                                } catch (Exception ex) {
                                    LOGGER.log(Level.SEVERE, "Erro ao processar arquivo: " + file.getName(), ex);
                                    SwingUtilities.invokeLater(() -> atualizarVisualizacaoArquivosComErro(directory, file.getName()));
                                }

                                processedFiles++;
                                final int progress = processedFiles;
                                SwingUtilities.invokeLater(() -> {
                                    progressBar.setValue(progress);
                                    modeloTabela.setValueAt("Concluído", finalProcessedFiles, 1);
                                });
                            }

                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Mesclagem concluída."));
                        } catch (Exception exception) {
                            LOGGER.log(Level.SEVERE, "Erro ao criar o arquivo mesclado", exception);
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Erro ao criar o arquivo mesclado."));
                        }

                        SwingUtilities.invokeLater(() -> atualizarVisualizacaoArquivos(directory));
                    }).start();
                } else {
                    JOptionPane.showMessageDialog(null, "Nenhum arquivo .xlsx válido encontrado na pasta.");
                }
            }
        });
    }

    private void adicionarAcaoBotaoCancelar(JButton buttonCancelar) {
        buttonCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelar = true;
            }
        });
    }

    private boolean validarArquivos(File[] files) {
        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".xlsx")) {
                return false;
            }
        }
        return true;
    }

    private void gerarArquivoXLSX(File outputFile, File[] inputFiles) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Dados Mesclados");

        int currentRow = 0;

        for (File inputFile : inputFiles) {
            try (FileInputStream fis = new FileInputStream(inputFile);
                 XSSFWorkbook inputWorkbook = new XSSFWorkbook(fis)) {

                XSSFSheet inputSheet = inputWorkbook.getSheetAt(0);

                for (Row inputRow : inputSheet) {
                    Row outputRow = sheet.createRow(currentRow++);
                    for (Integer col : colunasBase) {
                        if (col < inputRow.getLastCellNum()) {
                            Cell inputCell = inputRow.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            Cell outputCell = outputRow.createCell(col);
                            copiarConteudoCelula(inputCell, outputCell);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao processar o arquivo: " + inputFile.getName(), e);
                throw new IOException("Erro ao processar o arquivo: " + inputFile.getName(), e);
            }
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            workbook.write(fos);
        } finally {
            workbook.close();
        }
    }

    private void copiarConteudoCelula(Cell inputCell, Cell outputCell) {
        try {
            switch (inputCell.getCellType()) {
                case STRING:
                    outputCell.setCellValue(inputCell.getStringCellValue());
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(inputCell)) {
                        outputCell.setCellValue(inputCell.getDateCellValue());
                    } else {
                        outputCell.setCellValue(inputCell.getNumericCellValue());
                    }
                    break;
                case BOOLEAN:
                    outputCell.setCellValue(inputCell.getBooleanCellValue());
                    break;
                case FORMULA:
                    // Copia o valor calculado da célula se for fórmula
                    switch (inputCell.getCachedFormulaResultType()) {
                        case STRING:
                            outputCell.setCellValue(inputCell.getStringCellValue());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(inputCell)) {
                                outputCell.setCellValue(inputCell.getDateCellValue());
                            } else {
                                outputCell.setCellValue(inputCell.getNumericCellValue());
                            }
                            break;
                        case BOOLEAN:
                            outputCell.setCellValue(inputCell.getBooleanCellValue());
                            break;
                        case BLANK:
                            outputCell.setCellValue("");
                            break;
                        default:
                            outputCell.setCellValue("Erro");
                            break;
                    }
                    break;
                case BLANK:
                    outputCell.setCellValue("");
                    break;
                default:
                    outputCell.setCellValue(inputCell.toString());
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao copiar conteúdo da célula.", e);
            outputCell.setCellValue("Erro");  // Marca como erro se não conseguir copiar o conteúdo
        }
    }

    private void lerColunasBase(File arquivoBase) {
        try (FileInputStream fis = new FileInputStream(arquivoBase);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                // Passa o objeto Frame e o arquivo base corretamente para o construtor
                ColumnSelectDialog columnSelectDialog = new ColumnSelectDialog((Frame) SwingUtilities.getWindowAncestor(textAreaArquivos), arquivoBase);
                columnSelectDialog.setVisible(true);

                colunasBase = columnSelectDialog.getColunasSelecionadas();
                if (colunasBase.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Nenhuma coluna selecionada.");
                } else {
                    JOptionPane.showMessageDialog(null, "Colunas base definidas com sucesso.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao ler o arquivo base.", e);
            JOptionPane.showMessageDialog(null, "Erro ao ler o arquivo base.");
        }
    }

    private void atualizarTabelaArquivos(File pasta, DefaultTableModel modelo) {
        modelo.setRowCount(0);  // Limpa a tabela
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".xlsx"));

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                modelo.addRow(new Object[]{arquivo.getName(), "Pendente"});
            }
        }
    }

    private void atualizarVisualizacaoArquivos(File pasta) {
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".xlsx"));
        if (arquivos != null) {
            textAreaArquivos.setText("");  // Limpa a área de texto
            for (File arquivo : arquivos) {
                textAreaArquivos.append(arquivo.getName() + "\n");
            }
        }
    }

    private void atualizarVisualizacaoArquivosComErro(File pasta, String nomeArquivoErro) {
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".xlsx"));
        if (arquivos != null) {
            textAreaArquivos.setText("");  // Limpa a área de texto
            for (File arquivo : arquivos) {
                if (arquivo.getName().equals(nomeArquivoErro)) {
                    textAreaArquivos.append(arquivo.getName() + " - Erro ao processar\n");
                } else {
                    textAreaArquivos.append(arquivo.getName() + "\n");
                }
            }
        }
    }

    // Diálogo para selecionar colunas
    class ColumnSelectDialog extends JDialog {

        private Set<Integer> colunasSelecionadas = new HashSet<>();
        private JPanel panelColunas;
        private File arquivoBase;

        public ColumnSelectDialog(Frame owner, File arquivoBase) {
            super(owner, "Selecionar Colunas", true);
            this.arquivoBase = arquivoBase;
            inicializar();
        }

        private void inicializar() {
            setLayout(new BorderLayout(10, 10));
            setSize(800, 500);
            setLocationRelativeTo(getOwner());

            panelColunas = new JPanel();
            panelColunas.setLayout(new BoxLayout(panelColunas, BoxLayout.Y_AXIS)); // Usar BoxLayout para rolagem vertical

            JScrollPane scrollPane = new JScrollPane(panelColunas);
            add(scrollPane, BorderLayout.CENTER);

            JButton botaoConfirmar = new JButton("Confirmar");
            JButton botaoCancelar = new JButton("Cancelar");

            JPanel panelBotoes = new JPanel();
            panelBotoes.add(botaoConfirmar);
            panelBotoes.add(botaoCancelar);
            add(panelBotoes, BorderLayout.SOUTH);

            botaoConfirmar.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    colunasSelecionadas.clear();
                    for (Component component : panelColunas.getComponents()) {
                        JCheckBox checkBox = (JCheckBox) component;
                        if (checkBox.isSelected()) {
                            colunasSelecionadas.add((Integer) checkBox.getClientProperty("columnIndex"));
                        }
                    }
                    dispose();
                }
            });

            botaoCancelar.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    colunasSelecionadas.clear();
                    dispose();
                }
            });

            carregarColunas();
        }

        private void carregarColunas() {
            try (FileInputStream fis = new FileInputStream(arquivoBase);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                if (headerRow != null) {
                    for (Cell cell : headerRow) {
                        int colIndex = cell.getColumnIndex();
                        String colName = cell.getStringCellValue();

                        JCheckBox checkBox = new JCheckBox(colName);
                        checkBox.setSelected(true); // Marca todas as checkboxes por padrão
                        checkBox.putClientProperty("columnIndex", colIndex);
                        panelColunas.add(checkBox);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao carregar o arquivo base.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        public Set<Integer> getColunasSelecionadas() {
            return colunasSelecionadas;
        }
    }
}