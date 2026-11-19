package Model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da area de melhorias (manual, pag. 9, 11 e 12): 5 espacos, pilha,
 * pecas andando para a direita e reposicao quando o Khan chega.
 */
public class AreaDeMelhoriasTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private AreaDeMelhorias area;

	@Before
	public void criaAreaComPecasConhecidas() {
		// espacos: MOEDA, ESPADA, YURT, PUNHO, CAVALOS (todas vermelhas); pilha: 3 pecas verdes
		area = new AreaDeMelhorias();
		MelhoriaColorida[] espacos = { peca(TipoMelhoria.MOEDA), peca(TipoMelhoria.ESPADA), peca(TipoMelhoria.YURT),
				peca(TipoMelhoria.PUNHO), peca(TipoMelhoria.CAVALOS) };
		List<MelhoriaColorida> pilha = new ArrayList<MelhoriaColorida>();
		for (int i = 0; i < 3; i++)
			pilha.add(new MelhoriaColorida(TipoMelhoria.MOEDA, ColunaCartela.VERDE));
		area.restaurar(espacos, pilha);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void jogoTem44PecasDeMelhoria() {
		assertEquals("Manual: 44 pecas de melhoria", 44, AreaDeMelhorias.todasAsPecas().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novaArea_revela5PecasEGuarda39NaPilha() {
		AreaDeMelhorias nova = AreaDeMelhorias.nova();
		assertNotNull("Espaco da esquerda revelado", nova.getEspaco(0));
		assertNotNull("Espaco da direita revelado", nova.getEspaco(4));
		assertEquals("44 - 5 reveladas", 39, nova.getTamanhoPilha());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarDoMeio_pecasDaEsquerdaAndamParaDireita() {
		area.retirar(2); // YURT
		assertNull("O espaco da esquerda fica vazio", area.getEspaco(0));
		assertEquals("MOEDA andou para o espaco 1", TipoMelhoria.MOEDA, area.getEspaco(1).getTipo());
		assertEquals("ESPADA andou para o espaco 2", TipoMelhoria.ESPADA, area.getEspaco(2).getTipo());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarDaArea_naoRevelaPecaNova() {
		area.retirar(4);
		assertEquals("A pilha so e usada quando o Khan vem para a area", 3, area.getTamanhoPilha());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarDoTopo_tiraDaPilhaENaoMexeNaArea() {
		MelhoriaColorida m = area.retirar(-1);
		assertEquals("Peca do topo e verde", ColunaCartela.VERDE, m.getCor());
		assertEquals("A area continua cheia", TipoMelhoria.MOEDA, area.getEspaco(0).getTipo());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void espacoVazio_naoTemPecaParaComprar() {
		area.retirar(4);
		assertNull("Depois de andar para a direita o espaco 0 esta vazio", area.retirar(0));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doisEspacosVazios_iconeDeMoedaAMostra() {
		area.retirar(4);
		area.retirar(4);
		assertTrue("Espacos 0 e 1 vazios: o icone de moeda aparece", area.iconeDeMoedaAMostra());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doisEspacosVazios_iconeDeEspadaAindaCoberto() {
		area.retirar(4);
		area.retirar(4);
		assertFalse("O espaco 3 ainda tem peca", area.iconeDeEspadaAMostra());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void preencherVazios_usaAPilha() {
		area.retirar(4);
		area.retirar(4);
		area.preencherVazios();
		assertEquals("2 pecas sairam da pilha", 1, area.getTamanhoPilha());
		assertNotNull("O espaco 0 foi preenchido", area.getEspaco(0));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void codigoDaPeca_idaEVolta() {
		MelhoriaColorida m = MelhoriaColorida.deCodigo(peca(TipoMelhoria.TESOURO_SEDA).codigo());
		assertEquals("Tipo lido do codigo", TipoMelhoria.TESOURO_SEDA, m.getTipo());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void codigoInvalido_lancaExcecao() {
		MelhoriaColorida.deCodigo("BANANA:AZUL");
	}

	private static MelhoriaColorida peca(TipoMelhoria t) {
		return new MelhoriaColorida(t, ColunaCartela.VERMELHA);
	}
}
