package Model;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regras da partida: turno, movimento, tributos, ataque a cidades, yurts,
 * melhorias, votos, fim de jogo, influencia nas regioes e vencedor.
 * Usa Mapa (parte 1) e Jogador (parte 2). Nao e publica: a API para
 * View/Controller (Singleton + Facade) fica para a 3a iteracao.
 */
class Partida {
	static final int MAX_YURTS = 12;
	static final int CUSTO_MELHORIA = 2;
	static final int MAX_MELHORIAS_POR_COLUNA = 4;

	// herdeiros com habilidade especial (texto impresso em cada cartela)
	static final String ALTANI = "Altani";
	static final String CHAGATAI = "Chagatai";
	static final String JOCHI = "Jochi";
	static final String OGEDEI = "Ogedei";
	static final String TOLUI = "Tolui";

	// indice = numero de jogadores (2 a 5): 10 / 14 / 16 / 17 votos
	private static final int[] LIMITE_VOTOS = { 0, 0, 10, 14, 16, 17 };
	// indice = numero de melhorias na coluna da regiao (0 a 4)
	private static final int[] VOTOS_MAIS_YURTS = { 1, 1, 2, 3, 4 };
	private static final int[] VOTOS_SEGUNDO = { 0, 1, 1, 2, 2 };

	private final Mapa mapa;
	private final Map<String, Jogador> jogadores = new HashMap<String, Jogador>();
	private final List<String> ordem = new ArrayList<String>();
	private final Set<String> conselheirosComBonus = new HashSet<String>();
	private final Map<String, String> tipoBonusPorConselheiro = new HashMap<String, String>();
	private final List<String> tesourosBonusPendentes = new ArrayList<String>();
	private static final String[] PARADAS_COM_BONUS = { "R2", "R11", "C5", "C10", "P1", "P8", Mapa.KARAKORUM };
	private static final String[] TIPOS_BONUS = { "CADEIRA", "ESPADAS", "MOEDAS", "YURTS", "CAVALOS", "MAOS", "BAU" };
	/** As 14 cartas de conselheiro aberto do manual (pag. 18); 4 sorteadas ficam na mesa em cada partida. */
	static final String[] TODAS_CARTAS_ABERTAS = { "4_espadas", "6_yurts", "10_yurts", "4_tesouros",
			"5_tesouros_diferentes", "1_yurt_por_regiao", "2_yurts_por_regiao", "2_bonus", "3_bonus", "2_cidades",
			"3_cidades", "linha_completa", "4_melhorias_coluna", "2_colunas_2_melhorias" };
	private List<String> cartasAbertas = new ArrayList<String>();
	private Map<String, Integer> bonusPorJogador = new HashMap<String, Integer>();
	private final Deque<String> cidadesFechadas;
	private final Deque<Tesouro> pilhaTesouros;

	private int indiceAtual = 0;
	private int votosNeutra = 0;
	private final int limiteVotos;
	private boolean fimDisparado = false;
	private boolean ultimaRodada = false;
	private boolean encerrada = false;
	private boolean pontuada = false;

	// estado do turno atual
	private boolean turnoAtivo = false;
	private int movimentos;
	private int punhos;
	private List<String> visitadas = new ArrayList<String>();
	private Map<String, Integer> tesourosTomadosNoTurno = new HashMap<String, Integer>();

	// cartelas e estado do turno ligado a elas (3a iteracao)
	private final Map<String, Cartela> cartelas = new HashMap<String, Cartela>();
	private ColunaCartela colunaAtivada = null;
	private boolean khanPendente = false;
	private Map<TipoTributo, Integer> virtuais = new HashMap<TipoTributo, Integer>();
	private List<String> tesourosVirtuais = new ArrayList<String>();

	// area de melhorias e habilidades dos herdeiros (4a iteracao)
	private AreaDeMelhorias area;
	private boolean habilidadeUsadaNoTurno = false; // Chagatai e Ogedei: 1x por turno
	private final Map<String, Integer> peaoEscolhido = new HashMap<String, Integer>(); // Altani: 1 ou 2
	private final Map<String, List<MelhoriaColorida>> melhoriasIniciaisJochi = new HashMap<String, List<MelhoriaColorida>>();

	/**
	 * @param jogadoresEmOrdem  ordem de jogo (o primeiro e quem comeca)
	 * @param cidadesFechadas   cidades ainda nao rereveladas, na ordem da pilha
	 * @param pilhaTesouros     tesouros disponiveis para revelar cidades
	 */
	Partida(Mapa mapa, List<Jogador> jogadoresEmOrdem, Deque<String> cidadesFechadas,
			Deque<Tesouro> pilhaTesouros) {
		if (mapa == null || jogadoresEmOrdem == null || cidadesFechadas == null || pilhaTesouros == null)
			throw new IllegalArgumentException("Argumentos obrigatorios nao podem ser nulos.");
		int n = jogadoresEmOrdem.size();
		if (n < 2 || n > 5)
			throw new IllegalArgumentException("A partida e para 2 a 5 jogadores.");
		this.mapa = mapa;
		this.cidadesFechadas = cidadesFechadas;
		this.pilhaTesouros = pilhaTesouros;
		for (Jogador j : jogadoresEmOrdem) {
			if (jogadores.containsKey(j.getCor()))
				throw new IllegalArgumentException("Cor repetida: " + j.getCor());
			jogadores.put(j.getCor(), j);
			ordem.add(j.getCor());
			cartelas.put(j.getCor(), new Cartela(j.getHerdeiro()));
			mapa.colocarPeao(j.getCor(), Mapa.KARAKORUM);
		}
		this.limiteVotos = LIMITE_VOTOS[n];
		List<String> todas = new ArrayList<String>(java.util.Arrays.asList(TODAS_CARTAS_ABERTAS));
		java.util.Collections.shuffle(todas);
		this.cartasAbertas = new ArrayList<String>(todas.subList(0, 4));
		this.area = AreaDeMelhorias.nova();
		prepararBonus();
		prepararHabilidades();
	}

