package Model;

/**
 * Monta o mapa oficial do Herdeiros de Khan, conforme o tabuleiro.
 *
 * Nomes das paradas: R1..R11 (Russia), C1..C11 (China), P1..P11 (Persia)
 * e Karakorum. A posicao de cada parada na imagem do tabuleiro esta na
 * classe View.CoordenadasMapa.
 *
 * Paradas duplas (todas com conselheiro):
 *   R2   pede 2 sedas
 *   R11  pede 2 ferros
 *   C5   pede 2 gados
 *   C10  pede 3 tesouros de qualquer tipo
 *   P1   pede 2 peles
 *   P8   pede 2 especiarias
 *   Karakorum pede 3 tesouros especificos (peca de Karakorum)
 */
class MapaOficial {

	static Mapa criar() {
		Mapa m = new Mapa();

		adicionarParadas(m);
		adicionarRotas(m);
		adicionarProvincias(m);
		adicionarCidades(m);
		return m;
	}

	private static void adicionarParadas(Mapa m) {
		m.adicionarParada("R1", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada("R2", TipoParada.DUPLA, Regiao.RUSSIA, true);
		m.adicionarParada("R3", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada("R4", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada("R5", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada("R6", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada("R7", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada("R8", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada("R9", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada("R10", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		m.adicionarParada("R11", TipoParada.DUPLA, Regiao.RUSSIA, true);

		m.adicionarParada("C1", TipoParada.SIMPLES, Regiao.CHINA, false);
		m.adicionarParada("C2", TipoParada.SIMPLES, Regiao.CHINA, false);
		m.adicionarParada("C3", TipoParada.SIMPLES, Regiao.CHINA, false);
		m.adicionarParada("C4", TipoParada.SIMPLES, Regiao.CHINA, false);
		m.adicionarParada("C5", TipoParada.DUPLA, Regiao.CHINA, true);
		m.adicionarParada("C6", TipoParada.SIMPLES, Regiao.CHINA, false);
		m.adicionarParada("C7", TipoParada.SIMPLES, Regiao.CHINA, false);
		m.adicionarParada("C8", TipoParada.SIMPLES, Regiao.CHINA, false);
		m.adicionarParada("C9", TipoParada.SIMPLES, Regiao.CHINA, false);
		m.adicionarParada("C10", TipoParada.DUPLA, Regiao.CHINA, true);
		m.adicionarParada("C11", TipoParada.SIMPLES, Regiao.CHINA, false);

		m.adicionarParada("P1", TipoParada.DUPLA, Regiao.PERSIA, true);
		m.adicionarParada("P2", TipoParada.SIMPLES, Regiao.PERSIA, false);
		m.adicionarParada("P3", TipoParada.SIMPLES, Regiao.PERSIA, false);
		m.adicionarParada("P4", TipoParada.SIMPLES, Regiao.PERSIA, false);
		m.adicionarParada("P5", TipoParada.SIMPLES, Regiao.PERSIA, false);
		m.adicionarParada("P6", TipoParada.SIMPLES, Regiao.PERSIA, false);
		m.adicionarParada("P7", TipoParada.SIMPLES, Regiao.PERSIA, false);
		m.adicionarParada("P8", TipoParada.DUPLA, Regiao.PERSIA, true);
		m.adicionarParada("P9", TipoParada.SIMPLES, Regiao.PERSIA, false);
		m.adicionarParada("P10", TipoParada.SIMPLES, Regiao.PERSIA, false);
		m.adicionarParada("P11", TipoParada.SIMPLES, Regiao.PERSIA, false);

	}

	private static void adicionarRotas(Mapa m) {
		m.conectarParadas("R1", "R2");
		m.conectarParadas("R1", "R3");
		m.conectarParadas("R1", "R4");
		m.conectarParadas("R3", "R4");
		m.conectarParadas("R1", "R5");
		m.conectarParadas("R2", "R6");
		m.conectarParadas("R5", "R6");
		m.conectarParadas("R5", "R7");
		m.conectarParadas("R6", "R7");
		m.conectarParadas("R6", "R9");
		m.conectarParadas("R8", "R9");
		m.conectarParadas("R8", "R10");
		m.conectarParadas("R9", "R10");
		m.conectarParadas("R10", "R11");
		m.conectarParadas("R9", Mapa.KARAKORUM);
		m.conectarParadas("R11", Mapa.KARAKORUM);
		m.conectarParadas("R11", "C1");
		m.conectarParadas("R4", "P1");
		m.conectarParadas("R5", "P1");
		m.conectarParadas("R7", "P2");
		m.conectarParadas("C1", "C2");
		m.conectarParadas("C1", "C3");
		m.conectarParadas("C2", "C3");
		m.conectarParadas("C3", "C4");
		m.conectarParadas("C3", "C5");
		m.conectarParadas("C4", Mapa.KARAKORUM);
		m.conectarParadas("C4", "C6");
		m.conectarParadas("C4", "C7");
		m.conectarParadas("C6", "C7");
		m.conectarParadas("C5", "C8");
		m.conectarParadas("C7", "C8");
		m.conectarParadas("C6", "C10");
		m.conectarParadas("C10", "C9");
		m.conectarParadas("C9", "C8");
		m.conectarParadas("C8", "C11");
		m.conectarParadas("C9", "C11");
		m.conectarParadas("P9", "C6");
		m.conectarParadas("P10", "C10");
		m.conectarParadas("P1", "P2");
		m.conectarParadas("P1", "P4");
		m.conectarParadas("P2", "P3");
		m.conectarParadas("P2", "P5");
		m.conectarParadas("P3", "P5");
		m.conectarParadas("P3", "P4");
		m.conectarParadas("P5", Mapa.KARAKORUM);
		m.conectarParadas("P4", "P6");
		m.conectarParadas("P4", "P7");
		m.conectarParadas("P6", "P7");
		m.conectarParadas("P6", "P8");
		m.conectarParadas("P8", "P9");
		m.conectarParadas("P5", "P9");
		m.conectarParadas("P9", "P10");
		m.conectarParadas("P9", "P11");
		m.conectarParadas("P10", "P11");
	}

	private static void adicionarProvincias(Mapa m) {
		m.adicionarProvincia("Yurt Kiev", Regiao.RUSSIA, TipoTributo.YURT, true);
		m.adicionarProvincia("Espada Moscou", Regiao.RUSSIA, TipoTributo.ESPADA, false);
		m.adicionarProvincia("Moeda Moscou", Regiao.RUSSIA, TipoTributo.MOEDA, false);
		m.adicionarProvincia("Yurt Karakorum Norte", Regiao.RUSSIA, TipoTributo.YURT, false);
		m.adicionarProvincia("Moeda Sarai", Regiao.RUSSIA, TipoTributo.MOEDA, true);
		m.adicionarProvincia("Espada Kaesong", Regiao.CHINA, TipoTributo.ESPADA, false);
		m.adicionarProvincia("Moeda Pequim", Regiao.CHINA, TipoTributo.MOEDA, false);
		m.adicionarProvincia("Yurt Cantao", Regiao.CHINA, TipoTributo.YURT, true);
		m.adicionarProvincia("Espada Cabul", Regiao.CHINA, TipoTributo.ESPADA, false);
		m.adicionarProvincia("Yurt Karakorum Sul", Regiao.PERSIA, TipoTributo.YURT, false);
		m.adicionarProvincia("Moeda Bagda", Regiao.PERSIA, TipoTributo.MOEDA, true);
		m.adicionarProvincia("Espada Samarcanda", Regiao.PERSIA, TipoTributo.ESPADA, false);

		m.ligarProvincia("R1", "Yurt Kiev");
		m.ligarProvincia("R4", "Yurt Kiev");
		m.ligarProvincia("R5", "Yurt Kiev");
		m.ligarProvincia("P1", "Yurt Kiev");

		m.ligarProvincia("R1", "Espada Moscou");
		m.ligarProvincia("R2", "Espada Moscou");
		m.ligarProvincia("R5", "Espada Moscou");
		m.ligarProvincia("R6", "Espada Moscou");

		m.ligarProvincia("R5", "Moeda Moscou");
		m.ligarProvincia("P1", "Moeda Moscou");
		m.ligarProvincia("P2", "Moeda Moscou");
		m.ligarProvincia("R7", "Moeda Moscou");

		m.ligarProvincia("R6", "Yurt Karakorum Norte");
		m.ligarProvincia("R7", "Yurt Karakorum Norte");
		m.ligarProvincia("P2", "Yurt Karakorum Norte");
		m.ligarProvincia("P5", "Yurt Karakorum Norte");
		m.ligarProvincia(Mapa.KARAKORUM, "Yurt Karakorum Norte");
		m.ligarProvincia("R9", "Yurt Karakorum Norte");

		m.ligarProvincia("R9", "Moeda Sarai");
		m.ligarProvincia("R10", "Moeda Sarai");
		m.ligarProvincia("R11", "Moeda Sarai");
		m.ligarProvincia(Mapa.KARAKORUM, "Moeda Sarai");

		m.ligarProvincia("R11", "Espada Kaesong");
		m.ligarProvincia(Mapa.KARAKORUM, "Espada Kaesong");
		m.ligarProvincia("C4", "Espada Kaesong");
		m.ligarProvincia("C3", "Espada Kaesong");
		m.ligarProvincia("C1", "Espada Kaesong");

		m.ligarProvincia("C3", "Moeda Pequim");
		m.ligarProvincia("C5", "Moeda Pequim");
		m.ligarProvincia("C8", "Moeda Pequim");
		m.ligarProvincia("C7", "Moeda Pequim");
		m.ligarProvincia("C4", "Moeda Pequim");

		m.ligarProvincia("C6", "Yurt Cantao");
		m.ligarProvincia("C7", "Yurt Cantao");
		m.ligarProvincia("C8", "Yurt Cantao");
		m.ligarProvincia("C9", "Yurt Cantao");
		m.ligarProvincia("C10", "Yurt Cantao");

		m.ligarProvincia("C6", "Espada Cabul");
		m.ligarProvincia("C10", "Espada Cabul");
		m.ligarProvincia("P10", "Espada Cabul");
		m.ligarProvincia("P9", "Espada Cabul");

		m.ligarProvincia("P5", "Yurt Karakorum Sul");
		m.ligarProvincia("P9", "Yurt Karakorum Sul");
		m.ligarProvincia("C6", "Yurt Karakorum Sul");
		m.ligarProvincia("C4", "Yurt Karakorum Sul");
		m.ligarProvincia(Mapa.KARAKORUM, "Yurt Karakorum Sul");

		m.ligarProvincia("P3", "Moeda Bagda");
		m.ligarProvincia("P5", "Moeda Bagda");
		m.ligarProvincia("P9", "Moeda Bagda");
		m.ligarProvincia("P8", "Moeda Bagda");
		m.ligarProvincia("P6", "Moeda Bagda");
		m.ligarProvincia("P4", "Moeda Bagda");

		m.ligarProvincia("P1", "Espada Samarcanda");
		m.ligarProvincia("P4", "Espada Samarcanda");
		m.ligarProvincia("P3", "Espada Samarcanda");
		m.ligarProvincia("P2", "Espada Samarcanda");

		// setas das cabecas do Khan: a propria provincia e mais estas 2
		m.definirAlvosDoKhan("Yurt Kiev", "Espada Moscou", "Moeda Moscou");
		m.definirAlvosDoKhan("Moeda Sarai", "Yurt Karakorum Norte", "Espada Kaesong");
		m.definirAlvosDoKhan("Yurt Cantao", "Moeda Pequim", "Espada Cabul");
		m.definirAlvosDoKhan("Moeda Bagda", "Espada Samarcanda", "Yurt Karakorum Sul");
	}

	private static void adicionarCidades(Mapa m) {
		m.adicionarCidade("Kiev", Regiao.RUSSIA);
		m.ligarCidade("R1", "Kiev");
		m.ligarCidade("R3", "Kiev");
		m.ligarCidade("R4", "Kiev");
		m.adicionarCidade("Moscou", Regiao.RUSSIA);
		m.ligarCidade("R5", "Moscou");
		m.ligarCidade("R6", "Moscou");
		m.ligarCidade("R7", "Moscou");
		m.adicionarCidade("Sarai", Regiao.RUSSIA);
		m.ligarCidade("R8", "Sarai");
		m.ligarCidade("R9", "Sarai");
		m.ligarCidade("R10", "Sarai");
		m.adicionarCidade("Kaesong", Regiao.CHINA);
		m.ligarCidade("C1", "Kaesong");
		m.ligarCidade("C2", "Kaesong");
		m.ligarCidade("C3", "Kaesong");
		m.adicionarCidade("Pequim", Regiao.CHINA);
		m.ligarCidade("C4", "Pequim");
		m.ligarCidade("C6", "Pequim");
		m.ligarCidade("C7", "Pequim");
		m.adicionarCidade("Cantao", Regiao.CHINA);
		m.ligarCidade("C8", "Cantao");
		m.ligarCidade("C9", "Cantao");
		m.ligarCidade("C11", "Cantao");
		m.adicionarCidade("Samarcanda", Regiao.PERSIA);
		m.ligarCidade("P2", "Samarcanda");
		m.ligarCidade("P3", "Samarcanda");
		m.ligarCidade("P5", "Samarcanda");
		m.adicionarCidade("Bagda", Regiao.PERSIA);
		m.ligarCidade("P4", "Bagda");
		m.ligarCidade("P6", "Bagda");
		m.ligarCidade("P7", "Bagda");
		m.adicionarCidade("Cabul", Regiao.PERSIA);
		m.ligarCidade("P9", "Cabul");
		m.ligarCidade("P10", "Cabul");
		m.ligarCidade("P11", "Cabul");
	}
}
