import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GerenciadorAbas {

    private static final String ABA_NOMENCLATURA_ARQUIVOS = "Nomenclatura de arquivos";
    private static final String ABA_ORGANIZACAO_PASTAS = "Organização de pastas";
    private static final String ABA_MESCLAGEM_PLANILHAS = "Mesclagem de Planilhas";

    private static final String OPCAO_SUBSTITUICAO_SIMPLES = "Substituição Simples";
    private static final String OPCAO_RENOMEAR_ORDENAR = "Renomear e ordenar";

    private static final Color TAB_SELECTED_COLOR = new Color(0xEB5E28);
    private static final Font TAB_FONT = new Font("Arial", Font.PLAIN, 12);

    private JTabbedPane mainTabbedPane;
    private CardLayout cardLayout;
    private JPanel painelConteudo;
    private JTabbedPane nomenclaturaTabbedPane;
    private JTextArea textAreaArquivos;
    private Map<String, JPanel> abasNomenclaturaMap;

    public GerenciadorAbas(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
        this.abasNomenclaturaMap = new HashMap<>();
        configurarUI();
        criarAbas();
    }

    private void configurarUI() {
        UIManager.put("TabbedPane.selected", TAB_SELECTED_COLOR);
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10));
        UIManager.put("TabbedPane.font", TAB_FONT);
    }

    private void criarAbas() {
        mainTabbedPane = new JTabbedPane(JTabbedPane.LEFT);

        criarAbaNomenclaturaArquivos();
        criarAbaOrganizacaoPastas();
        criarAbaMesclagemPlanilhas();

        cardLayout = new CardLayout();
        painelConteudo = new JPanel(cardLayout);

        // Inicializa o painel de conteúdo com a primeira opção
        atualizarPainelConteudo(OPCAO_SUBSTITUICAO_SIMPLES);
    }

    private void criarAbaNomenclaturaArquivos() {
        JPanel panelNomenclatura = criarAba(ABA_NOMENCLATURA_ARQUIVOS, mainTabbedPane);
        panelNomenclatura.setLayout(new GridBagLayout());
        panelNomenclatura.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY)
        ));

        nomenclaturaTabbedPane = new JTabbedPane();
        nomenclaturaTabbedPane.addTab(OPCAO_SUBSTITUICAO_SIMPLES, criarPainelSubstituicaoSimples());
        nomenclaturaTabbedPane.addTab(OPCAO_RENOMEAR_ORDENAR, criarPainelRenomearOrdenar());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        panelNomenclatura.add(nomenclaturaTabbedPane, gbc);
    }

    private JPanel criarPainelSubstituicaoSimples() {
        return new PainelSubstituicaoSimples(textAreaArquivos).criarPainel();
    }

    private JPanel criarPainelRenomearOrdenar() {
        return new PainelRenomearOrdenar(textAreaArquivos).criarPainel();
    }

    private void criarAbaOrganizacaoPastas() {
        JPanel panelOrganizacaoPastas = new PainelOrganizacaoPastas(textAreaArquivos).criarPainel();
        mainTabbedPane.addTab(ABA_ORGANIZACAO_PASTAS, panelOrganizacaoPastas);
    }

    private void criarAbaMesclagemPlanilhas() {
        JPanel panelMesclagemPlanilhas = new PlanilhaMergerPanel(textAreaArquivos).criarPainel();
        mainTabbedPane.addTab(ABA_MESCLAGEM_PLANILHAS, panelMesclagemPlanilhas);
    }

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

    public void atualizarPainelConteudo(String opcaoSelecionada) {
        painelConteudo.removeAll();

        JPanel painel = abasNomenclaturaMap.get(opcaoSelecionada);
        if (painel != null) {
            painelConteudo.add(painel, BorderLayout.CENTER);
            cardLayout.show(painelConteudo, opcaoSelecionada);
        }

        painelConteudo.revalidate();
        painelConteudo.repaint();
    }
}
