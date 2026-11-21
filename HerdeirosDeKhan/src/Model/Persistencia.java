package Model;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Salvar e recuperar a partida em arquivo de texto ASCII puro (4a iteracao).
 *
 * Formato: uma linha por informacao, campos separados por "|". Todo texto livre
 * (nomes, condicoes das cartas...) e gravado com URLEncoder (UTF-8), o que deixa o
 * arquivo em ASCII mesmo com acentos; null e gravado como "~". Exemplo:
 *
 *   HERDEIROS_DE_KHAN|1
 *   JOGADOR|1|Ana|vermelho|Jochi|0
 *   RECURSO|vermelho|ESPADA|2
 *   ...
 *   FIM
 *
 * Ao ler, o arquivo inteiro e validado e montado em objetos NOVOS; so se tudo estiver
 * correto a ModeloApi troca o estado atual por eles. Assim um arquivo invalido nunca
 * estraga a partida em andamento.
 */
final class Persistencia {
	private static final String CABECALHO = "HERDEIROS_DE_KHAN";
	private static final int VERSAO = 1;
	private static final String NULO = "~";

	private Persistencia() {
	}

	/** Estado completo do Model, trocado de uma vez ao recuperar. */
	static final class Estado {
		Map<Integer, Jogador> jogadores = new HashMap<Integer, Jogador>();
		int proximoIdJogador = 1;
		Mapa mapa;
		List<Jogador> ordemJogadores = new ArrayList<Jogador>();
		Partida partida;
		boolean influenciaPontuada;
	}

	// =====================================================================
	// Gravar
	// =====================================================================