	/**
	 * Preparacao que depende do herdeiro (texto das cartelas):
	 * - Altani: um segundo peao, tambem em Karakorum;
	 * - Tolui: 1 yurt em Karakorum, que nao pertence a nenhuma regiao;
	 * - Jochi: tira 2 melhorias da pilha e depois escolhe 1 para a coluna branca.
	 */
	/** Distribui aleatoriamente as sete pecas de bonus sobre os sete conselheiros. */
	private void prepararBonus() {
		List<String> tipos = new ArrayList<String>(java.util.Arrays.asList(TIPOS_BONUS));
		java.util.Collections.shuffle(tipos);
		for (int i = 0; i < PARADAS_COM_BONUS.length; i++)
			tipoBonusPorConselheiro.put(PARADAS_COM_BONUS[i], tipos.get(i));
	}

	private void prepararHabilidades() {
		for (String cor : ordem) {
			String h = herdeiro(cor);
			if (h.equals(ALTANI)) {
				mapa.colocarPeao(cor + Mapa.SUFIXO_SEGUNDO_PEAO, Mapa.KARAKORUM);
				peaoEscolhido.put(cor, 1);
			} else if (h.equals(TOLUI)) {
				mapa.colocarYurtEmKarakorum(cor);
			} else if (h.equals(JOCHI)) {
				List<MelhoriaColorida> duas = new ArrayList<MelhoriaColorida>();
				for (int i = 0; i < 2; i++) {
					MelhoriaColorida m = area.retirar(-1);
					if (m != null)
						duas.add(m);
				}
				melhoriasIniciaisJochi.put(cor, duas);
			}
		}
	}

	/** Herdeiro do jogador (nome oficial da cartela). */
	String herdeiro(String cor) {
		Cartela c = cartelas.get(cor);
		return c == null ? "" : c.getHerdeiro();
	}

	/** Chave do peao que joga neste turno: a cor, ou a cor + "#2" se a Altani escolheu o 2o peao. */
	String peaoDaVez() {
		String cor = getCorAtual();
		Integer n = peaoEscolhido.get(cor);
		return (n != null && n == 2) ? cor + Mapa.SUFIXO_SEGUNDO_PEAO : cor;
	}

	// =====================================================================
	// Consultas
	// =====================================================================

	String getCorAtual() {
		return ordem.get(indiceAtual);
	}

	Jogador getJogador(String cor) {
		return jogadores.get(cor);
	}

	int getMovimentosRestantes() {
		return movimentos;
	}

	int getPunhosRestantes() {
		return punhos;
	}

	int getVotosNeutra() {
		return votosNeutra;
	}

	boolean isFimDisparado() {
		return fimDisparado;
	}

	boolean isEncerrada() {
		return encerrada;
	}

	int getMelhoriasNaColuna(String cor, Regiao coluna) {
		return cartelas.get(cor).getNumMelhorias(ColunaCartela.daRegiao(coluna));
	}

	private Jogador jogadorAtual() {
		return jogadores.get(getCorAtual());
	}

	private boolean podeAgir() {
		return turnoAtivo && !encerrada;
	}

	// =====================================================================
	// Turno
	// =====================================================================

	/** Comeca o turno do jogador atual com os movimentos e punhos da coluna escolhida. */
	void iniciarTurno(int movimentos, int punhos) {
		if (encerrada)
			throw new IllegalStateException("A partida ja terminou.");
		if (movimentos < 0 || punhos < 0)
			throw new IllegalArgumentException("Movimentos e punhos nao podem ser negativos.");
		this.movimentos = movimentos;
		this.punhos = punhos;
		this.turnoAtivo = true;
		this.visitadas = new ArrayList<String>();
		this.visitadas.add(mapa.getPosicao(peaoDaVez()));
		this.tesourosTomadosNoTurno = new HashMap<String, Integer>();
		this.colunaAtivada = null;
		this.khanPendente = false;
		this.virtuais = new HashMap<TipoTributo, Integer>();
		this.tesourosVirtuais = new ArrayList<String>();
	}

	/**
	 * Passa a vez. Quando a rodada fecha: se o fim ja foi disparado, a
	 * proxima e a rodada extra; terminada a rodada extra, o jogo acaba.
	 */
	void encerrarTurno() {
		cartelas.get(getCorAtual()).fimDoTurno();
		turnoAtivo = false;
		habilidadeUsadaNoTurno = false;
		verificarFimPorCidades();
		indiceAtual++;
		if (indiceAtual == ordem.size()) {
			indiceAtual = 0;
			if (fimDisparado) {
				if (ultimaRodada)
					encerrada = true;
				else
					ultimaRodada = true;
			}
		}
	}

	private void verificarFimPorCidades() {
		if (mapa.todasAsCidadesConquistadas())
			fimDisparado = true;
	}

	// =====================================================================
	// Acoes
	// =====================================================================

	/** Move o peao do jogador atual. O caminho nao inclui a parada de origem. */
	boolean mover(List<String> caminho) {
		if (!podeAgir())
			return false;
		String peao = peaoDaVez();
		int custo = mapa.custoDoCaminho(peao, caminho);
		if (custo < 0 || custo > movimentos)
			return false;
		List<String> novas = mapa.getParadasVisitadas(peao, caminho); // antes de mover
		mapa.moverPeao(peao, caminho, movimentos);
		movimentos -= custo;
		for (String p : novas)
			if (!visitadas.contains(p))
				visitadas.add(p);
		return true;
	}

	// ----- 2a iteracao: o jogador clica so na parada de destino -----

	/**
	 * Move o peao do jogador atual ate a parada clicada, pelo caminho mais
	 * barato (Mapa.melhorCaminho). Se nao for possivel, nada muda.
	 */
	boolean moverPara(String destino) {
		if (!podeAgir())
			return false;
		List<String> caminho = mapa.melhorCaminho(peaoDaVez(), destino);
		if (caminho.isEmpty())
			return false;
		return mover(caminho);
	}

