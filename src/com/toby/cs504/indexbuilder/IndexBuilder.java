package com.toby.cs504.indexbuilder;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.toby.cs504.ad.Ad;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.FailureMode;

/**
 * Created by xiaofeng on 11/24/17.
 */
public class IndexBuilder {
    private int EXP = 0; //0: never expire
    private String mMemcachedServer;
    private int mMemcachedPortal;
    private String mysql_host;
    private String mysql_db;
    private String mysql_user;
    private String mysql_pass;
    private MySQLAccess mysql;
    private MemcachedClient cache;


    public void Close() {
        if(mysql != null) {
            try {
                mysql.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public IndexBuilder(String memcachedServer,int memcachedPortal,String mysqlHost,String mysqlDb,String user,String pass)
    {
        mMemcachedServer = memcachedServer;
        mMemcachedPortal = memcachedPortal;
        mysql_host = mysqlHost;
        mysql_db = mysqlDb;
        mysql_user = user;
        mysql_pass = pass;
        mysql = new MySQLAccess(mysql_host, mysql_db, mysql_user, mysql_pass);
        String address = mMemcachedServer + ":" + mMemcachedPortal;
        try
        {
            cache = new MemcachedClient(new ConnectionFactoryBuilder().setDaemon(true).setFailureMode(FailureMode.Retry).build(), AddrUtil.getAddresses(address));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public Boolean buildInvertIndex(Ad ad, Long id)
    {
        ad.adId = id;
        String keyWords = Utility.strJoin(ad.keyWords, ",");
        List<String> tokens = Utility.cleanedTokenize(keyWords);
        for(int i = 0; i < tokens.size();i++)
        {
            String key = tokens.get(i);
            if(cache.get(key) instanceof Set)
            {
                @SuppressWarnings("unchecked")
                Set<Long>  adIdList = (Set<Long>)cache.get(key);
                adIdList.add(ad.adId);
                cache.set(key, EXP, adIdList);
            }
            else
            {
                Set<Long>  adIdList = new HashSet<Long>();
                adIdList.add(ad.adId);
                cache.set(key, EXP, adIdList);
            }
        }
        return true;
    }
    public Long buildForwardIndex(Ad ad)
    {
        Long id = -1L;
        try
        {
            id = mysql.addAdData(ad);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return id;
        }
        return id;
    }
    public Boolean updateBudget(Campaign camp)
    {
        try
        {
            mysql.addCampaignData(camp);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
