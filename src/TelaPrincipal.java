import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.intellijthemes.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.imageio.ImageIO;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TelaPrincipal {

    private static JTextArea textAreaArquivos; // Área de texto compartilhada
    private static JPanel painelConteudo; // Painel para exibir o conteúdo das opções
    private static JPanel panelConfiguracoes;
    private static JTabbedPane nomenclaturaTabbedPane;
    private static JFrame frame;
    private static CardLayout cardLayout;
    private static final String CONFIG_FILE = "config.properties";
    private static JRadioButton radioButtonFlatArcDarkOrangeIJTheme;
    private static JRadioButton radioButtonFlatDarkPurpleIJTheme;
    private static JRadioButton radioButtonFlatLafIntelliJ;
    private static JRadioButton radioButtonFlatCarbonIJTheme;
    private static JRadioButton radioButtonFlatMonokaiProIJTheme;
    private static JRadioButton radioButtonFlatGruvboxDarkHardIJTheme;
    private static JRadioButton radioButtonFlatHiDPIDarkIJTheme;

    public static void main(String[] args) {

        // Carrega as configurações ao iniciar
        loadConfig();

        // Tela de loading
        JPanel splashPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(0x4CAF50), // Verde claro
                        0, getHeight(), new Color(0x2E8B57)); // Verde escuro
                g2.setPaint(gp);

                int[] xPoints = {0, getWidth() / 2, getWidth()};
                int[] yPoints = {0, getHeight(), 0};
                g2.fillPolygon(xPoints, yPoints, 3);

                super.paintComponent(g);
            }
        };

        JLabel loadingLabel = new JLabel("Carregando...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Open Sans", Font.BOLD, 14));
        loadingLabel.setForeground(Color.WHITE);

        splashPanel.setLayout(new BorderLayout());
        splashPanel.add(loadingLabel, BorderLayout.CENTER);

        JWindow splashScreen = new JWindow();
        splashScreen.getContentPane().add(splashPanel);
        splashScreen.setSize(140, 80);
        splashScreen.setLocationRelativeTo(null);
        splashScreen.setVisible(true);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        frame = new JFrame("Livre Escolha - Utilities");

        try {
            BufferedImage originalImage = ImageIO.read(new File("resources/logo.png"));
            int newWidth = 16;
            int newHeight = 16;
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

            resizedImage.getGraphics().drawImage(scaledImage,
                    0, 0, null);
            frame.setIconImage(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1280,
                720));

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                frame.revalidate();
                frame.repaint();
            }
        });

        splashScreen.dispose();
        frame.setVisible(true);

        // Usa um Timer para forçar o redimensionamento com um pequeno delay
        Timer resizeTimer = new Timer(100, new ActionListener() { // Delay de 100 milissegundos
            @Override
            public void actionPerformed(ActionEvent e) {
                Dimension size = frame.getSize();
                frame.setSize(size.width + 1, size.height + 1);
                frame.setSize(size);
                ((Timer) e.getSource()).stop(); // Para o timer após o redimensionamento
            }
        });
        resizeTimer.setRepeats(false); // Executa apenas uma vez
        resizeTimer.start();

        textAreaArquivos = new JTextArea(10, 40);
        textAreaArquivos.setEditable(false);

        // Cria um JTabbedPane para as abas principais
        JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.LEFT); // Coloca as abas na esquerda
        UIManager.put("TabbedPane.selected", new Color(0xEB5E28));
        //UIManager.put("TabbedPane.background", new Color(0x333333));
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10)); // Aumenta o espaçamento das abas
        UIManager.put("TabbedPane.font", new Font("Arial", Font.PLAIN, 12)); // Define a fonte

        // Cria a aba "Nomenclatura de arquivos"
        JPanel panelNomenclatura = criarAba("Nomenclatura de arquivos", mainTabbedPane);
        panelNomenclatura.setLayout(new GridBagLayout());
        panelNomenclatura.setBorder(new EmptyBorder(20, 20, 20, 20));
        panelNomenclatura.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10), // Espaçamento interno
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY) // Sombra na parte inferior
        ));

        // Cria o painel de conteúdo e configura o CardLayout
        painelConteudo = new JPanel();
        painelConteudo.setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        painelConteudo.setLayout(cardLayout);

        // Cria o JTabbedPane para as sub-abas
        nomenclaturaTabbedPane = new JTabbedPane();
        UIManager.put("TabbedPane.selected", new Color(0xEB5E28));

        // Cria os painéis das opções e adiciona ao painel de conteúdo
        JPanel panelSubstituicaoSimples = new PainelSubstituicaoSimples(textAreaArquivos).criarPainel();
        JPanel panelRenomearOrdenar = new PainelRenomearOrdenar(textAreaArquivos).criarPainel();
        nomenclaturaTabbedPane.addTab("Substituição Simples", panelSubstituicaoSimples);
        nomenclaturaTabbedPane.addTab("Renomear e ordenar", panelRenomearOrdenar);

        // Configura o GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        panelNomenclatura.add(nomenclaturaTabbedPane, gbc);

        // Cria a aba "Organização de pastas"
        JPanel panelOrganizacaoPastas = new PainelOrganizacaoPastas(textAreaArquivos).criarPainel();
        mainTabbedPane.addTab("Organização de pastas", panelOrganizacaoPastas);

        frame.add(mainTabbedPane, BorderLayout.CENTER);

        JButton botaoConfiguracoesTema = new JButton("");

        panelConfiguracoes = new JPanel();
        panelConfiguracoes.setLayout(new BorderLayout());
        panelConfiguracoes.setBorder(new EmptyBorder(20, 20, 20, 20));
        configurarAbaConfiguracoes(panelConfiguracoes);

        // Cria o JDialog para as configurações
        JDialog dialogConfiguracoes = new JDialog(frame, "Configurações", true);
        dialogConfiguracoes.setSize(400, 300);
        dialogConfiguracoes.setLocationRelativeTo(frame);
        dialogConfiguracoes.getContentPane().add(panelConfiguracoes);

        // Cria o botão para abrir as configurações de tema
        JButton botaoTema = criarBotao("Tema");
        botaoTema.setText("Temas");

        botaoTema.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogConfiguracoes.setVisible(true);
            }
        });

        JPanel rodapePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodapePanel.add(botaoTema);

        frame.add(rodapePanel, BorderLayout.SOUTH);

        // Inicializa o painel de conteúdo com a primeira opção
        cardLayout.show(painelConteudo, "Substituição Simples");

        frame.pack();
    }

    // Metodo para criar botões com o preset de design
    public static JButton criarBotao(String texto) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(getBackground().darker());
                } else {
                    g.setColor(getBackground());
                }
                g.fillRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 4, 4);

                super.paintComponent(g);
            }

            @Override
            public void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (isFocusOwner()) {
                    g2.setColor(Color.DARK_GRAY);

                    g2.drawRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 2, 2); // Cantos menos arredondados
                }
            }
        };

        botao.setBackground(new Color(0xFF8343));
        botao.setForeground(Color.WHITE);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setContentAreaFilled(false);

        botao.setFont(new Font("Open Sans", Font.PLAIN, 12));

        botao.setMargin(new Insets(2, 2, 2, 2));
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createLineBorder(Color.GRAY, 1, true)
        ));

        return botao;
    }

    private static JPanel criarAba(String titulo, JTabbedPane tabbedPane) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab(titulo, panel); // Usa o título fornecido como argumento
        return panel;
    }

    // Atualiza o painel de conteúdo com base na opção selecionada
    private static void atualizarPainelConteudo(String opcaoSelecionada) {
        painelConteudo.removeAll();

        if (opcaoSelecionada.equals("Substituição Simples")) {
            painelConteudo.add(new PainelSubstituicaoSimples(textAreaArquivos).criarPainel(), BorderLayout.CENTER);
        } else if (opcaoSelecionada.equals("Renomear e ordenar")) {
            painelConteudo.add(new PainelRenomearOrdenar(textAreaArquivos).criarPainel(), BorderLayout.CENTER);
        }

        cardLayout.show(painelConteudo, opcaoSelecionada); // Usa o cardLayout da classe

        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    // Função auxiliar para obter a extensão de um arquivo
    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot > 0) {
            return fileName.substring(lastIndexOfDot);
        } else {
            return "";
        }
    }

    private static void configurarAbaConfiguracoes(JPanel panel) {
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Painel para as opções de tema
        JPanel temaPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        temaPanel.setBorder(new TitledBorder("Tema da Interface"));

        // Adiciona as opções de tema ao painel
        ButtonGroup temaButtonGroup = new ButtonGroup();
        radioButtonFlatArcDarkOrangeIJTheme = new JRadioButton("Flat Dark Orange", true); // Selecionado por padrão
        temaButtonGroup.add(radioButtonFlatArcDarkOrangeIJTheme);
        temaPanel.add(radioButtonFlatArcDarkOrangeIJTheme);

        radioButtonFlatDarkPurpleIJTheme = new JRadioButton("Flat Dark Purple");
        temaButtonGroup.add(radioButtonFlatDarkPurpleIJTheme);
        temaPanel.add(radioButtonFlatDarkPurpleIJTheme);

        radioButtonFlatLafIntelliJ = new JRadioButton("FlatLaf IntelliJ");
        temaButtonGroup.add(radioButtonFlatLafIntelliJ);
        temaPanel.add(radioButtonFlatLafIntelliJ);

        radioButtonFlatCarbonIJTheme = new JRadioButton("Flat Carbon");
        temaButtonGroup.add(radioButtonFlatCarbonIJTheme);
        temaPanel.add(radioButtonFlatCarbonIJTheme);

        radioButtonFlatMonokaiProIJTheme = new JRadioButton("Flat Monokai");
        temaButtonGroup.add(radioButtonFlatMonokaiProIJTheme);
        temaPanel.add(radioButtonFlatMonokaiProIJTheme);

        radioButtonFlatGruvboxDarkHardIJTheme = new JRadioButton("Flat Gruvbox Dark Hard");
        temaButtonGroup.add(radioButtonFlatGruvboxDarkHardIJTheme);
        temaPanel.add(radioButtonFlatGruvboxDarkHardIJTheme);

        radioButtonFlatHiDPIDarkIJTheme = new JRadioButton("Flat HiDPI Dark");
        temaButtonGroup.add(radioButtonFlatHiDPIDarkIJTheme);
        temaPanel.add(radioButtonFlatHiDPIDarkIJTheme);

        // Adiciona o painel de tema à aba
        panel.add(temaPanel, BorderLayout.NORTH);

        // Ação ao selecionar um tema
        ActionListener temaActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (radioButtonFlatArcDarkOrangeIJTheme.isSelected()) {
                        UIManager.setLookAndFeel(new FlatArcDarkOrangeIJTheme());
                    } else if (radioButtonFlatDarkPurpleIJTheme.isSelected()) {
                        UIManager.setLookAndFeel(new FlatDarkPurpleIJTheme());
                    } else if (radioButtonFlatLafIntelliJ.isSelected()) {
                        UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    } else if (radioButtonFlatCarbonIJTheme.isSelected()) {
                        UIManager.setLookAndFeel(new FlatCarbonIJTheme());
                    } else if (radioButtonFlatMonokaiProIJTheme.isSelected()) {
                        UIManager.setLookAndFeel(new FlatMonokaiProIJTheme());
                    } else if (radioButtonFlatGruvboxDarkHardIJTheme.isSelected()) {
                        UIManager.setLookAndFeel(new FlatGruvboxDarkHardIJTheme());
                    } else if (radioButtonFlatHiDPIDarkIJTheme.isSelected()) {
                        UIManager.setLookAndFeel(new FlatHiberbeeDarkIJTheme());
                    }
                    SwingUtilities.updateComponentTreeUI(frame);
                } catch (UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }
        };

        radioButtonFlatArcDarkOrangeIJTheme.addActionListener(temaActionListener);
        radioButtonFlatDarkPurpleIJTheme.addActionListener(temaActionListener);
        radioButtonFlatLafIntelliJ.addActionListener(temaActionListener);
        radioButtonFlatCarbonIJTheme.addActionListener(temaActionListener);
        radioButtonFlatMonokaiProIJTheme.addActionListener(temaActionListener);
        radioButtonFlatGruvboxDarkHardIJTheme.addActionListener(temaActionListener);
        radioButtonFlatHiDPIDarkIJTheme.addActionListener(temaActionListener);

        // Botão Salvar
        JButton buttonSalvar = criarBotao("Salvar");
        temaPanel.add(buttonSalvar);

        buttonSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveConfig();
            }
        });
    }

    private static void loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);

            String tema = prop.getProperty("tema", "Flat Dark Orange");

            try {
                switch (tema) {
                    case "FlatLaf IntelliJ" -> UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    case "Flat Dark Purple" -> UIManager.setLookAndFeel(new FlatDarkPurpleIJTheme());
                    case "Flat Carbon" -> UIManager.setLookAndFeel(new FlatCarbonIJTheme());
                    case "Flat Dark Orange" -> UIManager.setLookAndFeel(new FlatArcDarkOrangeIJTheme());
                    case "Flat Monokai" -> UIManager.setLookAndFeel(new FlatMonokaiProIJTheme());
                    case "Flat Gruvbox Dark Hard" -> UIManager.setLookAndFeel(new FlatGruvboxDarkHardIJTheme());
                    case "Flat HiDPI Dark" -> UIManager.setLookAndFeel(new FlatHiberbeeDarkIJTheme());
                }
            } catch (UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();

            String tema = "Flat Dark Orange";
            if (radioButtonFlatLafIntelliJ.isSelected()) {
                tema = "FlatLaf IntelliJ";
            } else if (radioButtonFlatDarkPurpleIJTheme.isSelected()) {
                tema = "Flat Dark Purple";
            } else if (radioButtonFlatCarbonIJTheme.isSelected()) {
                tema = "Flat Carbon";
            } else if (radioButtonFlatMonokaiProIJTheme.isSelected()) {
                tema = "Flat Monokai";
            } else if (radioButtonFlatGruvboxDarkHardIJTheme.isSelected()) {
                tema = "Flat Gruvbox Dark Hard";
            } else if (radioButtonFlatHiDPIDarkIJTheme.isSelected()) {
                tema = "Flat HiDPI Dark";
            }


            prop.setProperty("tema", tema);
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}