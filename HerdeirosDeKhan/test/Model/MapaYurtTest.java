package Model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da construcao e contagem de yurts no mapa (manual, paginas 11,
 * 14 e 15).
 */
public class MapaYurtTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private static final String VERMELHO = "Vermelho";
	private static final String AZUL = "Azul";

	private Mapa mapa;

	@Before
	public void criaMapa() {
		mapa = MapaDeTeste.criar();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void construirYurtEmKarakorum_naoPermitido() {
		assertFalse("Karakorum nao pode ter yurts",
				mapa.construirYurt(VERMELHO, Mapa.KARAKORUM));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void segundoYurtEmParadaSimples_naoPermitido() {
		mapa.construirYurt(AZUL, MapaDeTeste.A);
		assertFalse("A parada simples A ja tem um yurt",
				mapa.construirYurt(VERMELHO, MapaDeTeste.A));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void paradaDupla_aceitaDoisYurtsDoMesmoJogador() {
		mapa.construirYurt(VERMELHO, MapaDeTeste.B);
		assertTrue("A parada dupla B aceita um segundo yurt, mesmo da mesma cor",
				mapa.construirYurt(VERMELHO, MapaDeTeste.B));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void paradaDupla_recusaTerceiroYurt() {
		mapa.construirYurt(VERMELHO, MapaDeTeste.B);
		mapa.construirYurt(AZUL, MapaDeTeste.B);
		assertFalse("A parada dupla B ja tem 2 yurts",
				mapa.construirYurt(VERMELHO, MapaDeTeste.B));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void contarYurtsPorRegiao_somaSoAsParadasDaRegiao() {
		mapa.construirYurt(VERMELHO, MapaDeTeste.A); // Russia
		mapa.construirYurt(VERMELHO, MapaDeTeste.B); // Russia
		mapa.construirYurt(VERMELHO, MapaDeTeste.C); // China
		assertEquals("Yurts vermelhos na Russia", 2, mapa.contarYurts(VERMELHO, Regiao.RUSSIA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void contarYurtsPorRegiao_incluiYurtDeCidadeConquistada() {
		mapa.construirYurt(VERMELHO, MapaDeTeste.C);
		conquistaPequim(VERMELHO);
		assertEquals("Yurt em C + yurt no centro de Pequim (China)", 2,
				mapa.contarYurts(VERMELHO, Regiao.CHINA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void maiorGrupoDeYurtsConectados_ignoraYurtsSeparados() {
		mapa.construirYurt(VERMELHO, MapaDeTeste.A);
		mapa.construirYurt(VERMELHO, MapaDeTeste.B);
		mapa.construirYurt(VERMELHO, MapaDeTeste.B);
		mapa.construirYurt(VERMELHO, MapaDeTeste.D); // separado do grupo A-B
		assertEquals("Grupo A-B tem 3 yurts (B e dupla)", 3,
				mapa.maiorGrupoDeYurtsConectados(VERMELHO));
	}

	// auxiliar: revela Pequim com 2 tesouros e retira os dois a partir de E
	private void conquistaPequim(String cor) {
		mapa.revelarCidade(MapaDeTeste.PEQUIM,
				MapaDeTeste.tesouros(MapaDeTeste.SEDA, MapaDeTeste.FERRO));
		mapa.retirarTesouroDeCidade(MapaDeTeste.PEQUIM, MapaDeTeste.SEDA, cor,
				MapaDeTeste.caminho(MapaDeTeste.E));
		mapa.retirarTesouroDeCidade(MapaDeTeste.PEQUIM, MapaDeTeste.FERRO, cor,
				MapaDeTeste.caminho(MapaDeTeste.E));
	}
}
