package View;

import java.awt.Color;

/** Converte o nome da cor guardado no Model em java.awt.Color. Ajuste conforme as cores que voce usa. */
public final class Cores {
	private Cores() {
	}

	public static Color de(String cor) {
		if (cor == null)
			return Color.GRAY;
		String c = cor.toLowerCase();
		if (c.startsWith("verm") || c.equals("red"))
			return new Color(200, 30, 30);
		if (c.startsWith("azul") || c.equals("blue"))
			return new Color(40, 90, 210);
		if (c.startsWith("verde") || c.equals("green"))
			return new Color(30, 150, 60);
		if (c.startsWith("amar") || c.equals("yellow"))
			return new Color(235, 200, 30);
		if (c.startsWith("rox") || c.startsWith("viol") || c.equals("purple"))
			return new Color(130, 50, 170);
		if (c.startsWith("lar") || c.equals("orange"))
			return new Color(240, 140, 30);
		if (c.startsWith("bran") || c.equals("white"))
			return Color.WHITE;
		if (c.startsWith("pret") || c.equals("black"))
			return Color.BLACK;
		return Color.GRAY;
	}

	public static Color textoSobre(Color fundo) {
		double lum = 0.299 * fundo.getRed() + 0.587 * fundo.getGreen() + 0.114 * fundo.getBlue();
		return lum > 140 ? Color.BLACK : Color.WHITE;
	}
}
