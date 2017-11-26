package com.toby.cs504.feeder;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class FeederMain {

    private static void publishCrawlerFeed(String rawQueryDataFilePath) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare("q_feeds", true, false, false, null);

        try (BufferedReader br = new BufferedReader(new FileReader(rawQueryDataFilePath))) {

            String line;
            while ((line = br.readLine()) != null) {
                if(line.isEmpty())
                    continue;
                channel.basicPublish("", "q_feeds", null, line.getBytes("UTF-8"));
                System.out.println("[x] Sent '" + line + "'");
                Thread.sleep(2000);
            }

        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        channel.close();
        connection.close();
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 1) {
            System.out.println("Usage: FeederMain <rawQueryFilePath>");
        }
        String rawQueryFilePath = args[0];
        publishCrawlerFeed(rawQueryFilePath);
    }
}
