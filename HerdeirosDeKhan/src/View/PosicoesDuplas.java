package View;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/**
 * Posicao dos DOIS circulos de cada parada dupla (R2, R11, C5, C10, P1 e P8),
 * em coordenadas da imagem de referencia de CoordenadasMapa. Medidas sobre o
 * proprio tabuleiro. O ponto de CoordenadasMapa.getParada(...) dessas paradas
 * fica perto do meio dos dois circulos, mas nao exatamente (R2, C5 e C10 ficam
 * 20 a 35 pixels fora), entao o pino e o yurt sao desenhados em cima de cada circulo.
 */
public final class PosicoesDuplas {
	/** Raio de um circulo impresso no tabuleiro (aprox.). */
	public static final int RAIO_CIRCULO = 48;

	private static final Map<String, Point[]> circulos = new HashMap<String, Point[]>();

	static {
		circulos.put("R2", new Point[] { new Point(754, 153), new Point(821, 224) });
		circulos.put("R11", new Point[] { new Point(1924, 438), new Point(2024, 441) });
		circulos.put("C5", new Point[] { new Point(2256, 1326), new Point(2256, 1424) });
		circulos.put("C10", new Point[] { new Point(1566, 1958), new Point(1671, 1975) });
		circulos.put("P1", new Point[] { new Point(414, 743), new Point(400, 842) });
		circulos.put("P8", new Point[] { new Point(591, 1660), new Point(690, 1674) });
	}

	private PosicoesDuplas() {
	}

	public static boolean ehDupla(String parada) {
		return circulos.containsKey(parada);
	}

	/** Centros dos 2 circulos da parada dupla (copias), ou null se a parada nao for dupla. */
	public static Point[] circulos(String parada) {
		Point[] c = circulos.get(parada);
		if (c == null)
			return null;
		return new Point[] { new Point(c[0]), new Point(c[1]) };
	}

	/**
	 * Distancia do ponto (x, y) a parada. Nas paradas duplas e a distancia ao circulo
	 * mais proximo (assim clicar em qualquer um dos dois funciona); nas demais, a
	 * distancia ao ponto "centro" de CoordenadasMapa.
	 */
	public static double distancia(String parada, Point centro, double x, double y) {
		Point[] c = circulos.get(parada);
		if (c == null)
			return centro.distance(x, y);
		return Math.min(c[0].distance(x, y), c[1].distance(x, y));
	}
}
