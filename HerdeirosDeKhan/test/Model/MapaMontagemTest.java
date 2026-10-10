package Model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da montagem do grafo do mapa: rotas, ligacoes com provincias e
 * cidades, e protecao da estrutura interna.
 */
public class MapaMontagemTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private Mapa mapa;

	@Before
	public void criaMapa() {
		mapa = MapaDeTeste.criar();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conectarParadas_rotaValeNosDoisSentidos() {
		assertTrue("A rota K-A deveria valer tambem de A para K",
				mapa.saoAdjacentes(MapaDeTeste.A, MapaDeTeste.K));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conectarParadaInexistente_conexaoRecusada() {
		assertFalse("Nao deveria ser possivel ligar uma parada que nao existe",
				mapa.conectarParadas(MapaDeTeste.A, "Inexistente"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conectarParadasJaConectadas_conexaoRecusada() {
		assertFalse("A mesma rota nao deveria ser criada duas vezes",
				mapa.conectarParadas(MapaDeTeste.B, MapaDeTeste.A));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void adicionarSegundaKarakorum_recusado() {
		assertFalse("O mapa so pode ter uma Karakorum",
				mapa.adicionarParada("Outra", TipoParada.KARAKORUM, Regiao.CHINA, true));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void karakorumCriadaPeloConstrutor_temConselheiro() {
		assertTrue("Karakorum deveria existir e ter um conselheiro",
				mapa.getParada(Mapa.KARAKORUM).temConselheiro());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void alterarListaDeAdjacentesRetornada_naoAlteraOMapa() {
		List<String> vizinhas = mapa.getParadasAdjacentes(MapaDeTeste.A);
		vizinhas.add(MapaDeTeste.E);
		assertFalse("A lista retornada deveria ser uma copia",
				mapa.saoAdjacentes(MapaDeTeste.A, MapaDeTeste.E));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void definirAlvosDoKhanEmProvinciaComum_recusado() {
		assertFalse("So provincias do Khan podem ter alvos do Khan",
				mapa.definirAlvosDoKhan(MapaDeTeste.ESTEPE, MapaDeTeste.DESERTO, MapaDeTeste.VALE));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void distanciaEntreParadas_usaMenorCaminho() {
		// K -> D -> E (2 passos) e mais curto que K -> A -> B -> C -> E (4 passos)
		assertEquals("Distancia de Karakorum ate E", 2,
				mapa.distancia(MapaDeTeste.K, MapaDeTeste.E));
	}
}
