package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Model.ModeloApi;
import Model.ObservadoIF;
import Model.ObservadorIF;

/**
 * Escolha dos jogadores e dos herdeiros, ANTES de a partida começar (2 a 5 jogadores).
 * Cada jogador informa nome, cor e herdeiro; ao lado aparece a cartela do herdeiro
 * escolhido. Não deixa repetir cor nem herdeiro. É observadora do Model e se fecha
 * quando a partida começa.
 */
public class JanelaEscolhaHerdeiros extends JFrame implements ObservadorIF {
	private static final long serialVersionUID = 1L;
	private static final int MAX_JOGADORES = 5;

	private final ModeloApi modelo;
	private final OuvinteInicio ouvinte;

	private final JComboBox<Integer> quantidade = new JComboBox<Integer>();
	private final List<JPanel> linhas = new ArrayList<JPanel>();
	private final List<JTextField> nomes = new ArrayList<JTextField>();
	private final List<JComboBox<String>> cores = new ArrayList<JComboBox<String>>();
	private final List<JComboBox<String>> herdeiros = new ArrayList<JComboBox<String>>();
	private final PainelImagem previa = new PainelImagem();
	private final JLabel rotuloPrevia = new JLabel(" ");
	private final JLabel erro = new JLabel(" ");
	private final JButton comecar = new JButton("Começar partida");

	public JanelaEscolhaHerdeiros(ModeloApi modelo, OuvinteInicio ouvinte) {
		super("Escolha dos herdeiros");
		this.modelo = modelo;
		this.ouvinte = ouvinte;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new FechamentoJanela());
		setLayout(new BorderLayout(10, 10));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// topo: quantidade de jogadores
		JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topo.add(new JLabel("Número de jogadores:"));
		for (int i = 2; i <= MAX_JOGADORES; i++)
			quantidade.addItem(i);
		topo.add(quantidade);
		add(topo, BorderLayout.NORTH);

		// centro: uma linha por jogador
		JPanel tabela = new JPanel(new GridLayout(MAX_JOGADORES + 1, 1, 6, 6));
		tabela.add(cabecalho());
		for (int i = 0; i < MAX_JOGADORES; i++)
			tabela.add(criarLinha(i));
		JPanel esquerda = new JPanel(new BorderLayout());
		esquerda.add(tabela, BorderLayout.NORTH);
		esquerda.setPreferredSize(new Dimension(520, 300));
		add(esquerda, BorderLayout.WEST);

		// direita: cartela do herdeiro escolhido por último
		JPanel direita = new JPanel(new BorderLayout(0, 4));
		previa.setPreferredSize(new Dimension(560, 305));
		direita.add(previa, BorderLayout.CENTER);
		direita.add(rotuloPrevia, BorderLayout.SOUTH);
		add(direita, BorderLayout.CENTER);

		// rodapé: erro e botão
		JPanel rodape = new JPanel(new BorderLayout());
		erro.setForeground(new Color(190, 30, 30));
		rodape.add(erro, BorderLayout.CENTER);
		rodape.add(comecar, BorderLayout.EAST);
		add(rodape, BorderLayout.SOUTH);

		quantidade.addActionListener(new AcaoQuantidade());
		comecar.addActionListener(new AcaoComecar());
		quantidade.setSelectedItem(3);
		atualizarLinhas();
		mostrarPrevia(Herdeiros.NOMES[0]);

		pack();
		setLocationRelativeTo(null);
		modelo.add(this);
	}

	private JPanel cabecalho() {
		JPanel p = new JPanel(new GridLayout(1, 4, 6, 0));
		p.add(new JLabel(" "));
		p.add(new JLabel("Nome"));
		p.add(new JLabel("Cor"));
		p.add(new JLabel("Herdeiro"));
		return p;
	}

	private JPanel criarLinha(int i) {
		JPanel p = new JPanel(new GridLayout(1, 4, 6, 0));
		JTextField nome = new JTextField("Jogador " + (i + 1));
		JComboBox<String> cor = new JComboBox<String>(Herdeiros.CORES);
		cor.setSelectedIndex(i);
		cor.addActionListener(new AcaoLimparErro());
		JComboBox<String> herdeiro = new JComboBox<String>(Herdeiros.NOMES);
		herdeiro.setSelectedIndex(i);
		herdeiro.addActionListener(new AcaoHerdeiro(herdeiro));
		p.add(new JLabel("Jogador " + (i + 1)));
		p.add(nome);
		p.add(cor);
		p.add(herdeiro);
		linhas.add(p);
		nomes.add(nome);
		cores.add(cor);
		herdeiros.add(herdeiro);
		return p;
	}

	private int numeroDeJogadores() {
		return (Integer) quantidade.getSelectedItem();
	}

	private void atualizarLinhas() {
		int n = numeroDeJogadores();
		for (int i = 0; i < MAX_JOGADORES; i++)
			for (java.awt.Component c : linhas.get(i).getComponents())
				c.setVisible(i < n);
	}

	private void mostrarPrevia(String herdeiro) {
		previa.setImagem(Herdeiros.cartela(herdeiro), "Cartela não encontrada: " + herdeiro);
		rotuloPrevia.setText("Cartela de " + herdeiro);
	}

	private class AcaoLimparErro implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			erro.setText(" ");
		}
	}

	private class AcaoQuantidade implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			atualizarLinhas();
		}
	}

	private class AcaoHerdeiro implements ActionListener {
		private final JComboBox<String> combo;

		AcaoHerdeiro(JComboBox<String> combo) {
			this.combo = combo;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			erro.setText(" ");
			mostrarPrevia((String) combo.getSelectedItem());
		}
	}

	private class AcaoComecar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int n = numeroDeJogadores();
			Set<String> coresUsadas = new HashSet<String>();
			Set<String> herdeirosUsados = new HashSet<String>();
			List<String[]> escolha = new ArrayList<String[]>();
			for (int i = 0; i < n; i++) {
				String nome = nomes.get(i).getText().trim();
				String cor = (String) cores.get(i).getSelectedItem();
				String herdeiro = (String) herdeiros.get(i).getSelectedItem();
				if (nome.isEmpty()) {
					erro.setText("Informe o nome do jogador " + (i + 1) + ".");
					return;
				}
				if (!coresUsadas.add(cor)) {
					erro.setText("Cada jogador precisa de uma cor diferente.");
					return;
				}
				if (!herdeirosUsados.add(herdeiro)) {
					erro.setText("Cada jogador precisa de um herdeiro diferente.");
					return;
				}
				escolha.add(new String[] { nome, cor, herdeiro });
			}
			erro.setText(" ");
			comecar.setEnabled(false);
			ouvinte.iniciarPartida(escolha);
		}
	}

	private class FechamentoJanela extends WindowAdapter {
		@Override
		public void windowClosed(WindowEvent e) {
			modelo.remove(JanelaEscolhaHerdeiros.this);
		}
	}

	@Override
	public void notify(ObservadoIF o) {
		ThreadSwing.executar(new Runnable() {
			@Override
			public void run() {
				if (modelo.isPartidaIniciada()) {
					modelo.remove(JanelaEscolhaHerdeiros.this);
					dispose();
				}
			}
		});
	}
}