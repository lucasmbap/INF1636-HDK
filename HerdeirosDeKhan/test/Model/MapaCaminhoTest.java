package Model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes do caminho automatico ate a parada clicada (2a iteracao: o
 * jogador clica so no destino).
 */
public class MapaCaminhoTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private static final String VERMELHO = "Vermelho";
	private static final String AZUL = "Azul";

	private Mapa mapa;

	@Before
	public void criaMapaComPeaoEmKarakorum() {
		mapa = MapaDeTeste.criar();
		mapa.colocarPeao(VERMELHO, Mapa.KARAKORUM);
		mapa.colocarPeao(AZUL, Mapa.KARAKORUM);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void melhorCaminho_escolheORotaMaisCurta() {
		// K -> D -> E (2) e mais curto que K -> A -> B -> C -> E (4)
		List<String> c = mapa.melhorCaminho(VERMELHO, MapaDeTeste.E);
		assertEquals("Caminho K -> D -> E", MapaDeTeste.caminho(MapaDeTeste.D, MapaDeTeste.E), c);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void melhorCaminho_aproveitaYurtsProprios() {
		mapa.construirYurt(VERMELHO, MapaDeTeste.A);
		mapa.construirYurt(VERMELHO, MapaDeTeste.B);
		mapa.construirYurt(VERMELHO, MapaDeTeste.C);
		// K -> A -> B -> C -> E custa so 1 com os yurts; K -> D -> E custa 2
		int gasto = mapa.moverPeaoPara(VERMELHO, MapaDeTeste.E, 1);
		assertEquals("Com os yurts, chegar em E custa 1 movimento", 1, gasto);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void destinoLongeDemais_movimentoRecusado() {
		int gasto = mapa.moverPeaoPara(VERMELHO, MapaDeTeste.C, 2);
		assertEquals("C fica a 3 movimentos de Karakorum", -1, gasto);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void destinoOcupado_semCaminho() {
		mapa.moverPeaoPara(AZUL, MapaDeTeste.D, 1);
		assertTrue("D (simples) ja esta ocupada pelo azul",
				mapa.melhorCaminho(VERMELHO, MapaDeTeste.D).isEmpty());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void destinoComYurtProprio_custaUmMovimento() {
		mapa.construirYurt(VERMELHO, MapaDeTeste.A);
		int gasto = mapa.moverPeaoPara(VERMELHO, MapaDeTeste.A, 1);
		assertEquals("A parada final sempre custa 1, mesmo com yurt proprio", 1, gasto);
	}
}
