package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cartela (tabuleiro) de um jogador: 4 colunas, cada uma com a acao base
 * impressa pelo herdeiro e ate 4 espacos de melhoria acima dela.
 *
 * Pecas de ativacao: ao ativar uma coluna colorida, ela fica ocupada ate o
 * jogador ativar a coluna branca, que recupera todas as pecas (manual,
 * paginas 6 e 9). A coluna branca nunca fica ocupada.
 *
 * Melhorias sao colocadas de baixo para cima. Cada linha completa nas 3
 * colunas coloridas da direito a uma melhoria gratis na coluna branca. Uma
 * melhoria colocada no turno nao vale ate o turno seguinte.
 */
class Cartela {
	static final int ESPACOS_POR_COLUNA = 4;
	static final String[] HERDEIROS = { "Altani", "Chagatai", "Jochi", "Ogedei", "Tolui" };

	private final String herdeiro;
	private final Map<ColunaCartela, List<IconeAcao>> acoesBase = new HashMap<ColunaCartela, List<IconeAcao>>();
	// melhorias de cada coluna, de baixo para cima (null = melhoria sem tipo conhecido)
	private final Map<ColunaCartela, List<TipoMelhoria>> melhorias = new HashMap<ColunaCartela, List<TipoMelhoria>>();
	// quantas melhorias de cada coluna foram colocadas neste turno (ainda nao valem)
	private final Map<ColunaCartela, Integer> novasNoTurno = new HashMap<ColunaCartela, Integer>();
	private final List<ColunaCartela> ocupadas = new ArrayList<ColunaCartela>();
	private int melhoriasGratisRecebidas = 0;

	/** Cria a cartela do herdeiro (Altani, Chagatai, Jochi, Ogedei ou Tolui). */
	Cartela(String herdeiro) {
		this.herdeiro = nomeOficial(herdeiro);
		if (this.herdeiro == null)
			throw new IllegalArgumentException("Herdeiro desconhecido: " + herdeiro);
		for (ColunaCartela c : ColunaCartela.values()) {
			melhorias.put(c, new ArrayList<TipoMelhoria>());
			novasNoTurno.put(c, 0);
		}
		montarAcoesBase();
	}

	/** Nome oficial do herdeiro (ignorando maiusculas), ou null se nao existir. */
	static String nomeOficial(String nome) {
		if (nome == null)
			return null;
		for (String h : HERDEIROS)
			if (h.equalsIgnoreCase(nome.trim()))
				return h;
		return null;
	}

	// acoes impressas nas cartelas (faixa colorida embaixo de cada coluna)
	private void montarAcoesBase() {
		IconeAcao C = IconeAcao.CAVALO, P = IconeAcao.PUNHO, K = IconeAcao.KHAN, M = IconeAcao.MOEDA;
		IconeAcao[] doisCavalosUmPunho = { C, C, P };
		IconeAcao[] umCavaloDoisPunhosKhan = { C, P, P, K };
		IconeAcao[] umCavaloUmPunhoMoeda = { C, P, M };
		if (herdeiro.equals("Altani")) {
			definir(ColunaCartela.VERMELHA, umCavaloDoisPunhosKhan);
			definir(ColunaCartela.VERDE, doisCavalosUmPunho);
			definir(ColunaCartela.AMARELA, umCavaloUmPunhoMoeda);
		} else if (herdeiro.equals("Chagatai")) {
			definir(ColunaCartela.VERMELHA, umCavaloDoisPunhosKhan);
			definir(ColunaCartela.VERDE, umCavaloUmPunhoMoeda);
			definir(ColunaCartela.AMARELA, doisCavalosUmPunho);
		} else if (herdeiro.equals("Jochi")) {
			definir(ColunaCartela.VERMELHA, umCavaloUmPunhoMoeda);
			definir(ColunaCartela.VERDE, umCavaloDoisPunhosKhan);
			definir(ColunaCartela.AMARELA, doisCavalosUmPunho);
		} else if (herdeiro.equals("Ogedei")) {
			definir(ColunaCartela.VERMELHA, doisCavalosUmPunho);
			definir(ColunaCartela.VERDE, umCavaloUmPunhoMoeda);
			definir(ColunaCartela.AMARELA, umCavaloDoisPunhosKhan);
		} else { // Tolui
			definir(ColunaCartela.VERMELHA, umCavaloUmPunhoMoeda);
			definir(ColunaCartela.VERDE, doisCavalosUmPunho);
			definir(ColunaCartela.AMARELA, umCavaloDoisPunhosKhan);
		}
		IconeAcao[] branca = { C, C, K, IconeAcao.RECUPERAR }; // igual em todas as cartelas
		definir(ColunaCartela.BRANCA, branca);
	}

