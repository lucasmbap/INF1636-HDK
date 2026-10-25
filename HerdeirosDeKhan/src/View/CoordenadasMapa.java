package View;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/**
 * Posicao (em pixels) dos elementos do mapa na imagem do tabuleiro.
 * As coordenadas se referem a imagem original, de LARGURA_REF x ALTURA_REF
 * pixels. Se o tabuleiro for desenhado em outro tamanho, multiplique as
 * coordenadas pela escala usada no drawImage().
 *
 * Os nomes sao os mesmos usados pelo Model (MapaOficial).
 */
public class CoordenadasMapa {
	public static final int LARGURA_REF = 2564;
	public static final int ALTURA_REF = 2576;
	/** Raio (na imagem original) em que um clique ainda conta como sobre a parada. */
	public static final int RAIO_PARADA = 60;

	private static Map<String, Point> paradas = new HashMap<String, Point>();
	private static Map<String, Point> provincias = new HashMap<String, Point>();
	private static Map<String, Point> cidades = new HashMap<String, Point>();
	private static Map<String, Point> khan = new HashMap<String, Point>();

	static {
		// paradas (centro do circulo; nas duplas, o meio entre os 2 circulos)
		paradas.put("R1", new Point(432, 227));
		paradas.put("R2", new Point(784, 155));
		paradas.put("R3", new Point(112, 474));
		paradas.put("R4", new Point(414, 473));
		paradas.put("R5", new Point(755, 490));
		paradas.put("R6", new Point(1122, 437));
		paradas.put("R7", new Point(955, 743));
		paradas.put("R8", new Point(1547, 201));
		paradas.put("R9", new Point(1377, 592));
		paradas.put("R10", new Point(1669, 526));
		paradas.put("R11", new Point(1974, 438));
		paradas.put("C1", new Point(2202, 570));
		paradas.put("C2", new Point(2443, 779));
		paradas.put("C3", new Point(2231, 951));
		paradas.put("C4", new Point(1890, 1107));
		paradas.put("C5", new Point(2236, 1354));
		paradas.put("C6", new Point(1669, 1419));
		paradas.put("C7", new Point(1917, 1482));
		paradas.put("C8", new Point(2232, 1797));
		paradas.put("C9", new Point(1969, 1968));
		paradas.put("C10", new Point(1646, 1951));
		paradas.put("C11", new Point(2285, 2125));
		paradas.put("P1", new Point(407, 793));
		paradas.put("P2", new Point(741, 1053));
		paradas.put("P3", new Point(504, 1248));
		paradas.put("P4", new Point(190, 1317));
		paradas.put("P5", new Point(851, 1372));
		paradas.put("P6", new Point(397, 1450));
		paradas.put("P7", new Point(157, 1686));
		paradas.put("P8", new Point(640, 1666));
		paradas.put("P9", new Point(1123, 1606));
		paradas.put("P10", new Point(1301, 1815));
		paradas.put("P11", new Point(1072, 1973));
		paradas.put("Karakorum", new Point(1280, 1120));

		// provincias (posicao do icone de tributo)
		provincias.put("Yurt Kiev", new Point(540, 364));
		provincias.put("Espada Moscou", new Point(760, 336));
		provincias.put("Moeda Moscou", new Point(700, 820));
		provincias.put("Yurt Karakorum Norte", new Point(1090, 810));
		provincias.put("Moeda Sarai", new Point(1730, 670));
		provincias.put("Espada Kaesong", new Point(1900, 910));
		provincias.put("Moeda Pequim", new Point(2110, 1380));
		provincias.put("Yurt Cantao", new Point(1810, 1600));
		provincias.put("Espada Cabul", new Point(1484, 1684));
		provincias.put("Yurt Karakorum Sul", new Point(1320, 1430));
		provincias.put("Moeda Bagda", new Point(530, 1460));
		provincias.put("Espada Samarcanda", new Point(460, 1070));

		// cidades (centro da ilustracao)
		cidades.put("Kiev", new Point(280, 360));
		cidades.put("Moscou", new Point(956, 580));
		cidades.put("Sarai", new Point(1516, 430));
		cidades.put("Kaesong", new Point(2260, 780));
		cidades.put("Pequim", new Point(1844, 1310));
		cidades.put("Cantao", new Point(2156, 2010));
		cidades.put("Samarcanda", new Point(724, 1264));
		cidades.put("Bagda", new Point(220, 1516));
		cidades.put("Cabul", new Point(1096, 1824));

		// onde desenhar o Khan: cabeca impressa em cada provincia do Khan e na area de melhorias
		khan.put("Yurt Kiev", new Point(554, 536));
		khan.put("Moeda Sarai", new Point(1544, 810));
		khan.put("Yurt Cantao", new Point(1884, 1836));
		khan.put("Moeda Bagda", new Point(704, 1544));
		khan.put("Area de melhorias", new Point(110, 2470));
	}

	/** Posicao de uma parada na imagem original, ou null se o nome nao existir. */
	public static Point getParada(String nome) {
		return copia(paradas.get(nome));
	}

	public static Point getProvincia(String nome) {
		return copia(provincias.get(nome));
	}

	public static Point getCidade(String nome) {
		return copia(cidades.get(nome));
	}

	/** Posicao do Khan: nome de uma provincia do Khan ou "Area de melhorias". */
	public static Point getKhan(String posicao) {
		return copia(khan.get(posicao));
	}

	/**
	 * Converte um clique na tela para o nome da parada clicada.
	 * origemX e origemY: onde o canto superior esquerdo do tabuleiro foi
	 * desenhado no painel; escala: fator usado para desenhar o tabuleiro
	 * (largura desenhada / LARGURA_REF). Retorna null se o clique nao
	 * estiver sobre nenhuma parada.
	 */
	public static String paradaEm(int xTela, int yTela, int origemX, int origemY, double escala) {
		double x = (xTela - origemX) / escala;
		double y = (yTela - origemY) / escala;
		String maisProxima = null;
		double menorDist = RAIO_PARADA;
		for (Map.Entry<String, Point> e : paradas.entrySet()) {
			double d = PosicoesDuplas.distancia(e.getKey(), e.getValue(), x, y);
			if (d <= menorDist) {
				menorDist = d;
				maisProxima = e.getKey();
			}
		}
		return maisProxima;
	}

	private static Point copia(Point p) {
		if (p == null)
			return null;
		return new Point(p.x, p.y);
	}
}
