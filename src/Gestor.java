import java.util.concurrent.Semaphore;

public class Gestor extends Thread {

	boolean executarComportamento;

	boolean estaVaguear, estaSeguir, sensorAtivado;

	static final int portoDist = RobotLego.S_1;

	boolean desativarGestor;

	// int contador;

	int d;

	RobotPlayer robot;

	Semaphore acessoRobot, esperaTrabalho, desativar;

	// Automato
	int estado;
	final int espera = 0;
	final int lerSensor = 1;
	final int ativarVaguear = 3;
	final int ativarSeguir = 4;
	final int dormir = 5;

	Vaguear vaguear;
	SeguirParede seguirParede;
	Distancia distancia;

	public void iniciarVariaveis() {
		estado = espera;
		executarComportamento = true;
		estaVaguear = false;
		estaSeguir = false;
		sensorAtivado = false;
		esperaTrabalho = new Semaphore(0);
		desativarGestor = false;
		d = 0;
		desativar = new Semaphore(0);
	}

	public Gestor(Semaphore aR, RobotPlayer r, Vaguear v, SeguirParede s, Distancia d) {
		acessoRobot = aR;
		robot = r;
		vaguear = v;
		seguirParede = s;
		distancia = d;
		iniciarVariaveis();
	}

	private void adormecer(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void ativar() {
		System.out.println("gestor ativar");
		estado = lerSensor;
		esperaTrabalho.release();
		desativar.drainPermits();
	}

	public void desativar() {
		System.out.println("gestor desativar");
		estado = espera;
		esperaTrabalho.drainPermits();
		// TODO Desativar filhos
		// seguirParede.desativar();
		// vaguear.desativar();
		try {
			desativar.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void comportamentoGestor() {
		while (executarComportamento) {
			switch (estado) {

			case espera:
				System.out.println("gestor iniciar");
				desativar.release();
				estaVaguear = false;
				estaSeguir = false;
				sensorAtivado = false;
				try {
					esperaTrabalho.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;

			case lerSensor:
				System.out.println("gestor ler sensor");
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!sensorAtivado) {
					robot.setSensorLowspeed(portoDist);
					sensorAtivado = true;
					adormecer(500); // 250
				}
				d = robot.SensorUS(portoDist);
				acessoRobot.release();
				distancia.setDistancia(d);
				System.out.println("..............................distancia lida: " + d);
				if (estado == lerSensor) {
					if (d >= 100) {
						if (estaVaguear)
							estado = dormir;
						else
							estado = ativarVaguear;
					} else {
						if (estaSeguir)
							estado = dormir;
						else
							estado = ativarSeguir;
					}
				}
				break;

			case ativarVaguear:
				System.out.println("gestor ativar vaguear");
				seguirParede.desativar();
				estaSeguir = false;
				vaguear.ativar();
				estaVaguear = true;
				if (estado == ativarVaguear)
					estado = dormir;
				break;

			case ativarSeguir:
				System.out.println("gestor ativar seguir");
				vaguear.desativar();
				estaVaguear = false;
				seguirParede.ativar();
				estaSeguir = true;
				if (estado == ativarSeguir)
					estado = dormir;
				break;

			case dormir:
				adormecer(250);
				if (estado == dormir)
					estado = lerSensor;
				break;
			}
		}
	}

	public void run() {
		comportamentoGestor();
		System.exit(1);
	}

	public static void main(String[] args) {
	}
}
