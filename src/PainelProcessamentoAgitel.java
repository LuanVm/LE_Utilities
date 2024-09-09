import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFTableStyleInfo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;

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
    private int totalSheets;  // Total de abas
    private int totalLinhas;  // Total de linhas

    // Cache de estilos
    private CellStyle generalStyle;
    private CellStyle dateStyle;
    private CellStyle accountingStyle;

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

                    totalSheets = workbook.getNumberOfSheets() - 1;
                    totalLinhas = calcularTotalLinhas(workbook);
                    File outputFile = new File(file.getParent(), "leitura_agitel.xlsx");

                    try (SXSSFWorkbook outputWorkbook = new SXSSFWorkbook(50)) {
                        generalStyle = outputWorkbook.createCellStyle();
                        generalStyle.setDataFormat(outputWorkbook.getCreationHelper().createDataFormat().getFormat("General"));

                        dateStyle = outputWorkbook.createCellStyle();
                        dateStyle.setDataFormat(outputWorkbook.getCreationHelper().createDataFormat().getFormat("hh:mm:ss"));

                        accountingStyle = outputWorkbook.createCellStyle();
                        accountingStyle.setDataFormat(outputWorkbook.getCreationHelper().createDataFormat().getFormat("R$ 0.00"));

                        int abaIndex = 1;
                        SXSSFSheet outputSheet = outputWorkbook.createSheet("Dados Copiados " + abaIndex);

                        // Defina os cabeçalhos da tabela na primeira linha
                        Row headerRow = outputSheet.createRow(0);
                        String[] columnNames = {"Horário", "Setor", "Identificador", "Região", "Número Destino", "Duração", "Valor"};
                        for (int i = 0; i < columnNames.length; i++) {
                            Cell cell = headerRow.createCell(i);
                            cell.setCellValue(columnNames[i]);
                            cell.setCellStyle(generalStyle);
                        }

                        int rowIndex = 1;
                        int linhasProcessadas = 0;

                        for (int i = 1; i < workbook.getNumberOfSheets(); i++) { // Ignora a primeira aba (i = 1)
                            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);

                            publish("Processando aba: " + sheet.getSheetName() + " (" + sheet.getLastRowNum() + " linhas)");

                            Row header = findHeaderRow(sheet);

                            if (header != null) {
                                int startRowIndex = header.getRowNum() + 1;

                                for (int rowIndexInSheet = startRowIndex; rowIndexInSheet <= sheet.getLastRowNum(); rowIndexInSheet++) {
                                    Row row = sheet.getRow(rowIndexInSheet);
                                    if (row != null) {
                                        if (rowIndex >= 1048576) {
                                            abaIndex++;
                                            outputSheet = outputWorkbook.createSheet("Dados Copiados " + abaIndex);

                                            // Defina os cabeçalhos na nova aba
                                            headerRow = outputSheet.createRow(0);
                                            for (int j = 0; j < columnNames.length; j++) {
                                                Cell cell = headerRow.createCell(j);
                                                cell.setCellValue(columnNames[j]);
                                                cell.setCellStyle(generalStyle);
                                            }

                                            rowIndex = 1;
                                        }

                                        Row outputRow = outputSheet.createRow(rowIndex++);
                                        copyRow(row, outputRow, outputWorkbook);

                                        // Ajuste a formatação das células após a cópia
                                        Cell cellF = outputRow.getCell(5); // Coluna F
                                        if (cellF != null && cellF.getCellType() == CellType.NUMERIC) {
                                            double numericValue = cellF.getNumericCellValue();
                                            if (numericValue == -1) {
                                                cellF.setBlank(); // Ignora valores -1 na coluna F
                                            } else {
                                                cellF.setCellStyle(generalStyle);
                                            }
                                        }

                                        Cell cellG = outputRow.getCell(6); // Coluna G
                                        if (cellG != null && cellG.getCellType() == CellType.NUMERIC) {
                                            cellG.setCellStyle(accountingStyle);
                                        }
                                    }
                                    linhasProcessadas++;

                                    // Atualiza a ProgressBar progressivamente
                                    int progress = (int) (((double) linhasProcessadas / totalLinhas) * 100);
                                    setProgress(progress);
                                }
                            }

                            outputSheet.flushRows(50);
                        }

                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            outputWorkbook.write(fos);
                        }

                        publish("Processamento finalizado com sucesso.");
                    }
                } finally {
                    System.gc(); // Tenta limpar a memória
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
                int progress = (Integer) evt.getNewValue();
                progressBar.setValue(progress);
                progressBar.setString(progress + "%");
            }
        });

        worker.execute();
    }

    private int calcularTotalLinhas(XSSFWorkbook workbook) {
        int totalLinhas = 0;
        for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
            totalLinhas += sheet.getLastRowNum() + 1; // Ajustado para contar todas as linhas
        }
        return totalLinhas;
    }

    private void criarTabela(XSSFSheet sheet, int abaIndex) {
        XSSFTable table = sheet.createTable(null);
        table.setDisplayName("Tabela" + abaIndex); // Nome único para cada aba

        // Verifica se há pelo menos duas linhas antes de criar a tabela
        int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum >= 1) {
            AreaReference reference = sheet.getWorkbook().getCreationHelper().createAreaReference("A1:G" + (lastRowNum + 1));
            table.setArea(reference);

            // Estilo da Tabela
            XSSFTableStyleInfo style = (XSSFTableStyleInfo) table.getStyle();
            style.setName("TableStyleMedium2");
            style.setShowColumnStripes(true);
            style.setShowRowStripes(true);

            // Colunas
            CTTableColumns columns = table.getCTTable().addNewTableColumns();
            columns.setCount(7);

            String[] columnNames = {"Horário", "Setor", "Identificador", "Região", "Número Destino", "Duração", "Valor"};
            for (int i = 0; i < columnNames.length; i++) {
                CTTableColumn column = columns.addNewTableColumn();
                column.setId(i + 1);
                column.setName(columnNames[i]);
            }
        }
    }

    private Row findHeaderRow(Sheet sheet) {
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                boolean isHeader = true;
                for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    if (cell == null || cell.getCellType() == CellType.BLANK || cell.getCellType() == CellType.NUMERIC) {
                        isHeader = false;
                        break;
                    }
                }
                if (isHeader) {
                    return row;
                }
            }
        }
        return null;
    }

    private void copyRow(Row sourceRow, Row targetRow, SXSSFWorkbook outputWorkbook) {
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            if (sourceCell != null) {
                Cell targetCell = targetRow.createCell(i);
                switch (sourceCell.getCellType()) {
                    case STRING:
                        targetCell.setCellValue(sourceCell.getStringCellValue());
                        targetCell.setCellStyle(generalStyle);
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(sourceCell)) {
                            targetCell.setCellValue(sourceCell.getDateCellValue());
                            targetCell.setCellStyle(dateStyle);
                        } else {
                            targetCell.setCellValue(sourceCell.getNumericCellValue());
                            if (i == 6) { // Coluna G
                                targetCell.setCellStyle(accountingStyle);
                            } else {
                                targetCell.setCellStyle(generalStyle);
                            }
                        }
                        break;
                    case BOOLEAN:
                        targetCell.setCellValue(sourceCell.getBooleanCellValue());
                        targetCell.setCellStyle(generalStyle);
                        break;
                    case FORMULA:
                        try {
                            switch (sourceCell.getCachedFormulaResultType()) {
                                case STRING:
                                    targetCell.setCellValue(sourceCell.getStringCellValue());
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(sourceCell)) {
                                        targetCell.setCellValue(sourceCell.getDateCellValue());
                                    } else {
                                        targetCell.setCellValue(sourceCell.getNumericCellValue());
                                    }
                                    break;
                                case BOOLEAN:
                                    targetCell.setCellValue(sourceCell.getBooleanCellValue());
                                    break;
                                default:
                                    targetCell.setCellType(CellType.BLANK);
                                    break;
                            }
                        } catch (Exception e) {
                            targetCell.setCellType(CellType.BLANK);
                        }
                        targetCell.setCellStyle(generalStyle);
                        break;
                    case BLANK:
                        targetCell.setCellType(CellType.BLANK);
                        break;
                    default:
                        targetCell.setCellType(CellType.BLANK);
                        break;
                }
            }
        }

        // Unifica as colunas Número e Destino em uma só
        Cell numeroCell = targetRow.getCell(4); // Coluna E
        Cell destinoCell = targetRow.getCell(5); // Coluna F

        if (numeroCell != null && destinoCell != null) {
            String numero = "";
            if (numeroCell.getCellType() == CellType.STRING) {
                numero = numeroCell.getStringCellValue();
            } else if (numeroCell.getCellType() == CellType.NUMERIC) {
                numero = String.valueOf(numeroCell.getNumericCellValue());
            }

            String destino = "";
            if (destinoCell.getCellType() == CellType.STRING) {
                destino = destinoCell.getStringCellValue();
            } else if (destinoCell.getCellType() == CellType.NUMERIC) {
                destino = String.valueOf(destinoCell.getNumericCellValue());
            }

            Cell numeroDestinoCell = targetRow.createCell(4);
            numeroDestinoCell.setCellValue(numero + " " + destino);
            numeroDestinoCell.setCellStyle(generalStyle);

            // Remove as colunas separadas
            targetRow.removeCell(destinoCell);
        }
    }

    private void exibirMensagemErro(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
