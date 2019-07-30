package com.ys.mfc;

import com.WacomGSS.STU.ITabletHandler;
import com.WacomGSS.STU.Protocol.*;
import com.WacomGSS.STU.STUException;
import com.WacomGSS.STU.Tablet;
import com.WacomGSS.STU.UsbDevice;
import com.ys.mfc.util.DrawingUtils;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class EstimationQuestionForm implements ITabletHandler {
    private List<AnswerVariant> answerVariants;
    private List<Button> buttons = new ArrayList<>();

    public Tablet getTablet() {
        return tablet;
    }

    private Tablet tablet;
    private String modelName;
    private Capability capability;

    String getPressedButtonId() {
        return pressedButtonId;
    }

    private String pressedButtonId = null;
    private int headerHeight;
    private int pad = 4;

    private List<PenData> penData; // Array of data being stored. This can
    private EncodingMode encodingMode; // How we send the bitmap to the device.
    private byte[] bitmapData; // This is the flattened data of the bitmap

    EstimationQuestionForm(UsbDevice usbDevice,
                           String indicatorQueue,
                           String indicatorId,
                           String indicatorTitle,
                           String indicatorDescription,
                           List<AnswerVariant> answerVariants) throws STUException, InterruptedException {
        this.answerVariants = answerVariants;
        this.tablet = new Tablet();
        int e = -1;

        for (int i = 0; i < 100; i++) {
            e = tablet.usbConnect(usbDevice, true);
            if (e == 0) {
                break;
            } else {
                Thread.sleep(2000);
            }
        }
        if (e != 0) {
            throw new RuntimeException("Failed to connect to USB tablet, error " + e);
        }

        this.capability = tablet.getCapability();
        Information information = tablet.getInformation();
        modelName = information.getModelName();

        this.headerHeight = this.capability.getScreenHeight() / 4;
        int offset = this.headerHeight;

        for (int i = 0; i < answerVariants.size(); i++) {
            Button button = new Button();
            button.buttonType = ButtonType.ANSWERVARIANT;
            AnswerVariant answerVariant = answerVariants.get(i);
            button.id = answerVariant.getId();
            button.text = answerVariant.getAltTitle();
            if ("".equals(button.text)) button.text = answerVariant.getTitle();
            button.bounds = new Rectangle();
            RectangleDimensions buttonDimension = getAnswerButtonDimension();
            button.bounds.x = pad;
            button.bounds.y = offset + i * (buttonDimension.height + pad);
            button.bounds.width = buttonDimension.widht;
            button.bounds.height = buttonDimension.height;
            this.buttons.add(button);
        }

        byte encodingFlag = ProtocolHelper.simulateEncodingFlag(
                this.tablet.getProductId(),
                this.capability.getEncodingFlag());

        boolean useColor = ProtocolHelper
                .encodingFlagSupportsColor(encodingFlag);

        useColor = useColor && this.tablet.supportsWrite();

        // Calculate the encodingMode that will be used to update the image
        if (useColor) {
            if (this.tablet.supportsWrite())
                this.encodingMode = EncodingMode.EncodingMode_16bit_Bulk;
            else
                this.encodingMode = EncodingMode.EncodingMode_16bit;
        } else {
            this.encodingMode = EncodingMode.EncodingMode_1bit;
        }

        // Size the bitmap to the size of the LCD screen.
        // This application uses the same bitmap for both the screen and
        // client (window).
        // However, at high DPI, this bitmap will be stretch and it
        // would be better to
        // create individual bitmaps for screen and client at native
        // resolutions.
        // This bitmap that we display on the screen.
        BufferedImage bitmap = new BufferedImage(
                this.capability.getScreenWidth(),
                this.capability.getScreenHeight(),
                BufferedImage.TYPE_INT_RGB);
        {
            Graphics2D gfx = bitmap.createGraphics();
            gfx.setColor(Color.WHITE);
            gfx.fillRect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            double fontSize = (this.buttons.get(0).bounds.getHeight() / 3); // pixels

            // Draw question
            gfx.setColor(Color.BLACK);
//            gfx.setFont(new Font("Courier New", Font.BOLD, (int) fontSize));
            gfx.setFont(new Font("Times New Roman", Font.BOLD, (int) fontSize));
            DrawingUtils.drawLongStringBySpliting(gfx, indicatorDescription,
                    (int) 0, 0,
                    (int) this.capability.getScreenWidth(),
                    (int) this.headerHeight,
                    false);
            // Draw the buttons
            boolean useColour = useColor; //effective final for lambda
//            gfx.setFont(new Font("Courier New", Font.BOLD, (int) fontSize));
            gfx.setFont(new Font("Times New Roman", Font.BOLD, (int) fontSize));
            buttons.forEach(btn -> {
                if (useColour) {
                    gfx.setColor(Color.PINK);
                    gfx.fillRect((int) btn.bounds.getX(),
                            (int) btn.bounds.getY(),
                            (int) btn.bounds.getWidth(),
                            (int) btn.bounds.getHeight());
                }
                gfx.setColor(Color.BLACK);
                gfx.drawRect((int) btn.bounds.getX(),
                        (int) btn.bounds.getY(),
                        (int) btn.bounds.getWidth(),
                        (int) btn.bounds.getHeight());
                DrawingUtils.drawLongStringBySpliting(gfx, btn.text,
                        (int) btn.bounds.getX(),
                        (int) btn.bounds.getY(),
                        (int) btn.bounds.getWidth(),
                        (int) btn.bounds.getHeight(),
                        true);
            });
            gfx.dispose();
        }

        this.bitmapData = ProtocolHelper.flatten(bitmap,
                bitmap.getWidth(), bitmap.getHeight(),
                useColor);

        // Add the delagate that receives pen data.
        this.tablet.addTabletHandler(this);

        // Enable the pen data on the screen (if not already)
        this.tablet.setInkingMode(InkingMode.Off);
//        if (!this.tablet.isConnected()) {
//            for (int i = 0; i < 100; i++) {
//                e = tablet.usbConnect(usbDevice, true);
//                if (e == 0) {
//                    break;
//                } else {
//                    Thread.sleep(2000);
//                }
//            }
//            if (e != 0) {
//                throw new RuntimeException("Failed to connect to USB tablet, error " + e);
//            }
//        }
        int connectionError = 0;
        try {
            this.tablet.writeImage(this.encodingMode, this.bitmapData);
        }
        catch (Exception ex) {
            for (int i = 0; i < 20; i++) {
                if (!this.tablet.isConnected())
                    connectionError = tablet.usbConnect(usbDevice, true);
                if (connectionError == 0) {
                    break;
                } else {
                    Thread.sleep(2000);
                }
            }
            if (connectionError != 0) {
                throw new RuntimeException("Failed to connect to USB tablet, error " + e);
            }
        }
        this.tablet.endImageData();
    }

    public void waitForButtonPress() throws InterruptedException {
        while (this.getPressedButtonId() == null) {
            Thread.sleep(100);
            Thread.yield();
        }
    }

    void setAnswerButtonListener(AnswerButtonPressedListener answerButtonListener) {
    }

    private RectangleDimensions getAnswerButtonDimension() {
        int buttonCount = answerVariants.size();
        int answersAreaHeight = capability.getScreenHeight() - this.headerHeight;

        RectangleDimensions dim = new RectangleDimensions();
        dim.height = (answersAreaHeight - pad) / buttonCount - pad;
        dim.widht = capability.getScreenWidth() - pad * 2;
        return dim;
    }

    void dispose() {
        // Ensure that you correctly disconnect from the tablet, otherwise you are
        // likely to get errors when wanting to connect a second time.
        if (this.tablet != null) {
            try {
                this.tablet.setInkingMode(InkingMode.Off);
                this.tablet.setClearScreen();
            } catch (Throwable ignored) {
            }
            this.tablet.disconnect();
            this.tablet = null;
        }
    }

    public Capability getCapability() {
        return capability;
    }

    @Override
    public void onPenData(PenData penData) {
//        if (!readyToProcess) return;
        if (!"STU-530".equals(modelName)) return;
        pressedButton(penData);
    }

    private enum ButtonType {
        NAV, ANSWERVARIANT;
    }

    private static class RectangleDimensions {
        int height = 0;
        int widht = 0;
    }

    private class Button {
        Rectangle bounds; // in Screen coordinates
        String text;
        ButtonType buttonType = ButtonType.NAV;
        String id = "";
//        ActionListener click = (e -> {
//            EstimationQuestionForm.this.pressedButtonId = id;
//            EstimationQuestionForm.this.answerButtonListener
//                    .ansewrButtonPressed(new AnswerButtonPressedEvent(EstimationQuestionForm.this, "Button pressed id = " + id));
//        });
//
//        void performClick() {
////            actionPerformed(new AnswerButtonPressedEvent(this, "Нахата кнопка id = " + id));
//        }
    }

    public EncodingMode getEncodingMode() {
        return encodingMode;
    }

    public byte[] getBitmapData() {
        return bitmapData;
    }

    @Override
    public void onGetReportException(STUException e) {

    }

    @Override
    public void onUnhandledReportData(byte[] bytes) {
//        System.out.println("onUnhandledReportData");
    }


    @Override
    public void onPenDataOption(PenDataOption penDataOption) {
//        System.out.println("onPenDataOption");
    }

    @Override
    public void onPenDataEncrypted(PenDataEncrypted penDataEncrypted) {
//        System.out.println("onPenDataEncrypted");
    }

    @Override
    public void onPenDataEncryptedOption(PenDataEncryptedOption penDataEncryptedOption) {
//        System.out.println("onPenDataEncryptedOption");
    }

    @Override
    public void onPenDataTimeCountSequence(PenDataTimeCountSequence penDataTimeCountSequence) {
//        if (!readyToProcess) return;
        try {
            tablet.endCapture();
        } catch (STUException e) {
            e.printStackTrace();
        }
        if (!"STU-540".equals(modelName)) return;
        if (penDataTimeCountSequence.getPressure() < 100) return;
            pressedButton(penDataTimeCountSequence);
    }

    private void pressedButton(PenData penData) {
//        boolean readyToProcess = false;
        Point2D.Float point = DrawingUtils.tabletToScreen(penData, this);
        for (int i = buttons.size() - 1; i >= 0; i--) {
            Button button = buttons.get(i);
            if (penData.getPressure() > 0) {
                if (button.bounds.contains(Math.round(point.getX()), Math.round(point.getY()))) {
                    try {
                        this.tablet.setClearScreen();
                    } catch (STUException e) {
                    }
                    this.pressedButtonId = button.id;
                    tablet.disconnect();
                    break;
                }
            }
        }
    }

    @Override
    public void onPenDataTimeCountSequenceEncrypted(PenDataTimeCountSequenceEncrypted penDataTimeCountSequenceEncrypted) {
//        System.out.println("onPenDataTimeCountSequenceEncrypted");
    }

    @Override
    public void onEventDataPinPad(EventDataPinPad eventDataPinPad) {

    }

    @Override
    public void onEventDataKeyPad(EventDataKeyPad eventDataKeyPad) {

    }

    @Override
    public void onEventDataSignature(EventDataSignature eventDataSignature) {
        System.out.println("onEventDataSignature");
    }

    @Override
    public void onEventDataPinPadEncrypted(EventDataPinPadEncrypted eventDataPinPadEncrypted) {

    }

    @Override
    public void onEventDataKeyPadEncrypted(EventDataKeyPadEncrypted eventDataKeyPadEncrypted) {

    }

    @Override
    public void onEventDataSignatureEncrypted(EventDataSignatureEncrypted eventDataSignatureEncrypted) {

    }

    @Override
    public void onDevicePublicKey(DevicePublicKey devicePublicKey) {

    }

    @Override
    public void onEncryptionStatus(EncryptionStatus encryptionStatus) {

    }

}
