package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Mapa do jogo, representado como um grafo:
 * - paradas ligadas entre si por rotas;
 * - cada parada e adjacente a algumas provincias e a algumas cidades;
 * - cada provincia do Khan indica 2 outras provincias que tambem recebem
 *   tributo quando o Khan e colocado nela.
 *
 * Todos os elementos sao identificados pelo nome (String) e guardados em
 * HashMaps. A montagem do mapa e feita pelos metodos adicionar.../ligar...,
 * de modo que o mapa oficial e mapas menores (usados nos testes) sao
 * construidos da mesma forma.
 */
class Mapa {
	static final String KARAKORUM = "Karakorum";
	static final String AREA_DE_MELHORIAS = "Area de melhorias";

	private Map<String, Parada> paradas = new HashMap<String, Parada>();
	private Map<String, Provincia> provincias = new HashMap<String, Provincia>();
	private Map<String, Cidade> cidades = new HashMap<String, Cidade>();

	// parada -> paradas ligadas a ela por uma rota
	private Map<String, List<String>> rotas = new HashMap<String, List<String>>();
	// parada -> provincias adjacentes
	private Map<String, List<String>> provinciasDaParada = new HashMap<String, List<String>>();
	// parada -> cidades adjacentes
	private Map<String, List<String>> cidadesDaParada = new HashMap<String, List<String>>();
	// provincia do Khan -> as 2 provincias indicadas pelas setas
	private Map<String, List<String>> alvosDoKhan = new HashMap<String, List<String>>();

	private Map<String, String> posicaoPeao = new HashMap<String, String>();
	private String posicaoKhan = null; // null: fora do tabuleiro (inicio do jogo)

	Mapa() {
		Parada k = new Parada(KARAKORUM, TipoParada.KARAKORUM, null, true);
		registrarParada(k);
	}

	// =====================================================================
	// Montagem do mapa
	// =====================================================================

	private void registrarParada(Parada p) {
		paradas.put(p.getNome(), p);
		rotas.put(p.getNome(), new ArrayList<String>());
		provinciasDaParada.put(p.getNome(), new ArrayList<String>());
		cidadesDaParada.put(p.getNome(), new ArrayList<String>());
	}

	/** Karakorum ja e criada pelo construtor; nao pode haver outra. */
	boolean adicionarParada(String nome, TipoParada tipo, Regiao regiao, boolean temConselheiro) {
		if (nome == null || paradas.containsKey(nome) || tipo == TipoParada.KARAKORUM || regiao == null)
			return false;
		registrarParada(new Parada(nome, tipo, regiao, temConselheiro));
		return true;
	}

	boolean adicionarProvincia(String nome, Regiao regiao, TipoTributo tipo, boolean doKhan) {
		if (nome == null || provincias.containsKey(nome) || tipo == null)
			return false;
		provincias.put(nome, new Provincia(nome, regiao, tipo, doKhan));
		return true;
	}

	boolean adicionarCidade(String nome, Regiao regiao) {
		if (nome == null || cidades.containsKey(nome) || regiao == null)
			return false;
		cidades.put(nome, new Cidade(nome, regiao));
		return true;
	}

	/** Cria uma rota (nos dois sentidos) entre duas paradas. */
	boolean conectarParadas(String a, String b) {
		if (!paradas.containsKey(a) || !paradas.containsKey(b) || a.equals(b))
			return false;
		if (rotas.get(a).contains(b))
			return false;
		rotas.get(a).add(b);
		rotas.get(b).add(a);
		return true;
	}

	boolean ligarProvincia(String parada, String provincia) {
		if (!paradas.containsKey(parada) || !provincias.containsKey(provincia))
			return false;
		List<String> lst = provinciasDaParada.get(parada);
		if (lst.contains(provincia))
			return false;
		lst.add(provincia);
		return true;
	}

	boolean ligarCidade(String parada, String cidade) {
		if (!paradas.containsKey(parada) || !cidades.containsKey(cidade))
			return false;
		List<String> lst = cidadesDaParada.get(parada);
		if (lst.contains(cidade))
			return false;
		lst.add(cidade);
		return true;
	}

