/**
 * Cell.java, part of Lights Open
 * Author: Tor E Hagemann <hagemt@rpi.edu>
 */
package edu.rpi.hagemt.proglang.hw1;

import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

// FIXME merely a structural element, inline w/ LightsOpenFrame?
public class Cell extends JPanel {
	private static final long serialVersionUID = 9007632080038765089L;
	public int x, y, z;
	public boolean s;
	public JLabel l;

	public Cell(int i, int j, boolean state) {
		x = (i < 0) ? -i : i;
		y = (j < 0) ? -j : j;
		s = state;
    z = 0;
    /* TODO is this the best setup? */
		l = new JLabel(Integer.toString(z), JLabel.CENTER);
		l.setFont(new Font("Monospace", Font.BOLD, 48));
		setLayout(new GridBagLayout());
    add(l);
	}
}
