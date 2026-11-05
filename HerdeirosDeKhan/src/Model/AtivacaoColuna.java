package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O que uma coluna ativada da ao jogador neste turno: cavalos (movimentos),
 * punhos (pegar tributo), se o Khan tem de ser usado, se as pecas de
 * ativacao sao recuperadas e os recursos virtuais (moeda, espada, yurt e
 * tesouros), que so valem neste turno.
 */
class AtivacaoColuna {
	private final ColunaCartela coluna;
	private int cavalos = 0;
	private int punhos = 0;
	private boolean khan = false;
	private boolean recupera = false;
	private final Map<TipoTributo, Integer> virtuais = new HashMap<TipoTributo, Integer>();
	private final List<String> tesourosVirtuais = new ArrayList<String>();

	AtivacaoColuna(ColunaCartela coluna) {
		this.coluna = coluna;
	}

	/** Soma um icone; tesouro e o nome do tesouro virtual (so para TESOURO). */
	void somar(IconeAcao icone, String tesouro) {
		switch (icone) {
		case CAVALO:
			cavalos++;
			break;
		case PUNHO:
			punhos++;
			break;
		case KHAN:
			khan = true;
			break;
		case RECUPERAR:
			recupera = true;
			break;
		case MOEDA:
			somarVirtual(TipoTributo.MOEDA);
			break;
		case ESPADA:
			somarVirtual(TipoTributo.ESPADA);
			break;
		case YURT:
			somarVirtual(TipoTributo.YURT);
			break;
		case TESOURO:
			if (tesouro != null)
				tesourosVirtuais.add(tesouro);
			break;
		}
	}

	private void somarVirtual(TipoTributo t) {
		int atual = virtuais.containsKey(t) ? virtuais.get(t) : 0;
		virtuais.put(t, atual + 1);
	}

	ColunaCartela getColuna() {
		return coluna;
	}

	int getCavalos() {
		return cavalos;
	}

	int getPunhos() {
		return punhos;
	}

	boolean temKhan() {
		return khan;
	}

	boolean recupera() {
		return recupera;
	}

	/** Copia dos recursos virtuais (moeda, espada, yurt) deste turno. */
	Map<TipoTributo, Integer> getVirtuais() {
		return new HashMap<TipoTributo, Integer>(virtuais);
	}

	List<String> getTesourosVirtuais() {
		return new ArrayList<String>(tesourosVirtuais);
	}
}
