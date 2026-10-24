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
	public void partidaComUmJogador_naoInicia() {
		ModeloApi outra = new ModeloApi();
		outra.criarJogador("Ana", "Vermelho", "Tolui");
		assertFalse("Sao necessarios pelo menos 2 jogadores", outra.iniciarPartida());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void inicioDaPartida_peoesEmKarakorum() {
		assertEquals("O peao azul comeca em Karakorum", Mapa.KARAKORUM, api.getPosicao("Azul"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void inicioDaPartida_cadaProvinciaTemUmTributo() {
		assertEquals("Preparacao: 1 tributo por provincia", 1, api.getTributos("Espada Kaesong"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void inicioDaPartida_tresCidadesReveladas() {
		assertEquals("Preparacao: 3 cidades com tesouros", 3, api.getCidadesDisponiveis().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void moverSemDefinirMovimentos_naoFazNada() {
		assertFalse("O turno comeca com 0 movimentos", api.moverPara("R9"));
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
	public void moverDuasParadas_descontaDoisMovimentos() {
		api.definirMovimentos(3);
		api.moverPara("R6"); // Karakorum -> R9 -> R6
		assertEquals("Restava 3; o caminho gasta 2", 1, api.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void passarVez_vaiParaOProximoJogador() {
		api.passarVez();
		assertEquals("Depois do vermelho joga o azul", "Azul", api.getCorDaVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void movimento_avisaOsObservadores() {
		ContadorDeAvisos contador = new ContadorDeAvisos();
		api.registrarObservador(contador);
		api.definirMovimentos(1);
		api.moverPara("R9");
		assertEquals("Um aviso ao definir movimentos e outro ao mover", 2, contador.avisos);
	}

	// observador usado so neste teste
	private static class ContadorDeAvisos implements ObservadorModelo {
		int avisos = 0;

		public void modeloAlterado() {
			avisos++;
		}
	}
}
