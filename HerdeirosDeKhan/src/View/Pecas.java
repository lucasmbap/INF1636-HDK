package View;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Fabrica das imagens das pecas do jogo (todas exibidas depois com drawImage).
 *
 * - Pinos (jogadores e Khan) e yurts (discos na cor do jogador) sao gerados
 *   uma vez com Java2D, no tamanho exato em pixels em que serao mostrados.
 * - Pecas de tributo usam as imagens da pasta imagens/ (tributo_yurt.jpg,
 *   tributo_espada.jpg e tributo_moeda.jpg), recortadas em circulo.
 *
 * Tudo fica em HashMap, entao a busca de cada imagem e em tempo constante.
 * Os tamanhos abaixo estao em unidades do tabuleiro de referencia.
 */
public final class Pecas {
	public static final String PASTA = "imagens/";

	public static final int PEAO_W = 52;
	public static final int PEAO_H = 72;
	public static final int YURT_D = 44;
	public static final int YURT_CIDADE_D = 84;
	public static final int TRIBUTO_D = 72;

	private static final Map<Long, BufferedImage> peoes = new HashMap<Long, BufferedImage>();
	private static final Map<Long, BufferedImage> yurts = new HashMap<Long, BufferedImage>();
	private static final Map<String, BufferedImage> tributos = new HashMap<String, BufferedImage>();

	private Pecas() {
	}

	private static long chave(Color c, int a, int b) {
		return ((long) (c.getRGB() & 0xFFFFFF) << 40) | ((long) a << 20) | b;
	}

	private static int px(double unidades, double escala) {
		return Math.max(6, (int) Math.round(unidades * escala));
	}

	/** Pino da cor indicada (o Khan e o pino branco). */
	public static BufferedImage peao(Color cor, double escala) {
		int w = px(PEAO_W, escala);
		int h = px(PEAO_H, escala);
		long k = chave(cor, w, h);
		BufferedImage img = peoes.get(k);
		if (img == null) {
			img = criarPeao(cor, w, h);
			peoes.put(k, img);
		}
		return img;
	}

	/** Yurt: disco na cor do jogador com a silhueta de um yurt. diametro em unidades do tabuleiro. */
	public static BufferedImage yurt(Color cor, int diametro, double escala) {
		int d = px(diametro, escala);
		long k = chave(cor, d, d);
		BufferedImage img = yurts.get(k);
		if (img == null) {
			img = criarYurt(cor, d);
			yurts.put(k, img);
		}
		return img;
	}

	/** Peca de tributo: tipo "yurt", "espada" ou "moeda". */
	public static BufferedImage tributo(String tipo, double escala) {
		int d = px(TRIBUTO_D, escala);
		String k = tipo + "@" + d;
		BufferedImage img = tributos.get(k);
		if (img == null) {
			BufferedImage origem = Imagens.carregar(PASTA + "tributo_" + tipo + ".jpg");
			img = origem == null ? discoSubstituto(d) : circular(origem, d);
			tributos.put(k, img);
		}
		return img;
	}

	// ------------------------------------------------------------------

	private static BufferedImage novaImagem(int w, int h) {
		return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	}

	private static void qualidade(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	}

	private static BufferedImage criarPeao(Color cor, int w, int h) {
		BufferedImage img = novaImagem(w, h);
		Graphics2D g = img.createGraphics();
		qualidade(g);
		g.scale((double) w / PEAO_W, (double) h / PEAO_H);
		g.translate(PEAO_W / 2.0, PEAO_H / 2.0 + 1);

		Path2D corpo = new Path2D.Double();
		corpo.moveTo(-8, -10);
		corpo.lineTo(8, -10);
		corpo.lineTo(18, 24);
		corpo.lineTo(-18, 24);
		corpo.closePath();
		Ellipse2D cabeca = new Ellipse2D.Double(-12, -33, 24, 24);
		Ellipse2D base = new Ellipse2D.Double(-23, 18, 46, 13);

		g.setColor(cor);
		g.fill(base);
		g.fill(corpo);
		g.fill(cabeca);
		g.setColor(new Color(255, 255, 255, 90)); // brilho na cabeca
		g.fill(new Ellipse2D.Double(-7, -29, 9, 9));
		g.setStroke(new BasicStroke(4f));
		g.setColor(Color.BLACK);
		g.draw(base);
		g.draw(corpo);
		g.draw(cabeca);
		g.dispose();
		return img;
	}

	private static BufferedImage criarYurt(Color cor, int d) {
		BufferedImage img = novaImagem(d, d);
		Graphics2D g = img.createGraphics();
		qualidade(g);
		float borda = Math.max(1.2f, d * 0.07f);
		Ellipse2D disco = new Ellipse2D.Double(borda / 2, borda / 2, d - borda, d - borda);
		g.setColor(cor);
		g.fill(disco);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(borda));
		g.draw(disco);

		g.translate(d / 2.0, d * 0.55);
		double s = d / 44.0 * 0.62;
		g.scale(s, s);
		Path2D forma = new Path2D.Double();
		forma.moveTo(-18, 10);
		forma.lineTo(-18, -2);
		forma.quadTo(0, -28, 18, -2);
		forma.lineTo(18, 10);
		forma.closePath();
		g.setColor(Cores.textoSobre(cor));
		g.fill(forma);
		g.setColor(cor);
		g.fillRect(-4, 0, 8, 10);
		g.dispose();
		return img;
	}

	/** Recorta a imagem num circulo (borda suave) no tamanho d x d. */
	private static BufferedImage circular(BufferedImage origem, int d) {
		BufferedImage reduzida = Imagens.redimensionar(origem, d, d);
		BufferedImage saida = novaImagem(d, d);
		Graphics2D g = saida.createGraphics();
		qualidade(g);
		g.setColor(Color.WHITE);
		g.fill(new Ellipse2D.Double(0, 0, d, d));
		g.setComposite(AlphaComposite.SrcIn);
		g.drawImage(reduzida, 0, 0, null);
		g.dispose();
		return saida;
	}

	/** Usado so se o arquivo da imagem do tributo nao existir. */
	private static BufferedImage discoSubstituto(int d) {
		BufferedImage img = novaImagem(d, d);
		Graphics2D g = img.createGraphics();
		qualidade(g);
		g.setColor(new Color(200, 190, 170));
		g.fill(new Ellipse2D.Double(1, 1, d - 2, d - 2));
		g.setColor(Color.DARK_GRAY);
		g.draw(new Ellipse2D.Double(1, 1, d - 2, d - 2));
		g.dispose();
		return img;
	}
}
