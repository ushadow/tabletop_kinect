package edu.mit.yingyin.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/**
 * Swing component for displaying BufferedImage.
 * @author yingyin
 *
 */
public class ImageComponent extends JComponent {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Image to be displayed.
	 */
	protected BufferedImage img = null;
		
	public ImageComponent(Dimension d) {
	  setLayout(null);
	  setOpaque(false);
	  setPreferredSize(d);
	}
	
	/**
	 * Creates an <code>ImageComponent</code> with an initial image.
	 * @param img
	 */
	public ImageComponent(BufferedImage img) {
	  this(new Dimension(img.getWidth(), img.getHeight()));
	  setImage(img);
	}
	    
  public void setImage(BufferedImage img) {
    this.img = img;
    repaint();
  }
  
  /**
   * Called by paint().
   */
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (img != null) {
  	  ((Graphics2D)g).drawImage(img, null, 0, 0);
  	  g.dispose();
    }
  }
}
