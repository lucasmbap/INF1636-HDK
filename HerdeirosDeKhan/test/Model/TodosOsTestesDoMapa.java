package Model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite que executa todos os testes do mapa de uma vez.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	MapaMontagemTest.class,
	MapaMovimentoTest.class,
	MapaYurtTest.class,
	MapaProvinciaKhanTest.class,
	MapaCidadeTest.class,
	MapaOficialTest.class,
	MapaCaminhoTest.class,
	PartidaTest.class,
	ModeloApiPartidaTest.class
})
public class TodosOsTestesDoMapa {
}
