package com.ys.mfc;

import com.WacomGSS.STU.ITabletHandler;
import com.WacomGSS.STU.Protocol.*;
import com.WacomGSS.STU.STUException;
import com.WacomGSS.STU.Tablet;
import com.WacomGSS.STU.UsbDevice;

import javax.swing.*;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class EstimationQuestionForm extends JDialog implements ITabletHandler {
    private String indicatorQueue;
    private String indicatorId;
    private String indicatorTitle;
    private String indicatorDescription;
    List<AnswerVariant> answerVariants;

    private Tablet tablet;
    private Capability capability;
    private Information information;

    private int headerHeight = 0;

    List<Button> buttons = new ArrayList<>();

    private int pad = 4;

    public EstimationQuestionForm(UsbDevice usbDevice,
                                  String indicatorQueue,
                                  String indicatorId,
                                  String indicatorTitle,
                                  String indicatorDescription,
                                  List<AnswerVariant> answerVariants) throws STUException, InterruptedException {
        this.indicatorQueue = indicatorQueue;
        this.indicatorId = indicatorId;
        this.indicatorTitle = indicatorTitle;
        this.indicatorDescription = indicatorDescription;
        this.answerVariants = answerVariants;

        this.tablet = new Tablet();
        for (int i = 0; i < 10; i++) {
            int e = tablet.usbConnect(usbDevice, true);
            if (e == 0) {
                this.capability = tablet.getCapability();
                this.information = tablet.getInformation();
                break;
            } else {
                if (i < 9) {
                    Thread.sleep(500);
                    continue;
                }
                throw new RuntimeException("Failed to connect to USB tablet, error " + e);
            }
        }

        this.headerHeight = this.capability.getScreenHeight() / 6;

        int offset = this.headerHeight;

        for (int i = 0; i < answerVariants.size(); i++) {
            Button button = new Button();
            button.buttonType = ButtonType.ANSWERVARIANT;
            AnswerVariant answerVariant = answerVariants.get(i);
            button.id = answerVariant.getId();
//            button.text = ("".equals(answerVariant.getAltTitle()) || null == answerVariant.getAltTitle())?
//                    answerVariant.getAltTitle() :
//                    answerVariant.getTitle();
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


        // Set the size of the client window to be actual size,
        // based on the reported DPI of the monitor.

//        int screenResolution = this.getToolkit().getScreenResolution();
//        Dimension d = new Dimension(this.capability.getTabletMaxX()
//                * screenResolution / 2540,
//                this.capability.getTabletMaxY() * screenResolution
//                        / 2540);

//        this.panel.setPreferredSize(d);
        this.setLayout(new BorderLayout());
        this.setResizable(false);
//        this.add(this.panel);
        this.pack();

        byte encodingFlag = ProtocolHelper.simulateEncodingFlag(
                this.tablet.getProductId(),
                this.capability.getEncodingFlag());

        boolean useColor = ProtocolHelper
                .encodingFlagSupportsColor(encodingFlag);

        // Disable color if the bulk driver isn't installed.
        // This isn't necessary, but uploading colour images with out
        // the driver
        // is very slow.
        useColor = useColor && this.tablet.supportsWrite();

        // Calculate the encodingMode that will be used to update the
        // image
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
        this.bitmap = new BufferedImage(
                this.capability.getScreenWidth(),
                this.capability.getScreenHeight(),
                BufferedImage.TYPE_INT_RGB);
        {
            Graphics2D gfx = bitmap.createGraphics();
            gfx.setColor(Color.WHITE);
            gfx.fillRect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            double fontSize = (this.buttons.get(0).bounds.getHeight() / 3); // pixels
//            double fontSize = (this.buttons.get(0).bounds.getWidth() / 45.00); // pixels
            gfx.setFont(new Font("Courier New", Font.BOLD, (int) fontSize));

            // Draw the buttons

            boolean useColour = useColor; //effective final for lambda
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
//                DrawingUtils.drawCenteredString(gfx, btn.text,
                DrawingUtils.drawArrangedString(gfx, btn.text,
                        (int) btn.bounds.getX(),
                        (int) btn.bounds.getY(),
                        (int) btn.bounds.getWidth(),
                        (int) btn.bounds.getHeight());
            });

            gfx.dispose();
        }

        // Now the bitmap has been created, it needs to be converted to
        // device-native
        // format.
        this.bitmapData = ProtocolHelper.flatten(this.bitmap,
                this.bitmap.getWidth(), this.bitmap.getHeight(),
                useColor);

        // If you wish to further optimize image transfer, you can
        // compress the image using
        // the Zlib algorithm.
        boolean useZlibCompression = false;

        if (!useColor && useZlibCompression) {
            // m_bitmapData = compress_using_zlib(m_bitmapData); //
            // insert compression here!
            // m_encodingMode = EncodingMode.EncodingMode_1bit_Zlib;
        }

        // Add the delagate that receives pen data.
        this.tablet.addTabletHandler(this);

        // Initialize the screen
        //clearScreen();

        // Enable the pen data on the screen (if not already)
        this.tablet.setInkingMode(InkingMode.Off);

        this.tablet.writeImage(this.encodingMode, this.bitmapData);
        Thread.sleep(100000);


    }

    private enum ButtonType {
        NAV, ANSWERVARIANT;
    }

    private static class Button {
        Rectangle bounds; // in Screen coordinates
        String text;
        ActionListener click;
        ButtonType buttonType = ButtonType.NAV;
        String id = "";

        void performClick() {
            click.actionPerformed(null);
        }
    }

    private static class RectangleDimensions {
        int height = 0;
        int widht = 0;
    }

    RectangleDimensions getAnswerButtonDimension() {
        int buttonCount = answerVariants.size();
        int answersAreaHeight = capability.getScreenHeight() - this.headerHeight;

        RectangleDimensions dim = new RectangleDimensions();
        dim.height = (answersAreaHeight - pad) / buttonCount - pad;
        dim.widht = capability.getScreenWidth() - pad * 2;
        return dim;
    }

    // The isDown flag is used like this:
    // 0 = up
    // +ve = down, pressed on button number
    // -1 = down, inking
    // -2 = down, ignoring
    private int isDown;

    private List<PenData> penData; // Array of data being stored. This can
    // be subsequently used as desired.