	/** Define as 2 provincias indicadas pelas setas de uma provincia do Khan. */
	boolean definirAlvosDoKhan(String provinciaKhan, String alvo1, String alvo2) {
		Provincia pk = provincias.get(provinciaKhan);
		if (pk == null || !pk.isDoKhan())
			return false;
		if (!provincias.containsKey(alvo1) || !provincias.containsKey(alvo2))
			return false;
		if (alvo1.equals(alvo2) || alvo1.equals(provinciaKhan) || alvo2.equals(provinciaKhan))
			return false;
		List<String> alvos = new ArrayList<String>();
		alvos.add(alvo1);
		alvos.add(alvo2);
		alvosDoKhan.put(provinciaKhan, alvos);
		return true;
	}

	// =====================================================================
	// Consultas sobre a estrutura
	// =====================================================================

	Parada getParada(String nome) {
		return paradas.get(nome);
	}

	Provincia getProvincia(String nome) {
		return provincias.get(nome);
	}

	Cidade getCidade(String nome) {
		return cidades.get(nome);
	}

	boolean existeParada(String nome) {
		return paradas.containsKey(nome);
	}

	boolean saoAdjacentes(String a, String b) {
		List<String> vizinhas = rotas.get(a);
		return vizinhas != null && vizinhas.contains(b);
	}

	/** As listas retornadas sao copias: altera-las nao altera o mapa. */
	List<String> getParadasAdjacentes(String parada) {
		return copia(rotas.get(parada));
	}

	List<String> getProvinciasAdjacentes(String parada) {
		return copia(provinciasDaParada.get(parada));
	}

	List<String> getCidadesAdjacentes(String parada) {
		return copia(cidadesDaParada.get(parada));
	}

	List<String> getAlvosDoKhan(String provinciaKhan) {
		return copia(alvosDoKhan.get(provinciaKhan));
	}

	List<String> getProvinciasDoKhan() {
		List<String> lst = new ArrayList<String>();
		for (Provincia p : provincias.values())
			if (p.isDoKhan())
				lst.add(p.getNome());
		return lst;
	}

	private List<String> copia(List<String> lst) {
		if (lst == null)
			return new ArrayList<String>();
		return new ArrayList<String>(lst);
	}

	/** A provincia e adjacente a pelo menos uma das paradas da lista? */
	boolean provinciaAdjacenteAAlguma(String provincia, List<String> paradasVisitadas) {
		for (String p : paradasVisitadas)
			if (getProvinciasAdjacentes(p).contains(provincia))
				return true;
		return false;
	}

	/** A cidade e adjacente a pelo menos uma das paradas da lista? */
	boolean cidadeAdjacenteAAlguma(String cidade, List<String> paradasVisitadas) {
		for (String p : paradasVisitadas)
			if (getCidadesAdjacentes(p).contains(cidade))
				return true;
		return false;
	}

	// =====================================================================
	// Peoes e movimento
	// =====================================================================

	/** Coloca o peao no mapa (inicio do jogo: Karakorum). */
	boolean colocarPeao(String cor, String parada) {
		Parada p = paradas.get(parada);
		if (p == null || posicaoPeao.containsKey(cor))
			return false;
		if (!p.adicionarPeao(cor))
			return false;
		posicaoPeao.put(cor, parada);
		return true;
	}

	/** Nome da parada onde esta o peao, ou null se ele nao esta no mapa. */
	String getPosicao(String cor) {
		return posicaoPeao.get(cor);
	}

	/**
	 * Calcula quantos movimentos (cavalos) o caminho custa.
	 * O caminho e a sequencia de paradas visitadas, sem incluir a parada
	 * de origem. Regras aplicadas:
	 * - cada passo tem de seguir uma rota;
	 * - pode-se passar por paradas ocupadas, mas a parada final tem de
	 *   ter lugar para o peao;
	 * - passar por uma parada (intermediaria) que tenha yurt proprio nao
	 *   custa movimento: o jogador "pula" essa parada.
	 * Retorna -1 se o caminho nao for valido.
	 */
	int custoDoCaminho(String cor, List<String> caminho) {
		String origem = posicaoPeao.get(cor);
		if (origem == null || caminho == null || caminho.isEmpty())
			return -1;

		String anterior = origem;
		for (String atual : caminho) {
			if (!saoAdjacentes(anterior, atual))
				return -1;
			anterior = atual;
		}

		String destino = caminho.get(caminho.size() - 1);
		if (!paradas.get(destino).podeReceberPeao(cor))
			return -1;

		int custo = caminho.size();
		for (int i = 0; i < caminho.size() - 1; i++)
			if (paradas.get(caminho.get(i)).temYurtDe(cor))
				custo--;
		return custo;
	}

