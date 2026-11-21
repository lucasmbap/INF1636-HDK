package Controller;

import java.io.File;
import java.io.IOException;

import Model.ModeloApi;
import View.OuvinteArquivos;

/**
 * Coordena os pedidos de salvamento e recuperacao entre a View e o Model.
 */
public final class ControladorArquivos implements OuvinteArquivos {
	private final ModeloApi modelo;

	public ControladorArquivos(ModeloApi modelo) {
		if (modelo == null)
			throw new IllegalArgumentException("O modelo e obrigatorio.");
		this.modelo = modelo;
	}

	@Override
	public boolean podeSalvarPartida() {
		return modelo.podeSalvar();
	}

	@Override
	public String salvarPartida(File arquivo) {
		if (!modelo.podeSalvar())
			return "a partida so pode ser salva antes do inicio da jogada.";
		try {
			modelo.salvar(arquivo);
			return null;
		} catch (IOException e) {
			return e.getMessage();
		} catch (RuntimeException e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	@Override
	public String recuperarPartida(File arquivo) {
		try {
			modelo.recuperar(arquivo);
		} catch (IOException e) {
			return e.getMessage();
		} catch (RuntimeException e) {
			e.printStackTrace();
			return e.toString();
		}
		return modelo.isPartidaIniciada() ? null : "o arquivo nao tem uma partida em andamento.";
	}
}