//    private Button[] btns; // The array of buttons that we are emulating.

    private JPanel panel;

    private BufferedImage bitmap; // This bitmap that we display on the
    // screen.
    private EncodingMode encodingMode; // How we send the bitmap to the
    // device.
    private byte[] bitmapData; // This is the flattened data of the bitmap
    // that we send to the device.

    private Point2D.Float tabletToClient(PenData penData) {
        // Client means the panel coordinates.
        return new Point2D.Float(
                (float) penData.getX() * this.panel.getWidth() / this.capability.getTabletMaxX(),
                (float) penData.getY() * this.panel.getHeight() / this.capability.getTabletMaxY());
    }

    private Point2D.Float tabletToScreen(PenData penData) {
        // Screen means LCD screen of the tablet.
        return new Point2D.Float(
                (float) penData.getX() * this.capability.getScreenWidth() / this.capability.getTabletMaxX(),
                (float) penData.getY() * this.capability.getScreenHeight() / this.capability.getTabletMaxY());
    }

    private Point clientToScreen(Point pt) {
        // client (window) coordinates to LCD screen coordinates.
        // This is needed for converting mouse coordinates into LCD bitmap
        // coordinates as that's
        // what this application uses as the coordinate space for buttons.
        return new Point(
                Math.round((float) pt.getX() * this.capability.getScreenWidth() / this.panel.getWidth()),
                Math.round((float) pt.getY() * this.capability.getScreenHeight() / this.panel.getHeight()));
    }

    public void dispose() {
        // Ensure that you correctly disconnect from the tablet, otherwise
        // you are
        // likely to get errors when wanting to connect a second time.
        if (this.tablet != null) {
            try {
                this.tablet.setInkingMode(InkingMode.Off);
                this.tablet.setClearScreen();
            } catch (Throwable t) {
            }
            this.tablet.disconnect();
            this.tablet = null;
        }

        super.dispose();
    }

    public JPanel getPanel() {
        return panel;
    }

    public Capability getCapability() {
        return capability;
    }

    @Override
    public void onGetReportException(STUException e) {

    }

    @Override
    public void onUnhandledReportData(byte[] bytes) {

    }

    @Override
    public void onPenData(PenData penData) {

    }

    @Override
    public void onPenDataOption(PenDataOption penDataOption) {

    }

    @Override
    public void onPenDataEncrypted(PenDataEncrypted penDataEncrypted) {

    }

    @Override
    public void onPenDataEncryptedOption(PenDataEncryptedOption penDataEncryptedOption) {

    }

    @Override
    public void onPenDataTimeCountSequence(PenDataTimeCountSequence penDataTimeCountSequence) {

    }

    @Override
    public void onPenDataTimeCountSequenceEncrypted(PenDataTimeCountSequenceEncrypted penDataTimeCountSequenceEncrypted) {

    }

    @Override
    public void onEventDataPinPad(EventDataPinPad eventDataPinPad) {

    }

    @Override
    public void onEventDataKeyPad(EventDataKeyPad eventDataKeyPad) {

    }

    @Override
    public void onEventDataSignature(EventDataSignature eventDataSignature) {

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
