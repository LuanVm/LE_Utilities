import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Enumeration;

public class ConfiguracoesTema {

    private static final String CONFIG_FILE = "config.properties";
    private JPanel panelConfiguracoes;
    private JRadioButton radioButtonFlatArcDarkOrangeIJTheme;
    private JRadioButton radioButtonFlatDarkPurpleIJTheme;
    private JRadioButton radioButtonFlatLafIntelliJ;
    private JRadioButton radioButtonFlatCarbonIJTheme;
    private JRadioButton radioButtonFlatMonokaiProIJTheme;
    private JRadioButton radioButtonFlatGruvboxDarkHardIJTheme;
    private JRadioButton radioButtonFlatHiDPIDarkIJTheme;
    private ButtonGroup temaButtonGroup;

    public ConfiguracoesTema() {
        panelConfiguracoes = new JPanel();
        panelConfiguracoes.setLayout(new BorderLayout());
        panelConfiguracoes.setBorder(new EmptyBorder(20, 20, 20, 20));
        configurarPainelConfiguracoes();
    }

    public JPanel getPainelConfiguracoes() {
        return panelConfiguracoes;
    }

    private void configurarPainelConfiguracoes() {
        // Painel para as opções de tema
        JPanel temaPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        temaPanel.setBorder(new TitledBorder("Tema da Interface"));

        // Inicializa o ButtonGroup
        temaButtonGroup = new ButtonGroup();

        // ActionListener para alterar o tema
        ActionListener temaActionListener = e -> {
            JRadioButton source = (JRadioButton) e.getSource();
            if (source.isSelected()) { // Verifica se o RadioButton está selecionado
                Tema temaSelecionado = Tema.fromName(source.getText());
                atualizarTema(temaSelecionado.getLookAndFeel());
            }
        };

        // Criação dos RadioButtons utilizando método auxiliar
        radioButtonFlatArcDarkOrangeIJTheme = createRadioButton("Flat Dark Orange", true, temaButtonGroup, temaPanel, temaActionListener);
        radioButtonFlatDarkPurpleIJTheme = createRadioButton("Flat Dark Purple", false, temaButtonGroup, temaPanel, temaActionListener);
        radioButtonFlatLafIntelliJ = createRadioButton("FlatLaf IntelliJ (Light)", false, temaButtonGroup, temaPanel, temaActionListener);
        radioButtonFlatCarbonIJTheme = createRadioButton("Flat Carbon", false, temaButtonGroup, temaPanel, temaActionListener);
        radioButtonFlatMonokaiProIJTheme = createRadioButton("Flat Monokai Pro", false, temaButtonGroup, temaPanel, temaActionListener);
        radioButtonFlatGruvboxDarkHardIJTheme = createRadioButton("Flat Gruvbox Dark Hard", false, temaButtonGroup, temaPanel, temaActionListener);
        radioButtonFlatHiDPIDarkIJTheme = createRadioButton("Flat HiDPI Dark", false, temaButtonGroup, temaPanel, temaActionListener);

        // Adiciona o painel de tema à aba
        panelConfiguracoes.add(temaPanel, BorderLayout.NORTH);

        // Botão Salvar
        JButton buttonSalvar = new JButton("Salvar"); // Criação do botão diretamente
        temaPanel.add(buttonSalvar);

        buttonSalvar.addActionListener(e -> saveConfig());
    }

    // Método auxiliar para criar RadioButtons
    private JRadioButton createRadioButton(String text, boolean selected, ButtonGroup group, JPanel panel, ActionListener listener) {
        JRadioButton radioButton = new JRadioButton(text, selected);
        group.add(radioButton);
        panel.add(radioButton);
        radioButton.addActionListener(listener);
        return radioButton;
    }

    // Método para atualizar o tema da interface
    private void atualizarTema(LookAndFeel lookAndFeel) {
        try {
            UIManager.setLookAndFeel(lookAndFeel);
            FlatLaf.updateUI();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            Tema temaSelecionado = getTemaSelecionado();
            prop.setProperty("tema", temaSelecionado.getName());
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    // Método para obter o tema selecionado
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
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);

            String temaName = prop.getProperty("tema", "Flat Dark Orange");
            Tema tema = Tema.fromName(temaName);

            try {
                UIManager.setLookAndFeel(tema.getLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Enum para os temas
    enum Tema {
        FLAT_DARK_ORANGE("Flat Dark Orange", new FlatArcDarkOrangeIJTheme()),
        FLAT_DARK_PURPLE("Flat Dark Purple", new FlatDarkPurpleIJTheme()),
        FLAT_INTELLIJ("FlatLaf IntelliJ (Light)", new FlatIntelliJLaf()),
        FLAT_CARBON("Flat Carbon", new FlatCarbonIJTheme()),
        FLAT_MONOKAI_PRO("Flat Monokai Pro", new FlatMonokaiProIJTheme()),
        FLAT_GRUVBOX("Flat Gruvbox Dark Hard", new FlatGruvboxDarkHardIJTheme()),
        FLAT_HIDPI("Flat HiDPI Dark", new FlatHiberbeeDarkIJTheme());

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
            for (Tema tema : values()) {
                if (tema.getName().equals(name)) {
                    return tema;
                }
            }
            return FLAT_DARK_ORANGE; // Default
        }
    }
}