	static void gravar(Estado e, Path arquivo) throws IOException {
		if (e.partida == null)
			throw new IOException("nao ha partida em andamento para salvar.");
		StringBuilder sb = new StringBuilder();
		linha(sb, CABECALHO, String.valueOf(VERSAO));
		linha(sb, "PROXIMO_ID", String.valueOf(e.proximoIdJogador));
		linha(sb, "PARTIDA_ATUAL", bool(e.influenciaPontuada), String.valueOf(e.partida.getIndiceAtual()),
				String.valueOf(e.partida.getVotosNeutra()), bool(e.partida.isFimDisparado()),
				bool(e.partida.isUltimaRodada()), bool(e.partida.isEncerrada()), bool(e.partida.isPontuada()));

		Map<Jogador, Integer> idDe = new HashMap<Jogador, Integer>();
		for (Map.Entry<Integer, Jogador> en : e.jogadores.entrySet())
			idDe.put(en.getValue(), en.getKey());

		for (Jogador j : e.ordemJogadores) {
			String cor = j.getCor();
			linha(sb, "JOGADOR", String.valueOf(idDe.get(j)), j.getNome(), cor, j.getHerdeiro(),
					String.valueOf(j.getVotos()));
			for (TipoTributo t : TipoTributo.values())
				if (j.getQuantidadeRecurso(t) > 0)
					linha(sb, "RECURSO", cor, t.name(), String.valueOf(j.getQuantidadeRecurso(t)));
			for (Tesouro t : j.getTesouros())
				linha(sb, "TESOURO", cor, t.getNome());
			for (CartaConselheiro c : j.getCartasConselheiro())
				linha(sb, "CARTA", cor, c.getNome(), c.getCondicao(), c.getRecompensa(), bool(c.isAberta()));
			for (PecaMelhoria m : j.getMelhorias())
				linha(sb, "MELHORIA", cor, m.getNome(), m.getEfeito(), m.getCondicao());

			Cartela ct = e.partida.getCartela(cor);
			StringBuilder oc = new StringBuilder();
			for (ColunaCartela c : ct.getOcupadas())
				oc.append(oc.length() == 0 ? "" : ",").append(c.ordinal());
			linha(sb, "CARTELA", cor, oc.toString(), String.valueOf(ct.getMelhoriasGratisRecebidas()));
			for (ColunaCartela c : ColunaCartela.values()) {
				StringBuilder tipos = new StringBuilder();
				for (TipoMelhoria t : ct.getMelhorias(c))
					tipos.append(tipos.length() == 0 ? "" : ",").append(t == null ? "?" : t.name());
				linha(sb, "COLUNA", cor, String.valueOf(c.ordinal()), tipos.toString(),
						String.valueOf(ct.getNovasNoTurno(c)));
			}
		}

		Partida p = e.partida;
		for (String b : p.getConselheirosComBonus())
			linha(sb, "BONUS", b);
		for (Map.Entry<String, String> b : new java.util.TreeMap<String, String>(p.getTiposBonusConselheiros()).entrySet())
			linha(sb, "BONUS_TIPO", b.getKey(), b.getValue());
		for (String c : p.getCidadesFechadas())
			linha(sb, "FECHADA", c);
		for (Tesouro t : p.getPilhaTesouros())
			linha(sb, "PILHA", t.getNome());
		for (String c : p.getCartasAbertas())
			linha(sb, "CARTA_ABERTA", c);
		for (Map.Entry<String, Integer> b : new java.util.TreeMap<String, Integer>(p.getBonusPorJogador()).entrySet())
			linha(sb, "BONUS_DE", b.getKey(), String.valueOf(b.getValue()));
		linha(sb, "TURNO", bool(p.isTurnoAtivo()), String.valueOf(p.getMovimentosRestantes()),
				String.valueOf(p.getPunhosRestantes()),
				String.valueOf(p.getColunaAtivada() == null ? -1 : p.getColunaAtivada().ordinal()),
				bool(p.isKhanPendente()));
		for (String v : p.getParadasVisitadas())
			linha(sb, "VISITADA", v);
		for (Map.Entry<String, Integer> t : new java.util.TreeMap<String, Integer>(p.getTesourosTomadosNoTurno())
				.entrySet())
			linha(sb, "TOMADOS", t.getKey(), String.valueOf(t.getValue()));
		for (TipoTributo t : TipoTributo.values())
			if (p.getVirtuais().containsKey(t) && p.getVirtuais().get(t) > 0)
				linha(sb, "VIRTUAL", t.name(), String.valueOf(p.getVirtuais().get(t)));
		for (String t : p.getTesourosVirtuais())
			linha(sb, "TESOURO_VIRTUAL", t);

		// area de melhorias (5 espacos + pilha) e habilidades dos herdeiros
		AreaDeMelhorias area = p.getArea();
		for (int i = 0; i < AreaDeMelhorias.ESPACOS; i++) {
			MelhoriaColorida mc = area.getEspaco(i);
			linha(sb, "AREA", String.valueOf(i), mc == null ? null : mc.codigo());
		}
		for (MelhoriaColorida mc : area.getPilha())
			linha(sb, "PILHA_MELHORIA", mc.codigo());
		linha(sb, "HABILIDADE", bool(p.isHabilidadeUsadaNoTurno()));
		for (Jogador j : e.ordemJogadores) {
			if (p.getPeaoEscolhido(j.getCor()) > 0)
				linha(sb, "PEAO_ESCOLHIDO", j.getCor(), String.valueOf(p.getPeaoEscolhido(j.getCor())));
			for (MelhoriaColorida mc : p.getMelhoriasIniciaisJochi(j.getCor()))
				linha(sb, "JOCHI_INICIAL", j.getCor(), mc.codigo());
		}

		Mapa m = e.mapa;
		List<String> exigidos = m.getRequisitosKarakorum();
		if (exigidos.size() == 3)
			linha(sb, "KARAKORUM", exigidos.get(0), exigidos.get(1), exigidos.get(2));
		for (Jogador j : e.ordemJogadores) {
			linha(sb, "PEAO", j.getCor(), m.getPosicao(j.getCor()));
			String segundo = j.getCor() + Mapa.SUFIXO_SEGUNDO_PEAO; // so a Altani tem
			if (m.getPosicao(segundo) != null)
				linha(sb, "PEAO", segundo, m.getPosicao(segundo));
		}
		for (String parada : m.getNomesParadasOrdenados())
			for (String cor : m.getYurtsNaParada(parada))
				linha(sb, "YURT", parada, cor);
		List<String> provincias = m.getNomesProvincias();
		Collections.sort(provincias);
		for (String nome : provincias)
			linha(sb, "PROVINCIA", nome, String.valueOf(m.getProvincia(nome).getQuantidade()));
		List<String> cidades = m.getNomesCidades();
		Collections.sort(cidades);
		for (String nome : cidades) {
			Cidade c = m.getCidade(nome);
			StringBuilder ts = new StringBuilder();
			for (Tesouro t : c.getTesouros())
				ts.append(ts.length() == 0 ? "" : ",").append(enc(t.getNome()));
			linha(sb, "CIDADE", nome, bool(c.isRevelada()), c.getConquistador(), ts.toString());
		}
		linha(sb, "KHAN", m.getPosicaoKhan());
		sb.append("FIM\n");
		Files.write(arquivo, sb.toString().getBytes(StandardCharsets.US_ASCII));
	}

