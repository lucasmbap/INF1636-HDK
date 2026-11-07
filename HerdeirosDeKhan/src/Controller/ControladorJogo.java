package Controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import Model.ModeloApi;
import Model.ObservadoIF;
import Model.ObservadorIF;
import View.CoordenadasMapa;
import View.OuvinteAcoes;

/**
 * Organiza os eventos e a sequencia de acoes de cada turno.
 */
public final class ControladorJogo implements ActionListener, MouseListener, ObservadorIF, OuvinteAcoes {
	private static final String ENCERRAR_TURNO = "encerrarTurno";
	private static final String ENTREGAR_TESOUROS = "entregarTesouros";

	private final ModeloApi modelo;
	private final JComponent tabuleiro;
	private final PainelControle painelControle;
	private boolean encerrado;
	private boolean atualizandoConselheiros;

	public ControladorJogo(ModeloApi modelo, JComponent tabuleiro, PainelControle painelControle) {
		if (modelo == null || tabuleiro == null || painelControle == null)
			throw new IllegalArgumentException("Modelo, tabuleiro e painel de controle sao obrigatorios.");
		this.modelo = modelo;
		this.tabuleiro = tabuleiro;
		this.painelControle = painelControle;

		painelControle.getBotaoEncerrarTurno().setActionCommand(ENCERRAR_TURNO);
		painelControle.getBotaoEncerrarTurno().addActionListener(this);
		painelControle.getBotaoEntregarTesouros().setActionCommand(ENTREGAR_TESOUROS);
		painelControle.getBotaoEntregarTesouros().addActionListener(this);
		painelControle.getSeletorAcao().addActionListener(this);
		painelControle.getSeletorConselheiro().addActionListener(this);
		tabuleiro.addMouseListener(this);
		modelo.add(this);
		atualizarInterface();
	}

	@Override
	public void colunaEscolhida(int coluna) {
		if (modelo.ativarColuna(coluna))
			painelControle.exibirMensagem("Coluna ativada. Escolha uma acao e clique no tabuleiro.");
		else
			painelControle.exibirMensagem("Nao foi possivel ativar essa coluna.");
	}

	@Override
	public void actionPerformed(ActionEvent evento) {
		if (evento.getSource() == painelControle.getSeletorAcao()) {
			atualizarInterface();
			return;
		}
		if (evento.getSource() == painelControle.getSeletorConselheiro()) {
			if (!atualizandoConselheiros)
				atualizarEntregaSelecionada();
			return;
		}
		if (ENTREGAR_TESOUROS.equals(evento.getActionCommand())) {
			entregarTesouros();
			return;
		}
		if (!ENCERRAR_TURNO.equals(evento.getActionCommand()))
			return;
		if (!modelo.passarVez()) {
			painelControle.exibirMensagem(modelo.isKhanPendente()
					? "Use o Khan antes de encerrar o turno."
					: "O turno nao pode ser encerrado agora.");
			return;
		}
		if (modelo.isPartidaEncerrada())
			mostrarVencedores();
		else
			painelControle.exibirMensagem("Turno encerrado.");
	}

	@Override
	public void mouseClicked(MouseEvent evento) {
		if (evento.getButton() != MouseEvent.BUTTON1 || !modelo.isPartidaIniciada()
				|| modelo.getColunaAtivada() < 0)
			return;

		String acao = painelControle.getAcaoSelecionada();
		PointNoMapa ponto = pontoNoClique(evento.getX(), evento.getY(), tabuleiro.getSize());
		if (ponto == null)
			return;

		boolean realizado = false;
		if (PainelControle.ACAO_MOVIMENTAR.equals(acao)) {
			String parada = CoordenadasMapa.paradaEm(evento.getX(), evento.getY(), ponto.origemX,
					ponto.origemY, ponto.escala);
			if (parada == null || !modelo.moverPara(parada))
				return;
			painelControle.exibirMensagem("Movimento realizado.");
			return;
		} else if (PainelControle.ACAO_TRIBUTO.equals(acao)) {
			String provincia = CoordenadasMapa.provinciaEm(evento.getX(), evento.getY(), ponto.origemX,
					ponto.origemY, ponto.escala);
			if (provincia != null)
				realizado = modelo.pegarTributo(provincia);
		} else if (PainelControle.ACAO_ATACAR.equals(acao)) {
			String cidade = CoordenadasMapa.cidadeEm(evento.getX(), evento.getY(), ponto.origemX,
					ponto.origemY, ponto.escala);
			if (cidade != null)
				realizado = modelo.atacarCidade(cidade, painelControle.getTesouroSelecionado());
		} else if (PainelControle.ACAO_YURT.equals(acao)) {
			String parada = CoordenadasMapa.paradaEm(evento.getX(), evento.getY(), ponto.origemX,
					ponto.origemY, ponto.escala);
			if (parada != null)
				realizado = modelo.construirYurt(parada);
		} else if (PainelControle.ACAO_KHAN.equals(acao) && modelo.isKhanPendente()) {
			List<String> destinos = modelo.getDestinosDoKhan();
			String destino = CoordenadasMapa.destinoKhanEm(evento.getX(), evento.getY(), ponto.origemX,
					ponto.origemY, ponto.escala, destinos);
			if (destino != null)
				realizado = modelo.usarKhan(destino);
		}

		if (realizado)
			painelControle.exibirMensagem("Acao realizada.");
		else
			painelControle.exibirMensagem("Acao impossivel neste local ou com os recursos disponiveis.");
	}

