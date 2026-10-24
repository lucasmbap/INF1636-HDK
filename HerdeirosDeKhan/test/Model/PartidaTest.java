package Model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes do controle de turnos e movimentos da partida.
 */
public class PartidaTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private Partida partida;

	@Before
	public void criaPartidaComDoisJogadores() {
		partida = new Partida(MapaDeTeste.criar());
		partida.adicionarJogador(new Jogador("Ana", "Vermelho", "Tolui"));
		partida.adicionarJogador(new Jogador("Bia", "Azul", "Jochi"));
		partida.iniciar();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void inicioDaPartida_primeiroJogadorEDaVez() {
		assertEquals("Quem foi adicionado primeiro comeca", "Vermelho", partida.getCorDaVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void inicioDaPartida_peoesEmKarakorum() {
		assertEquals("O peao azul comeca em Karakorum", Mapa.KARAKORUM,
				partida.getMapa().getPosicao("Azul"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void partidaComUmJogador_naoInicia() {
		Partida p = new Partida(MapaDeTeste.criar());
		p.adicionarJogador(new Jogador("Ana", "Vermelho", "Tolui"));
		assertFalse("Sao necessarios pelo menos 2 jogadores", p.iniciar());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void corRepetida_jogadorRecusado() {
		Partida p = new Partida(MapaDeTeste.criar());
		p.adicionarJogador(new Jogador("Ana", "Vermelho", "Tolui"));
		assertFalse("Dois jogadores nao podem ter a mesma cor",
				p.adicionarJogador(new Jogador("Bia", "Vermelho", "Jochi")));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void moverSemMovimentos_recusado() {
		assertFalse("O turno comeca com 0 movimentos", partida.moverPara(MapaDeTeste.A));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void moverDuasParadas_descontaDoisMovimentos() {
		partida.definirMovimentos(3);
		partida.moverPara(MapaDeTeste.B);
		assertEquals("Restava 3; K -> A -> B gasta 2", 1, partida.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void movimentoDividido_paradasVisitadasSeAcumulam() {
		partida.definirMovimentos(3);
		partida.moverPara(MapaDeTeste.A);
		partida.moverPara(MapaDeTeste.B);
		assertEquals("Visitadas: Karakorum, A e B", 3, partida.getParadasVisitadasNoTurno().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void passarVez_vaiParaOProximoJogador() {
		partida.passarVez();
		assertEquals("Depois do vermelho joga o azul", "Azul", partida.getCorDaVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void passarVezDoUltimo_voltaAoPrimeiro() {
		partida.passarVez();
		partida.passarVez();
		assertEquals("Depois do ultimo, volta o primeiro", "Vermelho", partida.getCorDaVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void movimento_avisaOsObservadores() {
		ContadorDeAvisos contador = new ContadorDeAvisos();
		partida.registrarObservador(contador);
		partida.definirMovimentos(1);
		partida.moverPara(MapaDeTeste.A);
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
