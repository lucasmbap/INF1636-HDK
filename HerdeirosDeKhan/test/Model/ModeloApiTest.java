package Model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ModeloApiTest {
	@Test
	public void jogadorInvalidoNaoConsomeIdentificador() {
		ModeloApi api = new ModeloApi();

		RuntimeException erro = capturarErro(() -> api.criarJogador(" ", "Azul", "Khan"));

		assertTrue(erro instanceof IllegalArgumentException);
		assertEquals(1, api.criarJogador("Ana", "Azul", "Khan"));
	}

	@Test
	public void recursosNaoFicamNegativosNemPodemSerRetiradosAcimaDoSaldo() {
		ModeloApi api = new ModeloApi();
		int jogador = api.criarJogador("Ana", "Azul", "Khan");
		api.adicionarRecursos(jogador, TipoTributo.YURT, 3);

		assertFalse(api.removerRecursos(jogador, TipoTributo.YURT, 4));
		assertEquals(3, api.getQuantidadeRecurso(jogador, TipoTributo.YURT));
		assertTrue(api.removerRecursos(jogador, TipoTributo.YURT, 2));
		assertEquals(1, api.getQuantidadeRecurso(jogador, TipoTributo.YURT));
		assertTrue(capturarErro(() -> api.adicionarRecursos(jogador, TipoTributo.YURT, -1))
				instanceof IllegalArgumentException);
		assertEquals(1, api.getQuantidadeRecurso(jogador, TipoTributo.YURT));
	}

	@Test
	public void overflowDeRecursosNaoAlteraSaldoExistente() {
		ModeloApi api = new ModeloApi();
		int jogador = api.criarJogador("Ana", "Azul", "Khan");
		api.adicionarRecursos(jogador, TipoTributo.MOEDA, Integer.MAX_VALUE);

		RuntimeException erro = capturarErro(
				() -> api.adicionarRecursos(jogador, TipoTributo.MOEDA, 1));

		assertTrue(erro instanceof ArithmeticException);
		assertEquals(Integer.MAX_VALUE, api.getQuantidadeRecurso(jogador, TipoTributo.MOEDA));
	}

	@Test
	public void tesouroPodeSerRetiradoPorNomeECartaPodeSerRevelada() {
		ModeloApi api = new ModeloApi();
		int jogador = api.criarJogador("Ana", "Azul", "Khan");
		api.adicionarTesouro(jogador, "Jade");
		assertTrue(api.removerTesouro(jogador, "Jade"));
		assertTrue(api.getNomesTesouros(jogador).isEmpty());
		assertFalse(api.removerTesouro(jogador, "Jade"));

		api.adicionarCartaConselheiro(jogador, "Conselheira", "condicao", "recompensa", false);
		assertEquals("condicao", api.getCondicaoCartaConselheiro(jogador, 0));
		assertEquals("recompensa", api.getRecompensaCartaConselheiro(jogador, 0));
		assertFalse(api.isCartaConselheiroAberta(jogador, 0));
		api.revelarCartaConselheiro(jogador, 0);
		assertTrue(api.isCartaConselheiroAberta(jogador, 0));
	}

	private static RuntimeException capturarErro(Runnable operacao) {
		try {
			operacao.run();
		} catch (RuntimeException erro) {
			return erro;
		}
		fail("Era esperada uma exceção.");
		return null;
	}
}
