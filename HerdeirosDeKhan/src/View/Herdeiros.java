package View;

import java.awt.image.BufferedImage;

/** Herdeiros do jogo e suas cartelas (imagens/cartela_<nome em minusculas>.jpg). */
public final class Herdeiros {
	public static final String[] NOMES = { "Jochi", "Chagatai", "Tolui", "Altani", "Ogedei" };

	/** Cores que o jogador pode escolher (nomes entendidos por Cores.de). */
	public static final String[] CORES = { "vermelho", "azul", "verde", "amarelo", "roxo" };

	private Herdeiros() {
	}

	public static BufferedImage cartela(String herdeiro) {
		if (herdeiro == null)
			return null;
		return Imagens.carregar("imagens/cartela_" + herdeiro.toLowerCase() + ".jpg");
	}
}
