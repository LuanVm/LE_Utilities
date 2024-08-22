import com.formdev.flatlaf.FlatDarculaLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class TelaPrincipal {

    private static JTextArea textAreaArquivos;

    public static void main(String[] args) {

        // tema
        FlatDarculaLaf.setup();

        // tela de loading
        JPanel splashPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        50, 50);
                super.paintComponent(g);
            }
        };
        splashPanel.setLayout(new BorderLayout());
        splashPanel.add(new JLabel("Carregando...", SwingConstants.CENTER), BorderLayout.CENTER);

        JWindow splashScreen = new JWindow();
        splashScreen.getContentPane().add(splashPanel);
        splashScreen.setSize(120, 80);
        splashPanel.setBackground(new Color(0x3C3D37));
        splashPanel.setForeground(Color.WHITE);
        splashScreen.setLocationRelativeTo(null);
        splashScreen.setVisible(true);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Livre Escolha - Utilities");

        try {
            BufferedImage originalImage = ImageIO.read(new File("src/mini.png"));
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

        frame.pack();
        splashScreen.dispose();
        frame.setVisible(true);

        // Cria um JTabbedPane para as abas
        JTabbedPane tabbedPane = new JTabbedPane();
        UIManager.put("TabbedPane.selected", new Color(0xEB5E28));

        // Cria a área de texto (antes de criar as abas)
        textAreaArquivos = new JTextArea(10, 40);
        textAreaArquivos.setEditable(false);

        // Cria e configura as abas
        textAreaArquivos = new JTextArea(10, 40);
        textAreaArquivos.setEditable(false);
        tabbedPane.addTab("Substituição Simples", new PainelSubstituicaoSimples(textAreaArquivos).criarPainel());
        tabbedPane.addTab("Renomear e ordenar", new PainelRenomearOrdenar(textAreaArquivos).criarPainel()); // Passa a textAreaArquivos

        // Adiciona o JTabbedPane ao frame
        frame.add(tabbedPane);
    }

    // Método para criar botões com o preset de design
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
                    g2.drawRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 6, 6);
                }
            }
        };

        botao.setBackground(new Color(0xE85C0D));
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

    // Função auxiliar para obter a extensão de um arquivo
    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot > 0) {
            return fileName.substring(lastIndexOfDot);
        } else {
            return "";
        }
    }

}