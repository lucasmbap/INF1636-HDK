package Model;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

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

	// indice = numero de jogadores (2 a 5): 10 / 14 / 16 / 17 votos
	private static final int[] LIMITE_VOTOS = { 0, 0, 10, 14, 16, 17 };
	// indice = numero de melhorias na coluna da regiao (0 a 4)
	private static final int[] VOTOS_MAIS_YURTS = { 1, 1, 2, 3, 4 };
	private static final int[] VOTOS_SEGUNDO = { 0, 1, 1, 2, 2 };

	private final Mapa mapa;
	private final Map<String, Jogador> jogadores = new HashMap<String, Jogador>();
	private final List<String> ordem = new ArrayList<String>();
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
		this.visitadas.add(mapa.getPosicao(getCorAtual()));
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
		String cor = getCorAtual();
		int custo = mapa.custoDoCaminho(cor, caminho);
		if (custo < 0 || custo > movimentos)
			return false;
		List<String> novas = mapa.getParadasVisitadas(cor, caminho); // antes de mover
		mapa.moverPeao(cor, caminho, movimentos);
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
		List<String> caminho = mapa.melhorCaminho(getCorAtual(), destino);
		if (caminho.isEmpty())
			return false;
		return mover(caminho);
	}

	/** Paradas onde o jogador atual esteve de fato neste turno (copia). */
	List<String> getParadasVisitadas() {
		return new ArrayList<String>(visitadas);
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

	/** Constroi um yurt numa parada onde o peao esteve de fato neste turno. */
	boolean construirYurt(String parada) {
		if (!podeAgir() || !visitadas.contains(parada))
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
	 * Compra uma melhoria por 2 moedas (reais ou virtuais) e a coloca na
	 * coluna da regiao, na cartela do jogador. O tipo da melhoria vem do nome
	 * da peca (TipoMelhoria); nomes desconhecidos ocupam o espaco sem efeito.
	 */
	boolean comprarMelhoria(PecaMelhoria melhoria, Regiao coluna) {
		if (encerrada || melhoria == null || coluna == null)
			return false;
		Jogador j = jogadorAtual();
		Cartela cartela = cartelas.get(getCorAtual());
		ColunaCartela col = ColunaCartela.daRegiao(coluna);
		if (cartela.getNumMelhorias(col) >= MAX_MELHORIAS_POR_COLUNA)
			return false;
		if (disponivel(TipoTributo.MOEDA) < CUSTO_MELHORIA)
			return false;
		gastar(TipoTributo.MOEDA, CUSTO_MELHORIA);
		j.adicionarMelhoria(melhoria);
		cartela.adicionarMelhoria(col, TipoMelhoria.porNome(melhoria.getNome()));
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
		if (destino.equals(Mapa.AREA_DE_MELHORIAS))
			ok = mapa.moverKhanParaAreaDeMelhorias();
		else
			ok = mapa.moverKhanParaProvincia(destino);
		if (ok)
			khanPendente = false;
		return ok;
	}

	/** O turno so pode ser encerrado depois de usar o Khan, quando a coluna ativada o exige. */
	boolean podeEncerrarTurno() {
		return !khanPendente;
	}

	/** Coloca na coluna branca uma melhoria gratis ganha por linha completa. */
	boolean colocarMelhoriaGratis(String cor, TipoMelhoria tipo) {
		Cartela c = cartelas.get(cor);
		return c != null && c.adicionarMelhoriaGratis(tipo);
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
		pontuada = true;
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
		List<String> v = manterMaiores(ordem, c -> jogadores.get(c).getVotos());
		if (v.size() > 1)
			v = manterMaiores(v, c -> jogadores.get(c).getTesouros().size());
		if (v.size() > 1)
			v = manterMaiores(v, c -> {
				int d = mapa.distanciaAteKarakorum(c);
				return d < 0 ? Integer.MIN_VALUE : -d;
			});
		return v;
	}

	private List<String> manterMaiores(List<String> cores, ToIntFunction<String> criterio) {
		int melhor = Integer.MIN_VALUE;
		for (String c : cores)
			melhor = Math.max(melhor, criterio.applyAsInt(c));
		List<String> resultado = new ArrayList<String>();
		for (String c : cores)
			if (criterio.applyAsInt(c) == melhor)
				resultado.add(c);
		return resultado;
	}
}
