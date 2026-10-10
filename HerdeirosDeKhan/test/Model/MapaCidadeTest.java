package Model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes das cidades no mapa: revelacao, retirada de tesouros e
 * conquista (manual, pagina 10).
 */
public class MapaCidadeTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private static final String VERMELHO = "Vermelho";

	private Mapa mapa;

	@Before
	public void criaMapaComKievRevelada() {
		mapa = MapaDeTeste.criar();
		mapa.revelarCidade(MapaDeTeste.KIEV,
				MapaDeTeste.tesouros(MapaDeTeste.PELE, MapaDeTeste.GADO));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void cidadeNaoRevelada_naoEstaDisponivel() {
		assertFalse("Pequim ainda nao foi revelada",
				mapa.getCidadesDisponiveis().contains(MapaDeTeste.PEQUIM));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void retirarTesouroDeCidadeNaoAdjacente_recusado() {
		assertFalse("Kiev nao e adjacente a parada C",
				mapa.retirarTesouroDeCidade(MapaDeTeste.KIEV, MapaDeTeste.PELE, VERMELHO,
						MapaDeTeste.caminho(MapaDeTeste.C)));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void retirarTesouroQueACidadeNaoTem_recusado() {
		assertFalse("Kiev nao tem seda",
				mapa.retirarTesouroDeCidade(MapaDeTeste.KIEV, MapaDeTeste.SEDA, VERMELHO,
						MapaDeTeste.caminho(MapaDeTeste.A)));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void retirarPenultimoTesouro_cidadeNaoEConquistada() {
		retiraDeKiev(MapaDeTeste.PELE);
		assertFalse("Kiev ainda tem um tesouro",
				mapa.getCidade(MapaDeTeste.KIEV).isConquistada());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void retirarUltimoTesouro_jogadorConquistaACidade() {
		retiraDeKiev(MapaDeTeste.PELE);
		retiraDeKiev(MapaDeTeste.GADO);
		assertEquals("Quem retirou o ultimo tesouro conquista Kiev", VERMELHO,
				mapa.getCidade(MapaDeTeste.KIEV).getConquistador());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void todasAsCidadesConquistadas_disparaFimAlternativo() {
		retiraDeKiev(MapaDeTeste.PELE);
		retiraDeKiev(MapaDeTeste.GADO);
		mapa.revelarCidade(MapaDeTeste.PEQUIM,
				MapaDeTeste.tesouros(MapaDeTeste.SEDA, MapaDeTeste.FERRO));
		mapa.retirarTesouroDeCidade(MapaDeTeste.PEQUIM, MapaDeTeste.SEDA, VERMELHO,
				MapaDeTeste.caminho(MapaDeTeste.E));
		mapa.retirarTesouroDeCidade(MapaDeTeste.PEQUIM, MapaDeTeste.FERRO, VERMELHO,
				MapaDeTeste.caminho(MapaDeTeste.E));
		assertTrue("Kiev e Pequim foram conquistadas", mapa.todasAsCidadesConquistadas());
	}

	// auxiliar: retira um tesouro de Kiev a partir da parada A (adjacente)
	private void retiraDeKiev(Tesouro tesouro) {
		mapa.retirarTesouroDeCidade(MapaDeTeste.KIEV, tesouro, VERMELHO,
				MapaDeTeste.caminho(MapaDeTeste.A));
	}
}
