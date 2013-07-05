package com.tgh.dnswizard;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.tgh.dnswizard.fragment.WheelsFragment;

/**
 * Created with IntelliJ IDEA.
 * User: emellend
 * Date: 13-7-5
 * Time: 上午11:07
 * To change this template use File | Settings | File Templates.
 */
public class GuiActivity extends SherlockFragmentActivity implements WheelsFragment.DNSObserver {

    private WheelsFragment mWheelsFragment;
    private EditText mDns;
    private EditText mHost;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gui);
        initUIComponent();
    }

    private void initUIComponent() {
        FragmentManager fm = getSupportFragmentManager();
        mWheelsFragment = (WheelsFragment) fm.findFragmentById(R.id.frag_wheels);
        mWheelsFragment.setObserver(this);

        mHost = (EditText) findViewById(R.id.host);
        mDns = (EditText) findViewById(R.id.dns);
    }

    @Override
    public void onDnsValueChanged(String dnsValue) {
        mDns.setText(dnsValue);
    }
}