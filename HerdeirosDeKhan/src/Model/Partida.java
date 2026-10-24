package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estado geral de uma partida: jogadores, ordem dos turnos, jogador da
 * vez, movimentos disponiveis no turno e o mapa.
 *
 * Na 2a iteracao o numero de movimentos e definido pelo testador
 * (definirMovimentos). Quando as colunas de ativacao forem implementadas,
 * esse numero passara a vir da coluna escolhida pelo jogador.
 */
class Partida {
	static final int MIN_JOGADORES = 2;
	static final int MAX_JOGADORES = 5;

	private Mapa mapa;
	private Map<String, Jogador> jogadores = new HashMap<String, Jogador>(); // cor -> jogador
	private List<String> ordem = new ArrayList<String>();                   // cores na ordem dos turnos
	private boolean iniciada = false;
	private int vez = 0;
	private int movimentosRestantes = 0;
	private List<String> visitadasNoTurno = new ArrayList<String>();
	private List<ObservadorModelo> observadores = new ArrayList<ObservadorModelo>();

	Partida(Mapa mapa) {
		this.mapa = mapa;
	}

	Mapa getMapa() {
		return mapa;
	}

	// ---------------- preparacao ----------------

	/**
	 * Inclui na partida um jogador ja criado (pela ModeloApi). Os jogadores
	 * jogam na ordem em que foram adicionados. Nao aceita cor repetida, mais
	 * de 5 jogadores nem jogadores novos depois do inicio.
	 */
	boolean adicionarJogador(Jogador j) {
		if (iniciada || j == null || jogadores.containsKey(j.getCor()) || ordem.size() >= MAX_JOGADORES)
			return false;
		jogadores.put(j.getCor(), j);
		ordem.add(j.getCor());
		return true;
	}

	/**
	 * Comeca a partida: todos os peoes vao para Karakorum e cada provincia
	 * recebe 1 peca de tributo (manual, pagina 4).
	 */
	boolean iniciar() {
		if (iniciada || ordem.size() < MIN_JOGADORES)
			return false;
		for (String cor : ordem)
			mapa.colocarPeao(cor, Mapa.KARAKORUM);
		for (String prov : mapa.getNomesProvincias())
			mapa.colocarTributo(prov);
		iniciada = true;
		vez = 0;
		comecarTurno();
		return true;
	}

	boolean isIniciada() {
		return iniciada;
	}

	// ---------------- consultas ----------------

	/** Cor do jogador da vez, ou null se a partida nao comecou. */
	String getCorDaVez() {
		if (!iniciada)
			return null;
		return ordem.get(vez);
	}

	List<String> getCores() {
		return new ArrayList<String>(ordem);
	}

	Jogador getJogador(String cor) {
		return jogadores.get(cor);
	}

	int getMovimentosRestantes() {
		return movimentosRestantes;
	}

	/** Paradas onde o jogador da vez esteve de fato neste turno. */
	List<String> getParadasVisitadasNoTurno() {
		return new ArrayList<String>(visitadasNoTurno);
	}

	// ---------------- jogadas ----------------

	/** Define quantos movimentos o jogador da vez tem (escolha do testador). */
	boolean definirMovimentos(int quantidade) {
		if (!iniciada || quantidade < 0)
			return false;
		movimentosRestantes = quantidade;
		notificar();
		return true;
	}

	/**
	 * Tenta levar o peao da vez ate o destino clicado. Se nao for possivel,
	 * nada muda e o metodo retorna false (a interface nao mostra mensagem).
	 */
	boolean moverPara(String destino) {
		if (!iniciada)
			return false;
		String cor = getCorDaVez();
		List<String> caminho = mapa.melhorCaminho(cor, destino);
		if (caminho.isEmpty())
			return false;
		List<String> visitadas = mapa.getParadasVisitadas(cor, caminho);
		int gasto = mapa.moverPeao(cor, caminho, movimentosRestantes);
		if (gasto < 0)
			return false;
		movimentosRestantes -= gasto;
		for (String p : visitadas)
			if (!visitadasNoTurno.contains(p))
				visitadasNoTurno.add(p);
		notificar();
		return true;
	}

	/** Encerra o turno e passa a vez para o proximo jogador. */
	void passarVez() {
		if (!iniciada)
			return;
		vez = (vez + 1) % ordem.size();
		comecarTurno();
	}

	private void comecarTurno() {
		movimentosRestantes = 0;
		visitadasNoTurno.clear();
		visitadasNoTurno.add(mapa.getPosicao(getCorDaVez()));
		notificar();
	}

	// ---------------- Observer ----------------

	void registrarObservador(ObservadorModelo o) {
		if (o != null && !observadores.contains(o))
			observadores.add(o);
	}

	void removerObservador(ObservadorModelo o) {
		observadores.remove(o);
	}

	private void notificar() {
		for (ObservadorModelo o : observadores)
			o.modeloAlterado();
	}
}
