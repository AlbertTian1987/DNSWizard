package com.tgh.dnswizard;

import android.app.Application;
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
}
