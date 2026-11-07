package View;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import Model.ModeloApi;
import Model.ObservadoIF;
import Model.ObservadorIF;

/**
 * Cartas de conselheiro. Mostra TODAS as cartas do jogador da vez (abertas e
 * secretas) e, separadas, apenas as cartas ABERTAS dos outros jogadores: as
 * secretas dos outros nunca aparecem. Observadora do Model.
 *
 * As cartas sao desenhadas com Java2D (texto do Model); quando houver imagens
 * das cartas, basta trocar desenharCarta por drawImage.
 */
public class JanelaCartas extends JPanel implements ObservadorIF {
	private static final long serialVersionUID = 1L;

	private static final int LARG = 156;
	private static final int ALT = 200;
	private static final int FOLGA = 10;
	private static final int COLUNAS = 3;
	private static final int TITULO_SECAO = 28;

	private final ModeloApi modelo;
	private final PainelCartas painel = new PainelCartas();
	private final JLabel titulo = new JLabel(" ", SwingConstants.CENTER);

	public JanelaCartas(ModeloApi modelo) {
		this.modelo = modelo;
		setLayout(new BorderLayout());

		titulo.setOpaque(true);
		titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
		titulo.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		add(titulo, BorderLayout.NORTH);
		add(new JScrollPane(painel), BorderLayout.CENTER);

		modelo.add(this);
		atualizar();
	}

	/** Deixa de observar o Model (chamado quando a janela que contem esta aba fecha). */
	public void desregistrar() {
		modelo.remove(this);
	}

	@Override
	public void notify(ObservadoIF o) {
		ThreadSwing.executar(new Runnable() {
			@Override
			public void run() {
				atualizar();
			}
		});
	}

	private void atualizar() {
		int id = modelo.isPartidaIniciada() ? modelo.getIdJogadorDaVez() : -1;
		if (id < 0) {
			titulo.setText("Aguardando o início da partida");
			titulo.setBackground(Color.LIGHT_GRAY);
			titulo.setForeground(Color.BLACK);
		} else {
			Color cor = Cores.de(modelo.getCorJogador(id));
			titulo.setBackground(cor);
			titulo.setForeground(Cores.textoSobre(cor));
			titulo.setText("Cartas de " + modelo.getNomeJogador(id));
		}
		painel.recalcular(id);
		painel.revalidate();
		painel.repaint();
	}

	// ------------------------------------------------------------------

	private int linhas(int cartas) {
		return (cartas + COLUNAS - 1) / COLUNAS;
	}

	private int totalJogadores() {
		return modelo.getCores().size();
	}

	private int quantidadeAbertasDosOutros(int idVez) {
		int n = 0;
		for (int id = 1; id <= totalJogadores(); id++) {
			if (id == idVez)
				continue;
			for (int i = 0; i < modelo.getQuantidadeCartasConselheiro(id); i++)
				if (modelo.isCartaConselheiroAberta(id, i))
					n++;
		}
		return n;
	}

	private class PainelCartas extends JPanel {
		private static final long serialVersionUID = 1L;

		PainelCartas() {
			setBackground(new Color(45, 35, 30));
		}

		void recalcular(int idVez) {
			int minhas = idVez < 0 ? 0 : modelo.getQuantidadeCartasConselheiro(idVez);
			int outras = idVez < 0 ? 0 : quantidadeAbertasDosOutros(idVez);
			int altura = TITULO_SECAO + Math.max(1, linhas(minhas)) * (ALT + FOLGA) + TITULO_SECAO
					+ Math.max(1, linhas(outras)) * (ALT + FOLGA) + FOLGA;
			setPreferredSize(new Dimension(COLUNAS * (LARG + FOLGA) + FOLGA, altura));
		}

		@Override
		protected void paintComponent(Graphics gr) {
			super.paintComponent(gr);
			Graphics2D g = (Graphics2D) gr.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int idVez = modelo.isPartidaIniciada() ? modelo.getIdJogadorDaVez() : -1;
			if (idVez < 0) {
				g.dispose();
				return;
			}

			int y = 0;
			secao(g, "Suas cartas (secretas só você vê)", y);
			y += TITULO_SECAO;
			int n = modelo.getQuantidadeCartasConselheiro(idVez);
			if (n == 0)
				vazio(g, "Você não tem cartas de conselheiro.", y);
			for (int i = 0; i < n; i++)
				carta(g, idVez, i, i, y, Cores.de(modelo.getCorJogador(idVez)), false);
			y += Math.max(1, linhas(n)) * (ALT + FOLGA);

			secao(g, "Cartas abertas dos outros jogadores", y);
			y += TITULO_SECAO;
			int posicao = 0;
			for (int id = 1; id <= totalJogadores(); id++) {
				if (id == idVez)
					continue;
				for (int i = 0; i < modelo.getQuantidadeCartasConselheiro(id); i++) {
					if (!modelo.isCartaConselheiroAberta(id, i))
						continue; // as secretas dos outros nunca sao mostradas
					carta(g, id, i, posicao++, y, Cores.de(modelo.getCorJogador(id)), true);
				}
			}
			if (posicao == 0)
				vazio(g, "Nenhum outro jogador tem carta aberta.", y);
			g.dispose();
		}

