package View;

import java.util.List;

import javax.swing.SwingUtilities;

import Model.ModeloApi;

/**
 * Inicializacao do jogo e encaminhamento da configuracao inicial ao Model.
 */
public class Main implements OuvinteInicio {
	/** Imagem do tabuleiro, relativa a pasta de execucao (raiz do projeto). */
	public static final String CAMINHO_TABULEIRO = "imagens/tabuleiro.jpg";

	private final ModeloApi modelo = ModeloApi.getInstance();

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Main().abrir();
			}
		});
	}

	private void abrir() {
		new JanelaInicial(modelo, this).setVisible(true);
	}

	/** Chamado pela janela de escolha. Devolve null se deu certo, ou o motivo da falha. */
	@Override
	public String iniciarPartida(List<String[]> jogadores) {
		if (modelo.isPartidaIniciada())
			return null;
		try {
			modelo.novaPartida();
			for (String[] jogador : jogadores)
				modelo.criarJogador(jogador[0], jogador[1], jogador[2]);
			if (!modelo.iniciarPartida())
				return "O Model recusou iniciar a partida (sao necessarios de 2 a 5 jogadores, cada um com um "
						+ "herdeiro valido). Corrija e tente de novo.";
			PainelTabuleiro tabuleiro = new PainelTabuleiro(modelo, CAMINHO_TABULEIRO);
			JanelaJogador janelaJogador = new JanelaJogador(modelo);
			Controller.Main.abrir(modelo, tabuleiro, janelaJogador);
			janelaJogador.setVisible(true);
			return null;
		} catch (RuntimeException erro) {
			erro.printStackTrace();
			return "Erro ao iniciar: " + erro + ". Corrija e tente de novo.";
		}
	}
}
