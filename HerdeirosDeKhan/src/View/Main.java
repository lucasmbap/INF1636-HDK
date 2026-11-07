package View;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import Model.ModeloApi;

/**
 * Inicializacao do jogo. Abre a janela inicial; quando os jogadores e herdeiros sao
 * escolhidos, cria os jogadores no Model, inicia a partida e abre o tabuleiro (pelo
 * Controller), a cartela e as cartas. Esta classe e a "cola": se o grupo preferir,
 * o metodo iniciarPartida pode ser movido para o Controller (ele so implementa OuvinteInicio).
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

	/** Chamado pela janela de escolha, na thread de eventos. Devolve null se deu certo, ou o motivo da falha. */
	@Override
	public String iniciarPartida(List<String[]> jogadores) {
		if (modelo.isPartidaIniciada())
			return null;
		try {
			modelo.novaPartida(); // descarta jogadores de uma tentativa anterior que falhou
			for (String[] j : jogadores)
				modelo.criarJogador(j[0], j[1], j[2]);
			if (!modelo.iniciarPartida())
				return "O Model recusou iniciar a partida (sao necessarios de 2 a 5 jogadores, cada um com um "
						+ "herdeiro valido). Corrija e tente de novo.";
			PainelTabuleiro tabuleiro = new PainelTabuleiro(modelo, CAMINHO_TABULEIRO);
			abrirTabuleiro(tabuleiro);
			JanelaJogador jogador = new JanelaJogador(modelo);
			jogador.setOuvinte(new ControleProvisorio());
			jogador.setVisible(true);
			return null;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return "Erro ao iniciar: " + e + ". Corrija e tente de novo.";
		}
	}

	/**
	 * Entrega o tabuleiro ao Controller (Controller.Main.abrir), que abre a janela e liga
	 * cliques e botoes. Se a classe do Controller ainda nao existir no projeto, abre uma
	 * janela so de visualizacao, para a View poder ser testada sozinha. A chamada e por
	 * reflexao para que a View compile com ou sem o Controller.
	 */
	private void abrirTabuleiro(JComponent tabuleiro) {
		try {
			Class<?> classe = Class.forName("Controller.Main");
			classe.getMethod("abrir", ModeloApi.class, JComponent.class).invoke(null, modelo, tabuleiro);
			return;
		} catch (ClassNotFoundException e) {
			// Controller ainda nao publicado: usa a janela de visualizacao abaixo
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Nao foi possivel abrir o tabuleiro pelo Controller.", e);
		}
		JFrame janela = new JFrame("Herdeiros de Khan (sem Controller)");
		janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		janela.setLayout(new BorderLayout());
		janela.add(tabuleiro, BorderLayout.CENTER);
		janela.add(new JLabel("  Controller ainda nao conectado: o tabuleiro esta so para visualizacao."),
				BorderLayout.SOUTH);
		janela.setSize(1100, 1050);
		janela.setLocation(0, 0);
		janela.setVisible(true);
	}

	/**
	 * PROVISORIO: repassa o clique numa coluna da cartela ao Model enquanto o Controller
	 * do grupo nao trata OuvinteAcoes. Quando o Controller implementar OuvinteAcoes, ele
	 * deve chamar JanelaJogador.setOuvinte(...) no lugar desta classe, que pode ser apagada.
	 * Se a jogada for invalida, o Model devolve false e nada acontece na tela.
	 */
	private class ControleProvisorio implements OuvinteAcoes {
		@Override
		public void colunaEscolhida(int coluna) {
			modelo.ativarColuna(coluna);
		}
	}
}
