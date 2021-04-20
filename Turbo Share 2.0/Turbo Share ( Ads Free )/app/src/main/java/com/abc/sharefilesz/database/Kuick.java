

package com.abc.sharefilesz.database;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.migration.db.Migration;
import com.abc.sharefilesz.util.AppUtils;
import com.genonbeta.android.database.*;
import com.genonbeta.android.database.SQLValues.Column;

import java.util.List;

/**

 * Date: 4/14/17 11:47 PM
 */

public class Kuick extends KuickDb
{
    public static final int DATABASE_VERSION = 13;

    public static final String
            TAG = Kuick.class.getSimpleName(),
            DATABASE_NAME = Kuick.class.getSimpleName() + ".db";

    public static final String TABLE_CLIPBOARD = "clipboard",
            FIELD_CLIPBOARD_ID = "id",
            FIELD_CLIPBOARD_TEXT = "text",
            FIELD_CLIPBOARD_TIME = "time";

    public static final String TABLE_DEVICES = "devices",
            FIELD_DEVICES_ID = "deviceId",
            FIELD_DEVICES_USER = "user",
            FIELD_DEVICES_BRAND = "brand",
            FIELD_DEVICES_MODEL = "model",
            FIELD_DEVICES_BUILDNAME = "buildName",
            FIELD_DEVICES_BUILDNUMBER = "buildNumber",
            FIELD_DEVICES_CLIENTVERSION = "clientVersion",
            FIELD_DEVICES_LASTUSAGETIME = "lastUsedTime",
            FIELD_DEVICES_ISRESTRICTED = "isRestricted",
            FIELD_DEVICES_ISTRUSTED = "isTrusted",
            FIELD_DEVICES_ISLOCALADDRESS = "isLocalAddress",
            FIELD_DEVICES_SECUREKEY = "tmpSecureKey",
            FIELD_DEVICES_TYPE = "type";

    public static final String TABLE_DEVICECONNECTION = "deviceConnection",
            FIELD_DEVICECONNECTION_IPADDRESS = "ipAddress",
            FIELD_DEVICECONNECTION_DEVICEID = "deviceId",
            FIELD_DEVICECONNECTION_ADAPTERNAME = "adapterName",
            FIELD_DEVICECONNECTION_LASTCHECKEDDATE = "lastCheckedDate";

    public static final String TABLE_FILEBOOKMARK = "fileBookmark",
            FIELD_FILEBOOKMARK_TITLE = "title",
            FIELD_FILEBOOKMARK_PATH = "path";

    public static final String TABLE_TRANSFERASSIGNEE = "transferAssignee",
            FIELD_TRANSFERASSIGNEE_GROUPID = "groupId",
            FIELD_TRANSFERASSIGNEE_DEVICEID = "deviceId",
            FIELD_TRANSFERASSIGNEE_CONNECTIONADAPTER = "connectionAdapter",
            FIELD_TRANSFERASSIGNEE_TYPE = "type";

    public static final String TABLE_TRANSFER = "transfer",
            FIELD_TRANSFER_ID = "id",
            FIELD_TRANSFER_NAME = "name",
            FIELD_TRANSFER_SIZE = "size",
            FIELD_TRANSFER_MIME = "mime",
            FIELD_TRANSFER_TYPE = "type",
            FIELD_TRANSFER_GROUPID = "groupId",
            FIELD_TRANSFER_FILE = "file",
            FIELD_TRANSFER_DIRECTORY = "directory",
            FIELD_TRANSFER_LASTCHANGETIME = "lastAccessTime",
            FIELD_TRANSFER_FLAG = "flag";

    public static final String TABLE_TRANSFERGROUP = "transferGroup",
            FIELD_TRANSFERGROUP_ID = "id",
            FIELD_TRANSFERGROUP_SAVEPATH = "savePath",
            FIELD_TRANSFERGROUP_DATECREATED = "dateCreated",
            FIELD_TRANSFERGROUP_ISSHAREDONWEB = "isSharedOnWeb",
            FIELD_TRANSFERGROUP_ISPAUSED = "isPaused";

