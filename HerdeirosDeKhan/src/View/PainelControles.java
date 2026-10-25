package View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Model.ModeloApi;
import Model.ObservadorModelo;

/**
 * Painel lateral, so com componentes Swing (nenhuma imagem do jogo aqui).
 * Como o jogo nao tem dados, o testador escolhe os movimentos (cavalos) e os
 * punhos do turno nos combos. A View apenas avisa o ouvinte; quem altera o
 * Model e o Controller.
 */
public class PainelControles extends JPanel implements ObservadorModelo {
	private static final long serialVersionUID = 1L;

	private final ModeloApi modelo;
	private OuvinteJogo ouvinte = null;

	private final JComboBox<Integer> comboMovimentos = new JComboBox<Integer>();
	private final JComboBox<Integer> comboPunhos = new JComboBox<Integer>();
	private final JButton botaoIniciar = new JButton("Iniciar turno");
	private final JButton botaoPassar = new JButton("Passar a vez");
	private final JLabel rotuloVez = new JLabel(" ");
	private final JLabel rotuloInfo = new JLabel(" ");

	public PainelControles(ModeloApi modelo) {
		this.modelo = modelo;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		setPreferredSize(new Dimension(240, 980));

		for (int i = 0; i <= 12; i++)
			comboMovimentos.addItem(i);
		for (int i = 0; i <= 6; i++)
			comboPunhos.addItem(i);
		comboMovimentos.setSelectedItem(3);

		adicionar(rotuloVez);
		add(Box.createVerticalStrut(16));
		adicionar(new JLabel("Movimentos (cavalos):"));
		adicionar(comboMovimentos);
		add(Box.createVerticalStrut(8));
		adicionar(new JLabel("Punhos:"));
		adicionar(comboPunhos);
		add(Box.createVerticalStrut(12));
		adicionar(botaoIniciar);
		add(Box.createVerticalStrut(6));
		adicionar(botaoPassar);
		add(Box.createVerticalStrut(16));
		adicionar(rotuloInfo);
		add(Box.createVerticalGlue());

		botaoIniciar.addActionListener(new AcaoIniciar());
		botaoPassar.addActionListener(new AcaoPassar());
		modelo.registrarObservador(this);
		modeloAlterado();
	}

	public void setOuvinte(OuvinteJogo ouvinte) {
		this.ouvinte = ouvinte;
	}

	private void adicionar(JComponent c) {
		c.setAlignmentX(LEFT_ALIGNMENT);
		c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 4));
		add(c);
	}

	private class AcaoIniciar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (ouvinte == null)
				return;
			int mov = (Integer) comboMovimentos.getSelectedItem();
			int punhos = (Integer) comboPunhos.getSelectedItem();
			ouvinte.turnoSolicitado(mov, punhos);
		}
	}

	private class AcaoPassar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (ouvinte != null)
				ouvinte.passarVezSolicitado();
		}
	}

	@Override
	public void modeloAlterado() {
		if (!modelo.isPartidaIniciada())
			return;
		int id = modelo.getIdJogadorDaVez();
		Color cor = Cores.de(modelo.getCorDaVez());
		Color texto = Cores.textoSobre(cor);
		setBackground(cor);
		rotuloVez.setForeground(texto);
		rotuloInfo.setForeground(texto);
		for (java.awt.Component c : getComponents())
			if (c instanceof JLabel)
				c.setForeground(texto);

		if (id < 0)
			return;
		rotuloVez.setText("<html><b>" + modelo.getNomeJogador(id) + "</b> (" + modelo.getHerdeiro(id) + ")</html>");

		StringBuilder sb = new StringBuilder("<html>");
		sb.append("Movimentos restantes: ").append(modelo.getMovimentosRestantes()).append("<br>");
		sb.append("Votos: ").append(modelo.getVotos(id)).append("<br>");
		sb.append("Yurts na reserva: ").append(modelo.getYurts(id)).append("<br>");
		for (Map.Entry<?, Integer> r : modelo.getRecursos(id).entrySet())
			sb.append(r.getKey()).append(": ").append(r.getValue()).append("<br>");
		sb.append("</html>");
		rotuloInfo.setText(sb.toString());
		repaint();
	}
}
