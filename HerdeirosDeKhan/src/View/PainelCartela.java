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
				g.setColor(new Color(0, 0, 0, 150));
				g.fillRect(x0, y0, x1 - x0, y1 - y0);
				g.setColor(Color.WHITE);
				g.setFont(new Font("SansSerif", Font.BOLD, Math.max(11, imgW / 70)));
				FontMetrics fm = g.getFontMetrics();
				String t = "OCUPADA";
				g.drawString(t, (x0 + x1 - fm.stringWidth(t)) / 2, (y0 + y1) / 2);
			}

			List<String> melhorias = modelo.getMelhoriasDaColuna(cor, c);
			for (int k = 0; k < melhorias.size() && k < LINHAS_Y.length; k++) {
				int linha = LINHAS_Y.length - 1 - k; // a primeira melhoria fica embaixo
				desenharMelhoria(g, melhorias.get(k), px(COLUNAS_X[c][0] + 14), py(LINHAS_Y[linha][0] + 14),
						px(COLUNAS_X[c][1] - 14) - px(COLUNAS_X[c][0] + 14),
						py(LINHAS_Y[linha][1] - 14) - py(LINHAS_Y[linha][0] + 14));
			}

			if (c == ativada) {
				g.setColor(new Color(255, 215, 0));
				g.setStroke(new BasicStroke(5f));
				g.drawRect(x0, y0, x1 - x0, y1 - y0);
			}
		}
	}

	private void desenharMelhoria(Graphics2D g, String nome, int x, int y, int w, int h) {
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
