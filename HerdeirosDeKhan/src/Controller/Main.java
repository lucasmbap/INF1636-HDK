package Controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import Model.ModeloApi;

/**
 * Ponto de integracao da aplicacao. A View fornece o componente do tabuleiro.
 */
public final class Main {
	private Main() {
	}

	/**
	 * Ponto de entrada para a View ou para uma classe de inicializacao.
	 * Pode ser chamado fora da thread de eventos do Swing.
	 */
	public static void iniciar(final ModeloApi modelo, final JComponent tabuleiro) {
		if (modelo == null || tabuleiro == null)
			throw new IllegalArgumentException("Modelo e tabuleiro sao obrigatorios.");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				abrir(modelo, tabuleiro);
			}
		});
	}

	/**
	 * Abre a janela usando o componente de tabuleiro criado pela View.
	 * Deve ser chamado na thread de eventos do Swing, depois de iniciar a partida.
	 */
	public static JFrame abrir(ModeloApi modelo, JComponent tabuleiro) {
		if (!SwingUtilities.isEventDispatchThread())
			throw new IllegalStateException("Main.abrir deve ser chamado na thread de eventos do Swing.");
		if (modelo == null || tabuleiro == null)
			throw new IllegalArgumentException("Modelo e tabuleiro sao obrigatorios.");

		PainelControle controles = new PainelControle();
		final ControladorJogo controlador = new ControladorJogo(modelo, tabuleiro, controles);

		JFrame janela = new JFrame("Herdeiros de Khan");
		janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		janela.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent evento) {
				controlador.encerrar();
			}
		});
		janela.setResizable(false);
		janela.setLayout(new BorderLayout(8, 8));
		janela.add(tabuleiro, BorderLayout.CENTER);
		janela.add(controles, BorderLayout.EAST);
		janela.setSize(new Dimension(1400, 1050));
		janela.setMaximumSize(new Dimension(1400, 1050));
		janela.setLocationRelativeTo(null);
		janela.setVisible(true);
		return janela;
	}
}
