package Model;

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Mapa de teste:  K - A - B - C   e   K - D
 * A: simples (Russia), B: dupla (Russia), C: simples (China), D: simples (Persia)
 * P1 (moeda) e adjacente a A e B; P4 (espada) so a A.
 * Cid1 (Russia, 3 tesouros) e adjacente a B; Cid3 (Persia) esta fechada.
 */
public class PartidaTest {
	private Mapa mapa;
	private Partida partida;
	private Deque<String> fechadas;
	private Deque<Tesouro> pilha;
	private Jogador ana;
	private Jogador bruno;

	@Before
	public void setUp() {
		mapa = new Mapa();
		mapa.adicionarParada("A", TipoParada.SIMPLES, Regiao.RUSSIA, false);
		mapa.adicionarParada("B", TipoParada.DUPLA, Regiao.RUSSIA, true);
		mapa.adicionarParada("C", TipoParada.SIMPLES, Regiao.CHINA, false);
		mapa.adicionarParada("D", TipoParada.SIMPLES, Regiao.PERSIA, false);
		mapa.conectarParadas(Mapa.KARAKORUM, "A");
		mapa.conectarParadas("A", "B");
		mapa.conectarParadas("B", "C");
		mapa.conectarParadas(Mapa.KARAKORUM, "D");

		mapa.adicionarProvincia("P1", Regiao.RUSSIA, TipoTributo.MOEDA, false);
		mapa.adicionarProvincia("P4", Regiao.RUSSIA, TipoTributo.ESPADA, false);
		mapa.ligarProvincia("A", "P1");
		mapa.ligarProvincia("B", "P1");
		mapa.ligarProvincia("A", "P4");

		mapa.adicionarCidade("Cid1", Regiao.RUSSIA);
		mapa.adicionarCidade("Cid3", Regiao.PERSIA);
		mapa.ligarCidade("B", "Cid1");
		List<Tesouro> iniciais = new ArrayList<Tesouro>();
		iniciais.add(new Tesouro("ferro"));
		iniciais.add(new Tesouro("seda"));
		iniciais.add(new Tesouro("gado"));
		mapa.revelarCidade("Cid1", iniciais);

		fechadas = new ArrayDeque<String>();
		fechadas.add("Cid3");
		pilha = new ArrayDeque<Tesouro>();
		for (String n : new String[] { "t1", "t2", "t3", "t4" })
			pilha.add(new Tesouro(n));

		ana = new Jogador("Ana", "vermelho", "Tolui");
		bruno = new Jogador("Bruno", "azul", "Jochi");
		List<Jogador> ordem = new ArrayList<Jogador>();
		ordem.add(ana);
		ordem.add(bruno);
		partida = new Partida(mapa, ordem, fechadas, pilha);
	}

	private void terminarJogo() {
		while (!partida.isEncerrada())
			partida.encerrarTurno();
	}

	@Test
	public void movimentoRespeitaLimiteDeCavalos() {
		partida.iniciarTurno(2, 0);
		assertTrue(partida.mover(List.of("A")));
		assertTrue(partida.mover(List.of("B")));
		assertEquals(0, partida.getMovimentosRestantes());
		assertFalse(partida.mover(List.of("C")));
		assertEquals("B", mapa.getPosicao("vermelho"));
	}

	@Test
	public void podePassarPorParadaOcupadaMasNaoTerminarNela() {
		mapa.moverPeao("azul", List.of("A"), 5); // Bruno ocupa a parada simples A
		partida.iniciarTurno(2, 0);
		assertFalse(partida.mover(List.of("A")));
		assertTrue(partida.mover(List.of("A", "B")));
		assertEquals("B", mapa.getPosicao("vermelho"));
	}

	@Test
	public void tributoExigeAdjacenciaEConsomePunhos() {
		for (int i = 0; i < 3; i++)
			mapa.colocarTributo("P1");
		partida.iniciarTurno(1, 1);
		assertFalse(partida.pegarTributo("P1")); // em Karakorum, P1 nao e adjacente
		assertTrue(partida.mover(List.of("A")));
		assertTrue(partida.pegarTributo("P1"));
		assertEquals(1, ana.getQuantidadeRecurso(TipoTributo.MOEDA));
		assertFalse(partida.pegarTributo("P1")); // acabaram os punhos
		assertEquals(2, mapa.getProvincia("P1").getQuantidade());
	}

	@Test
	public void paradaPuladaPorYurtProprioNaoPermitePegarTributo() {
		mapa.construirYurt("vermelho", "A");
		mapa.colocarTributo("P4"); // adjacente so a A
		mapa.colocarTributo("P1"); // adjacente a A e B
		partida.iniciarTurno(2, 2);
		assertTrue(partida.mover(List.of("A", "B")));
		assertEquals(1, partida.getMovimentosRestantes()); // pular A nao custou
		assertFalse(partida.pegarTributo("P4"));
		assertTrue(partida.pegarTributo("P1"));
	}

	@Test
	public void ataqueCustaUmaEspadaDepoisDuasNaMesmaCidade() {
		ana.adicionarRecursos(TipoTributo.ESPADA, 3);
		partida.iniciarTurno(2, 0);
		partida.mover(List.of("A", "B"));
		assertTrue(partida.atacarCidade("Cid1", new Tesouro("ferro")));
		assertEquals(2, ana.getQuantidadeRecurso(TipoTributo.ESPADA));
		assertTrue(partida.atacarCidade("Cid1", new Tesouro("seda")));
		assertEquals(0, ana.getQuantidadeRecurso(TipoTributo.ESPADA));
		assertFalse(partida.atacarCidade("Cid1", new Tesouro("gado")));
		assertEquals(1, mapa.getCidade("Cid1").getNumTesouros());
		assertEquals(2, ana.getTesouros().size());
	}

