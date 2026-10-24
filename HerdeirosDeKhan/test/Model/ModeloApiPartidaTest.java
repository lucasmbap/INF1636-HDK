package Model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da parte da ModeloApi usada pela View e pelo Controller na 2a
 * iteracao (turno e movimento no mapa oficial).
 */
public class ModeloApiPartidaTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private ModeloApi api;
	private int idAna;

	@Before
	public void criaPartidaComDoisJogadores() {
		api = new ModeloApi();
		idAna = api.criarJogador("Ana", "Vermelho", "Tolui");
		api.criarJogador("Bia", "Azul", "Jochi");
		api.iniciarPartida();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void jogadorCriadoPrimeiro_eDaVez() {
		assertEquals("O id da vez e o do primeiro jogador criado", idAna, api.getIdJogadorDaVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void cliqueEmParadaAdjacente_movePeao() {
		api.definirMovimentos(1);
		api.moverPara("R9");
		assertEquals("R9 e adjacente a Karakorum", "R9", api.getPosicao("Vermelho"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void cliqueEmParadaDistante_naoFazNada() {
		api.definirMovimentos(1);
		assertFalse("R1 fica longe de Karakorum", api.moverPara("R1"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void inicioDaPartida_cadaProvinciaTemUmTributo() {
		assertEquals("Preparacao: 1 tributo por provincia", 1, api.getTributos("Espada Kaesong"));
	}
}
