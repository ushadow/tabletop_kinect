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
   * 
   * @param x
   * @param y
   * @return an integer pixel in the default RGB color model and default sRGB 
   *    colorspace.
   */
  public int getImageValue(int x, int y) {
    if (img == null || x < 0 || x >= img.getWidth() || y < 0 || 
        y >= img.getHeight())
      return 0;
    
    return img.getRGB(x, y);
  }
  
  public BufferedImage image() { return img; }
  
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
