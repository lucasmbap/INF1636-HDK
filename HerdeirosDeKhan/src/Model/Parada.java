package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Uma parada do mapa: local por onde os peoes andam e onde os yurts
 * sao construidos. Guarda quais peoes e quais yurts estao nela.
 */
class Parada {
	static final int MAX_JOGADORES = 5;

	private String nome;
	private TipoParada tipo;
	private Regiao regiao;          // null em Karakorum
	private boolean temConselheiro;
	private List<String> peoes = new ArrayList<String>();
	private List<String> yurts = new ArrayList<String>();

	Parada(String nome, TipoParada tipo, Regiao regiao, boolean temConselheiro) {
		this.nome = nome;
		this.tipo = tipo;
		this.regiao = regiao;
		this.temConselheiro = temConselheiro;
	}

	String getNome() {
		return nome;
	}

	TipoParada getTipo() {
		return tipo;
	}

	Regiao getRegiao() {
		return regiao;
	}

	boolean temConselheiro() {
		return temConselheiro;
	}

	// ---------------- peoes ----------------

	/** Quantos peoes cabem nesta parada ao final de um movimento. */
	int capacidadePeoes() {
		switch (tipo) {
		case SIMPLES:
			return 1;
		case DUPLA:
			return 2;
		default:
			return MAX_JOGADORES; // Karakorum: todos os peoes
		}
	}

	boolean temPeao(String cor) {
		return peoes.contains(cor);
	}

	int getNumPeoes() {
		return peoes.size();
	}

	/** Verifica se o peao da cor indicada pode terminar o movimento aqui. */
	boolean podeReceberPeao(String cor) {
		if (temPeao(cor))
			return true;
		return peoes.size() < capacidadePeoes();
	}

	boolean adicionarPeao(String cor) {
		if (temPeao(cor) || !podeReceberPeao(cor))
			return false;
		peoes.add(cor);
		return true;
	}

	boolean removerPeao(String cor) {
		return peoes.remove(cor);
	}

	// ---------------- yurts ----------------

	/** Quantos yurts podem ser construidos nesta parada. */
	int capacidadeYurts() {
		switch (tipo) {
		case SIMPLES:
			return 1;
		case DUPLA:
			return 2;
		default:
			return 0; // Karakorum nao pode ter yurts
		}
	}

	boolean podeConstruirYurt() {
		return yurts.size() < capacidadeYurts();
	}

	boolean construirYurt(String cor) {
		if (!podeConstruirYurt())
			return false;
		yurts.add(cor);
		return true;
	}

	boolean temYurtDe(String cor) {
		return yurts.contains(cor);
	}

	/** Numero de yurts da cor indicada nesta parada (0, 1 ou 2). */
	int contarYurtsDe(String cor) {
		int total = 0;
		for (String c : yurts)
			if (c.equals(cor))
				total++;
		return total;
	}

	int getNumYurts() {
		return yurts.size();
	}

	/** Cores dos donos dos yurts desta parada (copia). */
	List<String> getYurts() {
		return new ArrayList<String>(yurts);
	}

	/** Cores dos peoes que estao nesta parada (copia). */
	List<String> getPeoes() {
		return new ArrayList<String>(peoes);
	}
}
