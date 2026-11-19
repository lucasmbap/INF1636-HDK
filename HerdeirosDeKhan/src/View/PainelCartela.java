package View;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import Model.ModeloApi;

/**
 * Imagem da cartela do jogador da vez com as 4 colunas em cima: a coluna ativada fica
 * contornada, as ocupadas por peca de ativacao ficam escurecidas, as melhorias de cada
 * coluna aparecem nas casas (de baixo para cima) e clicar numa coluna avisa o ouvinte.
 * As posicoes das colunas sao medidas na imagem original das cartelas (2576 x 1405).
 */
class PainelCartela extends PainelImagem {
	private static final long serialVersionUID = 1L;

	private static final double LARG_REF = 2576;
	private static final double ALT_REF = 1405;
	private static final double[][] COLUNAS_X = { { 678, 1065 }, { 1131, 1519 }, { 1585, 1972 }, { 2092, 2480 } };
	private static final double[][] LINHAS_Y = { { 135, 330 }, { 362, 560 }, { 593, 795 }, { 822, 1025 } };
	private static final double TOPO_COLUNA = 110;
	private static final double BASE_COLUNA = 1270;
	private static final String[] NOMES_COLUNA = { "vermelha", "verde", "amarela", "branca" };

	private final ModeloApi modelo;
	private OuvinteAcoes ouvinte = null;

	PainelCartela(ModeloApi modelo) {
		this.modelo = modelo;
		addMouseListener(new Clique());
	}

	void setOuvinte(OuvinteAcoes ouvinte) {
		this.ouvinte = ouvinte;
		setCursor(ouvinte == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	static String nomeDaColuna(int coluna) {
		return NOMES_COLUNA[coluna];
	}

	private int px(double refX) {
		return imgX + (int) Math.round(refX / LARG_REF * imgW);
	}

	private int py(double refY) {
		return imgY + (int) Math.round(refY / ALT_REF * imgH);
	}

	/** Coluna sob o ponto (x, y) do painel, ou -1. */
	int colunaEm(int x, int y) {
		if (imgW <= 0)
			return -1;
		double rx = (x - imgX) / (double) imgW * LARG_REF;
		double ry = (y - imgY) / (double) imgH * ALT_REF;
		if (ry < TOPO_COLUNA || ry > BASE_COLUNA)
			return -1;
		for (int c = 0; c < COLUNAS_X.length; c++)
			if (rx >= COLUNAS_X[c][0] - 30 && rx <= COLUNAS_X[c][1] + 30)
				return c;
		return -1;
	}

	private class Clique extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (ouvinte == null || !modelo.isPartidaIniciada())
				return;
			int c = colunaEm(e.getX(), e.getY());
			if (c >= 0)
				ouvinte.colunaEscolhida(c);
		}
	}

	@Override
	protected void depoisDaImagem(Graphics2D g) {
		if (!modelo.isPartidaIniciada() || imgW <= 0)
			return;
		String cor = modelo.getCorDaVez();
		if (cor == null)
			return;
		int ativada = modelo.getColunaAtivada();
		for (int c = 0; c < COLUNAS_X.length; c++) {
			int x0 = px(COLUNAS_X[c][0] - 12);
			int x1 = px(COLUNAS_X[c][1] + 12);
			int y0 = py(TOPO_COLUNA);
			int y1 = py(BASE_COLUNA);

			if (modelo.isColunaOcupada(cor, c)) {
				// coluna usada: a peca de ativacao do jogador (o emblema do herdeiro) fica na casa de baixo
				g.setColor(new Color(0, 0, 0, 70));
				g.fillRect(x0, y0, x1 - x0, y1 - y0);
				desenharPecaDeAtivacao(g, (x0 + x1) / 2, py(1275), (int) ((x1 - x0) * 0.30));
			}

			List<String> melhorias = modelo.getMelhoriasDaColuna(cor, c);
			for (int k = 0; k < melhorias.size() && k < LINHAS_Y.length; k++) {
				int linha = LINHAS_Y.length - 1 - k; // a primeira melhoria fica embaixo
				desenharMelhoria(g, melhorias.get(k), c, px(COLUNAS_X[c][0] + 14), py(LINHAS_Y[linha][0] + 14),
						px(COLUNAS_X[c][1] - 14) - px(COLUNAS_X[c][0] + 14),
						py(LINHAS_Y[linha][1] - 14) - py(LINHAS_Y[linha][0] + 14));
			}

		}
	}

