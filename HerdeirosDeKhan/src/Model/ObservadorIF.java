package Model;

/**
 * Padrao Observer (cap. 17): interface dos observadores. Cada janela que
 * mostra o estado da partida (tabuleiro, cartela, cartas) a implementa e e
 * avisada por notify() sempre que esse estado muda.
 */
public interface ObservadorIF {
	void notify(ObservadoIF o);
}
