import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GerenciadorAbas {

    private static final String ABA_NOMENCLATURA_ARQUIVOS = "Nomenclatura de Arquivos";
    private static final String ABA_GERENCIAMENTO_PLANILHAS = "Gerenciamento de Planilhas";
    private static final String ABA_ORGANIZACAO_PASTAS = "Gerenciamento de Pastas";

    private static final String OPCAO_SUBSTITUICAO_SIMPLES = "Substituição Simples";
    private static final String OPCAO_RENOMEAR_ORDENAR = "Renomear e Ordenar";
    private static final String OPCAO_MESCLAGEM_PLANILHAS = "Mesclagem de Planilhas";
    private static final String OPCAO_PROCESSAMENTO_AGITEL = "Processamento Agitel";

    private static final Color TAB_SELECTED_COLOR = new Color(0xEB5E28);
    private static final Font TAB_FONT = new Font("Arial", Font.PLAIN, 12);

    private JTabbedPane mainTabbedPane;
    private JTabbedPane nomenclaturaTabbedPane;
    private JTabbedPane planilhasTabbedPane;
    private JTextArea textAreaArquivos;

    public GerenciadorAbas(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
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
        criarAbaGerenciamentoPlanilhas();
        criarAbaOrganizacaoPastas();

        mainTabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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

    private JPanel criarAbaGerenciamentoPlanilhas() {
        JPanel panelPlanilhas = criarAba(ABA_GERENCIAMENTO_PLANILHAS, mainTabbedPane);
        panelPlanilhas.setLayout(new GridBagLayout());
        panelPlanilhas.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY)
        ));

        planilhasTabbedPane = new JTabbedPane();
        planilhasTabbedPane.addTab(OPCAO_MESCLAGEM_PLANILHAS, criarPainelMesclagemPlanilhas());
        planilhasTabbedPane.addTab(OPCAO_PROCESSAMENTO_AGITEL, criarPainelProcessamentoAgitel());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        panelPlanilhas.add(planilhasTabbedPane, gbc);
        return panelPlanilhas;
    }

    private void criarAbaOrganizacaoPastas() {
        JPanel panelOrganizacaoPastas = criarAba(ABA_ORGANIZACAO_PASTAS, mainTabbedPane);
        panelOrganizacaoPastas.setLayout(new GridBagLayout());
        panelOrganizacaoPastas.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY)
        ));

        JPanel painelOrganizacao = new PainelOrganizacaoPastas(textAreaArquivos).criarPainel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        panelOrganizacaoPastas.add(painelOrganizacao, gbc);
    }

    private JPanel criarPainelSubstituicaoSimples() {
        return new PainelSubstituicaoSimples(textAreaArquivos).criarPainel();
    }

    private JPanel criarPainelRenomearOrdenar() {
        return new PainelRenomearOrdenar(textAreaArquivos).criarPainel();
    }

    private JPanel criarPainelMesclagemPlanilhas() {
        return new PainelMesclaPlanilha(textAreaArquivos).criarPainel();
    }

    private JPanel criarPainelProcessamentoAgitel() {
        return new PainelProcessamentoAgitel(textAreaArquivos).criarPainel();
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
}
