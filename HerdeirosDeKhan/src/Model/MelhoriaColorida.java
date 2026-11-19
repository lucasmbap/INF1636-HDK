package Model;

/**
 * Peca de melhoria como ela esta na area de melhorias: o tipo (o que ela da)
 * e a cor do fundo, que diz em qual coluna colorida da cartela ela pode ser
 * colocada (manual, pag. 11).
 */
class MelhoriaColorida {
	private final TipoMelhoria tipo;
	private final ColunaCartela cor;

	MelhoriaColorida(TipoMelhoria tipo, ColunaCartela cor) {
		if (tipo == null || cor == null || cor == ColunaCartela.BRANCA)
			throw new IllegalArgumentException("melhoria precisa de tipo e de cor (vermelha, verde ou amarela)");
		this.tipo = tipo;
		this.cor = cor;
	}

	TipoMelhoria getTipo() {
		return tipo;
	}

	ColunaCartela getCor() {
		return cor;
	}

	/** Texto usado no arquivo salvo, ex.: "MOEDA:VERDE". */
	String codigo() {
		return tipo.name() + ":" + cor.name();
	}

	/** Le o texto de codigo(); lanca IllegalArgumentException se for invalido. */
	static MelhoriaColorida deCodigo(String s) {
		String[] p = s == null ? new String[0] : s.split(":");
		if (p.length != 2 || TipoMelhoria.porNome(p[0]) == null)
			throw new IllegalArgumentException("melhoria invalida: " + s);
		return new MelhoriaColorida(TipoMelhoria.porNome(p[0]), ColunaCartela.valueOf(p[1]));
	}
}
