import java.io.*;
import java.net.*;

/**
 * Main class
 */
public class Main {
	/**
	 * Server port
	 */
	static int serverPort = 1200;

	/**
	 * Game board (null when no match is in progress)
	 */
	static public Board board = null;

	/**
	 * Server score
	 */
	static public int here;

	/**
	 * Client score
	 */
	static public int there;

	/**
	 * Board dimension
	 */
	static public int dim;

	/**
	 * Wraparound
	 */
	static public boolean w;

	/**
	 * Client socket output stream
	 */
	static public PrintStream os = null;

	/**
	 * Is server ready
	 */
	static public boolean hereReady;

	/**
	 * Is client ready
	 */
	static public boolean thereReady;

	/**
	 * Is server black
	 */
	static public boolean hereBlack;

	/**
	 * Server color
	 */
	static public byte hereColor;

	/**
	 * Client color
	 */
	static public byte thereColor;

	/**
	 * Prints command line help
	 */
	public static void help() {
		System.out.println("arguments:");
		System.out.println(" <boardSize>[w] [serverport]");
		System.out.println();
		System.out.println("where boardSize is an odd number, min 7 max 19");
		System.out.println("when w is present, board edges touch the opposite side (wraparound)");
	}

	/**
	 * Command help
	 * @param s Stream to print to
	 */
	public static void cmdHelp(PrintStream s) {
		s.println(".text            - converse with your opponent");
		s.println("ready            - ready for a new match");
		s.println("info             - game/match scores");
		s.println("Nnn..            - make a move ('A2', 'B15', etc)");
		s.println("resign           - resign");
		s.println("bye              - quit game");
		s.println("!                - display board again");
		s.println("?                - get this help text");
	}

	/**
	 * Prints game scores/info
	 * @param s Stream to print to
	 */
	public static void info(PrintStream s) {
		s.println();
		s.println("GAME SCORE - S: " + here + " C: " + there);

		if (board != null) {
			s.println("CAPTURES   - B: " + board.getScore((byte) 'B') + " W: " + board.getScore((byte) 'W'));
		} else {
			if (hereReady) s.println("server is ready");
			if (thereReady) s.println("client is ready");
		}

		s.println();
	}

	/**
	 * Prints game board
	 * @param s Stream to print to
	 */
	public static void printBoard(PrintStream s) {
		int x, y;
		s.println();
		s.print("    ");
		for (x = 0; x < dim; x ++) s.print((char) (x + 65) + " ");
		s.println();
		for (y = 0; y < dim; y ++) {
			if (y < 9) s.print("  "); else s.print(" ");
			s.print((y + 1));
			for (x = 0; x < dim; x ++) {
				if (x == 0) {
					if (board.lastY == y && board.lastX == 0) s.print("("); else s.print(" ");
				} else {
					if (board.lastY == y && board.lastX == x) s.print("(");
				}
	
				switch(board.getCell(x, y)) {
					case 'B': s.print("X"); break;
					case 'W': s.print("O"); break;
					default: 
						if (x == (dim - 1) / 2 && y == (dim - 1) / 2) s.print("+"); else s.print(".");
				}
				if (board.lastY == y && board.lastX == x) s.print(")");
				else if (board.lastY != y || board.lastX != x + 1) s.print(" ");
			}
			if (y < 9) s.print(" ");
			s.println((y + 1));
		}
		s.print("    ");
		for (x = 0; x < dim; x ++) s.print((char) (x + 65) + " ");
		s.println();
		if (s == System.out)
			s.println ((hereColor == board.player) ? "it's your turn" : "it's your opponent's turn");
		else
			s.println ((thereColor == board.player) ? "it's your turn" : "it's your opponent's turn");
	}

	/**
	 * Initialize game
	 */
	public static void gameInit() {
		hereReady = false;
		thereReady = false;
		board = new Board(dim, dim, w);
		if (hereBlack) {
			hereColor = 'B'; 
			thereColor = 'W';
		} else {
			hereColor = 'W';
			thereColor = 'B';
		}

		System.out.println();
		System.out.println("MATCH BEGINS");
		os.println();
		os.println("MATCH BEGINS");

		printBoard(System.out);
		printBoard(os);
	}