	/**
	 * Paradas onde o peao "esteve de fato" ao percorrer o caminho: a
	 * origem, as intermediarias que nao foram puladas e o destino. So
	 * nelas o jogador pode pegar tributos, atacar cidades etc.
	 * Retorna lista vazia se o caminho nao for valido.
	 */
	List<String> getParadasVisitadas(String cor, List<String> caminho) {
		List<String> visitadas = new ArrayList<String>();
		if (custoDoCaminho(cor, caminho) < 0)
			return visitadas;
		visitadas.add(posicaoPeao.get(cor));
		for (int i = 0; i < caminho.size() - 1; i++)
			if (!paradas.get(caminho.get(i)).temYurtDe(cor))
				visitadas.add(caminho.get(i));
		visitadas.add(caminho.get(caminho.size() - 1));
		return visitadas;
	}

	/**
	 * Move o peao pelo caminho, se o custo nao passar dos movimentos
	 * disponiveis. Retorna o numero de movimentos gastos, ou -1 se o
	 * movimento nao for permitido (nesse caso o peao nao sai do lugar).
	 */
	int moverPeao(String cor, List<String> caminho, int movimentosDisponiveis) {
		int custo = custoDoCaminho(cor, caminho);
		if (custo < 0 || custo > movimentosDisponiveis)
			return -1;
		String origem = posicaoPeao.get(cor);
		String destino = caminho.get(caminho.size() - 1);
		paradas.get(origem).removerPeao(cor);
		paradas.get(destino).adicionarPeao(cor);
		posicaoPeao.put(cor, destino);
		return custo;
	}

	/**
	 * Menor numero de passos entre duas paradas (busca em largura).
	 * Retorna -1 se nao houver caminho. Usado no desempate pela
	 * proximidade de Karakorum.
	 */
	int distancia(String origem, String destino) {
		if (!paradas.containsKey(origem) || !paradas.containsKey(destino))
			return -1;
		Map<String, Integer> dist = new HashMap<String, Integer>();
		Queue<String> fila = new LinkedList<String>();
		dist.put(origem, 0);
		fila.offer(origem);
		while (!fila.isEmpty()) {
			String atual = fila.poll();
			if (atual.equals(destino))
				return dist.get(atual);
			for (String viz : rotas.get(atual)) {
				if (!dist.containsKey(viz)) {
					dist.put(viz, dist.get(atual) + 1);
					fila.offer(viz);
				}
			}
		}
		return -1;
	}

	int distanciaAteKarakorum(String cor) {
		String pos = posicaoPeao.get(cor);
		if (pos == null)
			return -1;
		return distancia(pos, KARAKORUM);
	}

	// =====================================================================
	// Yurts
	// =====================================================================

	boolean podeConstruirYurt(String parada) {
		Parada p = paradas.get(parada);
		return p != null && p.podeConstruirYurt();
	}

	/**
	 * Constroi um yurt da cor indicada. Se o jogador passou pela parada
	 * no turno (e se tem a peca de tributo de yurt) e verificado por quem
	 * chama este metodo.
	 */
	boolean construirYurt(String cor, String parada) {
		Parada p = paradas.get(parada);
		if (p == null)
			return false;
		return p.construirYurt(cor);
	}

	/**
	 * Yurts do jogador numa regiao: os das paradas da regiao mais os do
	 * centro das cidades conquistadas por ele nessa regiao.
	 */
	int contarYurts(String cor, Regiao regiao) {
		int total = 0;
		for (Parada p : paradas.values())
			if (p.getRegiao() == regiao)
				total += p.contarYurtsDe(cor);
		for (Cidade c : cidades.values())
			if (c.getRegiao() == regiao && cor.equals(c.getConquistador()))
				total++;
		return total;
	}

	/** Total de yurts do jogador no mapa (paradas e cidades conquistadas). */
	int contarYurtsNoMapa(String cor) {
		int total = 0;
		for (Parada p : paradas.values())
			total += p.contarYurtsDe(cor);
		for (Cidade c : cidades.values())
			if (cor.equals(c.getConquistador()))
				total++;
		return total;
	}

