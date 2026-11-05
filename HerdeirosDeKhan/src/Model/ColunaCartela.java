package Model;

/**
 * As 4 colunas da cartela de um jogador. Cada coluna colorida corresponde
 * a uma regiao do mapa (usada na pontuacao de influencia).
 */
enum ColunaCartela {
	VERMELHA, VERDE, AMARELA, BRANCA;

	/** Regiao da coluna; a branca nao tem regiao (null). */
	Regiao getRegiao() {
		switch (this) {
		case VERMELHA:
			return Regiao.CHINA;
		case VERDE:
			return Regiao.PERSIA;
		case AMARELA:
			return Regiao.RUSSIA;
		default:
			return null;
		}
	}

	static ColunaCartela daRegiao(Regiao r) {
		switch (r) {
		case CHINA:
			return VERMELHA;
		case PERSIA:
			return VERDE;
		default:
			return AMARELA;
		}
	}
}
