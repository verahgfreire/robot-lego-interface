import java.util.concurrent.Semaphore;

public class SeguirParede extends Thread {

	RobotPlayer robot;
	static final int velRobot = 20; // cm por s
	String nomeRobot;

	int td;
	final int dReta = 10;
	boolean ajustar;

	int d0, d1;

	boolean executarComportamento;

	boolean sensorAtivado;

	// Automato
	int estado;
	final int espera = 0;
	final int primeiraDistancia = 1;
	final int segundaDistancia = 2;
	final int reta = 3;
	final int curvarEsquerda = 4;
	final int curvarDireita = 5;

	Semaphore acessoRobot, esperaTrabalho;//, desativar;

	Distancia distancia;

	public SeguirParede(Semaphore aR, RobotPlayer r, Distancia d) {
		acessoRobot = aR;
		robot = r;
		distancia = d;
		iniciarVariaveis();
	}

	private void iniciarVariaveis() {
		estado = espera;
		td = 0;
		d0 = 0;
		d1 = 0;
		executarComportamento = true;
		esperaTrabalho = new Semaphore(0);
//		desativar = new Semaphore(0);TODO
		sensorAtivado = false;
		ajustar = false;
	}

	public void ativar() {
		System.out.println("seguir ativar");
		estado = primeiraDistancia;
		esperaTrabalho.release();
//		desativar.drainPermits();TODO
	}

	public void desativar() {
		System.out.println("seguir desativar");
		estado = espera;
		esperaTrabalho.drainPermits();
//		try {
//			desativar.acquire();TODO
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	private void comportamentoSeguir() {
		while (executarComportamento) {
			switch (estado) {

			case espera: // 0
				System.out.println("seguir iniciar");
//				desativar.release();TODO
				td = 0;
				try {
					esperaTrabalho.acquire();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}

				break;

			case primeiraDistancia: // 1
				System.out.println("seguir ---" + estado);

				d0 = distancia.getDistancia();
				ajustar = true;

				if (estado == primeiraDistancia)
					estado = reta;
				break;

			case segundaDistancia: // 2
				System.out.println("seguir ---" + estado);

				d1 = distancia.getDistancia();
				ajustar= false;

				System.out.println("................seguir...........d0=" + d0 + ", d1=" + d1);
				if (estado == segundaDistancia) {
					if (d0 == d1)
						estado = reta;
					else if (d0 > d1)
						estado = curvarEsquerda;
					else if (d0 < d1)
						estado = curvarDireita;
				}
				break;

			case reta: // 3

				System.out.println("seguir ---" + estado);
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				robot.Reta(dReta);
				robot.Parar(false);
				acessoRobot.release();
				// td = dReta / velRobot;
				// td *= 1000*2;
				td = 1000;
				if (estado == reta) {
					adormecer(td);
					if(ajustar)
					estado = segundaDistancia;
					else estado = primeiraDistancia;
				}
				break;

			case curvarEsquerda:// 4
				System.out.println("seguir ---" + estado);
				int aEsq = (int) Math.toDegrees(Math.atan(Math.abs(d1 - d0) / (double) dReta));
				System.out.println(".......................................................alfa esquerda: " + aEsq);
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				robot.CurvarEsquerda(0, aEsq);
				robot.Parar(false);
				acessoRobot.release();
				// td = (int) ((4 * (aEsq * Math.PI) / 180) / velRobot);
				// td *= 1000 * 2;
				td = 1000;
				if (estado == curvarEsquerda) {
					adormecer(td);
					estado = primeiraDistancia;
				}
				break;

			case curvarDireita:// 5
				System.out.println("seguir --- " + estado);
				double alfa = Math.toDegrees(Math.atan(Math.abs(d1 - d0) / (double) dReta));
				int aDir = (int) alfa;
				System.out.println("........................................................ alfa direita: " + aDir);
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				robot.CurvarDireita(0, aDir);
				robot.Parar(false);
				acessoRobot.release();
				// td = (int) ((4 * (aDir * Math.PI) / 180) / velRobot);
				// td *= 1000 * 2;
				td = 1000;
				if (estado == curvarDireita) {
					adormecer(td);
					estado = primeiraDistancia;
				}
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
		td = 0;
	}

	public void run() {
		comportamentoSeguir();
		System.exit(1);
	}

	public static void main(String[] args) {

	}

}