    public Kuick(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        SQLQuery.createTables(db, tables());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old, int current)
    {
        Migration.migrate(this, db, old, current);
    }

    public static SQLValues tables()
    {
        SQLValues values = new SQLValues();

        values.defineTable(TABLE_CLIPBOARD)
                .define(new Column(FIELD_CLIPBOARD_ID, SQLType.INTEGER, false))
                .define(new Column(FIELD_CLIPBOARD_TEXT, SQLType.TEXT, false))
                .define(new Column(FIELD_CLIPBOARD_TIME, SQLType.LONG, false));

        values.defineTable(TABLE_DEVICES)
                .define(new Column(FIELD_DEVICES_ID, SQLType.TEXT, false))
                .define(new Column(FIELD_DEVICES_USER, SQLType.TEXT, false))
                .define(new Column(FIELD_DEVICES_BRAND, SQLType.TEXT, false))
                .define(new Column(FIELD_DEVICES_MODEL, SQLType.TEXT, false))
                .define(new Column(FIELD_DEVICES_BUILDNAME, SQLType.TEXT, false))
                .define(new Column(FIELD_DEVICES_BUILDNUMBER, SQLType.INTEGER, false))
                .define(new Column(FIELD_DEVICES_CLIENTVERSION, SQLType.INTEGER, false))
                .define(new Column(FIELD_DEVICES_LASTUSAGETIME, SQLType.INTEGER, false))
                .define(new Column(FIELD_DEVICES_ISRESTRICTED, SQLType.INTEGER, false))
                .define(new Column(FIELD_DEVICES_ISTRUSTED, SQLType.INTEGER, false))
                .define(new Column(FIELD_DEVICES_ISLOCALADDRESS, SQLType.INTEGER, false))
                .define(new Column(FIELD_DEVICES_SECUREKEY, SQLType.INTEGER, true))
                .define(new Column(FIELD_DEVICES_TYPE, SQLType.TEXT, false));

        values.defineTable(TABLE_DEVICECONNECTION)
                .define(new Column(FIELD_DEVICECONNECTION_IPADDRESS, SQLType.TEXT, false))
                .define(new Column(FIELD_DEVICECONNECTION_DEVICEID, SQLType.TEXT, false))
                .define(new Column(FIELD_DEVICECONNECTION_ADAPTERNAME, SQLType.TEXT, false))
                .define(new Column(FIELD_DEVICECONNECTION_LASTCHECKEDDATE, SQLType.INTEGER, false));

        values.defineTable(TABLE_FILEBOOKMARK)
                .define(new Column(FIELD_FILEBOOKMARK_TITLE, SQLType.TEXT, false))
                .define(new Column(FIELD_FILEBOOKMARK_PATH, SQLType.TEXT, false));

        values.defineTable(TABLE_TRANSFER)
                .define(new Column(FIELD_TRANSFER_ID, SQLType.LONG, false))
                .define(new Column(FIELD_TRANSFER_GROUPID, SQLType.LONG, false))
                .define(new Column(FIELD_TRANSFER_DIRECTORY, SQLType.TEXT, true))
                .define(new Column(FIELD_TRANSFER_FILE, SQLType.TEXT, false))
                .define(new Column(FIELD_TRANSFER_NAME, SQLType.TEXT, false))
                .define(new Column(FIELD_TRANSFER_SIZE, SQLType.INTEGER, false))
                .define(new Column(FIELD_TRANSFER_MIME, SQLType.TEXT, false))
                .define(new Column(FIELD_TRANSFER_TYPE, SQLType.TEXT, false))
                .define(new Column(FIELD_TRANSFER_FLAG, SQLType.TEXT, false))
                .define(new Column(FIELD_TRANSFER_LASTCHANGETIME, SQLType.LONG, false));

        values.defineTable(TABLE_TRANSFERASSIGNEE)
                .define(new Column(FIELD_TRANSFERASSIGNEE_GROUPID, SQLType.LONG, false))
                .define(new Column(FIELD_TRANSFERASSIGNEE_DEVICEID, SQLType.TEXT, false))
                .define(new Column(FIELD_TRANSFERASSIGNEE_CONNECTIONADAPTER, SQLType.TEXT, false))
                .define(new Column(FIELD_TRANSFERASSIGNEE_TYPE, SQLType.TEXT, false));

        values.defineTable(TABLE_TRANSFERGROUP)
                .define(new Column(FIELD_TRANSFERGROUP_ID, SQLType.LONG, false))
                .define(new Column(FIELD_TRANSFERGROUP_DATECREATED, SQLType.LONG, false))
                .define(new Column(FIELD_TRANSFERGROUP_SAVEPATH, SQLType.TEXT, true))
                .define(new Column(FIELD_TRANSFERGROUP_ISSHAREDONWEB, SQLType.INTEGER, true))
                .define(new Column(FIELD_TRANSFERGROUP_ISPAUSED, SQLType.INTEGER, false));

        return values;
    }

