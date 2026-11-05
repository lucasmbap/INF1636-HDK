package Model;

/**
 * Padrao Observer (cap. 17): interface do objeto observado. A ModeloApi a
 * implementa; as janelas da View se registram com add() e, quando sao
 * notificadas, pedem os dados com get() (callback) ou com os demais metodos
 * da API.
 */
public interface ObservadoIF {
	void add(ObservadorIF o);

	void remove(ObservadorIF o);

	/** Devolve uma informacao do estado, indicada por uma das constantes da ModeloApi. */
	Object get(int i);
}
