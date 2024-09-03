import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TelaPrincipal {

    private static JFrame frame;
    private static JTextArea textAreaArquivos;

    private static final Color BACKGROUND_COLOR = new Color(0xFFFBE6);
    private static final Color DARKER_BACKGROUND_COLOR = BACKGROUND_COLOR.darker();
    private static final Color HOVER_COLOR = new Color(0xF1D4AF);
    private static final Color CLICK_COLOR = new Color(0xC5936F);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot > 0 && lastIndexOfDot < fileName.length() - 1) {
            return fileName.substring(lastIndexOfDot + 1);
        }
        return "";
    }

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

            Timer resizeTimer = new Timer(100, e -> {
                Dimension size = frame.getSize();
                frame.setSize(size.width + 1, size.height + 1);
                frame.setSize(size);
                ((Timer) e.getSource()).stop();
            });
            resizeTimer.setRepeats(false);
            resizeTimer.start();

            splashScreen.dispose();
        });
    }

    private static JPanel createSplashPanel() {
        JPanel splashPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(0x4CAF50),
                        0, getHeight(), new Color(0x2E8B57));
                g2.setPaint(gp);

                int[] xPoints = {0, getWidth() / 2, getWidth()};
                int[] yPoints = {0, getHeight(), 0};
                g2.fillPolygon(xPoints, yPoints, 3);
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

        JDialog dialogConfiguracoes = new JDialog(frame, "Configurações", true);
        dialogConfiguracoes.setSize(400, 300);
        dialogConfiguracoes.setLocationRelativeTo(frame);
        dialogConfiguracoes.getContentPane().add(configuracoesTema.getPainelConfiguracoes());

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
        try {
            BufferedImage originalImage = ImageIO.read(new File("resources/logo.png"));
            int newWidth = 16;
            int newHeight = 16;
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();
            frame.setIconImage(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JButton criarBotao(String text) {
        CustomButton button = new CustomButton(text, BACKGROUND_COLOR, DARKER_BACKGROUND_COLOR, HOVER_COLOR, CLICK_COLOR);

        button.setPreferredSize(new Dimension(120, 24));
        button.setForeground(Color.BLACK);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);

        button.setFont(BUTTON_FONT);
        button.setMargin(new Insets(5, 10, 5, 10));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setMouseOver(true);
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setMouseOver(false);
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setMousePressed(true);
                button.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setMousePressed(false);
                button.repaint();
            }
        });

        return button;
    }

    static class CustomButton extends JButton {
        private boolean mouseOver = false;
        private boolean mousePressed = false;
        private final Color backgroundColor;
        private final Color darkerBackgroundColor;
        private final Color hoverColor;
        private final Color clickColor;

        public CustomButton(String text, Color backgroundColor, Color darkerBackgroundColor, Color hoverColor, Color clickColor) {
            super(text);
            this.backgroundColor = backgroundColor;
            this.darkerBackgroundColor = darkerBackgroundColor;
            this.hoverColor = hoverColor;
            this.clickColor = clickColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color startColor = backgroundColor;
            Color endColor = darkerBackgroundColor;

            if (mousePressed) {
                startColor = clickColor;
                endColor = clickColor.darker();
            } else if (mouseOver) {
                startColor = hoverColor;
                endColor = hoverColor.darker();
            }

            GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);

            if (mouseOver && !mousePressed) {
                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() / 2, 15, 15);
            }

            super.paintComponent(g);
        }

        @Override
        public void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isFocusOwner()) {
                g2.setColor(new Color(0, 0, 0, 50));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        }

        public void setMouseOver(boolean mouseOver) {
            this.mouseOver = mouseOver;
        }

        public void setMousePressed(boolean mousePressed) {
            this.mousePressed = mousePressed;
        }
    }
}
