package com.ys.mfc;

import com.WacomGSS.STU.*;
import com.WacomGSS.STU.Protocol.*;
import com.WacomGSS.STU.Tablet;
import com.ys.mfc.mkgu.MkguQuestionXmlIndicator;
import com.ys.mfc.mkgu.MkguQuestionXmlQuestions;
import com.ys.mfc.mkgu.MkguQuestionXmlRoot;
import com.ys.mfc.mkgu.MkguQuestionnaires;
import com.ys.mfc.util.DrawingUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AskForOredrCodeDialog extends JDialog implements ITabletHandler {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JButton buttonClose;
    private JButton buttonNext;
    private JTabbedPane tabbedPane;
    private JPanel askForOrderCode;
    private JPanel log;
    private JTextField orderCodeTextFieldl;
    private JButton startButton;
    private JTextArea logText;

    private String orderCode = "";
    private HttpAdapter adapter = HttpAdapter.getInstance();
    private Map mkguFormVersion;
    private MkguQuestionXmlRoot questions;
    private com.WacomGSS.STU.UsbDevice[] usbDevices = UsbDevice.getUsbDevices();

    List<AnswerVariant> answerVariants;
    List<com.ys.mfc.AskForOredrCodeDialog.Button> buttons = new ArrayList<>();
    private String indicatorQueue;
    private String indicatorId;
    private String indicatorTitle;
    private String indicatorDescription;

    public Tablet getTablet() {
        return tablet;
    }

    private Tablet tablet;
    private Capability capability;
    private Information information;

    public String getPressedButtonId() {
        return pressedButtonId;
    }

    private String pressedButtonId = null;
    private int headerHeight;
    private AnswerButtonPressedListener answerButtonListener;
    private int pad = 4;

    private List<PenData> penData; // Array of data being stored. This can
    private BufferedImage bitmap; // This bitmap that we display on the screen.
    private EncodingMode encodingMode; // How we send the bitmap to the device.
    private byte[] bitmapData; // This is the flattened data of the bitmap


//    private com.WacomGSS.STU.UsbDevice[] usbDevices = UsbDevice.getUsbDevices();


    public AskForOredrCodeDialog() throws STUException {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonClose);

        buttonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        });

        buttonNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    onStart();
                } catch (STUException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void onStart() throws STUException, InterruptedException {

        orderCode = orderCodeTextFieldl.getText();
        mkguFormVersion = adapter.getMkguFormVersion(orderCode);
        if ("OK".equals(mkguFormVersion.get("status"))) {
            if (usbDevices == null || usbDevices.length <= 0) {
                throw new RuntimeException("No USB tablets attached");
            }
            orderCode = orderCodeTextFieldl.getText();
            MkguQuestionXmlRoot questions = getQuestions(mkguFormVersion, orderCode);
            for (MkguQuestionXmlIndicator estimationQuestion : questions.getIndicator()) {
                List<AnswerVariant> answerVariants = new ArrayList<>();
                List<MkguQuestionXmlQuestions> stepAnswerVariants = estimationQuestion.getIndicator();

                stepAnswerVariants.forEach(variant ->
                        answerVariants.add(new AnswerVariant(
                                variant.getQuestionValue(),
                                variant.getQuestionText(),
                                variant.getAltTitle()))
                );

                initForm(
                        usbDevices[0],
                        estimationQuestion.getIndicatorId(),
                        estimationQuestion.getIndicatorId(),
                        estimationQuestion.getQuestionTitle(),
                        estimationQuestion.getDescriptionTitle(),
                        answerVariants);

                while (getPressedButtonId() == null) {
                    Thread.sleep(100);
                    Thread.yield();
                }
                System.out.println(getPressedButtonId());
                if (getTablet().isConnected()) getTablet().disconnect();
            }
        } else {
            throw new RuntimeException("No USB tablets attached");
        }
    }


    private static MkguQuestionXmlRoot getQuestions(Map<String, String> mkguFormVersion, String orderNumber) {
        List<MkguQuestionnaires> mkguQuestionnaires = HttpAdapter.getInstance().getMkguQuestionnaires();
        MkguQuestionXmlRoot mkguQuestionXmlRoot = new MkguQuestionXmlRoot();
        mkguQuestionXmlRoot.setOrderNumber(orderNumber);
        String xml = mkguQuestionnaires.stream()
                .filter(element -> element.getVersion().equals(mkguFormVersion.get("version")))
                .collect(Collectors.toList())
                .get(0)
                .getXml();

        xml = xml.replaceAll("\\n", "");
        xml = xml.replaceAll("\\\\", "");
        System.out.println(xml);

        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            Document doc = saxBuilder.build(new StringReader(xml));
            List<MkguQuestionXmlIndicator> mkguQuestionXmlIndicators = new ArrayList<>();
            Element rootElement = doc.getRootElement();
            Element blocks = (Element) rootElement.getChildren("blocks").get(0);
            mkguQuestionXmlRoot.setQuestionTitle(((Element) blocks.getChildren().get(0)).getValue());
            Element indicators = (Element) rootElement.getChildren("indicators").get(0);
            Iterator iterator = indicators.getChildren().iterator();

            indicators.getChildren().forEach((Element element) -> {
                MkguQuestionXmlIndicator mkguQuestionXmlIndicator = new MkguQuestionXmlIndicator();
                mkguQuestionXmlIndicator.setIndicatorId(element.getAttribute("id").getValue());
                mkguQuestionXmlIndicator.setQuestionTitle(element.getChild("title").getValue());
                mkguQuestionXmlIndicator.setDescriptionTitle(element.getChild("description").getValue());
                List<Element> values = element.getChild("values").getChildren();
                List<MkguQuestionXmlQuestions> mkguQuestionXmlList = new ArrayList<>();

                values.forEach(element1 -> mkguQuestionXmlList.add(
                        new MkguQuestionXmlQuestions(
                                element1.getAttribute("id").getValue(),
                                element1.getChild("title").getValue(),
                                element1.getChild("alt-title").getValue())));

                mkguQuestionXmlIndicator.setIndicator(mkguQuestionXmlList);
                mkguQuestionXmlIndicators.add(mkguQuestionXmlIndicator);
            });

            mkguQuestionXmlRoot.setIndicator(mkguQuestionXmlIndicators);
            return mkguQuestionXmlRoot;

        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MkguQuestionXmlRoot();
    }

    private void onClose() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) throws STUException {
        AskForOredrCodeDialog dialog = new AskForOredrCodeDialog();
        dialog.pack();
        dialog.setVisible(true);
//        System.exit(0);
    }


    public void initForm(UsbDevice usbDevice,
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
                tablet.setClearScreen();
                break;
            } else {
                if (i < 9) {
                    Thread.sleep(500);
                    Thread.yield();
                    continue;
                }
                throw new RuntimeException("Failed to connect to USB tablet, error " + e);
            }
        }

        this.headerHeight = this.capability.getScreenHeight() / 4;
        int offset = this.headerHeight;

        for (int i = 0; i < answerVariants.size(); i++) {
            com.ys.mfc.AskForOredrCodeDialog.Button button = new com.ys.mfc.AskForOredrCodeDialog.Button();
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
        this.bitmap = new BufferedImage(
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
            gfx.setFont(new Font("Courier New", Font.BOLD, (int) fontSize));
            DrawingUtils.drawLongStringBySpliting(gfx, indicatorDescription,
                    (int) 0, 0,
                    (int) this.capability.getScreenWidth(),
                    (int) this.headerHeight,
                    false);
            // Draw the buttons
            boolean useColour = useColor; //effective final for lambda
            gfx.setFont(new Font("Courier New", Font.BOLD, (int) fontSize));
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

        this.bitmapData = ProtocolHelper.flatten(this.bitmap,
                this.bitmap.getWidth(), this.bitmap.getHeight(),
                useColor);

        // Add the delagate that receives pen data.
        this.tablet.addTabletHandler(this);

        // Enable the pen data on the screen (if not already)
        this.tablet.setInkingMode(InkingMode.Off);
        this.tablet.writeImage(this.encodingMode, this.bitmapData);
    }

    public void waitForButtonPress() throws InterruptedException {
        while (this.getPressedButtonId() == null) {
            Thread.sleep(100);
            Thread.yield();
        }
    }

    public void setAnswerButtonListener(AnswerButtonPressedListener answerButtonListener) {
        this.answerButtonListener = answerButtonListener;
    }

    RectangleDimensions getAnswerButtonDimension() {
        int buttonCount = answerVariants.size();
        int answersAreaHeight = capability.getScreenHeight() - this.headerHeight;

        RectangleDimensions dim = new RectangleDimensions();
        dim.height = (answersAreaHeight - pad) / buttonCount - pad;
        dim.widht = capability.getScreenWidth() - pad * 2;
        return dim;
    }

    public void dispose() {
        // Ensure that you correctly disconnect from the tablet, otherwise you are
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
    }

    public Capability getCapability() {
        return capability;
    }

    @Override
    public void onPenData(PenData penData) {
        Point2D.Float point = tabletToScreen(penData);
        for (int i = buttons.size() - 1; i >= 0; i--) {
            Button button = buttons.get(i);
            if (penData.getPressure() > 0) {
                if (button.bounds.contains(Math.round(point.getX()), Math.round(point.getY()))) {
                    try {
                        this.tablet.setClearScreen();
                    } catch (STUException e) {
                        e.printStackTrace();
                    }
                    this.pressedButtonId = button.id;
                    tablet.disconnect();

//                    this.answerButtonListener.ansewrButtonPressed(
//                            new AnswerButtonPressedEvent(this, "Нахата кнопка id = "));
                    break;
                }
            }
        }
    }

    public Point2D.Float tabletToScreen(PenData penData) {
        // Screen means LCD screen of the tablet.
        return new Point2D.Float(
                (float) penData.getX() * capability.getScreenWidth() / capability.getTabletMaxX(),
                (float) penData.getY() * capability.getScreenHeight() / capability.getTabletMaxY());
    }


    private enum ButtonType {
    NAV, ANSWERVARIANT;
}

private static class RectangleDimensions {
    int height = 0;
    int widht = 0;
}

private class Button {
    java.awt.Rectangle bounds; // in Screen coordinates
    String text;
    ButtonType buttonType = ButtonType.NAV;
    String id = "";
//        ActionListener click = (e -> {
//            this.pressedButtonId = id;
//            this.answerButtonListener
//                    .ansewrButtonPressed(new AnswerButtonPressedEvent(this, "Button pressed id = " + id));
//        });
//
//        void performClick() {
////            actionPerformed(new AnswerButtonPressedEvent(this, "Нахата кнопка id = " + id));
//        }
}


    @Override
    public void onGetReportException(STUException e) {

    }

    @Override
    public void onUnhandledReportData(byte[] bytes) {

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
