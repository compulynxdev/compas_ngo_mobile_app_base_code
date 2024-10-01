package com.compastbc.core.data.db;

import android.content.Context;

import com.compastbc.core.data.db.model.DaoMaster;
import com.compastbc.core.data.db.model.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Created by Hemant Sharma on 08-10-19.
 * Divergent software labs pvt. ltd
 */
public class AppDBHelper implements DBHelper {
    private static AppDBHelper ourInstance;
    private final DaoSession daoSession;

    private AppDBHelper(Context context) {
        // regular SQLite database
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "CompasTbcOnline");
        Database db = helper.getWritableDb();

        // encrypted SQLCipher database
        // note: you need to add SQLCipher to your dependencies, check the build.gradle file
        // DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db-encrypted");
        // Database db = helper.getEncryptedWritableDb("encryption-key");
        daoSession = new DaoMaster(db).newSession();
    }

    public static AppDBHelper getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new AppDBHelper(context);
        }

        return ourInstance;
    }

    @Override
    public DaoSession getDaoSession() {
        return daoSession;
    }

}
