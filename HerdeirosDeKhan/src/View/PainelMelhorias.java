package View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import Model.ModeloApi;

/**
 * Area de melhorias (manual, pag. 9, 11 e 12): mostra as 5 pecas reveladas, da esquerda
 * (ao lado do Khan) para a direita, e o topo da pilha. O jogador escolhe uma peca e compra
 * (2 moedas) para a coluna da cor dela; o Jochi pode escolher qualquer coluna. Quando o
 * jogador completou uma linha, a peca escolhida pode ir de graca para a coluna branca.
 * O Jochi tambem escolhe aqui a sua melhoria inicial. Nao e Observer sozinho: a
 * JanelaJogador (que e) chama atualizar() a cada mudanca do Model.
 */
public class PainelMelhorias extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String[] COLUNAS = { "Vermelha (China)", "Verde (Persia)", "Amarela (Russia)" };
	private static final String[] COR_DO_ARQUIVO = { "vermelha", "verde", "amarela" };
	private static final int ICONE_W = 66;
	private static final int ICONE_H = 34;

	private final ModeloApi modelo;
	private OuvinteMelhorias ouvinte = null;

	// 5 espacos da area + 1 botao para o topo da pilha (indice 5)
	private final JToggleButton[] pecas = new JToggleButton[ModeloApi.ESPACOS_DA_AREA + 1];
	private final ButtonGroup grupo = new ButtonGroup();
	private final JComboBox<String> colunaCompra = new JComboBox<String>(COLUNAS);
	private final JButton comprar = new JButton("Comprar (2 moedas)");
	private final JButton gratis = new JButton("Colocar na coluna branca");
	private final JLabel infoGratis = new JLabel();
	private final JPanel linhaGratis = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
	private final JLabel infoJochi = new JLabel("Jochi: escolha a melhoria inicial da coluna branca:");
	private final JButton[] iniciaisJochi = { new JButton(), new JButton() };
	private final JPanel linhaJochi = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
	private final JLabel mensagem = new JLabel(" ");

	public PainelMelhorias(ModeloApi modelo) {
		this.modelo = modelo;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder("Area de melhorias"));

		JPanel linhaPecas = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 1));
		AcaoPeca acaoPeca = new AcaoPeca();
		for (int i = 0; i < pecas.length; i++) {
			pecas[i] = new JToggleButton();
			pecas[i].setMargin(new Insets(1, 1, 1, 1));
			pecas[i].setPreferredSize(new Dimension(ICONE_W + 8, ICONE_H + 22));
			pecas[i].setVerticalTextPosition(JToggleButton.BOTTOM);
			pecas[i].setHorizontalTextPosition(JToggleButton.CENTER);
			pecas[i].setFont(pecas[i].getFont().deriveFont(Font.PLAIN, 10f));
			pecas[i].addActionListener(acaoPeca);
			grupo.add(pecas[i]);
			linhaPecas.add(pecas[i]);
		}
		add(linhaPecas);

		JPanel linhaCompra = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
		linhaCompra.add(new JLabel("Coluna:"));
		linhaCompra.add(colunaCompra);
		linhaCompra.add(comprar);
		add(linhaCompra);

		linhaGratis.add(infoGratis);
		linhaGratis.add(gratis);
		add(linhaGratis);

		linhaJochi.add(infoJochi);
		AcaoJochi acaoJochi = new AcaoJochi();
		for (JButton b : iniciaisJochi) {
			b.addActionListener(acaoJochi);
			linhaJochi.add(b);
		}
		add(linhaJochi);

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
		String cor = jogando ? modelo.getCorDaVez() : null;

		for (int i = 0; i < ModeloApi.ESPACOS_DA_AREA; i++) {
			String tipo = modelo.getTipoMelhoriaNaArea(i);
			int corPeca = modelo.getCorMelhoriaNaArea(i);
			if (tipo == null) {
				pecas[i].setIcon(null);
				pecas[i].setText(i == ModeloApi.ESPACO_ICONE_MOEDA ? "vazio ($)"
						: i == ModeloApi.ESPACO_ICONE_ESPADA ? "vazio (espada)" : "vazio");
				pecas[i].setToolTipText("Espaco vazio. Se o Khan vier para a area, ele e preenchido.");
			} else {
				pecas[i].setIcon(icone(tipo, corPeca));
				pecas[i].setText(nome(tipo));
				pecas[i].setToolTipText(nome(tipo) + " - coluna " + COLUNAS[corPeca]);
			}
			pecas[i].setEnabled(jogando && tipo != null);
		}
		int naPilha = modelo.getMelhoriasNaPilha();
		pecas[ModeloApi.ESPACOS_DA_AREA].setIcon(null);
		pecas[ModeloApi.ESPACOS_DA_AREA].setText("<html><center>Topo da<br>pilha (" + naPilha + ")</center></html>");
		pecas[ModeloApi.ESPACOS_DA_AREA].setToolTipText("Peca aleatoria: vai para a coluna da cor dela.");
		pecas[ModeloApi.ESPACOS_DA_AREA].setEnabled(jogando && naPilha > 0);

		int selecionado = selecionado();
		if (selecionado >= 0 && !pecas[selecionado].isEnabled()) {
			grupo.clearSelection();
			selecionado = -1;
		}
		ajustarColuna();

		boolean jochi = cor != null && "Jochi".equals(modelo.getHerdeiroDaCartela(cor));
		comprar.setEnabled(jogando && selecionado >= 0);
		int pendentes = cor != null ? modelo.getMelhoriasGratisPendentes(cor) : 0;
		linhaGratis.setVisible(pendentes > 0);
		infoGratis.setText("Melhorias gratis: " + pendentes);
		gratis.setEnabled(selecionado >= 0);

		List<String> iniciais = cor != null ? modelo.getMelhoriasIniciaisJochi(cor) : null;
		linhaJochi.setVisible(jochi && iniciais != null && !iniciais.isEmpty());
		for (int k = 0; k < iniciaisJochi.length; k++) {
			boolean existe = iniciais != null && k < iniciais.size();
			iniciaisJochi[k].setVisible(existe);
			if (existe)
				iniciaisJochi[k].setText(nome(iniciais.get(k)));
		}
	}

	/** A coluna segue a cor da peca escolhida; so o Jochi pode trocar. */
	private void ajustarColuna() {
		String cor = modelo.isPartidaIniciada() ? modelo.getCorDaVez() : null;
		boolean jochi = cor != null && "Jochi".equals(modelo.getHerdeiroDaCartela(cor));
		int s = selecionado();
		boolean daArea = s >= 0 && s < ModeloApi.ESPACOS_DA_AREA;
		if (daArea && !jochi && modelo.getCorMelhoriaNaArea(s) >= 0)
			colunaCompra.setSelectedIndex(modelo.getCorMelhoriaNaArea(s));
		colunaCompra.setEnabled(jochi && s >= 0);
	}

	private int selecionado() {
		for (int i = 0; i < pecas.length; i++)
			if (pecas[i].isSelected())
				return i;
		return -1;
	}

	/** Indice para o Model: 0 a 4 ou TOPO_DA_PILHA. */
	private int espacoSelecionado() {
		int s = selecionado();
		return s == ModeloApi.ESPACOS_DA_AREA ? ModeloApi.TOPO_DA_PILHA : s;
	}

	private static String nome(String tipo) {
		if (tipo.startsWith("TESOURO_"))
			return tipo.substring(8).toLowerCase();
		return tipo.substring(0, 1) + tipo.substring(1).toLowerCase();
	}

	/** imagens/melhoria_<tipo>_<cor>.jpg; se nao houver, a View mostra so o texto. */
	private static ImageIcon icone(String tipo, int cor) {
		String base = "imagens/melhoria_" + Imagens.nomeDeArquivo(tipo);
		BufferedImage img = null;
		if (cor >= 0 && cor < COR_DO_ARQUIVO.length)
			img = Imagens.silenciosa(base + "_" + COR_DO_ARQUIVO[cor]);
		if (img == null)
			img = Imagens.silenciosa(base);
		if (img == null && tipo.equals("MOEDA") && cor == 0)
			img = Imagens.silenciosa("imagens/melhoria_moeda_vermalha"); // nome do arquivo no projeto
		if (img == null && tipo.equals("PUNHO"))
			img = Imagens.silenciosa("imagens/melhoria_mao_" + COR_DO_ARQUIVO[Math.max(0, cor)]);
		if (img == null && tipo.equals("CAVALOS"))
			img = cavalos(cor);
		if (img == null && tipo.equals("YURT"))
			img = Imagens.silenciosa("imagens/melhoria_yurt_" + (cor == 0 ? "vermelho" : cor == 2 ? "amarelo" : "verde"));
		if (img == null)
			return null;
		double e = Math.min((double) ICONE_W / img.getWidth(), (double) ICONE_H / img.getHeight());
		int w = Math.max(1, (int) Math.round(img.getWidth() * e));
		int h = Math.max(1, (int) Math.round(img.getHeight() * e));
		return new ImageIcon(Imagens.redimensionar(img, w, h));
	}

	private static BufferedImage cavalos(int cor) {
		if (cor == 0)
			return Imagens.silenciosa("imagens/melhoria_cavalo_vermelha");
		if (cor == 1)
			return Imagens.silenciosa("imagens/melhoria_cavalo_verde");
		return Imagens.silenciosa("imagens/melhoria_cavalos_amarela");
	}

	private void mostrar(String erro) {
		// html para quebrar a mensagem em linhas dentro da largura da janela
		mensagem.setText(erro == null ? " " : "<html><body style='width:350px'>" + erro + "</body></html>");
	}

	private class AcaoPeca implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			mostrar(null);
			atualizar();
		}
	}

	private class AcaoComprar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (ouvinte == null || selecionado() < 0)
				return;
			// coluna -1: a peca vai para a coluna da cor dela (o Model confere)
			int coluna = colunaCompra.isEnabled() ? colunaCompra.getSelectedIndex() : -1;
			if (selecionado() < ModeloApi.ESPACOS_DA_AREA && !colunaCompra.isEnabled())
				coluna = colunaCompra.getSelectedIndex();
			mostrar(ouvinte.comprar(espacoSelecionado(), coluna));
		}
	}

	private class AcaoGratis implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (ouvinte == null || selecionado() < 0)
				return;
			mostrar(ouvinte.colocarGratis(modelo.getCorDaVez(), espacoSelecionado()));
		}
	}

	private class AcaoJochi implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (ouvinte == null)
				return;
			int indice = e.getSource() == iniciaisJochi[0] ? 0 : 1;
			mostrar(ouvinte.escolherInicial(modelo.getCorDaVez(), indice));
		}
	}
}
