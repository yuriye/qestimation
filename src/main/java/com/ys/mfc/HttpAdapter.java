package com.ys.mfc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ys.mfc.mkgu.MkguQuestionnaires;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpAdapter {
    private static HttpAdapter instance;
    public static final Logger log = LoggerFactory.getLogger(HttpAdapter.class);
    private final Gson gson = new Gson();
//        private String qualityUrlString = "http://10.200.200.10/cpgu/action/";
//    private String qualityUrlString = "http://10.2.139.25/mkgu/server/";
    private String qualityUrlString = "http://10.200.200.10/";
    //    private String questsPartOfUrl = "getMkguQuestionnaires";
    private String questsPartOfUrl = "cpgu/action/getMkguQuestionnaires";
    //    private String formVersPartOfUrl = "getMkguFormVersion?orderNumber=";
    private String formVersPartOfUrl = "cpgu/action/getMkguFormVersion?orderNumber=";
//    private String orderNumberString = "0656051"; //hardcoded fo deb

    private HttpAdapter() {

    }

    public static HttpAdapter getInstance() {
        if (instance == null) {
            instance = new HttpAdapter();
        }
        return instance;
    }

    public Map<String, String> getMkguFormVersion(String orderNumber) {
        Object result = new HashMap();

        try {
            String urlString = qualityUrlString + formVersPartOfUrl + URLEncoder.encode(orderNumber, "UTF-8");
            log.info("getMkguFormVersion: {}", urlString);
            URL url = new URL(urlString);
            InputStreamReader isr = new InputStreamReader(url.openStream(), "UTF-8");

            try {
                Type queueModelType = (new TypeToken<Map<String, String>>() {
                }).getType();
                result = (Map) this.gson.fromJson(isr, queueModelType);
            } finally {
                isr.close();
            }
        } catch (IOException var11) {
            log.error("Connection fail");
        }

        return (Map) result;
    }


    public List<MkguQuestionnaires> getMkguQuestionnaires() {
        List result = new ArrayList();

        try {
            URL url = new URL(qualityUrlString + questsPartOfUrl);

            InputStreamReader isr = new InputStreamReader(url.openStream(), "UTF-8");

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

    public String postQuestions(String version, String orderNumber, String answers) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        try {
            String query = "<body><form-version>" + version + "</form-version>" + "<orderNumber>" + orderNumber + "</orderNumber>" + "<authorityId>" + "123" + "</authorityId>" + "<receivedDate>" + nowAsISO + "</receivedDate>" + "<okato>" + "50401000000" + "</okato>" + "<rates>" + answers + "</rates>" + "</body>";
            String urlString = qualityUrlString + "cpgu/action/sendMkguFormAnswers";
            log.info("postQuestionsMkgu: {}", urlString);
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(urlString);
            post.setHeader("User-Agent", "UserAgent");
            List<BasicNameValuePair> urlParameters = new ArrayList();
            urlParameters.add(new BasicNameValuePair("xmls[]", query));
            log.info("query xmls[]: {}", query);
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            return response.getStatusLine().getReasonPhrase() + " (код = " + response.getStatusLine().getStatusCode() + ")";
        } catch (UnsupportedEncodingException var13) {
            var13.printStackTrace();
        } catch (ClientProtocolException var14) {
            var14.printStackTrace();
        } catch (IOException var15) {
            var15.printStackTrace();
        }

        return "";
    }

    public void printPostXml(String version, String orderNumber, String answers) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        System.out.println("<body><form-version>" + version + "</form-version>" + "<orderNumber>" + orderNumber + "</orderNumber>" + "<authorityId>" + "123" + "</authorityId>" + "<receivedDate>" + nowAsISO + "</receivedDate>" + "<okato>" + "50401000000" + "</okato>" + "<rates>" + answers + "</rates>" + "</body>");

    }

}
