package com.communote.common.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link ImageScaler}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ImageScalerTest {

    /** this width is used to scale to landscape image */
    private final int landscapeWidth = 500;

    /** this height is used to scale to landscape image */
    private final int landscapeHeight = 200;

    /** this width is used to scale to portrait image */
    private final int portraitWidth = 250;

    /** this height is used to scale to portrait image */
    private final int portraitHeight = 400;

    /**
     * checks the result of resizing the image
     * 
     * @param data
     *            the image data
     * 
     * @param width
     *            the width
     * 
     * @param height
     *            the height
     */
    private void checkEqualityResult(byte[] data, int width, int height) {
        BufferedImage image = ImageHelper.byteToImage(data);
        Assert.assertEquals(image.getWidth(), width);
        Assert.assertEquals(image.getHeight(), height);
    }

    /**
     * checks the result of resizing the image
     * 
     * @param data
     *            the image data
     * 
     * @param width
     *            the width
     * 
     * @param height
     *            the height
     */
    private void checkResult(byte[] data, int width, int height) {
        BufferedImage image = ImageHelper.byteToImage(data);
        Assert.assertTrue((image.getWidth() <= width));
        Assert.assertTrue((image.getHeight() <= height));
    }

    /**
     * creates an image
     * 
     * @param width
     *            the width of the created image
     * 
     * @param height
     *            the height of the created image
     * 
     * @return the byte array of an image
     * 
     * @throws IOException
     *             in case of an exception
     */
    private byte[] createImage(int width, int height) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);

        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", baos);

        baos.flush();
        byte[] resultImageAsRawBytes = baos.toByteArray();
        baos.close();

        return resultImageAsRawBytes;
    }

    /**
     * Tests {@link ImageScaler}.
     * 
     * @throws IOException
     *             in case of an exception
     */
    @Test
    public void testResizeImage() throws IOException {
        ImageScaler scaler;
        byte[] data;
        for (ImageFormatType type : ImageFormatType.values()) {
            // LANDSCAPE MODE
            scaler = new ImageScaler(landscapeHeight, landscapeWidth, type);

            // resize smaller images to landscape
            // no scaling
            data = scaler.resizeImage(createImage(240, 80));
            checkResult(data, landscapeWidth, landscapeHeight);

            // resize smaller images to landscape
            // no scaling
            data = scaler.resizeImage(createImage(100, 190));
            checkResult(data, landscapeWidth, landscapeHeight);

            // resize larger images to landscape
            // scale landscape to width [ 4 >= 2.5 ]
            data = scaler.resizeImage(createImage(600, 150));
            checkResult(data, landscapeWidth, landscapeHeight);

            // resize larger images to landscape
            // scale landscape to height [ 2.115 >= 2.5 ]
            data = scaler.resizeImage(createImage(550, 260));
            checkResult(data, landscapeWidth, landscapeHeight);

            // resize larger images to landscape
            // scale portrait to height [ 0.625 >= 2.5 ]
            data = scaler.resizeImage(createImage(400, 640));
            checkResult(data, landscapeWidth, landscapeHeight);

            // PORTRAIT MODE
            scaler = new ImageScaler(portraitHeight, portraitWidth, type);

            // resize smaller images to portrait
            // no scaling
            data = scaler.resizeImage(createImage(240, 300));
            checkResult(data, portraitWidth, portraitHeight);

            // no scaling
            data = scaler.resizeImage(createImage(100, 300));
            checkResult(data, portraitWidth, portraitHeight);

            // resize larger images to portrait
            // scale landscape to width [ 2.333 >= 0.625 ]
            data = scaler.resizeImage(createImage(700, 300));
            checkResult(data, portraitWidth, portraitHeight);

            // resize larger images to portrait
            // scale portrait to height [ 0.476 >= 0.625 ]
            data = scaler.resizeImage(createImage(400, 840));
            checkResult(data, portraitWidth, portraitHeight);

            // resize larger images to portrait
            // scale portrait to width [ 0.666 >= 0.625 ]
            data = scaler.resizeImage(createImage(290, 435));
            checkResult(data, portraitWidth, portraitHeight);

            // resize larger images to portrait
            // extreme scalling test :-) [ 250 >= 0.625 ]
            data = scaler.resizeImage(createImage(2500, 10));
            checkResult(data, portraitWidth, portraitHeight);
        }
    }

    /**
     * Tests {@link ImageScaler}.
     * 
     * @throws IOException
     *             in case of an exception
     */
    @Test
    public void testResizeImageWithDrawBackground() throws IOException {
        ImageScaler scaler;
        byte[] data;
        for (ImageFormatType type : ImageFormatType.values()) {
            // LANDSCAPE MODE
            scaler = new ImageScaler(landscapeHeight, landscapeWidth, type);
            scaler.setDrawBackground(true);

            // resize smaller images to landscape
            // no scaling
            // result should have exactly the new size
            data = scaler.resizeImage(createImage(100, 100));
            checkEqualityResult(data, landscapeWidth, landscapeHeight);

            // resize larger images to landscape
            // scale landscape to height [ 0.8 >= 2.5 ]
            // result should have exactly the new size
            data = scaler.resizeImage(createImage(200, 250));
            checkEqualityResult(data, landscapeWidth, landscapeHeight);

            // resize larger images to landscape
            // scale landscape to width [ 2.95 >= 2.5 ]
            // result should have exactly the new size
            data = scaler.resizeImage(createImage(650, 220));
            checkEqualityResult(data, landscapeWidth, landscapeHeight);

            // PORTRAIT MODE
            scaler = new ImageScaler(portraitHeight, portraitWidth, type);
            scaler.setDrawBackground(true);

            // resize smaller images to portrait
            // no scaling
            // result should have exactly the new size
            data = scaler.resizeImage(createImage(200, 300));
            checkEqualityResult(data, portraitWidth, portraitHeight);

            // resize larger images to portrait
            // scale landscape to width [ 1.75 >= 0.625 ]
            // result should have exactly the new size
            data = scaler.resizeImage(createImage(350, 200));
            checkEqualityResult(data, portraitWidth, portraitHeight);

            // resize larger images to portrait
            // scale quadrat to width [ 1 >= 0.625 ]
            // result should have exactly the new size
            data = scaler.resizeImage(createImage(400, 400));
            checkEqualityResult(data, portraitWidth, portraitHeight);
        }
    }
}
