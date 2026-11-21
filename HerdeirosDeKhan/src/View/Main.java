package View;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import Controller.ControladorArquivos;
import Model.ModeloApi;

/**
 * Inicializacao do jogo. Abre a janela inicial; quando os jogadores e herdeiros sao
 * escolhidos, cria os jogadores no Model, inicia a partida e abre o tabuleiro (pelo
 * Controller), a cartela e as cartas. A View apresenta os dialogos e escolhe os arquivos;
 * o Controller coordena as operacoes do Model.
 */
public class Main implements OuvinteInicio, OuvinteArquivos, OuvinteMelhorias, OuvinteFimPartida {
	/** Imagem do tabuleiro, relativa a pasta de execucao (raiz do projeto). */
	public static final String CAMINHO_TABULEIRO = "imagens/tabuleiro.jpg";

	private final ModeloApi modelo = ModeloApi.getInstance();
	private final ControladorArquivos controladorArquivos = new ControladorArquivos(modelo);

	private boolean jogoAberto = false;
	private PainelTabuleiro tabuleiro = null;
	private JanelaJogador janelaJogador = null;
	private JFrame janelaJogo = null;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Main().abrir();
			}
		});
	}

	private void abrir() {
		JanelaInicial inicial = new JanelaInicial(modelo, this);
		inicial.setOuvinteArquivos(this);
		inicial.setVisible(true);
	}

	// ------------------------------------------------------------------
	// Comecar uma partida
	// ------------------------------------------------------------------

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
			sortearCartasSecretas();
			abrirJogo();
			return null;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return "Erro ao iniciar: " + e + ". Corrija e tente de novo.";
		}
	}

	/**
	 * Antes da 1a rodada: cada jogador recebe 1 carta de conselheiro secreto sorteada (as 10 cartas do
	 * manual sao embaralhadas, entao nenhum jogador repete carta). As cartas ficam visiveis
	 * para todos na aba "Cartas".
	 */
	private void sortearCartasSecretas() {
		List<String[]> baralho = CartasSecretas.baralhar();
		int i = 0;
		for (String cor : modelo.getCores())
			for (int id = 1; id <= 12; id++) {
				try {
					if (cor.equals(modelo.getCorJogador(id))) {
						String[] c = baralho.get(i++);
						modelo.adicionarCartaConselheiro(id, c[0], c[1], CartasSecretas.recompensa(), false);
						break;
					}
				} catch (RuntimeException e) {
					// id inexistente
				}
			}
	}

	// ------------------------------------------------------------------
	// Melhorias (OuvinteMelhorias): a janela so avisa, o Main chama o Model
	// ------------------------------------------------------------------

	@Override
	public String comprar(int espaco, int coluna) {
		boolean ok = coluna < 0 ? modelo.comprarMelhoria(espaco) : modelo.comprarMelhoria(espaco, coluna);
		if (!ok)
			return "Nao foi possivel: precisa de 2 moedas (reais ou virtuais), de espaco na coluna (maximo 4), "
					+ "de estar jogando e de usar a coluna da cor da peca (o Jochi pode usar qualquer uma).";
		return null;
	}

	@Override
	public String colocarGratis(String cor, int espaco) {
		if (!modelo.colocarMelhoriaGratis(cor, espaco))
			return "Nao foi possivel colocar essa melhoria gratis agora.";
		return null;
	}

	@Override
	public String escolherInicial(String cor, int indice) {
		if (!modelo.escolherMelhoriaInicial(cor, indice))
			return "Nao foi possivel escolher essa melhoria inicial.";
		return null;
	}

	/** Abre o tabuleiro pelo Controller e conecta as janelas auxiliares. */
	private void abrirJogo() {
		tabuleiro = new PainelTabuleiro(modelo, CAMINHO_TABULEIRO);
		janelaJogador = new JanelaJogador(modelo);
		janelaJogador.setOuvinteArquivos(this);
		janelaJogador.setOuvinteMelhorias(this);
		janelaJogo = abrirTabuleiro(tabuleiro, janelaJogador);
		janelaJogador.setVisible(true);
		jogoAberto = true;
	}

	/** Fecha as janelas do jogo (o Model continua como esta). */
	private void fecharJogo() {
		if (janelaJogador != null)
			janelaJogador.dispose();
		if (janelaJogo != null)
			janelaJogo.dispose();
		if (tabuleiro != null)
			tabuleiro.desregistrar();
		janelaJogador = null;
		janelaJogo = null;
		tabuleiro = null;
		jogoAberto = false;
	}

	/**
	 * Entrega o tabuleiro e as janelas auxiliares ao Controller. A chamada e por reflexao para
	 * manter a View utilizavel sem o Controller; nesse caso, o tabuleiro fica apenas para
	 * visualizacao.
	 */
	private JFrame abrirTabuleiro(JComponent painel, JanelaJogador jogador) {
		Object janela = null;
		try {
			Class<?> classe = Class.forName("Controller.Main");
			try {
				janela = classe.getMethod("abrir", ModeloApi.class, JComponent.class, JanelaJogador.class,
						OuvinteFimPartida.class).invoke(null, modelo, painel, jogador, this);
			} catch (NoSuchMethodException e) {
				try {
					janela = classe.getMethod("abrir", ModeloApi.class, JComponent.class, JanelaJogador.class)
							.invoke(null, modelo, painel, jogador);
				} catch (NoSuchMethodException versaoAntiga) {
					jogador.setOuvinte(new ControleProvisorio());
					janela = classe.getMethod("abrir", ModeloApi.class, JComponent.class).invoke(null, modelo,
							painel);
				}
			}
		} catch (ClassNotFoundException e) {
			jogador.setOuvinte(new ControleProvisorio());
			JFrame sem = abrirSemController(painel, jogador);
			return sem;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Nao foi possivel abrir o tabuleiro pelo Controller.", e);
		}
		if (janela instanceof JFrame) {
			ajustarJanelas((JFrame) janela, jogador);
			return (JFrame) janela;
		}
		return null;
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

	private JFrame abrirSemController(JComponent painel, JanelaJogador jogador) {
		JFrame janela = new JFrame("Herdeiros de Khan (sem Controller)");
		janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		janela.setLayout(new BorderLayout());
		janela.add(painel, BorderLayout.CENTER);
		janela.add(new JLabel("  Controller ainda nao conectado: o tabuleiro esta so para visualizacao."),
				BorderLayout.SOUTH);
		janela.setVisible(true);
		ajustarJanelas(janela, jogador);
		return janela;
	}

	// ------------------------------------------------------------------
	// Salvar e recuperar: a View escolhe o arquivo; o Controller coordena o Model.
	// ------------------------------------------------------------------

	@Override
	public boolean podeSalvarPartida() {
		return controladorArquivos.podeSalvarPartida();
	}

	@Override
	public String salvarPartida(File arquivo) {
		return controladorArquivos.salvarPartida(arquivo);
	}

	@Override
	public String recuperarPartida(File arquivo) {
		String erro = controladorArquivos.recuperarPartida(arquivo);
		if (erro != null)
			return erro;
		if (!jogoAberto)
			abrirJogo(); // recuperada a partir da janela inicial: abre o tabuleiro e a cartela
		return null;
	}

	// ------------------------------------------------------------------
	// Dialogos de fim da partida (OuvinteFimPartida)
	// ------------------------------------------------------------------

	@Override
	public void perguntarNovaPartida(List<String> vencedores, Runnable iniciarNovaPartida) {
		Object[] opcoes = { "Sim", "Nao" };
		int resposta = JOptionPane.showOptionDialog(janelaJogo,
				textoDoVencedor(vencedores) + "\n\nDeseja iniciar uma nova partida?", "Fim da partida",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
		if (resposta == JOptionPane.YES_OPTION)
			iniciarNovaPartida.run();
		else
			System.exit(0);
	}

	@Override
	public void prepararNovaPartida() {
		fecharJogo();
	}

	@Override
	public void abrirEscolhaNovaPartida() {
		new JanelaEscolhaHerdeiros(modelo, this).setVisible(true);
	}

	private String textoDoVencedor(List<String> cores) {
		if (cores.isEmpty())
			return "Partida encerrada.";
		StringBuilder sb = new StringBuilder(cores.size() > 1 ? "Vencedores: " : "Vencedor: ");
		for (int i = 0; i < cores.size(); i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(nomeDoJogador(cores.get(i)));
		}
		return sb.toString();
	}

	private String nomeDoJogador(String cor) {
		for (int id = 1; id <= modelo.getCores().size(); id++)
			if (cor.equals(modelo.getCorJogador(id)))
				return modelo.getNomeJogador(id) + " (" + cor + ")";
		return cor;
	}

	/**
	 * PROVISORIO: repassa o clique numa coluna da cartela ao Model enquanto o Controller
	 * do grupo nao trata OuvinteAcoes. Com o Controller novo (que implementa OuvinteAcoes e
	 * recebe a JanelaJogador em Controller.Main.abrir), esta classe nao e usada.
	 */
	private class ControleProvisorio implements OuvinteAcoes {
		@Override
		public void colunaEscolhida(int coluna) {
			modelo.ativarColuna(coluna);
		}
	}
}