	/** Paradas onde o jogador atual esteve de fato neste turno (copia). */
	List<String> getParadasVisitadas() {
		return new ArrayList<String>(visitadas);
	}

	List<String> getConselheirosVisitados() {
		List<String> resultado = new ArrayList<String>();
		for (String parada : visitadas)
			if (mapa.temConselheiro(parada))
				resultado.add(parada);
		return resultado;
	}

	List<String> getRequisitosConselheiro(String parada) {
		if (!mapa.temConselheiro(parada))
			return new ArrayList<String>();
		switch (parada) {
		case "R2":
			return List.of("seda", "seda");
		case "R11":
			return List.of("ferro", "ferro");
		case "C5":
			return List.of("gado", "gado");
		case "C10":
			return List.of("*", "*", "*");
		case "P1":
			return List.of("pele", "pele");
		case "P8":
			return List.of("especiaria", "especiaria");
		case Mapa.KARAKORUM:
			return mapa.getRequisitosKarakorum();
		default:
			return new ArrayList<String>();
		}
	}

	boolean entregarTesourosConselheiro(String parada, List<String> tesouros) {
		if (!podeAgir() || parada == null || tesouros == null || !visitadas.contains(parada))
			return false;
		List<String> requisitos = getRequisitosConselheiro(parada);
		if (requisitos.isEmpty() || tesouros.size() != requisitos.size())
			return false;
		if (requisitos.contains("*")) {
			for (String tipo : tesouros)
				if (tipo == null || tipo.isBlank())
					return false;
		} else if (!requisitos.equals(tesouros)) {
			return false;
		}

		Jogador jogador = jogadorAtual();
		List<String> disponiveis = new ArrayList<String>(jogador.getNomesTesouros());
		for (String tesouro : tesouros)
			if (tesouro == null || !disponiveis.remove(tesouro))
				return false;
		for (String tesouro : tesouros)
			jogador.removerTesouro(new Tesouro(tesouro));
		int votos = "Karakorum".equals(parada) ? 2 : 1;
		for (int i = 0; i < votos; i++)
			ganharVoto(getCorAtual());
		if (conselheirosComBonus.add(parada)) {
			aplicarBonus(tipoBonusPorConselheiro.get(parada));
			Integer antes = bonusPorJogador.get(getCorAtual());
			bonusPorJogador.put(getCorAtual(), antes == null ? 1 : antes + 1);
		}
		return true;
	}

	/** Aplica o efeito da peca recebida na primeira entrega ao conselheiro. */
	private void aplicarBonus(String tipo) {
		if (tipo == null)
			return;
		Jogador jogador = jogadorAtual();
		switch (tipo) {
		case "CADEIRA":
			ganharVoto(getCorAtual());
			break;
		case "ESPADAS":
			jogador.adicionarRecursos(TipoTributo.ESPADA, 2);
			break;
		case "MOEDAS":
			jogador.adicionarRecursos(TipoTributo.MOEDA, 2);
			break;
		case "YURTS":
			jogador.adicionarRecursos(TipoTributo.YURT, 2);
			break;
		case "CAVALOS":
			movimentos += 3;
			punhos += 1;
			break;
		case "MAOS":
			punhos += 2;
			break;
		case "BAU":
			tesourosBonusPendentes.clear();
			for (int i = 0; i < 3 && !pilhaTesouros.isEmpty(); i++)
				tesourosBonusPendentes.add(pilhaTesouros.removeFirst().getNome());
			break;
		default:
			throw new IllegalStateException("Tipo de bonus desconhecido: " + tipo);
		}
	}

	String getTipoBonus(String parada) {
		return tipoBonusPorConselheiro.get(parada);
	}

	Map<String, String> getTiposBonusConselheiros() {
		return new HashMap<String, String>(tipoBonusPorConselheiro);
	}

	void restaurarTiposBonus(Map<String, String> tipos) {
		if (tipos == null || tipos.isEmpty())
			return; // compatibilidade com arquivos salvos antes da distribuicao dos bonus
		if (tipos.size() != PARADAS_COM_BONUS.length)
			throw new IllegalArgumentException("distribuicao de bonus invalida");
		Set<String> valores = new HashSet<String>(tipos.values());
		if (valores.size() != TIPOS_BONUS.length || !valores.containsAll(java.util.Arrays.asList(TIPOS_BONUS)))
			throw new IllegalArgumentException("tipos de bonus invalidos");
		for (String parada : PARADAS_COM_BONUS)
			if (!tipos.containsKey(parada))
				throw new IllegalArgumentException("falta bonus para " + parada);
		tipoBonusPorConselheiro.clear();
		tipoBonusPorConselheiro.putAll(tipos);
	}

	List<String> getTesourosBonusPendentes() {
		return new ArrayList<String>(tesourosBonusPendentes);
	}

	boolean escolherTesouroBonus(String nome) {
		if (nome == null || !tesourosBonusPendentes.remove(nome))
			return false;
		jogadorAtual().adicionarTesouro(new Tesouro(nome));
		tesourosBonusPendentes.clear(); // os outros dois sao descartados
		return true;
	}

	/** Cores dos jogadores na ordem dos turnos (copia). */
	List<String> getCores() {
		return new ArrayList<String>(ordem);
	}

	boolean isTurnoAtivo() {
		return turnoAtivo;
	}

	/** Pega 1 tributo de uma provincia adjacente a uma parada onde o peao esteve de fato. */
	boolean pegarTributo(String provincia) {
		if (!podeAgir() || punhos <= 0)
			return false;
		TipoTributo tipo = mapa.pegarTributo(provincia, visitadas);
		if (tipo == null)
			return false;
		jogadorAtual().adicionarRecursos(tipo, 1);
		punhos--;
		return true;
	}

