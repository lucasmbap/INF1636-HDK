package View;

/**
 * Pedidos de melhoria feitos na janela do jogador. A View so avisa; quem chama o Model e o
 * ouvinte (hoje, View.Main). Cada metodo devolve null se deu certo, ou o motivo da falha.
 */
public interface OuvinteMelhorias {

	/** Compra (2 moedas) a melhoria do tipo indicado (ex.: "CAVALOS") para a coluna 0=vermelha, 1=verde, 2=amarela. */
	String comprar(String tipo, int coluna);

	/** Coloca, na coluna branca, a melhoria gratis do tipo indicado para o jogador da cor indicada. */
	String colocarGratis(String cor, String tipo);
}
