package com.tgh.dnswizard;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.tgh.dnswizard.bean.DaoMaster;
import com.tgh.dnswizard.bean.DaoSession;

/**
 * Created with IntelliJ IDEA.
 * User: emellend
 * Date: 13-7-5
 * Time: 下午4:00
 * To change this template use File | Settings | File Templates.
 */
public class DNSApplication extends Application {
    private static DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        initDaoSession();
    }

    private void initDaoSession() {
        DaoMaster.DevOpenHelper helper=new DaoMaster.DevOpenHelper(this,"dnswizard.db",null);
        DaoMaster master=new DaoMaster(helper.getWritableDatabase());
        daoSession = master.newSession();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        daoSession.getDatabase().close();
        daoSession=null;
    }

    public static DaoSession getDaoSession(){
        return daoSession;
    }

    private static final String CONFIG = "DNS_Config";
    private static final String ISP_DATA="ispData";
    private static final String ISP_UPDATE="ispUpdate";

    /**
     * 设置ISP数据是否需要升级
     * @param context
     * @param update
     */
    public static void setISPUpdateState(Context context,boolean update) {
        SharedPreferences.Editor edit = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).edit();
        edit.putBoolean(ISP_UPDATE,update);
        edit.commit();
    }

    /**
     * 查询ISP是否需要升级
     * @param context
     * @return
     */
    public static boolean ispNeedUpdate(Context context){
        SharedPreferences sp=context.getSharedPreferences(CONFIG,Context.MODE_PRIVATE);
        return sp.getBoolean(ISP_UPDATE,true);
    }

    /**
     * 设置ISP数据是否可用
     * @param context
     * @param ready
     * @return
     */
    public static void setISPDataReady(Context context,boolean ready){
        SharedPreferences.Editor edit = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).edit();
        edit.putBoolean(ISP_DATA,ready);
        edit.commit();
    }

    /**
     * 查询ISP数据是否可用
     * @param context
     * @return
     */
    public static boolean ispDataIsReady(Context context){
        SharedPreferences sp=context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        return sp.getBoolean(ISP_DATA,false);
    }
}
