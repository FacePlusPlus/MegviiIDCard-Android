package com.megvii.faceppidcardui.util;


public class RotaterUtil {

	public static byte[] rotate(byte[] data, int imageWidth, int imageHeight,
			int rotate) {
		switch (rotate) {
		case 0:
			return data;
		case 90:
			return rotateYUV420Degree90(data, imageWidth, imageHeight);
		case 180:
			return rotateYUV420Degree180(data, imageWidth, imageHeight);
		case 270:
			return rotateYUV420Degree270(data, imageWidth, imageHeight);
		}
		return data;
	}

	public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth,
			int imageHeight) {
		byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
		// Rotate the Y luma
		int i = 0;
		for (int x = 0; x < imageWidth; x++) {
			for (int y = imageHeight - 1; y >= 0; y--) {
				yuv[i] = data[y * imageWidth + x];
				i++;
			}

		}
		// int offset = imageWidth * imageHeight;
		// for (int x = 0; x < imageWidth / 2; x ++)
		// for (int y = imageHeight / 2 - 1; y >= 0; y --) {
		// yuv[i] = data[offset + (y * imageWidth) + x];
		// i ++ ;
		// yuv[i] = data[offset + (y * imageWidth) + x + 1];
		// i ++;
		// }

		// Rotate the U and V color components
		i = imageWidth * imageHeight * 3 / 2 - 1;
		for (int x = imageWidth - 1; x > 0; x = x - 2) {
			for (int y = 0; y < imageHeight / 2; y++) {
				yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
				i--;
				yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth)
						+ (x - 1)];
				i--;
			}
		}
		return yuv;
	}

	public static byte[] rotateYUV420Degree180(byte[] data, int imageWidth,
			int imageHeight) {
		byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
		int i = 0;
		int count = 0;

		for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
			yuv[count] = data[i];
			count++;
		}

		i = imageWidth * imageHeight * 3 / 2 - 1;
		for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
				* imageHeight; i -= 2) {
			yuv[count++] = data[i - 1];
			yuv[count++] = data[i];
		}
		return yuv;
	}

	public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth,
			int imageHeight) {
		byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
		// Rotate the Y luma

		int index = 0;
		int i = 0;
		for (int x = imageWidth - 1; x >= 0; x--) {
			index = 0;
			for (int y = 0; y < imageHeight; y++) {
				yuv[i] = data[index + x];
				i++;

				index += imageWidth;
			}

		}
		// Rotate the U and V color components
		i = imageWidth * imageHeight;
		int count = imageWidth * imageHeight;
		for (int x = imageWidth - 1; x > 0; x = x - 2) {
			index = count;
			for (int y = 0; y < imageHeight / 2; y++) {

				yuv[i] = data[index + (x - 1)];
				i++;
				yuv[i] = data[index + x];
				i++;

				index += imageWidth;
			}
		}
		return yuv;
	}
}
