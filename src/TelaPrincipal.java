import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TelaPrincipal {

    private static JFrame frame;
    private static JTextArea textAreaArquivos;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConfiguracoesTema.loadConfig();

            JPanel splashPanel = createSplashPanel();

            JWindow splashScreen = new JWindow();
            splashScreen.getContentPane().add(splashPanel);
            splashScreen.setSize(140, 80);
            splashScreen.setLocationRelativeTo(null);
            splashScreen.setVisible(true);

            createAndShowFrame();

            // Redesenha o frame sem usar Timer
            frame.revalidate();
            frame.repaint();

            splashScreen.dispose();
        });
    }

    private static JPanel createSplashPanel() {
        JPanel splashPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    GradientPaint gp = new GradientPaint(0, 0, new Color(0x4CAF50),
                            0, getHeight(), new Color(0x2E8B57));
                    g2.setPaint(gp);

                    int[] xPoints = {0, getWidth() / 2, getWidth()};
                    int[] yPoints = {0, getHeight(), 0};
                    g2.fillPolygon(xPoints, yPoints, 3);
                } finally {
                    g2.dispose(); // Libera o recurso
                }
            }
        };

        JLabel loadingLabel = new JLabel("Carregando...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Open Sans", Font.BOLD, 14));
        loadingLabel.setForeground(Color.WHITE);

        splashPanel.setLayout(new BorderLayout());
        splashPanel.add(loadingLabel, BorderLayout.CENTER);

        return splashPanel;
    }

    private static void createAndShowFrame() {
        frame = new JFrame("Livre Escolha - Utilities");

        setFrameIcon();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1280, 720));

        textAreaArquivos = new JTextArea(10, 40);
        textAreaArquivos.setEditable(false);

        GerenciadorAbas gerenciadorAbas = new GerenciadorAbas(textAreaArquivos);

        ConfiguracoesTema configuracoesTema = new ConfiguracoesTema();
        JDialog dialogConfiguracoes = createDialog(frame, "Configurações", configuracoesTema.getPainelConfiguracoes());

        JButton botaoTema = criarBotao("Temas");
        botaoTema.addActionListener(e -> dialogConfiguracoes.setVisible(true));

        JPanel rodapePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodapePanel.add(botaoTema);
        frame.add(rodapePanel, BorderLayout.SOUTH);

        frame.add(gerenciadorAbas.getMainTabbedPane(), BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    private static void setFrameIcon() {
        BufferedImage originalImage = loadImage("/logo.png");
        if (originalImage != null) {
            int newWidth = 16;
            int newHeight = 16;
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();
            frame.setIconImage(resizedImage);
        }
    }

    public static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(TelaPrincipal.class.getResource(path));
        } catch (IOException e) {
            System.err.println("Erro ao carregar imagem: " + e.getMessage());
            return null;
        }
    }

    private static JDialog createDialog(JFrame parent, String title, JPanel content) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().add(content);
        return dialog;
    }

    public static JButton criarBotao(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 24));
        button.setMargin(new Insets(5, 10, 5, 10));
        return button;
    }
}
