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
 * ATENCAO: o Controller (ControladorJogo.paradaNoClique) calcula em qual parada
 * o jogador clicou com a MESMA conta usada aqui:
 *   escala  = min(largura / LARGURA_REF, altura / ALTURA_REF)
 *   origemX = (largura - LARGURA_REF * escala) / 2
 *   origemY = (altura  - ALTURA_REF  * escala) / 2
 * Por isso o tabuleiro ocupa o componente inteiro, sem margens. Se esta conta
 * mudar aqui, o clique para de bater com o desenho.
 *
 * O tabuleiro e sempre tratado no tamanho de referencia de CoordenadasMapa,
 * qualquer que seja a resolucao do arquivo de imagem.
 */
public abstract class PainelMapa extends JPanel {
	private static final long serialVersionUID = 1L;

	protected final BufferedImage mapa;

	/** Posicao e escala com que o tabuleiro foi desenhado no ultimo paintComponent. */
	protected int origemX = 0;
	protected int origemY = 0;
	protected double escala = 0;
	protected int larguraDesenhada = 0;
	protected int alturaDesenhada = 0;

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
		if (mapa == null || getWidth() <= 0 || getHeight() <= 0) {
			g.setColor(Color.WHITE);
			g.drawString("Imagem do tabuleiro nao encontrada.", 20, 40);
			g.dispose();
			escala = 0;
			return null;
		}
		escala = Math.min((double) getWidth() / CoordenadasMapa.LARGURA_REF,
				(double) getHeight() / CoordenadasMapa.ALTURA_REF);
		larguraDesenhada = Math.max(1, (int) Math.round(CoordenadasMapa.LARGURA_REF * escala));
		alturaDesenhada = Math.max(1, (int) Math.round(CoordenadasMapa.ALTURA_REF * escala));
		origemX = (getWidth() - larguraDesenhada) / 2;
		origemY = (getHeight() - alturaDesenhada) / 2;

		if (mapaEscalado == null || mapaEscalado.getWidth() != larguraDesenhada
				|| mapaEscalado.getHeight() != alturaDesenhada)
			mapaEscalado = Imagens.redimensionar(mapa, larguraDesenhada, alturaDesenhada);
		g.drawImage(mapaEscalado, origemX, origemY, null);

		g.translate(origemX, origemY);
		g.scale(escala, escala);
		return g;
	}
}