	/**
	 * Ataca uma cidade: 1 espada pelo primeiro tesouro dela no turno,
	 * 2 espadas por cada tesouro seguinte da mesma cidade.
	 */
	boolean atacarCidade(String cidade, Tesouro tesouro) {
		if (!podeAgir() || tesouro == null)
			return false;
		Jogador j = jogadorAtual();
		int jaTomados = tesourosTomadosNoTurno.getOrDefault(cidade, 0);
		int custo = (jaTomados == 0) ? 1 : 2;
		if (disponivel(TipoTributo.ESPADA) < custo)
			return false;
		if (!mapa.retirarTesouroDeCidade(cidade, tesouro, getCorAtual(), visitadas))
			return false;
		gastar(TipoTributo.ESPADA, custo);
		j.adicionarTesouro(tesouro);
		tesourosTomadosNoTurno.put(cidade, jaTomados + 1);
		if (mapa.getCidade(cidade).isConquistada()) {
			revelarProximaCidade();
			verificarFimPorCidades();
		}
		return true;
	}

	/** Conquistada uma cidade, revela a proxima da pilha com 4 tesouros (se houver). */
	private void revelarProximaCidade() {
		if (cidadesFechadas.isEmpty() || pilhaTesouros.size() < 4)
			return;
		String proxima = cidadesFechadas.poll();
		List<Tesouro> novos = new ArrayList<Tesouro>();
		for (int i = 0; i < 4; i++)
			novos.add(pilhaTesouros.poll());
		mapa.revelarCidade(proxima, novos);
	}

	/**
	 * Constroi um yurt numa parada onde o peao esteve de fato neste turno. O
	 * Tolui tambem pode construir nas paradas adjacentes ao peao dele.
	 */
	boolean construirYurt(String parada) {
		if (!podeAgir() || parada == null)
			return false;
		boolean aoLado = herdeiro(getCorAtual()).equals(TOLUI)
				&& mapa.saoAdjacentes(mapa.getPosicao(peaoDaVez()), parada);
		if (!visitadas.contains(parada) && !aoLado)
			return false;
		String cor = getCorAtual();
		Jogador j = jogadorAtual();
		if (disponivel(TipoTributo.YURT) < 1)
			return false;
		if (mapa.contarYurtsNoMapa(cor) >= MAX_YURTS)
			return false;
		if (!mapa.construirYurt(cor, parada))
			return false;
		gastar(TipoTributo.YURT, 1);
		return true;
	}

	/**
	 * Compra por 2 moedas (reais ou virtuais) a melhoria de um espaco da area
	 * (0 a 4) ou a do topo da pilha (espaco -1) e a coloca na coluna indicada
	 * da cartela (manual, pag. 11). A coluna tem de ser da cor da peca, menos
	 * para o Jochi. Com coluna null a peca vai para a coluna da sua cor.
	 */
	boolean comprarMelhoria(int espaco, ColunaCartela coluna) {
		if (encerrada)
			return false;
		MelhoriaColorida m = espaco < 0 ? area.verTopo() : area.getEspaco(espaco);
		if (m == null)
			return false;
		ColunaCartela col = coluna == null ? m.getCor() : coluna;
		if (col == ColunaCartela.BRANCA)
			return false;
		if (col != m.getCor() && !herdeiro(getCorAtual()).equals(JOCHI))
			return false; // Jochi: nao precisa seguir a cor das pecas
		Cartela cartela = cartelas.get(getCorAtual());
		if (cartela.getNumMelhorias(col) >= MAX_MELHORIAS_POR_COLUNA)
			return false;
		if (disponivel(TipoTributo.MOEDA) < CUSTO_MELHORIA)
			return false;
		gastar(TipoTributo.MOEDA, CUSTO_MELHORIA);
		area.retirar(espaco);
		TipoMelhoria t = m.getTipo();
		jogadorAtual().adicionarMelhoria(new PecaMelhoria(t.name(), t.getIcones().toString(), m.getCor().name()));
		cartela.adicionarMelhoria(col, t);
		return true;
	}

	/** Todo voto avanca a ficha do jogador e a ficha neutra; pode disparar o fim do jogo. */
	void ganharVoto(String cor) {
		Jogador j = jogadores.get(cor);
		if (j == null)
			throw new IllegalArgumentException("Jogador inexistente: " + cor);
		j.adicionarVotos(1);
		votosNeutra++;
		if (votosNeutra >= limiteVotos)
			fimDisparado = true;
	}

	// =====================================================================
	// Cartela e Khan (3a iteracao)
	// =====================================================================

	Cartela getCartela(String cor) {
		return cartelas.get(cor);
	}

	/** Coluna ativada pelo jogador da vez neste turno, ou null. */
	ColunaCartela getColunaAtivada() {
		return colunaAtivada;
	}

	boolean isKhanPendente() {
		return khanPendente;
	}

	/** Recurso virtual (moeda, espada ou yurt) que ainda resta neste turno. */
	int getVirtual(TipoTributo t) {
		return virtuais.containsKey(t) ? virtuais.get(t) : 0;
	}

	List<String> getTesourosVirtuais() {
		return new ArrayList<String>(tesourosVirtuais);
	}

	/**
	 * Comeca o turno do jogador da vez ativando uma coluna da cartela dele:
	 * os cavalos viram movimentos, os punhos viram acoes de pegar tributo e
	 * os icones de moeda, espada, yurt e tesouro viram recursos virtuais.
	 * So uma coluna por turno; uma coluna ocupada nao pode ser ativada.
	 */
	boolean ativarColuna(ColunaCartela coluna) {
		if (encerrada || coluna == null || colunaAtivada != null)
			return false;
		Cartela cartela = cartelas.get(getCorAtual());
		if (cartela.isOcupada(coluna))
			return false;
		AtivacaoColuna a = cartela.ativar(coluna);
		iniciarTurno(a.getCavalos(), a.getPunhos());
		colunaAtivada = coluna;
		khanPendente = a.temKhan();
		virtuais = a.getVirtuais();
		tesourosVirtuais = a.getTesourosVirtuais();
		return true;
	}

