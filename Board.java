/**
 * Represents a Gobang game (board and score)
 */
public class Board {
	byte[] data;
	int w;
	int h;
	boolean wraparound;
	public int lastX = -1;
	public int lastY = -1;

	/**
	 * Next player.
	 */
	public byte player = 'B';

	/**
	 * Is game over.
	 */
	public boolean gameOver = false;

	int score[] = {0, 0};

	/**
	 * Constructor
	 * @param width Board width
	 * @param height Board height
	 * @param wrp Wraparound
	 */
	public Board(int width, int height, boolean wrp) {
		data = new byte[height * width];
		w = width;
		h = height;
		wraparound = wrp;
	}

	/**
	 * Gets a cell
	 * @param p Point
	 * @return Cell value
	 */
	public byte getCell(Point p) {
		return data[p.y * w + p.x];
	}

	/**
	 * Gets a cell
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return Cell value
	 */
	public byte getCell(int x, int y) {
		return data[y * w + x];
	}

	/**
	 * Sets a cell
	 * @param p Point
	 * @param v Value
	 */
	public void setCell(Point p, byte v) {
		data[p.y * w + p.x] = v;
	}

	/**
	 * Sets a cell
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param v Value
	 */
	public void setCell(int x, int y, byte v) {
		data[y * w + x] = v;
	}

	/**
	 * Get score (number of captured stones so far)
	 * @param p Player ('B' or 'W')
	 */
	public int getScore(byte p) {
		switch(p) {
			case 'B': return score[0];
			case 'W': return score[1];
		}
		return -1;
	}

	/**
	 * Set score
	 * @param p Player ('B' or 'W')
	 * @param s Score
	 */
	void setScore(byte p, int s) {
		switch(p) {
			case 'B':
				score[0] = s;
				break;
			case 'W':
				score[1] = s;
				break;
		}
	}

	/**
	 * Increments score (by 2)
	 * @param p Player ('B' or 'W')
	 */
	void incScore(byte p) {
		switch(p) {
			case 'B':
				score[0] += 2;
				break;
			case 'W':
				score[1] += 2;
				break;
		}
	}

	/**
	 * Moves Point in direction.
	 * @param p Point
	 * @param d Direction (0-7, 0 is north, positive rotation (1 is NW and so on))
	 * @return False if the Point slips off the edge of the board (always true when wraparound is active)
	 */
	public boolean advance(Point p, int d) {
		switch(d) {
			case 0: p.y --; break;
			case 1: p.y --; p.x --; break;
			case 2: p.x --; break;
			case 3: p.y ++; p.x --; break;
			case 4: p.y ++; break;
			case 5: p.y ++; p.x ++; break;
			case 6: p.x ++; break;
			case 7: p.y --; p.x ++; break;
		}

		if (wraparound) {
			if (p.y < 0) p.y = h - 1; else if (p.y >= h) p.y = 0;
			if (p.x < 0) p.x = w - 1; else if (p.x >= w) p.x = 0;
			return true;
		} else {
			if (p.y < 0) return false;
			if (p.y == h) return false;
			if (p.x < 0) return false;
			if (p.x == w) return false;
			return true;
		}
	}

	/**
	 * Counts the length of the streak of same values on the board in a direction
	 * @param p Point of origin
	 * @param dir Direction
	 * @return Array of two integers - first is the length of the streak, second is the color that ends the streak (-1 if edge)
	 */
	public int[] streak(Point p, int dir) {
		byte i, c = getCell(p);
		int limit = w > h ? w : h;
		int l = 0;
		while(true) {
			if (!advance(p, dir))
				return new int[] {l, -1};
			if ((i = getCell(p)) != c)
				return new int[] {l, i};
			l ++;
			if (l == limit)
				return new int[] {l, c};
		}
	}

	/**
	 * Prints a representation of the board on stdout
	 */
	public void print() {
		int x, y;
		for (y = 0; y < h; y ++) {
			for (x = 0; x < w; x ++) {
				System.out.print (" ");
				switch (getCell(x, y)) {
					case 'B': System.out.print ("B"); break;
					case 'W': System.out.print ("W"); break;
					default: System.out.print (".");
				}
			}
			System.out.println();
		}
	}

	/**
	 * Returns the color of the opposing player
	 * @return The color of the opposing player
	 */
	public byte opposing(byte c) {
		if (c == 'B') return 'W';
		if (c == 'W') return 'B';
		return 0;
	}

	/**
	 * Makes a move
	 * @param p Point
	 * @return -1 if the move is illegal, 1 if the move wins the game
	 */
	public int move(Point p) {
		if (gameOver) return -1;
		if (getCell(p) != 0) return -1;
		setCell(p, player);

		lastX = p.x;
		lastY = p.y;

		for (int d = 0; d < 8; d ++) {
			Point q = p.copy();
			if (!advance(q, d)) continue;
			if (getCell(q) == opposing(player)) {
				int st[] = streak(q.copy(), d);
				if (st[0] == 1 && st[1] == player) {
					setCell(q, (byte) 0);
					advance(q, d);
					setCell(q, (byte) 0);
					incScore(player);
				}
			}
		}

		if (getScore(player) >= 10) {
			gameOver = true;
			return 1;
		}

		for (int d = 0; d < 4; d ++) {
			int s = 0;
			int st[] = streak(p.copy(), d);
			s = st[0];
			st = streak(p.copy(), d + 4);
			s += st[0];
			if (s > 3) {
				gameOver = true;
				return 1;
			}
		}

		player = opposing(player);

		return 0;
	}
}
