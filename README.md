InvertImage ImageJ plugin

MIT license, but with no advertising clause

Public domain, copyright Prof Phil Threlfall-Holmes, TH Collaborative Innovation, 2022, 2023
ver2 Oct2023: added status and progress updates: helpful for a large stack

True inversion of 8, 16 or 32-bit greyscale, and RGB color images - not just inverting LUT

i.e. not just inverting LUT so the image *appears* black-on-white rather than white on black.  
This plugin re-writes the pixel values, so that 0 becomes 255, 1 becomes 254 etc. (example for 8 bit greyscale)

for RGB colour images;

  Red   [ 255,  0 ,  0  ] -> Cyan    [  0 , 255, 255 ];
	
  Green [  0 , 255,  0  ] -> Magenta [ 255,  0 , 255 ];
	
  Blue  [  0 ,  0 , 255 ] -> Yellow  [ 255, 255,  0  ];
	
  Black [  0 ,  0 ,  0  ] -> White   [ 255, 255, 255 ];
	
  White [ 255, 255, 255 ] -> Black   [  0 ,  0 ,  0  ];
	

This is useful for numerical edge finding codes for thresholded images:
Typically for a shadowgraph image, raw image has a black object of interest on a white background.
(or more normally in high speed imaging, a dark grey object on a light grey background).
First step in image processing is normally finding the difference from a blank reference background
image, to improve the signal to noise ratio, but the result of that difference is typically near 0
where there is background in your image and near 255 where there is the shadowgraphed object of interest.
i.e the image with the background difference removed, has become a negative, a white object on a black background.
This is a pain if we sometimes have images where we remove the background, and sometimes don't,
because we need two different versions of the numerical edge finding code, one for black-on-white
thresholds, one for white-on-black.

So for numerical codes I run, I want background white to be pixel value 255, and object black to be 0

Auto local threshold methods do this, depending of your selection of "White objects on black background"

But the normal global theresholding method, depending of your selection of "Dark background",
just makes it an Inverting LUT, or not: i.e. whether 0 is displayed as white or black.

Additionally, for some of the advanced codes with density determination to get sub-pixel resolution of an edge,
I needed a 16-bit true invert, not just a black-white swap on a thresholded image.  
Where the opacity of the object may differ at different places in the image. Especially, a very thin 
clear liquid filament, in not perfect telecentric illumination and lensing, may go very light near the point of
break, a few pixel width or below, in which case even local thresholding methods are unreliable.

Based on Johannes Schindelin's plugin tutorial template for processing each pixel of either
GRAY8, GRAY16, GRAY32 or COLOR_RGB images.

And for COLOR_RGB, acknowledging Kieren Holland's RGB_Recolor as a useful exemplar of use of the ColorProcessor class