	/**
	 * Entry point
	 * @param argv Comand line arguments
	 */
	public static void main(String argv[]) {
		if (Math.random() > 0.5) hereBlack = true;

		if (argv.length < 1 || argv.length > 2) {
			help();
			System.exit(1);
		}

		if (argv[0].endsWith("w")) {
			argv[0] = argv[0].substring(0, argv[0].length() - 1);
			w = true;
		}

		try {
			dim = Integer.parseInt(argv[0]);
		} catch (NumberFormatException e) {
			System.out.println("invalid board size");
			System.exit(1);
		}

		if ((dim & 1) == 0 || dim < 7 || dim > 19) {
			System.out.println("invalid board size");
			System.exit(1);
		}

		if (argv.length == 2)
			try {
				serverPort = Integer.parseInt(argv[1]);
				if (serverPort < 1 || serverPort > 65535) {
					System.out.println("invalid server port");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.out.println("invalid server port");
				System.exit(1);
			}

		ServerSocket ss = null;
		String line;
		BufferedReader is = null;
		Socket cs = null;

		try {
			ss = new ServerSocket(serverPort);
		} catch (IOException e) {
			System.out.println("foiled! " + e);
			System.exit(1);
		}

		System.out.print((char) 27 + "[2J" + (char) 27 + "[H");
		System.out.println("!waiting for client");
		try {
			cs = ss.accept();
			is = new BufferedReader(new InputStreamReader(cs.getInputStream()));
			os = new PrintStream(cs.getOutputStream());
		} catch (IOException e) {
			System.out.println("foiled! " + e);
			System.exit(1);
		}

		System.out.println("!client is connected");
		System.out.println("Type ? for help");
		System.out.println();

		os.print((char) 27 + "[2J" + (char) 27 + "[H");
		os.println("Welcome to a game of Gobang");
		os.println("Type ? for help");
		os.print("The board is " + dim + "x" + dim);
		if (w) os.print(" wraparound");
		os.println();
		os.println();

		gameInit();

		new Local().start();

		try {
			while (true) {
				line = is.readLine();
				if (line == null) {
					System.out.println("!client disconnected");
					System.exit(0);
				}

				if (line.startsWith(".")) {
					System.out.println(":" + line.substring(1));
				} else {
					line.toLowerCase();
					if (line.equals("bye")) {
						System.out.println("!client says goodbye");
						os.close();
						System.exit(0);
					}

					if (line.equals("?")) {
						cmdHelp(os);
						continue;
					}

					if (line.equals("!")) {
						if (board == null) System.out.println("there is no match in progress!");
							else printBoard(os);
						continue;
					}

					if (line.equals("info")) {
						info(os);
						continue;
					}

					if (line.equals("ready")) {
						if (Main.board != null) {
							os.println("A match is already in progress");
							continue;
						}

						if (thereReady) {
							os.println("You were ready anyway");
							continue;
						}

						thereReady = true;
						System.out.println("!client is ready for the next match");

						if (hereReady && thereReady) Main.gameInit();

						continue;
					}

					if (line.equals("resign") && board != null) {
						os.println("!you resign");
						System.out.println("!your opponent has resigned");
						board = null;
						hereReady = false;
						thereReady = false;
						here ++;
						info(os);
						info(System.out);
						hereBlack = false;
						continue;
					}

					if (line.length() > 0 && board != null) {
						if (board.player == thereColor) {
							int x = (byte) line.charAt(0) - (byte) 'a';
							int y;
							if (x >= 0 && x < dim) {
										line = line.substring(1);
								try {
									y = Integer.parseInt(line) - 1;
									if (y >= 0 && y < dim) {
										int r = board.move(new Point(x, y));
										if (r == -1) os.println("invalid move");
										if (r == 0 || r == 1) {
											printBoard(System.out);
											printBoard(os);
										}
										if (r == 1) {
											os.println("you win!");
											System.out.println("you lose");
											board = null;
											hereReady = false;
											thereReady = false;
											there ++;
											info(os);
											info(System.out);
											hereBlack = true;
										}
										continue;
									}
								} catch (NumberFormatException e) {
								}
							}
						}
					}

					os.println("?what");
				}
			}
		} catch (IOException e) {
			System.out.println("foiled! " + e);
			System.exit(1);
		}
	}
}
