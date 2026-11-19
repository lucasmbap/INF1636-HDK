package Model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Area de melhorias do tabuleiro principal (manual, pag. 4, 9, 11 e 12):
 * - 44 pecas embaralhadas numa pilha; 5 ficam reveladas nos espacos da area;
 * - quem compra uma peca da area deixa o espaco vazio e as pecas que sobram
 *   andam para a direita; nao se revelam pecas novas;
 * - tambem se pode comprar a peca do topo da pilha (aleatoria);
 * - so quando o Khan vai para a area os espacos vazios sao preenchidos. Antes
 *   disso, quem levou o Khan ganha 1 moeda se o espaco com o icone de moeda
 *   estiver vazio e 1 espada se o espaco com o icone de espada estiver vazio.
 *
 * Os espacos vao de 0 (esquerda, ao lado do Khan) a 4 (direita).
 */
class AreaDeMelhorias {
	static final int ESPACOS = 5;
	static final int ESPACO_ICONE_MOEDA = 1;
	static final int ESPACO_ICONE_ESPADA = 3;

	private final MelhoriaColorida[] espacos = new MelhoriaColorida[ESPACOS];
	private final Deque<MelhoriaColorida> pilha = new ArrayDeque<MelhoriaColorida>();

	/** Area vazia e pilha vazia (usado ao recuperar uma partida). */
	AreaDeMelhorias() {
	}

	/** Monta as 44 pecas do jogo, embaralha e revela 5 na area. */
	static AreaDeMelhorias nova() {
		AreaDeMelhorias a = new AreaDeMelhorias();
		List<MelhoriaColorida> todas = todasAsPecas();
		Collections.shuffle(todas);
		a.pilha.addAll(todas);
		a.preencherVazios();
		return a;
	}

	/**
	 * As 44 pecas (manual, pag. 16): moeda 8, espada 8, yurt 8, punho 6,
	 * cavalos 9 e 1 de cada tesouro. As cores foram divididas entre as tres
	 * colunas; cada tesouro so existe numa cor.
	 */
	static List<MelhoriaColorida> todasAsPecas() {
		List<MelhoriaColorida> lst = new ArrayList<MelhoriaColorida>();
		ColunaCartela r = ColunaCartela.VERMELHA, g = ColunaCartela.VERDE, y = ColunaCartela.AMARELA;
		adicionar(lst, TipoMelhoria.MOEDA, 3, 3, 2);
		adicionar(lst, TipoMelhoria.ESPADA, 3, 2, 3);
		adicionar(lst, TipoMelhoria.YURT, 2, 3, 3);
		adicionar(lst, TipoMelhoria.PUNHO, 2, 2, 2);
		adicionar(lst, TipoMelhoria.CAVALOS, 3, 3, 3);
		lst.add(new MelhoriaColorida(TipoMelhoria.TESOURO_FERRO, r));
		lst.add(new MelhoriaColorida(TipoMelhoria.TESOURO_PELE, g));
		lst.add(new MelhoriaColorida(TipoMelhoria.TESOURO_SEDA, g));
		lst.add(new MelhoriaColorida(TipoMelhoria.TESOURO_GADO, y));
		lst.add(new MelhoriaColorida(TipoMelhoria.TESOURO_ESPECIARIA, y));
		return lst;
	}

	private static void adicionar(List<MelhoriaColorida> lst, TipoMelhoria t, int vermelhas, int verdes,
			int amarelas) {
		for (int i = 0; i < vermelhas; i++)
			lst.add(new MelhoriaColorida(t, ColunaCartela.VERMELHA));
		for (int i = 0; i < verdes; i++)
			lst.add(new MelhoriaColorida(t, ColunaCartela.VERDE));
		for (int i = 0; i < amarelas; i++)
			lst.add(new MelhoriaColorida(t, ColunaCartela.AMARELA));
	}

	// =====================================================================
	// Consultas
	// =====================================================================

	/** Peca no espaco (0 a 4), ou null se o espaco estiver vazio ou nao existir. */
	MelhoriaColorida getEspaco(int i) {
		if (i < 0 || i >= ESPACOS)
			return null;
		return espacos[i];
	}

	/** Peca do topo da pilha (sem tirar), ou null se a pilha acabou. */
	MelhoriaColorida verTopo() {
		return pilha.peek();
	}

	int getTamanhoPilha() {
		return pilha.size();
	}

	/** Copia da pilha, do topo para o fundo. */
	List<MelhoriaColorida> getPilha() {
		return new ArrayList<MelhoriaColorida>(pilha);
	}

	// =====================================================================
	// Acoes
	// =====================================================================

	/**
	 * Tira a peca do espaco (0 a 4) ou do topo da pilha (espaco -1). As pecas
	 * restantes da area andam para a direita. Devolve null se nao houver peca.
	 */
	MelhoriaColorida retirar(int espaco) {
		if (espaco < 0)
			return pilha.poll();
		MelhoriaColorida m = getEspaco(espaco);
		if (m == null)
			return null;
		espacos[espaco] = null;
		moverParaDireita();
		return m;
	}

	// junta as pecas no lado direito, mantendo a ordem entre elas
	private void moverParaDireita() {
		int destino = ESPACOS - 1;
		for (int i = ESPACOS - 1; i >= 0; i--) {
			if (espacos[i] != null) {
				MelhoriaColorida m = espacos[i];
				espacos[i] = null;
				espacos[destino] = m;
				destino--;
			}
		}
	}

	/** O Khan chegou na area: o icone de moeda esta a mostra (espaco vazio)? */
	boolean iconeDeMoedaAMostra() {
		return espacos[ESPACO_ICONE_MOEDA] == null;
	}

	boolean iconeDeEspadaAMostra() {
		return espacos[ESPACO_ICONE_ESPADA] == null;
	}

	/** Revela pecas da pilha nos espacos vazios (feito quando o Khan vem para a area). */
	void preencherVazios() {
		for (int i = 0; i < ESPACOS; i++)
			if (espacos[i] == null && !pilha.isEmpty())
				espacos[i] = pilha.poll();
	}

	/** Devolve uma peca ao fundo da pilha (a melhoria inicial que o Jochi nao escolheu). */
	void devolverAoFundo(MelhoriaColorida m) {
		if (m != null)
			pilha.addLast(m);
	}

	/** Refaz a area a partir de um arquivo salvo. */
	void restaurar(MelhoriaColorida[] salvos, List<MelhoriaColorida> pilhaSalva) {
		if (salvos.length != ESPACOS)
			throw new IllegalArgumentException("area de melhorias invalida");
		for (int i = 0; i < ESPACOS; i++)
			espacos[i] = salvos[i];
		pilha.clear();
		pilha.addAll(pilhaSalva);
	}
}
