package com.ys.mfc;

import com.WacomGSS.STU.STUException;
import com.WacomGSS.STU.UsbDevice;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AskForOredrCodeDialog extends JDialog {
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


    public AskForOredrCodeDialog() {
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
            EstimationQuestionForm estimationQuestionForm;
            for (MkguQuestionXmlIndicator estimationQuestion : questions.getIndicator()) {
                List<AnswerVariant> answerVariants = new ArrayList<>();
                List<MkguQuestionXmlQuestions> stepAnswerVariants = estimationQuestion.getIndicator();

                stepAnswerVariants.forEach(variant ->
                        answerVariants.add(new AnswerVariant(
                                variant.getQuestionValue(),
                                variant.getQuestionText(),
                                variant.getAltTitle()))
                );

                estimationQuestionForm = new EstimationQuestionForm(usbDevices[0],
                        estimationQuestion.getIndicatorId(),
                        estimationQuestion.getIndicatorId(),
                        estimationQuestion.getQuestionTitle(),
                        estimationQuestion.getDescriptionTitle(),
                        answerVariants);
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

    public static void main(String[] args) {
        AskForOredrCodeDialog dialog = new AskForOredrCodeDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
