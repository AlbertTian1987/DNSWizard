package com.tgh.dnswizard.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.common.collect.Lists;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tgh.dnswizard.DNSApplication;
import com.tgh.dnswizard.R;
import com.tgh.dnswizard.bean.*;
import com.tgh.dnswizard.net.IspRequest;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: emellend
 * Date: 13-7-5
 * Time: 下午3:07
 * To change this template use File | Settings | File Templates.
 */
public class WheelsFragment extends SherlockFragment{

    public static interface DNSObserver{
        void onDnsValueChanged(String dnsValue);
    }

    private static final String WHEEL_CONFIG="wheelConfig";
    private static final String ISP_POS = "ispPos";
    private static final String PROVINCE_POS = "provincePos";
    private static final String CITY_POS = "cityPos";
    private static final String DNS_POS = "dnsPos";
    public static final String TAG = "DNSWizard";

    private WheelView mIspWheel;
    private WheelView mProvinceWheel;
    private WheelView mCityWheel;
    private WheelView mDnsWheel;
    private SpiceManager mSpiceManager;

    private List<ISP> mIspData= Lists.newArrayList();
    private List<Province> mProvinceData= Lists.newArrayList();
    private List<City> mCityData= Lists.newArrayList();
    private List<DNS> mDNSData= Lists.newArrayList();

    private int mLastIspPos;
    private int mLastProvincePos;
    private int mLastCityPos;
    private int mLastDNSPos;
    private ProgressDialog mLoadingDialog;

    private boolean mIspScrolling=false;
    private boolean mProvinceScrolling=false;
    private boolean mCityScrolling=false;