	private void definir(ColunaCartela c, IconeAcao[] icones) {
		List<IconeAcao> lst = new ArrayList<IconeAcao>();
		for (IconeAcao i : icones)
			lst.add(i);
		acoesBase.put(c, lst);
	}

	// =====================================================================
	// Consultas
	// =====================================================================

	String getHerdeiro() {
		return herdeiro;
	}

	List<IconeAcao> getAcoesBase(ColunaCartela c) {
		return new ArrayList<IconeAcao>(acoesBase.get(c));
	}

	List<TipoMelhoria> getMelhorias(ColunaCartela c) {
		return new ArrayList<TipoMelhoria>(melhorias.get(c));
	}

	int getNumMelhorias(ColunaCartela c) {
		return melhorias.get(c).size();
	}

	boolean isOcupada(ColunaCartela c) {
		return ocupadas.contains(c);
	}

	/** Pecas de ativacao que ainda nao estao em nenhuma coluna (de 1 a 4). */
	int getPecasLivres() {
		return ColunaCartela.values().length - ocupadas.size();
	}

	/** Linhas com melhoria nas 3 colunas coloridas. */
	int getLinhasCompletas() {
		int menor = ESPACOS_POR_COLUNA;
		for (ColunaCartela c : ColunaCartela.values())
			if (c != ColunaCartela.BRANCA && getNumMelhorias(c) < menor)
				menor = getNumMelhorias(c);
		return menor;
	}

	/** Melhorias gratis que o jogador ja ganhou (linhas completas) e ainda nao colocou. */
	int getMelhoriasGratisPendentes() {
		int pendentes = getLinhasCompletas() - melhoriasGratisRecebidas;
		int espacoNaBranca = ESPACOS_POR_COLUNA - getNumMelhorias(ColunaCartela.BRANCA);
		if (pendentes > espacoNaBranca)
			pendentes = espacoNaBranca;
		return pendentes < 0 ? 0 : pendentes;
	}

	// =====================================================================
	// Acoes
	// =====================================================================

	/**
	 * Ativa uma coluna: devolve a soma da acao base com as melhorias que ja
	 * valem nela, ou null se a coluna estiver ocupada por uma peca de
	 * ativacao. Uma coluna colorida fica ocupada; a branca devolve todas as
	 * pecas.
	 */
	AtivacaoColuna ativar(ColunaCartela c) {
		if (isOcupada(c))
			return null;
		AtivacaoColuna a = new AtivacaoColuna(c);
		for (IconeAcao i : acoesBase.get(c))
			a.somar(i, null);
		List<TipoMelhoria> lst = melhorias.get(c);
		int validas = lst.size() - novasNoTurno.get(c);
		for (int k = 0; k < validas; k++) {
			TipoMelhoria t = lst.get(k);
			if (t != null)
				for (IconeAcao i : t.getIcones())
					a.somar(i, t.getTesouro());
		}
		if (c == ColunaCartela.BRANCA)
			ocupadas.clear();
		else
			ocupadas.add(c);
		return a;
	}

	/**
	 * Coloca uma melhoria (comprada) no proximo espaco livre da coluna. Na
	 * coluna branca so entram as melhorias gratis (use adicionarMelhoriaGratis).
	 */
	boolean adicionarMelhoria(ColunaCartela c, TipoMelhoria tipo) {
		if (c == null || c == ColunaCartela.BRANCA || getNumMelhorias(c) >= ESPACOS_POR_COLUNA)
			return false;
		colocar(c, tipo);
		return true;
	}

	/** Coloca na coluna branca uma melhoria gratis ganha por linha completa. */
	boolean adicionarMelhoriaGratis(TipoMelhoria tipo) {
		if (getMelhoriasGratisPendentes() <= 0)
			return false;
		colocar(ColunaCartela.BRANCA, tipo);
		melhoriasGratisRecebidas++;
		return true;
	}

	private void colocar(ColunaCartela c, TipoMelhoria tipo) {
		melhorias.get(c).add(tipo);
		novasNoTurno.put(c, novasNoTurno.get(c) + 1);
	}

	/** Fim do turno: as melhorias colocadas neste turno passam a valer. */
	void fimDoTurno() {
		for (ColunaCartela c : ColunaCartela.values())
			novasNoTurno.put(c, 0);
	}
}
