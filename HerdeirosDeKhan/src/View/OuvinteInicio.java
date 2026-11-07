package View;

import java.util.List;

/**
 * Quem recebe a configuracao feita nas janelas iniciais. A View so pergunta;
 * quem cria os jogadores no Model e inicia a partida e o ouvinte (hoje, View.Main).
 */
public interface OuvinteInicio {

	/**
	 * Cada item e {nome, cor, herdeiro}, na ordem dos turnos.
	 *
	 * @return null se a partida comecou; senao, o motivo (mostrado na janela de escolha)
	 */
	String iniciarPartida(List<String[]> jogadores);
}
