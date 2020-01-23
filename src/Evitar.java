import java.util.concurrent.Semaphore;

public class Evitar extends Thread {

	RobotLego robot;
	static final int portoToque = RobotLego.S_2;
	static final int velRobot = 20; // cm por s

	String nomeRobot;
	boolean ligacaoSucesso;
	int s, td;

	boolean executarComportamento;

	boolean sensorAtivado;

	// Automato
	int estado;
	final int espera = 0;
	final int lerSensorToque = 1;
	final int evitar = 2;
	final int dormir = 3;

	Semaphore acessoRobot, esperaTrabalho, desativar;

	private void iniciarVariaveis() {
		estado = espera;
		executarComportamento = true;
		ligacaoSucesso = false;
		s = 0;
		td = 0;
		sensorAtivado = false;
		desativar = new Semaphore(0);
	}

	public Evitar(Semaphore aR, RobotLego r) {
		acessoRobot = aR;
		robot = r;
		esperaTrabalho = new Semaphore(0);
		iniciarVariaveis();
	}

	public void ativar() {
		System.out.println("evitar ativar");
		estado = lerSensorToque;
		esperaTrabalho.release();
		desativar.drainPermits();

	}

	public void desativar() {
		System.out.println("evitar desativar");
		estado = espera;
		esperaTrabalho.drainPermits();
		try {
			desativar.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void comportamentoEvitar() {
		while (executarComportamento) {
			switch (estado) {

			case espera:
				System.out.println("evitar --- " + estado);
				desativar.release();
				s = 0;
				td = 0;
				try {
					esperaTrabalho.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				break;

			case lerSensorToque:
				System.out.println("evitar --- " + estado);
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				if (!sensorAtivado) {
					robot.setSensorTouch(portoToque);
					sensorAtivado = true;
				}

				s = robot.Sensor(portoToque);
				acessoRobot.release();
				System.out.println("Sensor: " + s);
				td = 500;
				if (estado == lerSensorToque) {
					if (s == 0)
						estado = dormir;
					else if (s == 1)
						estado = evitar;
				}
				break;

			case evitar:
				System.out.println("evitar --- " + estado);
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				robot.Parar(true);
				robot.Reta(-15);
				robot.CurvarEsquerda(0, 90);
				robot.Parar(false);
				acessoRobot.release();
				td = 15 / velRobot;
				td += (int) ((4 * (90 * Math.PI) / 180) / velRobot);
				td *= 1000;
				td += 500;
				if (estado == evitar)
					estado = dormir;
				break;

			case dormir:
				System.out.println("evitar --- " + estado);
				adormecer(td);
				td = 0;
				if (estado == dormir)
					estado = lerSensorToque;
				break;
			}
		}
	}

	private void adormecer(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		comportamentoEvitar();
		System.exit(1);
	}

	public static void main(String[] args) {

	}
}
