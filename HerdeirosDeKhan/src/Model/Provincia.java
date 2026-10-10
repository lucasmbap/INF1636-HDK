package Model;

/**
 * Uma provincia do mapa: area entre as rotas que guarda pecas de
 * tributo de um unico tipo (o icone impresso nela), no maximo 3.
 * As provincias do Khan sao as que podem receber o peao do Khan.
 */
class Provincia {
	static final int MAX_TRIBUTOS = 3;

	private String nome;
	private Regiao regiao;
	private TipoTributo tipo;
	private boolean doKhan;
	private int quantidade = 0;

	Provincia(String nome, Regiao regiao, TipoTributo tipo, boolean doKhan) {
		this.nome = nome;
		this.regiao = regiao;
		this.tipo = tipo;
		this.doKhan = doKhan;
	}

	String getNome() {
		return nome;
	}

	Regiao getRegiao() {
		return regiao;
	}

	TipoTributo getTipo() {
		return tipo;
	}

	boolean isDoKhan() {
		return doKhan;
	}

	int getQuantidade() {
		return quantidade;
	}

	/** Coloca 1 peca de tributo. Retorna false se a provincia ja tem 3. */
	boolean adicionarTributo() {
		if (quantidade >= MAX_TRIBUTOS)
			return false;
		quantidade++;
		return true;
	}

	/** Retira 1 peca de tributo. Retorna false se a provincia esta vazia. */
	boolean retirarTributo() {
		if (quantidade == 0)
			return false;
		quantidade--;
		return true;
	}
}
