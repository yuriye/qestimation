package com.ys.mfc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ys.mfc.mkgu.MkguQuestionnaires;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class HttpAdapter {
    //    private static final Lock sLock = new ReentrantLock();
//    public static final Logger log = LoggerFactory.getLogger(HttpAdapter.class);
//    private TreeLoader tr;
//    private TreeLoader trPopular;
//    private Map<String, String> localSettings;
//    private final JdomParser jdom = new JdomParser();
    private static final String qualityUrlString = "http://10.2.139.25/mkgu/server/";
    private static final String getFreeTime = "getfreetime";

//    public static final Logger log = LoggerFactory.getLogger(HttpAdapter.class);
//    private final Gson gson = new Gson();
//    private String qualityUrlString = "http://10.200.200.10/cpgu/action/";
//    private String questsPartOfUrl = "getMkguQuestionnaires";
//    private String formVersPartOfUrl = "getMkguFormVersion?orderNumber=";

    //    private String orderNumberString = "0656051"; //hardcoded fo deb
    private static final String getManagedOptions = "getmanagedoptions";
    private static final String getOrderStatus = "cpgu/action/getOrderStatusTitle";
    private static final String getMkguFormVersion = "cpgu/action/getMkguFormVersion";
    private static final String getMkguQuestionnaires = "cpgu/action/getMkguQuestionnaires";
    private static final String postMkguQuestionnaires = "cpgu/action/sendMkguFormAnswers";
    private static HttpAdapter instance;
    private static String testQuestionaries = "\n" +
            "[{\"serviceType\":\"direct\",\"version\":\"0.0.4\",\"xml\":\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\\n<mkgu:body xmlns:mkgu=\\\"http://vashkontrol.ru#\\\" xmlns:ds=\\\"http://www.w3.org/2000/09/xmldsig#\\\" xmlns:ec=\\\"http://www.w3.org/2001/10/xml-exc-c14n#\\\" ID=\\\"mkgu\\\">\\n  <form service-type=\\\"direct\\\">\\n    <version>0.0.4</version>\\n    <updated_at>2013-04-25 11:22:03 UTC</updated_at>\\n  </form>\\n  <blocks>\\n    <block id=\\\"1\\\" queue=\\\"\\\" optional=\\\"false\\\">Как вы оцениваете отдельные аспекты качества государственной (муниципальной) услуги?</block>\\n    <block id=\\\"4\\\" queue=\\\"\\\" optional=\\\"false\\\">комментарий</block>\\n  </blocks>\\n  <indicators>\\n    <indicator id=\\\"9\\\" queue=\\\"2\\\" view-as=\\\"radio\\\" block-id=\\\"1\\\">\\n      <title>Время предоставления государственной услуги</title>\\n      <description>Оцените, соответствует ли срок предоставления услуги вашим ожиданиям и заявленному сроку с момента подачи заявления, включая комплект необходимых документов</description>\\n      <values>\\n        <value id=\\\"41\\\" data-type=\\\"boolean\\\">\\n          <title>Очень плохо</title>\\n          <alt-title>Услугу до сих пор не получил, несмотря на то, что срок предоставления давно истек</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"42\\\" data-type=\\\"boolean\\\">\\n          <title>Плохо</title>\\n          <alt-title>Заявленные сроки не соблюдаются и должны быть короче</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"43\\\" data-type=\\\"boolean\\\">\\n          <title>Нормально</title>\\n          <alt-title>Заявленные сроки соблюдаются, но могли бы быть немного короче</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"44\\\" data-type=\\\"boolean\\\">\\n          <title>Хорошо</title>\\n          <alt-title>Заявленные сроки полностью устраивают и соблюдаются</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"45\\\" data-type=\\\"boolean\\\">\\n          <title>Отлично</title>\\n          <alt-title></alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n      </values>\\n    </indicator>\\n    <indicator id=\\\"10\\\" queue=\\\"3\\\" view-as=\\\"radio\\\" block-id=\\\"1\\\">\\n      <title>Время ожидания в очереди при получении государственной услуги</title>\\n      <description>Оцените ваше отношение к затратам времени на ожидание в очередях при подаче документов и при получении результата услуги</description>\\n      <values>\\n        <value id=\\\"46\\\" data-type=\\\"boolean\\\">\\n          <title>Очень плохо</title>\\n          <alt-title>Пришлось постоять в больших очередях несколько раз</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"47\\\" data-type=\\\"boolean\\\">\\n          <title>Плохо</title>\\n          <alt-title>Пришлось постоять в большой очереди один раз</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"48\\\" data-type=\\\"boolean\\\">\\n          <title>Нормально</title>\\n          <alt-title>Пришлось постоять в небольшой очереди один раз за все время обращения за услугой</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"49\\\" data-type=\\\"boolean\\\">\\n          <title>Хорошо</title>\\n          <alt-title>В очередях не стоял ни разу за все время обращения за услугой</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"50\\\" data-type=\\\"boolean\\\">\\n          <title>Отлично</title>\\n          <alt-title></alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n      </values>\\n    </indicator>\\n    <indicator id=\\\"11\\\" queue=\\\"4\\\" view-as=\\\"radio\\\" block-id=\\\"1\\\">\\n      <title>Вежливость и компетентность сотрудника, взаимодействующего с заявителем при предоставлении государственной услуги</title>\\n      <description>В случае, если вы получали консультацию по телефону или посещали ведомство для подачи или получения документов, оцените, насколько вы удовлетворены отношением и компетентностью сотрудников ведомства (вежливость, полнота предоставляемой информации, внимательность, желание помочь и т. д.)</description>\\n      <values>\\n        <value id=\\\"51\\\" data-type=\\\"boolean\\\">\\n          <title>Очень плохо</title>\\n          <alt-title>Сотрудники хамили или были некомпетентны</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"52\\\" data-type=\\\"boolean\\\">\\n          <title>Плохо</title>\\n          <alt-title>Сотрудники были недостаточно вежливы и/или недостаточно компетентны</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"53\\\" data-type=\\\"boolean\\\">\\n          <title>Нормально</title>\\n          <alt-title>Сотрудники были достаточно вежливы и компетентны</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"54\\\" data-type=\\\"boolean\\\">\\n          <title>Хорошо</title>\\n          <alt-title>Сотрудники были очень вежливы и демонстрировали высокий уровень компетентности</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"55\\\" data-type=\\\"boolean\\\">\\n          <title>Отлично</title>\\n          <alt-title></alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n      </values>\\n    </indicator>\\n    <indicator id=\\\"12\\\" queue=\\\"5\\\" view-as=\\\"radio\\\" block-id=\\\"1\\\">\\n      <title>Комфортность условий в помещении, в котором предоставлена государственная услуга</title>\\n      <description>Оцените уровень комфорта при посещении ведомства (наличие сидячих мест,, столов для заполнения документов, бланков, ручек, образцов заполнения, просторность и комфортность помещений и т. п.)</description>\\n      <values>\\n        <value id=\\\"56\\\" data-type=\\\"boolean\\\">\\n          <title>Очень плохо</title>\\n          <alt-title>Помещение абсолютно не предназначено для обслуживания</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"57\\\" data-type=\\\"boolean\\\">\\n          <title>Плохо</title>\\n          <alt-title>Уровнем комфорта не удовлетворен, есть существенные замечания</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"58\\\" data-type=\\\"boolean\\\">\\n          <title>Нормально</title>\\n          <alt-title>В целом комфортно, но есть незначительные замечания</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"59\\\" data-type=\\\"boolean\\\">\\n          <title>Хорошо</title>\\n          <alt-title>Уровнем комфорта в помещении полностью удовлетворен</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"60\\\" data-type=\\\"boolean\\\">\\n          <title>Отлично</title>\\n          <alt-title></alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n      </values>\\n    </indicator>\\n    <indicator id=\\\"13\\\" queue=\\\"6\\\" view-as=\\\"radio\\\" block-id=\\\"1\\\">\\n      <title>Доступность информации о порядке предоставления государственной услуги</title>\\n      <description>Оцените качество информирования о порядке предоставления услуги и ее соответствие действительности (необходимые для предоставления документы, сроки оказания услуги, стоимость услуги) на официальном сайте ведомства, на Едином портале госуслуг, на информационном стенде в ведомстве, при получении консультации по телефону или при посещении ведомства</description>\\n      <values>\\n        <value id=\\\"61\\\" data-type=\\\"boolean\\\">\\n          <title>Очень плохо</title>\\n          <alt-title>Потратил много времени, но информацию не нашел, или она не соответствует действительности</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"62\\\" data-type=\\\"boolean\\\">\\n          <title>Плохо</title>\\n          <alt-title>Информацию не нашел, или она  оказалась недостаточно точной , подробной или вовсе недостоверной</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"63\\\" data-type=\\\"boolean\\\">\\n          <title>Нормально</title>\\n          <alt-title>Информацию получил в полном объеме, но пришлось потратить больше времени на ее поиск, чем хотелось</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"64\\\" data-type=\\\"boolean\\\">\\n          <title>Хорошо</title>\\n          <alt-title>Информацию получил быстро и в полном объеме</alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n        <value id=\\\"65\\\" data-type=\\\"boolean\\\">\\n          <title>Отлично</title>\\n          <alt-title></alt-title>\\n          <description></description>\\n          <min></min>\\n          <max></max>\\n        </value>\\n      </values>\\n    </indicator>\\n  </indicators>\\n</mkgu:body>\\n\"}]";

    private static String testFormVersion = "{\"serviceType\":\"direct\",\"status\":\"OK\",\"version\":\"0.0.4\"}";

    private final Gson gson = new Gson();

    public static HttpAdapter getInstance() {
        if (instance == null) {
            instance = new HttpAdapter();
        }
        return instance;
    }

    public Map<String, String> getMkguFormVersion(String orderNumber) {
        Object result = new HashMap();

        try {
            InputStreamReader isr;
            if (!Main.onlyUI) {
                String urlString = qualityUrlString + getMkguFormVersion + "?orderNumber=" + URLEncoder.encode(orderNumber, "UTF-8");
//            log.info("getMkguFormVersion: {}", urlString);
                URL url = new URL(urlString);
                isr = new InputStreamReader(url.openStream(), "UTF-8");
            }
            else {
                isr = new InputStreamReader(new ByteArrayInputStream(testFormVersion.getBytes()));
            }
            try {
                Type queueModelType = (new TypeToken<Map<String, String>>() {
                }).getType();
                result = (Map) this.gson.fromJson(isr, queueModelType);
            } finally {
                isr.close();
            }
        } catch (IOException var11) {
//            log.error("Connection fail");
        }

        return (Map) result;
    }


    public List<MkguQuestionnaires> getMkguQuestionnaires(boolean mock) {
        List result = new ArrayList();

        try {
            URL url = new URL(qualityUrlString + getMkguQuestionnaires);
            InputStreamReader isr;
            if (!mock)
                isr = new InputStreamReader(url.openStream(), "UTF-8");
            else
                isr = new InputStreamReader(new ByteArrayInputStream(testQuestionaries.getBytes()));

            try {

                result = (List) this.gson.fromJson(isr, (new TypeToken<ArrayList<MkguQuestionnaires>>() {
                }).getType());
            } finally {
                isr.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public int postQuestions(String version, String orderNumber, String answers) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        try {
            String query = "<body><form-version>" + version + "</form-version>" + "<orderNumber>" + orderNumber + "</orderNumber>" + "<authorityId>" + "123" + "</authorityId>" + "<receivedDate>" + nowAsISO + "</receivedDate>" + "<okato>" + "50401000000" + "</okato>" + "<rates>" + answers + "</rates>" + "</body>";
            String urlString = qualityUrlString + "cpgu/action/sendMkguFormAnswers";
//            log.info("postQuestionsMkgu: {}", urlString);
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(urlString);
            post.setHeader("User-Agent", "UserAgent");
            List<BasicNameValuePair> urlParameters = new ArrayList();
            urlParameters.add(new BasicNameValuePair("xmls[]", query));
//            log.info("query xmls[]: {}", query);
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            return response.getStatusLine().getStatusCode();
        } catch (UnsupportedEncodingException var13) {
            var13.printStackTrace();
        } catch (ClientProtocolException var14) {
            var14.printStackTrace();
        } catch (IOException var15) {
            var15.printStackTrace();
        }

        return 0;
    }

    public void printPostXml(String version, String orderNumber, String answers) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        System.out.println("<body><form-version>" + version + "</form-version>" + "<orderNumber>" + orderNumber + "</orderNumber>" + "<authorityId>" + "123" + "</authorityId>" + "<receivedDate>" + nowAsISO + "</receivedDate>" + "<okato>" + "50401000000" + "</okato>" + "<rates>" + answers + "</rates>" + "</body>");
    }

}
