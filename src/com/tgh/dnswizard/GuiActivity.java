package com.tgh.dnswizard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.tgh.dnswizard.fragment.WheelsFragment;
import org.xbill.DNS.Type;

/**
 * Created with IntelliJ IDEA.
 * User: emellend
 * Date: 13-7-5
 * Time: 上午11:07
 * To change this template use File | Settings | File Templates.
 */
public class GuiActivity extends SherlockFragmentActivity implements WheelsFragment.DNSObserver, View.OnClickListener {

    public static final String HOST = "host";
    public static final String DNS = "dns";
    public static final String USE_TCP = "useTcp";
    public static final String TYPE = "type";
    private WheelsFragment mWheelsFragment;
    private EditText mDns;
    private EditText mHost;
    private Context mContext;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_gui);
        initUIComponent();
    }

    private void initUIComponent() {
        FragmentManager fm = getSupportFragmentManager();
        mWheelsFragment = (WheelsFragment) fm.findFragmentById(R.id.frag_wheels);
        mWheelsFragment.setObserver(this);

        mHost = (EditText) findViewById(R.id.host);
        mDns = (EditText) findViewById(R.id.dns);

        findViewById(R.id.parse).setOnClickListener(this);

    }

    @Override
    public void onDnsValueChanged(String dnsValue) {
        mDns.setText(dnsValue);
    }

    @Override
    public void onClick(View v) {

        String host = mHost.getText().toString().trim();
        String dns = mDns.getText().toString().trim();

        if (TextUtils.isEmpty(host)||TextUtils.isEmpty(dns)){
            Toast.makeText(mContext,R.string.gui_toast_query_not_null,Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent=new Intent(ParseResultListActivity.ACTION_PARSE_AND_SHOWLOG);
        intent.putExtra(HOST,host);
        intent.putExtra(DNS,dns);
        intent.putExtra(USE_TCP,true);
        intent.putExtra(TYPE, Type.A);
        startActivity(intent);
    }
}