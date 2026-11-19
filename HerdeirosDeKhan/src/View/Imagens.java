package View;

import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/** Carrega cada imagem uma unica vez (cache por caminho). Retorna null se nao achar. */
public final class Imagens {
	private static final Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();

	private Imagens() {
	}

	public static BufferedImage carregar(String caminho) {
		if (cache.containsKey(caminho))
			return cache.get(caminho);
		BufferedImage img = null;
		try {
			File f = new File(caminho);
			if (f.exists())
				img = ImageIO.read(f);
		} catch (IOException e) {
			img = null;
		}
		cache.put(caminho, img);
		return img;
	}

	private static final String[] EXTENSOES = { ".png", ".jpg", ".jpeg" };

	/**
	 * Procura imagens/<nome>.png, .jpg ou .jpeg (o caminho vem sem extensao). Devolve null se
	 * nao achar e avisa UMA vez no console qual arquivo faltou, para facilitar achar o nome certo.
	 */
	public static BufferedImage procurar(String semExtensao) {
		for (String ext : EXTENSOES) {
			BufferedImage img = carregar(semExtensao + ext);
			if (img != null)
				return img;
		}
		if (!avisados.containsKey(semExtensao)) {
			avisados.put(semExtensao, Boolean.TRUE);
			System.out.println("[imagens] nao encontrada: " + semExtensao + ".png (ou .jpg)");
		}
		return null;
	}

	/** Como procurar, mas sem avisar no console quando nao acha. */
	public static BufferedImage silenciosa(String semExtensao) {
		for (String ext : EXTENSOES) {
			BufferedImage img = carregar(semExtensao + ext);
			if (img != null)
				return img;
		}
		return null;
	}

	private static final Map<String, Boolean> avisados = new HashMap<String, Boolean>();

	/** "Conselheiro de Rússia" -> "conselheiro_de_russia": minusculas, sem acento, so letras e numeros. */
	public static String nomeDeArquivo(String texto) {
		if (texto == null)
			return "";
		String t = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
		return t.replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
	}

	/** Reducao em passos de metade (melhor qualidade do que um unico drawImage grande). */
	public static BufferedImage redimensionar(BufferedImage origem, int w, int h) {
		BufferedImage atual = origem;
		int cw = origem.getWidth();
		int ch = origem.getHeight();
		while (cw / 2 >= w && ch / 2 >= h) {
			cw /= 2;
			ch /= 2;
			atual = passo(atual, cw, ch);
		}
		return passo(atual, w, h);
	}

	private static BufferedImage passo(BufferedImage origem, int w, int h) {
		BufferedImage destino = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = destino.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(origem, 0, 0, w, h, null);
		g.dispose();
		return destino;
	}
}
