package Model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ModeloApi {
	private final Map<Integer, Jogador> jogadores = new HashMap<>();
	private int proximoIdJogador = 1;
	private final Mapa mapa = MapaOficial.criar();
	private final List<Jogador> ordemJogadores = new ArrayList<Jogador>(); // ordem dos turnos
	private Partida partida = null; // criada em iniciarPartida()
	private final List<ObservadorModelo> observadores = new ArrayList<ObservadorModelo>();

	public int criarJogador(String nome, String cor, String herdeiro) {
		Jogador jogador = new Jogador(nome, cor, herdeiro);
		int id = proximoIdJogador;
		int proximoId = Math.incrementExact(proximoIdJogador);
		jogadores.put(id, jogador);
		proximoIdJogador = proximoId;
		if (partida == null)
			ordemJogadores.add(jogador); // entra na partida, na ordem de criacao
		return id;
	}

	public String getNomeJogador(int idJogador) {
		return obterJogador(idJogador).getNome();
	}

	public String getCorJogador(int idJogador) {
		return obterJogador(idJogador).getCor();
	}

	public String getHerdeiro(int idJogador) {
		return obterJogador(idJogador).getHerdeiro();
	}

	public void setHerdeiro(int idJogador, String herdeiro) {
		obterJogador(idJogador).setHerdeiro(herdeiro);
	}

	public void adicionarTributo(int idJogador, TipoTributo tipo) {
		obterJogador(idJogador).adicionarTributo(new PecaTributo(tipo));
	}

	public void adicionarRecursos(int idJogador, TipoTributo tipo, int quantidade) {
		obterJogador(idJogador).adicionarRecursos(tipo, quantidade);
	}

	public boolean removerRecursos(int idJogador, TipoTributo tipo, int quantidade) {
		return obterJogador(idJogador).removerRecursos(tipo, quantidade);
	}

	public int getQuantidadeRecurso(int idJogador, TipoTributo tipo) {
		return obterJogador(idJogador).getQuantidadeRecurso(tipo);
	}

	public Map<TipoTributo, Integer> getRecursos(int idJogador) {
		return obterJogador(idJogador).getRecursos();
	}

	public int getYurts(int idJogador) {
		return obterJogador(idJogador).getYurts();
	}

	public void adicionarTesouro(int idJogador, String nome) {
		obterJogador(idJogador).adicionarTesouro(new Tesouro(nome));
	}

	public List<String> getNomesTesouros(int idJogador) {
		return obterJogador(idJogador).getNomesTesouros();
	}

	public boolean removerTesouro(int idJogador, String nome) {
		Tesouro tesouro = new Tesouro(nome);
		return obterJogador(idJogador).removerTesouro(tesouro);
	}

	public void adicionarCartaConselheiro(int idJogador, String nome, String condicao, String recompensa,
			boolean aberta) {
		obterJogador(idJogador)
				.adicionarCartaConselheiro(new CartaConselheiro(nome, condicao, recompensa, aberta));
	}

	public List<String> getNomesCartasConselheiro(int idJogador) {
		return obterJogador(idJogador).getNomesCartasConselheiro();
	}

	public int getQuantidadeCartasConselheiro(int idJogador) {
		return obterJogador(idJogador).getCartasConselheiro().size();
	}

	public String getNomeCartaConselheiro(int idJogador, int indice) {
		return obterCartaConselheiro(idJogador, indice).getNome();
	}

	public String getCondicaoCartaConselheiro(int idJogador, int indice) {
		return obterCartaConselheiro(idJogador, indice).getCondicao();
	}

	public String getRecompensaCartaConselheiro(int idJogador, int indice) {
		return obterCartaConselheiro(idJogador, indice).getRecompensa();
	}

	public boolean isCartaConselheiroAberta(int idJogador, int indice) {
		return obterCartaConselheiro(idJogador, indice).isAberta();
	}

	public void revelarCartaConselheiro(int idJogador, int indice) {
		obterCartaConselheiro(idJogador, indice).revelar();
	}

	public void adicionarMelhoria(int idJogador, String nome, String efeito, String condicao) {
		obterJogador(idJogador).adicionarMelhoria(new PecaMelhoria(nome, efeito, condicao));
	}

	public List<String> getNomesMelhorias(int idJogador) {
		return obterJogador(idJogador).getNomesMelhorias();
	}

	public int getQuantidadeMelhorias(int idJogador) {
		return obterJogador(idJogador).getMelhorias().size();
	}

	public String getNomeMelhoria(int idJogador, int indice) {
		return obterMelhoria(idJogador, indice).getNome();
	}

	public String getEfeitoMelhoria(int idJogador, int indice) {
		return obterMelhoria(idJogador, indice).getEfeito();
	}

	public String getCondicaoMelhoria(int idJogador, int indice) {
		return obterMelhoria(idJogador, indice).getCondicao();
	}

	public int getVotos(int idJogador) {
		return obterJogador(idJogador).getVotos();
	}

	public void adicionarVotos(int idJogador, int quantidade) {
		obterJogador(idJogador).adicionarVotos(quantidade);
	}

	public boolean removerVotos(int idJogador, int quantidade) {
		return obterJogador(idJogador).removerVotos(quantidade);
	}

	// =====================================================================
	// Partida, turno e mapa (2a iteracao)
	// As regras ficam na classe Partida; o mapa, em MapaOficial. Os
	// jogadores sao identificados pela cor nestes metodos, pois e pela cor
	// que a View desenha peoes e yurts.
	// =====================================================================

	// ordem em que as cidades sao reveladas e tipos de tesouro (8 de cada)
	private static final String[] ORDEM_CIDADES = { "Kiev", "Pequim", "Bagda", "Moscou", "Kaesong",
			"Samarcanda", "Sarai", "Cantao", "Cabul" };
	private static final String[] TIPOS_TESOURO = { "gado", "especiaria", "pele", "ferro", "seda" };
	private static final int CIDADES_INICIAIS = 3;
	private static final int TESOUROS_POR_CIDADE = 4;

	/**
	 * Comeca a partida com os jogadores ja criados (2 a 5): peoes em
	 * Karakorum, 1 tributo em cada provincia e 3 cidades reveladas com 4
	 * tesouros cada. Retorna false se ja comecou ou se o numero de
	 * jogadores nao for valido.
	 */
	public boolean iniciarPartida() {
		if (partida != null || ordemJogadores.size() < 2 || ordemJogadores.size() > 5)
			return false;
		Deque<Tesouro> pilha = new ArrayDeque<Tesouro>();
		for (int i = 0; i < 8; i++)
			for (String t : TIPOS_TESOURO)
				pilha.add(new Tesouro(t));
		Deque<String> fechadas = new ArrayDeque<String>();
		for (int i = 0; i < ORDEM_CIDADES.length; i++) {
			if (i < CIDADES_INICIAIS) {
				List<Tesouro> tesouros = new ArrayList<Tesouro>();
				for (int k = 0; k < TESOUROS_POR_CIDADE; k++)
					tesouros.add(pilha.poll());
				mapa.revelarCidade(ORDEM_CIDADES[i], tesouros);
			} else {
				fechadas.add(ORDEM_CIDADES[i]);
			}
		}
		for (String prov : mapa.getNomesProvincias())
			mapa.colocarTributo(prov);
		partida = new Partida(mapa, ordemJogadores, fechadas, pilha);
		partida.iniciarTurno(0, 0);
		notificar();
		return true;
	}

	public boolean isPartidaIniciada() {
		return partida != null;
	}

	/** Cor do jogador da vez, ou null se a partida nao comecou. */
	public String getCorDaVez() {
		if (partida == null)
			return null;
		return partida.getCorAtual();
	}

	/** Identificador (o mesmo de criarJogador) do jogador da vez, ou -1. */
	public int getIdJogadorDaVez() {
		String cor = getCorDaVez();
		for (Map.Entry<Integer, Jogador> e : jogadores.entrySet())
			if (e.getValue().getCor().equals(cor))
				return e.getKey();
		return -1;
	}

	/** Cores dos jogadores na ordem dos turnos. */
	public List<String> getCores() {
		List<String> cores = new ArrayList<String>();
		for (Jogador j : ordemJogadores)
			cores.add(j.getCor());
		return cores;
	}

	/**
	 * Define os movimentos do jogador da vez (escolha do testador, no lugar
	 * dos "dados"). Comeca de novo o turno do jogador atual com esses
	 * movimentos e sem punhos.
	 */
	public boolean definirMovimentos(int quantidade) {
		return iniciarTurno(quantidade, 0);
	}

	/** Comeca o turno do jogador da vez com os movimentos e punhos da coluna escolhida. */
	public boolean iniciarTurno(int movimentos, int punhos) {
		if (partida == null || partida.isEncerrada() || movimentos < 0 || punhos < 0)
			return false;
		partida.iniciarTurno(movimentos, punhos);
		notificar();
		return true;
	}

	public int getMovimentosRestantes() {
		if (partida == null)
			return 0;
		return partida.getMovimentosRestantes();
	}

	/**
	 * Tenta mover o peao da vez ate a parada clicada. Retorna false se o
	 * movimento nao for permitido; nesse caso nada muda.
	 */
	public boolean moverPara(String parada) {
		if (partida == null || !partida.moverPara(parada))
			return false;
		notificar();
		return true;
	}

	/** Encerra o turno e passa a vez para o proximo jogador. */
	public void passarVez() {
		if (partida == null || partida.isEncerrada())
			return;
		partida.encerrarTurno();
		if (!partida.isEncerrada())
			partida.iniciarTurno(0, 0);
		notificar();
	}

	/** Paradas onde o jogador da vez esteve neste turno. */
	public List<String> getParadasVisitadasNoTurno() {
		if (partida == null)
			return new ArrayList<String>();
		return partida.getParadasVisitadas();
	}

	/** Parada onde esta o peao da cor indicada, ou null. */
	public String getPosicao(String cor) {
		return mapa.getPosicao(cor);
	}

	/** Nomes de todas as paradas (R1..R11, C1..C11, P1..P11 e Karakorum). */
	public List<String> getParadas() {
		return mapa.getNomesParadas();
	}

	/** Cores dos peoes que estao na parada. */
	public List<String> getPeoesNaParada(String parada) {
		return mapa.getPeoesNaParada(parada);
	}

	/** Cores dos donos dos yurts da parada. */
	public List<String> getYurtsNaParada(String parada) {
		return mapa.getYurtsNaParada(parada);
	}

	public List<String> getProvincias() {
		return mapa.getNomesProvincias();
	}

	/** Quantidade de pecas de tributo na provincia (0 a 3). */
	public int getTributos(String provincia) {
		Provincia p = mapa.getProvincia(provincia);
		if (p == null)
			return 0;
		return p.getQuantidade();
	}

	/** Tipo de tributo da provincia, ou null se ela nao existir. */
	public TipoTributo getTipoTributo(String provincia) {
		Provincia p = mapa.getProvincia(provincia);
		if (p == null)
			return null;
		return p.getTipo();
	}

	/** Provincia do Khan onde ele esta, "Area de melhorias" ou null. */
	public String getPosicaoKhan() {
		return mapa.getPosicaoKhan();
	}

	public List<String> getCidades() {
		return mapa.getNomesCidades();
	}

	/** Cidades que ja foram reveladas e ainda tem tesouros. */
	public List<String> getCidadesDisponiveis() {
		return mapa.getCidadesDisponiveis();
	}

	/** Cor de quem conquistou a cidade, ou null. */
	public String getConquistadorCidade(String cidade) {
		Cidade c = mapa.getCidade(cidade);
		if (c == null)
			return null;
		return c.getConquistador();
	}

	/** Padrao Observer: a View e avisada a cada mudanca da partida. */
	public void registrarObservador(ObservadorModelo o) {
		if (o != null && !observadores.contains(o))
			observadores.add(o);
	}

	public void removerObservador(ObservadorModelo o) {
		observadores.remove(o);
	}

	private void notificar() {
		for (ObservadorModelo o : observadores)
			o.modeloAlterado();
	}

	private Jogador obterJogador(int idJogador) {
		Jogador jogador = jogadores.get(idJogador);
		if (jogador == null) {
			throw new IllegalArgumentException("Não existe jogador com o identificador " + idJogador + ".");
		}
		return jogador;
	}

	private CartaConselheiro obterCartaConselheiro(int idJogador, int indice) {
		return obterPorIndice(obterJogador(idJogador).getCartasConselheiro(), indice);
	}

	private PecaMelhoria obterMelhoria(int idJogador, int indice) {
		return obterPorIndice(obterJogador(idJogador).getMelhorias(), indice);
	}

	private static <T> T obterPorIndice(List<T> itens, int indice) {
		Objects.checkIndex(indice, itens.size());
		return itens.get(indice);
	}
}