    private DNSObserver mObserver=new DNSObserver() {
        @Override
        public void onDnsValueChanged(String dnsValue) {
            //doNothing
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        mSpiceManager = new SpiceManager(UncachedSpiceService.class);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSpiceManager.start(getActivity());
    }

    @Override
    public void onDetach() {
        mSpiceManager.shouldStop();

        if (mLoadingDialog!=null && mLoadingDialog.isShowing()){
            mLoadingDialog.dismiss();
            mLoadingDialog=null;
        }

        super.onDetach();
    }

    public String getCurrentIsp(){
        return mIspData.get(mLastIspPos).getDisplayText();
    }

    public String getCurrentProvince(){
        return mProvinceData.get(mLastProvincePos).getDisplayText();
    }

    public String getCurrentCity(){
        return mCityData.get(mLastCityPos).getDisplayText();
    }

    public String getCurrentDNS(){
        return mDNSData.get(mLastDNSPos).getDisplayText();
    }

    public void setObserver(DNSObserver observer) {
        this.mObserver = observer;
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreWheelPos();
        prepareWheelsData();
    }

    @Override
    public void onPause() {
        super.onPause();
        storeWheelPos();
    }

    private void prepareWheelsData() {
        if (DNSApplication.ispDataIsReady(getActivity())){
            updateData();
            updateUI();
        }else{
            showLoadingDialog();
            loadDataFromNet();
        }
    }

    private void updateData() {
        List<ISP> isps = DNSApplication.getDaoSession().getISPDao().loadAll();
        reput(mIspData,isps);
        List<Province> provinces = isps.get(mLastIspPos).getProvinces();
        reput(mProvinceData,provinces);
        List<City> cities = provinces.get(mLastProvincePos).getCities();
        reput(mCityData,cities);
        List<DNS> dnsList = cities.get(mLastCityPos).getDnsList();
        reput(mDNSData,dnsList);
    }

    private <T> void reput(List<T> container,List<T> temp){
        Log.i(TAG,temp.toString());
        container.clear();
        container.addAll(temp);
    }

    private void updateUI() {
        updateWheel(mIspWheel,mIspData,mLastIspPos);
        updateWheel(mProvinceWheel,mProvinceData,mLastProvincePos);
        updateWheel(mCityWheel,mCityData,mLastCityPos);
        updateWheel(mDnsWheel,mDNSData,mLastDNSPos);
        mObserver.onDnsValueChanged(getCurrentDNS());
    }

    private <T extends DisplayText> void updateWheel(WheelView wheel, List<T> wheelData, int lastPos){
       wheel.setViewAdapter(new TextWheelAdapter<T>(getActivity(),wheelData));
       wheel.setCurrentItem(lastPos);
    }

    private void loadDataFromNet() {
        mSpiceManager.execute(new IspRequest(getActivity()),new RequestListener<Void>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                DNSApplication.setISPDataReady(getActivity(),false);
                mLoadingDialog.dismiss();
            }
            @Override
            public void onRequestSuccess(Void aVoid) {
                DNSApplication.setISPDataReady(getActivity(),true);
                mLoadingDialog.dismiss();
                prepareWheelsData();
            }
        });
    }

    private void showLoadingDialog() {
        if (mLoadingDialog==null){
            final Context context=getActivity();
            final String title = context.getString(R.string.dialog_title);
            final String message = context.getString(R.string.dialog_message);
            mLoadingDialog = ProgressDialog.show(context, title, message, true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (!DNSApplication.ispDataIsReady(context)){
                        System.exit(0);
                    }
                }
            });
        }
        mLoadingDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_wheels, container, false);

        mIspWheel = (WheelView) root.findViewById(R.id.ispWheel);
        mIspWheel.addScrollingListener(mIspWheelScrollListener);
        mIspWheel.addChangingListener(mIspWheelChangedListener);

        mProvinceWheel = (WheelView) root.findViewById(R.id.provinceWheel);
        mProvinceWheel.addScrollingListener(mProvinceWheelScrollListener);
        mProvinceWheel.addChangingListener(mProvinceWheelChangedListener);

        mCityWheel = (WheelView) root.findViewById(R.id.cityWheel);
        mCityWheel.addScrollingListener(mCityWheelScrollListener);
        mCityWheel.addChangingListener(mCityWheelChangedListener);

        mDnsWheel = (WheelView) root.findViewById(R.id.dnsWheel);
        mDnsWheel.addChangingListener(mDNSWheelChangedListener);
        return root;
    }

    private class TextWheelAdapter<T extends DisplayText> extends AbstractWheelTextAdapter{

        private List<? extends DisplayText> mData;

        protected TextWheelAdapter(Context context,List<? extends DisplayText> data) {
            super(context);
            setItemResource(R.layout.item_wheels);
            setItemTextResource(NO_RESOURCE);
            mData=data;
        }

        @Override
        protected CharSequence getItemText(int index) {
            DisplayText displayText = mData.get(index);
            return displayText.getDisplayText();
        }

        @Override
        public int getItemsCount() {
            return mData.size();
        }
    }


    public void storeWheelPos(){
        int ispPos = mIspWheel.getCurrentItem();
        int provincePos = mProvinceWheel.getCurrentItem();
        int cityPos = mCityWheel.getCurrentItem();
        int dnsPos = mDnsWheel.getCurrentItem();
        SharedPreferences.Editor edit = getActivity().getSharedPreferences(WHEEL_CONFIG, Context.MODE_PRIVATE).edit();
        edit.putInt(ISP_POS,ispPos);
        edit.putInt(PROVINCE_POS,provincePos);
        edit.putInt(CITY_POS,cityPos);
        edit.putInt(DNS_POS,dnsPos);
        edit.commit();
    }

    public void restoreWheelPos(){
        SharedPreferences sp = getActivity().getSharedPreferences(WHEEL_CONFIG, Context.MODE_PRIVATE);
        mLastIspPos=sp.getInt(ISP_POS,0);
        mLastProvincePos=sp.getInt(PROVINCE_POS,0);
        mLastCityPos=sp.getInt(CITY_POS,0);
        mLastDNSPos=sp.getInt(DNS_POS,0);
    }

    private OnWheelScrollListener mIspWheelScrollListener=new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {
            mIspScrolling=true;
            mProvinceWheel.stopScrolling();
            mCityWheel.stopScrolling();
            mDnsWheel.stopScrolling();
        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            mIspScrolling=false;
            mLastIspPos=wheel.getCurrentItem();
            mLastProvincePos=0;
            mLastCityPos=0;
            mLastDNSPos=0;
            prepareWheelsData();
        }
    };

    private OnWheelChangedListener mIspWheelChangedListener=new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (!mIspScrolling){
                mLastIspPos=newValue;
                mLastProvincePos=0;
                mLastCityPos=0;
                mLastDNSPos=0;
                prepareWheelsData();
            }
        }
    };
    private OnWheelScrollListener mProvinceWheelScrollListener=new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {
            mProvinceScrolling=true;
            mCityWheel.stopScrolling();
            mDnsWheel.stopScrolling();
        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            mProvinceScrolling=false;
            if (!mIspScrolling){
                mLastProvincePos=wheel.getCurrentItem();
                mLastCityPos=0;
                mLastDNSPos=0;
                prepareWheelsData();
            }
        }
    };

    private OnWheelChangedListener mProvinceWheelChangedListener=new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (!mIspScrolling&&!mProvinceScrolling){
                mLastProvincePos=newValue;
                mLastCityPos=0;
                mLastDNSPos=0;
                prepareWheelsData();
            }
        }
    };

    private OnWheelScrollListener mCityWheelScrollListener=new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {
            mCityScrolling=true;
            mDnsWheel.stopScrolling();
        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            mCityScrolling=false;
            if (!mIspScrolling&&!mProvinceScrolling){
                mLastCityPos=wheel.getCurrentItem();
                mLastDNSPos=0;
                prepareWheelsData();
            }
        }
    };

    private OnWheelChangedListener mCityWheelChangedListener=new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (!mIspScrolling&&!mProvinceScrolling&&!mCityScrolling){
                mLastCityPos=newValue;
                mLastDNSPos=0;
                prepareWheelsData();
            }
        }
    };

    private OnWheelChangedListener mDNSWheelChangedListener=new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            mLastDNSPos=newValue;
        }
    };

}
