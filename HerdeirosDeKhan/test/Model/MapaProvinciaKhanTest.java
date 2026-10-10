package Model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes dos tributos nas provincias e do peao do Khan (manual, paginas
 * 8 e 9).
 */
public class MapaProvinciaKhanTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private Mapa mapa;

	@Before
	public void criaMapaComUmTributoPorProvincia() {
		mapa = MapaDeTeste.criar();
		mapa.colocarTributo(MapaDeTeste.ESTEPE);
		mapa.colocarTributo(MapaDeTeste.DESERTO);
		mapa.colocarTributo(MapaDeTeste.PLANALTO);
		mapa.colocarTributo(MapaDeTeste.VALE);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void provinciaComTresTributos_recusaQuarto() {
		mapa.colocarTributo(MapaDeTeste.ESTEPE);
		mapa.colocarTributo(MapaDeTeste.ESTEPE);
		assertFalse("Uma provincia guarda no maximo 3 tributos",
				mapa.colocarTributo(MapaDeTeste.ESTEPE));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributoDeProvinciaAdjacente_retornaTipoDaProvincia() {
		TipoTributo t = mapa.pegarTributo(MapaDeTeste.ESTEPE, MapaDeTeste.caminho(MapaDeTeste.A));
		assertEquals("A Estepe, adjacente a A, tem tributos de espada", TipoTributo.ESPADA, t);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributoDeProvinciaNaoAdjacente_recusado() {
		TipoTributo t = mapa.pegarTributo(MapaDeTeste.DESERTO, MapaDeTeste.caminho(MapaDeTeste.A));
		assertNull("O Deserto nao e adjacente a A", t);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributoDeProvinciaVazia_recusado() {
		mapa.pegarTributo(MapaDeTeste.ESTEPE, MapaDeTeste.caminho(MapaDeTeste.A));
		TipoTributo t = mapa.pegarTributo(MapaDeTeste.ESTEPE, MapaDeTeste.caminho(MapaDeTeste.A));
		assertNull("A Estepe tinha 1 tributo e ja foi esvaziada", t);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanEmProvincia_adicionaTributoNasDuasProvinciasDasSetas() {
		mapa.moverKhanParaProvincia(MapaDeTeste.PLANALTO);
		assertEquals("Deserto (seta do Planalto) deveria passar de 1 para 2", 2,
				mapa.getProvincia(MapaDeTeste.DESERTO).getQuantidade());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanEmProvinciaCheia_naoPassaDeTres() {
		mapa.colocarTributo(MapaDeTeste.ESTEPE);
		mapa.colocarTributo(MapaDeTeste.ESTEPE);
		mapa.moverKhanParaProvincia(MapaDeTeste.PLANALTO);
		assertEquals("A Estepe ja tinha 3 e deve continuar com 3", 3,
				mapa.getProvincia(MapaDeTeste.ESTEPE).getQuantidade());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanNaoPodePermanecerNaMesmaProvincia() {
		mapa.moverKhanParaProvincia(MapaDeTeste.PLANALTO);
		assertFalse("O Khan ja esta no Planalto",
				mapa.moverKhanParaProvincia(MapaDeTeste.PLANALTO));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanEmProvinciaComum_recusado() {
		assertFalse("A Estepe nao e uma provincia do Khan",
				mapa.moverKhanParaProvincia(MapaDeTeste.ESTEPE));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanNaAreaDeMelhorias_podeVoltarParaProvincia() {
		mapa.moverKhanParaProvincia(MapaDeTeste.PLANALTO);
		mapa.moverKhanParaAreaDeMelhorias();
		assertTrue("Depois de sair do Planalto o Khan pode voltar para ele",
				mapa.moverKhanParaProvincia(MapaDeTeste.PLANALTO));
	}
}