	private static PointNoMapa pontoNoClique(int x, int y, Dimension tamanho) {
		if (tamanho.width <= 0 || tamanho.height <= 0)
			return null;
		double escala = Math.min((double) tamanho.width / CoordenadasMapa.LARGURA_REF,
				(double) tamanho.height / CoordenadasMapa.ALTURA_REF);
		int largura = (int) Math.round(CoordenadasMapa.LARGURA_REF * escala);
		int altura = (int) Math.round(CoordenadasMapa.ALTURA_REF * escala);
		return new PointNoMapa((tamanho.width - largura) / 2, (tamanho.height - altura) / 2, escala);
	}

	@Override
	public void notify(ObservadoIF observado) {
		if (SwingUtilities.isEventDispatchThread()) {
			atualizarInterface();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					atualizarInterface();
				}
			});
		}
	}

	private void atualizarInterface() {
		if (encerrado)
			return;
		atualizarConselheiros();
		painelControle.atualizarEstado(modelo.getCorDaVez(), modelo.getMovimentosRestantes(),
				modelo.getPunhosRestantes(), modelo.isPartidaIniciada(), modelo.isPartidaEncerrada(),
				modelo.getColunaAtivada(), modelo.isKhanPendente());
		tabuleiro.repaint();
	}

	private void atualizarConselheiros() {
		atualizandoConselheiros = true;
		try {
			painelControle.atualizarConselheiros(modelo.getConselheirosVisitadosNoTurno());
		} finally {
			atualizandoConselheiros = false;
		}
		atualizarEntregaSelecionada();
	}

	private void atualizarEntregaSelecionada() {
		String conselheiro = painelControle.getConselheiroSelecionado();
		List<String> requisitos = conselheiro == null
				? java.util.Collections.<String>emptyList()
				: modelo.getRequisitosConselheiro(conselheiro);
		painelControle.atualizarEntrega(requisitos);
	}

	private void entregarTesouros() {
		String conselheiro = painelControle.getConselheiroSelecionado();
		if (conselheiro == null)
			return;
		List<String> requisitos = modelo.getRequisitosConselheiro(conselheiro);
		if (requisitos.isEmpty()) {
			painelControle.exibirMensagem("Os requisitos deste conselheiro ainda nao estao configurados.");
			return;
		}
		List<String> tesouros = painelControle.getTesourosEntregaSelecionados(requisitos.size());
		if (modelo.entregarTesourosConselheiro(conselheiro, tesouros))
			painelControle.exibirMensagem("Tesouros entregues. Voto(s) e bonus aplicados.");
		else
			painelControle.exibirMensagem("Entrega impossivel: verifique os tesouros disponiveis.");
	}

	private void mostrarVencedores() {
		List<String> vencedores = modelo.getVencedores();
		if (vencedores.isEmpty()) {
			painelControle.exibirMensagem("Partida encerrada.");
			return;
		}
		StringBuilder mensagem = new StringBuilder("Vencedor");
		if (vencedores.size() > 1)
			mensagem.append("es");
		mensagem.append(": ");
		for (int i = 0; i < vencedores.size(); i++) {
			if (i > 0)
				mensagem.append(", ");
			mensagem.append(nomeJogador(vencedores.get(i)));
		}
		painelControle.exibirMensagem(mensagem.toString());
	}

	private String nomeJogador(String cor) {
		for (int id = 1; id <= modelo.getCores().size(); id++)
			if (cor.equals(modelo.getCorJogador(id)))
				return modelo.getNomeJogador(id);
		return cor;
	}

	/** Desregistra listeners ao fechar a janela do tabuleiro. */
	public void encerrar() {
		if (encerrado)
			return;
		encerrado = true;
		tabuleiro.removeMouseListener(this);
		modelo.remove(this);
		painelControle.getBotaoEncerrarTurno().removeActionListener(this);
		painelControle.getBotaoEntregarTesouros().removeActionListener(this);
		painelControle.getSeletorAcao().removeActionListener(this);
		painelControle.getSeletorConselheiro().removeActionListener(this);
	}

	private static final class PointNoMapa {
		private final int origemX;
		private final int origemY;
		private final double escala;

		private PointNoMapa(int origemX, int origemY, double escala) {
			this.origemX = origemX;
			this.origemY = origemY;
			this.escala = escala;
		}
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
