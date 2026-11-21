package View;

import java.util.List;

/**
 * A View apresenta o resultado e executa as operacoes de interface ao reiniciar.
 */
public interface OuvinteFimPartida {
	void perguntarNovaPartida(List<String> vencedores, Runnable iniciarNovaPartida);

	void prepararNovaPartida();

	void abrirEscolhaNovaPartida();
}