    public <T, V extends DatabaseObject<T>> void removeAsynchronous(Activity activity, V object, T parent)
    {
        BackgroundService.run(activity, new SingleRemovalTask<>(activity, getWritableDatabase(), object, parent));
    }

    public <T, V extends DatabaseObject<T>> void removeAsynchronous(Activity activity, List<V> objects, T parent)
    {
        BackgroundService.run(activity, new MultipleRemovalTask<>(activity, getWritableDatabase(), objects, parent));
    }

    private static abstract class BgTaskImpl extends BackgroundTask
    {
        private SQLiteDatabase mDb;
        private String mTitle;
        private String mDescription;

        BgTaskImpl(Context context, int titleRes, int descriptionRes, SQLiteDatabase db)
        {
            mTitle = context.getString(titleRes);
            mDescription = context.getString(descriptionRes);
            mDb = db;
        }

        @Override
        protected void onProgressChange(Progress progress)
        {
            super.onProgressChange(progress);
            setOngoingContent(getService().getString(R.string.text_transferStatusFiles, progress.getCurrent(),
                    progress.getTotal()));
        }

        public SQLiteDatabase getDb()
        {
            return mDb;
        }

        @Override
        public String getDescription()
        {
            return mDescription;
        }

        @Override
        public String getTitle()
        {
            return mTitle;
        }
    }

    private static class SingleRemovalTask<T, V extends DatabaseObject<T>> extends BgTaskImpl
    {
        private V mObject;
        private T mParent;

        SingleRemovalTask(Context context, SQLiteDatabase db, V object, T parent)
        {
            super(context, R.string.mesg_removing, R.string.mesg_mayTakeLong, db);
            mObject = object;
            mParent = parent;
        }

        @Override
        protected void onRun() throws InterruptedException
        {
            Kuick kuick = AppUtils.getKuick(getService());

            kuick.remove(getDb(), mObject, mParent, progressListener());
            kuick.broadcast();
        }
    }

    private static class MultipleRemovalTask<T, V extends DatabaseObject<T>> extends BgTaskImpl
    {
        private List<V> mObjectList;
        private T mParent;

        MultipleRemovalTask(Context context, SQLiteDatabase db, List<V> objectList, T parent)
        {
            super(context, R.string.mesg_removing, R.string.mesg_mayTakeLong, db);
            mObjectList = objectList;
            mParent = parent;
        }

        @Override
        protected void onRun() throws InterruptedException
        {
            Kuick kuick = AppUtils.getKuick(getService());

            kuick.remove(getDb(), mObjectList, mParent, progressListener());
            kuick.broadcast();
        }
    }
}