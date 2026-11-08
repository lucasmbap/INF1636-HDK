package View;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
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
			JanelaJogador jogador = new JanelaJogador(modelo);
			abrirTabuleiro(tabuleiro, jogador);
			jogador.setVisible(true);
			return null;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return "Erro ao iniciar: " + e + ". Corrija e tente de novo.";
		}
	}

	/**
	 * Entrega o tabuleiro e a janela do jogador ao Controller (Controller.Main.abrir), que abre a
	 * janela do jogo e se liga aos cliques, botoes e a cartela. A chamada e por reflexao para que
	 * a View compile com ou sem o Controller. Se o Controller so tiver a versao antiga de abrir
	 * (sem a janela do jogador), a cartela fica ligada ao ControleProvisorio; se nao houver
	 * Controller, abre uma janela so de visualizacao.
	 */
	private void abrirTabuleiro(JComponent tabuleiro, JanelaJogador jogador) {
		Object janelaDoJogo = null;
		try {
			Class<?> classe = Class.forName("Controller.Main");
			try {
				janelaDoJogo = classe.getMethod("abrir", ModeloApi.class, JComponent.class, JanelaJogador.class)
						.invoke(null, modelo, tabuleiro, jogador);
			} catch (NoSuchMethodException e) {
				jogador.setOuvinte(new ControleProvisorio());
				janelaDoJogo = classe.getMethod("abrir", ModeloApi.class, JComponent.class).invoke(null, modelo,
						tabuleiro);
			}
		} catch (ClassNotFoundException e) {
			jogador.setOuvinte(new ControleProvisorio());
			abrirSemController(tabuleiro, jogador);
			return;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Nao foi possivel abrir o tabuleiro pelo Controller.", e);
		}
		if (janelaDoJogo instanceof JFrame)
			ajustarJanelas((JFrame) janelaDoJogo, jogador);
	}

	/**
	 * Faz a janela do jogo e a do jogador caberem na area util da tela (sem barra de tarefas):
	 * jogo a esquerda (no maximo 1400 x 1050) e jogador a direita. O tabuleiro se redesenha
	 * na nova escala, entao nada e cortado em telas menores.
	 */
	private static void ajustarJanelas(JFrame jogo, JanelaJogador jogador) {
		Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int largJogador = Math.max(400, Math.min(520, area.width / 3));
		int largJogo = Math.min(1400, area.width - largJogador);
		int alt = Math.min(1050, area.height);
		jogo.setSize(largJogo, alt);
		jogo.setLocation(area.x, area.y);
		jogador.setBounds(area.x + largJogo, area.y, Math.min(largJogador, area.width - largJogo), alt);
		jogo.validate();
	}

	private void abrirSemController(JComponent tabuleiro, JanelaJogador jogador) {
		JFrame janela = new JFrame("Herdeiros de Khan (sem Controller)");
		janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		janela.setLayout(new BorderLayout());
		janela.add(tabuleiro, BorderLayout.CENTER);
		janela.add(new JLabel("  Controller ainda nao conectado: o tabuleiro esta so para visualizacao."),
				BorderLayout.SOUTH);
		janela.setVisible(true);
		ajustarJanelas(janela, jogador);
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