	/**
	 * Usa o Khan (acao obrigatoria quando a coluna ativada tem o icone dele):
	 * leva-o a uma provincia do Khan ou a area de melhorias (Mapa.AREA_DE_MELHORIAS).
	 * Ele nao pode ficar onde ja esta.
	 */
	boolean usarKhan(String destino) {
		if (!podeAgir() || !khanPendente || destino == null)
			return false;
		boolean ok;
		if (destino.equals(Mapa.AREA_DE_MELHORIAS)) {
			ok = mapa.moverKhanParaAreaDeMelhorias();
			if (ok)
				reporAreaDeMelhorias();
		} else {
			ok = mapa.moverKhanParaProvincia(destino);
		}
		if (ok)
			khanPendente = false;
		return ok;
	}

	/**
	 * O Khan chegou na area de melhorias (manual, pag. 9): o jogador pega 1
	 * moeda e/ou 1 espada se os icones estiverem a mostra e depois os espacos
	 * vazios recebem pecas novas da pilha.
	 */
	private void reporAreaDeMelhorias() {
		if (area.iconeDeMoedaAMostra())
			jogadorAtual().adicionarRecursos(TipoTributo.MOEDA, 1);
		if (area.iconeDeEspadaAMostra())
			jogadorAtual().adicionarRecursos(TipoTributo.ESPADA, 1);
		area.preencherVazios();
	}

	/** O turno so pode ser encerrado depois de usar o Khan, quando a coluna ativada o exige. */
	boolean podeEncerrarTurno() {
		return !khanPendente;
	}

	/**
	 * Coloca na coluna branca a melhoria gratis ganha por linha completa: uma
	 * das pecas da area (0 a 4) ou a do topo da pilha (-1), sem regra de cor.
	 */
	boolean colocarMelhoriaGratis(String cor, int espaco) {
		Cartela c = cartelas.get(cor);
		if (encerrada || cor == null || !cor.equals(getCorAtual())
				|| c == null || c.getMelhoriasGratisPendentes() <= 0)
			return false;
		MelhoriaColorida m = espaco < 0 ? area.verTopo() : area.getEspaco(espaco);
		if (m == null)
			return false;
		area.retirar(espaco);
		return c.adicionarMelhoriaGratis(m.getTipo());
	}

	// =====================================================================
	// Habilidades especiais dos herdeiros (texto das cartelas, manual pag. 20)
	// =====================================================================

	AreaDeMelhorias getArea() {
		return area;
	}

	boolean isHabilidadeUsadaNoTurno() {
		return habilidadeUsadaNoTurno;
	}

	/** Altani: qual peao (1 ou 2) joga; 0 para os outros herdeiros. */
	int getPeaoEscolhido(String cor) {
		Integer n = peaoEscolhido.get(cor);
		return n == null ? 0 : n;
	}

	/**
	 * Altani: escolhe o peao (1 ou 2) que vai mover e fazer as acoes neste
	 * turno. So antes de ativar a coluna; o outro peao nao faz nada.
	 */
	boolean escolherPeao(int numero) {
		String cor = getCorAtual();
		if (encerrada || colunaAtivada != null || !herdeiro(cor).equals(ALTANI) || (numero != 1 && numero != 2))
			return false;
		peaoEscolhido.put(cor, numero);
		visitadas = new ArrayList<String>();
		visitadas.add(mapa.getPosicao(peaoDaVez()));
		return true;
	}

	/** Chagatai: 1x por turno, deixa de usar 1 cavalo para ganhar 1 peca de espada (que pode guardar). */
	boolean trocarCavaloPorEspada() {
		if (!podeAgir() || habilidadeUsadaNoTurno || movimentos < 1 || !herdeiro(getCorAtual()).equals(CHAGATAI))
			return false;
		movimentos--;
		jogadorAtual().adicionarRecursos(TipoTributo.ESPADA, 1);
		habilidadeUsadaNoTurno = true;
		return true;
	}

	/**
	 * Ogedei: 1x por turno, paga 1 moeda (real ou virtual) para pegar um tesouro
	 * de qualquer cidade do mapa, desde que nao seja o ultimo tesouro dela.
	 */
	boolean pegarTesouroComMoeda(String cidade, Tesouro tesouro) {
		if (!podeAgir() || habilidadeUsadaNoTurno || tesouro == null || !herdeiro(getCorAtual()).equals(OGEDEI))
			return false;
		Cidade c = mapa.getCidade(cidade);
		if (c == null || !c.isDisponivel() || c.getNumTesouros() < 2 || disponivel(TipoTributo.MOEDA) < 1)
			return false;
		if (!c.retirarTesouro(tesouro, getCorAtual()))
			return false;
		gastar(TipoTributo.MOEDA, 1);
		jogadorAtual().adicionarTesouro(tesouro);
		habilidadeUsadaNoTurno = true;
		return true;
	}

	/** Jochi: as 2 melhorias tiradas no inicio do jogo que ele ainda nao escolheu (vazia depois). */
	List<MelhoriaColorida> getMelhoriasIniciaisJochi(String cor) {
		List<MelhoriaColorida> lst = melhoriasIniciaisJochi.get(cor);
		return lst == null ? new ArrayList<MelhoriaColorida>() : new ArrayList<MelhoriaColorida>(lst);
	}

	/**
	 * Jochi: coloca na coluna branca uma das 2 melhorias iniciais (indice 0 ou 1);
	 * a outra volta para o fundo da pilha. Ela ocupa o espaco da 1a linha, entao
	 * completar a 1a linha nao da outra melhoria gratis (manual, pag. 20).
	 */
	boolean escolherMelhoriaInicial(String cor, int indice) {
		List<MelhoriaColorida> lst = melhoriasIniciaisJochi.get(cor);
		if (encerrada || lst == null || indice < 0 || indice >= lst.size())
			return false;
		MelhoriaColorida escolhida = lst.get(indice);
		if (!cartelas.get(cor).colocarMelhoriaInicial(escolhida.getTipo()))
			return false;
		for (int i = 0; i < lst.size(); i++)
			if (i != indice)
				area.devolverAoFundo(lst.get(i));
		melhoriasIniciaisJochi.remove(cor);
		return true;
	}

