import java.util.concurrent.Semaphore;

public class Distancia {

	int distancia;

	Semaphore escreverDistancia, lerDistancia;

	public Distancia() {
		distancia = 0;
		escreverDistancia = new Semaphore(1);
		lerDistancia = new Semaphore(0);
	}

//	public void setDistanciaSimples(int d) {
//		distancia = d;
//	}
//
//	public int getDistanciaSimples() {
//		return distancia;
//	}

	public void setDistancia(int d) {
		distancia = d;
		lerDistancia.drainPermits();
		// if (lerDistancia.availablePermits()==0)
		lerDistancia.release();
	}

	public int getDistancia() {
		int dist;
		try {
			lerDistancia.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		dist = distancia;
		return dist;
	}

	public static void main(String[] args) {

	}

}
