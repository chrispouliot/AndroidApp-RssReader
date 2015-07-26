package com.android.acios.blocly.api;

import java.util.Random;

/**
 * Created by christophepouliot on 15-07-19.
 */
public class UIUtils {

    public static int generateRandomColor(int baseColor) {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        int baseRed = (baseColor & 0xFF0000) >> 16;
        int baseGreen = (baseColor & 0xFF00) >> 8;
        int baseBlue = baseColor & 0xff;

        red = (red + baseRed) / 2;
        green = (green + baseGreen) / 2;
        blue = (blue + baseBlue) / 2;
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