	/** Refaz area de melhorias e habilidades a partir de um arquivo salvo. */
	void restaurarHabilidades(MelhoriaColorida[] espacos, List<MelhoriaColorida> pilha, boolean usada,
			Map<String, Integer> peoes, Map<String, List<MelhoriaColorida>> iniciaisJochi) {
		area.restaurar(espacos, pilha);
		habilidadeUsadaNoTurno = usada;
		for (Map.Entry<String, Integer> e : peoes.entrySet()) {
			if (!herdeiro(e.getKey()).equals(ALTANI) || (e.getValue() != 1 && e.getValue() != 2))
				throw new IllegalArgumentException("peao escolhido invalido: " + e.getKey());
			peaoEscolhido.put(e.getKey(), e.getValue());
		}
		melhoriasIniciaisJochi.clear();
		for (Map.Entry<String, List<MelhoriaColorida>> e : iniciaisJochi.entrySet()) {
			if (!herdeiro(e.getKey()).equals(JOCHI))
				throw new IllegalArgumentException("melhoria inicial de quem nao e o Jochi: " + e.getKey());
			melhoriasIniciaisJochi.put(e.getKey(), new ArrayList<MelhoriaColorida>(e.getValue()));
		}
	}

	// recursos reais do jogador + virtuais do turno
	private int disponivel(TipoTributo t) {
		return jogadorAtual().getQuantidadeRecurso(t) + getVirtual(t);
	}

	// gasta primeiro os recursos virtuais, depois os reais
	private void gastar(TipoTributo t, int quantidade) {
		int doVirtual = Math.min(getVirtual(t), quantidade);
		if (doVirtual > 0)
			virtuais.put(t, getVirtual(t) - doVirtual);
		if (quantidade - doVirtual > 0)
			jogadorAtual().removerRecursos(t, quantidade - doVirtual);
	}

	// =====================================================================
	// Fim de jogo
	// =====================================================================

	/**
	 * Votos de influencia: 1o lugar em yurts (empatados ficam todos em 1o e
	 * ninguem fica em 2o) e 2o lugar (empatados ficam todos em 2o). Os votos
	 * dependem do numero de melhorias na coluna da regiao.
	 */
	void pontuarInfluencia() {
		if (!encerrada)
			throw new IllegalStateException("A partida ainda nao terminou.");
		if (pontuada)
			throw new IllegalStateException("A influencia ja foi pontuada.");
		for (Regiao regiao : Regiao.values()) {
			Map<String, Integer> yurts = new HashMap<String, Integer>();
			for (String cor : ordem) {
				int n = mapa.contarYurts(cor, regiao);
				if (n > 0)
					yurts.put(cor, n);
			}
			if (yurts.isEmpty())
				continue;
			int max = 0;
			for (int n : yurts.values())
				max = Math.max(max, n);
			int contPrimeiros = 0;
			int segundo = 0;
			for (int n : yurts.values()) {
				if (n == max)
					contPrimeiros++;
				else
					segundo = Math.max(segundo, n);
			}
			for (String cor : yurts.keySet()) {
				int n = yurts.get(cor);
				if (n == max)
					darVotosDeInfluencia(cor, regiao, VOTOS_MAIS_YURTS);
				else if (contPrimeiros == 1 && n == segundo)
					darVotosDeInfluencia(cor, regiao, VOTOS_SEGUNDO);
			}
		}
		pontuarCartasSecretas();
		pontuada = true;
	}

	/** Aplica os votos de 0 a 3 da carta secreta escolhida por cada jogador. */
	private void pontuarCartasSecretas() {
		for (String cor : ordem) {
			for (CartaConselheiro carta : jogadores.get(cor).getCartasConselheiro()) {
				if (carta.isAberta())
					continue;
				int votos = avaliarCartaSecreta(carta.getNome(), cor);
				if (votos > 0)
					jogadores.get(cor).adicionarVotos(votos); // pontuacao final nao move a ficha neutra
				break; // cada jogador escolhe e mantem somente uma carta secreta
			}
		}
	}

	private int avaliarCartaSecreta(String nome, String cor) {
		Cartela ct = cartelas.get(cor);
		int totalMelhorias = 0;
		Set<TipoMelhoria> simbolosDiferentes = new HashSet<TipoMelhoria>();
		Map<IconeAcao, Integer> porSimbolo = new HashMap<IconeAcao, Integer>();
		int melhoriasCavaloOuPunho = 0;
		for (ColunaCartela coluna : ColunaCartela.values()) {
			for (TipoMelhoria melhoria : ct.getMelhorias(coluna)) {
				if (melhoria == null)
					continue;
				totalMelhorias++;
				simbolosDiferentes.add(melhoria);
				List<IconeAcao> icones = melhoria.getIcones();
				if (icones.contains(IconeAcao.CAVALO) || icones.contains(IconeAcao.PUNHO))
					melhoriasCavaloOuPunho++;
				Set<IconeAcao> unicos = new HashSet<IconeAcao>(icones);
				unicos.remove(IconeAcao.TESOURO); // tesouros diferentes nao formam pares iguais
				for (IconeAcao icone : unicos)
					porSimbolo.put(icone, porSimbolo.getOrDefault(icone, 0) + 1);
			}
		}
		int pares = 0;
		for (int quantidade : porSimbolo.values())
			pares += quantidade * (quantidade - 1) / 2;

		switch (nome) {
		case "Viajante":
			return votosPorMeta(mapa.maiorGrupoDeYurtsConectados(cor), 3, 6, 8);
		case "Soldado":
			return votosPorMeta(totalMelhorias, 4, 7, 10);
		case "Colecionador":
			return votosPorMeta(simbolosDiferentes.size(), 4, 6, 7);
		case "Sacerdote":
			return votosPorMeta(pares, 1, 2, 4);
		case "Governador": {
			int regioes = 0;
			for (Regiao regiao : Regiao.values())
				if (mapa.contarYurts(cor, regiao) >= 2
						&& ct.getNumMelhorias(ColunaCartela.daRegiao(regiao)) >= 2)
					regioes++;
			return votosPorMeta(regioes, 1, 2, 3);
		}
		case "Bajulador":
			return votosPorMeta(contarLocaisComDuasYurtsAdjacentes(cor), 2, 3, 4);
		case "Prefeito":
			return votosPorMeta(contarCidadesAdjacentes(cor, 2), 2, 3, 4);
		case "Cidadao":
			return votosPorMeta(contarCidadesAdjacentes(cor, 1), 3, 6, 8);
		case "Cavaleiro":
			return votosPorMeta(melhoriasCavaloOuPunho, 2, 4, 5);
		case "Mensageiro":
			return votosPorMeta(contarParadasDuplasComYurt(cor), 2, 4, 6);
		default:
			return 0;
		}
	}