		private void secao(Graphics2D g, String texto, int y) {
			g.setColor(new Color(245, 225, 170));
			g.setFont(new Font("SansSerif", Font.BOLD, 13));
			g.drawString(texto, FOLGA, y + 19);
		}

		private void vazio(Graphics2D g, String texto, int y) {
			g.setColor(Color.LIGHT_GRAY);
			g.setFont(new Font("SansSerif", Font.ITALIC, 13));
			g.drawString(texto, FOLGA, y + 24);
		}

		private void carta(Graphics2D g, int id, int indice, int posicao, int yBase, Color dono, boolean mostrarDono) {
			int x = FOLGA + (posicao % COLUNAS) * (LARG + FOLGA);
			int y = yBase + (posicao / COLUNAS) * (ALT + FOLGA);
			boolean aberta = modelo.isCartaConselheiroAberta(id, indice);
			desenharCarta(g, x, y, modelo.getNomeCartaConselheiro(id, indice),
					modelo.getCondicaoCartaConselheiro(id, indice), modelo.getRecompensaCartaConselheiro(id, indice),
					aberta, dono, mostrarDono ? modelo.getNomeJogador(id) : null);
		}
	}

	private void desenharCarta(Graphics2D g, int x, int y, String nome, String condicao, String recompensa,
			boolean aberta, Color dono, String nomeDono) {
		Color fundo = aberta ? new Color(245, 235, 205) : new Color(70, 45, 95);
		Color texto = aberta ? new Color(40, 25, 10) : new Color(240, 230, 250);
		g.setColor(fundo);
		g.fill(new RoundRectangle2D.Double(x, y, LARG, ALT, 16, 16));
		g.setColor(dono);
		g.setStroke(new BasicStroke(5));
		g.draw(new RoundRectangle2D.Double(x, y, LARG, ALT, 16, 16));

		int tx = x + 10;
		int largura = LARG - 20;
		int ty = y + 20;
		g.setColor(texto);
		g.setFont(new Font("SansSerif", Font.BOLD, 10));
		g.drawString(aberta ? "ABERTA" : "SECRETA", tx, ty);
		if (nomeDono != null) {
			FontMetrics fm = g.getFontMetrics();
			g.drawString(nomeDono, x + LARG - 10 - fm.stringWidth(nomeDono), ty);
		}
		ty += 18;
		g.setFont(new Font("SansSerif", Font.BOLD, 12));
		ty = quebrar(g, nome, tx, ty, largura, 15, 2) + 4;
		g.setFont(new Font("SansSerif", Font.BOLD, 10));
		g.drawString("Condição", tx, ty);
		g.setFont(new Font("SansSerif", Font.PLAIN, 11));
		ty = quebrar(g, condicao, tx, ty + 13, largura, 13, 4) + 6;
		g.setFont(new Font("SansSerif", Font.BOLD, 10));
		g.drawString("Recompensa", tx, ty);
		g.setFont(new Font("SansSerif", Font.PLAIN, 11));
		quebrar(g, recompensa, tx, ty + 13, largura, 13, 3);
	}

	/** Escreve o texto quebrado em linhas; devolve o y logo abaixo da ultima linha escrita. */
	private static int quebrar(Graphics2D g, String texto, int x, int y, int largura, int alturaLinha,
			int maxLinhas) {
		if (texto == null)
			texto = "";
		FontMetrics fm = g.getFontMetrics();
		String[] palavras = texto.split("\\s+");
		StringBuilder linha = new StringBuilder();
		int escritas = 0;
		for (int i = 0; i < palavras.length; i++) {
			String tentativa = linha.length() == 0 ? palavras[i] : linha + " " + palavras[i];
			if (fm.stringWidth(tentativa) <= largura || linha.length() == 0) {
				linha = new StringBuilder(tentativa);
			} else {
				g.drawString(linha.toString(), x, y);
				y += alturaLinha;
				escritas++;
				linha = new StringBuilder(palavras[i]);
				if (escritas >= maxLinhas)
					return y;
			}
		}
		if (linha.length() > 0 && escritas < maxLinhas) {
			g.drawString(linha.toString(), x, y);
			y += alturaLinha;
		}
		return y;
	}
}
