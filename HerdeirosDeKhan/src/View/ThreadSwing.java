package View;

import javax.swing.SwingUtilities;

/** Utilitario: garante que a atualizacao de uma janela observadora rode na thread de eventos do Swing. */
final class ThreadSwing {
	private ThreadSwing() {
	}

	static void executar(Runnable tarefa) {
		if (SwingUtilities.isEventDispatchThread())
			tarefa.run();
		else
			SwingUtilities.invokeLater(tarefa);
	}
}
