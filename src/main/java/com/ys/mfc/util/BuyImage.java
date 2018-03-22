package com.ys.mfc.util;

import com.WacomGSS.STU.Protocol.Capability;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BuyImage {

    BufferedImage bitmap;
    private Capability capability;

    public BuyImage(Capability capability) {
        this.capability = capability;
        createImage();
    }

    public void createImage() {
        this.bitmap = new BufferedImage(
                this.capability.getScreenWidth(),
                this.capability.getScreenHeight(),
                BufferedImage.TYPE_INT_RGB);
        {
            Graphics2D gfx = bitmap.createGraphics();
            gfx.setColor(Color.WHITE);
            gfx.fillRect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            double fontSize = 40; // pixels

            // Draw question
            gfx.setColor(Color.BLACK);
            gfx.setFont(new Font("Courier New", Font.BOLD, (int) fontSize));
            DrawingUtils.drawLongStringBySpliting(gfx, "Спасибо за участие в оценке качества оказания услуг!",
                    (int) 0, 0,
                    (int) this.capability.getScreenWidth(),
                    (int) this.capability.getScreenHeight(),
                    true);

            gfx.dispose();
        }
    }

    public BufferedImage getBitmap() {
        return bitmap;
    }
}
