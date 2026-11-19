package View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** As 10 cartas de conselheiro secreto do manual (pagina 19): nome, condicao e recompensa. */
public final class CartasSecretas {
	private static final String RECOMPENSA = "De 0 a 3 votos no fim da partida: o 1o, o 2o ou o 3o valor da condicao = 1, 2 ou 3 votos.";

	private static final String[][] CARTAS = {
			{ "Viajante", "Ter um grupo de 3-6-8 yurts conectados. A rede pode ter ramificacoes." },
			{ "Soldado", "Ter 4-7-10 pecas de melhoria." },
			{ "Colecionador",
					"Ter 4-6-7 pecas de melhoria com simbolos diferentes (simbolos de tesouro contam como diferentes)." },
			{ "Sacerdote",
					"Ter 1-2-4 pares de melhorias com o mesmo simbolo (simbolos de tesouro diferentes nao contam como iguais)." },
			{ "Governador", "Ter 1-2-3 regioes com 2 yurts e 2 melhorias na coluna associada." },
			{ "Bajulador", "Colocar ao menos 2 yurts adjacentes a 2-3-4 provincias de Khan." },
			{ "Prefeito",
					"Colocar ao menos 2 yurts adjacentes a 2-3-4 cidades. Yurts dentro de cidades nao contam como adjacentes." },
			{ "Cidadao",
					"Colocar ao menos 1 yurt adjacente a 3-6-8 cidades. Yurts dentro de cidades nao contam como adjacentes." },
			{ "Cavaleiro", "Ter 2-4-5 pecas de melhoria com simbolos de cavalo ou de punho." },
			{ "Mensageiro",
					"Colocar ao menos 1 yurt em 2-4-6 paradas duplas no mapa (qualquer parada de conselheiro, exceto Karakorum)." } };

	private CartasSecretas() {
	}

	public static String recompensa() {
		return RECOMPENSA;
	}

	/** Baralha as 10 cartas e devolve todas ({nome, condicao}); cada jogador (ate 5) recebe 2. */
	public static List<String[]> baralhar() {
		List<String[]> lst = new ArrayList<String[]>();
		for (String[] c : CARTAS)
			lst.add(c);
		Collections.shuffle(lst);
		return lst;
	}
}
