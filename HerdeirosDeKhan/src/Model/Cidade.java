package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Uma cidade do mapa. So pode ser atacada depois de revelada (com 4
 * tesouros). Quem retira o ultimo tesouro conquista a cidade e coloca
 * um yurt no centro dela, que conta para a maioria de yurts da regiao.
 */
class Cidade {
	private String nome;
	private Regiao regiao;
	private boolean revelada = false;
	private List<Tesouro> tesouros = new ArrayList<Tesouro>();
	private String conquistador = null;

	Cidade(String nome, Regiao regiao) {
		this.nome = nome;
		this.regiao = regiao;
	}

	String getNome() {
		return nome;
	}

	Regiao getRegiao() {
		return regiao;
	}

	boolean isRevelada() {
		return revelada;
	}

	boolean isConquistada() {
		return conquistador != null;
	}

	/** Cor de quem conquistou a cidade, ou null se ainda nao foi conquistada. */
	String getConquistador() {
		return conquistador;
	}

	/** Esta disponivel para ataque: revelada, nao conquistada e com tesouros. */
	boolean isDisponivel() {
		return revelada && !isConquistada() && !tesouros.isEmpty();
	}

	/** Revela a cidade colocando nela os tesouros sorteados. */
	boolean revelar(List<Tesouro> novosTesouros) {
		if (revelada || novosTesouros == null || novosTesouros.isEmpty())
			return false;
		tesouros.addAll(novosTesouros);
		revelada = true;
		return true;
	}

	List<Tesouro> getTesouros() {
		return new ArrayList<Tesouro>(tesouros);
	}

	int getNumTesouros() {
		return tesouros.size();
	}

	/**
	 * Retira o tesouro indicado. Se for o ultimo, a cidade passa
	 * a ser do jogador da cor indicada. O custo em espadas do ataque e
	 * verificado por quem chama este metodo.
	 */
	boolean retirarTesouro(Tesouro tesouro, String cor) {
		if (!isDisponivel() || !tesouros.remove(tesouro))
			return false;
		if (tesouros.isEmpty())
			conquistador = cor;
		return true;
	}
}
