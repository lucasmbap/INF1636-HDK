package View;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import Model.ModeloApi;

/**
 * Janela do jogo (2a iteracao): tabuleiro a esquerda e controles a direita.
 * Tamanho total dentro do limite de 1400 x 1050 pixels.
 *
 * Para ligar o Controller, basta chamar setOuvinte(controller), onde controller
 * implementa OuvinteJogo.
 */
public class JanelaJogo extends JFrame {
	private static final long serialVersionUID = 1L;

	/** Caminho da imagem do tabuleiro (relativo a pasta de execucao). */
	public static final String CAMINHO_TABULEIRO = "imagens/tabuleiro.jpg";

	private final PainelTabuleiro painelTabuleiro;
	private final PainelControles painelControles;

	public JanelaJogo(ModeloApi modelo) {
		super("Herdeiros de Khan");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		painelTabuleiro = new PainelTabuleiro(modelo, CAMINHO_TABULEIRO);
		painelTabuleiro.setPreferredSize(new java.awt.Dimension(980, 980));
		painelControles = new PainelControles(modelo);

		add(painelTabuleiro, BorderLayout.CENTER);
		add(painelControles, BorderLayout.EAST);

		setResizable(false);
		pack(); // 980 + 240 = 1220 de largura e 980 de altura (+ bordas): dentro de 1400 x 1050
		setLocationRelativeTo(null);
	}

	/** Liga o Controller: ele recebe cliques nas paradas e os pedidos dos botoes. */
	public void setOuvinte(OuvinteJogo ouvinte) {
		painelTabuleiro.setOuvinte(ouvinte);
		painelControles.setOuvinte(ouvinte);
	}
}
