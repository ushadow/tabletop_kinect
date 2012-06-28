package edu.mit.yingyin.gui;

import javax.swing.JLabel;

public class StatusBar extends JLabel {

	private static final long serialVersionUID = 1L;

	public StatusBar() {
		super();
		setMessage("Ready");
	}
	
	public void setMessage(String message) {
		setText(" " + message);
	}
}
