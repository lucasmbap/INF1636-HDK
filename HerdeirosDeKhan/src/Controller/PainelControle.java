package Controller;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Controles laterais da partida. Os eventos sao conectados pelo controlador.
 */
public final class PainelControle extends JPanel {
	private static final long serialVersionUID = 1L;

	static final String ACAO_MOVIMENTAR = "Movimentar";
	static final String ACAO_TRIBUTO = "Retirar tributo";
	static final String ACAO_ATACAR = "Atacar cidade";
	static final String ACAO_YURT = "Construir yurt";
	static final String ACAO_KHAN = "Mover Khan";

	private final JComboBox<String> acao = new JComboBox<String>();
	private final JComboBox<String> tesouro = new JComboBox<String>();
	private final JComboBox<String> conselheiro = new JComboBox<String>();
	private final ArrayList<JComboBox<String>> tesourosEntrega = new ArrayList<JComboBox<String>>();
	private final JLabel requisitosEntrega = new JLabel(" ");
	private final JButton entregarTesouros = new JButton("Entregar tesouros");
	private final JButton encerrarTurno = new JButton("Encerrar turno");
	private final JLabel jogadorDaVez = new JLabel("Jogador da vez: -");
	private final JLabel estadoTurno = new JLabel("Escolha uma coluna na cartela.");
	private final JLabel estado = new JLabel(" ");

	public PainelControle() {
		setLayout(new GridLayout(0, 1, 6, 6));
		setPreferredSize(new Dimension(250, 300));

		acao.addItem(ACAO_MOVIMENTAR);
		acao.addItem(ACAO_TRIBUTO);
		acao.addItem(ACAO_ATACAR);
		acao.addItem(ACAO_YURT);
		acao.addItem(ACAO_KHAN);
		tesouro.addItem("gado");
		tesouro.addItem("especiaria");
		tesouro.addItem("pele");
		tesouro.addItem("ferro");
		tesouro.addItem("seda");
		for (int i = 0; i < 3; i++) {
			JComboBox<String> seletorTesouro = new JComboBox<String>();
			seletorTesouro.addItem("gado");
			seletorTesouro.addItem("especiaria");
			seletorTesouro.addItem("pele");
			seletorTesouro.addItem("ferro");
			seletorTesouro.addItem("seda");
			tesourosEntrega.add(seletorTesouro);
		}

		add(jogadorDaVez);
		add(estadoTurno);
		add(new JLabel("Acao para o proximo clique:"));
		add(acao);
		add(new JLabel("Tesouro para ataque:"));
		add(tesouro);
		add(new JLabel("Conselheiro visitado:"));
		add(conselheiro);
		add(requisitosEntrega);
		for (JComboBox<String> seletorTesouro : tesourosEntrega)
			add(seletorTesouro);
		add(entregarTesouros);
		add(encerrarTurno);
		add(estado);
	}

	JComboBox<String> getSeletorAcao() {
		return acao;
	}

	String getAcaoSelecionada() {
		return (String) acao.getSelectedItem();
	}

	String getTesouroSelecionado() {
		return (String) tesouro.getSelectedItem();
	}

	JComboBox<String> getSeletorConselheiro() {
		return conselheiro;
	}

	JButton getBotaoEntregarTesouros() {
		return entregarTesouros;
	}

	String getConselheiroSelecionado() {
		return (String) conselheiro.getSelectedItem();
	}

	List<String> getTesourosEntregaSelecionados(int quantidade) {
		List<String> resultado = new ArrayList<String>();
		for (int i = 0; i < quantidade; i++)
			resultado.add((String) tesourosEntrega.get(i).getSelectedItem());
		return resultado;
	}

	void atualizarConselheiros(List<String> conselheirosDisponiveis) {
		List<String> atuais = new ArrayList<String>();
		for (int i = 0; i < conselheiro.getItemCount(); i++)
			atuais.add(conselheiro.getItemAt(i));
		if (!atuais.equals(conselheirosDisponiveis)) {
			conselheiro.removeAllItems();
			for (String nome : conselheirosDisponiveis)
				conselheiro.addItem(nome);
		}
	}

	void atualizarEntrega(List<String> requisitos) {
		boolean disponivel = conselheiro.getItemCount() > 0 && !requisitos.isEmpty();
		requisitosEntrega.setText(requisitos.isEmpty()
				? "Nenhum conselheiro selecionado."
				: "Exigidos: " + String.join(", ", requisitos));
		for (int i = 0; i < tesourosEntrega.size(); i++) {
			JComboBox<String> seletor = tesourosEntrega.get(i);
			boolean qualquer = i < requisitos.size() && "*".equals(requisitos.get(i));
			if (i < requisitos.size() && !qualquer)
				seletor.setSelectedItem(requisitos.get(i));
			seletor.setEnabled(disponivel && qualquer);
		}
		entregarTesouros.setEnabled(disponivel);
	}

	JButton getBotaoEncerrarTurno() {
		return encerrarTurno;
	}

	void atualizarEstado(String cor, int movimentos, int punhos, boolean partidaIniciada, boolean partidaEncerrada,
			int colunaAtivada, boolean khanPendente) {
		jogadorDaVez.setText("Jogador da vez: " + (cor == null ? "-" : cor));
		if (!partidaIniciada) {
			estadoTurno.setText("A partida ainda nao comecou.");
		} else if (partidaEncerrada) {
			estadoTurno.setText("Partida encerrada.");
		} else if (colunaAtivada < 0) {
			estadoTurno.setText("Escolha uma coluna na cartela.");
		} else {
			estadoTurno.setText("Movimentos: " + movimentos + " | Punhos: " + punhos
					+ (khanPendente ? " | Khan obrigatorio" : ""));
		}
		boolean turnoAtivo = partidaIniciada && !partidaEncerrada && colunaAtivada >= 0;
		acao.setEnabled(turnoAtivo);
		tesouro.setEnabled(turnoAtivo
				&& ACAO_ATACAR.equals(getAcaoSelecionada()));
		conselheiro.setEnabled(turnoAtivo && conselheiro.getItemCount() > 0);
		entregarTesouros.setEnabled(turnoAtivo && conselheiro.getItemCount() > 0
				&& entregarTesouros.isEnabled());
		encerrarTurno.setEnabled(turnoAtivo);
	}

	void exibirMensagem(String mensagem) {
		estado.setText(mensagem == null || mensagem.isBlank() ? " " : mensagem);
	}

}
