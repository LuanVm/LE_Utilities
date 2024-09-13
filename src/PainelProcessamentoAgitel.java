import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class PainelProcessamentoAgitel {

    // Componentes da interface
    private JTextArea textAreaResultados;
    private JFileChooser fileChooser;
    private JProgressBar progressBar;
    private JCheckBox checkboxEqualizar;

    // Variáveis de controle
    private SwingWorker<Void, String> worker;
    private int totalSheets;
    private int totalLinhas;

    // Cache de estilos para celulas
    private CellStyle generalStyle;
    private CellStyle dateStyle;
    private CellStyle accountingStyle;

    // Constantes de colunas para facilitar manutenção
    private static final int COLUNA_HORARIO = 0;
    private static final int COLUNA_SETOR = 1;
    private static final int COLUNA_IDENTIFICADOR = 2;
    private static final int COLUNA_REGIAO = 3;
    private static final int COLUNA_NUMERO_DESTINO = 4;
    private static final int COLUNA_DURACAO = 5;
    private static final int COLUNA_VALOR = 6;

    public PainelProcessamentoAgitel(JTextArea textAreaResultados) {
        this.textAreaResultados = textAreaResultados;
        this.fileChooser = new JFileChooser();
        this.progressBar = new JProgressBar();
    }

    // Metodo para criar o painel principal
    public JPanel criarPainel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Adiciona subcomponentes ao painel
        panel.add(criarPainelInput(), BorderLayout.NORTH);
        panel.add(criarScrollPaneResultados(), BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        return panel;
    }

    // Metodo para criar o painel de input
    private JPanel criarPainelInput() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Processamento de Arquivo Excel",
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12)
        ));

        // Configurações do GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Adiciona os componentes ao painel de input
        adicionarComponentesPainelInput(inputPanel, gbc);

        return inputPanel;
    }

    // Metodo para adicionar os componentes ao painel de input
    private void adicionarComponentesPainelInput(JPanel inputPanel, GridBagConstraints gbc) {
        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Arquivo:"), gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JTextField textArquivo = new JTextField(20);
        inputPanel.add(textArquivo, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JButton buttonSelecionar = new JButton("Selecionar Arquivo");
        inputPanel.add(buttonSelecionar, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        JButton buttonProcessar = new JButton("Processar");
        inputPanel.add(buttonProcessar, gbc);

        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        checkboxEqualizar = new JCheckBox("Equalizar 'Região'");
        inputPanel.add(checkboxEqualizar, gbc);

        // Adiciona a caixa de texto sobre a RAM
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        JTextArea textAreaRAM = new JTextArea("Certifique-se de ter pelo menos 12 GB de RAM para processar o arquivo da Agitel.", 2, 60);
        textAreaRAM.setEditable(false);
        textAreaRAM.setBackground(inputPanel.getBackground()); // Mantem o fundo do painel
        textAreaRAM.setWrapStyleWord(true);
        textAreaRAM.setLineWrap(true);
        textAreaRAM.setForeground(Color.GRAY);
        textAreaRAM.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        inputPanel.add(textAreaRAM, gbc);

        // Configura os eventos de ação dos botões
        buttonSelecionar.addActionListener(e -> selecionarArquivo(textArquivo));
        buttonProcessar.addActionListener(e -> processarArquivo(textArquivo.getText()));
    }

    // Metodo para criar o JScrollPane que contem a área de texto de resultados
    private JScrollPane criarScrollPaneResultados() {
        textAreaResultados = new JTextArea(20, 60);
        textAreaResultados.setEditable(false);
        JScrollPane scrollPaneResultados = new JScrollPane(textAreaResultados);
        scrollPaneResultados.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Resultados",
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12)
        ));
        return scrollPaneResultados;
    }

    private void selecionarArquivo(JTextField textArquivo) {
        // Configura o JFileChooser para permitir apenas a seleção de arquivos
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Exibe a caixa de diálogo para seleção do arquivo
        int returnValue = fileChooser.showOpenDialog(null);

        // Verifica se um arquivo foi selecionado com sucesso
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textArquivo.setText(selectedFile.getAbsolutePath()); // Atualiza o campo de texto com o caminho do arquivo selecionado
        }
    }

    private void processarArquivo(String filePath) {
        File file = new File(filePath);

        // Validação do arquivo: se não existe ou e um diretório
        if (!file.exists() || file.isDirectory()) {
            exibirMensagemErro("Arquivo inválido.");
            return;
        }

        // Configura e inicia o processamento
        configurarProgressBar();
        iniciarWorker(file);
    }

    private void configurarProgressBar() {
        progressBar.setIndeterminate(false); // Mude para modo determinado
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setString("Processando...");
        progressBar.setStringPainted(true);
    }

    private void finalizarProcessamento() {
        progressBar.setIndeterminate(false);
        progressBar.setValue(100);
        progressBar.setString("Processamento concluído!");

        try {
            worker.get(); // Aguarda o termino do SwingWorker
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            exibirMensagemErro("Erro durante o processamento.");
        }
    }

    private void exibirMensagemErro(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void salvarArquivo(SXSSFWorkbook workbook, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
            fos.flush();
        }
    }

    private void aplicarEstilos(Row outputRow) {
        Cell cellDuracao = outputRow.getCell(COLUNA_DURACAO);
        if (cellDuracao != null) {
            cellDuracao.setCellStyle(dateStyle);
        }

        Cell cellValor = outputRow.getCell(COLUNA_VALOR);
        if (cellValor != null) {
            cellValor.setCellStyle(accountingStyle);
        }
    }

    private Row criarCabecalho(SXSSFSheet outputSheet) {
        Row headerRow = outputSheet.createRow(0);
        String[] columnNames = {"Horário", "Setor", "Identificador", "Região", "Número_Destino", "Duração", "Valor"};

        for (int i = 0; i < columnNames.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnNames[i]);
            cell.setCellStyle(generalStyle);
        }
        return headerRow;
    }

    private void configurarEstilos(SXSSFWorkbook workbook) {
        generalStyle = workbook.createCellStyle();
        generalStyle.setDataFormat(workbook.createDataFormat().getFormat("General"));

        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("hh:mm:ss"));

        accountingStyle = workbook.createCellStyle();
        accountingStyle.setDataFormat(workbook.createDataFormat().getFormat("R$ 0.00"));
    }

    private void iniciarWorker(File file) {
        worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                FileInputStream fis = null;
                SXSSFWorkbook outputWorkbook = null;
                XSSFWorkbook workbookLeitura = null; // Workbook para leitura após a criação do arquivo

                try {
                    fis = new FileInputStream(file);
                    XSSFWorkbook workbook = new XSSFWorkbook(fis);
                    totalLinhas = calcularTotalLinhas(workbook);
                    File outputFile = new File(file.getParent(), "leitura_agitel.xlsx");

                    outputWorkbook = new SXSSFWorkbook(500);
                    configurarEstilos(outputWorkbook);
                    SXSSFSheet outputSheet = outputWorkbook.createSheet("Dados Copiados");
                    criarCabecalho(outputSheet);
                    processarAbas(workbook, outputWorkbook, outputSheet, outputFile);

                    removerLinhasVazias(outputWorkbook);
                    salvarArquivo(outputWorkbook, outputFile);

                    if (checkboxEqualizar.isSelected()) {
                        publish("Equalizando nomes, aguarde...");

                        try (FileInputStream inputStream = new FileInputStream(outputFile)) {
                            workbookLeitura = new XSSFWorkbook(inputStream);
                            equalizarColunaRegiao(workbookLeitura);

                            // Salva as alterações no arquivo
                            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                                workbookLeitura.write(outputStream);
                            }
                            // Fecha o inputStream após salvar as alterações
                            inputStream.close();
                        }

                        publish("Equalização concluída.");
                    } else {
                        System.out.println("Checkbox Equalizar não está marcada. Pulando equalização.");
                    }

                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                    if (outputWorkbook != null) {
                        outputWorkbook.dispose();
                    }
                    if (workbookLeitura != null) {
                        workbookLeitura.close();
                    }
                    System.gc();
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
                finalizarProcessamento();
                textAreaResultados.append("Processamento concluído.\n");
            }

            private void processarAbas(XSSFWorkbook workbook, SXSSFWorkbook outputWorkbook, SXSSFSheet outputSheet, File outputFile) throws Exception {
                int rowIndex = 0;
                int linhasProcessadas = 0;

                totalSheets = workbook.getNumberOfSheets();
                criarCabecalho(outputSheet);
                rowIndex++;

                DataFormatter formatter = new DataFormatter(); // Declare formatter here

                for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
                    XSSFSheet sheet = workbook.getSheetAt(i);
                    publish("Processando aba: " + sheet.getSheetName() + " (" + sheet.getLastRowNum() + " linhas)");

                    atualizarProgresso(i);
                    Row header = findHeaderRow(sheet);
                    if (header == null) continue;

                    int startRowIndex = header.getRowNum() + 1;

                    for (int rowIndexInSheet = startRowIndex; rowIndexInSheet <= sheet.getLastRowNum(); rowIndexInSheet++) {
                        Row row = sheet.getRow(rowIndexInSheet);
                        if (row != null && !isRowEmpty(row)) {
                            if (rowIndex >= 1048576) {
                                outputSheet = outputWorkbook.createSheet("Dados Copiados " + (outputWorkbook.getSheetIndex(outputSheet) + 2));
                                rowIndex = 0;
                            }

                            Row outputRow = outputSheet.createRow(rowIndex);
                            copyRow(row, outputRow);
                            aplicarEstilos(outputRow);

                            // Equalização da coluna 'Região'
                            if (checkboxEqualizar.isSelected()) {
                                Cell cellRegiao = outputRow.getCell(COLUNA_REGIAO);
                                if (cellRegiao != null) {
                                    String valorRegiao = formatter.formatCellValue(cellRegiao).trim();
                                    cellRegiao.setCellValue(equalizar_valor(valorRegiao));
                                }
                            }

                            rowIndex++;
                            linhasProcessadas++;
                        }
                    }
                }

                while (outputWorkbook.getNumberOfSheets() > 1) {
                    SXSSFSheet segundaSheet = outputWorkbook.getSheetAt(1);
                    if (segundaSheet.getLastRowNum() > 0) {
                        copiarDadosParaPrimeiraAba(outputWorkbook, segundaSheet, outputSheet);
                    }
                    outputWorkbook.removeSheetAt(1);
                }

                removerLinhasVazias(outputWorkbook);
                salvarArquivo(outputWorkbook, outputFile);
                publish("Transcrição dos dados concluída.");
            }

            private String equalizar_valor(String valor) {
                valor = valor.toLowerCase();
                if (valor.contains("fixo")) {
                    return "Fixo";
                } else if (valor.contains("movel")) {
                    return "Movel";
                } else if (valor.isBlank()) {
                    return "Intragrupo";
                } else {
                    return valor;
                }
            }

            private void equalizarColunaRegiao(XSSFWorkbook workbook) {
                XSSFSheet sheet = workbook.getSheetAt(0);
                System.out.println("Iniciando equalização da coluna 'Região'.");

                int lastRowNum = sheet.getLastRowNum();

                for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;

                    Cell cellRegiao = row.getCell(COLUNA_REGIAO);
                    if (cellRegiao == null) {
                        cellRegiao = row.createCell(COLUNA_REGIAO);
                    }

                    String valorRegiao = "";

                    DataFormatter formatter = new DataFormatter();
                    valorRegiao = formatter.formatCellValue(cellRegiao).trim().toLowerCase();

                    if (valorRegiao.contains("fixo")) {
                        cellRegiao.setCellValue("Fixo");
                    } else if (valorRegiao.contains("movel")) {
                        cellRegiao.setCellValue("Movel");
                    } else if (valorRegiao.isBlank()) {
                        cellRegiao.setCellValue("Intragrupo");
                    } else {
                        // Mantém o valor original se não for "fixo", "movel" ou vazio
                        cellRegiao.setCellValue(valorRegiao);
                    }

                    if (rowIndex % 1000 == 0) {
                        int progressoPercentual = (rowIndex * 100) / lastRowNum;
                        publish("Equalizando... " + progressoPercentual + "%");
                    }
                }
            }

            private void copiarDadosParaPrimeiraAba(SXSSFWorkbook outputWorkbook, SXSSFSheet segundaSheet, SXSSFSheet primeiraSheet) {
                int proximaLinhaVazia = encontrarProximaLinhaVazia(primeiraSheet);
                int rowIndexSegundaSheet = 1;

                while (rowIndexSegundaSheet <= segundaSheet.getLastRowNum()) {
                    Row row = segundaSheet.getRow(rowIndexSegundaSheet);
                    if (row != null) {
                        // Verifica se atingiu o limite de linhas da aba
                        if (proximaLinhaVazia >= 1048576) {
                            // Cria uma nova aba se o limite de linhas for atingido
                            primeiraSheet = outputWorkbook.createSheet("Dados Copiados " + (outputWorkbook.getNumberOfSheets() + 1));
                            proximaLinhaVazia = 0;
                            criarCabecalho(primeiraSheet); // Cria o cabeçalho na nova aba
                        }

                        // Cria uma nova linha apenas se não já foi gravada no disco
                        if (proximaLinhaVazia > primeiraSheet.getLastRowNum()) {
                            Row outputRow = primeiraSheet.createRow(proximaLinhaVazia++);
                            copyRow(row, outputRow);
                            aplicarEstilos(outputRow);
                        }
                    }
                    rowIndexSegundaSheet++;
                }
            }

            private int encontrarProximaLinhaVazia(SXSSFSheet sheet) {
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null || isRowEmpty(row)) {
                        return rowIndex;
                    }
                }
                return sheet.getLastRowNum() + 1;
            }

            private void atualizarProgresso(int abasProcessadas) {
                int progressoPercentual = (abasProcessadas * 100) / totalSheets;
                progressBar.setValue(progressoPercentual);
                progressBar.setString("Processando... " + progressoPercentual + "%");
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

    private void copyRow(Row sourceRow, Row outputRow) {
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            if (sourceCell != null) {
                Cell newCell = outputRow.createCell(i);
                copiarConteudoECelula(sourceCell, newCell);
            }
        }
    }

    private void copiarConteudoECelula(Cell sourceCell, Cell newCell) {
        switch (sourceCell.getCellType()) {
            case STRING:
                newCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(sourceCell.getNumericCellValue());
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    newCell.setCellStyle(dateStyle);
                } else {
                    newCell.setCellStyle(generalStyle);
                }
                break;
            case BOOLEAN:
                newCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(sourceCell.getCellFormula());
                break;
            default:
                newCell.setCellStyle(generalStyle);
        }
    }

    private int calcularTotalLinhas(XSSFWorkbook workbook) {
        return IntStream.range(0, workbook.getNumberOfSheets())
                .map(i -> workbook.getSheetAt(i).getLastRowNum())
                .sum();
    }

    private Row findHeaderRow(Sheet sheet) {
        for (Row row : sheet) {
            if (row != null && row.getCell(0) != null &&
                    "Data".equalsIgnoreCase(row.getCell(0).getStringCellValue())) {
                return row;
            }
        }
        return null;
    }

    private void removerLinhasVazias(SXSSFWorkbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            SXSSFSheet sheet = (SXSSFSheet) workbook.getSheetAt(i);
            for (int rowIndex = sheet.getLastRowNum(); rowIndex >= 0; rowIndex--) {
                Row row = sheet.getRow(rowIndex);
                if (row != null && (isRowEmpty(row))) {
                    sheet.removeRow(row);
                }
            }
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i <= 3; i++) { // Verifica as colunas A (0), B (1), C (2) e D (3)
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false; // Se qualquer celula dessas colunas tiver valor, a linha não está vazia
            }
        }
        return true; // Todas as celulas nas colunas A, B, C e D estão vazias
    }
}