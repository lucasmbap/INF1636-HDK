package View;

/**
 * Pedidos de melhoria feitos na janela do jogador. A View so avisa; quem chama o Model e o
 * ouvinte (hoje, View.Main). Cada metodo devolve null se deu certo, ou o motivo da falha.
 * Os espacos da area vao de 0 a 4; ModeloApi.TOPO_DA_PILHA e a peca do topo da pilha.
 */
public interface OuvinteMelhorias {

	/**
	 * Compra (2 moedas) a melhoria do espaco indicado para a coluna 0=vermelha, 1=verde,
	 * 2=amarela, ou -1 para a coluna da cor da peca.
	 */
	String comprar(int espaco, int coluna);

	/** Coloca, na coluna branca, a melhoria gratis do espaco indicado para o jogador da cor indicada. */
	String colocarGratis(String cor, int espaco);

	/** Jochi: escolhe a melhoria inicial (indice 0 ou 1) para a coluna branca. */
	String escolherInicial(String cor, int indice);
}
