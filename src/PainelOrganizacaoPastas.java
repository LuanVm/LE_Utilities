import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PainelOrganizacaoPastas {

    private JTextArea textAreaArquivos;

    public PainelOrganizacaoPastas(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
    }

    public JPanel criarPainel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Painel para os campos de entrada
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder("Configurações de Organização"));

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
        JButton buttonSelecionar = TelaPrincipal.criarBotao("Selecionar Pasta");
        inputPanel.add(buttonSelecionar, gbc);

        // Exceções
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        JLabel labelExcecoes = new JLabel("Exceções (separadas por vírgula):");
        inputPanel.add(labelExcecoes, gbc);

        gbc.gridy = 2;
        JTextField textExcecoes = new JTextField(20);
        inputPanel.add(textExcecoes, gbc);

        // Botão Organizar
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton buttonOrganizar = TelaPrincipal.criarBotao("Organizar");
        inputPanel.add(buttonOrganizar, gbc);

        // Painel para exibir o resultado (inicialmente vazio)
        JPanel painelResultado = new JPanel();
        painelResultado.setLayout(new BorderLayout());
        painelResultado.setBorder(new TitledBorder("Resultado da Organização"));

        // Adiciona os painéis à aba
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(painelResultado, BorderLayout.CENTER);

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
                    atualizarVisualizacaoArquivos(selectedFile); // Atualiza a textAreaArquivos
                }
            }
        });

        // Ação do botão Organizar
        buttonOrganizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pasta = textPasta.getText();
                String excecoesStr = textExcecoes.getText();
                List<String> excecoes = Arrays.asList(excecoesStr.split(","));

                File directory = new File(pasta);
                if (directory.exists() && directory.isDirectory()) {
                    organizarArquivos(directory, excecoes, painelResultado);
                } else {
                    JOptionPane.showMessageDialog(panel, "Pasta não encontrada ou inválida.");
                }
            }
        });

        return panel;
    }

    // Lógica para organizar os arquivos
    private void organizarArquivos(File directory, List<String> excecoes, JPanel painelResultado) {
        painelResultado.removeAll();

        File[] files = directory.listFiles();
        if (files != null) {
            Map<String, List<File>> clienteArquivos = new HashMap<>();

            for (File file : files) {
                if (file.isFile()) {
                    String nomeArquivo = file.getName();
                    String nomeCliente = extrairNomeCliente(nomeArquivo, excecoes);

                    if (!nomeCliente.isEmpty()) {
                        clienteArquivos.computeIfAbsent(nomeCliente, k -> new ArrayList<>()).add(file);
                    }
                }
            }

            for (Map.Entry<String, List<File>> entry : clienteArquivos.entrySet()) {
                if (entry.getValue().size() > 1) {
                    String nomePasta = entry.getKey();
                    File novaPasta = new File(directory, nomePasta);
                    if (novaPasta.mkdir()) {
                        for (File arquivo : entry.getValue()) {
                            try {
                                moverArquivo(arquivo, novaPasta);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(null, "Erro ao mover o arquivo: " + arquivo.getName() + " - " + ex.getMessage());
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Erro ao criar a pasta: " + nomePasta);
                    }
                }
            }

            atualizarVisualizacaoArquivos(directory);

            JLabel labelSucesso = new JLabel("Organização concluída com sucesso!", SwingConstants.CENTER);
            painelResultado.add(labelSucesso, BorderLayout.CENTER);
        } else {
            JLabel labelErro = new JLabel("Pasta não encontrada ou vazia.", SwingConstants.CENTER);
            painelResultado.add(labelErro, BorderLayout.CENTER);
        }

        painelResultado.revalidate();
        painelResultado.repaint();
    }

    // Função auxiliar para extrair o nome do cliente do nome do arquivo
    private String extrairNomeCliente(String nomeArquivo, List<String> excecoes) {
        String[] partes = nomeArquivo.split("_");

        if (excecoes.contains(partes[0])) {
            return "";
        }

        // O nome do cliente será a junção das partes até a penúltima parte do nome
        // Isso exclui a última parte que geralmente é específica para cada arquivo (por exemplo, "Junho_3022")
        StringBuilder nomeCliente = new StringBuilder(partes[0]);
        for (int i = 1; i < partes.length - 2; i++) {
            nomeCliente.append("_").append(partes[i]);
        }

        return nomeCliente.toString();
    }

    // Função auxiliar para mover um arquivo para uma pasta
    private static void moverArquivo(File arquivo, File pastaDestino) throws IOException {
        File novoArquivo = new File(pastaDestino, arquivo.getName());
        if (novoArquivo.exists()) {
            throw new IOException("Arquivo já existe na pasta de destino.");
        }
        if (!arquivo.renameTo(novoArquivo)) {
            throw new IOException("Falha ao mover o arquivo.");
        }
    }

    private void atualizarVisualizacaoArquivos(File directory) {
        File[] files = directory.listFiles();
        textAreaArquivos.setText("");
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(
                    File::getName));
            for (File file : files) {
                if (file.isFile()) {
                    textAreaArquivos.append(file.getName() + "\n");
                } else if (file.isDirectory()) {
                    textAreaArquivos.append("[" + file.getName() + "]\n");
                }
            }
        }
    }
}
