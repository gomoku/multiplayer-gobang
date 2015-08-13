/**
 * Defines an intersection on the board.
 */
public class Point {
	public int x;
	public int y;

	/**
	 * Constructor.
	 * @param _x Initial X
	 * @param _y Initial Y
	 */
	public Point(int _x, int _y) {
		x = _x; y = _y;
	}

	/**
	 * Clones the Point
	 * @return A clone
	 */
	public Point copy() {
		return new Point(x, y);
	}
}
