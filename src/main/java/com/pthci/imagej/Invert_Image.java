/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
  *
 * Invert_image image code 2022 Prof Phil Threlfall-Holmes, TH Collaborative Innovation
 * modification from tutorial template, licence terms unmodified.
 */

package com.pthci.imagej;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * True invert greyscale of 8-bit, 16-bit, or 32-bit greyscale image, or a color RGB image.
 *
 * i.e. not just inverting LUT so the image appears black-on-white rather than white on black
 * this re-writes the pixel values, so that 0 becomes 255, 1 becomes 254 etc.
 *
 * This is useful for numerical edge finding codes for thresholded images.
 * Typically for a shadowgraph image, raw image has a black object of interest on a white background.
 * or more normally in high speed imaging, a dark grey object on a light grey background.
 * First step in image processing is normally finding the difference from a blank reference background
 * image, to improve the signal to noise ratio, but the result of that difference is typically near 0
 * where there is background in your image and near 255 where there is the shadowgraphed object of interest.
 * i.e the image with the background difference removed, has become a white object on a black background.
 * This is a pain if we sometimes have images where we remove the background, and sometimes don't,
 * because we need 2 different versions of the numerical edsge finding code, one for black-on-white
 * thresholds, one for white-on-black.
 * 
 *
 * Based on Johannes Schindelin's template for processing each pixel of either
 * GRAY8, GRAY16, GRAY32 or COLOR_RGB images.
 *
 * @author Phil Threlfall-Holmes
 */
public class Invert_Image implements PlugInFilter {
	protected ImagePlus image;

	// image property members
	private int width   ;
	private int height  ;
	private int type    ;
	private int nSlices ;
	
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}

		image = imp;
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	} //end public int setup(String arg, ImagePlus imp)
	//-----------------------------------------------------


	@Override
	public void run(ImageProcessor ip) {
		width   = ip.getWidth();    //in pixel units
		height  = ip.getHeight();
		type    = image.getType();
		nSlices = image.getStackSize();
		process(image);
		image.updateAndDraw();
	} //end public void run(ImageProcessor ip)
	//-----------------------------------------------------


	/**
	 * Process an image.
	 * <p>
	 * Please provide this method even if {@link ij.plugin.filter.PlugInFilter} does require it;
	 * the method {@link ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)} can only
	 * handle 2-dimensional data.
	 * </p>
	 * <p>
	 * If your plugin does not change the pixels in-place, make this method return the results and
	 * change the {@link #setup(java.lang.String, ij.ImagePlus)} method to return also the
	 * <i>DOES_NOTHING</i> flag.
	 * </p>
	 *
	 * @param image the image (possible multi-dimensional)
	 */
	public void process(ImagePlus image) {
		// slice numbers start with 1 for historical reasons
		for (int i = 1; i <= nSlices; i++)
			process( image.getStack().getProcessor(i) );
	} //end public void process(ImagePlus image) 
	//-----------------------------------------------------


	// Select processing method depending on image type
	public void process(ImageProcessor ip) {
		if      (type == ImagePlus.GRAY8)       process( (byte[])  ip.getPixels() );
		else if (type == ImagePlus.GRAY16)		process( (short[]) ip.getPixels() );
		else if (type == ImagePlus.GRAY32)		process( (float[]) ip.getPixels() );
		else if (type == ImagePlus.COLOR_RGB)	process( (int[])   ip.getPixels() );
		else {
			throw new RuntimeException("not supported");
		}
	} //end public void process(ImageProcessor ip)
	//-----------------------------------------------------


	// processing of GRAY8 images
	public void process(byte[] pixels) {
		//pixels = ip.getPixels() is a 1-D array, not a 2D array as you would intuit, so pixels[x+y*width] instead of pixels[x,y]
		//It would be slow constantly redoing explicit integer addition and multiplication as per the example
		//So define a pixelIndex - which can just be incremented ++ for row scanning
		//Also,
		//Images are 8-bit (unsigned, i.e. values between 0 and 255).
		//Java has no data type for unsigned 8-bit integers: the byte type is signed , so we have to use the & 0xff dance
		//(a Boolean AND operation) to make sure that the value is treated as unsigned integer,
		//do the sum as integers (the constant 255 is implicitly integer, unless defined otherwise, but here we specify an
		//explict cast to make the intention clearer ) and then cast back to byte
		//The & operator promotes to int so doesn't need an explicit cast.
		//It feels very wasteful promoting to int rather than just bit shifting, but a casual internet search
		//suggests it is the Java solution.
		for( int pixelPos=0; pixelPos<(width*height); pixelPos++ ) {
			pixels[pixelPos] = (byte)( (int)255- (pixels[pixelPos] & 0xff ) ) ;
		}
	} //end public void process(byte[] pixels)
  //-----------------------------------------------------


	// processing of GRAY16 images
	public void process(short[] pixels) {
		//Java short is 16 bit signed, so -32,768 to 32,767
		//Java int is 32 bit, signed -2,147,483,648 to 2,147,483,647
		//so we can safely promote to int type for the subtraction
		//and then cast back to short
	  for( int pixelPos=0; pixelPos<(width*height); pixelPos++ ) {
			pixels[pixelPos] = (short)( (int)65535 - (pixels[pixelPos] & 0x00ffff ) ) ;
		}
	} //end public void process(short[] pixels)
  //-----------------------------------------------------


	// processing of GRAY32 images	
	public void process(float[] pixels) {
		//IJ.log("public void process GREY32");
		//Java int is 32 bit, signed -2,147,483,648 to 2,147,483,647
		//Java long is 64 bit, signed -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807
		//So it would be possible for ImageJ to implement 32-bit greyscale using the int data type
		//in the same way as (signed)byte is used to implement (unsigned)8-bit
		// or (signed)short for (unsigned)16-bit greyscale
		//But it is actually implemented by float, 0.0 (black) to 1.0 (white)
		//float data type is 32 bit
		//With a float data type, we don't need to cast
		for( int pixelPos=0; pixelPos<(width*height); pixelPos++ ) {
			pixels[pixelPos] = (float)1.0 - pixels[pixelPos] ;
		}
	} //end public void process(float[] pixels)
  //-----------------------------------------------------


	// processing of COLOR_RGB images
	public void process(int[] pixels) {
		IJ.log("public void process RGB");
/*	
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (int)value;
			}
		}
*/		
	} //end public void process(int[] pixels)
  //-----------------------------------------------------


/*=================================================================================*/


	public void showAbout() {
		IJ.showMessage("Invert 8 bit",
			"True invert of 8 bit image - not just inverting LUT: reset pixel value 0 to 255 etc"
		);
	} //end public void showAbout()
  //-----------------------------------------------------


/*=================================================================================*/

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) throws Exception {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		// see: https://stackoverflow.com/a/7060464/1207769
		Class<?> clazz = Invert_Image.class;
		java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
		java.io.File file = new java.io.File(url.toURI());
		System.setProperty("plugins.dir", file.getAbsolutePath());

		// start ImageJ
		new ImageJ();

		//ImagePlus image = IJ.openImage("d:/CaBER example.tif");
		//ImagePlus image = IJ.openImage("d:/test16bitBandWinvert.tif");
		ImagePlus image = IJ.openImage("d:/test32bitBandWinvert.tif");
		
		
		// open the Clown sample
		//ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		image.show();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}  //end public static void main(String[] args)
  
/*=================================================================================*/
  
}  //end public class Invert_Image
//========================================================================================
//                         end public class Invert_Image
//========================================================================================