	private static String bool(boolean b) {
		return b ? "1" : "0";
	}

	private static String enc(String s) {
		return s == null ? NULO : URLEncoder.encode(s, StandardCharsets.UTF_8);
	}

	private static void linha(StringBuilder sb, String chave, String... campos) {
		sb.append(chave);
		for (String c : campos) {
			sb.append('|');
			// listas ja separadas por virgula e o indice de coluna nao precisam de escape; texto livre, sim
			sb.append(ehSimples(chave, c) ? c : enc(c));
		}
		sb.append('\n');
	}

	/** Campos que so tem letras, numeros, virgula e "?" (listas de tipos, numeros) ficam como estao. */
	private static boolean ehSimples(String chave, String c) {
		if (c == null)
			return false;
		if (!(chave.equals("COLUNA") || chave.equals("CARTELA") || chave.equals("CIDADE")
				|| chave.equals("PARTIDA_ATUAL") || chave.equals("TURNO") || chave.equals("HERDEIROS_DE_KHAN")
				|| chave.equals("PROXIMO_ID") || chave.equals("RECURSO") || chave.equals("VIRTUAL")
				|| chave.equals("TOMADOS") || chave.equals("PROVINCIA")))
			return false;
		return c.matches("[A-Za-z0-9_,?%+.-]*");
	}

	// =====================================================================
	// Ler
	// =====================================================================

