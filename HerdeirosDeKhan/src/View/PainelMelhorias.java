package View;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Model.ModeloApi;

/**
 * Parte de baixo da janela do jogador: comprar uma peca de melhoria (2 moedas) e, quando o jogador
 * completou uma linha da cartela, colocar a melhoria gratis na coluna branca. Nao e Observer
 * sozinho: a JanelaJogador (que e) chama atualizar() a cada mudanca do Model.
 */
public class PainelMelhorias extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String[] TIPOS = { "MOEDA", "ESPADA", "YURT", "PUNHO", "CAVALOS", "TESOURO_GADO",
			"TESOURO_ESPECIARIA", "TESOURO_PELE", "TESOURO_FERRO", "TESOURO_SEDA" };
	private static final String[] NOMES = { "Moeda", "Espada", "Yurt", "Punho", "Cavalos", "Tesouro: gado",
			"Tesouro: especiaria", "Tesouro: pele", "Tesouro: ferro", "Tesouro: seda" };
	private static final String[] COLUNAS = { "Vermelha (China)", "Verde (Persia)", "Amarela (Russia)" };

	private final ModeloApi modelo;
	// coluna fixa das pecas de tesouro (so existem numa cor): gado/especiaria amarela, ferro vermelha, pele/seda verde
	private static final int[] COLUNA_FIXA = { -1, -1, -1, -1, -1, 2, 2, 1, 0, 1 };
	private OuvinteMelhorias ouvinte = null;

	private final JComboBox<String> tipoCompra = new JComboBox<String>(NOMES);
	private final JComboBox<String> colunaCompra = new JComboBox<String>(COLUNAS);
	private final JButton comprar = new JButton("Comprar (2 moedas)");
	private final JComboBox<String> tipoGratis = new JComboBox<String>(NOMES);
	private final JButton gratis = new JButton("Colocar na coluna branca");
	private final JLabel infoGratis = new JLabel();
	private final JLabel mensagem = new JLabel(" ");
	private final JPanel linhaGratis = new JPanel();

	public PainelMelhorias(ModeloApi modelo) {
		this.modelo = modelo;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder("Melhorias"));

		JPanel linhaTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
		linhaTipo.add(new JLabel("Comprar:"));
		linhaTipo.add(tipoCompra);
		linhaTipo.add(new JLabel("na coluna"));
		linhaTipo.add(colunaCompra);
		add(linhaTipo);

		JPanel linhaBotao = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
		linhaBotao.add(comprar);
		add(linhaBotao);

		linhaGratis.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
		linhaGratis.add(infoGratis);
		linhaGratis.add(tipoGratis);
		linhaGratis.add(gratis);
		add(linhaGratis);

		mensagem.setForeground(new Color(150, 30, 30));
		mensagem.setFont(mensagem.getFont().deriveFont(Font.PLAIN, 11f));
		JPanel linhaMsg = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		linhaMsg.add(mensagem);
		add(linhaMsg);

		comprar.addActionListener(new AcaoComprar());
		gratis.addActionListener(new AcaoGratis());
		atualizar();
	}

	public void setOuvinte(OuvinteMelhorias ouvinte) {
		this.ouvinte = ouvinte;
	}

	/** Chamado pela JanelaJogador na thread de eventos, a cada notificacao do Model. */
	public void atualizar() {
		boolean jogando = modelo.isPartidaIniciada() && !modelo.isPartidaEncerrada() && modelo.getIdJogadorDaVez() >= 0;
		int pendentes = jogando ? modelo.getMelhoriasGratisPendentes(modelo.getCorDaVez()) : 0;
		comprar.setEnabled(jogando);
		linhaGratis.setVisible(pendentes > 0);
		infoGratis.setText("Melhorias gratis: " + pendentes);
	}

	private void mostrar(String erro) {
		// html para quebrar a mensagem em linhas dentro da largura da janela
		mensagem.setText(erro == null ? " " : "<html><body style='width:350px'>" + erro + "</body></html>");
	}

	/** Pecas de tesouro so servem numa coluna: escolhe e trava a coluna certa. */
	private class AcaoTipo implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int fixa = COLUNA_FIXA[tipoCompra.getSelectedIndex()];
			if (fixa >= 0)
				colunaCompra.setSelectedIndex(fixa);
			colunaCompra.setEnabled(fixa < 0);
		}
	}

	private class AcaoComprar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (ouvinte == null)
				return;
			String erro = ouvinte.comprar(TIPOS[tipoCompra.getSelectedIndex()], colunaCompra.getSelectedIndex());
			mostrar(erro);
		}
	}

	private class AcaoGratis implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (ouvinte == null)
				return;
			mostrar(ouvinte.colocarGratis(modelo.getCorDaVez(), TIPOS[tipoGratis.getSelectedIndex()]));
		}
	}
}
