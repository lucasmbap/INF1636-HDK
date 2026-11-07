package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import Model.ModeloApi;
import Model.ObservadoIF;
import Model.ObservadorIF;

/**
 * Primeira janela: "Nova partida" (abre a escolha de herdeiros) ou "Sair".
 * É observadora do Model: quando a partida começa, ela se fecha sozinha.
 */
public class JanelaInicial extends JFrame implements ObservadorIF {
	private static final long serialVersionUID = 1L;

	private final ModeloApi modelo;
	private final OuvinteInicio ouvinte;
	private final JButton botaoNova = new JButton("Nova partida");
	private final JButton botaoSair = new JButton("Sair");

	public JanelaInicial(ModeloApi modelo, OuvinteInicio ouvinte) {
		super("Herdeiros de Khan");
		this.modelo = modelo;
		this.ouvinte = ouvinte;
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		PainelFundo fundo = new PainelFundo(Imagens.carregar(Main.CAMINHO_TABULEIRO));
		fundo.setLayout(new BorderLayout());
		JPanel botoes = new JPanel();
		botoes.setOpaque(false);
		botoes.setLayout(new BoxLayout(botoes, BoxLayout.Y_AXIS));
		botoes.setBorder(BorderFactory.createEmptyBorder(0, 120, 50, 120));
		botaoNova.setAlignmentX(CENTER_ALIGNMENT);
		botaoSair.setAlignmentX(CENTER_ALIGNMENT);
		botaoNova.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		botaoSair.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		botoes.add(botaoNova);
		botoes.add(Box.createVerticalStrut(10));
		botoes.add(botaoSair);
		fundo.add(botoes, BorderLayout.SOUTH);
		add(fundo);

		botaoNova.addActionListener(new AcaoNova());
		botaoSair.addActionListener(new AcaoSair());

		setSize(520, 460);
		setResizable(false);
		setLocationRelativeTo(null);
		modelo.add(this);
	}

	private class AcaoNova implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JanelaEscolhaHerdeiros escolha = new JanelaEscolhaHerdeiros(modelo, ouvinte);
			escolha.setVisible(true);
		}
	}

	private class AcaoSair implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	@Override
	public void notify(ObservadoIF o) {
		ThreadSwing.executar(new Runnable() {
			@Override
			public void run() {
				if (modelo.isPartidaIniciada()) {
					modelo.remove(JanelaInicial.this);
					dispose();
				}
			}
		});
	}

	/** Fundo: o tabuleiro escurecido, com o título desenhado por cima. */
	private static class PainelFundo extends JPanel {
		private static final long serialVersionUID = 1L;
		private final BufferedImage imagem;
		private BufferedImage escalada = null;

		PainelFundo(BufferedImage imagem) {
			this.imagem = imagem;
			setBackground(new Color(30, 20, 10));
		}

		@Override
		protected void paintComponent(Graphics gr) {
			super.paintComponent(gr);
			Graphics2D g = (Graphics2D) gr.create();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			if (imagem != null) {
				int lado = Math.max(getWidth(), getHeight());
				if (escalada == null || escalada.getWidth() != lado)
					escalada = Imagens.redimensionar(imagem, lado, lado);
				g.drawImage(escalada, (getWidth() - lado) / 2, (getHeight() - lado) / 2, null);
			}
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(new Color(245, 225, 170));
			g.setFont(new Font("Serif", Font.BOLD, 44));
			String t = "Herdeiros de Khan";
			g.drawString(t, (getWidth() - g.getFontMetrics().stringWidth(t)) / 2, 150);
			g.dispose();
		}
	}
}