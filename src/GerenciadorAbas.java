import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class GerenciadorAbas {

    // Constantes para os nomes das abas
    private static final String ABA_NOMENCLATURA_ARQUIVOS = "Nomenclatura de arquivos";
    private static final String ABA_ORGANIZACAO_PASTAS = "Organização de pastas";

    // Constantes para os nomes das opções na aba "Nomenclatura de arquivos"
    private static final String OPCAO_SUBSTITUICAO_SIMPLES = "Substituição Simples";
    private static final String OPCAO_RENOMEAR_ORDENAR = "Renomear e ordenar";

    private JTabbedPane mainTabbedPane;
    private CardLayout cardLayout;
    private JPanel painelConteudo;
    private JTabbedPane nomenclaturaTabbedPane;
    private JTextArea textAreaArquivos;

    public GerenciadorAbas(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
        criarAbas();
    }

    private void criarAbas() {
        // Cria um JTabbedPane para as abas principais
        mainTabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        UIManager.put("TabbedPane.selected", new Color(0xEB5E28));
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10));
        UIManager.put("TabbedPane.font", new Font("Arial", Font.PLAIN, 12));

        criarAbaNomenclaturaArquivos();
        criarAbaOrganizacaoPastas();

        // Inicializa o painel de conteúdo com a primeira opção
        cardLayout.show(painelConteudo, OPCAO_SUBSTITUICAO_SIMPLES);
    }

    private void criarAbaNomenclaturaArquivos() {
        JPanel panelNomenclatura = criarAba(ABA_NOMENCLATURA_ARQUIVOS, mainTabbedPane);
        panelNomenclatura.setLayout(new GridBagLayout());
        panelNomenclatura.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY)
        ));

        // Cria o painel de conteúdo e configura o CardLayout
        painelConteudo = new JPanel();
        cardLayout = new CardLayout();
        painelConteudo.setLayout(cardLayout);

        // Cria o JTabbedPane para as sub-abas
        nomenclaturaTabbedPane = new JTabbedPane();
        UIManager.put("TabbedPane.selected", new Color(0xEB5E28));

        // Cria os painéis das opções e adiciona ao painel de conteúdo
        JPanel panelSubstituicaoSimples = new PainelSubstituicaoSimples(textAreaArquivos).criarPainel();
        JPanel panelRenomearOrdenar = new PainelRenomearOrdenar(textAreaArquivos).criarPainel();
        nomenclaturaTabbedPane.addTab(OPCAO_SUBSTITUICAO_SIMPLES, panelSubstituicaoSimples);
        nomenclaturaTabbedPane.addTab(OPCAO_RENOMEAR_ORDENAR, panelRenomearOrdenar);

        // Configura o GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        panelNomenclatura.add(nomenclaturaTabbedPane, gbc);
    }

    private void criarAbaOrganizacaoPastas() {
        JPanel panelOrganizacaoPastas = new PainelOrganizacaoPastas(textAreaArquivos).criarPainel();
        mainTabbedPane.addTab(ABA_ORGANIZACAO_PASTAS, panelOrganizacaoPastas);
    }

    // Método auxiliar para criar abas
    private JPanel criarAba(String titulo, JTabbedPane tabbedPane) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab(titulo, panel);
        return panel;
    }

    public JTabbedPane getMainTabbedPane() {
        return mainTabbedPane;
    }

    // Atualiza o painel de conteúdo com base na opção selecionada
    public void atualizarPainelConteudo(String opcaoSelecionada) {
        painelConteudo.removeAll();

        if (opcaoSelecionada.equals(OPCAO_SUBSTITUICAO_SIMPLES)) {
            painelConteudo.add(new PainelSubstituicaoSimples(textAreaArquivos).criarPainel(), BorderLayout.CENTER);
        } else if (opcaoSelecionada.equals(OPCAO_RENOMEAR_ORDENAR)) {
            painelConteudo.add(new PainelRenomearOrdenar(textAreaArquivos).criarPainel(), BorderLayout.CENTER);
        }

        cardLayout.show(painelConteudo, opcaoSelecionada);

        painelConteudo.revalidate();
        painelConteudo.repaint();
    }
}