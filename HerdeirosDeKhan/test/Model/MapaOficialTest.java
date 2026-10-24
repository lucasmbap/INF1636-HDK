package Model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Confere a estrutura do mapa oficial montado a partir do tabuleiro.
 */
public class MapaOficialTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private Mapa mapa;

	@Before
	public void criaMapaOficial() {
		mapa = MapaOficial.criar();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapaOficial_tem34Paradas() {
		assertEquals("11 paradas por regiao + Karakorum", 34, mapa.getNomesParadas().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapaOficial_tem12ProvinciasE9Cidades() {
		assertEquals("Provincias + cidades do tabuleiro", 21,
				mapa.getNomesProvincias().size() + mapa.getNomesCidades().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapaOficial_tem4ProvinciasDoKhan() {
		assertEquals("Ha 4 cabecas do Khan no tabuleiro", 4, mapa.getProvinciasDoKhan().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void cadaCidade_ficaEntreTresParadas() {
		int total = 0;
		for (String p : mapa.getNomesParadas())
			total += mapa.getCidadesAdjacentes(p).size();
		assertEquals("9 cidades x 3 paradas ao redor", 27, total);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void todasAsParadas_saoAlcancaveisDeKarakorum() {
		int alcancaveis = 0;
		for (String p : mapa.getNomesParadas())
			if (mapa.distancia(Mapa.KARAKORUM, p) >= 0)
				alcancaveis++;
		assertEquals("O mapa tem de ser conexo", 34, alcancaveis);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void paradasDuplas_saoAsSeisDeConselheiro() {
		int duplasComConselheiro = 0;
		for (String p : mapa.getNomesParadas()) {
			Parada parada = mapa.getParada(p);
			if (parada.getTipo() == TipoParada.DUPLA && parada.temConselheiro())
				duplasComConselheiro++;
		}
		assertEquals("6 paradas duplas com conselheiro", 6, duplasComConselheiro);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void karakorum_ligaAsTresRegioes() {
		// R9 e R11 (Russia), C4 (China) e P5 (Persia)
		assertEquals("Rotas saindo de Karakorum", 4,
				mapa.getParadasAdjacentes(Mapa.KARAKORUM).size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void kiev_ficaEntreR1R3eR4() {
		assertTrue("R3 deveria ser adjacente a Kiev",
				mapa.getCidadesAdjacentes("R3").contains("Kiev"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanEmMoedaSarai_daTributoEmTresProvincias() {
		mapa.moverKhanParaProvincia("Moeda Sarai");
		int total = mapa.getProvincia("Moeda Sarai").getQuantidade()
				+ mapa.getProvincia("Yurt Karakorum Norte").getQuantidade()
				+ mapa.getProvincia("Espada Kaesong").getQuantidade();
		assertEquals("A propria provincia e as 2 setas recebem 1 tributo", 3, total);
	}
}
