import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PainelOrganizacaoPastas {

    private JTextArea textAreaArquivos;
    private Map<File, File> historicoOrganizacao = new HashMap<>();
    private File directory;
    private int numSecoesNomeCliente = 2;
    private JLabel statusLabel;
    private boolean aguardandoConfirmacao = false;

    // Constantes para o número de seções
    private static final int MIN_SECOES_NOME_CLIENTE = 1;
    private static final int MAX_SECOES_NOME_CLIENTE = 3;

    private static final Logger LOGGER = Logger.getLogger(PainelOrganizacaoPastas.class.getName());

    private Map<String, List<File>> gerarPreVisualizacao(File directory, List<String> excecoes) {
        Map<String, List<File>> clienteArquivos = new HashMap<>();

        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                String nomeArquivo = file.getName();
                String nomeCliente = extrairNomeCliente(nomeArquivo, excecoes);

                if (!nomeCliente.isEmpty()) {
                    clienteArquivos.computeIfAbsent(nomeCliente, k -> new ArrayList<>()).add(file);
                }
            }
        }

        return clienteArquivos;
    }

    private void exibirPreVisualizacao(Map<String, List<File>> preVisualizacao, JTextArea textArea) {
        textArea.setText(""); // Clear previous content
        for (Map.Entry<String, List<File>> entry : preVisualizacao.entrySet()) {
            String nomePasta = entry.getKey();
            List<File> arquivosNaPasta = entry.getValue();

            textArea.append("[" + nomePasta + "]\n"); // Display the folder name
            for (File arquivo : arquivosNaPasta) {
                textArea.append("  - " + arquivo.getName() + "\n"); // Display the files within the folder
            }
        }
    }

    public PainelOrganizacaoPastas(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
        this.statusLabel = new JLabel("Pronto para organizar!");
    }

    public JPanel criarPainel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Painel para os campos de entrada (GridBagLayout)
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

        // Opções de configuração
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.setBorder(new TitledBorder("Opções"));
        JLabel labelNumSecoes = new JLabel("Número de seções para a organização de pastas:");
        configPanel.add(labelNumSecoes);
        JSpinner spinnerNumSecoes = new JSpinner(new SpinnerNumberModel(2, MIN_SECOES_NOME_CLIENTE, MAX_SECOES_NOME_CLIENTE, 1));
        spinnerNumSecoes.addChangeListener(e -> numSecoesNomeCliente = (int) spinnerNumSecoes.getValue());
        configPanel.add(spinnerNumSecoes);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        inputPanel.add(configPanel, gbc);

        // Botão Organizar
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton buttonOrganizar = TelaPrincipal.criarBotao("Organizar");
        inputPanel.add(buttonOrganizar, gbc);

        // Painel para exibir a área de texto dos arquivos e a pré-visualização
        JPanel painelInferior = new JPanel(new GridBagLayout()); // GridBagLayout para melhor controle do layout

        // Área de visualização de arquivos
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5; // Distribui o espaço horizontalmente
        gbc.weighty = 1.0; // Expande verticalmente para preencher o espaço
        gbc.fill = GridBagConstraints.BOTH;
        JPanel painelArquivos = new JPanel(new BorderLayout());
        painelArquivos.setBorder(new TitledBorder("Arquivos na Pasta"));
        textAreaArquivos = new JTextArea(10, 40);
        textAreaArquivos.setEditable(false);
        JScrollPane scrollPaneArquivos = new JScrollPane(textAreaArquivos);
        painelArquivos.add(scrollPaneArquivos, BorderLayout.CENTER);
        painelInferior.add(painelArquivos, gbc);

        // Painel para a pré-visualização
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel painelPreVisualizacao = new JPanel(new BorderLayout());
        painelPreVisualizacao.setBorder(new TitledBorder("Pré-visualização da Organização"));
        JTextArea textAreaPreVisualizacao = new JTextArea(10, 40);
        textAreaPreVisualizacao.setEditable(false);
        JScrollPane scrollPanePreVisualizacao = new JScrollPane(textAreaPreVisualizacao);
        painelPreVisualizacao.add(scrollPanePreVisualizacao, BorderLayout.CENTER);
        painelInferior.add(painelPreVisualizacao, gbc);

        // Adiciona os painéis à aba
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(painelInferior, BorderLayout.CENTER);

        // Painel para os botões e o status
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton buttonReverter = TelaPrincipal.criarBotao("Reverter");
        buttonPanel.add(buttonOrganizar);
        buttonPanel.add(buttonReverter);

        // Adiciona o JLabel de status abaixo dos botões
        buttonPanel.add(statusLabel);

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
                    atualizarVisualizacaoArquivos(selectedFile);
                    statusLabel.setText("Pronto para organizar!"); // Limpa o status ao selecionar uma nova pasta
                }
            }
        });

        // Ação do botão Organizar (modificado para usar confirmação no botão)
        buttonOrganizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pasta = textPasta.getText();
                String excecoesStr = textExcecoes.getText();
                List<String> excecoes = Arrays.asList(excecoesStr.split(","));

                directory = new File(pasta);
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();

                    // Validação da entrada
                    if (files == null || files.length == 0) {
                        statusLabel.setText("A pasta selecionada está vazia.");
                        return;
                    }

                    if (aguardandoConfirmacao) {
                        // Confirmação recebida, prossegue com a organização
                        JProgressBar progressBar = new JProgressBar(0, files.length);
                        progressBar.setStringPainted(true);

                        organizarArquivos(directory, excecoes, progressBar);

                        // Restaura o estado original dos botões e painéis
                        buttonOrganizar.setText("Organizar");
                        buttonReverter.setText("Reverter");
                        aguardandoConfirmacao = false;

                        // Atualiza a visualização de arquivos APÓS a organização
                        atualizarVisualizacaoArquivos(directory);

                        // Atualiza a mensagem de status
                        statusLabel.setText("Organização concluída com sucesso!");
                    } else {
                        // Primeira vez que o botão é clicado, gera a pré-visualização e pede confirmação
                        Map<String, List<File>> preVisualizacao = gerarPreVisualizacao(directory, excecoes);
                        exibirPreVisualizacao(preVisualizacao, textAreaArquivos); // Exibe no painel de arquivos

                        buttonOrganizar.setText("Confirmar Alt.");
                        buttonReverter.setText("Cancelar");
                        aguardandoConfirmacao = true;
                    }
                } else {
                    statusLabel.setText("Pasta não encontrada ou inválida.");
                }
            }
        });

        // Ação do botão Reverter
        buttonReverter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (aguardandoConfirmacao) {
                    // Cancela a organização pendente
                    buttonOrganizar.setText("Organizar");
                    buttonReverter.setText("Reverter");
                    aguardandoConfirmacao = false;

                    // Restaura a visualização original dos arquivos
                    atualizarVisualizacaoArquivos(directory);

                    // Limpa a pré-visualização
                    textAreaPreVisualizacao.setText("");

                    statusLabel.setText("Organização cancelada.");
                } else {
                    // Reverte a última organização (lógica existente)
                    if (directory != null && directory.exists()) {
                        reverterUltimaOrganizacao(directory);

                        // Atualiza a visualização de arquivos
                        atualizarVisualizacaoArquivos(directory);

                        // Atualiza a mensagem de status
                        statusLabel.setText("Reversão concluída com sucesso!");

                        // Limpa a pré-visualização
                        textAreaPreVisualizacao.setText("");
                    } else {
                        statusLabel.setText("Nenhuma organização realizada para reverter.");
                    }
                }
            }
        });

        return panel;
    }

    // Metodo para organizar arquivos em diretórios
    private void organizarArquivos(File directory, List<String> excecoes, JProgressBar progressBar) {
        Map<String, List<File>> clienteArquivos = gerarPreVisualizacao(directory, excecoes);
        historicoOrganizacao.clear();
        int pastasCriadas = 0;
        int arquivosMovidos = 0;
        int arquivosIgnorados = 0;

        for (Map.Entry<String, List<File>> entry : clienteArquivos.entrySet()) {
            String nomePasta = entry.getKey();
            File novaPasta = new File(directory, nomePasta);

            try {
                if (!novaPasta.exists() && !novaPasta.mkdir()) { // Verifica se a pasta já existe ou se pode ser criada
                    throw new IOException("Erro ao criar a pasta: " + nomePasta);
                }

                for (File arquivo : entry.getValue()) {
                    try {
                        File novoArquivo = new File(novaPasta, arquivo.getName());
                        Files.move(arquivo.toPath(), novoArquivo.toPath());
                        historicoOrganizacao.put(novoArquivo, arquivo);
                        arquivosMovidos++;
                    } catch (IOException e) {
                        // Tratamento de erro mais específico
                        String mensagemErro = "Erro ao mover o arquivo: " + arquivo.getName() + " para " + novaPasta.getAbsolutePath() + "\n" +
                                "Motivo: " + e.getMessage();
                        JOptionPane.showMessageDialog(null, mensagemErro, "Erro ao Mover Arquivo", JOptionPane.ERROR_MESSAGE);
                        LOGGER.log(Level.SEVERE, mensagemErro, e); // Log do erro
                        arquivosIgnorados++;
                    }
                }
            } catch (IOException ex) {
                // Tratamento de erro ao criar a pasta
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Erro ao Criar Pasta", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Erro ao criar a pasta: " + nomePasta, ex); // Log do erro
            }

            progressBar.setValue(progressBar.getValue() + entry.getValue().size());
        }

        // Atualiza a mensagem de status com o resultado da organização
        statusLabel.setText("Organização completa! Pastas criadas: " + pastasCriadas +
                ", Arquivos movidos: " + arquivosMovidos +
                ", Arquivos ignorados: " + arquivosIgnorados);
    }

    // Método para reverter a última organização realizada
    private void reverterUltimaOrganizacao(File directory) {
        for (Map.Entry<File, File> entry : historicoOrganizacao.entrySet()) {
            File arquivoNovo = entry.getKey();
            File arquivoAntigo = entry.getValue();

            try {
                Files.move(arquivoNovo.toPath(), arquivoAntigo.toPath());
                arquivoNovo.getParentFile().delete(); // Deleta a pasta se ela estiver vazia após mover o arquivo
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Atualiza a mensagem de status após a reversão
        statusLabel.setText("Reversão concluída com sucesso!");
        atualizarVisualizacaoArquivos(directory); // Atualiza a visualização após a reversão
    }

    // Atualiza a visualização dos arquivos na área de texto
    private void atualizarVisualizacaoArquivos(File pastaSelecionada) {
        StringBuilder conteudo = new StringBuilder();
        for (File file : Objects.requireNonNull(pastaSelecionada.listFiles())) {
            conteudo.append(file.getName()).append("\n");
        }
        textAreaArquivos.setText(conteudo.toString());

        // Adiciona uma mensagem de status indicando que a atualização foi concluída
        statusLabel.setText("Arquivos na pasta atualizados.");
    }

    // Extrai o nome do cliente com base no padrão estabelecido
    private String extrairNomeCliente(String nomeArquivo, List<String> excecoes) {
        String[] partes = nomeArquivo.split("_");
        StringBuilder nomeClienteBuilder = new StringBuilder();

        for (int i = 0; i < Math.min(partes.length, numSecoesNomeCliente); i++) {
            String parte = partes[i];
            if (!excecoes.contains(parte.toLowerCase())) {
                nomeClienteBuilder.append(parte).append("_");
            }
        }

        return nomeClienteBuilder.length() > 0 ? nomeClienteBuilder.substring(0, nomeClienteBuilder.length() - 1) : "";
    }
}