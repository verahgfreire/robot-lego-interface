
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.awt.event.ActionEvent;

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField nomeRobotTextField;
	private JTextField offsetEsqTextField;
	private JTextField offsetDirTextField;
	private JTextField raioTextField;
	private JTextField anguloTextField;
	private JTextField distanciaTextField;
	private JTextField logTextField;
	private JButton esquerdaButton;
	private JButton frenteButton;
	private JButton pararButton;
	private JButton direitaButton;
	private JButton retaguardaButton;
	private JCheckBox vaguearCheckBox;
	private JCheckBox evitarCheckBox;
	private JCheckBox gestorCheckBox;
	private JCheckBox seguirParedeCheckbox;
	private JCheckBox ReproduzirInversoCheckBox;

	boolean executarGUI;
	boolean vaguearOnOff, evitarOnOff, seguirOnOff, gestorOnOff;
	boolean executarV, executarE, executarG, executarS;
	boolean reproduzir, reproduzirInverso;

	Evitar evitar;
	Vaguear vaguear;
	SeguirParede seguir;
	Gestor gestor;
	Distancia MyDistancia;

	FileOutputStream escrever;
	FileInputStream ler;

	/**
	 * As minhas variaveis
	 */
	public static boolean debug;
	String nomeRobot;
	int offsetEsq, offsetDir, raio, angulo, distancia;
	boolean onOff, executar;
	RobotPlayer robotPlayer;
	RobotLego robotLego;

	Semaphore acessoRobot;

	// Automato
	int estado;
	final int esperar = 0;
	final int comecarVaguear = 1;
	final int pararVaguear = 2;
	final int comecarEvitar = 3;
	final int pararEvitar = 4;
	final int comecarSeguir = 5;
	final int pararSeguir = 6;
	final int comecarGestor = 7;
	final int pararGestor = 8;
	final int comecarReproduzir = 9;
	final int pararReproduzir = 10;
	final int comecarReproduzirInv = 11;
	final int pararReproduzirInv = 12;

	private static JTextField ficheiroTextField;
	private JCheckBox reproduzirCheckBox;
	private JCheckBox gravarCheckBox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		GUI frame = new GUI();
		frame.run();
	}

	public void run() {
		while (executarGUI) {
			switch (estado) {
			case esperar:
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (vaguearOnOff && !executarV)
					estado = comecarVaguear;
				else if (evitarOnOff && !executarE)
					estado = comecarEvitar;
				else if (seguirOnOff && !executarS)
					estado = comecarSeguir;
				else if (gestorOnOff && !executarG)
					estado = comecarGestor;
				else if (!vaguearOnOff && executarV)
					estado = pararVaguear;
				else if (!evitarOnOff && executarE)
					estado = pararEvitar;
				else if (!seguirOnOff && executarS)
					estado = pararSeguir;
				else if (!gestorOnOff && executarG)
					estado = pararGestor;
				else if (reproduzir)
					estado = comecarReproduzir;
				else if (reproduzirInverso)
					estado = comecarReproduzirInv;
				break;

			case comecarVaguear:
				if (!executarV) {
					vaguear.ativar();
					executarV = true;
				}
				estado = esperar;
				break;

			case pararVaguear:
				if (executarV) {
					vaguear.desativar();
					executarV = false;
				}
				estado = esperar;
				break;

			case comecarEvitar:
				if (!executarE) {
					evitar.ativar();
					executarE = true;
				}
				estado = esperar;
				break;

			case pararEvitar:
				if (executarE) {
					evitar.desativar();
					executarE = false;
				}
				estado = esperar;
				break;

			case comecarSeguir:
				if (!executarS) {
					seguir.ativar();
					executarS = true;
				}
				estado = esperar;
				break;

			case pararSeguir:
				if (executarS) {
					seguir.desativar();
					executarS = false;
				}
				estado = esperar;
				break;

			case comecarGestor:
				if (!executarG) {
					gestor.ativar();
					executarG = true;
				}
				estado = esperar;
				break;

			case pararGestor:
				if (executarG) {
					gestor.desativar();
					executarG = false;
				}
				estado = esperar;
				break;

			case comecarReproduzir:
				robotPlayer.reproduzir();
				myPrint("acabou de reproduzir");
				reproduzir = false;
				reproduzirCheckBox.setSelected(reproduzir);
				estado = esperar;
				break;

			case pararReproduzir:
				break;

			case comecarReproduzirInv:
				robotPlayer.reproduzirInverso();
				myPrint("acabou de reproduzir inverso");
				reproduzirInverso = false;
				ReproduzirInversoCheckBox.setSelected(reproduzirInverso);
				estado = esperar;
				break;

			case pararReproduzirInv:
				break;
			}
		}
		System.exit(1);
	}

	public void iniciarVariaveis() {
		executarGUI = true;
		robotLego = new RobotLego();
		robotPlayer = new RobotPlayer(robotLego);
		nomeRobot = new String("");
		onOff = false;
		debug = true;
		offsetEsq = 0;
		offsetDir = 0;
		raio = 10;
		angulo = 90;
		distancia = 20;
		executar = true;
		MyDistancia = new Distancia();
		acessoRobot = new Semaphore(1);
		evitar = new Evitar(acessoRobot, robotLego);
		evitar.start();
		vaguear = new Vaguear(acessoRobot, robotPlayer);
		vaguear.start();
		seguir = new SeguirParede(acessoRobot, robotPlayer, MyDistancia);
		seguir.start();
		gestor = new Gestor(acessoRobot, robotPlayer, vaguear, seguir, MyDistancia);
		gestor.start();

		executarV = false;
		executarE = false;
		executarG = false;
		executarS = false;

		vaguearOnOff = false;
		evitarOnOff = false;
		gestorOnOff = false;
		seguirOnOff = false;

		reproduzir = false;
		reproduzirInverso = false;
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		createJFrame();
	}

	private void createJFrame() {

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (onOff) {
					robotLego.Parar(true);// TODO robotlego?
					robotLego.CloseNXT();
				}
				try {
					executar = false;
					Thread.sleep(500);
					System.exit(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});

		iniciarVariaveis();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 582, 452);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel nomeRobotLabel = new JLabel("Nome do Robot:");
		nomeRobotLabel.setBounds(97, 43, 108, 16);
		contentPane.add(nomeRobotLabel);

		nomeRobotTextField = new JTextField();
		nomeRobotTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nomeRobot = nomeRobotTextField.getText();
				myPrint("Nome robot: " + nomeRobot);
				carregarConfigGUIPorNome(nomeRobot + ".txt");
			}
		});
		nomeRobotTextField.setBounds(205, 38, 179, 26);
		contentPane.add(nomeRobotTextField);
		nomeRobotTextField.setColumns(10);
		nomeRobotTextField.setText(nomeRobot);

		JRadioButton onOffRadioButton = new JRadioButton("On/Off");
		onOffRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOff = onOffRadioButton.isSelected();
				if (onOff) {
					onOff = robotLego.OpenNXT(nomeRobot); // TODO robotlego?
				} else
					robotLego.CloseNXT();
				onOffRadioButton.setSelected(onOff);
				enableBotoes(onOff);
				myPrint("On/Off: " + onOff);
			}
		});
		onOffRadioButton.setBounds(396, 39, 77, 23);
		contentPane.add(onOffRadioButton);
		onOffRadioButton.setSelected(onOff);

		offsetEsqTextField = new JTextField();
		offsetEsqTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					offsetEsq = Integer.parseInt(offsetEsqTextField.getText());
					robotLego.AjustarVME(offsetEsq);
					myPrint("Offset Esq: " + offsetEsq);
				} catch (NumberFormatException exc) {
					myPrint("Offset Esquerda so pode ser um valor inteiro!");
					offsetEsqTextField.setText(Integer.toString(offsetEsq));
				}
			}
		});
		offsetEsqTextField.setEnabled(false);
		offsetEsqTextField.setBounds(6, 6, 30, 26);
		contentPane.add(offsetEsqTextField);
		offsetEsqTextField.setColumns(10);
		offsetEsqTextField.setText("" + offsetEsq);

		JLabel offsetEsqLabel = new JLabel("Offset Esquerda");
		offsetEsqLabel.setBounds(40, 11, 100, 16);
		contentPane.add(offsetEsqLabel);

		offsetDirTextField = new JTextField();
		offsetDirTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					offsetDir = Integer.parseInt(offsetDirTextField.getText());
					robotLego.AjustarVMD(offsetDir);
					myPrint("Offset Dir: " + offsetDir);
				} catch (NumberFormatException exc) {
					myPrint("Offset Direita so pode ser um valor inteiro!");
					offsetDirTextField.setText(Integer.toString(offsetDir));
				}
			}
		});
		offsetDirTextField.setEnabled(false);
		offsetDirTextField.setColumns(10);
		offsetDirTextField.setBounds(546, 6, 30, 26);
		contentPane.add(offsetDirTextField);
		offsetDirTextField.setText("" + offsetDir);

		JLabel offsetDirLabel = new JLabel("Offset Direita");
		offsetDirLabel.setBounds(457, 11, 89, 16);
		contentPane.add(offsetDirLabel);

		raioTextField = new JTextField();
		raioTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					raio = Integer.parseInt(raioTextField.getText());
					myPrint("Raio: " + raio);
				} catch (NumberFormatException exc) {
					myPrint("Raio tem de ser um valor inteiro!");
					raioTextField.setText(Integer.toString(raio));
				}
			}
		});
		raioTextField.setEnabled(false);
		raioTextField.setColumns(10);
		raioTextField.setBounds(77, 71, 30, 26);
		contentPane.add(raioTextField);
		raioTextField.setText("" + raio);

		anguloTextField = new JTextField();
		anguloTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					angulo = Integer.parseInt(anguloTextField.getText());
					myPrint("Angulo: " + angulo);
				} catch (NumberFormatException exc) {
					myPrint("Angulo tem de ser um valor inteiro!");
					anguloTextField.setText(Integer.toString(angulo));
				}
			}
		});
		anguloTextField.setEnabled(false);
		anguloTextField.setColumns(10);
		anguloTextField.setBounds(235, 71, 30, 26);
		contentPane.add(anguloTextField);
		anguloTextField.setText("" + angulo);

		distanciaTextField = new JTextField();
		distanciaTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					distancia = Integer.parseInt(distanciaTextField.getText());
					myPrint("Distancia: " + distancia);
				} catch (NumberFormatException exc) {
					myPrint("Distancia tem de ser um valor inteiro!");
					distanciaTextField.setText(Integer.toString(distancia));
				}
			}
		});
		distanciaTextField.setEnabled(false);
		distanciaTextField.setColumns(10);
		distanciaTextField.setBounds(394, 71, 30, 26);
		contentPane.add(distanciaTextField);
		distanciaTextField.setText("" + distancia);

		JLabel raioLabel = new JLabel("Raio:");
		raioLabel.setBounds(42, 76, 37, 16);
		contentPane.add(raioLabel);

		JLabel raioCmLabel = new JLabel("cm");
		raioCmLabel.setBounds(107, 76, 19, 16);
		contentPane.add(raioCmLabel);

		JLabel anguloLabel = new JLabel("Angulo:");
		anguloLabel.setBounds(185, 76, 49, 16);
		contentPane.add(anguloLabel);

		JLabel anguloGrausLabel = new JLabel("graus");
		anguloGrausLabel.setBounds(265, 76, 37, 16);
		contentPane.add(anguloGrausLabel);

		JLabel distanciaLabel = new JLabel("Distancia:");
		distanciaLabel.setBounds(325, 76, 69, 16);
		contentPane.add(distanciaLabel);

		JLabel distanciaCmLabel = new JLabel("cm");
		distanciaCmLabel.setBounds(425, 76, 30, 16);
		contentPane.add(distanciaCmLabel);

		JCheckBox debugCheckBox = new JCheckBox("Debug");
		debugCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				debug = debugCheckBox.isSelected();
				myPrint("Debug: " + debug);
			}
		});
		debugCheckBox.setBounds(6, 301, 77, 23);
		contentPane.add(debugCheckBox);
		debugCheckBox.setSelected(debug);

		JLabel logLabel = new JLabel("Consola:");
		logLabel.setBounds(6, 333, 62, 16);
		contentPane.add(logLabel);

		logTextField = new JTextField();
		logTextField.setEnabled(false);
		logTextField.setBounds(66, 327, 510, 26);
		contentPane.add(logTextField);
		logTextField.setColumns(10);

		frenteButton = new JButton("Frente");
		frenteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				robotPlayer.Reta(distancia);
				// robotLego.Parar(false);
				myPrint("Andar em frente " + distancia + " cm.");
			}
		});
		frenteButton.setEnabled(false);
		frenteButton.setBounds(185, 122, 117, 41);
		contentPane.add(frenteButton);

		pararButton = new JButton("Parar");
		pararButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				robotPlayer.Parar(true);
				myPrint("Parar");
			}
		});
		pararButton.setEnabled(false);
		pararButton.setForeground(Color.RED);
		pararButton.setBounds(185, 163, 117, 41);
		contentPane.add(pararButton);

		retaguardaButton = new JButton("Retaguarda");
		retaguardaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				robotPlayer.Reta(-distancia);
				// robotLego.Parar(false);
				myPrint("Andar na retaguarda " + distancia + " cm.");
			}
		});
		retaguardaButton.setEnabled(false);
		retaguardaButton.setBounds(185, 204, 117, 41);
		contentPane.add(retaguardaButton);

		esquerdaButton = new JButton("Esquerda");
		esquerdaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				robotPlayer.CurvarEsquerda(raio, angulo);
				// robotLego.Parar(false);
				myPrint("Rodar a esquerda " + angulo + " graus e " + raio + " cm.");
			}
		});
		esquerdaButton.setEnabled(false);
		esquerdaButton.setBounds(56, 163, 117, 41);
		contentPane.add(esquerdaButton);

		direitaButton = new JButton("Direita");
		direitaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				robotPlayer.CurvarDireita(raio, angulo);
				// robotLego.Parar(false);
				myPrint("Rodar a direita " + angulo + " graus e " + raio + " cm.");
			}
		});
		direitaButton.setEnabled(false);
		direitaButton.setBounds(314, 163, 117, 41);
		contentPane.add(direitaButton);

		vaguearCheckBox = new JCheckBox("Vaguear");
		vaguearCheckBox.setEnabled(false);
		vaguearCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vaguearOnOff = vaguearCheckBox.isSelected();
				esquerdaButton.setEnabled(!vaguearOnOff);
				frenteButton.setEnabled(!vaguearOnOff);
				direitaButton.setEnabled(!vaguearOnOff);
				retaguardaButton.setEnabled(!vaguearOnOff);
			}
		});
		vaguearCheckBox.setBounds(459, 124, 89, 23);
		contentPane.add(vaguearCheckBox);

		evitarCheckBox = new JCheckBox("Evitar");
		evitarCheckBox.setEnabled(false);
		evitarCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evitarOnOff = evitarCheckBox.isSelected();
				esquerdaButton.setEnabled(!evitarOnOff);
				frenteButton.setEnabled(!evitarOnOff);
				direitaButton.setEnabled(!evitarOnOff);
				retaguardaButton.setEnabled(!evitarOnOff);
			}
		});
		evitarCheckBox.setBounds(459, 104, 89, 23);
		contentPane.add(evitarCheckBox);

		gestorCheckBox = new JCheckBox("Gestor");
		gestorCheckBox.setEnabled(false);
		gestorCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gestorOnOff = gestorCheckBox.isSelected();
				esquerdaButton.setEnabled(!gestorOnOff);
				frenteButton.setEnabled(!gestorOnOff);
				direitaButton.setEnabled(!gestorOnOff);
				retaguardaButton.setEnabled(!gestorOnOff);
			}
		});
		gestorCheckBox.setBounds(459, 148, 89, 23);
		contentPane.add(gestorCheckBox);

		seguirParedeCheckbox = new JCheckBox("Seguir Parede");
		seguirParedeCheckbox.setEnabled(false);
		seguirParedeCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				seguirOnOff = seguirParedeCheckbox.isSelected();
				esquerdaButton.setEnabled(!seguirOnOff);
				frenteButton.setEnabled(!seguirOnOff);
				direitaButton.setEnabled(!seguirOnOff);
				retaguardaButton.setEnabled(!seguirOnOff);
			}
		});
		seguirParedeCheckbox.setBounds(459, 168, 117, 23);
		contentPane.add(seguirParedeCheckbox);

		ReproduzirInversoCheckBox = new JCheckBox("Reproduzir Inverso");
		ReproduzirInversoCheckBox.setEnabled(false);
		ReproduzirInversoCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reproduzirInverso = ReproduzirInversoCheckBox.isSelected();
			}
		});
		ReproduzirInversoCheckBox.setBounds(426, 290, 150, 23);
		contentPane.add(ReproduzirInversoCheckBox);

		JLabel ficheiroLabel = new JLabel("Ficheiro:");
		ficheiroLabel.setBounds(6, 367, 62, 16);
		contentPane.add(ficheiroLabel);

		ficheiroTextField = new JTextField();
		ficheiroTextField.setEnabled(false);
		ficheiroTextField.setColumns(10);
		ficheiroTextField.setBounds(66, 361, 510, 63);
		contentPane.add(ficheiroTextField);

		reproduzirCheckBox = new JCheckBox("Reproduzir");
		reproduzirCheckBox.setEnabled(false);
		reproduzirCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reproduzir = reproduzirCheckBox.isSelected();
			}
		});
		reproduzirCheckBox.setBounds(426, 268, 150, 23);
		contentPane.add(reproduzirCheckBox);

		gravarCheckBox = new JCheckBox("Gravar");
		gravarCheckBox.setEnabled(false);
		gravarCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean gravar = gravarCheckBox.isSelected();
				if (gravar)
					robotPlayer.comecarGravar();// TODO gravar
				else
					robotPlayer.pararGravar();
			}
		});
		gravarCheckBox.setBounds(426, 246, 150, 23);
		contentPane.add(gravarCheckBox);

		JCheckBox gravarGUICheckBox = new JCheckBox("Gravar Config");
		gravarGUICheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guardarConfigGUI(nomeRobot, offsetDir, offsetEsq);
				gravarGUICheckBox.setSelected(false);
			}
		});
		gravarGUICheckBox.setBounds(12, 227, 128, 23);
		contentPane.add(gravarGUICheckBox);

		JCheckBox carregarGUICheckBox = new JCheckBox("Carregar Config");
		carregarGUICheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				carregarConfigGUI();
				carregarGUICheckBox.setSelected(false);
			}
		});
		carregarGUICheckBox.setBounds(12, 246, 139, 23);
		contentPane.add(carregarGUICheckBox);

		setVisible(true);
	}

	private void myPrint(String t) {
		if (debug)
			logTextField.setText(t);
		else
			logTextField.setText("");
	}

	public static void myPrintFicheiro(String t) {
		ficheiroTextField.setText(t);
	}

	private void enableBotoes(boolean b) {
		offsetDirTextField.setEnabled(b);
		offsetEsqTextField.setEnabled(b);
		raioTextField.setEnabled(b);
		anguloTextField.setEnabled(b);
		distanciaTextField.setEnabled(b);
		esquerdaButton.setEnabled(b);
		frenteButton.setEnabled(b);
		direitaButton.setEnabled(b);
		retaguardaButton.setEnabled(b);
		pararButton.setEnabled(b);
		vaguearCheckBox.setEnabled(b);
		evitarCheckBox.setEnabled(b);
		gestorCheckBox.setEnabled(b);
		seguirParedeCheckbox.setEnabled(b);
		gravarCheckBox.setEnabled(b);
		reproduzirCheckBox.setEnabled(b);
		ReproduzirInversoCheckBox.setEnabled(b);
	}

	private void guardarConfigGUI(String nomeRobot, int offsetDir, int offsetEsq) {
		ArrayList<String> configs = new ArrayList<String>();
		configs.add("offsetDir<" + offsetDir + ">");
		configs.add("offsetEsq<" + offsetEsq + ">");

		String nomeFicheiro = "" + nomeRobot + ".txt";
		try {
			escrever = new FileOutputStream(nomeFicheiro);

			for (String c : configs) {
				try {
					for (int i = 0; i < c.length(); i++) {
						escrever.write(c.charAt(i));
					}
					escrever.write(';');// windows \r\n
					escrever.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (escrever != null)
					escrever.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void carregarConfigGUI() {
		FileDialog fd = new FileDialog(this, "Choose a file", FileDialog.LOAD);
		fd.setDirectory("/Users/verafreire/Documents/7 SEMESTRE/FSO/FSO-Trabalho4"); // TODO
																						// URL
		fd.setFile("*.txt");
		fd.setVisible(true);
		String nomeFicheiro = fd.getFile();
		carregarConfigGUIPorNome(nomeFicheiro);
	}

	private void carregarConfigGUIPorNome(String nomeFicheiro) {
		if (nomeFicheiro != null) {
			try {
				ler = new FileInputStream(nomeFicheiro);
				nomeRobot = nomeFicheiro.substring(0, nomeFicheiro.indexOf("."));
				nomeRobotTextField.setText(nomeRobot);
				String s = "";
				try {
					int c;
					while ((c = ler.read()) != -1) {
						char a = (char) c;
						if (a == ';') {
							String tipoComp = s.substring(0, s.indexOf('<'));
							if (tipoComp.equals("offsetDir")) {
								offsetDir = Integer.parseInt(s.substring(s.indexOf('<') + 1, s.indexOf('>')));
								offsetDirTextField.setText(Integer.toString(offsetDir));
							}
							if (tipoComp.equals("offsetEsq")) {
								offsetEsq = Integer.parseInt(s.substring(s.indexOf('<') + 1, s.indexOf('>')));
								offsetEsqTextField.setText(Integer.toString(offsetEsq));
							}
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
		}
	}
}
