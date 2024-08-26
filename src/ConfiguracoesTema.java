import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.intellijthemes.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

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

        // Adiciona as opções de tema ao painel
        ButtonGroup temaButtonGroup = new ButtonGroup();

        radioButtonFlatArcDarkOrangeIJTheme = new JRadioButton("Flat Dark Orange", true);
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
        panelConfiguracoes.add(temaPanel, BorderLayout.NORTH);

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
                    SwingUtilities.updateComponentTreeUI(TelaPrincipal.getFrame());
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
        JButton buttonSalvar = TelaPrincipal.criarBotao("Salvar");
        temaPanel.add(buttonSalvar);

        buttonSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveConfig();
            }
        });
    }

    private void saveConfig() {
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

    public static void loadConfig() {
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
}