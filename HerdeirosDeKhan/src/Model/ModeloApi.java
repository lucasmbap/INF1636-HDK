package Model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * API do Model: unico ponto de acesso as regras do jogo para a View e o
 * Controller.
 *
 * Padroes de design (cap. 17):
 * - Singleton: ha uma unica instancia, obtida com getInstance();
 * - Facade: esconde as classes internas do Model (Partida, Mapa, Jogador,
 *   Cartela...), que nao sao publicas;
 * - Observer: e o objeto observado (ObservadoIF). As janelas se registram
 *   com add() e sao avisadas a cada mudanca de estado.
 */
public final class ModeloApi implements ObservadoIF {
	// constantes para get(int) do Observer
	public static final int COR_DA_VEZ = 1;
	public static final int MOVIMENTOS_RESTANTES = 2;
	public static final int PUNHOS_RESTANTES = 3;
	public static final int KHAN_PENDENTE = 4;
	public static final int POSICAO_KHAN = 5;
	public static final int COLUNA_ATIVADA = 6;

	// colunas da cartela (use nos metodos que recebem "coluna")
	public static final int COLUNA_VERMELHA = 0;
	public static final int COLUNA_VERDE = 1;
	public static final int COLUNA_AMARELA = 2;
	public static final int COLUNA_BRANCA = 3;

	/** Destino do Khan fora do mapa (ver usarKhan). */
	public static final String AREA_DE_MELHORIAS = Mapa.AREA_DE_MELHORIAS;

	private static ModeloApi instancia = null;

	private Map<Integer, Jogador> jogadores;
	private int proximoIdJogador;
	private Mapa mapa;
	private List<Jogador> ordemJogadores; // ordem dos turnos
	private Partida partida; // criada em iniciarPartida()
	private final List<ObservadorIF> observadores = new ArrayList<ObservadorIF>();

	private ModeloApi() {
		limpar();
	}

	/** Singleton: devolve a unica instancia da API. */
	public static ModeloApi getInstance() {
		if (instancia == null)
			instancia = new ModeloApi();
		return instancia;
	}

	/**
	 * Descarta a partida atual (jogadores, mapa e estado) para comecar outra.
	 * Os observadores registrados continuam registrados e sao avisados.
	 */
	public void novaPartida() {
		limpar();
		notificar();
	}

	private void limpar() {
		jogadores = new HashMap<Integer, Jogador>();
		proximoIdJogador = 1;
		mapa = MapaOficial.criar();
		ordemJogadores = new ArrayList<Jogador>();
		partida = null;
	}

	public int criarJogador(String nome, String cor, String herdeiro) {
		Jogador jogador = new Jogador(nome, cor, herdeiro);
		int id = proximoIdJogador;
		int proximoId = Math.incrementExact(proximoIdJogador);
		jogadores.put(id, jogador);
		proximoIdJogador = proximoId;
		if (partida == null)
			ordemJogadores.add(jogador); // entra na partida, na ordem de criacao
		notificar();
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
		notificar();
	}

	public void adicionarTributo(int idJogador, TipoTributo tipo) {
		obterJogador(idJogador).adicionarTributo(new PecaTributo(tipo));
		notificar();
	}

	public void adicionarRecursos(int idJogador, TipoTributo tipo, int quantidade) {
		obterJogador(idJogador).adicionarRecursos(tipo, quantidade);
		notificar();
	}

	public boolean removerRecursos(int idJogador, TipoTributo tipo, int quantidade) {
		return avisarSe(obterJogador(idJogador).removerRecursos(tipo, quantidade));
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
		notificar();
	}

	public List<String> getNomesTesouros(int idJogador) {
		return obterJogador(idJogador).getNomesTesouros();
	}

	public boolean removerTesouro(int idJogador, String nome) {
		Tesouro tesouro = new Tesouro(nome);
		return avisarSe(obterJogador(idJogador).removerTesouro(tesouro));
	}

	public void adicionarCartaConselheiro(int idJogador, String nome, String condicao, String recompensa,
			boolean aberta) {
		obterJogador(idJogador)
				.adicionarCartaConselheiro(new CartaConselheiro(nome, condicao, recompensa, aberta));
		notificar();
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
		notificar();
	}

	public void adicionarMelhoria(int idJogador, String nome, String efeito, String condicao) {
		obterJogador(idJogador).adicionarMelhoria(new PecaMelhoria(nome, efeito, condicao));
		notificar();
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
		notificar();
	}

