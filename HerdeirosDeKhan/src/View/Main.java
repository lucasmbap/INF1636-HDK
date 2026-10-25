package View;

import Model.ModeloApi;

/**
 * Inicializacao do jogo (2a iteracao, sem janelas iniciais): cria a partida de
 * teste e entrega o componente do tabuleiro ao Controller, que abre a janela,
 * liga os cliques e os botoes (Controller.Main.iniciar).
 */
public class Main {
	/** Imagem do tabuleiro, relativa a pasta de execucao (raiz do projeto). */
	public static final String CAMINHO_TABULEIRO = "imagens/tabuleiro.jpg";

	public static void main(String[] args) {
		ModeloApi modelo = new ModeloApi();
		modelo.criarJogador("Ana", "vermelho", "Tolui");
		modelo.criarJogador("Bruno", "azul", "Jochi");
		modelo.criarJogador("Carla", "verde", "Chagatai");
		modelo.iniciarPartida();

		// o componente e criado na thread principal, mas so e mostrado pela thread de eventos
		PainelTabuleiro tabuleiro = new PainelTabuleiro(modelo, CAMINHO_TABULEIRO);
		Controller.Main.iniciar(modelo, tabuleiro);
	}
}
