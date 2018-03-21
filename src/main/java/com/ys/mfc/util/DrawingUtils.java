package com.ys.mfc.util;

import com.WacomGSS.STU.Protocol.PenData;
import com.ys.mfc.EstimationQuestionForm;

import java.awt.*;
import java.awt.geom.Point2D;

public class DrawingUtils {

    static public void drawCenteredString(Graphics2D gfx, String text, int x,
                                          int y, int width, int height) {
        FontMetrics fm = gfx.getFontMetrics(gfx.getFont());
        int textHeight = fm.getHeight();
        int textWidth = fm.stringWidth(text);

        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - textHeight) / 2 + fm.getAscent();

        gfx.drawString(text, textX, textY);
    }

    static public void drawLongStringBySpliting(Graphics2D gfx, String text, int x,
                                                int y, int width, int height) {
        FontMetrics fm = gfx.getFontMetrics(gfx.getFont());
        int textHeight = fm.getHeight();
        int textWidth = fm.stringWidth(text);
        String head = "";
        String tail = "";
        int headWidth = textWidth;
        a:
        if (textWidth > width) {
            int endOfHeadPos = text.length();

            while (headWidth >= width) {
                endOfHeadPos = text.lastIndexOf(" ", endOfHeadPos - 1);
                if (endOfHeadPos == -1) break a;
                head = text.substring(0, endOfHeadPos);
                headWidth = fm.stringWidth(head);
            }
            tail = text.substring(endOfHeadPos + 1);
        }
        int textX;
        int textY;
        if ("".equals(tail)) {
            textX = x + (width - textWidth) / 2;
            textY = y + (height - textHeight) / 2 + fm.getAscent();
            gfx.drawString(text, textX, textY);
        } else {
            textX = 8;
            textY = y + (height / 2 - textHeight) / 2 + fm.getAscent();
            gfx.drawString(head, textX, textY);
            textY = textY + height / 2;
            gfx.drawString(tail, textX, textY);
        }
    }

    private static String[] getHeadTail(String text, int width, FontMetrics fm) {
        String[] retarr = {"", ""};
        int textWidth = fm.stringWidth(text);
        if (textWidth <= width) {
            retarr[0] = text;
            return retarr;
        }
        String head = "text";
        String tail = "";
        int headWidth = textWidth;
        b:
        if (textWidth > width) {
            int endOfHeadPos = text.length();

            while (headWidth >= width) {
                endOfHeadPos = text.lastIndexOf(" ", endOfHeadPos - 1);
                if (endOfHeadPos == -1) break b;
                head = text.substring(0, endOfHeadPos);
                headWidth = fm.stringWidth(head);
            }
            tail = text.substring(endOfHeadPos + 1);
        }
        retarr[0] = head;
        retarr[1] = tail;
        return retarr;
    }

    public static Point2D.Float tabletToScreen(PenData penData, EstimationQuestionForm estimationQuestionForm) {
        // Screen means LCD screen of the tablet.
        return new Point2D.Float(
                (float) penData.getX() * estimationQuestionForm.getCapability().getScreenWidth() / estimationQuestionForm.getCapability().getTabletMaxX(),
                (float) penData.getY() * estimationQuestionForm.getCapability().getScreenHeight() / estimationQuestionForm.getCapability().getTabletMaxY());
    }
}
