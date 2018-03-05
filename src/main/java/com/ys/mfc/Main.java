package com.ys.mfc;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    public static final Logger log = LoggerFactory.getLogger(HttpAdapter.class);

    public static void main(String[] args) {
        String orderCode = "0656051";
        HttpAdapter adapter = HttpAdapter.getInstance();
        Map mkguFormVersion = adapter.getMkguFormVersion(orderCode);
        if ("OK".equals(mkguFormVersion.get("status"))) {
            MkguQuestionXmlRoot questions = getQuestions(mkguFormVersion, orderCode);
            System.out.println(questions.getQuestionTitle());
            System.out.println(questions.getOrderNumber());
            System.out.println(questions.getIndicator().size());

//            questions.getIndicator().stream().forEach(e -> {
            for(MkguQuestionXmlIndicator e:  questions.getIndicator()) {
                System.out.println(e.getQuestionTitle());
                System.out.println(e.getDescriptionTitle());
                System.out.println(e.getIndicator());
                System.out.println(e.getIndicatorId());
            }

//            });


//            System.out.println(getQuestions(mkguFormVersion, "0656051").getQuestionTitle());
//            List<MkguQuestionnaires> questionnaires = adapter.getMkguQuestionnaires();

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

                values.forEach( element1 -> mkguQuestionXmlList.add(
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
