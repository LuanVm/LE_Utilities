import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TelaPrincipal {

    private static JFrame frame;
    private static JTextArea textAreaArquivos;

    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot > 0) {
            return fileName.substring(lastIndexOfDot);
        } else {
            return "";
        }
    }

    public static void main(String[] args) {
        // Executa a criação da GUI na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Carrega as configurações ao iniciar
            ConfiguracoesTema.loadConfig();

            // Tela de loading
            JPanel splashPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    GradientPaint gp = new GradientPaint(0, 0, new Color(0x4CAF50), // Verde claro
                            0, getHeight(), new Color(0x2E8B57)); // Verde escuro
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

            // Configuração do ícone da janela
            try {
                BufferedImage originalImage = ImageIO.read(new File("resources/logo.png"));
                int newWidth = 16;
                int newHeight = 16;
                Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                resizedImage.getGraphics().drawImage(scaledImage, 0, 0, null);
                frame.setIconImage(resizedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setPreferredSize(new Dimension(1280, 720));
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    frame.revalidate();
                    frame.repaint();
                }
            });

            splashScreen.dispose();
            frame.setVisible(true);

            // Usa um Timer para forçar o redimensionamento com um pequeno delay (opcional)
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

            // Cria o gerenciador de abas
            GerenciadorAbas gerenciadorAbas = new GerenciadorAbas(textAreaArquivos);

            // Cria o painel de configurações de tema
            ConfiguracoesTema configuracoesTema = new ConfiguracoesTema();

            // Cria o JDialog para as configurações
            JDialog dialogConfiguracoes = new JDialog(frame, "Configurações", true);
            dialogConfiguracoes.setSize(400, 300);
            dialogConfiguracoes.setLocationRelativeTo(frame);
            dialogConfiguracoes.getContentPane().add(configuracoesTema.getPainelConfiguracoes());

            // Cria o botão para abrir as configurações de tema
            JButton botaoTema = criarBotao("Temas");
            botaoTema.addActionListener(e -> dialogConfiguracoes.setVisible(true));

            // Adiciona o botão de configurações ao rodapé
            JPanel rodapePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            rodapePanel.add(botaoTema);
            frame.add(rodapePanel, BorderLayout.SOUTH);

            // Adiciona o painel de abas ao frame
            frame.add(gerenciadorAbas.getMainTabbedPane(), BorderLayout.CENTER);

            frame.pack();
        });
    }

    // Metodo para criar botões com o preset de design
    public static JButton criarBotao(String texto) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Definição da cor de fundo do botão
                g2.setColor(new Color(0xFF8343)); // Cor de fundo #FF8343
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8); // Cantos arredondados

                super.paintComponent(g);
            }

            @Override
            public void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isFocusOwner()) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8); // Cantos arredondados
                }
            }
        };

        botao.setPreferredSize(new Dimension(120, 28)); // Define o tamanho fixo como 120x28 pixels
        botao.setForeground(Color.WHITE); // Cor do texto
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setContentAreaFilled(false);

        botao.setFont(new Font("Arial", Font.BOLD, 12)); // Fonte em negrito e tamanho 12

        botao.setMargin(new Insets(5, 10, 5, 10)); // Espaçamento interno
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(Color.GRAY, 1, true)
        ));

        return botao;
    }


    // Metodo para obter o JFrame (usado na classe ConfiguracoesTema)
    public static JFrame getFrame() {
        return frame;
    }
}
