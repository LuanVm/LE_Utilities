import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class PainelRenomearOrdenar {

    private static JTextArea textAreaArquivos;
    private DefaultTableModel modeloTabela;

    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    public PainelRenomearOrdenar(JTextArea textAreaArquivos) {
        PainelRenomearOrdenar.textAreaArquivos = textAreaArquivos;
    }

    public JPanel criarPainel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Configurações de Renomeação", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 12)));

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
        final JTextField textPasta = new JTextField(20);
        inputPanel.add(textPasta, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton buttonSelecionar = TelaPrincipal.criarBotao("Selecionar Pasta");
        inputPanel.add(buttonSelecionar, gbc);

        // Novo nome base para os arquivos
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel labelNovoNome = new JLabel("Alterar para:");
        inputPanel.add(labelNovoNome, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        final JTextField textNovoNome = new JTextField(20);
        inputPanel.add(textNovoNome, gbc);

        // Tabela de arquivos
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        String[] colunas = {"Nome do Arquivo", "Ordem"};
        modeloTabela = new DefaultTableModel(colunas, 0);
        JTable tabelaArquivos = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaArquivos);
        scrollPane.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Arquivos na pasta", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 12)));

        // Painel para os botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton buttonRenomear = TelaPrincipal.criarBotao("Renomear");
        JButton buttonLimpar = TelaPrincipal.criarBotao("Limpar Ordem");
        buttonPanel.add(buttonRenomear);
        buttonPanel.add(buttonLimpar);

        // Adiciona os painéis à aba
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Variáveis para controlar o arrasto do mouse (agora usando AtomicInteger)
        final AtomicInteger startRow = new AtomicInteger(-1);
        final AtomicInteger startValue = new AtomicInteger(-1);

        // MouseListener para detectar o início do arrasto
        tabelaArquivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tabelaArquivos.rowAtPoint(e.getPoint());
                int column = tabelaArquivos.columnAtPoint(e.getPoint());

                if (column == 1 && SwingUtilities.isLeftMouseButton(e)) {
                    startRow.set(row);
                    try {
                        startValue.set(Integer.parseInt((String) modeloTabela.getValueAt(row, column)));
                    } catch (NumberFormatException ex) {
                        startValue.set(-1); // Valor inválido, ignora o arrasto
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                startRow.set(-1);
                startValue.set(-1);
            }
        });

        // MouseMotionListener para detectar o arrasto do mouse
        tabelaArquivos.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (startRow.get() != -1 && startValue.get() != -1) {
                    int row = tabelaArquivos.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < modeloTabela.getRowCount()) {
                        int incremento = 1; // Sempre soma, mesmo arrastando para cima

                        int valor;
                        int inicio, fim;
                        if (row > startRow.get()) { // Arrastar para baixo
                            valor = startValue.get();
                            inicio = startRow.get();
                            fim = row + 1;
                        } else { // Arrastar para cima
                            valor = startValue.get() + (startRow.get() - row);
                            inicio = row;
                            fim = startRow.get() + 1;
                        }

                        for (int i = inicio; i != fim && i < modeloTabela.getRowCount(); i += incremento) {
                            String valorOriginalStr = (String) modeloTabela.getValueAt(i, 1);
                            int valorOriginal = valorOriginalStr.isEmpty() ? -1 : Integer.parseInt(valorOriginalStr);

                            // Altera o valor da célula apenas se:
                            // - Ela estiver vazia
                            // - Ou se o novo valor for maior que o original E a célula não tiver sido alterada manualmente
                            if (valorOriginalStr.isEmpty() || (valor > valorOriginal && valorOriginal == -1)) {
                                // Garante que o valor seja positivo
                                int novoValor = Math.max(0, valor);
                                modeloTabela.setValueAt(String.valueOf(novoValor), i, 1);
                            }
                            valor += incremento;
                        }
                    }
                }
            }
        });

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
                    atualizarVisualizacaoArquivos(selectedFile); // Atualiza a textAreaArquivos
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
                String pasta = textPasta.getText();
                String novoNomeBase = textNovoNome.getText();

                if (pasta.isEmpty() || novoNomeBase.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Por favor, preencha todos os campos.");
                    return;
                }

                File directory = new File(pasta);
                File[] files = directory.listFiles();

                if (files != null) {
                    Arrays.sort(files, Comparator.comparing(File::getName));
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isFile()) {
                            String nomeArquivo = files[i].getName();
                            String ordem = (String) modeloTabela.getValueAt(i, 1);

                            if (ordem.isEmpty()) {
                                JOptionPane.showMessageDialog(panel, "Por favor, preencha a coluna 'Ordem' para todos os arquivos.");
                                return;
                            }

                            try {
                                int numeroOrdem = Integer.parseInt(ordem);

                                // Ajusta o formato de numeração com base no valor da ordem
                                String formatoNumeracao = (numeroOrdem < 100) ? "%02d" : "%03d";
                                String novoNome = String.format("%s%s%s", novoNomeBase, String.format(formatoNumeracao, numeroOrdem), getFileExtension(nomeArquivo));

                                File novoArquivo = new File(directory, novoNome);
                                if (!files[i].renameTo(novoArquivo)) {
                                    JOptionPane.showMessageDialog(panel, "Erro ao renomear: " + nomeArquivo);
                                } else {
                                    modeloTabela.setValueAt(novoNome, i, 0); // Atualiza a tabela com o novo nome
                                }

                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(panel, "Erro na numeração para o arquivo: " + nomeArquivo);
                                return;
                            }
                        }
                    }
                }

                // Atualiza a visualização dos arquivos renomeados na textAreaArquivos
                atualizarVisualizacaoArquivos(directory);
            }
        });

        // Ação do botão Limpar Ordem
        buttonLimpar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                    modeloTabela.setValueAt("", i, 1);
                }
            }
        });

        return panel;
    }

    // Metodo para atualizar a tabela de arquivos
    private void atualizarTabelaArquivos(File directory, DefaultTableModel modeloTabela) {
        modeloTabela.setRowCount(0); // Limpa a tabela existente
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

    // Metodo para atualizar a visualização dos arquivos na textAreaArquivos
    private void atualizarVisualizacaoArquivos(File directory) {
        textAreaArquivos.setText(""); // Limpa o conteúdo existente
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                if (file.isFile()) {
                    textAreaArquivos.append(file.getName() + "\n");
                }
            }
        }
    }
}
