import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class PainelSubstituicaoSimples {

    private JTextArea textAreaArquivos;

    // Construtor que recebe a textAreaArquivos
    public PainelSubstituicaoSimples(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
    }

    public JPanel criarPainel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Painel para os campos de entrada
        JPanel inputPanel = criarPainelInput();
        JScrollPane scrollPaneArquivos = criarScrollPaneArquivos();

        // Adiciona os painéis à aba
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPaneArquivos, BorderLayout.CENTER);

        return panel;
    }

    private JPanel criarPainelInput() {
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
        JTextField textPasta = new JTextField(20);
        inputPanel.add(textPasta, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton buttonSelecionar = TelaPrincipal.criarBotao("Selecionar Pasta");
        inputPanel.add(buttonSelecionar, gbc);

        // Nome original
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel labelOriginal = new JLabel("Nome original:");
        inputPanel.add(labelOriginal, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField textOriginal = new JTextField(20);
        inputPanel.add(textOriginal, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        inputPanel.add(new JLabel(""), gbc);

        // Alterar para
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel labelNova = new JLabel("Alterar para:");
        inputPanel.add(labelNova, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField textNova = new JTextField(20);
        inputPanel.add(textNova, gbc);

        // Botão Renomear
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JButton buttonRenomear = TelaPrincipal.criarBotao("Renomear");
        inputPanel.add(buttonRenomear, gbc);

        // Label com a informação sobre caixa alta
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel labelCaseSensitive = new JLabel("Lembrando que a aplicação respeita caracteres em caixa alta.");
        labelCaseSensitive.setForeground(Color.GRAY);
        inputPanel.add(labelCaseSensitive, gbc);

        // Ação do botão Selecionar
        buttonSelecionar.addActionListener(e -> selecionarPasta(textPasta));

        // Ação do botão Renomear
        buttonRenomear.addActionListener(e -> renomearArquivos(textPasta, textOriginal, textNova, inputPanel));

        return inputPanel;
    }

    private JScrollPane criarScrollPaneArquivos() {
        textAreaArquivos = new JTextArea(10, 40);
        textAreaArquivos.setEditable(false);
        JScrollPane scrollPaneArquivos = new JScrollPane(textAreaArquivos);
        scrollPaneArquivos.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Arquivos na pasta", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 12)));
        return scrollPaneArquivos;
    }

    private void selecionarPasta(JTextField textPasta) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textPasta.setText(selectedFile.getAbsolutePath());
            atualizarVisualizacaoArquivos(selectedFile);
        }
    }

    private void renomearArquivos(JTextField textPasta, JTextField textOriginal, JTextField textNova, JPanel inputPanel) {
        String pasta = textPasta.getText();
        String palavraNova = textNova.getText();
        String palavraAntiga = textOriginal.getText();

        File directory = new File(pasta);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String nomeArquivo = file.getName();
                    String novoNome = nomeArquivo.replace(palavraAntiga, palavraNova);

                    File novoArquivo = new File(directory, novoNome);
                    if (!file.renameTo(novoArquivo)) {
                        exibirMensagemErro(inputPanel, "Erro ao renomear: " + nomeArquivo);
                    }
                }
            }
            exibirMensagemSucesso(inputPanel, "Renomeação concluída!");
            atualizarVisualizacaoArquivos(directory);
        } else {
            exibirMensagemErro(inputPanel, "Pasta não encontrada ou vazia.");
        }
    }

    private void exibirMensagemErro(Component parentComponent, String mensagem) {
        JOptionPane.showMessageDialog(parentComponent, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void exibirMensagemSucesso(Component parentComponent, String mensagem) {
        JOptionPane.showMessageDialog(parentComponent, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void atualizarVisualizacaoArquivos(File directory) {
        File[] files = directory.listFiles();
        textAreaArquivos.setText("");
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
