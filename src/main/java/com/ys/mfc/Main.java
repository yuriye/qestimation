package com.ys.mfc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.WacomGSS.STU.Protocol.InkingMode;
import com.WacomGSS.STU.Protocol.PenData;
import com.WacomGSS.STU.Protocol.ProtocolHelper;
import com.WacomGSS.STU.STUException;
import com.WacomGSS.STU.UsbDevice;
import com.ys.mfc.mkgu.MkguQuestionXmlIndicator;
import com.ys.mfc.mkgu.MkguQuestionXmlQuestions;
import com.ys.mfc.mkgu.MkguQuestionXmlRoot;
import com.ys.mfc.mkgu.MkguQuestionnaires;
import com.ys.mfc.util.BuyImage;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;


public class Main extends JFrame {
    public static final Logger log = LoggerFactory.getLogger(HttpAdapter.class);

    static HttpAdapter adapter = HttpAdapter.getInstance();
    private static Map mkguFormVersion;

    private String informString;
    private JLabel informStringLabel = new JLabel();
    private JPanel panel = new JPanel();
    private BufferedImage buyImage;

    public Main() {
        this.setTitle("Оценка качеcтва оказания услуг");
        informStringLabel.setText("Начинаем...");
        this.panel.setPreferredSize(new Dimension(400, 200));
        this.panel.add(informStringLabel);
        this.add(this.panel, BorderLayout.NORTH);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {

        Main mainFrame = new Main();
        String orderCode = JOptionPane.showInputDialog("Введите код заявления");

        try {
            com.WacomGSS.STU.UsbDevice[] usbDevices = UsbDevice.getUsbDevices();
            if (usbDevices != null && usbDevices.length > 0) {

                mkguFormVersion = adapter.getMkguFormVersion(orderCode);
                MkguQuestionXmlRoot questions = getQuestions(mkguFormVersion, orderCode);
                mainFrame.setVisible(true);
                EstimationQuestionForm estimationQuestionForm = null;
                for (MkguQuestionXmlIndicator estimationQuestion : questions.getIndicator()) {
                    List<AnswerVariant> answerVariants = new ArrayList<>();
                    List<MkguQuestionXmlQuestions> stepAnswerVariants = estimationQuestion.getIndicator();

                    stepAnswerVariants.forEach(variant ->
                            answerVariants.add(new AnswerVariant(
                                    variant.getQuestionValue(),
                                    variant.getQuestionText(),
                                    variant.getAltTitle()))
                    );
                    estimationQuestionForm = new EstimationQuestionForm(
                            usbDevices[0],
                            estimationQuestion.getIndicatorId(),
                            estimationQuestion.getIndicatorId(),
                            estimationQuestion.getQuestionTitle(),
                            estimationQuestion.getDescriptionTitle(),
                            answerVariants);

                    while (estimationQuestionForm.getPressedButtonId() == null) {
                        Thread.sleep(100);
                    }
                    mainFrame.informStringLabel.setText("Ответ = " + estimationQuestionForm.getPressedButtonId());
                    System.out.println(estimationQuestionForm.getPressedButtonId());
                    estimationQuestionForm.dispose();
                }
                mainFrame.informStringLabel.setText("Оценка завершена");
                if (estimationQuestionForm != null) {
                    BufferedImage buyImage = new BuyImage(estimationQuestionForm.getCapability()).getBitmap();
                    byte[] bitmapData = ProtocolHelper.flatten(buyImage,
                            buyImage.getWidth(), buyImage.getHeight(),
                            true);

                    // Add the delagate that receives pen data.
                    estimationQuestionForm.getTablet().addTabletHandler(estimationQuestionForm);

                    // Enable the pen data on the screen (if not already)
                    estimationQuestionForm.getTablet().setInkingMode(InkingMode.Off);
                    estimationQuestionForm.getTablet().writeImage(estimationQuestionForm.getEncodingMode(),
                            estimationQuestionForm.getBitmapData());
                }


                return;

            } else {
                throw new RuntimeException("No USB tablets attached");
            }
        } catch (STUException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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

}
