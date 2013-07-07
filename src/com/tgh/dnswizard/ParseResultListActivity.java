package com.tgh.dnswizard;

import android.os.Bundle;
import android.util.Log;
import com.actionbarsherlock.app.SherlockListActivity;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tgh.dnswizard.net.ParseHostRequest;
import org.xbill.DNS.Message;

/**
 * Created with IntelliJ IDEA.
 * User: emellend
 * Date: 13-7-6
 * Time: 下午5:50
 * To change this template use File | Settings | File Templates.
 */
public class ParseResultListActivity extends SherlockListActivity implements RequestListener<Message>{

    public static final String ACTION_PARSE_AND_SHOWLOG="com.tgh.dns.ParseAndShowLog";
    public static final String ACTION_SHOWLOG="com.tgh.dns.ShowLog";
    private boolean mUseTcp;
    private int mType;
    private String mDNS;
    private String mHost;
    private SpiceManager mSpiceManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpiceManager = new SpiceManager(UncachedSpiceService.class);
        String action = getIntent().getAction();
        if (ACTION_PARSE_AND_SHOWLOG.equals(action)){
            getArgsFromIntent();
            parseHost();
        }
    }

    @Override
    protected void onResume() {
        mSpiceManager.start(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mSpiceManager.shouldStop();
        super.onPause();
    }

    private void parseHost() {
        mSpiceManager.execute(new ParseHostRequest(mHost,mDNS,mType,mUseTcp),this);
    }

    private void getArgsFromIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras==null){
            return;
        }
        mHost = extras.getString(GuiActivity.HOST);
        mDNS = extras.getString(GuiActivity.DNS);
        mType = extras.getInt(GuiActivity.TYPE);
        mUseTcp = extras.getBoolean(GuiActivity.USE_TCP);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        Log.e("test", spiceException.getMessage(),spiceException);
    }

    @Override
    public void onRequestSuccess(Message message) {
        Log.i("test",message.toString());
    }
}