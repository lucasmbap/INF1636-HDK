package View;

import java.io.File;
import java.io.IOException;

import Model.ModeloApi;

/**
 * UNICO ponto da View que pede ao Model para gravar e ler o estado da partida. Quem sabe
 * montar o arquivo e o Model (so ele enxerga todo o estado); a View so escolhe o arquivo
 * com JFileChooser (SeletorDeArquivos) e chama a fachada:
 *
 *   ModeloApi.salvar(File)     grava o estado em texto ASCII puro
 *   ModeloApi.recuperar(File)  le o arquivo, refaz o estado e avisa os observadores
 */
final class PersistenciaModelo {

	private PersistenciaModelo() {
	}

	static void gravar(File arquivo) throws IOException {
		ModeloApi.getInstance().salvar(arquivo);
	}

	static void ler(File arquivo) throws IOException {
		ModeloApi.getInstance().recuperar(arquivo);
	}
}
