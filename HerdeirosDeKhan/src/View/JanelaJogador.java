package View;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
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

	public JanelaJogador(ModeloApi modelo) {
		super("Jogador da vez");
		this.modelo = modelo;
		this.cartela = new JanelaCartela(modelo);
		this.cartas = new JanelaCartas(modelo);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new Fechamento());
		setLayout(new BorderLayout());
		JTabbedPane abas = new JTabbedPane();
		abas.addTab("Cartela", cartela);
		abas.addTab("Cartas de conselheiro", cartas);
		add(abas, BorderLayout.CENTER);

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

	private void atualizar() {
		if (modelo.isPartidaIniciada() && modelo.getIdJogadorDaVez() >= 0)
			setTitle("Vez de " + modelo.getNomeJogador(modelo.getIdJogadorDaVez()));
		else
			setTitle("Jogador da vez");
	}
}
