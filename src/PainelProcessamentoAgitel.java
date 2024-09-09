import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PainelProcessamentoAgitel {

    private JTextArea textAreaResultados;
    private JFileChooser fileChooser;
    private JProgressBar progressBar;
    private SwingWorker<Void, String> worker;
    private int totalSheets;
    private int totalLinhas;

    // Cache de estilos
    private CellStyle generalStyle;
    private CellStyle dateStyle;

    public PainelProcessamentoAgitel(JTextArea textAreaResultados) {
        this.textAreaResultados = textAreaResultados;
        this.fileChooser = new JFileChooser();
        this.progressBar = new JProgressBar();
    }

    public JPanel criarPainel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = criarPainelInput();
        JScrollPane scrollPaneResultados = criarScrollPaneResultados();

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPaneResultados, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarPainelInput() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Processamento de Arquivo Excel", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel labelArquivo = new JLabel("Arquivo:");
        inputPanel.add(labelArquivo, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField textArquivo = new JTextField(20);
        inputPanel.add(textArquivo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton buttonSelecionar = TelaPrincipal.criarBotao("Selecionar Arquivo");
        inputPanel.add(buttonSelecionar, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton buttonProcessar = TelaPrincipal.criarBotao("Processar");
        inputPanel.add(buttonProcessar, gbc);

        buttonSelecionar.addActionListener(e -> selecionarArquivo(textArquivo));
        buttonProcessar.addActionListener(e -> processarArquivo(textArquivo.getText()));

        return inputPanel;
    }

    private JScrollPane criarScrollPaneResultados() {
        textAreaResultados = new JTextArea(20, 60);
        textAreaResultados.setEditable(false);
        JScrollPane scrollPaneResultados = new JScrollPane(textAreaResultados);
        scrollPaneResultados.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Resultados", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 12)));
        return scrollPaneResultados;
    }

    private void selecionarArquivo(JTextField textArquivo) {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textArquivo.setText(selectedFile.getAbsolutePath());
        }
    }

    private void processarArquivo(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            exibirMensagemErro("Arquivo inválido.");
            return;
        }

        progressBar.setIndeterminate(true);
        progressBar.setString("Processando...");
        progressBar.setStringPainted(true);

        worker = new SwingWorker<Void, String>() {

            @Override
            protected Void doInBackground() throws Exception {
                try (FileInputStream fis = new FileInputStream(file);
                     XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

                    totalSheets = workbook.getNumberOfSheets() - 1; // Exclui a primeira aba
                    totalLinhas = calcularTotalLinhas(workbook); // Calcula o total de linhas
                    File outputFile = new File(file.getParent(), "leitura_agitel.xlsx");

                    try (SXSSFWorkbook outputWorkbook = new SXSSFWorkbook(50)) {
                        int abaIndex = 1;
                        SXSSFSheet outputSheet = outputWorkbook.createSheet("Dados Copiados " + abaIndex);

                        Row headerRow = outputSheet.createRow(0);
                        String[] headers = {"Data", "Origem", "Servico", "Regiao", "Destino", "Duracao", "Preco"};
                        for (int i = 0; i < headers.length; i++) {
                            headerRow.createCell(i).setCellValue(headers[i]);
                        }

                        int rowIndex = 1;

                        int linhasProcessadas = 0;

                        if (generalStyle == null) {
                            generalStyle = outputWorkbook.createCellStyle();
                            generalStyle.setDataFormat(outputWorkbook.getCreationHelper().createDataFormat().getFormat("General"));
                        }

                        if (dateStyle == null) {
                            dateStyle = outputWorkbook.createCellStyle();
                            dateStyle.setDataFormat(outputWorkbook.getCreationHelper().createDataFormat().getFormat("hh:mm:ss"));
                        }

                        for (int i = 1; i < workbook.getNumberOfSheets(); i++) { // Ignora a primeira aba (i = 1)
                            Sheet sheet = workbook.getSheetAt(i);
                            publish("Processando aba: " + sheet.getSheetName() + " (" + sheet.getLastRowNum() + " linhas)");

                            Row header = findHeaderRow(sheet);
                            if (header != null) {
                                int startRowIndex = header.getRowNum() + 1;

                                for (int rowIndexInSheet = startRowIndex; rowIndexInSheet <= sheet.getLastRowNum(); rowIndexInSheet++) {
                                    Row row = sheet.getRow(rowIndexInSheet);
                                    if (row != null && !isRowEmpty(row)) {
                                        // Encontra a próxima linha vazia na aba de saída
                                        int nextAvailableRowIndex = findNextAvailableRow(outputSheet);

                                        // Cria uma nova linha na aba de saída na próxima linha disponível
                                        Row outputRow = outputSheet.createRow(nextAvailableRowIndex);

                                        // Copia os dados da linha atual
                                        copyRow(row, outputRow, outputWorkbook);
                                    }

                                    linhasProcessadas++;

                                    if (linhasProcessadas % 100 == 0) {
                                        int progress = (int) (((double) linhasProcessadas / totalLinhas) * 100);
                                        setProgress(progress);
                                    }
                                }
                            }

                            ((SXSSFSheet) outputSheet).flushRows(50);
                        }

                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            outputWorkbook.write(fos);
                        }

                        publish("Processamento finalizado com sucesso.");
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    textAreaResultados.append(message + "\n");
                }
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                progressBar.setString("Processamento concluído!");
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    exibirMensagemErro("Erro durante o processamento.");
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
    }

    private Row findHeaderRow(Sheet sheet) {
        for (int i = 8; i <= 10; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(0);
                if (cell != null && "Data".equals(getCellValue(cell))) {
                    return row;
                }
            }
        }
        return null;
    }

    private void copyRow(Row sourceRow, Row targetRow, SXSSFWorkbook workbook) {
        for (int i = 0; i < 7; i++) { // Copiando até a coluna G (índice 6)
            Cell sourceCell = sourceRow.getCell(i);
            Cell targetCell = targetRow.createCell(i);
            if (sourceCell != null) {
                copyCell(sourceCell, targetCell, workbook);
            }
        }
    }

    private void copyCell(Cell sourceCell, Cell targetCell, SXSSFWorkbook workbook) {
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                targetCell.setCellStyle(generalStyle);
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                    targetCell.setCellStyle(dateStyle);
                } else {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                    targetCell.setCellStyle(generalStyle);
                }
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                targetCell.setCellStyle(generalStyle);
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                targetCell.setCellStyle(generalStyle);
                break;
            default:
                targetCell.setCellStyle(generalStyle);
                break;
        }
    }

    private int findNextAvailableRow(Sheet sheet) {
        int rowIndex = sheet.getLastRowNum() + 1;
        while (sheet.getRow(rowIndex) != null && !isRowEmpty(sheet.getRow(rowIndex))) {
            rowIndex++;
        }
        return rowIndex;
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < 7; i++) { // Verifica até a coluna G
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private int calcularTotalLinhas(XSSFWorkbook workbook) {
        int totalLinhas = 0;
        for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            totalLinhas += sheet.getLastRowNum();
        }
        return totalLinhas;
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private void exibirMensagemErro(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
