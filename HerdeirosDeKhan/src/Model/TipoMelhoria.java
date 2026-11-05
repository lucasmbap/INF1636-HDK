package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tipos de peca de melhoria (manual, pagina 16) e os icones que cada uma
 * acrescenta a coluna em que for colocada.
 * Quantidades no jogo: moeda 8, espada 8, yurt 8, punho 6, cavalos 9 e
 * 1 de cada tesouro.
 */
enum TipoMelhoria {
	MOEDA, ESPADA, YURT, PUNHO, CAVALOS,
	TESOURO_GADO, TESOURO_ESPECIARIA, TESOURO_PELE, TESOURO_FERRO, TESOURO_SEDA;

	List<IconeAcao> getIcones() {
		List<IconeAcao> lst = new ArrayList<IconeAcao>();
		switch (this) {
		case MOEDA:
			lst.add(IconeAcao.MOEDA);
			break;
		case ESPADA:
			lst.add(IconeAcao.ESPADA);
			break;
		case YURT:
			lst.add(IconeAcao.YURT);
			break;
		case PUNHO:
			lst.add(IconeAcao.PUNHO);
			break;
		case CAVALOS:
			lst.add(IconeAcao.CAVALO);
			lst.add(IconeAcao.CAVALO);
			break;
		default:
			lst.add(IconeAcao.TESOURO);
		}
		return lst;
	}

	/** Nome do tesouro virtual (ex.: "seda"), ou null se nao for melhoria de tesouro. */
	String getTesouro() {
		if (!name().startsWith("TESOURO_"))
			return null;
		return name().substring("TESOURO_".length()).toLowerCase();
	}

	/** Tipo a partir do nome (ex.: "MOEDA" ou "moeda"), ou null se nao existir. */
	static TipoMelhoria porNome(String nome) {
		if (nome == null)
			return null;
		for (TipoMelhoria t : values())
			if (t.name().equalsIgnoreCase(nome))
				return t;
		return null;
	}
}
