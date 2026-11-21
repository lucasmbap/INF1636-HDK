package Model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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

	// area de melhorias: espacos 0 (esquerda) a 4; TOPO_DA_PILHA = peca aleatoria do topo
	public static final int ESPACOS_DA_AREA = AreaDeMelhorias.ESPACOS;
	public static final int TOPO_DA_PILHA = -1;
	public static final int ESPACO_ICONE_MOEDA = AreaDeMelhorias.ESPACO_ICONE_MOEDA;
	public static final int ESPACO_ICONE_ESPADA = AreaDeMelhorias.ESPACO_ICONE_ESPADA;

	private static ModeloApi instancia = null;

	private Map<Integer, Jogador> jogadores;
	private int proximoIdJogador;
	private Mapa mapa;
	private List<Jogador> ordemJogadores; // ordem dos turnos
	private Partida partida; // criada em iniciarPartida()
	private boolean influenciaPontuada;
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
		influenciaPontuada = false;
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
		List<String> pecasKarakorum = new ArrayList<String>(PecaKarakorum.TIPOS_TESOURO);
		Collections.shuffle(pecasKarakorum);
		if (!mapa.configurarPecaKarakorum(new PecaKarakorum(pecasKarakorum.subList(0, 3)))
				|| mapa.getRequisitosKarakorum().size() != 3)
			return false;
		Deque<Tesouro> pilha = new ArrayDeque<Tesouro>();
		for (int i = 0; i < 8; i++)
			for (String t : PecaKarakorum.TIPOS_TESOURO)
				pilha.add(new Tesouro(t));
		Deque<String> fechadas = new ArrayDeque<String>();
		// as cidades sao sorteadas: as 3 primeiras ja comecam reveladas, as outras ficam guardadas (manual, pag. 4)
		List<String> sorteio = new ArrayList<String>(java.util.Arrays.asList(ORDEM_CIDADES));
		java.util.Collections.shuffle(sorteio);
		for (int i = 0; i < ORDEM_CIDADES.length; i++) {
			if (i < CIDADES_INICIAIS) {
				List<Tesouro> tesouros = new ArrayList<Tesouro>();
				for (int k = 0; k < TESOUROS_POR_CIDADE; k++)
					tesouros.add(pilha.poll());
				mapa.revelarCidade(sorteio.get(i), tesouros);
			} else {
				fechadas.add(sorteio.get(i));
			}
		}
		for (String prov : mapa.getNomesProvincias())
			mapa.colocarTributo(prov);
		// Preparacao oficial (manual, pagina 3): 1 moeda para os dois primeiros
		// jogadores e 2 moedas para os demais.
		for (int i = 0; i < ordemJogadores.size(); i++)
			ordemJogadores.get(i).adicionarRecursos(TipoTributo.MOEDA, i < 2 ? 1 : 2);
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
		partida.verificarCartasAbertas();
		partida.encerrarTurno();
		if (!partida.isEncerrada()) {
			partida.iniciarTurno(0, 0);
		} else {
			partida.pontuarInfluencia();
			influenciaPontuada = true;
		}
		notificar();
		return true;
	}

	/** Indica se a ultima rodada terminou e o vencedor pode ser apurado. */
	public boolean isPartidaEncerrada() {
		return partida != null && partida.isEncerrada();
	}

	/** Cores dos vencedores, apos o fim da partida; lista vazia antes do fim. */
	public List<String> getVencedores() {
		if (!isPartidaEncerrada() || !influenciaPontuada)
			return new ArrayList<String>();
		return new ArrayList<String>(partida.vencedores());
	}

	/** Paradas onde o jogador da vez esteve neste turno. */
	public List<String> getParadasVisitadasNoTurno() {
		if (partida == null)
			return new ArrayList<String>();
		return partida.getParadasVisitadas();
	}

	/** Conselheiros visitados durante este turno, nos quais o jogador pode entregar tesouros. */
	public List<String> getConselheirosVisitadosNoTurno() {
		if (partida == null)
			return new ArrayList<String>();
		return partida.getConselheirosVisitados();
	}

	/** Requisitos de entrega do conselheiro; "*" representa qualquer tesouro. */
	public List<String> getRequisitosConselheiro(String parada) {
		if (partida == null)
			return new ArrayList<String>();
		return partida.getRequisitosConselheiro(parada);
	}

	/** Tesouros exigidos pela peca de Karakorum sorteada para esta partida. */
	public List<String> getTesourosExigidosEmKarakorum() {
		return mapa.getRequisitosKarakorum();
	}

	/** Entrega tesouros ao conselheiro visitado e aplica votos e bonus. */
	public boolean entregarTesourosConselheiro(String parada, List<String> tesouros) {
		if (partida == null || !partida.entregarTesourosConselheiro(parada, tesouros))
			return false;
		notificar();
		return true;
	}

	/** Cartas de conselheiro aberto ainda na mesa (ate 4): ids como "6_yurts", "2_cidades"... */
	public List<String> getCartasAbertasNaMesa() {
		return partida == null ? new ArrayList<String>() : partida.getCartasAbertas();
	}

	/** A peca de bonus deste conselheiro ainda esta no tabuleiro (ninguem entregou tesouros a ele)? */
	public boolean isBonusDisponivel(String parada) {
		return partida != null && mapa.temConselheiro(parada) && !partida.getConselheirosComBonus().contains(parada);
	}

	/** Tipo da peca de bonus sorteada para o conselheiro (CADEIRA, ESPADAS etc.). */
	public String getTipoBonus(String parada) {
		return partida == null ? null : partida.getTipoBonus(parada);
	}

	/** Tesouros revelados pelo bonus do bau, aguardando o jogador escolher um. */
	public List<String> getTesourosBonusPendentes() {
		return partida == null ? new ArrayList<String>() : partida.getTesourosBonusPendentes();
	}

	/** Mantem um dos tesouros revelados pelo bonus do bau e descarta os demais. */
	public boolean escolherTesouroBonus(String tesouro) {
		if (partida == null || !partida.escolherTesouroBonus(tesouro))
			return false;
		notificar();
		return true;
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

	/**
	 * Coloca na coluna branca a melhoria gratis (linha completa): a peca de um
	 * espaco da area (0 a 4) ou a do topo da pilha (TOPO_DA_PILHA). Sem regra de cor.
	 */
	public boolean colocarMelhoriaGratis(String cor, int espaco) {
		if (partida == null || !partida.colocarMelhoriaGratis(cor, espaco))
			return false;
		notificar();
		return true;
	}

	/**
	 * O jogador da vez compra por 2 moedas (reais ou virtuais) a melhoria de um
	 * espaco da area (0 a 4) ou a do topo da pilha (TOPO_DA_PILHA) e a coloca na
	 * coluna indicada (vermelha, verde ou amarela), que tem de ser da cor da
	 * peca (o Jochi pode escolher qualquer uma). As pecas que sobram na area
	 * andam para a direita.
	 */
	public boolean comprarMelhoria(int espaco, int coluna) {
		ColunaCartela c = coluna(coluna);
		if (partida == null || c == null || c == ColunaCartela.BRANCA || !partida.comprarMelhoria(espaco, c))
			return false;
		notificar();
		return true;
	}

	/** Compra a melhoria e a coloca na coluna da cor dela. */
	public boolean comprarMelhoria(int espaco) {
		if (partida == null || !partida.comprarMelhoria(espaco, null))
			return false;
		notificar();
		return true;
	}

	/** Tipo da melhoria no espaco da area (ex.: "MOEDA", "TESOURO_SEDA"), ou null se vazio. */
	public String getTipoMelhoriaNaArea(int espaco) {
		MelhoriaColorida m = partida == null ? null : partida.getArea().getEspaco(espaco);
		return m == null ? null : m.getTipo().name();
	}

	/** Cor (coluna 0, 1 ou 2) da melhoria no espaco da area, ou -1 se vazio. */
	public int getCorMelhoriaNaArea(int espaco) {
		MelhoriaColorida m = partida == null ? null : partida.getArea().getEspaco(espaco);
		return m == null ? -1 : m.getCor().ordinal();
	}

	/** Pecas de melhoria que restam viradas para baixo na pilha. */
	public int getMelhoriasNaPilha() {
		return partida == null ? 0 : partida.getArea().getTamanhoPilha();
	}

	// =====================================================================
	// Habilidades especiais dos herdeiros (texto impresso nas cartelas)
	// =====================================================================

	/** Altani: parada do 2o peao, ou null para os outros herdeiros. */
	public String getPosicaoSegundoPeao(String cor) {
		return cor == null ? null : mapa.getPosicao(cor + Mapa.SUFIXO_SEGUNDO_PEAO);
	}

	/** Altani: peao (1 ou 2) que joga neste turno; 0 para os outros herdeiros. */
	public int getPeaoEscolhido(String cor) {
		return partida == null ? 0 : partida.getPeaoEscolhido(cor);
	}

	/** Parada do peao que joga neste turno (para a Altani, o peao escolhido). */
	public String getPosicaoPeaoDaVez() {
		return partida == null ? null : mapa.getPosicao(partida.peaoDaVez());
	}

	/** Altani: escolhe o peao (1 ou 2) que move e faz as acoes. So antes de ativar a coluna. */
	public boolean escolherPeao(int numero) {
		if (partida == null || !partida.escolherPeao(numero))
			return false;
		notificar();
		return true;
	}

	/** Chagatai: 1x por turno, troca 1 movimento por 1 peca de espada. */
	public boolean trocarCavaloPorEspada() {
		if (partida == null || !partida.trocarCavaloPorEspada())
			return false;
		notificar();
		return true;
	}

	/** Ogedei: 1x por turno, paga 1 moeda e pega um tesouro de qualquer cidade (nao o ultimo dela). */
	public boolean pegarTesouroComMoeda(String cidade, String tesouro) {
		if (partida == null || tesouro == null || !partida.pegarTesouroComMoeda(cidade, new Tesouro(tesouro)))
			return false;
		notificar();
		return true;
	}

	/** Chagatai e Ogedei: a habilidade (1x por turno) ja foi usada neste turno? */
	public boolean isHabilidadeUsadaNoTurno() {
		return partida != null && partida.isHabilidadeUsadaNoTurno();
	}

	/** Jochi: tipos das 2 melhorias iniciais ainda nao escolhidas (vazia depois da escolha). */
	public List<String> getMelhoriasIniciaisJochi(String cor) {
		List<String> lst = new ArrayList<String>();
		if (partida != null)
			for (MelhoriaColorida m : partida.getMelhoriasIniciaisJochi(cor))
				lst.add(m.getTipo().name());
		return lst;
	}

	/** Jochi: poe na coluna branca a melhoria inicial de indice 0 ou 1; a outra volta para a pilha. */
	public boolean escolherMelhoriaInicial(String cor, int indice) {
		if (partida == null || !partida.escolherMelhoriaInicial(cor, indice))
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

	// =====================================================================
	// Salvar e recuperar a partida (4a iteracao)
	// A View escolhe o arquivo com JFileChooser e chama estes metodos.
	// O arquivo e texto ASCII puro (ver Persistencia).
	// =====================================================================

	/** O jogador da vez ainda nao comecou a jogar? So nesse momento a partida pode ser salva. */
	public boolean podeSalvar() {
		return partida != null && !partida.isEncerrada() && partida.getColunaAtivada() == null;
	}

	/**
	 * Grava o estado completo da partida em um arquivo .txt (ASCII). Lanca
	 * IOException se nao houver partida em andamento ou se o arquivo nao puder ser escrito.
	 */
	public void salvar(File arquivo) throws IOException {
		if (arquivo == null)
			throw new IOException("nenhum arquivo foi indicado.");
		Persistencia.Estado e = new Persistencia.Estado();
		e.jogadores = jogadores;
		e.proximoIdJogador = proximoIdJogador;
		e.mapa = mapa;
		e.ordemJogadores = ordemJogadores;
		e.partida = partida;
		e.influenciaPontuada = influenciaPontuada;
		Persistencia.gravar(e, arquivo.toPath());
	}

	/**
	 * Le um arquivo salvo e troca a partida atual por ele (os observadores continuam
	 * registrados e sao avisados). Se o arquivo for invalido, lanca IOException e a
	 * partida atual nao muda.
	 */
	public void recuperar(File arquivo) throws IOException {
		if (arquivo == null)
			throw new IOException("nenhum arquivo foi indicado.");
		Persistencia.Estado e = Persistencia.ler(arquivo.toPath());
		jogadores = e.jogadores;
		proximoIdJogador = e.proximoIdJogador;
		mapa = e.mapa;
		ordemJogadores = e.ordemJogadores;
		partida = e.partida;
		influenciaPontuada = e.influenciaPontuada;
		notificar();
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
		if (partida != null)
			partida.verificarCartasAbertas(); // carta de conselheiro aberto cumprida = 1 voto
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