	/** Le e valida o arquivo; devolve o estado novo ou lanca IOException dizendo o que esta errado. */
	static Estado ler(Path arquivo) throws IOException {
		List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.US_ASCII);
		try {
			return montar(linhas);
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new IOException("arquivo de partida invalido (" + e.getMessage() + ").");
		}
	}

	private static String dec(String s) {
		return NULO.equals(s) ? null : URLDecoder.decode(s, StandardCharsets.UTF_8);
	}

	private static int inteiro(String s) {
		return Integer.parseInt(s);
	}

	private static boolean booleano(String s) {
		if ("1".equals(s))
			return true;
		if ("0".equals(s))
			return false;
		throw new IllegalArgumentException("valor logico invalido: " + s);
	}

	private static List<String> csv(String s) {
		List<String> lst = new ArrayList<String>();
		if (s != null && !s.isEmpty())
			for (String x : s.split(",", -1))
				lst.add(x);
		return lst;
	}

	private static Estado montar(List<String> linhas) throws IOException {
		if (linhas.isEmpty() || !linhas.get(0).equals(CABECALHO + "|" + VERSAO))
			throw new IOException("este arquivo nao e uma partida salva do Herdeiros de Khan.");
		if (!linhas.get(linhas.size() - 1).equals("FIM"))
			throw new IOException("o arquivo esta incompleto (falta a linha FIM).");

		Estado est = new Estado();
		Map<String, Jogador> porCor = new LinkedHashMap<String, Jogador>();
		Map<String, Integer> idDaCor = new HashMap<String, Integer>();
		int indice = 0, votosNeutra = 0;
		boolean influencia = false, fim = false, ultima = false, terminada = false, pontuada = false;
		List<String> bonus = new ArrayList<String>();
		Map<String, String> bonusTipos = new HashMap<String, String>();
		Deque<String> fechadas = new ArrayDeque<String>();
		Deque<Tesouro> pilha = new ArrayDeque<Tesouro>();
		List<String> cartasAbertas = new ArrayList<String>();
		Map<String, Integer> bonusDe = new HashMap<String, Integer>();
		boolean ativo = false, khanPendente = false;
		int mov = 0, punhos = 0, colunaAtivada = -1;
		List<String> visitadas = new ArrayList<String>();
		Map<String, Integer> tomados = new HashMap<String, Integer>();
		Map<TipoTributo, Integer> virtuais = new EnumMap<TipoTributo, Integer>(TipoTributo.class);
		List<String> tesVirtuais = new ArrayList<String>();
		List<String> karakorum = null;
		Map<String, String> peoes = new LinkedHashMap<String, String>();
		Map<String, List<String>> yurts = new HashMap<String, List<String>>();
		Map<String, Integer> tributos = new HashMap<String, Integer>();
		List<String[]> cidadesSalvas = new ArrayList<String[]>();
		String khan = null;
		boolean temKhan = false;
		MelhoriaColorida[] area = new MelhoriaColorida[AreaDeMelhorias.ESPACOS];
		boolean temArea = false; // arquivos antigos nao tem a area: ela fica sorteada
		List<MelhoriaColorida> pilhaMelhorias = new ArrayList<MelhoriaColorida>();
		boolean habilidadeUsada = false;
		Map<String, Integer> peaoEscolhido = new HashMap<String, Integer>();
		Map<String, List<MelhoriaColorida>> iniciaisJochi = new HashMap<String, List<MelhoriaColorida>>();
		// por cor: ocupadas/gratis e colunas
		Map<String, List<ColunaCartela>> ocupadas = new HashMap<String, List<ColunaCartela>>();
		Map<String, Integer> gratis = new HashMap<String, Integer>();
		Map<String, Map<ColunaCartela, List<TipoMelhoria>>> melhorias = new HashMap<String, Map<ColunaCartela, List<TipoMelhoria>>>();
		Map<String, Map<ColunaCartela, Integer>> novas = new HashMap<String, Map<ColunaCartela, Integer>>();

		for (int i = 1; i < linhas.size() - 1; i++) {
			String[] f = linhas.get(i).split("\\|", -1);
			String chave = f[0];
			switch (chave) {
			case "PROXIMO_ID":
				est.proximoIdJogador = inteiro(f[1]);
				break;
			case "PARTIDA_ATUAL":
				influencia = booleano(f[1]);
				indice = inteiro(f[2]);
				votosNeutra = inteiro(f[3]);
				fim = booleano(f[4]);
				ultima = booleano(f[5]);
				terminada = booleano(f[6]);
				pontuada = booleano(f[7]);
				break;
			case "JOGADOR": {
				int id = inteiro(f[1]);
				Jogador j = new Jogador(dec(f[2]), dec(f[3]), dec(f[4]));
				if (Cartela.nomeOficial(j.getHerdeiro()) == null)
					throw new IOException("herdeiro desconhecido: " + j.getHerdeiro());
				if (porCor.containsKey(j.getCor()) || est.jogadores.containsKey(id))
					throw new IOException("jogador repetido: " + j.getCor());
				j.adicionarVotos(inteiro(f[5]));
				porCor.put(j.getCor(), j);
				idDaCor.put(j.getCor(), id);
				est.jogadores.put(id, j);
				est.ordemJogadores.add(j);
				break;
			}
			case "RECURSO":
				jogador(porCor, f[1]).adicionarRecursos(TipoTributo.valueOf(f[2]), inteiro(f[3]));
				break;
			case "TESOURO":
				jogador(porCor, f[1]).adicionarTesouro(new Tesouro(dec(f[2])));
				break;
			case "CARTA":
				jogador(porCor, f[1])
						.adicionarCartaConselheiro(new CartaConselheiro(dec(f[2]), dec(f[3]), dec(f[4]), booleano(f[5])));
				break;
			case "MELHORIA":
				jogador(porCor, f[1]).adicionarMelhoria(new PecaMelhoria(dec(f[2]), dec(f[3]), dec(f[4])));
				break;
			case "CARTELA": {
				jogador(porCor, f[1]);
				List<ColunaCartela> oc = new ArrayList<ColunaCartela>();
				for (String x : csv(f[2]))
					oc.add(ColunaCartela.values()[inteiro(x)]);
				ocupadas.put(f[1], oc);
				gratis.put(f[1], inteiro(f[3]));
				break;
			}
			case "COLUNA": {
				jogador(porCor, f[1]);
				ColunaCartela c = ColunaCartela.values()[inteiro(f[2])];
				List<TipoMelhoria> tipos = new ArrayList<TipoMelhoria>();
				for (String x : csv(f[3])) {
					if (x.equals("?"))
						tipos.add(null);
					else {
						TipoMelhoria t = TipoMelhoria.porNome(x);
						if (t == null)
							throw new IOException("melhoria desconhecida: " + x);
						tipos.add(t);
					}
				}
				if (!melhorias.containsKey(f[1])) {
					melhorias.put(f[1], new EnumMap<ColunaCartela, List<TipoMelhoria>>(ColunaCartela.class));
					novas.put(f[1], new EnumMap<ColunaCartela, Integer>(ColunaCartela.class));
				}
				melhorias.get(f[1]).put(c, tipos);
				novas.get(f[1]).put(c, inteiro(f[4]));
				break;
			}
			case "BONUS":
				bonus.add(dec(f[1]));
				break;
			case "BONUS_TIPO":
				bonusTipos.put(dec(f[1]), dec(f[2]));
				break;
			case "FECHADA":
				fechadas.add(dec(f[1]));
				break;
			case "PILHA":
				pilha.add(new Tesouro(dec(f[1])));
				break;
			case "CARTA_ABERTA":
				cartasAbertas.add(dec(f[1]));
				break;
			case "BONUS_DE":
				bonusDe.put(dec(f[1]), inteiro(f[2]));
				break;
			case "TURNO":
				ativo = booleano(f[1]);
				mov = inteiro(f[2]);
				punhos = inteiro(f[3]);
				colunaAtivada = inteiro(f[4]);
				khanPendente = booleano(f[5]);
				break;
			case "VISITADA":
				visitadas.add(dec(f[1]));
				break;
			case "TOMADOS":
				tomados.put(dec(f[1]), inteiro(f[2]));
				break;
			case "VIRTUAL":
				virtuais.put(TipoTributo.valueOf(f[1]), inteiro(f[2]));
				break;
			case "TESOURO_VIRTUAL":
				tesVirtuais.add(dec(f[1]));
				break;
			case "KARAKORUM":
				karakorum = new ArrayList<String>();
				karakorum.add(dec(f[1]));
				karakorum.add(dec(f[2]));
				karakorum.add(dec(f[3]));
				break;
			case "PEAO":
				peoes.put(dec(f[1]), dec(f[2]));
				break;
			case "YURT":
				if (!yurts.containsKey(dec(f[1])))
					yurts.put(dec(f[1]), new ArrayList<String>());
				yurts.get(dec(f[1])).add(dec(f[2]));
				break;
			case "PROVINCIA":
				tributos.put(dec(f[1]), inteiro(f[2]));
				break;
			case "CIDADE":
				cidadesSalvas.add(f);
				break;
			case "KHAN":
				khan = dec(f[1]);
				temKhan = true;
				break;
			case "AREA": {
				int i2 = inteiro(f[1]);
				String cod = dec(f[2]);
				area[i2] = cod == null ? null : MelhoriaColorida.deCodigo(cod);
				temArea = true;
				break;
			}
			case "PILHA_MELHORIA":
				pilhaMelhorias.add(MelhoriaColorida.deCodigo(dec(f[1])));
				break;
			case "HABILIDADE":
				habilidadeUsada = booleano(f[1]);
				break;
			case "PEAO_ESCOLHIDO":
				jogador(porCor, f[1]);
				peaoEscolhido.put(dec(f[1]), inteiro(f[2]));
				break;
			case "JOCHI_INICIAL":
				jogador(porCor, f[1]);
				if (!iniciaisJochi.containsKey(dec(f[1])))
					iniciaisJochi.put(dec(f[1]), new ArrayList<MelhoriaColorida>());
				iniciaisJochi.get(dec(f[1])).add(MelhoriaColorida.deCodigo(dec(f[2])));
				break;
			default:
				throw new IOException("linha desconhecida: " + chave);
			}
		}

		int n = est.ordemJogadores.size();
		if (n < 2 || n > 5)
			throw new IOException("a partida salva precisa ter de 2 a 5 jogadores.");
		if (karakorum == null || !temKhan)
			throw new IOException("faltam informacoes do tabuleiro no arquivo.");
		for (Jogador j : est.ordemJogadores)
			if (!ocupadas.containsKey(j.getCor()) || melhorias.get(j.getCor()) == null
					|| melhorias.get(j.getCor()).size() != ColunaCartela.values().length)
				throw new IOException("falta a cartela do jogador " + j.getNome() + ".");
		for (Integer id : est.jogadores.keySet())
			if (id >= est.proximoIdJogador)
				throw new IOException("identificadores de jogador inconsistentes.");

		// mapa novo, com o estado do arquivo
		Mapa mapa = MapaOficial.criar();
		if (!mapa.configurarPecaKarakorum(new PecaKarakorum(karakorum)))
			throw new IOException("peca de Karakorum invalida.");
		for (Map.Entry<String, Integer> t : tributos.entrySet()) {
			Provincia p = mapa.getProvincia(t.getKey());
			if (p == null || t.getValue() < 0 || t.getValue() > Provincia.MAX_TRIBUTOS)
				throw new IOException("provincia invalida: " + t.getKey());
			for (int k = 0; k < t.getValue(); k++)
				p.adicionarTributo();
		}
		for (String[] f : cidadesSalvas) {
			Cidade c = mapa.getCidade(dec(f[1]));
			if (c == null)
				throw new IOException("cidade invalida: " + dec(f[1]));
			List<Tesouro> ts = new ArrayList<Tesouro>();
			for (String x : csv(f[4]))
				ts.add(new Tesouro(dec(x)));
			String conquistador = dec(f[3]);
			if (conquistador != null && !porCor.containsKey(conquistador))
				throw new IOException("conquistador desconhecido: " + conquistador);
			c.restaurar(booleano(f[2]), ts, conquistador);
		}

		Partida partida = new Partida(mapa, est.ordemJogadores, fechadas, pilha);
		mapa.restaurarPosicoes(peoes, yurts, khan);
		for (Jogador j : est.ordemJogadores)
			if (mapa.getPosicao(j.getCor()) == null)
				throw new IOException("falta o peao de " + j.getNome() + ".");
		for (Jogador j : est.ordemJogadores) {
			String cor = j.getCor();
			partida.getCartela(cor).restaurar(ocupadas.get(cor), gratis.get(cor), melhorias.get(cor), novas.get(cor));
		}
		for (String b : bonus)
			if (!mapa.temConselheiro(b))
				throw new IOException("conselheiro invalido: " + b);
		partida.restaurarCartasAbertas(cartasAbertas, bonusDe);
		partida.restaurarTiposBonusConselheiros(bonusTipos);
		ColunaCartela coluna = null;
		if (colunaAtivada >= 0)
			coluna = ColunaCartela.values()[colunaAtivada];
		partida.restaurar(indice, votosNeutra, fim, ultima, terminada, pontuada, bonus, ativo, mov, punhos,
				visitadas, tomados, coluna, khanPendente, virtuais, tesVirtuais);
		if (temArea)
			partida.restaurarHabilidades(area, pilhaMelhorias, habilidadeUsada, peaoEscolhido, iniciaisJochi);

		est.mapa = mapa;
		est.partida = partida;
		est.influenciaPontuada = influencia;
		return est;
	}

	private static Jogador jogador(Map<String, Jogador> porCor, String campo) throws IOException {
		Jogador j = porCor.get(URLDecoder.decode(campo, StandardCharsets.UTF_8));
		if (j == null)
			throw new IOException("linha de um jogador que nao foi declarado: " + campo);
		return j;
	}
}
