package View;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import Model.ModeloApi;
import Model.ObservadorModelo;

/**
 * Componente do tabuleiro: desenha a imagem, os tributos, yurts, pinos, o Khan
 * e as cidades conquistadas, tudo com drawImage, e a faixa do jogador da vez.
 *
 * Esta classe so LE o Model. Quem trata os cliques e o Controller
 * (Controller.Main.iniciar recebe este componente e registra o MouseListener).
 */
public class PainelTabuleiro extends PainelMapa implements ObservadorModelo {
	private static final long serialVersionUID = 1L;

	/** Cada jogador tem 12 yurts (mesmo valor de Partida.MAX_YURTS, que nao e publico). */
	public static final int MAX_YURTS = 12;

	private static final int ALTURA_FAIXA = 22;
	private static final int DIAMETRO_RESERVA = 16;

	private final ModeloApi modelo;

	public PainelTabuleiro(ModeloApi modelo, String caminhoTabuleiro) {
		super(caminhoTabuleiro);
		this.modelo = modelo;
		modelo.registrarObservador(this);
	}

	@Override
	public void modeloAlterado() {
		repaint();
	}

	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		Graphics2D g = iniciarMapa(gr);
		if (g == null)
			return;
		if (modelo.isPartidaIniciada()) {
			desenharTributos(g);
			desenharCidades(g);
			desenharParadas(g);
			desenharKhan(g);
		}
		g.dispose();

		Graphics2D h = (Graphics2D) gr.create();
		h.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		h.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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

			boolean karakorum = parada.equals("Karakorum");
			// paradas duplas: cada pino/yurt vai sobre um dos 2 circulos impressos (nao entre eles)
			Point[] circulos = PosicoesDuplas.circulos(parada);

			List<String> peoes = modelo.getPeoesNaParada(parada);
			int n = peoes.size();

			if (parada.equals(posVez)) { // anel em volta do pino da vez
				Point a = c;
				if (circulos != null)
					a = circulos[Math.max(0, Math.min(circulos.length - 1, peoes.indexOf(modelo.getCorDaVez())))];
				g.setStroke(new BasicStroke(14));
				g.setColor(Color.BLACK);
				g.draw(new Ellipse2D.Double(a.x - 62, a.y - 62, 124, 124));
				g.setStroke(new BasicStroke(7));
				g.setColor(Color.WHITE);
				g.draw(new Ellipse2D.Double(a.x - 62, a.y - 62, 124, 124));
			}

			List<String> yurts = modelo.getYurtsNaParada(parada);
			for (int i = 0; i < yurts.size(); i++) {
				double x = c.x + (i - (yurts.size() - 1) / 2.0) * 48;
				double y = c.y + 28;
				if (circulos != null) {
					Point q = circulos[Math.min(i, circulos.length - 1)];
					x = q.x;
					y = q.y + 28;
				}
				int d = Pecas.YURT_D;
				desenharImagem(g, Pecas.yurt(Cores.de(yurts.get(i)), d, escala), x, y, d, d);
			}

			for (int i = 0; i < n; i++) {
				double x = c.x + (i - (n - 1) / 2.0) * (karakorum ? 46 : 40);
				double y = karakorum ? c.y : c.y - 12;
				if (circulos != null) {
					Point q = circulos[Math.min(i, circulos.length - 1)];
					x = q.x;
					y = q.y - 12;
				}
				desenharImagem(g, Pecas.peao(Cores.de(peoes.get(i)), escala), x, y, Pecas.PEAO_W, Pecas.PEAO_H);
			}
		}
	}

	// ------------------------------------------------------------------
	// Faixa do jogador da vez (em pixels do painel, sobre a borda de cima do tabuleiro)
	// ------------------------------------------------------------------

	/** Quantos yurts do jogador ja estao no mapa (em paradas ou no centro de cidades conquistadas). */
	private int yurtsNoMapa(String cor) {
		int n = 0;
		for (String parada : modelo.getParadas())
			for (String dono : modelo.getYurtsNaParada(parada))
				if (cor.equals(dono))
					n++;
		for (String cidade : modelo.getCidades())
			if (cor.equals(modelo.getConquistadorCidade(cidade)))
				n++;
		return n;
	}

	/**
	 * Indica o jogador da vez pela cor da faixa e por texto, direto no tabuleiro
	 * (sem JOptionPane), e mostra as 12 pecas de yurt dele: cheias = na reserva,
	 * apagadas = ja colocadas no mapa.
	 */
	private void desenharFaixaDaVez(Graphics2D g) {
		if (!modelo.isPartidaIniciada() || escala <= 0)
			return;
		String cor = modelo.getCorDaVez();
		if (cor == null)
			return;
		Color fundo = Cores.de(cor);
		Color texto = Cores.textoSobre(fundo);
		int x0 = origemX;
		int y0 = origemY;
		int w = getWidth();

		g.setColor(new Color(fundo.getRed(), fundo.getGreen(), fundo.getBlue(), 238));
		g.fillRect(x0, y0, w, ALTURA_FAIXA);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1.5f));
		g.drawLine(x0, y0 + ALTURA_FAIXA, x0 + w, y0 + ALTURA_FAIXA);

		int id = modelo.getIdJogadorDaVez();
		String nome = id >= 0 ? modelo.getNomeJogador(id) : cor;
		String herdeiro = id >= 0 ? modelo.getHerdeiro(id) : "";
		String votos = id >= 0 ? "   |   Votos: " + modelo.getVotos(id) : "";
		g.setColor(texto);
		g.setFont(new Font("SansSerif", Font.BOLD, 13));
		g.drawString("Vez de " + nome + (herdeiro == null || herdeiro.isEmpty() ? "" : " (" + herdeiro + ")")
				+ "   |   Movimentos restantes: " + modelo.getMovimentosRestantes() + votos, x0 + 10,
				y0 + ALTURA_FAIXA / 2 + 5);

		// reserva de yurts: 12 pecas redondas da cor do jogador
		int noMapa = Math.min(MAX_YURTS, yurtsNoMapa(cor));
		int reserva = MAX_YURTS - noMapa;
		int passo = DIAMETRO_RESERVA + 3;
		int largura = MAX_YURTS * passo + 8;
		int xi = x0 + w - largura - 6;
		int yi = y0 + (ALTURA_FAIXA - DIAMETRO_RESERVA) / 2;
		g.setColor(new Color(0, 0, 0, 120));
		g.fill(new RoundRectangle2D.Double(xi - 4, yi - 3, largura, DIAMETRO_RESERVA + 6, 10, 10));
		BufferedImage disco = Pecas.yurt(fundo, DIAMETRO_RESERVA, 1.0);
		java.awt.Composite normal = g.getComposite();
		for (int i = 0; i < MAX_YURTS; i++) {
			g.setComposite(i < reserva ? normal : AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
			g.drawImage(disco, xi + i * passo, yi, DIAMETRO_RESERVA, DIAMETRO_RESERVA, null);
		}
		g.setComposite(normal);
	}
}
