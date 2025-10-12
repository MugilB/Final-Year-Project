package com.securevoting.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class SteganographyService {

    public byte[] embedData(byte[] data) throws IOException {
        File coverImageFile = new File("cover.png");
        if (!coverImageFile.exists()) {
            throw new IOException("cover.png not found! Please place it in the project root.");
        }
        BufferedImage coverImage = ImageIO.read(coverImageFile);
        BufferedImage stegoImage = embed(coverImage, data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(stegoImage, "png", baos);
        return baos.toByteArray();
    }

    public byte[] extractData(byte[] stegoImageData) throws IOException {
        BufferedImage stegoImage = ImageIO.read(new ByteArrayInputStream(stegoImageData));
        return extract(stegoImage);
    }

    private BufferedImage embed(BufferedImage image, byte[] data) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] dataLengthBytes = new byte[4];
        dataLengthBytes[0] = (byte) (data.length >> 24);
        dataLengthBytes[1] = (byte) (data.length >> 16);
        dataLengthBytes[2] = (byte) (data.length >> 8);
        dataLengthBytes[3] = (byte) data.length;

        boolean[] dataLengthBits = bytesToBits(dataLengthBytes);
        boolean[] dataBits = bytesToBits(data);

        boolean[] combinedBits = new boolean[dataLengthBits.length + dataBits.length];
        System.arraycopy(dataLengthBits, 0, combinedBits, 0, dataLengthBits.length);
        System.arraycopy(dataBits, 0, combinedBits, dataLengthBits.length, dataBits.length);

        if (combinedBits.length > width * height * 3) {
            throw new IOException("Image is too small to embed data.");
        }

        int bitIndex = 0;
        for (int y = 0; y < height && bitIndex < combinedBits.length; y++) {
            for (int x = 0; x < width && bitIndex < combinedBits.length; x++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                if (bitIndex < combinedBits.length) red = (red & 0xFE) | (combinedBits[bitIndex++] ? 1 : 0);
                if (bitIndex < combinedBits.length) green = (green & 0xFE) | (combinedBits[bitIndex++] ? 1 : 0);
                if (bitIndex < combinedBits.length) blue = (blue & 0xFE) | (combinedBits[bitIndex++] ? 1 : 0);

                int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newPixel);
            }
        }
        return image;
    }

    private byte[] extract(BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        boolean[] dataLengthBits = new boolean[32];
        int bitIndex = 0;
        for (int y = 0; y < height && bitIndex < 32; y++) {
            for (int x = 0; x < width && bitIndex < 32; x++) {
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                if (bitIndex < 32) dataLengthBits[bitIndex++] = (red & 1) == 1;
                if (bitIndex < 32) dataLengthBits[bitIndex++] = (green & 1) == 1;
                if (bitIndex < 32) dataLengthBits[bitIndex++] = (blue & 1) == 1;
            }
        }

        byte[] dataLengthBytes = bitToBytes(dataLengthBits);
        int dataLength = ((dataLengthBytes[0] & 0xFF) << 24) |
                ((dataLengthBytes[1] & 0xFF) << 16) |
                ((dataLengthBytes[2] & 0xFF) << 8) |
                (dataLengthBytes[3] & 0xFF);

        if (dataLength <= 0 || dataLength * 8 > width * height * 3 - 32) {
            throw new IOException("No data or invalid data length found in the image.");
        }

        boolean[] extractedBits = new boolean[dataLength * 8];
        bitIndex = 0;
        int bitsToSkip = 32;
        int currentBitSkippedCount = 0;

        for (int y = 0; y < height && bitIndex < extractedBits.length; y++) {
            for (int x = 0; x < width && bitIndex < extractedBits.length; x++) {
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                if (currentBitSkippedCount < bitsToSkip) currentBitSkippedCount++; else if (bitIndex < extractedBits.length) extractedBits[bitIndex++] = (red & 1) == 1;
                if (currentBitSkippedCount < bitsToSkip) currentBitSkippedCount++; else if (bitIndex < extractedBits.length) extractedBits[bitIndex++] = (green & 1) == 1;
                if (currentBitSkippedCount < bitsToSkip) currentBitSkippedCount++; else if (bitIndex < extractedBits.length) extractedBits[bitIndex++] = (blue & 1) == 1;
            }
        }
        return bitToBytes(extractedBits);
    }

    private boolean[] bytesToBits(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bits[(i * 8) + j] = ((bytes[i] >> (7 - j)) & 1) == 1;
            }
        }
        return bits;
    }

    private byte[] bitToBytes(boolean[] bits) {
        byte[] bytes = new byte[bits.length / 8];
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                bytes[i / 8] |= (128 >> (i % 8));
            }
        }
        return bytes;
    }
}