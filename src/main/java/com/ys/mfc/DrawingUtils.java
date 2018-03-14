package com.ys.mfc;

import com.WacomGSS.STU.Protocol.PenData;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

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

    static public void drawArrangedString(Graphics2D gfx, String text, int x,
                                          int y, int width, int height) {
        FontMetrics fm = gfx.getFontMetrics(gfx.getFont());
        int textHeight = fm.getHeight();
        int textWidth = fm.stringWidth(text);
        String head = "";
        String tail = "";

        a:
        if (textWidth > width) {
            int endOfHeadPos = text.length();
            int headWidth = textWidth;

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
            textX = 2;
            textY = y + (height / 2 - textHeight) / 2 + fm.getAscent();
            gfx.drawString(head, textX, textY);
            textY = textY + height / 2;
            gfx.drawString(tail, textX, textY);
        }
    }


    static public void drawInk(Graphics2D gfx, PenData pd0, PenData pd1, EstimationQuestionForm estimationQuestionForm) {
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        gfx.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        gfx.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        int lineWidth = 5;
        int pressure = pd0.getPressure();
//            gfx.setColor(new Color(0, 0, 64, 255));
        int trans = 255 * pressure / 1024 + 100;
        trans = trans > 255 ? 255 : trans;
        gfx.setColor(new Color(0, 0, 64, trans));

        gfx.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        Point2D.Float pt0 = tabletToClient(pd0, estimationQuestionForm);
        Point2D.Float pt1 = tabletToClient(pd1, estimationQuestionForm);
        Shape l = new Line2D.Float(pt0, pt1);
        gfx.draw(l);
    }

    static public void drawInk(Graphics2D gfx, List<PenData> penData, EstimationQuestionForm estimationQuestionForm) {
        PenData[] pd = new PenData[0];
        pd = penData.toArray(pd);
        for (int i = 1; i < pd.length; ++i) {
            if (pd[i - 1].getSw() != 0 && pd[i].getSw() != 0) {
                drawInk(gfx, pd[i - 1], pd[i], estimationQuestionForm);
            }
        }
    }

    private static Point2D.Float tabletToClient(PenData penData, EstimationQuestionForm estimationQuestionForm) {
        // Client means the panel coordinates.
        return new Point2D.Float(
                (float) penData.getX() * estimationQuestionForm.getPanel().getWidth() / estimationQuestionForm.getCapability().getTabletMaxX(),
                (float) penData.getY() * estimationQuestionForm.getPanel().getHeight() / estimationQuestionForm.getCapability().getTabletMaxY());
    }

    private static Point2D.Float tabletToScreen(PenData penData, EstimationQuestionForm estimationQuestionForm) {
        // Screen means LCD screen of the tablet.
        return new Point2D.Float(
                (float) penData.getX() * estimationQuestionForm.getCapability().getScreenWidth() / estimationQuestionForm.getCapability().getTabletMaxX(),
                (float) penData.getY() * estimationQuestionForm.getCapability().getScreenHeight() / estimationQuestionForm.getCapability().getTabletMaxY());
    }

    private static Point clientToScreen(Point pt, EstimationQuestionForm estimationQuestionForm) {
        // client (window) coordinates to LCD screen coordinates.
        // This is needed for converting mouse coordinates into LCD bitmap
        // coordinates as that's
        // what this application uses as the coordinate space for buttons.
        return new Point(
                Math.round((float) pt.getX() * estimationQuestionForm.getCapability().getScreenWidth() / estimationQuestionForm.getPanel().getWidth()),
                Math.round((float) pt.getY() * estimationQuestionForm.getCapability().getScreenHeight() / estimationQuestionForm.getPanel().getHeight()));
    }

}
