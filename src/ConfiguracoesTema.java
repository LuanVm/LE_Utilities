import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

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
    private static final Tema DEFAULT_TEMA = Tema.FLAT_MAC_LIGHT; // Define o tema padrão
    private static final Logger LOGGER = Logger.getLogger(ConfiguracoesTema.class.getName());

    private JPanel panelConfiguracoes;
    private ButtonGroup temaButtonGroup;

    public ConfiguracoesTema() {
        panelConfiguracoes = new JPanel(new BorderLayout(10, 10));
        panelConfiguracoes.setBorder(new EmptyBorder(20, 20, 20, 20));
        configurarPainelConfiguracoes();
    }

    public JPanel getPainelConfiguracoes() {
        return panelConfiguracoes;
    }

    private void configurarPainelConfiguracoes() {
        JPanel temaDarkPanel = criarPainelDeTemas("Temas Escuros", true);
        JPanel temaLightPanel = criarPainelDeTemas("Temas Claros", false);

        // Painel principal contendo os temas e o botão salvar
        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 10, 10));
        panelCentro.add(temaDarkPanel);
        panelCentro.add(temaLightPanel);

        panelConfiguracoes.add(new JLabel("Selecione o tema:"), BorderLayout.NORTH);
        panelConfiguracoes.add(panelCentro, BorderLayout.CENTER);

        JButton buttonSalvar = new JButton("Salvar");
        buttonSalvar.addActionListener(e -> saveConfig());
        panelConfiguracoes.add(buttonSalvar, BorderLayout.SOUTH);
    }

    private JPanel criarPainelDeTemas(String titulo, boolean isDark) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(new TitledBorder(titulo));

        if (temaButtonGroup == null) {
            temaButtonGroup = new ButtonGroup(); // Garantir que há apenas um grupo de botões para todos os temas
        }

        ActionListener temaActionListener = e -> {
            JRadioButton source = (JRadioButton) e.getSource();
            Tema temaSelecionado = Tema.fromName(source.getText());
            atualizarTema(temaSelecionado.getLookAndFeel());
        };

        // Adiciona os temas ao painel
        for (Tema tema : Tema.values()) {
            if (tema.isDark() == isDark) {
                JRadioButton radioButton = new JRadioButton(tema.getName());
                if (tema == DEFAULT_TEMA) {
                    radioButton.setSelected(true); // Tema padrão inicial
                }
                radioButton.addActionListener(temaActionListener);
                temaButtonGroup.add(radioButton); // Adiciona ao grupo global
                panel.add(radioButton);
            }
        }

        return panel;
    }

    private void atualizarTema(LookAndFeel lookAndFeel) {
        SwingUtilities.invokeLater(() -> {
            try {
                LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
                String newThemeName = lookAndFeel.getClass().getName();

                if (currentLookAndFeel == null || !newThemeName.equals(currentLookAndFeel.getClass().getName())) {
                    LOGGER.info("Aplicando tema: " + newThemeName);

                    // Aplicar o novo LookAndFeel diretamente
                    UIManager.setLookAndFeel(lookAndFeel);
                    FlatLaf.updateUI();

                    // Força a atualização de todos os componentes e janelas
                    for (Window window : Window.getWindows()) {
                        SwingUtilities.updateComponentTreeUI(window);
                    }

                    // Atualiza o painel de configurações
                    panelConfiguracoes.revalidate();
                    panelConfiguracoes.repaint();
                }
            } catch (UnsupportedLookAndFeelException e) {
                LOGGER.log(Level.SEVERE, "Erro ao definir o tema: " + lookAndFeel.getClass().getName(), e);
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
        return DEFAULT_TEMA; // Default
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

            String temaName = prop.getProperty("tema", DEFAULT_TEMA.getName());
            Tema tema = Tema.fromName(temaName);

            try {
                UIManager.setLookAndFeel(tema.getLookAndFeel());
                FlatLaf.updateUI();
                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                }
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
                    writer.write("tema=" + DEFAULT_TEMA.getName());
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar arquivo de configuração padrão", e);
        }
    }

    enum Tema {
        FLAT_DARK_ORANGE("Flat Dark Orange", new FlatArcDarkOrangeIJTheme(), true),
        FLAT_DARK_PURPLE("Flat Dark Purple", new FlatDarkPurpleIJTheme(), true),
        FLAT_CARBON("Flat Carbon", new FlatCarbonIJTheme(), true),
        FLAT_GITHUB_DARK("Flat GitHub Dark", new FlatGitHubDarkIJTheme(), true),
        FLAT_ONE_DARK("Flat One Dark", new FlatOneDarkIJTheme(), true),
        FLAT_SOLARIZED_DARK("Flat Solarized Dark", new FlatSolarizedDarkIJTheme(), true),
        FLAT_DRACULA("Flat Dracula", new FlatDraculaIJTheme(), true),

        // Temas light
        FLAT_MAC_LIGHT("Flat Mac Light (Padrão)", new FlatMacLightLaf(), false),
        FLAT_INTELLIJ("FlatLaf IntelliJ (Light)", new FlatIntelliJLaf(), false),
        FLAT_LIGHT_LAF("Flat Light Laf", new FlatLightLaf(), false),
        FLAT_MATERIAL_LIGHTER("Flat Material Lighter", new FlatMaterialLighterIJTheme(), false),
        FLAT_ATOM_ONE("Flat Atom One", new FlatAtomOneLightIJTheme(), false);

        private static final Map<String, Tema> NAME_TO_ENUM = new HashMap<>();
        static {
            for (Tema tema : values()) {
                NAME_TO_ENUM.put(tema.name, tema);
            }
        }

        private final String name;
        private final LookAndFeel lookAndFeel;
        private final boolean isDark;

        Tema(String name, LookAndFeel lookAndFeel, boolean isDark) {
            this.name = name;
            this.lookAndFeel = lookAndFeel;
            this.isDark = isDark;
        }

        public String getName() {
            return name;
        }

        public LookAndFeel getLookAndFeel() {
            return lookAndFeel;
        }

        public boolean isDark() {
            return isDark;
        }

        public static Tema fromName(String name) {
            return NAME_TO_ENUM.getOrDefault(name, DEFAULT_TEMA); // Default
        }
    }
}