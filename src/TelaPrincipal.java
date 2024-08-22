import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class TelaPrincipal {

    private static JTextArea textAreaArquivos; // Área de texto compartilhada
    private static JPanel painelConteudo; // Painel para exibir o conteúdo das opções
    private static JPanel panelOrganizacaoPastas;

    public static void main(String[] args) {

        // Tema
        FlatLaf.registerCustomDefaultsSource("com.formdev.flatlaf.intellijthemes");
        try {
            UIManager.setLookAndFeel(new FlatArcDarkOrangeIJTheme());
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }

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
        loadingLabel.setFont(new Font("Open Sans", Font.BOLD, 18));
        loadingLabel.setForeground(Color.WHITE);

        splashPanel.setLayout(new BorderLayout());
        splashPanel.add(loadingLabel, BorderLayout.CENTER);

        JWindow splashScreen = new JWindow();
        splashScreen.getContentPane().add(splashPanel);
        splashScreen.setSize(300, 200);
        splashScreen.setLocationRelativeTo(null);
        splashScreen.setVisible(true);

        try {
            Thread.sleep(2000);
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
        JTabbedPane mainTabbedPane = new JTabbedPane();
        UIManager.put("TabbedPane.selected", new Color(0xEB5E28));

        // Cria a aba "Nomenclatura de arquivos"
        JPanel panelNomenclatura = criarAba("Nomenclatura de arquivos", mainTabbedPane);
        panelNomenclatura.setLayout(new BorderLayout());

        // Cria o painel de conteúdo e configura o CardLayout
        painelConteudo = new JPanel();
        // Gerenciador de layout para o painel de conteúdo
        CardLayout cardLayout = new CardLayout();
        painelConteudo.setLayout(cardLayout);
        panelNomenclatura.add(painelConteudo, BorderLayout.CENTER);

        // Cria os painéis das opções e adiciona ao painel de conteúdo
        JPanel panelSubstituicaoSimples = new PainelSubstituicaoSimples(textAreaArquivos).criarPainel();
        JPanel panelRenomearOrdenar = new PainelRenomearOrdenar(textAreaArquivos).criarPainel();
        painelConteudo.add(panelSubstituicaoSimples, "Substituição Simples");
        painelConteudo.add(panelRenomearOrdenar, "Renomear e ordenar");

        // Cria o JPopupMenu (menu suspenso)
        JPopupMenu popupMenu = new JPopupMenu();

        // Adiciona as opções ao menu suspenso
        JMenuItem menuItemSubstituicao = new JMenuItem("Substituição Simples");
        menuItemSubstituicao.addActionListener(e -> atualizarPainelConteudo("Substituição Simples"));
        popupMenu.add(menuItemSubstituicao);

        JMenuItem menuItemRenomearOrdenar = new JMenuItem("Renomear e ordenar");
        menuItemRenomearOrdenar.addActionListener(e -> atualizarPainelConteudo("Renomear e ordenar"));
        popupMenu.add(menuItemRenomearOrdenar);

// Adiciona um PopupMenuListener para exibir o popupMenu ao clicar com o botão direito na aba
        mainTabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int tabIndex = mainTabbedPane.indexAtLocation(e.getX(), e.getY());
                if (tabIndex == 0) { // Índice da aba "Nomenclatura de arquivos"
                    // Calcula a posição X e Y da aba
                    Rectangle tabBounds = mainTabbedPane.getBoundsAt(0);
                    int x = tabBounds.x;
                    int y = tabBounds.y + tabBounds.height; // Posição Y abaixo da aba

                    popupMenu.show(mainTabbedPane, x, y);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Rectangle bounds = mainTabbedPane.getBoundsAt(0);
                if (!bounds.contains(e.getPoint()) && !popupMenu.contains(e.getPoint())) {
                    popupMenu.setVisible(false);
                }
            }
        });

        // Cria a aba "Organização de pastas"
        panelOrganizacaoPastas = new PainelOrganizacaoPastas(textAreaArquivos).criarPainel();
        mainTabbedPane.addTab("Organização de pastas", panelOrganizacaoPastas);

        // Adiciona o JTabbedPane principal ao frame
        frame.add(mainTabbedPane);

        // Inicializa o painel de conteúdo com a primeira opção
        cardLayout.show(painelConteudo, "Substituição Simples");

        frame.pack();
    }

    // Metodo para criar botões com o preset de design
    public static JButton criarBotao(String texto) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isArmed())
                {
                    // Degradê mais escuro quando pressionado
                    GradientPaint gp = new GradientPaint(0, 0, getBackground().darker(),
                            0, getHeight(), getBackground().darker().darker());
                    g2.setPaint(gp);
                } else {
                    // Degradê normal
                    GradientPaint gp = new GradientPaint(0, 0, getBackground(),
                            0, getHeight(), getBackground().darker());
                    g2.setPaint(gp);
                }
                g2.fillRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 5, 5); // Cantos menos arredondados

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
        tabbedPane.addTab(titulo, panel);
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

}