	/**
	 * Tamanho do maior grupo de paradas com yurt do jogador ligadas por
	 * rotas (usado na carta de conselheiro secreto "Viajante").
	 */
	int maiorGrupoDeYurtsConectados(String cor) {
		Set<String> visitadas = new HashSet<String>();
		int maior = 0;
		for (Parada p : paradas.values()) {
			if (!p.temYurtDe(cor) || visitadas.contains(p.getNome()))
				continue;
			int tamanho = 0;
			Queue<String> fila = new LinkedList<String>();
			fila.offer(p.getNome());
			visitadas.add(p.getNome());
			while (!fila.isEmpty()) {
				String atual = fila.poll();
				tamanho += paradas.get(atual).contarYurtsDe(cor);
				for (String viz : rotas.get(atual)) {
					if (!visitadas.contains(viz) && paradas.get(viz).temYurtDe(cor)) {
						visitadas.add(viz);
						fila.offer(viz);
					}
				}
			}
			if (tamanho > maior)
				maior = tamanho;
		}
		return maior;
	}

	// =====================================================================
	// Tributos nas provincias
	// =====================================================================

	/** Coloca 1 tributo na provincia (preparacao do jogo). */
	boolean colocarTributo(String provincia) {
		Provincia p = provincias.get(provincia);
		return p != null && p.adicionarTributo();
	}

	/**
	 * Pega 1 tributo de uma provincia adjacente a alguma das paradas
	 * visitadas no turno. Retorna o tipo do tributo pego, ou null se nao
	 * for permitido (provincia vazia ou nao adjacente).
	 */
	TipoTributo pegarTributo(String provincia, List<String> paradasVisitadas) {
		Provincia p = provincias.get(provincia);
		if (p == null || !provinciaAdjacenteAAlguma(provincia, paradasVisitadas))
			return null;
		if (!p.retirarTributo())
			return null;
		return p.getTipo();
	}

	// =====================================================================
	// Khan
	// =====================================================================

	/** Nome da provincia onde esta o Khan, AREA_DE_MELHORIAS ou null. */
	String getPosicaoKhan() {
		return posicaoKhan;
	}

	/**
	 * Coloca o Khan numa provincia do Khan. Ela e as 2 provincias indicadas
	 * pelas setas recebem 1 tributo cada (respeitando o maximo de 3).
	 * O Khan nao pode permanecer onde ja esta.
	 */
	boolean moverKhanParaProvincia(String provincia) {
		Provincia p = provincias.get(provincia);
		if (p == null || !p.isDoKhan() || provincia.equals(posicaoKhan))
			return false;
		posicaoKhan = provincia;
		p.adicionarTributo();
		for (String alvo : getAlvosDoKhan(provincia))
			provincias.get(alvo).adicionarTributo();
		return true;
	}

	/** A reposicao das melhorias e feita por quem chama este metodo. */
	boolean moverKhanParaAreaDeMelhorias() {
		if (AREA_DE_MELHORIAS.equals(posicaoKhan))
			return false;
		posicaoKhan = AREA_DE_MELHORIAS;
		return true;
	}

	// =====================================================================
	// Cidades
	// =====================================================================

	boolean revelarCidade(String cidade, List<Tesouro> tesouros) {
		Cidade c = cidades.get(cidade);
		return c != null && c.revelar(tesouros);
	}

	List<String> getCidadesDisponiveis() {
		List<String> lst = new ArrayList<String>();
		for (Cidade c : cidades.values())
			if (c.isDisponivel())
				lst.add(c.getNome());
		return lst;
	}

	/**
	 * Retira um tesouro de uma cidade adjacente a alguma parada visitada.
	 * O pagamento em espadas e verificado por quem chama este metodo.
	 */
	boolean retirarTesouroDeCidade(String cidade, Tesouro tesouro, String cor,
			List<String> paradasVisitadas) {
		Cidade c = cidades.get(cidade);
		if (c == null || !cidadeAdjacenteAAlguma(cidade, paradasVisitadas))
			return false;
		return c.retirarTesouro(tesouro, cor);
	}

	/** Fim de jogo alternativo: todas as cidades foram conquistadas. */
	boolean todasAsCidadesConquistadas() {
		if (cidades.isEmpty())
			return false;
		for (Cidade c : cidades.values())
			if (!c.isConquistada())
				return false;
		return true;
	}
}