	private int votosPorMeta(int valor, int primeiro, int segundo, int terceiro) {
		if (valor >= terceiro)
			return 3;
		if (valor >= segundo)
			return 2;
		if (valor >= primeiro)
			return 1;
		return 0;
	}

	/** Quantas provincias do Khan tem pelo menos duas yurts do jogador em paradas adjacentes. */
	private int contarLocaisComDuasYurtsAdjacentes(String cor) {
		int total = 0;
		for (String provincia : mapa.getProvinciasDoKhan()) {
			int yurts = 0;
			for (String parada : mapa.getNomesParadas())
				if (mapa.getProvinciasAdjacentes(parada).contains(provincia))
					for (String dono : mapa.getYurtsNaParada(parada))
						if (cor.equals(dono))
							yurts++;
			if (yurts >= 2)
				total++;
		}
		return total;
	}

	/** Cidades com pelo menos minYurts yurts do jogador em paradas adjacentes. */
	private int contarCidadesAdjacentes(String cor, int minYurts) {
		int total = 0;
		for (String cidade : mapa.getNomesCidades()) {
			int yurts = 0;
			for (String parada : mapa.getNomesParadas())
				if (mapa.getCidadesAdjacentes(parada).contains(cidade))
					for (String dono : mapa.getYurtsNaParada(parada))
						if (cor.equals(dono))
							yurts++;
			if (yurts >= minYurts)
				total++;
		}
		return total;
	}

	private int contarParadasDuplasComYurt(String cor) {
		int total = 0;
		for (String parada : mapa.getNomesParadas()) {
			if (mapa.getTipoParada(parada) != TipoParada.DUPLA || !mapa.temConselheiro(parada))
				continue;
			if (mapa.getYurtsNaParada(parada).contains(cor))
				total++;
		}
		return total;
	}

	private void darVotosDeInfluencia(String cor, Regiao regiao, int[] tabela) {
		int votos = tabela[getMelhoriasNaColuna(cor, regiao)];
		if (votos > 0)
			jogadores.get(cor).adicionarVotos(votos);
	}

	/**
	 * Vencedor(es): mais votos; empate -> mais tesouros nao entregues;
	 * empate -> peao mais perto de Karakorum; se persistir, dividem a vitoria.
	 */
	List<String> vencedores() {
		if (!pontuada)
			throw new IllegalStateException("Pontue a influencia antes de apurar o vencedor.");
		List<String> v = manterMaiores(ordem, CRITERIO_VOTOS);
		if (v.size() > 1)
			v = manterMaiores(v, CRITERIO_TESOUROS);
		if (v.size() > 1)
			v = manterMaiores(v, CRITERIO_KARAKORUM);
		return v;
	}

	private static final int CRITERIO_VOTOS = 0;
	private static final int CRITERIO_TESOUROS = 1;
	private static final int CRITERIO_KARAKORUM = 2;

	// valor do jogador no criterio de desempate (maior e melhor)
	private int valor(String cor, int criterio) {
		switch (criterio) {
		case CRITERIO_VOTOS:
			return jogadores.get(cor).getVotos();
		case CRITERIO_TESOUROS:
			return jogadores.get(cor).getTesouros().size();
		default:
			int d = mapa.distanciaAteKarakorum(cor);
			return d < 0 ? Integer.MIN_VALUE : -d;
		}
	}

	private List<String> manterMaiores(List<String> cores, int criterio) {
		int melhor = Integer.MIN_VALUE;
		for (String c : cores)
			melhor = Math.max(melhor, valor(c, criterio));
		List<String> resultado = new ArrayList<String>();
		for (String c : cores)
			if (valor(c, criterio) == melhor)
				resultado.add(c);
		return resultado;
	}

	// =====================================================================
	// Salvar e recuperar a partida
	// =====================================================================

	int getIndiceAtual() {
		return indiceAtual;
	}

	boolean isUltimaRodada() {
		return ultimaRodada;
	}

	boolean isPontuada() {
		return pontuada;
	}

	List<String> getConselheirosComBonus() {
		List<String> lst = new ArrayList<String>(conselheirosComBonus);
		java.util.Collections.sort(lst);
		return lst;
	}

	/** Cidades ainda fechadas, na ordem em que serao reveladas. */
	List<String> getCidadesFechadas() {
		return new ArrayList<String>(cidadesFechadas);
	}

	/** Tesouros que restam na pilha, na ordem em que serao tirados. */
	List<Tesouro> getPilhaTesouros() {
		return new ArrayList<Tesouro>(pilhaTesouros);
	}

	Map<String, Integer> getTesourosTomadosNoTurno() {
		return new HashMap<String, Integer>(tesourosTomadosNoTurno);
	}

