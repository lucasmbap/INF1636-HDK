package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapa pequeno usado pelos testes. Tem a mesma estrutura do mapa oficial
 * (paradas simples e duplas, Karakorum, provincias, provincias do Khan e
 * cidades), mas com poucos elementos, para que cada teste seja simples.
 *
 * Rotas:
 *
 *   KARAKORUM --- A --- B(dupla, conselheiro) --- C --- E
 *       |                                               |
 *       +---------------------- D ----------------------+
 *
 * Regioes: A e B na Russia, C e E na China, D na Persia.
 *
 * Provincias:
 *   ESTEPE   (Russia, espada)          adjacente a A e B
 *   DESERTO  (China, moeda)            adjacente a C e E
 *   PLANALTO (Persia, yurt, do Khan)   adjacente a D e Karakorum
 *            setas do Khan -> ESTEPE e DESERTO
 *   VALE     (China, espada, do Khan)  adjacente a E
 *            setas do Khan -> PLANALTO e DESERTO
 *
 * Cidades: KIEV (Russia) adjacente a A; PEQUIM (China) adjacente a E.
 */
class MapaDeTeste {
	static final String K = Mapa.KARAKORUM;
	static final String A = "A";
	static final String B = "B";
	static final String C = "C";
	static final String D = "D";
	static final String E = "E";

	static final String ESTEPE = "Estepe";
	static final String DESERTO = "Deserto";
	static final String PLANALTO = "Planalto";
	static final String VALE = "Vale";

	static final String KIEV = "Kiev";
	static final String PEQUIM = "Pequim";

	// tesouros usados nos testes (classe Tesouro do pacote Model)
	static final Tesouro GADO = new Tesouro("Gado");
	static final Tesouro PELE = new Tesouro("Pele");
	static final Tesouro FERRO = new Tesouro("Ferro");
	static final Tesouro SEDA = new Tesouro("Seda");

	static Mapa criar() {
		Mapa m = new Mapa();

		m.adicionarParada(A, TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada(B, TipoParada.DUPLA, Regiao.RUSSIA, true);
		m.adicionarParada(C, TipoParada.SIMPLES, Regiao.CHINA, false);
		m.adicionarParada(D, TipoParada.SIMPLES, Regiao.PERSIA, false);
		m.adicionarParada(E, TipoParada.SIMPLES, Regiao.CHINA, false);

		m.conectarParadas(K, A);
		m.conectarParadas(A, B);
		m.conectarParadas(B, C);
		m.conectarParadas(C, E);
		m.conectarParadas(K, D);
		m.conectarParadas(D, E);

		m.adicionarProvincia(ESTEPE, Regiao.RUSSIA, TipoTributo.ESPADA, false);
		m.adicionarProvincia(DESERTO, Regiao.CHINA, TipoTributo.MOEDA, false);
		m.adicionarProvincia(PLANALTO, Regiao.PERSIA, TipoTributo.YURT, true);
		m.adicionarProvincia(VALE, Regiao.CHINA, TipoTributo.ESPADA, true);

		m.ligarProvincia(A, ESTEPE);
		m.ligarProvincia(B, ESTEPE);
		m.ligarProvincia(C, DESERTO);
		m.ligarProvincia(E, DESERTO);
		m.ligarProvincia(D, PLANALTO);
		m.ligarProvincia(K, PLANALTO);
		m.ligarProvincia(E, VALE);

		m.definirAlvosDoKhan(PLANALTO, ESTEPE, DESERTO);
		m.definirAlvosDoKhan(VALE, PLANALTO, DESERTO);

		m.adicionarCidade(KIEV, Regiao.RUSSIA);
		m.adicionarCidade(PEQUIM, Regiao.CHINA);
		m.ligarCidade(A, KIEV);
		m.ligarCidade(E, PEQUIM);

		return m;
	}

	// ----- auxiliares para montar caminhos e listas de tesouros -----

	static List<String> caminho(String p1) {
		List<String> c = new ArrayList<String>();
		c.add(p1);
		return c;
	}

	static List<String> caminho(String p1, String p2) {
		List<String> c = caminho(p1);
		c.add(p2);
		return c;
	}

	static List<String> caminho(String p1, String p2, String p3) {
		List<String> c = caminho(p1, p2);
		c.add(p3);
		return c;
	}

	static List<Tesouro> tesouros(Tesouro t1, Tesouro t2) {
		List<Tesouro> lst = new ArrayList<Tesouro>();
		lst.add(t1);
		lst.add(t2);
		return lst;
	}
}
