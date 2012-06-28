package edu.mit.yingyin.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/*
 * The AlphaComposite class defines an additional alpha value that is applied to 
 * the source alpha. 
 * This value is applied as if an implicit SRC_IN rule were first applied to the 
 * source pixel against a pixel with the indicated alpha by multiplying both the 
 * raw source alpha and the raw source colors by the alpha in the 
 * AlphaComposite. This leads to the following equation for producing the alpha 
 * used in the Porter and Duff blending equation:
 *
 * As = Asr * Aac 
 *
 * All of the raw source color components need to be multiplied by the alpha in 
 * the AlphaComposite instance. Additionally, if the source was not in 
 * premultiplied form then the color components also need to be multiplied by 
 * the source alpha. Thus, the equation for producing the source color 
 * components for the Porter and Duff equation depends on whether the source 
 * pixels are premultiplied or not:
 *
 * Cs = Csr * Asr * Aac     (if source is not premultiplied)
 * Cs = Csr * Aac           (if source is premultiplied) 
 */
public class ImageCompositeUtils {

	/**
	 * Adds an overlay image to the destination image. The source is composited 
	 * over the destination (Porter-Duff Source Over Destination rule).
	 *	Cs	a color component of the source pixel in premultiplied form 
	 *	Fs = 1 and Fd = (1-As), thus:
	 *	
     *  Ar = As + Ad*(1-As)
     *  Cr = Cs + Cd*(1-As)
	 *	
	 * @param dst Destination image which will be changed
	 * @param src Source image.
	 * @param alpha the constant alpha to multiply with the alpha of the source
	 */
	public static void addOverlay(BufferedImage dst, BufferedImage src, 
	                              float alpha) {
		Graphics2D g2d = dst.createGraphics();

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
		                                               alpha);
		g2d.setComposite(ac);
		g2d.drawImage(src, 0, 0, null);
		g2d.dispose();
	}
}
