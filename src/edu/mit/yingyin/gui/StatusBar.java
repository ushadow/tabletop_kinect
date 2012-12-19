package edu.mit.yingyin.gui;

import java.awt.Dimension;

import javax.swing.JLabel;

public class StatusBar extends JLabel {

	private static final long serialVersionUID = 1L;

	public StatusBar(int width, int height) {
		super();
		setMessage("Ready");
		setPreferredSize(new Dimension(width, height));
	}
	
	public void setMessage(String message) {
		setText(" " + message);
	}
}
