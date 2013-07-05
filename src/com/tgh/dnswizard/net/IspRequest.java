package com.tgh.dnswizard.net;

import android.content.Context;
import com.octo.android.robospice.request.SpiceRequest;
import com.tgh.dnswizard.DNSApplication;
import com.tgh.dnswizard.R;
import com.tgh.dnswizard.bean.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: emellend
 * Date: 13-7-4
 * Time: 上午11:15
 * To change this template use File | Settings | File Templates.
 */
public class IspRequest extends SpiceRequest<Void>{

    private String url;
    private Context context;

    public IspRequest(Context context) {
        super(Void.class);
        this.url=context.getString(R.string.isp_url);
        this.context=context;
    }

    @Override
    public Void loadDataFromNetwork() throws Exception {
        HttpURLConnection urlConnection=null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            performPersistence(inputStream);
        } finally {
            if (urlConnection!=null){
                urlConnection.disconnect();
            }
        }
        return null;
    }

    private void performPersistence(final InputStream inputStream) {
        final DaoSession daoSession = DNSApplication.getDaoSession();
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                try {
                    DNSDao.dropTable(daoSession.getDatabase(),true);
                    CityDao.dropTable(daoSession.getDatabase(),true);
                    ProvinceDao.dropTable(daoSession.getDatabase(),true);
                    ISPDao.dropTable(daoSession.getDatabase(),true);

                    ISPDao.createTable(daoSession.getDatabase(),true);
                    ProvinceDao.createTable(daoSession.getDatabase(),true);
                    CityDao.createTable(daoSession.getDatabase(),true);
                    DNSDao.createTable(daoSession.getDatabase(),true);

                    parse(inputStream,daoSession);
                } catch (XmlPullParserException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static void parse(InputStream inputStream,DaoSession daoSession) throws XmlPullParserException,IOException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        final XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(inputStream, "UTF-8");
        ISP isp=null;
        Province province=null;
        City city=null;
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType){
                case XmlPullParser.START_TAG:
                    String tagName=xpp.getName();
                    if ("isp".equals(tagName)){
                        isp=persistenceISP(daoSession,xpp);
                    }
                    else if ("province".equals(tagName)){
                        province = persistenceProvince(daoSession, isp, xpp);
                    }
                    else if ("city".equals(tagName)){
                        city=persistenceCity(daoSession,province,xpp);
                    }
                    else if ("dns".equals(tagName)){
                        persistenceDNS(daoSession,city,xpp);
                    }
                    break;
            }
            eventType = xpp.next();
        }
    }

    private static ISP persistenceISP(DaoSession session,XmlPullParser xpp) {
        ISP isp =new ISP();
        isp.setText(xpp.getAttributeValue(0));
        long rowId = session.getISPDao().insert(isp);
        isp.setId(rowId);
        return isp;
    }

    private static Province persistenceProvince(DaoSession session,ISP isp,XmlPullParser xpp) {
        Province province=new Province();
        province.setText(xpp.getAttributeValue(0));
        long rowId = session.getProvinceDao().insert(province);
        province.setId(rowId);
        province.setISP(isp);
        province.update();
        return province;
    }
    private static City persistenceCity(DaoSession session,Province province,XmlPullParser xpp) {
        City city=new City();
        city.setText(xpp.getAttributeValue(0));
        long rowId = session.getCityDao().insert(city);
        city.setId(rowId);
        city.setProvince(province);
        city.update();
        return city;
    }
    private static void persistenceDNS(DaoSession session,City city,XmlPullParser xpp) throws IOException,XmlPullParserException{
        DNS dns=new DNS();
        dns.setText(xpp.nextText());
        long rowId = session.getDNSDao().insert(dns);
        dns.setId(rowId);
        dns.setCity(city);
        dns.update();
    }
}
