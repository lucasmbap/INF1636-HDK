package View;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import Model.ModeloApi;
import Model.ObservadoIF;
import Model.ObservadorIF;

/**
 * Janela lateral do jogador da vez, ao lado do tabuleiro: duas abas, Cartela e Cartas
 * de conselheiro. Cada aba e observadora do Model; a janela tambem (mostra a vez no titulo).
 */
public class JanelaJogador extends JFrame implements ObservadorIF {
	private static final long serialVersionUID = 1L;

	private final ModeloApi modelo;
	private final JanelaCartela cartela;
	private final JanelaCartas cartas;
	private final JButton botaoSalvar = new JButton("Salvar partida");
	private final JButton botaoRecuperar = new JButton("Recuperar partida");
	private final PainelMelhorias melhorias;
	private OuvinteArquivos ouvinteArquivos = null;

	public JanelaJogador(ModeloApi modelo) {
		super("Jogador da vez");
		this.modelo = modelo;
		this.cartela = new JanelaCartela(modelo);
		this.cartas = new JanelaCartas(modelo);
		this.melhorias = new PainelMelhorias(modelo);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new Fechamento());
		setLayout(new BorderLayout());
		JTabbedPane abas = new JTabbedPane();
		abas.addTab("Cartela", cartela);
		abas.addTab("Cartas de conselheiro", cartas);
		add(abas, BorderLayout.CENTER);

		JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
		barra.add(botaoSalvar);
		barra.add(botaoRecuperar);
		// no topo (e nao embaixo) para nunca ficar cortado em telas baixas
		JPanel topo = new JPanel();
		topo.setLayout(new javax.swing.BoxLayout(topo, javax.swing.BoxLayout.Y_AXIS));
		topo.add(barra);
		topo.add(melhorias);
		add(topo, BorderLayout.NORTH);
		botaoSalvar.addActionListener(new AcaoSalvar());
		botaoRecuperar.addActionListener(new AcaoRecuperar());

		// ocupa a direita da tela, ao lado do tabuleiro (janela de ~1100 px, a esquerda)
		Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
		int largura = Math.max(480, Math.min(560, tela.width - 1110));
		int altura = Math.min(900, tela.height - 80);
		setSize(largura, altura);
		setLocation(Math.max(0, tela.width - largura - 10), 20);
		modelo.add(this);
		atualizar();
	}

	/** Quem recebe o clique numa coluna da cartela (o Controller). */
	public void setOuvinte(OuvinteAcoes ouvinte) {
		cartela.setOuvinte(ouvinte);
	}

	/** Quem grava e le o arquivo da partida (o Main). */
	public void setOuvinteArquivos(OuvinteArquivos ouvinte) {
		this.ouvinteArquivos = ouvinte;
	}

	/** Quem compra e coloca melhorias no Model (o Main). */
	public void setOuvinteMelhorias(OuvinteMelhorias ouvinte) {
		melhorias.setOuvinte(ouvinte);
	}

	private class AcaoSalvar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SeletorDeArquivos.salvar(JanelaJogador.this, ouvinteArquivos);
		}
	}

	private class AcaoRecuperar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SeletorDeArquivos.recuperar(JanelaJogador.this, ouvinteArquivos);
		}
	}

	private class Fechamento extends WindowAdapter {
		@Override
		public void windowClosed(WindowEvent e) {
			cartela.desregistrar();
			cartas.desregistrar();
			JanelaJogador.this.modelo.remove(JanelaJogador.this);
		}
	}

	@Override
	public void notify(ObservadoIF observado) {
		ThreadSwing.executar(new Runnable() {
			@Override
			public void run() {
				atualizar();
			}
		});
	}

	/**
	 * Salvar so fica habilitado imediatamente antes de o jogador da vez comecar a jogar:
	 * partida em andamento e nenhuma coluna da cartela ativada ainda neste turno.
	 */
	private boolean podeSalvar() {
		return modelo.podeSalvar(); // a regra fica no Model
	}

	private void atualizar() {
		botaoSalvar.setEnabled(podeSalvar());
		melhorias.atualizar();
		if (modelo.isPartidaIniciada() && modelo.getIdJogadorDaVez() >= 0)
			setTitle("Vez de " + modelo.getNomeJogador(modelo.getIdJogadorDaVez()));
		else
			setTitle("Jogador da vez");
	}
}
