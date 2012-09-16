/**
 * LightsOpenFrame.java, part of Lights Open
 * Author: Tor E Hagemann <hagemt@rpi.edu>
 */
package edu.rpi.hagemt.proglang.hw1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class LightsOpenFrame extends javax.swing.JFrame
  implements MouseListener, LightsOpenConstants {
	private static final long serialVersionUID = -8678965842858286808L;
	private JPanel container;
	private Cell[][] grid;
  /* extra representation for record-keeping */
	protected int toggleCount;
	protected LinkedList<Dimension> moves;

	public LightsOpenFrame(ArrayList<boolean[]> state) {
    /* Find the thinest row of the given boardstate */
		int min_width = Integer.MAX_VALUE;
		for (boolean[] b : state) {
			if (b == null) {
				min_width = 0;
			} else if (b.length < min_width) {
				min_width = b.length;
			}
		}
		if (state.size() < 1 || min_width == 0) {
			throw new IllegalArgumentException("state cannot contain zero-length entries");
		}
		moves = new LinkedList<Dimension>();
    /* Initialize the frame's GUI components */
		container = new JPanel(new GridLayout(state.size(), min_width, PADDING, PADDING));
		grid = new Cell[state.size()][min_width];
		for (int i = 0; i < state.size(); ++i) {
			for (int j = 0; j < min_width; ++j) {
				container.add(grid[i][j] = new Cell(i, j, state.get(i)[j]));
				grid[i][j].setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
				grid[i][j].setBackground((grid[i][j].s) ? Color.WHITE : Color.BLACK);
				grid[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
				grid[i][j].addMouseListener(this);
			}
		}
		container.setBackground(Color.DARK_GRAY);
		container.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
    /* Initialize properties of the frame */
		getContentPane().add(container);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Lights Open (t = " + (toggleCount = 0) + ")");
		pack();
	}

	private void setEnabled(Cell c, boolean state) {
		c.setBackground((c.s = state) ? Color.WHITE : Color.BLACK);
	}

  private void hovered(Cell c, Color bg) {
    if (bg != null) {
      c.setBackground(bg);
    } else {
      setEnabled(c, c.s);
    }
  }

  public boolean toggle(int x, int y) {
    try {
      Cell c = grid[x][y];
      setEnabled(c, !c.s);
    } catch (IndexOutOfBoundsException ioobe) {
      return false;
    }
    return true;
  }

  public boolean toggleNeighborhood(int x, int y) {
      boolean success = toggle(x, y);
      if (success) {
  			success &= toggle(x - 1, y);
	  		success &= toggle(x, y - 1);
		  	success &= toggle(x + 1, y);
			  success &= toggle(x, y + 1);
      }
      return success;
  }

	@Override
	public void mouseClicked(MouseEvent e) {
    Cell c;
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			c = (Cell)(e.getComponent());
      toggleNeighborhood(c.x, c.y);
			c.l.setText(Integer.toString(++c.z));
			setTitle("Lights Open (t = " + ++toggleCount + ")");
      /* Record the click for undo stack */
			moves.push(new Dimension(c.x, c.y));
			break;
		case MouseEvent.BUTTON3:
			if (moves.isEmpty()) {
				return;
			}
      /* Trigger an undo */
			mouseExited(e); // clear the colorings
			Dimension d = moves.pop();
			c = grid[d.width][d.height];
      toggleNeighborhood(c.x, c.y);
			c.l.setText(Integer.toString(--c.z));
			setTitle("Lights Open (t = " + --toggleCount + ")");
			mouseEntered(e); // reset the colorings
			break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
    Cell c = (Cell)(e.getComponent());
    hovered(c, Color.RED);
		if (c.x > 0) {
			hovered(grid[c.x - 1][c.y], Color.ORANGE);
		}
		if (c.y > 0) {
			hovered(grid[c.x][c.y - 1], Color.YELLOW);
		}
		if (c.x + 1 < grid.length) {
			hovered(grid[c.x + 1][c.y], Color.GREEN);
		}
		if (c.y + 1 < grid[c.x].length) {
			hovered(grid[c.x][c.y + 1], Color.BLUE);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
    Cell c = (Cell)(e.getComponent());
    hovered(c, null);
		if (c.x > 0) {
			hovered(grid[c.x - 1][c.y], null);
		}
		if (c.y > 0) {
			hovered(grid[c.x][c.y - 1], null);
		}
		if (c.x + 1 < grid.length) {
			hovered(grid[c.x + 1][c.y], null);
		}
		if (c.y + 1 < grid[c.x].length) {
			hovered(grid[c.x][c.y + 1], null);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
    // TODO provide feedback?
  }

	@Override
	public void mouseReleased(MouseEvent e) {
    // TODO provide feedback?
  }

	public static void main(String... args) {
		ArrayList<boolean[]> boardstate = new ArrayList<boolean[]>();
    /* Treat all the arguments as paths to boardstate files */
		for (String s : args) {
			File f = new File(s);
			if (f.canRead()) {
				try {
          /* This mess makes sure Java cleans up after itself */
					BufferedReader reader = new BufferedReader(new FileReader(f));
					try {
						String line;
						while ((line = reader.readLine()) != null) {
              /* Ignore empty lines and comments, disregarding whitespace*/
              line = line.trim();
							if (line.isEmpty() || line.startsWith(COMMENT_SEQUENCE)) {
								continue;
							}
              /* Read a line from the file as boardstate */
							boolean[] row = new boolean[line.length()];
							for (int i = 0; i < line.length(); ++i) {
								row[i] = (line.charAt(i) == ACTIVE_STATE);
							}
							boardstate.add(row);
						}
					} catch (IOException ioe) {
						System.err.println("[ERROR] '" + s + "' (cannot parse file)");
					} finally {
						reader.close();
					}
          /* Instantiate a local copy of the state for the thread */
					final ArrayList<boolean[]> final_state = new ArrayList<boolean[]>(boardstate);
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new LightsOpenFrame(final_state).setVisible(true);
						}
					});
				} catch (Exception e) {
          /* Just dump all errors for now FIXME */
          e.printStackTrace();
					continue;
				} finally {
          /* Wipe the slate clean */
					boardstate.clear();
				}
			}
		}
	}
}
