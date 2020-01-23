import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class RobotPlayer extends Thread {

	RobotLego robot;
	FileOutputStream escrever;
	FileInputStream ler;
	boolean flagGravar;

	final String nomeFicheiro = "percurso.txt";

	public RobotPlayer(RobotLego r) {
		robot = r;
		iniciarVariaveis();
	}

	private void iniciarVariaveis() {
		flagGravar = false;
	}

	private void iniciarEscrever() {
		try {
			escrever = new FileOutputStream(nomeFicheiro);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void terminarEscrever() {
		try {
			if (escrever != null)
				escrever.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void Reta(int d) {
		robot.Reta(d);
		if (GUI.debug)
			robot.Parar(false);
		if (flagGravar) {
			GUI.myPrintFicheiro("Gravar Reta<" + d + ">");
			escrever("Reta<" + d + ">");
		} else
			GUI.myPrintFicheiro("Reproduzir Reta<" + d + ">");
	}

	public void CurvarDireita(int raio, int ang) {
		robot.CurvarDireita(raio, ang);
		if (GUI.debug)
			robot.Parar(false);
		if (flagGravar) {
			GUI.myPrintFicheiro("Gravar CurvarDireita<" + raio + "," + ang + ">");
			escrever("CurvarDireita<" + raio + "," + ang + ">");
		} else
			GUI.myPrintFicheiro("Reproduzir CurvarDireita<" + raio + "," + ang + ">");
	}

	public void CurvarEsquerda(int raio, int ang) {
		robot.CurvarEsquerda(raio, ang);
		if (GUI.debug)
			robot.Parar(false);
		if (flagGravar) {
			GUI.myPrintFicheiro("Gravar CurvarEsquerda<" + raio + "," + ang + ">");
			escrever("CurvarEsquerda<" + raio + "," + ang + ">");
		} else
			GUI.myPrintFicheiro("Reproduzir CurvarEsquerda<" + raio + "," + ang + ">");
	}

	public void Parar(boolean b) {
		robot.Parar(b);
		if (flagGravar) {
			GUI.myPrintFicheiro("Gravar Parar<" + b + ">");
			escrever("Parar<" + b + ">");
		} else
			GUI.myPrintFicheiro("Reproduzir Parar<" + b + ">");
	}

	public void setSensorLowspeed(int porto) {
		robot.setSensorLowspeed(porto);
	}

	public int SensorUS(int porto) {
		int d = robot.SensorUS(porto);
		return d;
	}

	public void comecarGravar() {
		flagGravar = true;
		iniciarEscrever();
	}

	public void pararGravar() {
		flagGravar = false;
		terminarEscrever();
	}

	private synchronized void escrever(String s) {
		try {
			for (int i = 0; i < s.length(); i++) {
				escrever.write(s.charAt(i));
			}
			escrever.write(';');// windows \r\n
			escrever.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reproduzir() {
		ArrayList<String> comportamentos = lerComportamentos();
		reproduzir(comportamentos);
	}

	public void reproduzirInverso() {
		ArrayList<String> comportamentos = lerComportamentos();
		Collections.reverse(comportamentos);
		GUI.myPrintFicheiro("" + comportamentos);
		System.out.println("" + comportamentos);
		reproduzirInverso(comportamentos);
	}

	private ArrayList<String> lerComportamentos() {
		ArrayList<String> comportamentos = new ArrayList<String>();
		try {
			ler = new FileInputStream(nomeFicheiro);
			String s = "";
			try {
				int c;
				while ((c = ler.read()) != -1) {
					char a = (char) c;
					if (a == ';') {
						comportamentos.add(s);
						s = "";
					} else
						s += a;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ler != null)
					ler.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return comportamentos;
	}

	private void reproduzir(ArrayList<String> comportamentos) {
		for (String comp : comportamentos) {
			String tipoComp = comp.substring(0, comp.indexOf('<'));
			if (tipoComp.equals("Reta")) {
				int distancia = Integer.parseInt(comp.substring(comp.indexOf('<') + 1, comp.indexOf('>')));
				Reta(distancia);
				adormecer((Math.abs(distancia) / 20) * 1000 * 2);
			} else if (tipoComp.equals("CurvarDireita")) {// raio, ang
				int raio = Integer.parseInt(comp.substring(comp.indexOf('<') + 1, comp.indexOf(',')));
				int angulo = Integer.parseInt(comp.substring(comp.indexOf(',') + 1, comp.indexOf('>')));
				CurvarDireita(raio, angulo);
				adormecer((int) ((Math.abs(raio) * (Math.abs(angulo) * Math.PI) / 180) / 20) * 1000 * 3);
			} else if (tipoComp.equals("CurvarEsquerda")) {
				int raio = Integer.parseInt(comp.substring(comp.indexOf('<') + 1, comp.indexOf(',')));
				int angulo = Integer.parseInt(comp.substring(comp.indexOf(',') + 1, comp.indexOf('>')));
				CurvarEsquerda(raio, angulo);
				adormecer((int) ((Math.abs(raio) * (Math.abs(angulo) * Math.PI) / 180) / 20) * 1000 * 3);
			} else if (tipoComp.equals("Parar")) {
				boolean sync = comp.substring(comp.indexOf('<') + 1, comp.indexOf('>')).equals("true") ? true : false;
				Parar(sync);
			}
		}
	}

	private void reproduzirInverso(ArrayList<String> comportamentos) {
		CurvarEsquerda(0, 181);
		robot.Parar(false);
		adormecer(2500);
		for (String comp : comportamentos) {
			String tipoComp = comp.substring(0, comp.indexOf('<'));
			if (tipoComp.equals("Reta")) {
				int distancia = Integer.parseInt(comp.substring(comp.indexOf('<') + 1, comp.indexOf('>')));
				Reta(distancia);
				adormecer((Math.abs(distancia) / 20) * 1000 * 2);
			} else if (tipoComp.equals("CurvarDireita")) {// raio, ang
				int raio = Integer.parseInt(comp.substring(comp.indexOf('<') + 1, comp.indexOf(',')));
				int angulo = Integer.parseInt(comp.substring(comp.indexOf(',') + 1, comp.indexOf('>')));
				CurvarEsquerda(raio, angulo);
				adormecer((int) ((Math.abs(raio) * (Math.abs(angulo) * Math.PI) / 180) / 20) * 1000 * 3);
			} else if (tipoComp.equals("CurvarEsquerda")) {
				int raio = Integer.parseInt(comp.substring(comp.indexOf('<') + 1, comp.indexOf(',')));
				int angulo = Integer.parseInt(comp.substring(comp.indexOf(',') + 1, comp.indexOf('>')));
				CurvarDireita(raio, angulo);
				adormecer((int) ((Math.abs(raio) * (Math.abs(angulo) * Math.PI) / 180) / 20) * 1000 * 3);
			} else if (tipoComp.equals("Parar")) {
				boolean sync = comp.substring(comp.indexOf('<') + 1, comp.indexOf('>')).equals("true") ? true : false;
				Parar(sync);
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
		System.exit(1);
	}

	public static void main(String[] args) {
	}
}
