package Model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes das regras de movimento dos peoes no mapa (manual, pagina 7).
 * Todos os peoes comecam em Karakorum.
 */
public class MapaMovimentoTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private static final String VERMELHO = "Vermelho";
	private static final String AZUL = "Azul";
	private static final String VERDE = "Verde";

	private Mapa mapa;

	@Before
	public void criaMapaComPeoesEmKarakorum() {
		mapa = MapaDeTeste.criar();
		mapa.colocarPeao(VERMELHO, Mapa.KARAKORUM);
		mapa.colocarPeao(AZUL, Mapa.KARAKORUM);
		mapa.colocarPeao(VERDE, Mapa.KARAKORUM);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void moverParaParadaNaoAdjacente_movimentoRecusado() {
		int gasto = mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.B), 3);
		assertEquals("Karakorum nao tem rota direta para B", -1, gasto);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void movimentoRecusado_peaoPermaneceNaOrigem() {
		mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.B), 3);
		assertEquals("O peao nao deveria ter saido de Karakorum",
				Mapa.KARAKORUM, mapa.getPosicao(VERMELHO));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void caminhoSemYurts_custaUmMovimentoPorParada() {
		int custo = mapa.custoDoCaminho(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B));
		assertEquals("K -> A -> B deveria custar 2 movimentos", 2, custo);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void movimentoAlemDoNumeroDeCavalos_recusado() {
		int gasto = mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B), 1);
		assertEquals("Com 1 cavalo nao da para andar 2 paradas", -1, gasto);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void passarPorYurtProprio_naoCustaMovimento() {
		mapa.construirYurt(VERMELHO, MapaDeTeste.A);
		int custo = mapa.custoDoCaminho(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B));
		assertEquals("Pular o proprio yurt em A: K -> B deveria custar 1", 1, custo);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void passarPorYurtDeOutroJogador_custaMovimentoNormal() {
		mapa.construirYurt(AZUL, MapaDeTeste.A);
		int custo = mapa.custoDoCaminho(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B));
		assertEquals("Yurt de outro jogador nao pode ser pulado", 2, custo);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void paradaPuladaPeloYurt_naoContaComoVisitada() {
		mapa.construirYurt(VERMELHO, MapaDeTeste.A);
		List<String> visitadas = mapa.getParadasVisitadas(VERMELHO,
				MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B));
		assertFalse("O jogador nunca esteve de fato em A, que foi pulada",
				visitadas.contains(MapaDeTeste.A));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void paradasVisitadas_incluemOrigemEDestino() {
		List<String> visitadas = mapa.getParadasVisitadas(VERMELHO,
				MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B));
		assertEquals("Visitadas: Karakorum, A e B", 3, visitadas.size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void terminarEmParadaSimplesOcupada_naoPermitido() {
		mapa.moverPeao(AZUL, MapaDeTeste.caminho(MapaDeTeste.A), 1);
		int gasto = mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A), 1);
		assertEquals("A ja esta ocupada pelo peao azul", -1, gasto);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void passarPorParadaSimplesOcupada_permitido() {
		mapa.moverPeao(AZUL, MapaDeTeste.caminho(MapaDeTeste.A), 1);
		int gasto = mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B), 2);
		assertEquals("Pode-se passar por A ocupada e terminar em B", 2, gasto);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void paradaDupla_aceitaDoisPeoes() {
		mapa.moverPeao(AZUL, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B), 2);
		int gasto = mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B), 2);
		assertEquals("A parada dupla B deveria aceitar um segundo peao", 2, gasto);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void paradaDupla_recusaTerceiroPeao() {
		mapa.moverPeao(AZUL, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B), 2);
		mapa.moverPeao(VERDE, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B), 2);
		int gasto = mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B), 2);
		assertEquals("A parada dupla B ja tem 2 peoes", -1, gasto);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void voltarParaKarakorumOcupada_permitido() {
		mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A), 1);
		int gasto = mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(Mapa.KARAKORUM), 1);
		assertEquals("Em Karakorum cabem todos os peoes", 1, gasto);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void distanciaAteKarakorum_aposMovimento() {
		mapa.moverPeao(VERMELHO, MapaDeTeste.caminho(MapaDeTeste.A, MapaDeTeste.B), 2);
		assertEquals("B fica a 2 passos de Karakorum", 2, mapa.distanciaAteKarakorum(VERMELHO));
	}
}
