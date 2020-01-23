import java.util.concurrent.Semaphore;

public class Vaguear extends Thread {

	RobotPlayer robot;
	static final int velRobot = 20; // cm por s

	int dmin, dmax, amin, amax, rmin, rmax;
	int td, dr;

	String nomeRobot;
	// boolean ligacaoSucesso;

	boolean executarComportamento;

	// Automato
	int estado;
	final int espera = 0;
	final int escolha = 1;
	final int reta = 2;
	final int curvarEsquerda = 3;
	final int curvarDireita = 4;
	final int parar = 5;
	final int dormir = 6;

	Semaphore acessoRobot, esperaTrabalho, desativar;

	private void iniciarVariaveis() {
		dmin = 10;
		dmax = 75;
		amin = 0;
		amax = 90;
		rmin = 10;
		rmax = 50;
		estado = espera;
		td = 0;
		dr = 0;
		executarComportamento = true;
		esperaTrabalho = new Semaphore(0);
		desativar = new Semaphore(0);
	}

	public void ativar() {
		System.out.println("vaguear ativar");
		estado = escolha;
		esperaTrabalho.release();
		desativar.drainPermits();
	}

	public void desativar() {
		System.out.println("vaguear desativar");
		estado = espera;
		esperaTrabalho.drainPermits();
		try {
			desativar.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Vaguear(Semaphore aR, RobotPlayer r) {
		acessoRobot = aR;
		robot = r;
		iniciarVariaveis();
	}

	private void comportamentoVaguear() {
		while (executarComportamento) {
			double i;
			switch (estado) {

			case espera:
				System.out.println("vaguear --- " + estado);
				desativar.release();
				try {
					esperaTrabalho.acquire();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
				break;

			case escolha:
				System.out.println("vaguear --- " + estado);
				i = Math.random() * 4;
				td = 0;
				if (estado == escolha) {
					if (i >= 0.0 && i < 1.0)
						estado = reta;
					else if (i >= 1.0 && i < 2.0)
						estado = curvarDireita;
					else if (i >= 2.0 && i < 3.0)
						estado = curvarEsquerda;
					else if (i >= 3.0 && i <= 4.0)
						estado = parar;
				}
				else 
					pararRobot();
				break;

			case reta:
				System.out.println("vaguear --- " + estado);
				
				dr = (int) (dmin + (dmax - dmin) * Math.random());
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				robot.Reta(dr);
				robot.Parar(false);
				acessoRobot.release();
				td = dr / velRobot;
				td *= 1000 * 2;
				if (estado == reta)
					estado = dormir;
				else 
					pararRobot();
				break;

			case curvarEsquerda:
				System.out.println("vaguear --- " + estado);
				
				int aEsq = (int) (amax * Math.random());
				int rEsq = (int) (rmin + (rmax - rmin) * Math.random());
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				robot.CurvarEsquerda(rEsq, aEsq);
				robot.Parar(false);
				acessoRobot.release();
				td = (int) ((rEsq * (aEsq * Math.PI) / 180) / velRobot);
				td *= 1000 * 2;
				if (estado == curvarEsquerda)
					estado = dormir;
				else 
					pararRobot();
				break;

			case curvarDireita:
				System.out.println("vaguear --- " + estado);
				
				int aDir = (int) (amax * Math.random());
				int rDir = (int) (rmin + (rmax - rmin) * Math.random());
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				robot.CurvarDireita(rDir, aDir);
				robot.Parar(false);
				acessoRobot.release();
				td = (int) ((rDir * (aDir * Math.PI) / 180) / velRobot);
				td *= 1000 * 2;
				if (estado == curvarDireita)
					estado = dormir;
				else 
					pararRobot();
				break;

			case parar:
				System.out.println("vaguear --- " + estado);
				
				try {
					acessoRobot.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				robot.Parar(false);
				acessoRobot.release();
				if (estado == parar)
					estado = escolha;
				else 
					pararRobot();
				break;

			case dormir:
				System.out.println("vaguear --- " + estado);
				
				try {
					Thread.sleep(td);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				td = 0;
				if (estado == dormir)
					estado = escolha;
				
				else 
					pararRobot();
				break;
			}
		}
	}

	private void pararRobot(){
		try {
			acessoRobot.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		robot.Parar(true);
		acessoRobot.release();
	}
	
	public void run() {
		comportamentoVaguear();
		System.exit(1);
	}

	public static void main(String[] args) {

	}

}
