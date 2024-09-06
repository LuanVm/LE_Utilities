import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class PainelMesclaPlanilha {

    private static final Logger LOGGER = Logger.getLogger(PainelMesclaPlanilha.class.getName());

    private JTextArea textAreaArquivos;
    private DefaultTableModel modeloTabela;
    private JProgressBar progressBar;
    private JTextField textPasta;
    private JTextField textNovoNome;
    private JTextField textArquivoBase;
    private JLabel statusColunasLabel;
    private Set<Integer> colunasBase = new HashSet<>();
    private volatile boolean cancelar = false;
    private JLabel labelStatusProcessamento;

    public PainelMesclaPlanilha(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
    }

    public JPanel criarPainel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(criarInputPanel(), BorderLayout.NORTH);
        panel.add(criarTabelaArquivos(), BorderLayout.CENTER);
        panel.add(criarButtonPanel(), BorderLayout.SOUTH);
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

        // Campo Pasta
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Pasta:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        textPasta = new JTextField(20);
        inputPanel.add(textPasta, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton buttonSelecionar = new JButton("Selecionar Pasta");
        buttonSelecionar.setToolTipText("Clique para selecionar a pasta que deseja organizar");
        inputPanel.add(buttonSelecionar, gbc);

        // Campo Nome do Arquivo
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Nome do arquivo:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        textNovoNome = new JTextField(20);
        inputPanel.add(textNovoNome, gbc);

        // Campo Arquivo Base
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Arquivo Base:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        textArquivoBase = new JTextField(20);
        inputPanel.add(textArquivoBase, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton buttonSelecionarBase = new JButton("Selecionar Arquivo Base");
        buttonSelecionarBase.setToolTipText("Clique para selecionar o arquivo base para definir as colunas.");
        inputPanel.add(buttonSelecionarBase, gbc);

        // Barra de Progresso
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Progresso:"), gbc);

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

        // Label de Status de Colunas
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        statusColunasLabel = new JLabel("Colunas não selecionadas.");
        statusColunasLabel.setForeground(Color.RED);
        inputPanel.add(statusColunasLabel, gbc);

        // Label de Status do Processamento
        gbc.gridy = 5;
        labelStatusProcessamento = new JLabel("Pronto para iniciar.");
        inputPanel.add(labelStatusProcessamento, gbc);

        buttonSelecionar.addActionListener(e -> selecionarPasta());
        buttonSelecionarBase.addActionListener(e -> selecionarArquivoBase());

        return inputPanel;
    }

    private void selecionarPasta() {
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

    private void selecionarArquivoBase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel", "xlsx"));

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textArquivoBase.setText(selectedFile.getAbsolutePath());

            // Ler colunas do arquivo base e abrir o diálogo de seleção
            SwingUtilities.invokeLater(() -> lerColunasBase(selectedFile));
        }
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

    private class MesclarArquivosWorker extends SwingWorker<Void, String> {

        private File[] files;
        private File directory;
        private String novoNomeBase;

        @Override
        protected Void doInBackground() throws Exception {
            mesclarArquivos();
            return null;
        }

        private void mesclarArquivos() {
            String pasta = textPasta.getText();
            novoNomeBase = textNovoNome.getText();

            if (pasta.isEmpty() || novoNomeBase.isEmpty() || colunasBase.isEmpty()) {
                publish("Por favor, preencha todos os campos e defina as colunas base.");
                return;
            }

            directory = new File(pasta);
            files = directory.listFiles((dir, name) -> name.endsWith(".xlsx"));

            if (files == null || files.length == 0 || !validarArquivos(files)) {
                publish("Nenhum arquivo .xlsx válido encontrado na pasta.");
                return;
            }

            // Configura a barra de progresso
            progressBar.setMaximum(files.length);
            progressBar.setValue(0);
            cancelar = false;

            try {
                File outputFile = new File(directory, novoNomeBase + ".xlsx");
                gerarArquivoXLSX(outputFile, files);
                publish("Mesclagem concluída.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro durante a mesclagem.", e);
                publish("Erro durante a mesclagem: " + e.getMessage());
            }
        }

        @Override
        protected void process(List<String> chunks) {
            for (String message : chunks) {
                JOptionPane.showMessageDialog(null, message);
            }
        }

        @Override
        protected void done() {
            try {
                get();
                atualizarVisualizacaoArquivos(directory);
                labelStatusProcessamento.setText("Processo concluído.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro durante a mesclagem.", e);
                JOptionPane.showMessageDialog(null, "Erro durante a mesclagem: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                labelStatusProcessamento.setText("Erro durante o processamento.");
            }
        }
    }

    private void adicionarAcaoBotaoMesclar(JButton buttonMesclar) {
        buttonMesclar.addActionListener(e -> {
            if (SwingUtilities.isEventDispatchThread()) {
                new MesclarArquivosWorker().execute();
            } else {
                SwingUtilities.invokeLater(() -> new MesclarArquivosWorker().execute());
            }
        });
    }

    private void adicionarAcaoBotaoCancelar(JButton buttonCancelar) {
        buttonCancelar.addActionListener(e -> {
            cancelar = true;
            labelStatusProcessamento.setText("Cancelamento solicitado...");
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
        // Usando SXSSFWorkbook para melhor desempenho com grandes quantidades de dados
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); // Mantém na memória apenas as últimas 100 linhas
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            workbook.setCompressTempFiles(true); // Ativa compressão para arquivos temporários

            SXSSFSheet sheet = workbook.createSheet("Dados Mesclados");

            int rowCount = 0; // Contador de linhas

            for (int fileIndex = 0; fileIndex < inputFiles.length; fileIndex++) {
                if (cancelar) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Processo cancelado."));
                    return;
                }

                File inputFile = inputFiles[fileIndex];
                int finalFileIndex = fileIndex;
                SwingUtilities.invokeLater(() -> {
                    modeloTabela.setValueAt("Em Processamento", finalFileIndex, 1);
                    labelStatusProcessamento.setText("Processando: " + inputFile.getName());
                });

                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
                     XSSFWorkbook inputWorkbook = new XSSFWorkbook(bis)) {

                    Sheet inputSheet = inputWorkbook.getSheetAt(0);

                    int lastRowNum = inputSheet.getLastRowNum();

                    for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
                        Row inputRow = inputSheet.getRow(rowIndex);
                        if (inputRow == null) {
                            continue;
                        }

                        Row outputRow = sheet.createRow(rowCount++);

                        for (int colIndex : colunasBase) {
                            Cell inputCell = inputRow.getCell(colIndex);
                            Cell outputCell = outputRow.createCell(colIndex);

                            if (inputCell != null) {
                                copiarConteudoCelula(inputCell, outputCell);
                            }
                        }

                        if (rowCount % 100 == 0) {
                            sheet.flushRows(100); // Libera memória a cada 100 linhas
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Erro ao processar arquivo: " + inputFile.getName(), e);
                    int finalFileIndex1 = fileIndex;
                    SwingUtilities.invokeLater(() -> modeloTabela.setValueAt("Erro", finalFileIndex1, 1));
                }

                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(progressBar.getValue() + 1);
                    modeloTabela.setValueAt("Concluído", finalFileIndex, 1);
                });
            }

            workbook.write(bos);
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
                    FormulaEvaluator evaluator = inputCell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(inputCell);
                    if (cellValue != null) {
                        switch (cellValue.getCellType()) {
                            case STRING:
                                outputCell.setCellValue(cellValue.getStringValue());
                                break;
                            case NUMERIC:
                                outputCell.setCellValue(cellValue.getNumberValue());
                                break;
                            case BOOLEAN:
                                outputCell.setCellValue(cellValue.getBooleanValue());
                                break;
                            case BLANK:
                                outputCell.setCellValue("");
                                break;
                            default:
                                outputCell.setCellValue("Erro");
                                break;
                        }
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
            outputCell.setCellValue("Erro");
        }
    }

    private void lerColunasBase(File baseFile) {
        colunasBase.clear();
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(baseFile));
             XSSFWorkbook baseWorkbook = new XSSFWorkbook(bis)) {

            Sheet baseSheet = baseWorkbook.getSheetAt(0);
            Row headerRow = baseSheet.getRow(0);

            if (headerRow != null) {
                // Exibir o diálogo para selecionar as colunas
                mostrarDialogoColunas(baseFile, headerRow);
            } else {
                JOptionPane.showMessageDialog(null, "A planilha base não possui cabeçalho.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao ler arquivo base: " + baseFile.getName(), e);
            JOptionPane.showMessageDialog(null, "Erro ao ler arquivo base: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarTabelaArquivos(File directory, DefaultTableModel modeloTabela) {
        modeloTabela.setRowCount(0); // Limpa a tabela
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".xlsx"));

        if (files != null) {
            for (File file : files) {
                modeloTabela.addRow(new Object[]{file.getName(), "Aguardando"});
            }
        }
    }

    private void atualizarVisualizacaoArquivos(File pasta) {
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".xlsx"));
        if (arquivos != null) {
            textAreaArquivos.setText(""); // Limpa a área de texto
            for (File arquivo : arquivos) {
                textAreaArquivos.append(arquivo.getName() + "\n");
            }
        }
    }

    private void mostrarDialogoColunas(File arquivoBase, Row headerRow) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(textAreaArquivos);
            ColumnSelectDialog dialog = new ColumnSelectDialog(frame, arquivoBase, headerRow);
            dialog.setModal(true); // Garante que o diálogo é modal
            dialog.setLocationRelativeTo(frame); // Centraliza o diálogo em relação ao frame
            dialog.setVisible(true);
            Set<Integer> colunasSelecionadas = dialog.getColunasSelecionadas();
            colunasBase = colunasSelecionadas;
            atualizarStatusColunas(colunasSelecionadas); // Atualiza o status das colunas
        });
    }

    private void atualizarStatusColunas(Set<Integer> colunasSelecionadas) {
        if (colunasSelecionadas.isEmpty()) {
            statusColunasLabel.setText("Nenhuma coluna selecionada.");
            statusColunasLabel.setForeground(Color.RED);
        } else {
            statusColunasLabel.setText("Colunas selecionadas: " + colunasSelecionadas);
            statusColunasLabel.setForeground(Color.GREEN);
        }
    }

    class ColumnSelectDialog extends JDialog {

        private Set<Integer> colunasSelecionadas = new HashSet<>();
        private JPanel panelColunas;
        private File arquivoBase;
        private Row headerRow;

        public ColumnSelectDialog(Frame owner, File arquivoBase, Row headerRow) {
            super(owner, "Selecionar Colunas", true);
            this.arquivoBase = arquivoBase;
            this.headerRow = headerRow;
            inicializar();
        }

        private void inicializar() {
            setLayout(new BorderLayout(10, 10));
            setSize(400, 300);
            setLocationRelativeTo(getOwner());

            panelColunas = new JPanel();
            panelColunas.setLayout(new BoxLayout(panelColunas, BoxLayout.Y_AXIS));

            JScrollPane scrollPane = new JScrollPane(panelColunas);
            add(scrollPane, BorderLayout.CENTER);

            JButton botaoConfirmar = new JButton("Confirmar");
            JButton botaoCancelar = new JButton("Cancelar");

            JPanel panelBotoes = new JPanel();
            panelBotoes.add(botaoConfirmar);
            panelBotoes.add(botaoCancelar);
            add(panelBotoes, BorderLayout.SOUTH);

            botaoConfirmar.addActionListener(e -> confirmarSelecao());
            botaoCancelar.addActionListener(e -> cancelarSelecao());

            carregarColunas();
        }

        private void confirmarSelecao() {
            colunasSelecionadas.clear();
            for (Component component : panelColunas.getComponents()) {
                if (component instanceof JCheckBox) {
                    JCheckBox checkBox = (JCheckBox) component;
                    if (checkBox.isSelected()) {
                        colunasSelecionadas.add((Integer) checkBox.getClientProperty("columnIndex"));
                    }
                }
            }
            dispose();
        }

        private void cancelarSelecao() {
            colunasSelecionadas.clear();
            dispose();
        }

        private void carregarColunas() {
            for (Cell cell : headerRow) {
                int colIndex = cell.getColumnIndex();
                String colName = cell.getStringCellValue();

                JCheckBox checkBox = new JCheckBox(colName);
                checkBox.setSelected(true);
                checkBox.putClientProperty("columnIndex", colIndex);
                panelColunas.add(checkBox);
            }
        }

        public Set<Integer> getColunasSelecionadas() {
            return colunasSelecionadas;
        }
    }
}
