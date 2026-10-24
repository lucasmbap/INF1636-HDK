package Model;

/**
 * Padrao Observer (cap. 17): quem implementa esta interface (por exemplo,
 * o painel do tabuleiro) e avisado sempre que o estado da partida muda,
 * e entao pode pedir os dados novos a API e chamar repaint().
 */
public interface ObservadorModelo {
	void modeloAlterado();
}