	public boolean removerVotos(int idJogador, int quantidade) {
		return avisarSe(obterJogador(idJogador).removerVotos(quantidade));
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
		for (Jogador j : ordemJogadores)
			if (Cartela.nomeOficial(j.getHerdeiro()) == null)
				return false; // cada jogador precisa de um herdeiro com cartela
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

	/**
	 * Encerra o turno e passa a vez para o proximo jogador. Retorna false
	 * (e nada muda) se a coluna ativada exigir o uso do Khan e ele ainda nao
	 * tiver sido usado.
	 */
	public boolean passarVez() {
		if (partida == null || partida.isEncerrada() || !partida.podeEncerrarTurno())
			return false;
		partida.encerrarTurno();
		if (!partida.isEncerrada())
			partida.iniciarTurno(0, 0);
		notificar();
		return true;
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

	// =====================================================================
	// Cartela e acoes do turno (3a iteracao)
	// As colunas sao indicadas pelas constantes COLUNA_VERMELHA (0),
	// COLUNA_VERDE (1), COLUNA_AMARELA (2) e COLUNA_BRANCA (3).
	// Icones e melhorias sao devolvidos pelo nome (ex.: "CAVALO", "PUNHO",
	// "KHAN", "MOEDA", "RECUPERAR"; melhorias: "MOEDA", "CAVALOS",
	// "TESOURO_SEDA"...), para a View escolher a imagem.
	// =====================================================================

	/**
	 * O jogador da vez ativa uma coluna da sua cartela: recebe os cavalos
	 * (movimentos), os punhos e os recursos virtuais dela. So uma coluna por
	 * turno; uma coluna ocupada por peca de ativacao nao pode ser escolhida.
	 */
	public boolean ativarColuna(int coluna) {
		ColunaCartela c = coluna(coluna);
		if (partida == null || c == null || !partida.ativarColuna(c))
			return false;
		notificar();
		return true;
	}

	/** Coluna ativada pelo jogador da vez neste turno (0 a 3), ou -1 se ainda nenhuma. */
	public int getColunaAtivada() {
		if (partida == null || partida.getColunaAtivada() == null)
			return -1;
		return partida.getColunaAtivada().ordinal();
	}

	/** A coluna do jogador da cor indicada esta ocupada por uma peca de ativacao? */
	public boolean isColunaOcupada(String cor, int coluna) {
		Cartela ct = cartela(cor);
		ColunaCartela c = coluna(coluna);
		return ct != null && c != null && ct.isOcupada(c);
	}

	/** Pecas de ativacao ainda livres do jogador (de 0 a 4). */
	public int getPecasDeAtivacaoLivres(String cor) {
		Cartela ct = cartela(cor);
		return ct == null ? 0 : ct.getPecasLivres();
	}

	/** Nome do herdeiro da cartela do jogador, ou null. */
	public String getHerdeiroDaCartela(String cor) {
		Cartela ct = cartela(cor);
		return ct == null ? null : ct.getHerdeiro();
	}

	/** Icones da acao base impressa na coluna (ex.: [CAVALO, PUNHO, MOEDA]). */
	public List<String> getAcoesDaColuna(String cor, int coluna) {
		List<String> lst = new ArrayList<String>();
		Cartela ct = cartela(cor);
		ColunaCartela c = coluna(coluna);
		if (ct != null && c != null)
			for (IconeAcao i : ct.getAcoesBase(c))
				lst.add(i.name());
		return lst;
	}

	/** Melhorias da coluna, de baixo para cima ("?" = melhoria sem tipo conhecido). */
	public List<String> getMelhoriasDaColuna(String cor, int coluna) {
		List<String> lst = new ArrayList<String>();
		Cartela ct = cartela(cor);
		ColunaCartela c = coluna(coluna);
		if (ct != null && c != null)
			for (TipoMelhoria t : ct.getMelhorias(c))
				lst.add(t == null ? "?" : t.name());
		return lst;
	}

	/** Melhorias gratis (por linhas completas) que o jogador pode colocar na coluna branca. */
	public int getMelhoriasGratisPendentes(String cor) {
		Cartela ct = cartela(cor);
		return ct == null ? 0 : ct.getMelhoriasGratisPendentes();
	}

	/** Coloca na coluna branca uma melhoria gratis do tipo indicado (ex.: "CAVALOS"). */
	public boolean colocarMelhoriaGratis(String cor, String tipoMelhoria) {
		TipoMelhoria t = TipoMelhoria.porNome(tipoMelhoria);
		if (partida == null || t == null || !partida.colocarMelhoriaGratis(cor, t))
			return false;
		notificar();
		return true;
	}

	/**
	 * O jogador da vez compra uma melhoria por 2 moedas (reais ou virtuais) e
	 * a coloca na coluna indicada (vermelha, verde ou amarela). A escolha da
	 * peca na area de melhorias e a regra da cor ficam com quem chama.
	 */
	public boolean comprarMelhoria(String tipoMelhoria, int coluna) {
		TipoMelhoria t = TipoMelhoria.porNome(tipoMelhoria);
		ColunaCartela c = coluna(coluna);
		if (partida == null || t == null || c == null || c == ColunaCartela.BRANCA)
			return false;
		PecaMelhoria peca = new PecaMelhoria(t.name(), t.getIcones().toString(), "-");
		if (!partida.comprarMelhoria(peca, c.getRegiao()))
			return false;
		notificar();
		return true;
	}

	public int getPunhosRestantes() {
		return partida == null ? 0 : partida.getPunhosRestantes();
	}

	/** Recurso virtual (moeda, espada ou yurt) que ainda resta neste turno. */
	public int getRecursoVirtual(TipoTributo tipo) {
		return partida == null ? 0 : partida.getVirtual(tipo);
	}

	/** Tesouros virtuais (melhorias de tesouro) da coluna ativada neste turno. */
	public List<String> getTesourosVirtuais() {
		if (partida == null)
			return new ArrayList<String>();
		return partida.getTesourosVirtuais();
	}

	/** A coluna ativada tem o Khan e ele ainda nao foi usado neste turno? */
	public boolean isKhanPendente() {
		return partida != null && partida.isKhanPendente();
	}

	/**
	 * Lugares para onde o Khan pode ir agora: as provincias do Khan e a area
	 * de melhorias, menos o lugar onde ele ja esta.
	 */
	public List<String> getDestinosDoKhan() {
		List<String> lst = new ArrayList<String>();
		String atual = mapa.getPosicaoKhan();
		for (String p : mapa.getProvinciasDoKhan())
			if (!p.equals(atual))
				lst.add(p);
		if (!AREA_DE_MELHORIAS.equals(atual))
			lst.add(AREA_DE_MELHORIAS);
		return lst;
	}

	/**
	 * Usa o Khan: leva-o a uma provincia do Khan (que, com as 2 das setas,
	 * recebe tributo) ou a AREA_DE_MELHORIAS. So vale quando a coluna ativada
	 * tem o icone do Khan. A reposicao das melhorias fica com a area de
	 * melhorias.
	 */
	public boolean usarKhan(String destino) {
		if (partida == null || !partida.usarKhan(destino))
			return false;
		notificar();
		return true;
	}

	/** Pega 1 tributo da provincia (precisa de punho e de ter passado ao lado dela). */
	public boolean pegarTributo(String provincia) {
		if (partida == null || !partida.pegarTributo(provincia))
			return false;
		notificar();
		return true;
	}

	/** Ataca a cidade e pega o tesouro indicado (ex.: "seda"): 1 espada, depois 2 na mesma cidade. */
	public boolean atacarCidade(String cidade, String tesouro) {
		if (partida == null || tesouro == null || tesouro.isEmpty())
			return false;
		if (!partida.atacarCidade(cidade, new Tesouro(tesouro)))
			return false;
		notificar();
		return true;
	}

	/** Nomes dos tesouros que ainda estao na cidade. */
	public List<String> getTesourosDaCidade(String cidade) {
		List<String> lst = new ArrayList<String>();
		Cidade c = mapa.getCidade(cidade);
		if (c != null)
			for (Tesouro t : c.getTesouros())
				lst.add(t.getNome());
		return lst;
	}

	/** Constroi um yurt numa parada onde o peao da vez esteve neste turno. */
	public boolean construirYurt(String parada) {
		if (partida == null || !partida.construirYurt(parada))
			return false;
		notificar();
		return true;
	}

	private Cartela cartela(String cor) {
		return partida == null ? null : partida.getCartela(cor);
	}

	private static ColunaCartela coluna(int indice) {
		if (indice < 0 || indice >= ColunaCartela.values().length)
			return null;
		return ColunaCartela.values()[indice];
	}

	// =====================================================================
	// Observer (cap. 17): a ModeloApi e o objeto observado
	// =====================================================================

	public void add(ObservadorIF o) {
		if (o != null && !observadores.contains(o))
			observadores.add(o);
	}

	public void remove(ObservadorIF o) {
		observadores.remove(o);
	}

	/**
	 * Callback do Observer: o observador, ao ser notificado, pede aqui a
	 * informacao que precisa (COR_DA_VEZ, MOVIMENTOS_RESTANTES,
	 * PUNHOS_RESTANTES, KHAN_PENDENTE, POSICAO_KHAN ou COLUNA_ATIVADA).
	 */
	public Object get(int i) {
		switch (i) {
		case COR_DA_VEZ:
			return getCorDaVez();
		case MOVIMENTOS_RESTANTES:
			return getMovimentosRestantes();
		case PUNHOS_RESTANTES:
			return getPunhosRestantes();
		case KHAN_PENDENTE:
			return isKhanPendente();
		case POSICAO_KHAN:
			return getPosicaoKhan();
		case COLUNA_ATIVADA:
			return getColunaAtivada();
		default:
			return null;
		}
	}

	private void notificar() {
		for (ObservadorIF o : new ArrayList<ObservadorIF>(observadores))
			o.notify(this);
	}

	private boolean avisarSe(boolean mudou) {
		if (mudou)
			notificar();
		return mudou;
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