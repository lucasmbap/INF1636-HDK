package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import Model.ModeloApi;
import Model.ObservadoIF;
import Model.ObservadorIF;
import Model.TipoTributo;

/**
 * Cartela do herdeiro do JOGADOR DA VEZ (so a dele aparece), com as colunas clicaveis.
 * Observadora do Model: a cada mudanca (troca de jogador, coluna ativada, melhoria
 * comprada...) ela se redesenha.
 */
public class JanelaCartela extends JPanel implements ObservadorIF {
	private static final long serialVersionUID = 1L;

	private final ModeloApi modelo;
	private final PainelCartela painel;
	private final JLabel titulo = new JLabel(" ", SwingConstants.CENTER);
	private final JLabel info = new JLabel(" ");

	public JanelaCartela(ModeloApi modelo) {
		this.modelo = modelo;
		this.painel = new PainelCartela(modelo);
		setLayout(new BorderLayout());

		titulo.setOpaque(true);
		titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
		titulo.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		info.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
		add(titulo, BorderLayout.NORTH);
		add(painel, BorderLayout.CENTER);
		add(info, BorderLayout.SOUTH);

		modelo.add(this);
		atualizar();
	}

	/** Quem recebe o clique numa coluna (o Controller). */
	public void setOuvinte(OuvinteAcoes ouvinte) {
		painel.setOuvinte(ouvinte);
	}

	/** Deixa de observar o Model (chamado quando a janela que contem esta aba fecha). */
	public void desregistrar() {
		modelo.remove(this);
	}

	@Override
	public void notify(ObservadoIF observado) {
		ThreadSwing.executar(new Runnable() {
			@Override
			public void run() {
				atualizar();
			}
		});
	}

	private void atualizar() {
		int id = modelo.isPartidaIniciada() ? modelo.getIdJogadorDaVez() : -1;
		if (id < 0) {
			titulo.setText("Aguardando o início da partida");
			titulo.setBackground(Color.LIGHT_GRAY);
			titulo.setForeground(Color.BLACK);
			painel.setImagem(null, "");
			info.setText(" ");
			return;
		}
		String cor = modelo.getCorJogador(id);
		Color corJogador = Cores.de(cor);
		String herdeiro = modelo.getHerdeiro(id);
		titulo.setBackground(corJogador);
		titulo.setForeground(Cores.textoSobre(corJogador));
		int ativada = modelo.getColunaAtivada();
		titulo.setText("Cartela de " + modelo.getNomeJogador(id) + " (" + herdeiro + ")  -  "
				+ (ativada < 0 ? "escolha uma coluna" : "coluna " + PainelCartela.nomeDaColuna(ativada) + " ativada"));
		painel.setImagem(Herdeiros.cartela(herdeiro), "Cartela não encontrada: " + herdeiro);
		painel.repaint();

		StringBuilder sb = new StringBuilder("<html><b>Neste turno:</b> movimentos ");
		sb.append(modelo.getMovimentosRestantes()).append(", punhos ").append(modelo.getPunhosRestantes());
		for (TipoTributo t : TipoTributo.values()) {
			int v = modelo.getRecursoVirtual(t);
			if (v > 0)
				sb.append(", ").append(t).append(" virtual ").append(v);
		}
		sb.append("&nbsp;&nbsp;|&nbsp;&nbsp;<b>Peças de ativação livres:</b> ")
				.append(modelo.getPecasDeAtivacaoLivres(cor));
		sb.append("<br><b>Recursos:</b> ");
		boolean algum = false;
		for (Map.Entry<TipoTributo, Integer> r : modelo.getRecursos(id).entrySet()) {
			if (r.getValue() > 0) {
				sb.append(r.getKey()).append(" ").append(r.getValue()).append("&nbsp;&nbsp; ");
				algum = true;
			}
		}
		if (!algum)
			sb.append("nenhum");
		sb.append("</html>");
		info.setText(sb.toString());
	}
}
