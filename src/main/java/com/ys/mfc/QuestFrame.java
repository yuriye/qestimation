package com.ys.mfc;

import com.WacomGSS.STU.STUException;
import com.WacomGSS.STU.UsbDevice;

import com.ys.mfc.mkgu.MkguQuestionXmlIndicator;
import com.ys.mfc.mkgu.MkguQuestionXmlQuestions;
import com.ys.mfc.mkgu.MkguQuestionXmlRoot;
import com.ys.mfc.mkgu.MkguQuestionnaires;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.*;
import java.awt.*;

public class QuestFrame extends JFrame {

    private String orderCode = "0656051";
    private HttpAdapter adapter = HttpAdapter.getInstance();
    private Map mkguFormVersion;
    private MkguQuestionXmlRoot questions;

    private com.WacomGSS.STU.UsbDevice[] usbDevices = UsbDevice.getUsbDevices();

    public QuestFrame() throws HeadlessException {
        this.setTitle("Оценка качества оказания услуги");
        this.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton btn = new JButton("Старт");
        btn.addActionListener(evt -> {
            try {
                onStart();
            } catch (STUException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        panel.add(btn);
        this.add(panel);
    }

    private static MkguQuestionXmlRoot getQuestions(Map<String, String> mkguFormVersion, String orderNumber) {
        java.util.List<MkguQuestionnaires> mkguQuestionnaires = HttpAdapter.getInstance().getMkguQuestionnaires();
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
            java.util.List<MkguQuestionXmlIndicator> mkguQuestionXmlIndicators = new ArrayList<>();
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
                java.util.List<Element> values = element.getChild("values").getChildren();
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

    private static void runProgram() {
        QuestFrame questFrame = new QuestFrame();
        questFrame.pack();
        questFrame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> runProgram());
    }

    private void onStart() throws STUException, InterruptedException {

        mkguFormVersion = adapter.getMkguFormVersion(orderCode);
        if ("OK".equals(mkguFormVersion.get("status"))) {
            if (usbDevices == null || usbDevices.length <= 0) {
                throw new RuntimeException("No USB tablets attached");
            }
//            orderCode = orderCodeTextFieldl.getText();
            MkguQuestionXmlRoot questions = getQuestions(mkguFormVersion, orderCode);
            for (MkguQuestionXmlIndicator estimationQuestion : questions.getIndicator()) {
                java.util.List<AnswerVariant> answerVariants = new ArrayList<>();
                java.util.List<MkguQuestionXmlQuestions> stepAnswerVariants = estimationQuestion.getIndicator();

                stepAnswerVariants.forEach(variant ->
                        answerVariants.add(new AnswerVariant(
                                variant.getQuestionValue(),
                                variant.getQuestionText(),
                                variant.getAltTitle()))
                );

                EstimationQuestionForm estimationQuestionForm = new EstimationQuestionForm(usbDevices[0],
                        estimationQuestion.getIndicatorId(),
                        estimationQuestion.getIndicatorId(),
                        estimationQuestion.getQuestionTitle(),
                        estimationQuestion.getDescriptionTitle(),
                        answerVariants);

                estimationQuestionForm.setAnswerButtonListener(evt ->
                        System.out.println(estimationQuestionForm.getPressedButtonId()));

//                estimationQuestionForm.setAnswerButtonListener(e -> System.out.println("Нажата кнопка ответа "
//                        + estimationQuestionForm.getPressedButtonId()));
//                estimationQuestionForm.setVisible(true);
                while (estimationQuestionForm.getPressedButtonId() == null) {
                    Thread.sleep(100);
                    Thread.yield();
                }
                System.out.println(estimationQuestionForm.getPressedButtonId());
            }
        } else {
            throw new RuntimeException("No USB tablets attached");
        }
    }
}
