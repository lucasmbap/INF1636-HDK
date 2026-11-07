package View;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/** Painel que mostra uma imagem inteira, centralizada e escalada para caber (so com drawImage). */
public class PainelImagem extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Retangulo (em pixels do painel) em que a imagem foi desenhada; imgW = 0 se nao ha imagem. */
	protected int imgX = 0;
	protected int imgY = 0;
	protected int imgW = 0;
	protected int imgH = 0;

	private BufferedImage imagem = null;
	private BufferedImage escalada = null;
	private String mensagem = "";

	/** Onde (em pixels do painel) e com que escala a imagem foi desenhada da ultima vez. */
	protected int origemX = 0;
	protected int origemY = 0;
	protected double escalaTela = 0;

	public PainelImagem() {
		setBackground(new Color(30, 30, 30));
	}

	public BufferedImage getImagem() {
		return imagem;
	}

	public void setImagem(BufferedImage nova, String mensagemSeVazia) {
		this.imagem = nova;
		this.escalada = null;
		this.mensagem = mensagemSeVazia == null ? "" : mensagemSeVazia;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		Graphics2D g = (Graphics2D) gr.create();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if (imagem == null || getWidth() <= 0 || getHeight() <= 0) {
			escalaTela = 0;
			imgW = 0;
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(mensagem, 20, 30);
			g.dispose();
			return;
		}
		double e = Math.min((double) getWidth() / imagem.getWidth(), (double) getHeight() / imagem.getHeight());
		int w = Math.max(1, (int) Math.round(imagem.getWidth() * e));
		int h = Math.max(1, (int) Math.round(imagem.getHeight() * e));
		if (escalada == null || escalada.getWidth() != w || escalada.getHeight() != h)
			escalada = Imagens.redimensionar(imagem, w, h);
		origemX = (getWidth() - w) / 2;
		origemY = (getHeight() - h) / 2;
		escalaTela = e;
		imgX = origemX;
		imgY = origemY;
		imgW = w;
		imgH = h;
		g.drawImage(escalada, origemX, origemY, null);
		depoisDaImagem(g);
		g.dispose();
	}

	/** Gancho para as subclasses desenharem por cima da imagem (g usa coordenadas do painel). */
	protected void depoisDaImagem(Graphics2D g) {
	}
}
