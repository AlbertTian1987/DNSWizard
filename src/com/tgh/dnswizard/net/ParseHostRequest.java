package com.tgh.dnswizard.net;

import com.octo.android.robospice.request.SpiceRequest;
import org.xbill.DNS.*;

/**
 * Created with IntelliJ IDEA.
 * User: emellend
 * Date: 13-7-7
 * Time: 下午6:46
 * To change this template use File | Settings | File Templates.
 */
public class ParseHostRequest extends SpiceRequest<Message> {

    private final String mHost;
    private final String mDns;
    private final int mType;
    private final boolean mUseTcp;

    public ParseHostRequest(String host,String dns,int type,boolean useTcp) {
        super(Message.class);
        mHost=host;
        mDns=dns;
        mType=type;
        mUseTcp=useTcp;
    }

    @Override
    public Message loadDataFromNetwork() throws Exception {
        Name name = Name.fromString(mHost, Name.root);
        Record record = Record.newRecord(name, mType, DClass.IN);
        SimpleResolver resolver=new SimpleResolver(mDns);
        resolver.setTCP(mUseTcp);
        Message query = Message.newQuery(record);
        Message response = resolver.send(query);
        return response;
    }
}
