package edu.mit.yingyin.tabletop.controllers;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.gui.ImageFrame;

/**
 * Controls the viewing of image values.
 * @author yingyin
 *
 */
public class ViewImageValueController extends MouseAdapter {
  private static final Logger LOGGER = Logger.getLogger(
      ViewImageValueController.class.getName());
  private final ImageFrame imageFrame;
  private final ImageComponent ic;
  private Point point = new Point(221, 371);

  public ViewImageValueController(String name, ImageComponent ic) {
    imageFrame = new ImageFrame(name, ic);
    this.ic = ic;
    ic.addMouseListener(this);
    displayValue();
  }
  
  public ImageFrame frame() { return imageFrame; }
  
  @Override
  public void mouseClicked(MouseEvent me) {
    LOGGER.info("Mouse clicked.");
    point = me.getPoint();
    displayValue();
  }
  
  public void update() { displayValue(); }
  
  private void displayValue() {
    int v = ic.getImageValue(point.x, point.y);
    imageFrame.setStatus("(" + point.x + ", " + point.y + ") = " + v);
  }
}
