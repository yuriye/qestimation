package com.ys.mfc.util;

import com.WacomGSS.STU.Protocol.PenData;
import com.ys.mfc.EstimationQuestionForm;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

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
                                                int y, int width, int height, Boolean verticalCentered) {
        FontMetrics fm = gfx.getFontMetrics(gfx.getFont());
        int textHeight = fm.getHeight();
        int textWidth = fm.stringWidth(text);

        java.util.List<String> textParts = new ArrayList<>();

        int textX = 8;
        int textY;
        int lineSpacing = 4;

        if (textWidth > width) {
            String[] headTail;
            String head = text;
            do {
                headTail = getHeadTail(head, width, fm);
                if (!"".equals(headTail[0])) {
                    textParts.add(headTail[0]);
                    head = headTail[1];
                }
            }
            while (!"".equals(headTail[1]));
            if (textParts.size() > 0) {
                if (verticalCentered) {
                    textY = y + (height - (textHeight + lineSpacing) * textParts.size() - lineSpacing) / 2 + fm.getAscent();
                }
                else {
                    textY = 13;
                }

                for (int i = 0; i < textParts.size(); i++) {
                    gfx.drawString(textParts.get(i), textX, textY + i * textHeight + lineSpacing);
                }
            }
        } else {
//        textX = x + (width - textWidth) / 2;
            textY = y + (height - textHeight) / 2 + fm.getAscent();
            gfx.drawString(text, textX, textY);
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
    //

}
