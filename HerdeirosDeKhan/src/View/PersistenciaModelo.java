package View;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import Model.ModeloApi;

/**
 * UNICO ponto da View que fala com o Model sobre gravar e ler o estado da partida.
 *
 * Contrato combinado com o Model (metodos de ModeloApi, que so o Model consegue implementar,
 * porque so ele enxerga todo o estado):
 *
 *   public void salvar(File arquivo) throws IOException     // grava o estado em texto ASCII puro
 *   public void recuperar(File arquivo) throws IOException  // le o arquivo, refaz o estado e notifica
 *
 * A chamada e por reflexao para a View compilar mesmo antes de o Model ter esses metodos;
 * enquanto nao existirem, o usuario ve uma mensagem avisando. Quando existirem, esta classe
 * pode chamar modelo.salvar(arquivo) e modelo.recuperar(arquivo) direto.
 */
final class PersistenciaModelo {

	private PersistenciaModelo() {
	}

	static void gravar(File arquivo) throws IOException {
		chamar("salvar", arquivo, "salvamento");
	}

	static void ler(File arquivo) throws IOException {
		chamar("recuperar", arquivo, "recuperacao");
	}

	private static void chamar(String nome, File arquivo, String descricao) throws IOException {
		Method metodo;
		try {
			metodo = ModeloApi.class.getMethod(nome, File.class);
		} catch (NoSuchMethodException e) {
			throw new IOException("o Model ainda nao oferece o " + descricao + " da partida (ModeloApi." + nome
					+ "(File)).");
		}
		try {
			metodo.invoke(ModeloApi.getInstance(), arquivo);
		} catch (InvocationTargetException e) {
			Throwable causa = e.getCause();
			if (causa instanceof IOException)
				throw (IOException) causa;
			if (causa instanceof RuntimeException)
				throw (RuntimeException) causa;
			throw new IOException(String.valueOf(causa));
		} catch (IllegalAccessException e) {
			throw new IOException(e.toString());
		}
	}
}
