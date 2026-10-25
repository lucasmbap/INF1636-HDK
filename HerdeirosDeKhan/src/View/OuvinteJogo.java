package View;

/**
 * Contrato entre a View e o Controller.
 *
 * A View nunca altera o Model: quando o jogador clica ou aperta um botao,
 * ela apenas avisa o ouvinte registrado com JanelaJogo.setOuvinte(...).
 * Quem implementar o Controller decide o que fazer (por exemplo, chamar
 * ModeloApi.moverPara, iniciarTurno ou passarVez). Se o Model recusar uma
 * jogada, o Controller nao precisa fazer mais nada: a View so se redesenha
 * quando o Model avisa (Observer), entao nada aparece na tela.
 */
public interface OuvinteJogo {

	/** O jogador clicou sobre uma parada do tabuleiro (nomes do Model: R1.., C1.., P1.., Karakorum). */
	void paradaClicada(String parada);

	/** O jogador pediu para comecar o turno com os movimentos e punhos escolhidos nos combos. */
	void turnoSolicitado(int movimentos, int punhos);

	/** O jogador pediu para encerrar o turno e passar a vez. */
	void passarVezSolicitado();
}
