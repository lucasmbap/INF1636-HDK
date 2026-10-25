package View;

import javax.swing.SwingUtilities;

import Model.ModeloApi;

/**
 * Teste da View (2a iteracao, sem janelas iniciais).
 * Cria 3 jogadores so para ver o tabuleiro. O Controller ainda nao esta ligado:
 * quando ele existir, descomente a linha indicada.
 */
public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ModeloApi modelo = new ModeloApi();
				modelo.criarJogador("Ana", "vermelho", "Tolui");
				modelo.criarJogador("Bruno", "azul", "Jochi");
				modelo.criarJogador("Carla", "verde", "Chagatai");
				modelo.iniciarPartida();

				JanelaJogo janela = new JanelaJogo(modelo);
				// janela.setOuvinte(new Controlador(modelo)); // Controller implementa OuvinteJogo
				janela.setVisible(true);
			}
		});
	}
}