	/** Copia dos recursos virtuais deste turno. */
	Map<TipoTributo, Integer> getVirtuais() {
		return new HashMap<TipoTributo, Integer>(virtuais);
	}

	/** Refaz o estado da partida a partir de um arquivo salvo (lanca IllegalArgumentException se invalido). */
	void restaurar(int indice, int votos, boolean fim, boolean ultima, boolean terminada, boolean pontuou,
			List<String> bonus, boolean ativo, int mov, int pun, List<String> paradasVisitadas,
			Map<String, Integer> tomados, ColunaCartela coluna, boolean khan, Map<TipoTributo, Integer> virt,
			List<String> tesVirt) {
		if (indice < 0 || indice >= ordem.size() || votos < 0 || mov < 0 || pun < 0)
			throw new IllegalArgumentException("estado da partida invalido");
		this.indiceAtual = indice;
		this.votosNeutra = votos;
		this.fimDisparado = fim;
		this.ultimaRodada = ultima;
		this.encerrada = terminada;
		this.pontuada = pontuou;
		this.conselheirosComBonus.clear();
		this.conselheirosComBonus.addAll(bonus);
		this.turnoAtivo = ativo;
		this.movimentos = mov;
		this.punhos = pun;
		this.visitadas = new ArrayList<String>(paradasVisitadas);
		this.tesourosTomadosNoTurno = new HashMap<String, Integer>(tomados);
		this.colunaAtivada = coluna;
		this.khanPendente = khan;
		this.virtuais = new HashMap<TipoTributo, Integer>(virt);
		this.tesourosVirtuais = new ArrayList<String>(tesVirt);
	}

	// =====================================================================
	// Cartas de conselheiro aberto (manual, pag. 13 e 18)
	// =====================================================================

	List<String> getCartasAbertas() {
		return new ArrayList<String>(cartasAbertas);
	}

	Map<String, Integer> getBonusPorJogador() {
		return new HashMap<String, Integer>(bonusPorJogador);
	}

	void restaurarTiposBonusConselheiros(Map<String, String> tipos) {
		restaurarTiposBonus(tipos);
	}

	void restaurarCartasAbertas(List<String> cartas, Map<String, Integer> bonus) {
		for (String c : cartas)
			if (!java.util.Arrays.asList(TODAS_CARTAS_ABERTAS).contains(c))
				throw new IllegalArgumentException("carta aberta desconhecida: " + c);
		this.cartasAbertas = new ArrayList<String>(cartas);
		this.bonusPorJogador = new HashMap<String, Integer>(bonus);
	}

	/**
	 * Em qualquer momento do turno, quem cumpre a demanda de uma carta aberta ganha 1 voto e a carta sai
	 * do jogo (sem carta nova). So o primeiro a cumprir leva o voto.
	 */
	void verificarCartasAbertas() {
		if (encerrada || !turnoAtivo)
			return;
		String cor = getCorAtual();
		for (String carta : new ArrayList<String>(cartasAbertas))
			if (cartaAbertaCumprida(carta, cor)) {
				cartasAbertas.remove(carta);
				ganharVoto(cor);
			}
	}

	private int yurtsDe(String cor, String prefixo) {
		int n = 0;
		for (String parada : mapa.getNomesParadasOrdenados()) {
			if (prefixo != null && !parada.startsWith(prefixo))
				continue;
			for (String c : mapa.getYurtsNaParada(parada))
				if (cor.equals(c))
					n++;
		}
		return n;
	}

	private int cidadesDe(String cor) {
		int n = 0;
		for (String nome : mapa.getNomesCidades())
			if (cor.equals(mapa.getCidade(nome).getConquistador()))
				n++;
		return n;
	}

	private int melhoriasNaColuna(Cartela ct, ColunaCartela c) {
		int n = 0;
		for (TipoMelhoria t : ct.getMelhorias(c))
			if (t != null)
				n++;
		return n;
	}

	private boolean cartaAbertaCumprida(String carta, String cor) {
		Jogador j = jogadores.get(cor);
		Cartela ct = cartelas.get(cor);
		switch (carta) {
		case "4_espadas":
			return j.getQuantidadeRecurso(TipoTributo.ESPADA) >= 4;
		case "6_yurts":
			return yurtsDe(cor, null) >= 6;
		case "10_yurts":
			return yurtsDe(cor, null) >= 10;
		case "4_tesouros":
			return j.getTesouros().size() >= 4;
		case "5_tesouros_diferentes": {
			java.util.Set<String> tipos = new java.util.HashSet<String>(j.getNomesTesouros());
			return tipos.size() >= 5;
		}
		case "1_yurt_por_regiao":
			return yurtsDe(cor, "R") >= 1 && yurtsDe(cor, "C") >= 1 && yurtsDe(cor, "P") >= 1;
		case "2_yurts_por_regiao":
			return yurtsDe(cor, "R") >= 2 && yurtsDe(cor, "C") >= 2 && yurtsDe(cor, "P") >= 2;
		case "2_bonus":
			return bonusDe(cor) >= 2;
		case "3_bonus":
			return bonusDe(cor) >= 3;
		case "2_cidades":
			return cidadesDe(cor) >= 2;
		case "3_cidades":
			return cidadesDe(cor) >= 3;
		case "linha_completa":
			return ct.getLinhasCompletas() >= 1; // a melhoria inicial do Jochi nao conta
		case "4_melhorias_coluna":
			for (ColunaCartela c : ColunaCartela.values())
				if (melhoriasNaColuna(ct, c) >= 4)
					return true;
			return false;
		case "2_colunas_2_melhorias": {
			int colunas = 0;
			for (ColunaCartela c : ColunaCartela.values())
				if (melhoriasNaColuna(ct, c) >= 2)
					colunas++;
			return colunas >= 2;
		}
		default:
			return false;
		}
	}

	private int bonusDe(String cor) {
		Integer n = bonusPorJogador.get(cor);
		return n == null ? 0 : n;
	}
}
