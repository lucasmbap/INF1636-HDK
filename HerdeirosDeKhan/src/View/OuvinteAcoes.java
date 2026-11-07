package View;

/**
 * Eventos das janelas do jogo que o Controller trata. A View so avisa; quem chama o
 * Model (ativarColuna, pegarTributo, ...) e o ouvinte. Se a jogada for invalida, o Model
 * devolve false, nada muda e nada e mostrado.
 */
public interface OuvinteAcoes {

	/** O jogador clicou numa coluna da cartela: ModeloApi.COLUNA_VERMELHA (0) a COLUNA_BRANCA (3). */
	void colunaEscolhida(int coluna);
}
