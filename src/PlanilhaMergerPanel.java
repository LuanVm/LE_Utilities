import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    private JLabel statusColunasLabel; // Adiciona um label para status das colunas
    private Set<Integer> colunasBase = new HashSet<>();
    private volatile boolean cancelar = false;

    public PlanilhaMergerPanel(JTextArea textAreaArquivos) {
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

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Nome do arquivo:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        textNovoNome = new JTextField(20);
        inputPanel.add(textNovoNome, gbc);

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

        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Progresso:"), gbc);

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

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        statusColunasLabel = new JLabel("Colunas não selecionadas.");
        statusColunasLabel.setForeground(Color.RED); // Cor vermelha para indicar erro
        inputPanel.add(statusColunasLabel, gbc);

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
            lerColunasBase(selectedFile);
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

    private class MesclarArquivosWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            mesclarArquivos();
            return null;
        }

        @Override
        protected void done() {
            try {
                get(); // Verifica se houve exceção durante o doInBackground()
                JOptionPane.showMessageDialog(null, "Mesclagem concluída.");
                atualizarVisualizacaoArquivos(new File(textPasta.getText()));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro durante a mesclagem.", e);
                JOptionPane.showMessageDialog(null, "Erro durante a mesclagem: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
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
        buttonCancelar.addActionListener(e -> cancelar = true);
    }

    private void mesclarArquivos() {
        String pasta = textPasta.getText();
        String novoNomeBase = textNovoNome.getText();

        if (pasta.isEmpty() || novoNomeBase.isEmpty() || colunasBase.isEmpty()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos e defina as colunas base."));
            return;
        }

        File directory = new File(pasta);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".xlsx"));

        if (files != null && files.length > 0 && validarArquivos(files)) {
            progressBar.setMaximum(files.length);
            progressBar.setValue(0);
            cancelar = false;

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (File file : files) {
                executor.submit(() -> processarArquivo(file, files, directory, novoNomeBase));
            }

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Interrupção durante a execução", e);
            }
        } else {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Nenhum arquivo .xlsx válido encontrado na pasta."));
        }
    }

    private void processarArquivo(File file, File[] files, File directory, String novoNomeBase) {
        if (cancelar) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Processo cancelado."));
            return;
        }

        int finalProcessedFiles = Arrays.asList(files).indexOf(file);
        SwingUtilities.invokeLater(() -> modeloTabela.setValueAt("Em Processamento", finalProcessedFiles, 1));

        try {
            File outputFile = new File(directory, novoNomeBase + ".xlsx");
            gerarArquivoXLSX(outputFile, files);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao processar arquivo: " + file.getName(), ex);
            SwingUtilities.invokeLater(() -> atualizarVisualizacaoArquivosComErro(directory, file.getName()));
        }

        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progressBar.getValue() + 1);
            modeloTabela.setValueAt("Concluído", finalProcessedFiles, 1);
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
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(); // Use SXSSFWorkbook para streaming de grandes dados
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            SXSSFSheet sheet = workbook.createSheet("Dados Mesclados");

            int rowCount = 0; // Contador de linhas

            for (File inputFile : inputFiles) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
                     XSSFWorkbook inputWorkbook = new XSSFWorkbook(bis)) {

                    XSSFSheet inputSheet = inputWorkbook.getSheetAt(0);

                    for (Row row : inputSheet) {
                        if (rowCount >= 100000) { // Exemplo de limite de linhas, ajuste conforme necessário
                            sheet.flushRows(100); // Flush rows para liberar memória
                            rowCount = 0;
                        }

                        Row newRow = sheet.createRow(rowCount++);
                        for (Cell cell : row) {
                            Cell newCell = newRow.createCell(cell.getColumnIndex(), cell.getCellType());
                            copiarConteudoCelula(cell, newCell);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Erro ao processar arquivo: " + inputFile.getName(), e);
                }
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
            outputCell.setCellValue("Erro");
        }
    }

    private void lerColunasBase(File baseFile) {
        colunasBase.clear();
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(baseFile));
             XSSFWorkbook baseWorkbook = new XSSFWorkbook(bis)) {

            XSSFSheet baseSheet = baseWorkbook.getSheetAt(0);
            Row headerRow = baseSheet.getRow(0);

            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    colunasBase.add(cell.getColumnIndex());
                }
            }

            mostrarDialogoColunas(baseFile);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao ler arquivo base: " + baseFile.getName(), e);
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

    private void atualizarVisualizacaoArquivosComErro(File pasta, String nomeArquivoErro) {
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".xlsx"));
        if (arquivos != null) {
            textAreaArquivos.setText(""); // Limpa a área de texto
            for (File arquivo : arquivos) {
                if (arquivo.getName().equals(nomeArquivoErro)) {
                    textAreaArquivos.append(arquivo.getName() + " - Erro ao processar\n");
                } else {
                    textAreaArquivos.append(arquivo.getName() + "\n");
                }
            }
        }
    }

    private void mostrarDialogoColunas(File arquivoBase) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(textAreaArquivos);
            ColumnSelectDialog dialog = new ColumnSelectDialog(frame, arquivoBase);
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
            try (FileInputStream fis = new FileInputStream(arquivoBase);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                if (headerRow != null) {
                    for (Cell cell : headerRow) {
                        int colIndex = cell.getColumnIndex();
                        String colName = cell.getStringCellValue();

                        JCheckBox checkBox = new JCheckBox(colName);
                        checkBox.setSelected(true);
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