	/** Peca de ativacao: o emblema do herdeiro do jogador da vez, redondo (imagens/marcador_<herdeiro>). */
	private void desenharPecaDeAtivacao(Graphics2D g, int cx, int cy, int d) {
		int id = modelo.getIdJogadorDaVez();
		String herdeiro = id >= 0 ? modelo.getHerdeiro(id) : null;
		java.awt.image.BufferedImage img = herdeiro == null ? null
				: Imagens.procurar("imagens/marcador_" + Imagens.nomeDeArquivo(herdeiro));
		java.awt.Shape clip = g.getClip();
		if (img != null) {
			g.clip(new java.awt.geom.Ellipse2D.Double(cx - d / 2.0, cy - d / 2.0, d, d));
			g.drawImage(Imagens.redimensionar(img, d, d), cx - d / 2, cy - d / 2, d, d, null);
			g.setClip(clip);
		} else {
			g.setColor(Color.WHITE);
			g.fillOval(cx - d / 2, cy - d / 2, d, d);
		}
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(3f));
		g.drawOval(cx - d / 2, cy - d / 2, d, d);
	}

	private static final String[] COR_DA_COLUNA = { "vermelha", "verde", "amarela", null };

	/**
	 * Cada peca de melhoria tem uma cor por regiao (manual, pag. 11): imagens/melhoria_<tipo>_<cor>
	 * (cor = vermelha, verde ou amarela). Na coluna branca vale qualquer cor. Se nao houver a imagem
	 * da cor, usa imagens/melhoria_<tipo> (sem cor) e, por fim, qualquer cor que existir.
	 */
	private BufferedImage imagemDaMelhoria(String nome, int coluna) {
		String base = "imagens/melhoria_" + Imagens.nomeDeArquivo(nome);
		String cor = coluna >= 0 && coluna < COR_DA_COLUNA.length ? COR_DA_COLUNA[coluna] : null;
		if (cor != null) {
			BufferedImage img = Imagens.silenciosa(base + "_" + cor);
			if (img != null)
				return img;
		}
		BufferedImage img = Imagens.silenciosa(base);
		if (img != null)
			return img;
		for (String c : COR_DA_COLUNA)
			if (c != null && (img = Imagens.silenciosa(base + "_" + c)) != null)
				return img;
		return Imagens.procurar(base); // so para avisar no console qual arquivo falta
	}

	private void desenharMelhoria(Graphics2D g, String nome, int coluna, int x, int y, int w, int h) {
		BufferedImage img = imagemDaMelhoria(nome, coluna);
		if (img != null) {
			double e = Math.min((double) w / img.getWidth(), (double) h / img.getHeight());
			int iw = Math.max(1, (int) Math.round(img.getWidth() * e));
			int ih = Math.max(1, (int) Math.round(img.getHeight() * e));
			g.drawImage(Imagens.redimensionar(img, iw, ih), x + (w - iw) / 2, y + (h - ih) / 2, null);
			return;
		}
		g.setColor(new Color(255, 255, 255, 215));
		g.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
		g.setColor(new Color(60, 35, 10));
		g.setStroke(new BasicStroke(2f));
		g.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
		g.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, imgW / 90)));
		FontMetrics fm = g.getFontMetrics();
		String t = nome.replace('_', ' ');
		g.drawString(t, x + (w - fm.stringWidth(t)) / 2, y + (h + fm.getAscent()) / 2 - 2);
	}
}
