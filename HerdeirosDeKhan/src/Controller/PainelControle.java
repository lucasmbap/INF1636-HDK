package Controller;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Controles laterais da partida. Os eventos sao conectados pelo controlador.
 */
public final class PainelControle extends JPanel {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Integer> movimentos = new JComboBox<Integer>();
	private final JButton iniciarTurno = new JButton("Aplicar movimentos");
	private final JButton proximoJogador = new JButton("Proximo jogador");
	private final JButton salvar = new JButton("Salvar resumo");
	private final JLabel jogadorDaVez = new JLabel("Jogador da vez: -");
	private final JLabel movimentosRestantes = new JLabel("Movimentos restantes: -");
	private final JLabel estado = new JLabel(" ");

	public PainelControle() {
		setLayout(new GridLayout(0, 1, 6, 6));
		setPreferredSize(new Dimension(240, 180));

		for (int quantidade = 2; quantidade <= 12; quantidade++)
			movimentos.addItem(quantidade);

		add(jogadorDaVez);
		add(movimentosRestantes);
		add(new JLabel("Movimentos para este turno:"));
		add(movimentos);
		add(iniciarTurno);
		add(proximoJogador);
		add(salvar);
		add(estado);
	}

	JComboBox<Integer> getSeletorMovimentos() {
		return movimentos;
	}

	JButton getBotaoIniciarTurno() {
		return iniciarTurno;
	}

	JButton getBotaoProximoJogador() {
		return proximoJogador;
	}

	JButton getBotaoSalvar() {
		return salvar;
	}

	void atualizarEstado(String cor, int restantes, boolean partidaIniciada, boolean podeDefinirMovimentos) {
		jogadorDaVez.setText("Jogador da vez: " + (cor == null ? "-" : cor));
		movimentosRestantes.setText("Movimentos restantes: " + (partidaIniciada ? restantes : "-"));
		movimentos.setEnabled(podeDefinirMovimentos);
		iniciarTurno.setEnabled(podeDefinirMovimentos);
		proximoJogador.setEnabled(partidaIniciada);
		salvar.setEnabled(partidaIniciada);
	}

	void exibirMensagem(String mensagem) {
		estado.setText(mensagem == null || mensagem.isBlank() ? " " : mensagem);
	}
}
