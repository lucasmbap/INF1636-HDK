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
import Model.ObservadoIF;
import Model.ObservadorIF;

/**
 * Componente do tabuleiro: desenha a imagem, os tributos, yurts, pinos, o Khan
 * e as cidades conquistadas, tudo com drawImage, e a faixa do jogador da vez.
 *
 * Esta classe so LE o Model. Quem trata os cliques e o Controller
 * (Controller.Main.iniciar recebe este componente e registra o MouseListener).
 */
public class PainelTabuleiro extends PainelMapa implements ObservadorIF {
	private static final long serialVersionUID = 1L;

	/** Cada jogador tem 12 yurts (mesmo valor de Partida.MAX_YURTS, que nao e publico). */
	public static final int MAX_YURTS = 12;

	private static final int ALTURA_FAIXA = 22;
	private static final int DIAMETRO_RESERVA = 16;

	private final ModeloApi modelo;

	public PainelTabuleiro(ModeloApi modelo, String caminhoTabuleiro) {
		super(caminhoTabuleiro);
		this.modelo = modelo;
		modelo.add(this);
	}

	/** Deixa de observar o Model (quando a janela do jogo e fechada para comecar outra partida). */
	public void desregistrar() {
		modelo.remove(this);
	}

	@Override
	public void notify(ObservadoIF o) {
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
			desenharTesouros(g);
			desenharParadas(g);
			desenharKhan(g);
			desenharAreaDeMelhorias(g);
			desenharTrilhaDeVotos(g);
			desenharExigenciaDeKarakorum(g);
			desenharBonus(g);
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

	/** Tesouros que ainda restam em cada cidade revelada, em fileira abaixo do nome da cidade. */
	private void desenharTesouros(Graphics2D g) {
		for (String cidade : modelo.getCidades()) {
			Point c = CoordenadasMapa.getCidade(cidade);
			if (c == null)
				continue;
			List<String> lista = modelo.getTesourosDaCidade(cidade);
			int n = lista.size();
			for (int i = 0; i < n; i++) {
				double x = c.x + (i - (n - 1) / 2.0) * 58;
				int d = Pecas.TESOURO_D;
				desenharImagem(g, Pecas.tesouro(lista.get(i), escala), x, c.y + 100, d, d);
			}
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

	// ------------------------------------------------------------------
	// Espacos do tabuleiro que ficavam vazios: cidades fechadas, area de melhorias e trilha de votos
	// (coordenadas medidas na imagem de referencia 2564 x 2576)
	// ------------------------------------------------------------------

	/** Desenha a imagem (procurada em imagens/) dentro do retangulo, sem distorcer. Devolve false se nao existe. */
	private boolean desenharCarta(Graphics2D g, String nomeSemExtensao, double cx, double cy, double w, double h) {
		BufferedImage img = Imagens.procurar(nomeSemExtensao);
		if (img == null)
			return false;
		double e = Math.min(w / img.getWidth(), h / img.getHeight());
		int iw = Math.max(1, (int) Math.round(img.getWidth() * e * escala));
		int ih = Math.max(1, (int) Math.round(img.getHeight() * e * escala));
		BufferedImage pronta = Imagens.redimensionar(img, iw, ih);
		g.drawImage(pronta, (int) Math.round(cx - img.getWidth() * e / 2), (int) Math.round(cy - img.getHeight() * e / 2),
				(int) Math.round(img.getWidth() * e), (int) Math.round(img.getHeight() * e), null);
		return true;
	}

	// as 5 casas retangulares da faixa de melhorias (esquerda, rodape)
	private static final int[] AREA_X = { 268, 501, 733, 965, 1198 };
	private static final int AREA_Y = 2471;
	private static final int AREA_W = 190;
	private static final int AREA_H = 100;
	private static final String[] AREA_TIPOS = { "punho", "moeda", "cavalos", "espada", "yurt" };

	/** As 5 pecas de melhoria que podem ser compradas ficam nas casas da area de melhorias. */
	private void desenharAreaDeMelhorias(Graphics2D g) {
		for (int i = 0; i < AREA_X.length; i++) {
			// usa imagens/melhoria_<tipo>; se so existirem as versoes coloridas, usa a primeira que achar
			String base = "imagens/melhoria_" + AREA_TIPOS[i];
			String[] candidatos = { base, base + "_amarela", base + "_verde", base + "_vermelha" };
			for (String c : candidatos)
				if (Imagens.silenciosa(c) != null) {
					desenharCarta(g, c, AREA_X[i], AREA_Y, AREA_W, AREA_H);
					break;
				}
		}
	}

	// trilha de votos (0 votos = marcadores na largada, a esquerda da 1a casa): 10 casas em cima (esq -> dir) e 7 embaixo (dir -> esq), 1 a 17 votos
	private static final int VOTO_MAX = 17;
	private static final double VOTO_D = 118;

	private static Point casaDoVoto(int v) {
		if (v <= 10)
			return new Point((int) Math.round(1010 + 164.7 * (v - 1)), 2322);
		return new Point((int) Math.round(2404 - 164.3 * (v - 11)), 2466);
	}

	/** Um marcador (com a imagem do herdeiro) para cada jogador, na casa do total de votos dele. */
	private void desenharTrilhaDeVotos(Graphics2D g) {
		java.util.Map<Integer, Integer> noMesmoLugar = new java.util.HashMap<Integer, Integer>();
		for (String cor : modelo.getCores()) {
			int id = idDaCor(cor);
			if (id < 0)
				continue;
			int votos = Math.min(VOTO_MAX, modelo.getVotos(id));
			if (votos < 1) {
				// sem votos: so um circulo, da cor do jogador, na 1a casa (todos juntos, um sobre o outro)
				Point p0 = casaDoVoto(1);
				g.setColor(Cores.de(cor));
				g.fill(new Ellipse2D.Double(p0.x - VOTO_D / 2, p0.y - VOTO_D / 2, VOTO_D, VOTO_D));
				continue;
			}
			Point p = casaDoVoto(votos);
			double x = p.x, y = p.y; // marcadores na mesma casa ficam exatamente um sobre o outro
			String herdeiro = modelo.getHerdeiro(id);
			g.setColor(Cores.de(cor));
			g.fill(new Ellipse2D.Double(x - VOTO_D / 2 - 8, y - VOTO_D / 2 - 8, VOTO_D + 16, VOTO_D + 16));
			java.awt.Shape clipAntigo = g.getClip();
			g.clip(new Ellipse2D.Double(x - VOTO_D / 2, y - VOTO_D / 2, VOTO_D, VOTO_D)); // marcador redondo
			boolean achou = desenharCarta(g, "imagens/marcador_" + Imagens.nomeDeArquivo(herdeiro), x, y, VOTO_D,
					VOTO_D);
			g.setClip(clipAntigo);
			if (!achou) {
				g.setColor(Color.WHITE);
				g.fill(new Ellipse2D.Double(x - VOTO_D / 2, y - VOTO_D / 2, VOTO_D, VOTO_D));
			}
		}
	}

	// as 3 casas dos tesouros exigidos no pergaminho impresso de Karakorum (imagem de referencia)
	private static final int[][] KARAKORUM_TESOUROS = { { 1144, 997 }, { 1106, 1070 }, { 1190, 1073 } };

	/**
	 * A peca de Karakorum sorteia 3 tesouros diferentes a cada partida; o tabuleiro impresso so mostra
	 * um exemplo. Cobrimos os 3 icones impressos com os tesouros realmente exigidos nesta partida.
	 */
	private void desenharExigenciaDeKarakorum(Graphics2D g) {
		List<String> exigidos = modelo.getTesourosExigidosEmKarakorum();
		for (int i = 0; i < exigidos.size() && i < KARAKORUM_TESOUROS.length; i++) {
			int d = Pecas.TESOURO_D;
			desenharImagem(g, Pecas.tesouro(exigidos.get(i), escala), KARAKORUM_TESOUROS[i][0],
					KARAKORUM_TESOUROS[i][1], d + 18, d + 18);
		}
	}

	// quadrados com a "1" no tabuleiro, um por conselheiro (centro na imagem de referencia 2564 x 2576)
	private static final String[] BONUS_PARADA = { "R2", "R11", "C5", "C10", "P1", "P8", "Karakorum" };
	private static final int[][] BONUS_XY = { { 1192, 157 }, { 2123, 224 }, { 2433, 1480 }, { 1491, 2136 },
			{ 207, 905 }, { 637, 1844 }, { 1281, 1236 } };
	private static final int BONUS_D = 150;

	/** A peca de bonus (2 moedas) cobre o quadrado do conselheiro ate o primeiro jogador entregar tesouros a ele. */
	private void desenharBonus(Graphics2D g) {
		for (int i = 0; i < BONUS_PARADA.length; i++)
			if (modelo.isBonusDisponivel(BONUS_PARADA[i]))
				desenharCarta(g, "imagens/bonus_moedas", BONUS_XY[i][0], BONUS_XY[i][1], BONUS_D, BONUS_D);
	}

	private int idDaCor(String cor) {
		for (int id = 1; id <= 12; id++) {
			try {
				if (cor.equals(modelo.getCorJogador(id)))
					return id;
			} catch (RuntimeException e) {
				// id inexistente
			}
		}
		return -1;
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
		String posVez = modelo.getPosicaoPeaoDaVez();
		String corVez = modelo.getCorDaVez();
		// chave do peao da vez no mapa: a Altani pode jogar com o 2o peao ("cor#2")
		String peaoVez = corVez != null && modelo.getPeaoEscolhido(corVez) == 2 ? corVez + "#2" : corVez;
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
					a = circulos[Math.max(0, Math.min(circulos.length - 1, peoes.indexOf(peaoVez)))];
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
				if (peoes.get(i).endsWith("#2")) { // 2o peao da Altani
					g.setFont(new Font("SansSerif", Font.BOLD, 34));
					g.setColor(Cores.textoSobre(Cores.de(peoes.get(i))));
					g.drawString("2", (int) Math.round(x) - 10, (int) Math.round(y) + 4);
				}
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
		int w = larguraDesenhada;

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
