package Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ModeloApi {
	private final Map<Integer, Jogador> jogadores = new HashMap<>();
	private int proximoIdJogador = 1;
	private final Partida partida = new Partida(MapaOficial.criar());

	public int criarJogador(String nome, String cor, String herdeiro) {
		Jogador jogador = new Jogador(nome, cor, herdeiro);
		int id = proximoIdJogador;
		int proximoId = Math.incrementExact(proximoIdJogador);
		jogadores.put(id, jogador);
		proximoIdJogador = proximoId;
		partida.adicionarJogador(jogador); // entra na partida, na ordem de criacao
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
	// Os jogadores sao identificados pela cor nestes metodos, pois e pela
	// cor que a View desenha peoes e yurts.
	// =====================================================================

	/**
	 * Comeca a partida com os jogadores ja criados (minimo 2): coloca os
	 * peoes em Karakorum e 1 tributo em cada provincia.
	 */
	public boolean iniciarPartida() {
		return partida.iniciar();
	}

	public boolean isPartidaIniciada() {
		return partida.isIniciada();
	}

	/** Cor do jogador da vez, ou null se a partida nao comecou. */
	public String getCorDaVez() {
		return partida.getCorDaVez();
	}

	/** Identificador (o mesmo de criarJogador) do jogador da vez, ou -1. */
	public int getIdJogadorDaVez() {
		String cor = partida.getCorDaVez();
		for (Map.Entry<Integer, Jogador> e : jogadores.entrySet())
			if (e.getValue().getCor().equals(cor))
				return e.getKey();
		return -1;
	}

	/** Cores dos jogadores na ordem dos turnos. */
	public List<String> getCores() {
		return partida.getCores();
	}

	/** Define os movimentos do jogador da vez (escolha do testador). */
	public boolean definirMovimentos(int quantidade) {
		return partida.definirMovimentos(quantidade);
	}

	public int getMovimentosRestantes() {
		return partida.getMovimentosRestantes();
	}

	/**
	 * Tenta mover o peao da vez ate a parada clicada. Retorna false se o
	 * movimento nao for permitido; nesse caso nada muda.
	 */
	public boolean moverPara(String parada) {
		return partida.moverPara(parada);
	}

	/** Encerra o turno e passa a vez para o proximo jogador. */
	public void passarVez() {
		partida.passarVez();
	}

	/** Paradas onde o jogador da vez esteve neste turno. */
	public List<String> getParadasVisitadasNoTurno() {
		return partida.getParadasVisitadasNoTurno();
	}

	/** Parada onde esta o peao da cor indicada, ou null. */
	public String getPosicao(String cor) {
		return partida.getMapa().getPosicao(cor);
	}

	/** Nomes de todas as paradas (R1..R11, C1..C11, P1..P11 e Karakorum). */
	public List<String> getParadas() {
		return partida.getMapa().getNomesParadas();
	}

	/** Cores dos peoes que estao na parada. */
	public List<String> getPeoesNaParada(String parada) {
		return partida.getMapa().getPeoesNaParada(parada);
	}

	/** Cores dos donos dos yurts da parada. */
	public List<String> getYurtsNaParada(String parada) {
		return partida.getMapa().getYurtsNaParada(parada);
	}

	public List<String> getProvincias() {
		return partida.getMapa().getNomesProvincias();
	}

	/** Quantidade de pecas de tributo na provincia (0 a 3). */
	public int getTributos(String provincia) {
		Provincia p = partida.getMapa().getProvincia(provincia);
		if (p == null)
			return 0;
		return p.getQuantidade();
	}

	/** Tipo de tributo da provincia, ou null se ela nao existir. */
	public TipoTributo getTipoTributo(String provincia) {
		Provincia p = partida.getMapa().getProvincia(provincia);
		if (p == null)
			return null;
		return p.getTipo();
	}

	/** Provincia do Khan onde ele esta, "Area de melhorias" ou null. */
	public String getPosicaoKhan() {
		return partida.getMapa().getPosicaoKhan();
	}

	public List<String> getCidades() {
		return partida.getMapa().getNomesCidades();
	}

	/** Cor de quem conquistou a cidade, ou null. */
	public String getConquistadorCidade(String cidade) {
		Cidade c = partida.getMapa().getCidade(cidade);
		if (c == null)
			return null;
		return c.getConquistador();
	}

	/** Padrao Observer: a View e avisada a cada mudanca da partida. */
	public void registrarObservador(ObservadorModelo o) {
		partida.registrarObservador(o);
	}

	public void removerObservador(ObservadorModelo o) {
		partida.removerObservador(o);
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