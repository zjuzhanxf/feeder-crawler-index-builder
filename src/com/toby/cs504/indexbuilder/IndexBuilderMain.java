package com.toby.cs504.indexbuilder;

/**
 * Created by xiaofeng on 11/22/17.
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.toby.cs504.ad.Ad;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class IndexBuilderMain {

    private static final String mMemcachedServer = "127.0.0.1";
    private static final int mMemcachedPortal = 11211;
    private static final String mysql_host = "127.0.0.1:3306";
    private static final String mysql_db = "search_ads_homework2";
    private static final String mysql_user = "root";
    private static final String mysql_pass = "password";

    private final static String IN_QUEUE_NAME = "q_product";
    private static ObjectMapper mapper;

    public static void main(String[] args) throws IOException,TimeoutException,InterruptedException{

        mapper = new ObjectMapper();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel inChannel = connection.createChannel();
        inChannel.queueDeclare(IN_QUEUE_NAME, true, false, false, null);
        inChannel.basicQos(10);
        System.out.println("[*] Waiting for messages. To exit press CTRL+C");

        IndexBuilder indexBuilder = new IndexBuilder(mMemcachedServer,mMemcachedPortal,mysql_host,mysql_db,mysql_user,mysql_pass);

        Consumer consumer = new DefaultConsumer(inChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                try {
                    String message = new String(body, "UTF-8");
                    System.out.println("[x] Received '" + message + "'");

                    JSONObject adJson = new JSONObject(message);
                    Ad ad = new Ad();
                    if(adJson.isNull("adId") || adJson.isNull("campaignId")) {

                    }
                    // ad.adId = adJson.getLong("adId");
                    ad.campaignId = adJson.isNull("campaignId")? 0:adJson.getLong("campaignId");
                    ad.relevanceScore =  adJson.isNull("relevanceScore")?0.0:adJson.getDouble("relevanceScore");
                    ad.pClick = adJson.isNull("pClick")? 0.0 : adJson.getDouble("pClick");
                    ad.bidPrice = adJson.isNull("bidPrice")? 1.0 : adJson.getDouble("bidPrice");
                    ad.rankScore = adJson.isNull("rankScore")? 0.0 : adJson.getDouble("rankScore");
                    ad.qualityScore = adJson.isNull("qualityScore")? 0.0 : adJson.getDouble("qualityScore");
                    ad.costPerClick = adJson.isNull("costPerClick")? 0.0 : adJson.getDouble("costPerClick");
                    ad.position = adJson.isNull("position")? 0 : adJson.getInt("position");
                    ad.title = adJson.isNull("title") ? "" : adJson.getString("title");
                    ad.keyWords = Utility.cleanedTokenize(ad.title);
                    ad.price = adJson.isNull("price") ? 100.0 : adJson.getDouble("price");
                    ad.thumbnail = adJson.isNull("thumbnail") ? "" : adJson.getString("thumbnail");
                    ad.description = adJson.isNull("description") ? "" : adJson.getString("description");
                    ad.brand = adJson.isNull("brand") ? "" : adJson.getString("brand");
                    ad.detail_url = adJson.isNull("detail_url") ? "" : adJson.getString("detail_url");
                    ad.query = adJson.isNull("query")? "":adJson.getString("query");
                    ad.query_group_id = adJson.isNull("query_group_id") ? 0L:adJson.getLong("query_group_id");
                    ad.category =  adJson.isNull("category") ? "" : adJson.getString("category");

                    Long generatedAdId = indexBuilder.buildForwardIndex(ad);
                    indexBuilder.buildInvertIndex(ad, generatedAdId);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        inChannel.basicConsume(IN_QUEUE_NAME, true, consumer);
        // indexBuilder.Close();

        return;


        // ==== below is reference..
//        if (args.length != 2) {
//            System.out.println("Usage: IndexBuilder <adsDataFilePath> <budgetDataFilePath>");
//            System.exit(0);
//        }
//
//        String mAdsDataFilePath = args[0];
//        String mBudgetFilePath = args[1];
//
//        IndexBuilder indexBuilder = new IndexBuilder(mMemcachedServer,mMemcachedPortal,mysql_host,mysql_db,mysql_user,mysql_pass);
//
//        try (BufferedReader brAd = new BufferedReader(new FileReader(mAdsDataFilePath))) {
//            String line;
//            while ((line = brAd.readLine()) != null) {
//                JSONObject adJson = new JSONObject(line);
//                Ad ad = new Ad();
//                if(adJson.isNull("adId") || adJson.isNull("campaignId")) {
//                    continue;
//                }
//                ad.adId = adJson.getLong("adId");
//                ad.campaignId = adJson.getLong("campaignId");
//                ad.brand = adJson.isNull("brand") ? "" : adJson.getString("brand");
//                ad.price = adJson.isNull("price") ? 100.0 : adJson.getDouble("price");
//                ad.thumbnail = adJson.isNull("thumbnail") ? "" : adJson.getString("thumbnail");
//                ad.title = adJson.isNull("title") ? "" : adJson.getString("title");
//                ad.detail_url = adJson.isNull("detail_url") ? "" : adJson.getString("detail_url");
//                ad.bidPrice = adJson.isNull("bidPrice") ? 1.0 : adJson.getDouble("bidPrice");
//                ad.pClick = adJson.isNull("pClick") ? 0.0 : adJson.getDouble("pClick");
//                ad.category =  adJson.isNull("category") ? "" : adJson.getString("category");
//                ad.description = adJson.isNull("description") ? "" : adJson.getString("description");
//                ad.keyWords = new ArrayList<String>();
//                JSONArray keyWords = adJson.isNull("keyWords") ? null :  adJson.getJSONArray("keyWords");
//                for(int j = 0; j < keyWords.length();j++)
//                {
//                    ad.keyWords.add(keyWords.getString(j));
//                }
//
//                if(!indexBuilder.buildInvertIndex(ad)) {
//                    System.out.println("ERROR! Failed to insert ad into Memcached");
//                }
//                if (!indexBuilder.buildForwardIndex(ad)) {
//                    System.out.println("ERROR! Failed to insert ad into DB");
//                }
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //load budget data
//        try (BufferedReader brBudget = new BufferedReader(new FileReader(mBudgetFilePath))) {
//            String line;
//            while ((line = brBudget.readLine()) != null) {
//                JSONObject campaignJson = new JSONObject(line);
//                Long campaignId = campaignJson.getLong("campaignId");
//                double budget = campaignJson.getDouble("budget");
//                Campaign camp = new Campaign();
//                camp.campaignId = campaignId;
//                camp.budget = budget;
//                if(!indexBuilder.updateBudget(camp))
//                {
//                    //log
//                }
//            }
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        indexBuilder.Close();
//
//        return;
    }

}
