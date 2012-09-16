/**
 * LightsOpenFrame.java, part of Lights Open
 * Author: Tor E Hagemann <hagemt@rpi.edu>
 */
package edu.rpi.proglang.hw1;

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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class LightsOpenFrame extends JFrame implements MouseListener, SwingConstants {
	private static final long serialVersionUID = -8678965842858286808L;
	private JPanel container;
	private Cell[][] grid;
	private int toggleCount;
	private LinkedList<Dimension> moves;

	public LightsOpenFrame(ArrayList<boolean[]> state) {
		int min_width = Integer.MAX_VALUE;
		for (boolean[] b : state) {
			if (b == null) {
				min_width = 0;
			} else if (b.length < min_width) {
				min_width = b.length;
			}
		}
		if (state.size() < 1 || min_width == 0) {
			throw new IllegalArgumentException("Invalid state!");
		}
		container = new JPanel(new GridLayout(state.size(), min_width, 5, 5));
		grid = new Cell[state.size()][min_width];
		moves = new LinkedList<Dimension>();
		for (int i = 0; i < state.size(); ++i) {
			for (int j = 0; j < min_width; ++j) {
				container.add(grid[i][j] = new Cell(i, j, state.get(i)[j]));
				grid[i][j].setPreferredSize(new Dimension(192, 192));
				grid[i][j].setBackground((grid[i][j].s) ? Color.WHITE : Color.BLACK);
				grid[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
				grid[i][j].addMouseListener(this);
			}
		}
		container.setBackground(Color.DARK_GRAY);
		container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getContentPane().add(container);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Lights Open (t = " + (toggleCount = 0) + ")");
		pack();
	}

	private void setEnabled(Cell c, boolean state) {
		c.setBackground((c.s = state) ? Color.WHITE : Color.BLACK);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Cell c;
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			c = (Cell)(e.getComponent());
			setEnabled(c, !c.s);
			if (c.x > 0) {
				setEnabled(grid[c.x - 1][c.y], !grid[c.x - 1][c.y].s);
			}
			if (c.y > 0) {
				setEnabled(grid[c.x][c.y - 1], !grid[c.x][c.y - 1].s);
			}
			if (c.x + 1 < grid.length) {
				setEnabled(grid[c.x + 1][c.y], !grid[c.x + 1][c.y].s);
			}
			if (c.y + 1 < grid[c.x].length) {
				setEnabled(grid[c.x][c.y + 1], !grid[c.x][c.y + 1].s);
			}
			setTitle("Lights Open (t = " + ++toggleCount + ")");
			c.l.setText(Integer.toString(++c.z));
			moves.push(new Dimension(c.x, c.y));
			break;
		case MouseEvent.BUTTON3:
			if (moves.isEmpty()) {
				return;
			}
			mouseExited(e);
			Dimension d = moves.pop();
			c = grid[d.width][d.height];
			setEnabled(c, !c.s);
			if (c.x > 0) {
				setEnabled(grid[c.x - 1][c.y], !grid[c.x - 1][c.y].s);
			}
			if (c.y > 0) {
				setEnabled(grid[c.x][c.y - 1], !grid[c.x][c.y - 1].s);
			}
			if (c.x + 1 < grid.length) {
				setEnabled(grid[c.x + 1][c.y], !grid[c.x + 1][c.y].s);
			}
			if (c.y + 1 < grid[c.x].length) {
				setEnabled(grid[c.x][c.y + 1], !grid[c.x][c.y + 1].s);
			}
			setTitle("Lights Open (t = " + --toggleCount + ")");
			c.l.setText(Integer.toString(--c.z));
			mouseEntered(e);
			break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Cell c = (Cell)(e.getComponent());
		c.setBackground(Color.RED);
		if (c.x > 0) {
			grid[c.x - 1][c.y].setBackground(Color.YELLOW);
		}
		if (c.y > 0) {
			grid[c.x][c.y - 1].setBackground(Color.YELLOW);
		}
		if (c.x + 1 < grid.length) {
			grid[c.x + 1][c.y].setBackground(Color.YELLOW);
		}
		if (c.y + 1 < grid[c.x].length) {
			grid[c.x][c.y + 1].setBackground(Color.YELLOW);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		Cell c = (Cell)(e.getComponent());
		setEnabled(c, c.s);
		if (c.x > 0) {
			setEnabled(grid[c.x - 1][c.y], grid[c.x - 1][c.y].s);
		}
		if (c.y > 0) {
			setEnabled(grid[c.x][c.y - 1], grid[c.x][c.y - 1].s);
		}
		if (c.x + 1 < grid.length) {
			setEnabled(grid[c.x + 1][c.y], grid[c.x + 1][c.y].s);
		}
		if (c.y + 1 < grid[c.x].length) {
			setEnabled(grid[c.x][c.y + 1], grid[c.x][c.y + 1].s);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) { }

	public static void main(String... args) {
		ArrayList<boolean[]> boardstate = new ArrayList<boolean[]>();
		for (String s : args) {
			File f = new File(s);
			if (f.canRead()) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(f));
					try {
						String line;
						while ((line = reader.readLine()) != null) {
							if (line.isEmpty() || line.startsWith("#")) {
								continue;
							}
							boolean[] row = new boolean[line.length()];
							for (int i = 0; i < line.length(); ++i) {
								row[i] = (line.charAt(i) == 'W');
							}
							boardstate.add(row);
						}
					} catch (IOException ioe) {
						System.err.println("Cannot parse " + s + "!");
					} finally {
						reader.close();
					}
					final ArrayList<boolean[]> final_state = (ArrayList<boolean[]>)(boardstate.clone());
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new LightsOpenFrame(final_state).setVisible(true);
						}
					});
				} catch (Exception e) {
					continue;
				} finally {
					boardstate.clear();
				}
			}
		}
	}
}
