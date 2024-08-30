import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//Verificar sobre possibilidade de alterar o delimitador de seções no código de organização de pastas, para "-" ou Uppercase.

public class TelaPrincipal {

    private static JFrame frame;
    private static JTextArea textAreaArquivos;

    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot > 0) {
            return fileName.substring(lastIndexOfDot + 1);
        } else {
            return "";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConfiguracoesTema.loadConfig();

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
                Graphics2D g2d = resizedImage.createGraphics();
                g2d.drawImage(scaledImage, 0, 0, null);
                g2d.dispose();
                frame.setIconImage(resizedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setPreferredSize(new Dimension(1280, 720));

            splashScreen.dispose();
            frame.setVisible(true);

            Timer resizeTimer = new Timer(100, e -> {
                Dimension size = frame.getSize();
                frame.setSize(size.width + 1, size.height + 1);
                frame.setSize(size);
                ((Timer) e.getSource()).stop();
            });
            resizeTimer.setRepeats(false);
            resizeTimer.start();

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
        });
    }

    // Metodo para criar botões com o preset de design
    public static JButton criarBotao(String texto) {
        final Color backgroundColor = new Color(0xFFFBE6);
        final Color darkerBackgroundColor = backgroundColor.darker();
        final Color hoverColor = new Color(0xF1D4AF);
        final Color clickColor = new Color(0xC5936F);

        CustomButton botao = new CustomButton(texto, backgroundColor, darkerBackgroundColor, hoverColor, clickColor);

        botao.setPreferredSize(new Dimension(120, 24));
        botao.setForeground(Color.BLACK);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setContentAreaFilled(false);

        botao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        botao.setMargin(new Insets(5, 10, 5, 10));

        // Efeito de hover e clique usando mouseListener
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setMouseOver(true);
                botao.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setMouseOver(false);
                botao.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                botao.setMousePressed(true);
                botao.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                botao.setMousePressed(false);
                botao.repaint();
            }
        });

        return botao;
    }

    // Classe CustomButton para encapsular o comportamento dos botões customizados
    static class CustomButton extends JButton {
        private boolean mouseOver = false;
        private boolean mousePressed = false;
        private final Color backgroundColor;
        private final Color darkerBackgroundColor;
        private final Color hoverColor;
        private final Color clickColor;

        public CustomButton(String texto, Color backgroundColor, Color darkerBackgroundColor, Color hoverColor, Color clickColor) {
            super(texto);
            this.backgroundColor = backgroundColor;
            this.darkerBackgroundColor = darkerBackgroundColor;
            this.hoverColor = hoverColor;
            this.clickColor = clickColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Determina a cor de fundo com base no estado
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