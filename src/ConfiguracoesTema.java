import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfiguracoesTema {

    private static final String CONFIG_FILE = ".my-app-config/config.properties";
    private static final String DEFAULT_THEME_NAME = "Flat Dark Orange";
    private static final Logger LOGGER = Logger.getLogger(ConfiguracoesTema.class.getName());

    private JPanel panelConfiguracoes;
    private ButtonGroup temaButtonGroup;
    private JTextPane infoLabel;

    public ConfiguracoesTema() {
        panelConfiguracoes = new JPanel(new BorderLayout());
        panelConfiguracoes.setBorder(new EmptyBorder(20, 20, 20, 20));
        configurarPainelConfiguracoes();
    }

    public JPanel getPainelConfiguracoes() {
        return panelConfiguracoes;
    }

    private void configurarPainelConfiguracoes() {
        JPanel temaPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        temaPanel.setBorder(new TitledBorder("Tema da Interface"));

        temaButtonGroup = new ButtonGroup();
        ActionListener temaActionListener = e -> {
            JRadioButton source = (JRadioButton) e.getSource();
            if (source.isSelected()) {
                Tema temaSelecionado = Tema.fromName(source.getText());
                atualizarTema(temaSelecionado.getLookAndFeel());
            }
        };

        for (Tema tema : Tema.values()) {
            createRadioButton(tema.getName(), tema == Tema.FLAT_DARK_ORANGE, temaButtonGroup, temaPanel, temaActionListener);
        }

        JButton buttonSalvar = new JButton("Salvar");
        buttonSalvar.addActionListener(e -> saveConfig());
        temaPanel.add(buttonSalvar);

        panelConfiguracoes.add(temaPanel, BorderLayout.NORTH);
    }

    private JRadioButton createRadioButton(String text, boolean selected, ButtonGroup group, JPanel panel, ActionListener listener) {
        JRadioButton radioButton = new JRadioButton(text, selected);
        group.add(radioButton);
        panel.add(radioButton);
        radioButton.addActionListener(listener);
        return radioButton;
    }

    private void atualizarTema(LookAndFeel lookAndFeel) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(lookAndFeel);
                FlatLaf.updateUI();
                SwingUtilities.updateComponentTreeUI(panelConfiguracoes);
                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                }
            } catch (UnsupportedLookAndFeelException e) {
                LOGGER.log(Level.SEVERE, "Erro ao definir o tema", e);
            }
        });
    }


    private void saveConfig() {
        File configFile = new File(System.getProperty("user.home"), CONFIG_FILE);

        try {
            if (!configFile.getParentFile().exists() && !configFile.getParentFile().mkdirs()) {
                throw new IOException("Erro ao criar o diretório de configuração.");
            }

            try (OutputStream output = new FileOutputStream(configFile)) {
                Properties prop = new Properties();
                Tema temaSelecionado = getTemaSelecionado();
                prop.setProperty("tema", temaSelecionado.getName());
                prop.store(output, null);
            }
        } catch (IOException io) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar a configuração", io);
        }
    }

    private Tema getTemaSelecionado() {
        for (Enumeration<AbstractButton> buttons = temaButtonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return Tema.fromName(button.getText());
            }
        }
        return Tema.FLAT_DARK_ORANGE; // Default
    }

    public static void loadConfig() {
        File configFile = new File(System.getProperty("user.home"), CONFIG_FILE);

        if (!configFile.exists()) {
            criarArquivoConfiguracaoPadrao(configFile);
            return;
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);

            String temaName = prop.getProperty("tema", DEFAULT_THEME_NAME);
            Tema tema = Tema.fromName(temaName);

            try {
                UIManager.setLookAndFeel(tema.getLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
                LOGGER.log(Level.SEVERE, "Erro ao carregar o tema", ex);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar o arquivo de configuração", ex);
        }
    }

    private static void criarArquivoConfiguracaoPadrao(File configFile) {
        LOGGER.info("Arquivo de configuração não encontrado: " + configFile.getAbsolutePath());
        try {
            if (configFile.getParentFile() != null && configFile.getParentFile().mkdirs()) {
                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write("tema=" + DEFAULT_THEME_NAME);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar arquivo de configuração padrão", e);
        }
    }

    enum Tema {
        FLAT_DARK_ORANGE("Flat Dark Orange", new FlatArcDarkOrangeIJTheme()),
        FLAT_DARK_PURPLE("Flat Dark Purple", new FlatDarkPurpleIJTheme()),
        FLAT_INTELLIJ("FlatLaf IntelliJ (Light)", new FlatIntelliJLaf()),
        FLAT_CARBON("Flat Carbon", new FlatCarbonIJTheme()),
        FLAT_MONOKAI_PRO("Flat Monokai Pro", new FlatMonokaiProIJTheme()),
        FLAT_GRUVBOX("Flat Gruvbox Dark Hard", new FlatGruvboxDarkHardIJTheme()),
        FLAT_HIDPI("Flat Hiberbee Dark", new FlatHiberbeeDarkIJTheme());

        private static final Map<String, Tema> NAME_TO_ENUM;

        static {
            NAME_TO_ENUM = new HashMap<>();
            for (Tema tema : values()) {
                NAME_TO_ENUM.put(tema.name, tema);
            }
        }

        private final String name;
        private final LookAndFeel lookAndFeel;

        Tema(String name, LookAndFeel lookAndFeel) {
            this.name = name;
            this.lookAndFeel = lookAndFeel;
        }

        public String getName() {
            return name;
        }

        public LookAndFeel getLookAndFeel() {
            return lookAndFeel;
        }

        public static Tema fromName(String name) {
            return NAME_TO_ENUM.getOrDefault(name, FLAT_DARK_ORANGE); // Default
        }
    }
}
