package com.ys.mfc.mkgu;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ys.mfc.HttpAdapter;
import com.ys.mfc.Main;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class MkguQuestions {

    private static MkguQuestionXmlRoot getQuestions(Map<String, String> mkguFormVersion, String text) {

        List<MkguQuestionnaires> mkguQuestionnaires = HttpAdapter.getInstance().getMkguQuestionnaires(Main.onlyUI);
        MkguQuestionXmlRoot mkguQuestionXmlRoot = new MkguQuestionXmlRoot();
        String xml = "";
        Iterator mkguQuestionnairesIterator = mkguQuestionnaires.iterator();

        while(mkguQuestionnairesIterator.hasNext()) {
            MkguQuestionnaires mq = (MkguQuestionnaires)mkguQuestionnairesIterator.next();
            if (mq.getVersion().equals(mkguFormVersion.get("version"))) {
                mkguQuestionXmlRoot.setVersion((String)mkguFormVersion.get("version"));
                xml = mq.getXml();
            }
        }

        xml = xml.replaceAll("\\n", "");
        xml = xml.replaceAll("\\\\", "");
        SAXBuilder saxBuilder = new SAXBuilder();
        mkguQuestionXmlRoot.setOrderNumber(text);

        try {
            Document doc = saxBuilder.build(new StringReader(xml));
            List<MkguQuestionXmlIndicator> mkguQuestionXmlIndicators = new ArrayList();
            Element rootElement = doc.getRootElement();
            Element blocks = (Element)rootElement.getChildren("blocks").get(0);
            mkguQuestionXmlRoot.setQuestionTitle(((Element)blocks.getChildren().get(0)).getValue());
            Element indicators = (Element)rootElement.getChildren("indicators").get(0);
            Iterator iterator = indicators.getChildren().iterator();

            while(iterator.hasNext()) {
                MkguQuestionXmlIndicator mkguQuestionXmlIndicator = new MkguQuestionXmlIndicator();
                Element next = (Element)iterator.next();
                mkguQuestionXmlIndicator.setIndicatorId(next.getAttribute("id").getValue());
                mkguQuestionXmlIndicator.setQuestionTitle(next.getChild("title").getValue());
                mkguQuestionXmlIndicator.setDescriptionTitle(next.getChild("description").getValue());
                List<Element> values = next.getChild("values").getChildren();
                List<MkguQuestionXmlQuestions> MkguQuestionXmlList = new ArrayList();
                Iterator elementIterator = values.iterator();

                while(elementIterator.hasNext()) {
                    Element element = (Element)elementIterator.next();
                    MkguQuestionXmlList.add(new MkguQuestionXmlQuestions(element.getAttribute("id").getValue(), element.getChild("title").getValue(), element.getChild("alt-title").getValue()));
                }

                mkguQuestionXmlIndicator.setIndicator(MkguQuestionXmlList);
                mkguQuestionXmlIndicators.add(mkguQuestionXmlIndicator);
            }

            mkguQuestionXmlRoot.setIndicator(mkguQuestionXmlIndicators);
            return mkguQuestionXmlRoot;
        } catch (JDOMException var19) {
            ;
        } catch (IOException var20) {
            ;
        }

        return new MkguQuestionXmlRoot();
    }
}