	@Test
	public void conquistaGeraYurtNaCidadeERevelaProximaCidade() {
		ana.adicionarRecursos(TipoTributo.ESPADA, 5); // 1 + 2 + 2
		partida.iniciarTurno(2, 0);
		partida.mover(List.of("A", "B"));
		partida.atacarCidade("Cid1", new Tesouro("ferro"));
		partida.atacarCidade("Cid1", new Tesouro("seda"));
		partida.atacarCidade("Cid1", new Tesouro("gado"));
		assertTrue(mapa.getCidade("Cid1").isConquistada());
		assertEquals(1, mapa.contarYurts("vermelho", Regiao.RUSSIA));
		assertTrue(mapa.getCidade("Cid3").isRevelada());
		assertEquals(4, mapa.getCidade("Cid3").getNumTesouros());
		assertTrue(fechadas.isEmpty());
	}

	@Test
	public void yurtExigeParadaVisitadaERecursoDeYurt() {
		ana.adicionarRecursos(TipoTributo.YURT, 1);
		partida.iniciarTurno(1, 0);
		assertFalse(partida.construirYurt("C")); // nunca visitada
		partida.mover(List.of("A"));
		assertTrue(partida.construirYurt("A"));
		assertEquals(0, ana.getQuantidadeRecurso(TipoTributo.YURT));
		assertFalse(partida.construirYurt("A")); // sem peca de yurt
		assertEquals(1, mapa.contarYurts("vermelho", Regiao.RUSSIA));
	}

	@Test
	public void melhoriaCustaDuasMoedasELimitaQuatroPorColuna() {
		ana.adicionarRecursos(TipoTributo.MOEDA, 1);
		assertFalse(partida.comprarMelhoria(new PecaMelhoria("m0", "e", "c"), Regiao.CHINA));
		assertEquals(0, ana.getMelhorias().size());

		ana.adicionarRecursos(TipoTributo.MOEDA, 9); // total 10
		for (int i = 1; i <= 4; i++)
			assertTrue(partida.comprarMelhoria(new PecaMelhoria("m" + i, "e", "c"), Regiao.CHINA));
		assertFalse(partida.comprarMelhoria(new PecaMelhoria("m5", "e", "c"), Regiao.CHINA));
		assertEquals(2, ana.getQuantidadeRecurso(TipoTributo.MOEDA));
		assertEquals(4, partida.getMelhoriasNaColuna("vermelho", Regiao.CHINA));
	}

	@Test
	public void fimDeJogoDisparaNoLimiteEEncerraAposRodadaExtra() {
		for (int i = 0; i < 9; i++)
			partida.ganharVoto("vermelho");
		assertFalse(partida.isFimDisparado());
		partida.ganharVoto("azul"); // ficha neutra conta votos de todos
		assertTrue(partida.isFimDisparado());
		partida.encerrarTurno(); // Ana
		partida.encerrarTurno(); // Bruno: fecha a rodada atual
		partida.encerrarTurno(); // Ana na rodada extra
		assertFalse(partida.isEncerrada());
		partida.encerrarTurno(); // Bruno: fecha a rodada extra
		assertTrue(partida.isEncerrada());
	}

	@Test
	public void influenciaDependeDeYurtsEDeMelhoriasNaColuna() {
		mapa.construirYurt("vermelho", "A");
		mapa.construirYurt("vermelho", "B");
		mapa.construirYurt("azul", "B");

		ana.adicionarRecursos(TipoTributo.MOEDA, 6);
		for (int i = 0; i < 3; i++)
			partida.comprarMelhoria(new PecaMelhoria("m" + i, "e", "c"), Regiao.RUSSIA);
		partida.encerrarTurno();
		bruno.adicionarRecursos(TipoTributo.MOEDA, 2);
		partida.comprarMelhoria(new PecaMelhoria("x", "e", "c"), Regiao.RUSSIA);

		for (int i = 0; i < 10; i++)
			partida.ganharVoto("azul");
		terminarJogo();
		int votosBrunoAntes = bruno.getVotos();
		partida.pontuarInfluencia();

		assertEquals(3, ana.getVotos());              // 1o com 3 melhorias
		assertEquals(votosBrunoAntes + 1, bruno.getVotos()); // 2o com 1 melhoria
	}

	@Test(expected = IllegalStateException.class)
	public void naoPontuaAntesDoFimDaPartida() {
		partida.pontuarInfluencia();
	}

	@Test
	public void empateDeVotosDecididoPorTesourosNaoEntregues() {
		for (int i = 0; i < 5; i++)
			partida.ganharVoto("vermelho");
		partida.encerrarTurno();
		for (int i = 0; i < 5; i++)
			partida.ganharVoto("azul");
		ana.adicionarTesouro(new Tesouro("ferro"));
		terminarJogo();
		partida.pontuarInfluencia();
		assertEquals(List.of("vermelho"), partida.vencedores());
	}

	@Test
	public void empateTotalDecididoPelaProximidadeDeKarakorum() {
		for (int i = 0; i < 5; i++)
			partida.ganharVoto("vermelho");
		partida.encerrarTurno();
		for (int i = 0; i < 5; i++)
			partida.ganharVoto("azul");
		mapa.moverPeao("azul", List.of("D"), 1); // Bruno a 1 passo, Ana em Karakorum
		terminarJogo();
		partida.pontuarInfluencia();
		assertEquals(List.of("vermelho"), partida.vencedores());
	}
}
