import java.net.*;
import java.io.*;

public class Local extends Thread {
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		try {
			while (true) {
				line = in.readLine();

				if (line.startsWith(".")) {
					Main.os.println(":" + line.substring(1));
				} else {
					line.toLowerCase();
					if (line.equals("bye")) {
						Main.os.println("!server says goodbye");
						System.exit(0);
					}

					if (line.equals("?")) {
						Main.cmdHelp(System.out);
						continue;
					}

					if (line.equals("!")) {
						if (Main.board == null) System.out.println("there is no match in progress!");
							else Main.printBoard(System.out);
						continue;
					}

					if (line.equals("info")) {
						Main.info(System.out);
						continue;
					}

					if (line.equals("ready")) {
						if (Main.board != null) {
							System.out.println("A match is already in progress");
							continue;
						}

						if (Main.hereReady) {
							System.out.println("You were ready anyway");
							continue;
						}

						Main.hereReady = true;
						Main.os.println("!server is ready for the next match");

						if (Main.hereReady && Main.thereReady) Main.gameInit();

						continue;
					}

					if (line.equals("resign") && Main.board != null) {
						Main.os.println("!your opponent has resigned");
						System.out.println("!you resign");
						Main.board = null;
						Main.hereReady = false;
						Main.thereReady = false;
						Main.there ++;
						Main.info(Main.os);
						Main.info(System.out);
						Main.hereBlack = true;
						continue;
					}

					if (line.length() > 0 && Main.board != null) {
						if (Main.board.player == Main.hereColor) {
							int x = (byte) line.charAt(0) - (byte) 'a';
							int y;
							if (x >= 0 && x < Main.dim) {
										line = line.substring(1);
								try {
									y = Integer.parseInt(line) - 1;
									if (y >= 0 && y < Main.dim) {
										int r = Main.board.move(new Point(x, y));
										if (r == -1) System.out.println("invalid move");
										if (r == 0 || r == 1) {
											Main.printBoard(System.out);
											Main.printBoard(Main.os);
										}
										if (r == 1) {
											Main.os.println("you lose");
											System.out.println("you win!");
											Main.board = null;
											Main.hereReady = false;
											Main.thereReady = false;
											Main.here ++;
											Main.info(Main.os);
											Main.info(System.out);
											Main.hereBlack = false;
										}
										continue;
									}
								} catch (NumberFormatException e) {
								}
							}
						}
					}

					System.out.println("?what");
				}
			}
		} catch (IOException e) {
			System.out.println("local io dies");
			System.exit(1);
		}
    }
}
