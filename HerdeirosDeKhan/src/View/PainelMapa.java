package View;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Base do painel que mostra o tabuleiro. Desenha a imagem somente com
 * Graphics2D.drawImage(), escalada para caber no painel e centralizada.
 *
 * O tabuleiro e sempre tratado no tamanho de referencia de CoordenadasMapa
 * (LARGURA_REF x ALTURA_REF), qualquer que seja a resolucao do arquivo de
 * imagem. Assim as coordenadas de CoordenadasMapa valem sempre.
 */
public abstract class PainelMapa extends JPanel {
	private static final long serialVersionUID = 1L;

	protected final BufferedImage mapa;
	/** Altura (em pixels do painel) reservada no topo para a faixa do jogador da vez. */
	protected int margemTopo = 0;

	/** Posicao e escala com que o tabuleiro foi desenhado no ultimo paintComponent. */
	protected int origemX = 0;
	protected int origemY = 0;
	protected double escala = 0;

	private BufferedImage mapaEscalado = null;

	protected PainelMapa(String caminhoMapa) {
		mapa = Imagens.carregar(caminhoMapa);
		setBackground(new Color(30, 30, 30));
	}

	/**
	 * Desenha o tabuleiro e devolve um Graphics2D ja em coordenadas da imagem de
	 * referencia (quem chama desenha com as posicoes de CoordenadasMapa e
	 * deve dar dispose() no resultado). Devolve null se a imagem nao existe.
	 */
	protected Graphics2D iniciarMapa(Graphics gr) {
		Graphics2D g = (Graphics2D) gr.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		if (mapa == null) {
			g.setColor(Color.WHITE);
			g.drawString("Imagem do tabuleiro nao encontrada.", 20, margemTopo + 40);
			g.dispose();
			escala = 0;
			return null;
		}
		int disponivelW = getWidth();
		int disponivelH = getHeight() - margemTopo;
		double e = Math.min((double) disponivelW / CoordenadasMapa.LARGURA_REF,
				(double) disponivelH / CoordenadasMapa.ALTURA_REF);
		int w = Math.max(1, (int) Math.round(CoordenadasMapa.LARGURA_REF * e));
		int h = Math.max(1, (int) Math.round(CoordenadasMapa.ALTURA_REF * e));
		origemX = (disponivelW - w) / 2;
		origemY = margemTopo + (disponivelH - h) / 2;
		escala = (double) w / CoordenadasMapa.LARGURA_REF;

		if (mapaEscalado == null || mapaEscalado.getWidth() != w || mapaEscalado.getHeight() != h)
			mapaEscalado = Imagens.redimensionar(mapa, w, h);
		g.drawImage(mapaEscalado, origemX, origemY, null);

		g.translate(origemX, origemY);
		g.scale(escala, escala);
		return g;
	}

	/** Nome da parada sob o ponto (x, y) do painel, ou null. */
	protected String paradaEm(int x, int y) {
		if (escala <= 0)
			return null;
		return CoordenadasMapa.paradaEm(x, y, origemX, origemY, escala);
	}
}
