package Controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import Model.ModeloApi;
import Model.ObservadorModelo;
import View.CoordenadasMapa;

/**
 * Faz a ligacao entre os cliques/controles da interface e a API do modelo.
 */
public final class ControladorJogo implements ActionListener, MouseListener, ObservadorModelo {
	private static final String APLICAR_MOVIMENTOS = "aplicarMovimentos";
	private static final String PROXIMO_JOGADOR = "proximoJogador";
	private static final String SALVAR_RESUMO = "salvarResumo";

	private final ModeloApi modelo;
	private final JComponent tabuleiro;
	private final PainelControle painelControle;
	private String corConfigurada;
	private boolean movimentosConfigurados;
	private boolean encerrado;

	public ControladorJogo(ModeloApi modelo, JComponent tabuleiro, PainelControle painelControle) {
		if (modelo == null || tabuleiro == null || painelControle == null)
			throw new IllegalArgumentException("Modelo, tabuleiro e painel de controle sao obrigatorios.");
		this.modelo = modelo;
		this.tabuleiro = tabuleiro;
		this.painelControle = painelControle;
		this.corConfigurada = modelo.getCorDaVez();

		painelControle.getBotaoIniciarTurno().setActionCommand(APLICAR_MOVIMENTOS);
		painelControle.getBotaoProximoJogador().setActionCommand(PROXIMO_JOGADOR);
		painelControle.getBotaoSalvar().setActionCommand(SALVAR_RESUMO);
		painelControle.getBotaoIniciarTurno().addActionListener(this);
		painelControle.getBotaoProximoJogador().addActionListener(this);
		painelControle.getBotaoSalvar().addActionListener(this);
		tabuleiro.addMouseListener(this);
		modelo.registrarObservador(this);
		atualizarInterface();
	}

	@Override
	public void actionPerformed(ActionEvent evento) {
		String comando = evento.getActionCommand();
		if (APLICAR_MOVIMENTOS.equals(comando)) {
			if (movimentosConfigurados)
				return;
			Integer movimentos = (Integer) painelControle.getSeletorMovimentos().getSelectedItem();
			if (movimentos != null && modelo.definirMovimentos(movimentos.intValue())) {
				movimentosConfigurados = true;
				atualizarInterface();
				painelControle.exibirMensagem("Movimentos aplicados.");
			} else {
				painelControle.exibirMensagem("Nao foi possivel iniciar o turno.");
			}
		} else if (PROXIMO_JOGADOR.equals(comando)) {
			modelo.passarVez();
			painelControle.exibirMensagem(" ");
		} else if (SALVAR_RESUMO.equals(comando)) {
			salvarResumo();
		}
	}

	@Override
	public void mouseClicked(MouseEvent evento) {
		if (evento.getButton() != MouseEvent.BUTTON1)
			return;
		String parada = paradaNoClique(evento.getX(), evento.getY(), tabuleiro.getSize());
		if (parada != null)
			modelo.moverPara(parada);
	}

	private static String paradaNoClique(int x, int y, Dimension tamanho) {
		if (tamanho.width <= 0 || tamanho.height <= 0)
			return null;
		double escala = Math.min((double) tamanho.width / CoordenadasMapa.LARGURA_REF,
				(double) tamanho.height / CoordenadasMapa.ALTURA_REF);
		int larguraDesenhada = (int) Math.round(CoordenadasMapa.LARGURA_REF * escala);
		int alturaDesenhada = (int) Math.round(CoordenadasMapa.ALTURA_REF * escala);
		int origemX = (tamanho.width - larguraDesenhada) / 2;
		int origemY = (tamanho.height - alturaDesenhada) / 2;
		return CoordenadasMapa.paradaEm(x, y, origemX, origemY, escala);
	}

	private void salvarResumo() {
		JFileChooser seletor = new JFileChooser();
		seletor.setDialogTitle("Salvar resumo da partida");
		if (seletor.showSaveDialog(tabuleiro) != JFileChooser.APPROVE_OPTION)
			return;

		try (BufferedWriter escritor = Files.newBufferedWriter(seletor.getSelectedFile().toPath(),
				StandardCharsets.UTF_8)) {
			escritor.write("Resumo da partida - este arquivo nao permite retomar o jogo.");
			escritor.newLine();
			escritor.write("Jogador da vez: " + modelo.getCorDaVez());
			escritor.newLine();
			escritor.write("Movimentos restantes: " + modelo.getMovimentosRestantes());
			escritor.newLine();
			escritor.write("Posicoes dos peoes:");
			escritor.newLine();
			for (String cor : modelo.getCores()) {
				escritor.write(cor + ": " + modelo.getPosicao(cor));
				escritor.newLine();
			}
			escritor.write("Paradas visitadas neste turno: " + modelo.getParadasVisitadasNoTurno());
			escritor.newLine();
			painelControle.exibirMensagem("Resumo salvo.");
		} catch (IOException erro) {
			painelControle.exibirMensagem("Falha ao salvar: " + erro.getMessage());
		}
	}

	@Override
	public void modeloAlterado() {
		if (SwingUtilities.isEventDispatchThread()) {
			atualizarDepoisDaMudanca();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					atualizarDepoisDaMudanca();
				}
			});
		}
	}

	private void atualizarDepoisDaMudanca() {
		String corAtual = modelo.getCorDaVez();
		if (corAtual != null && !corAtual.equals(corConfigurada)) {
			corConfigurada = corAtual;
			movimentosConfigurados = false;
		}
		atualizarInterface();
	}

	private void atualizarInterface() {
		if (encerrado)
			return;
		painelControle.atualizarEstado(modelo.getCorDaVez(), modelo.getMovimentosRestantes(),
				modelo.isPartidaIniciada(), modelo.isPartidaIniciada() && !movimentosConfigurados);
		tabuleiro.repaint();
	}

	/**
	 * Remove os listeners quando a janela da partida e fechada.
	 */
	public void encerrar() {
		if (encerrado)
			return;
		encerrado = true;
		tabuleiro.removeMouseListener(this);
		modelo.removerObservador(this);
		painelControle.getBotaoIniciarTurno().removeActionListener(this);
		painelControle.getBotaoProximoJogador().removeActionListener(this);
		painelControle.getBotaoSalvar().removeActionListener(this);
	}

	@Override
	public void mousePressed(MouseEvent evento) {
	}

	@Override
	public void mouseReleased(MouseEvent evento) {
	}

	@Override
	public void mouseEntered(MouseEvent evento) {
	}

	@Override
	public void mouseExited(MouseEvent evento) {
	}
}
