package View;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.List;

import Model.ModeloApi;
import Model.ObservadorModelo;

/**
 * Desenha o tabuleiro (imagem + tributos, yurts, pinos, Khan e cidades
 * conquistadas), tudo com drawImage, e avisa o ouvinte quando o jogador clica
 * sobre uma parada. Esta classe so LE o Model; nenhuma jogada e feita aqui.
 */
public class PainelTabuleiro extends PainelMapa implements ObservadorModelo {
	private static final long serialVersionUID = 1L;

	private static final int ALTURA_FAIXA = 34;

	private final ModeloApi modelo;
	private OuvinteJogo ouvinte = null;

	public PainelTabuleiro(ModeloApi modelo, String caminhoTabuleiro) {
		super(caminhoTabuleiro);
		this.modelo = modelo;
		this.margemTopo = ALTURA_FAIXA;
		addMouseListener(new TratadorClique());
		modelo.registrarObservador(this);
	}

	public void setOuvinte(OuvinteJogo ouvinte) {
		this.ouvinte = ouvinte;
	}

	@Override
	public void modeloAlterado() {
		repaint();
	}

	private class TratadorClique extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (ouvinte == null || !modelo.isPartidaIniciada())
				return;
			String parada = paradaEm(e.getX(), e.getY());
			if (parada != null)
				ouvinte.paradaClicada(parada);
		}
	}

	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		Graphics2D g = iniciarMapa(gr);
		if (g != null) {
			if (modelo.isPartidaIniciada()) {
				desenharTributos(g);
				desenharCidades(g);
				desenharParadas(g);
				desenharKhan(g);
			}
			g.dispose();
		}
		Graphics2D h = (Graphics2D) gr.create();
		desenharFaixaDaVez(h);
		h.dispose();
	}

	// ------------------------------------------------------------------
	// Elementos do mapa (g ja esta em coordenadas da imagem de referencia)
	// ------------------------------------------------------------------

	/** Desenha a imagem centrada em (cx, cy), ocupando w x h unidades do tabuleiro. */
	private void desenharImagem(Graphics2D g, BufferedImage img, double cx, double cy, double w, double h) {
		g.drawImage(img, (int) Math.round(cx - w / 2), (int) Math.round(cy - h / 2), (int) Math.round(w),
				(int) Math.round(h), null);
	}

	/** Cidade conquistada: yurt da cor do conquistador, acima do nome da cidade. */
	private void desenharCidades(Graphics2D g) {
		for (String cidade : modelo.getCidades()) {
			String dono = modelo.getConquistadorCidade(cidade);
			Point c = CoordenadasMapa.getCidade(cidade);
			if (dono == null || c == null)
				continue;
			int d = Pecas.YURT_CIDADE_D;
			desenharImagem(g, Pecas.yurt(Cores.de(dono), d, escala), c.x, c.y - 70, d, d);
		}
	}

	/**
	 * Pecas de tributo de cada provincia: ficam empilhadas sobre o icone impresso
	 * (de 1 a 3), com um selo mostrando a quantidade.
	 */
	private void desenharTributos(Graphics2D g) {
		g.setFont(new Font("SansSerif", Font.BOLD, 40));
		FontMetrics fm = g.getFontMetrics();
		for (String prov : modelo.getProvincias()) {
			Point p = CoordenadasMapa.getProvincia(prov);
			if (p == null)
				continue;
			int n = modelo.getTributos(prov);
			if (n <= 0)
				continue;
			BufferedImage img = Pecas.tributo(tipoTributo(prov), escala);
			for (int i = 0; i < n; i++)
				desenharImagem(g, img, p.x + i * 14, p.y - i * 14, Pecas.TRIBUTO_D, Pecas.TRIBUTO_D);

			int x = p.x + 52 + (n - 1) * 14;
			int y = p.y - 52 - (n - 1) * 14;
			g.setColor(new Color(60, 30, 10));
			g.fill(new Ellipse2D.Double(x - 22, y - 22, 44, 44));
			g.setColor(Color.WHITE);
			g.setStroke(new BasicStroke(4));
			g.draw(new Ellipse2D.Double(x - 22, y - 22, 44, 44));
			String t = String.valueOf(n);
			g.drawString(t, x - fm.stringWidth(t) / 2, y + fm.getAscent() / 2 - 4);
		}
	}

	/** Tipo de tributo da provincia: usa o tipo do Model e, se nao reconhecer, o prefixo do nome. */
	private String tipoTributo(String provincia) {
		Object t = modelo.getTipoTributo(provincia);
		String r = classificar(t == null ? null : t.toString());
		if (r == null)
			r = classificar(provincia);
		return r == null ? "yurt" : r;
	}

	private static String classificar(String texto) {
		if (texto == null)
			return null;
		String s = texto.toLowerCase();
		if (s.contains("espada") || s.contains("sword"))
			return "espada";
		if (s.contains("moeda") || s.contains("coin"))
			return "moeda";
		if (s.contains("yurt"))
			return "yurt";
		return null;
	}

	/** O Khan e um pino branco, igual aos dos jogadores, sobre a cabeca impressa onde ele esta. */
	private void desenharKhan(Graphics2D g) {
		String pos = modelo.getPosicaoKhan();
		if (pos == null)
			return;
		Point p = CoordenadasMapa.getKhan(pos);
		if (p == null)
			return;
		desenharImagem(g, Pecas.peao(Color.WHITE, escala), p.x, p.y, Pecas.PEAO_W, Pecas.PEAO_H);
	}

	private void desenharParadas(Graphics2D g) {
		String posVez = modelo.getPosicao(modelo.getCorDaVez());
		for (String parada : modelo.getParadas()) {
			Point c = CoordenadasMapa.getParada(parada);
			if (c == null)
				continue;

			if (parada.equals(posVez)) { // anel em volta do pino da vez
				g.setStroke(new BasicStroke(14));
				g.setColor(Color.BLACK);
				g.draw(new Ellipse2D.Double(c.x - 62, c.y - 62, 124, 124));
				g.setStroke(new BasicStroke(7));
				g.setColor(Color.WHITE);
				g.draw(new Ellipse2D.Double(c.x - 62, c.y - 62, 124, 124));
			}

			boolean karakorum = parada.equals("Karakorum");

			List<String> yurts = modelo.getYurtsNaParada(parada);
			for (int i = 0; i < yurts.size(); i++) {
				double x = c.x + (i - (yurts.size() - 1) / 2.0) * 48;
				int d = Pecas.YURT_D;
				desenharImagem(g, Pecas.yurt(Cores.de(yurts.get(i)), d, escala), x, c.y + 28, d, d);
			}

			List<String> peoes = modelo.getPeoesNaParada(parada);
			int n = peoes.size();
			for (int i = 0; i < n; i++) {
				double x = c.x + (i - (n - 1) / 2.0) * (karakorum ? 46 : 40);
				double y = karakorum ? c.y : c.y - 12;
				desenharImagem(g, Pecas.peao(Cores.de(peoes.get(i)), escala), x, y, Pecas.PEAO_W, Pecas.PEAO_H);
			}
		}
	}

	/** Indica o jogador da vez por cor e texto, direto no tabuleiro (sem JOptionPane). */
	private void desenharFaixaDaVez(Graphics2D g) {
		if (!modelo.isPartidaIniciada())
			return;
		Color cor = Cores.de(modelo.getCorDaVez());
		g.setColor(cor);
		g.fillRect(0, 0, getWidth(), ALTURA_FAIXA);
		g.setColor(Cores.textoSobre(cor));
		g.setFont(new Font("SansSerif", Font.BOLD, 16));
		int id = modelo.getIdJogadorDaVez();
		String nome = id >= 0 ? modelo.getNomeJogador(id) : "";
		g.drawString("Vez de " + nome + "   |   Movimentos restantes: " + modelo.getMovimentosRestantes(), 12, 23);
	}
}
