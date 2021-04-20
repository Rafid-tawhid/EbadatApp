package com.abc.sharefilesz.service;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.object.Identity;
import com.abc.sharefilesz.object.IndexOfTransferGroup;
import com.abc.sharefilesz.object.TextStreamObject;
import com.abc.sharefilesz.object.TransferAssignee;
import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.CommunicationBridge;
import com.abc.sharefilesz.util.HotspotManager;
import com.abc.sharefilesz.util.NetworkDeviceLoader;
import com.abc.sharefilesz.util.NotificationHelper;
import com.abc.sharefilesz.util.NotificationUtils;
import com.abc.sharefilesz.util.NsdDiscovery;
import com.abc.sharefilesz.util.UpdateUtils;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Service;
import com.abc.sharefilesz.config.AppConfig;
import com.abc.sharefilesz.config.Keyword;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.*;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.task.FileTransferTask;
import com.abc.sharefilesz.task.IndexTransferTask;
import com.abc.sharefilesz.util.*;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.exception.ReconstructionFailedException;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONException;
import org.json.JSONObject;
import org.monora.coolsocket.core.CoolSocket;
import org.monora.coolsocket.core.response.Response;
import org.monora.coolsocket.core.session.ActiveConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class BackgroundService extends Service
{
    public static final String TAG = BackgroundService.class.getSimpleName();

    public static final String
            ACTION_CLIPBOARD = "com.abc.sharefilesz.action.CLIPBOARD",
            ACTION_DEVICE_ACQUAINTANCE = "com.abc.sharefilesz.transaction.action.DEVICE_ACQUAINTANCE",
            ACTION_DEVICE_APPROVAL = "com.abc.sharefilesz.action.DEVICE_APPROVAL",
            ACTION_END_SESSION = "com.abc.sharefilesz.action.END_SESSION",
            ACTION_FILE_TRANSFER = "com.abc.sharefilesz.action.FILE_TRANSFER",
            ACTION_INCOMING_TRANSFER_READY = "com.abc.sharefilesz.transaction.action.INCOMING_TRANSFER_READY",
            ACTION_KILL_SIGNAL = "com.genonbeta.intent.action.KILL_SIGNAL",
            ACTION_PIN_USED = "com.abc.sharefilesz.transaction.action.PIN_USED",
            ACTION_START_TRANSFER = "com.genonbeta.intent.action.START_TRANSFER",
            ACTION_STOP_TASK = "com.abc.sharefilesz.transaction.action.CANCEL_JOB",
            ACTION_TASK_CHANGE = "com.abc.sharefilesz.transaction.action.TASK_STATUS_CHANGE", // FIXME: only the parent activity should listen to this
            EXTRA_CLIPBOARD_ACCEPTED = "extraClipboardAccepted",
            EXTRA_CLIPBOARD_ID = "extraTextId",
            EXTRA_CONNECTION = "extraConnectionAdapterName",
            EXTRA_DEVICE = "extraDevice",
            EXTRA_DEVICE_PIN = "extraDevicePin",
            EXTRA_GROUP = "extraGroup",
            EXTRA_IDENTITY = "extraIdentity",
            EXTRA_ACCEPTED = "extraAccepted",
            EXTRA_REQUEST_ID = "extraRequest",
            EXTRA_TRANSFER_TYPE = "extraTransferType";

    private final List<BackgroundTask> mTaskList = new ArrayList<>();
    private CommunicationServer mCommunicationServer = new CommunicationServer();
    private WebShareServer mWebShareServer;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(10);
    private NsdDiscovery mNsdDiscovery;
    private NotificationHelper mNotificationHelper;
    private WifiManager.WifiLock mWifiLock;
    private MediaScannerConnection mMediaScanner;
    private HotspotManager mHotspotManager;
    private LocalBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        WifiManager wifiManager = ((WifiManager) getApplicationContext().getSystemService(Service.WIFI_SERVICE));

        mWebShareServer = new WebShareServer(this, AppConfig.SERVER_PORT_WEBSHARE);
        mNotificationHelper = new NotificationHelper(getNotificationUtils());
        mNsdDiscovery = new NsdDiscovery(getApplicationContext(), getKuick(), getDefaultPreferences());
        mMediaScanner = new MediaScannerConnection(this, null);
        mHotspotManager = HotspotManager.newInstance(this);

        if (wifiManager != null)
            mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG);

        mMediaScanner.connect();
        mNsdDiscovery.registerService();

        if (mWifiLock != null)
            mWifiLock.acquire();

        tryStartingServices();
        takeForeground(true);
    }

    private void takeForeground(boolean take)
    {
        if (take)
            startForeground(NotificationHelper.ID_BG_SERVICE, getNotificationHelper().getForegroundNotification().build());
        else
            stopForeground(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId)
    {
        super.onStartCommand(intent, flags, startId);

        if (intent != null)
            Log.d(TAG, "onStart() : action = " + intent.getAction());

        if (intent != null && AppUtils.checkRunningConditions(this)) {
            if (ACTION_FILE_TRANSFER.equals(intent.getAction())) {
                Device device = intent.getParcelableExtra(EXTRA_DEVICE);
                TransferGroup group = intent.getParcelableExtra(EXTRA_GROUP);
                final int notificationId = intent.getIntExtra(NotificationUtils.EXTRA_NOTIFICATION_ID, -1);
                final boolean isAccepted = intent.getBooleanExtra(EXTRA_ACCEPTED, false);

                getNotificationHelper().getUtils().cancel(notificationId);

                try {
                    if (device == null || group == null)
                        throw new Exception("The device or group instance is broken");

                    FileTransferTask task = FileTransferTask.createFrom(getKuick(), group, device,
                            TransferObject.Type.INCOMING);

                    CommunicationBridge.connect(getKuick(), client -> {
                        try {
                            ActiveConnection activeConnection = client.communicate(device, task.connection);

                            activeConnection.reply(new JSONObject()
                                    .put(Keyword.REQUEST, Keyword.REQUEST_RESPONSE)
                                    .put(Keyword.TRANSFER_GROUP_ID, group.id)
                                    .put(Keyword.TRANSFER_IS_ACCEPTED, isAccepted)
                                    .toString());

                            activeConnection.receive();
                            activeConnection.getSocket().close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    if (isAccepted)
                        run(task);
                    else {
                        getKuick().remove(getKuick().getWritableDatabase(), task.assignee, task.group, null);
                        getKuick().broadcast();
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    if (isAccepted)
                        getNotificationHelper().showToast(R.string.mesg_somethingWentWrong);
                }
            } else if (ACTION_DEVICE_APPROVAL.equals(intent.getAction())) {
                Device device = intent.getParcelableExtra(EXTRA_DEVICE);
                boolean accepted = intent.getBooleanExtra(EXTRA_ACCEPTED, false);
                int notificationId = intent.getIntExtra(NotificationUtils.EXTRA_NOTIFICATION_ID, -1);
                int suggestedPin = intent.getIntExtra(EXTRA_DEVICE_PIN, -1);

                getNotificationHelper().getUtils().cancel(notificationId);

                if (device != null) {
                    device.isRestricted = !accepted;

                    if (accepted)
                        device.secureKey = suggestedPin;

                    getKuick().update(device);
                    getKuick().broadcast();
                }
            } else if (ACTION_CLIPBOARD.equals(intent.getAction()) && intent.hasExtra(EXTRA_CLIPBOARD_ACCEPTED)) {
                int notificationId = intent.getIntExtra(NotificationUtils.EXTRA_NOTIFICATION_ID, -1);
                long clipboardId = intent.getLongExtra(EXTRA_CLIPBOARD_ID, -1);
                boolean isAccepted = intent.getBooleanExtra(EXTRA_CLIPBOARD_ACCEPTED, false);
                TextStreamObject textStreamObject = new TextStreamObject(clipboardId);

                getNotificationHelper().getUtils().cancel(notificationId);

                try {
                    getKuick().reconstruct(textStreamObject);

                    if (isAccepted) {
                        ClipboardManager cbManager = ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE));

                        if (cbManager != null) {
                            cbManager.setPrimaryClip(ClipData.newPlainText("receivedText", textStreamObject.text));
                            Toast.makeText(this, R.string.mesg_textCopiedToClipboard, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ACTION_END_SESSION.equals(intent.getAction())) {
                stopSelf();
            } else if (ACTION_START_TRANSFER.equals(intent.getAction()) && intent.hasExtra(EXTRA_GROUP)
                    && intent.hasExtra(EXTRA_DEVICE) && intent.hasExtra(EXTRA_TRANSFER_TYPE)) {
                Device device = intent.getParcelableExtra(EXTRA_DEVICE);
                TransferGroup group = intent.getParcelableExtra(EXTRA_GROUP);
                TransferObject.Type type = (TransferObject.Type) intent.getSerializableExtra(EXTRA_TRANSFER_TYPE);

                try {
                    if (device == null || group == null || type == null)
                        throw new Exception();

                    FileTransferTask task = (FileTransferTask) findTaskBy(FileTransferTask.identifyWith(group.id,
                            device.id, type));

                    if (task == null)
                        run(FileTransferTask.createFrom(getKuick(), group, device, type));
                    else
                        Toast.makeText(this, getString(R.string.mesg_groupOngoingNotice, task.object.name),
                                Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ACTION_STOP_TASK.equals(intent.getAction()) && intent.hasExtra(EXTRA_IDENTITY)) {
                int notificationId = intent.getIntExtra(NotificationUtils.EXTRA_NOTIFICATION_ID, -1);
                Identity identity = intent.getParcelableExtra(EXTRA_IDENTITY);

                try {
                    BackgroundTask task = findTaskBy(identity);

                    if (task == null) {
                        getNotificationHelper().getUtils().cancel(notificationId);
                    } else {
                        // FIXME: 16.03.2020 Should we use this notification?
                        //task.notification = getNotificationHelper().notifyStuckThread(task);

                        if (task.isInterrupted())
                            task.forceQuit();
                        else
                            task.interrupt(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        takeForeground(false);

        try {
            mCommunicationServer.stop();
        } catch (InterruptedException ignored) {
        }

        mMediaScanner.disconnect();
        mNsdDiscovery.unregisterService();
        mWebShareServer.stop();

        {
            ContentValues values = new ContentValues();
            values.put(Kuick.FIELD_TRANSFERGROUP_ISSHAREDONWEB, 0);
            getKuick().update(new SQLQuery.Select(Kuick.TABLE_TRANSFERGROUP)
                    .setWhere(String.format("%s = ?", Kuick.FIELD_TRANSFERGROUP_ISSHAREDONWEB),
                            String.valueOf(1)), values);
        }

        if (getHotspotUtils().unloadPreviousConfig())
            Log.d(TAG, "onDestroy: Stopping hotspot (previously started)=" + getHotspotUtils().disable());

        if (getWifiLock() != null && getWifiLock().isHeld()) {
            getWifiLock().release();
            Log.d(TAG, "onDestroy: Releasing Wi-Fi lock");
        }

        stopForeground(true);

        synchronized (mTaskList) {
            for (BackgroundTask task : mTaskList) {
                task.interrupt(false);
                Log.d(TAG, "onDestroy(): Ongoing indexing stopped: " + task.getTitle());
            }
        }

        AppUtils.generateNetworkPin(this);
        getKuick().broadcast();
    }

    public void attach(BackgroundTask task)
    {
        runInternal(task);
    }

    public boolean canStopService()
    {
        return getTaskList().size() > 0 || mHotspotManager.isStarted() || mWebShareServer.hadClients();
    }

    @Nullable
    public BackgroundTask findTaskBy(Identity identity)
    {
        List<BackgroundTask> taskList = findTasksBy(identity);
        return taskList.size() > 0 ? taskList.get(0) : null;
    }

    @NonNull
    public synchronized List<BackgroundTask> findTasksBy(Identity identity)
    {
        synchronized (mTaskList) {
            return findTasksBy(mTaskList, identity);
        }
    }

    public static <T extends BackgroundTask> List<T> findTasksBy(List<T> taskList, Identity identity)
    {
        List<T> foundList = new ArrayList<>();
        for (T task : taskList)
            if (task.getIdentity().equals(identity))
                foundList.add(task);
        return foundList;
    }

    private HotspotManager getHotspotUtils()
    {
        return mHotspotManager;
    }

    public WifiConfiguration getHotspotConfig()
    {
        return getHotspotUtils().getConfiguration();
    }

    public MediaScannerConnection getMediaScanner()
    {
        return mMediaScanner;
    }

    public NotificationHelper getNotificationHelper()
    {
        return mNotificationHelper;
    }

    private ExecutorService getSelfExecutor()
    {
        return mExecutor;
    }

    public List<BackgroundTask> getTaskList()
    {
        return mTaskList;
    }

    public <T extends BackgroundTask> List<T> getTaskListOf(Class<T> clazz)
    {
        synchronized (mTaskList) {
            return getTaskListOf(mTaskList, clazz);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends BackgroundTask> List<T> getTaskListOf(List<? extends BackgroundTask> taskList,
                                                                   Class<T> clazz)
    {
        List<T> foundList = new ArrayList<>();
        for (BackgroundTask task : taskList)
            if (clazz.isInstance(task))
                foundList.add((T) task);
        return foundList;
    }

    private WifiManager.WifiLock getWifiLock()
    {
        return mWifiLock;
    }

    public static int hashIntent(@NonNull Intent intent)
    {
        StringBuilder builder = new StringBuilder()
                .append(intent.getComponent())
                .append(intent.getData())
                .append(intent.getPackage())
                .append(intent.getAction())
                .append(intent.getFlags())
                .append(intent.getType());

        if (intent.getExtras() != null)
            builder.append(intent.getExtras().toString());

        return builder.toString().hashCode();
    }

    public boolean hasTaskOf(Class<? extends BackgroundTask> clazz)
    {
        synchronized (mTaskList) {
            return hasTaskOf(mTaskList, clazz);
        }
    }

    public static boolean hasTaskOf(List<? extends BackgroundTask> taskList, Class<? extends BackgroundTask> clazz)
    {
        for (BackgroundTask task : taskList)
            if (clazz.isInstance(task))
                return true;
        return false;
    }

    public static boolean hasTaskWith(List<? extends BackgroundTask> taskList, Identity identity)
    {
        for (BackgroundTask task : taskList)
            if (task.getIdentity().equals(identity))
                return true;
        return false;
    }

    public void interruptTasksBy(Identity identity, boolean userAction)
    {
        synchronized (mTaskList) {
            for (BackgroundTask task : findTasksBy(identity))
                task.interrupt(userAction);
        }
    }

    private boolean isProcessRunning(long groupId, String deviceId, TransferObject.Type type)
    {
        return findTaskBy(FileTransferTask.identifyWith(groupId, deviceId, type)) != null;
    }

    protected synchronized <T extends BackgroundTask> void registerWork(T task)
    {
        synchronized (mTaskList) {
            mTaskList.add(task);
        }

        Log.d(TAG, "registerWork: " + task.getClass().getSimpleName());
        sendBroadcast(new Intent(ACTION_TASK_CHANGE));
    }

    public static <T extends BackgroundTask> void run(Activity activity, T task)
    {
        try {
            AppUtils.getBgService(activity).run(task);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void run(final BackgroundTask runningTask)
    {
        mExecutor.submit(() -> attach(runningTask));
    }

    private void runInternal(BackgroundTask runningTask)
    {
        registerWork(runningTask);

        try {
            runningTask.run(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        unregisterWork(runningTask);
    }

    public void toggleHotspot()
    {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this))
            return;

        if (getHotspotUtils().isEnabled())
            getHotspotUtils().disable();
        else
            Log.d(TAG, "toggleHotspot: Enabling=" + getHotspotUtils().enableConfigured(AppUtils.getHotspotName(
                    this), null));
    }

    /**
     * Some services like file transfer server, web share portal server involve writing and reading data.
     * So, it is best to avoid starting them when the app doesn't have the right permissions.
     */
    public boolean tryStartingServices()
    {
        Log.d(TAG, "tryStartingServices: Starting...");

        if (mWebShareServer.isAlive() && mCommunicationServer.isListening())
            return true;

        if (!AppUtils.checkRunningConditions(this)) {
            Log.d(TAG, "tryStartingServices: The app doesn't have the satisfactory permissions to start " +
                    "services.");
            return false;
        }


        if (!mCommunicationServer.isListening()) {
            try {
                mCommunicationServer.start();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "tryStartingServices: Cannot start the service=" + mCommunicationServer.isListening());
            }
        }

        try {
            mWebShareServer.setAsyncRunner(new WebShareServer.BoundRunner(
                    Executors.newFixedThreadPool(AppConfig.WEB_SHARE_CONNECTION_MAX)));
            mWebShareServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start Web Share Server");
            return false;
        }

        return true;
    }

    protected synchronized void unregisterWork(BackgroundTask task)
    {
        synchronized (mTaskList) {
            mTaskList.remove(task);
            // FIXME: 20.03.2020 Should we stop the service if there is no task left?
        }

        Log.d(TAG, "unregisterWork: " + task.getClass().getSimpleName());
        sendBroadcast(new Intent(ACTION_TASK_CHANGE));
    }

    class CommunicationServer extends CoolSocket
    {
        CommunicationServer()
        {
            super(AppConfig.SERVER_PORT_COMMUNICATION);
            getConfigFactory().setAcceptTimeout(AppConfig.DEFAULT_SOCKET_TIMEOUT_LARGE);
            getConfigFactory().setReadTimeout(AppConfig.DEFAULT_SOCKET_TIMEOUT);
        }

        @Override
        public void onConnected(final ActiveConnection activeConnection)
        {
            // check if the same address has other connections and limit that to 5
            try {
                JSONObject responseJSON = activeConnection.receive().getAsJson();

                if (isUpdateRequest(activeConnection, responseJSON))
                    return;

                boolean result = false;
                boolean shouldContinue = false;
                boolean handshakeRequired = responseJSON.has(Keyword.HANDSHAKE_REQUIRED) && responseJSON.getBoolean(
                        Keyword.HANDSHAKE_REQUIRED);
                boolean handshakeOnly = responseJSON.has(Keyword.HANDSHAKE_ONLY)
                        && responseJSON.getBoolean(Keyword.HANDSHAKE_ONLY);
                final int activePin = getDefaultPreferences().getInt(Keyword.NETWORK_PIN, -1);
                final boolean hasPin = activePin != -1 && responseJSON.has(Keyword.DEVICE_PIN)
                        && activePin == responseJSON.getInt(Keyword.DEVICE_PIN);

                if (hasPin) // pin is known, should be changed. Warn the listeners.
                    sendBroadcast(new Intent(ACTION_PIN_USED));

                JSONObject replyJSON = new JSONObject();
                AppUtils.applyDeviceToJSON(BackgroundService.this, replyJSON);

                if (handshakeRequired) {
                    pushReply(activeConnection, replyJSON, true);

                    if (handshakeOnly)
                        return;
                }

                Log.d(TAG, "onConnected: hasPin: " + hasPin);
                Device device;

                try {
                    device = NetworkDeviceLoader.loadFrom(getKuick(), responseJSON);
                } catch (JSONException e) {
                    // Deprecated: This is a fallback option to generate device information.
                    // Clients must send the device info with the requests asking no handshake or not only handshake.
                    device = new Device(responseJSON.getString(Keyword.DEVICE_INFO_SERIAL));
                }

                if (device.clientVersion >= 1 && device.secureKey < 0) {
                    // Because the client didn't know whom it was talking to, it did not provide a key that might be
                    // exchanged between us before. Now we are asking for the key. Also, this does not work with
                    // the older client versions.
                    device.secureKey = activeConnection.receive().getAsJson().getInt(Keyword.DEVICE_INFO_KEY);
                    activeConnection.reply(Keyword.STUB);
                }

                try {
                    Device existingInfo = new Device(device.id);
                    getKuick().reconstruct(existingInfo);

                    device.applyPreferences(existingInfo); // apply known preferences

                    boolean keysMatch = existingInfo.secureKey == device.secureKey;
                    boolean needsBlocking = device.clientVersion >= 1 && !keysMatch && !hasPin;

                    // We don't update the device info. Instead, we request a check from the user.
                    // If she or he accepts the request, we update the old key with the new one.
                    if (!existingInfo.isRestricted && needsBlocking) {
                        Log.d(TAG, "onConnected: Notifying a PIN issue. Revoked the access for now.");
                        getNotificationHelper().notifyConnectionRequest(existingInfo, device.secureKey);

                        // Previously, the device had the access rights which should now be revoked, because
                        // the device does not have a matching key or valid PIN.
                        existingInfo.isRestricted = true;
                        getKuick().publish(existingInfo);
                    } else {
                        shouldContinue = true;

                        // The device does not have a matching key, but has a valid PIN. So we accept the new key it
                        // sent us and save it.
                        if (device.clientVersion >= 1 && !keysMatch && hasPin)
                            getKuick().publish(device);
                    }
                } catch (ReconstructionFailedException ignored) {
                    if (device.clientVersion < 1)
                        device = NetworkDeviceLoader.load(true, getKuick(),
                                activeConnection.getAddress().getHostAddress(), null);

                    if (device == null || device.id == null || device.id.length() < 1)
                        throw new Exception("Device is not valid.");

                    device.isTrusted = hasPin;
                    device.isRestricted = !hasPin;

                    getKuick().publish(device);

                    shouldContinue = true; // For the first round, we let the client pass.

                    if (device.isRestricted)
                        getNotificationHelper().notifyConnectionRequest(device, device.secureKey);
                }

                if (handshakeRequired) {
                    responseJSON = activeConnection.receive().getAsJson();
                    replyJSON = new JSONObject();
                }

                DeviceConnection connection = NetworkDeviceLoader.processConnection(getKuick(), device,
                        activeConnection.getAddress().getHostAddress());

                getKuick().broadcast();

                if (!shouldContinue || device.clientVersion < 1)
                    replyJSON.put(Keyword.ERROR, Keyword.ERROR_NOT_ALLOWED);
                else if (responseJSON.has(Keyword.REQUEST)) {
                    switch (responseJSON.getString(Keyword.REQUEST)) {
                        case (Keyword.REQUEST_TRANSFER):
                            if (responseJSON.has(Keyword.FILES_INDEX) && responseJSON.has(Keyword.TRANSFER_GROUP_ID)
                                    && !hasTaskOf(IndexTransferTask.class)) {
                                long groupId = responseJSON.getLong(Keyword.TRANSFER_GROUP_ID);
                                String jsonIndex = responseJSON.getString(Keyword.FILES_INDEX);
                                result = true;

                                run(new IndexTransferTask(groupId, jsonIndex, device, connection, hasPin));
                            }
                            break;
                        case (Keyword.REQUEST_RESPONSE):
                            if (responseJSON.has(Keyword.TRANSFER_GROUP_ID)) {
                                int groupId = responseJSON.getInt(Keyword.TRANSFER_GROUP_ID);
                                boolean isAccepted = responseJSON.getBoolean(Keyword.TRANSFER_IS_ACCEPTED);

                                TransferGroup group = new TransferGroup(groupId);
                                TransferAssignee assignee = new TransferAssignee(group, device,
                                        TransferObject.Type.OUTGOING);

                                try {
                                    getKuick().reconstruct(group);
                                    getKuick().reconstruct(assignee);

                                    if (!isAccepted) {
                                        getKuick().remove(assignee);
                                        getKuick().broadcast();
                                    }

                                    result = true;
                                } catch (Exception ignored) {
                                }
                            }
                            break;
                        case (Keyword.REQUEST_CLIPBOARD):
                            if (responseJSON.has(Keyword.TRANSFER_CLIPBOARD_TEXT)) {
                                TextStreamObject textStreamObject = new TextStreamObject(AppUtils.getUniqueNumber(),
                                        responseJSON.getString(Keyword.TRANSFER_CLIPBOARD_TEXT));

                                getKuick().publish(textStreamObject);
                                getKuick().broadcast();
                                getNotificationHelper().notifyClipboardRequest(device, textStreamObject);

                                result = true;
                            }
                            break;
                        case (Keyword.REQUEST_ACQUAINTANCE):
                            sendBroadcast(new Intent(ACTION_DEVICE_ACQUAINTANCE)
                                    .putExtra(EXTRA_DEVICE, device)
                                    .putExtra(EXTRA_CONNECTION, connection));
                            result = true;
                            break;
                        case (Keyword.REQUEST_HANDSHAKE):
                            result = true;
                            break;
                        case (Keyword.REQUEST_TRANSFER_JOB):
                            if (responseJSON.has(Keyword.TRANSFER_GROUP_ID)) {
                                int groupId = responseJSON.getInt(Keyword.TRANSFER_GROUP_ID);
                                String typeValue = responseJSON.getString(Keyword.TRANSFER_TYPE);

                                try {
                                    TransferObject.Type type = TransferObject.Type.valueOf(typeValue);

                                    // The type is reversed to match our side
                                    if (TransferObject.Type.INCOMING.equals(type))
                                        type = TransferObject.Type.OUTGOING;
                                    else if (TransferObject.Type.OUTGOING.equals(type))
                                        type = TransferObject.Type.INCOMING;

                                    TransferGroup group = new TransferGroup(groupId);
                                    getKuick().reconstruct(group);

                                    Log.d(BackgroundService.TAG, "CommunicationServer.onConnected(): "
                                            + "groupId=" + groupId + " typeValue=" + typeValue);

                                    if (!isProcessRunning(groupId, device.id, type)) {
                                        FileTransferTask task = new FileTransferTask();
                                        task.activeConnection = activeConnection;
                                        task.group = group;
                                        task.device = device;
                                        task.type = type;
                                        task.assignee = new TransferAssignee(group, device, type);
                                        task.index = new IndexOfTransferGroup(group);

                                        getKuick().reconstruct(task.assignee);

                                        if (TransferObject.Type.OUTGOING.equals(type)) {
                                            Log.d(TAG, "onConnected: Informing before starting to send.");

                                            pushReply(activeConnection, new JSONObject(), true);
                                            attach(task);

                                            result = true;
                                        } else if (TransferObject.Type.INCOMING.equals(type)) {
                                            JSONObject currentReply = new JSONObject();
                                            result = device.isTrusted;

                                            if (!result)
                                                currentReply.put(Keyword.ERROR, Keyword.ERROR_NOT_TRUSTED);

                                            pushReply(activeConnection, currentReply, result);
                                            Log.d(TAG, "onConnected: Replied: " + currentReply.toString());
                                            Log.d(TAG, "onConnected: " + activeConnection.receive().getAsString());

                                            if (result)
                                                attach(task);

                                            Log.d(TAG, "onConnected: " + activeConnection.receive().getAsString());
                                        }
                                    } else
                                        responseJSON.put(Keyword.ERROR, Keyword.ERROR_NOT_ACCESSIBLE);
                                } catch (Exception e) {
                                    responseJSON.put(Keyword.ERROR, Keyword.ERROR_NOT_FOUND);
                                }
                            }
                            break;
                    }
                }

                pushReply(activeConnection, replyJSON, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void pushReply(ActiveConnection activeConnection, JSONObject reply, boolean result)
                throws JSONException, TimeoutException, IOException
        {
            activeConnection.reply(reply.put(Keyword.RESULT, result).toString());
        }

        private boolean isUpdateRequest(ActiveConnection activeConnection, JSONObject responseJSON)
                throws TimeoutException, JSONException, IOException
        {
            if (!responseJSON.has(Keyword.REQUEST))
                return false;

            JSONObject replyJSON = new JSONObject();
            String request = responseJSON.getString(Keyword.REQUEST);

            if (Keyword.REQUEST_UPDATE.equals(request)) {
                activeConnection.reply(replyJSON.put(Keyword.RESULT, true).toString());

                getSelfExecutor().submit(() -> {
                    try {
                        UpdateUtils.sendUpdate(getApplicationContext(), activeConnection.getAddress().getHostAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else if (Keyword.REQUEST_UPDATE_V2.equals(request)) {
                Device thisDevice = AppUtils.getLocalDevice(BackgroundService.this);
                File file = new File(getApplicationInfo().sourceDir);

                {
                    JSONObject reply = new JSONObject()
                            .put(Keyword.RESULT, true)
                            .put(Keyword.INDEX_FILE_SIZE, file.length())
                            .put(Keyword.APP_INFO_VERSION_CODE, thisDevice.versionCode);
                    activeConnection.reply(reply.toString());
                }

                {
                    Response responseObject = activeConnection.receive();
                    JSONObject response = responseObject.getAsJson();

                    if (response.getBoolean(Keyword.RESULT) && response.getBoolean(Keyword.RESULT)) {
                        OutputStream outputStream = activeConnection.getSocket().getOutputStream();
                        FileInputStream inputStream = new FileInputStream(file);

                        byte[] buffer = new byte[AppConfig.BUFFER_LENGTH_DEFAULT];
                        int len;
                        long lastRead = 0;

                        while ((len = inputStream.read(buffer)) != -1) {
                            long currentTime = System.nanoTime();

                            if (len > 0) {
                                lastRead = currentTime;

                                outputStream.write(buffer, 0, len);
                                outputStream.flush();
                            }

                            if (currentTime - lastRead > AppConfig.DEFAULT_SOCKET_TIMEOUT * 1e6)
                                throw new TimeoutException("Did not read any bytes for 5secs.");
                        }

                        inputStream.close();
                    }
                }
            } else
                return false;

            return true;
        }
    }

    public class LocalBinder extends Binder
    {
        public BackgroundService getService()
        {
            return BackgroundService.this;
        }
    }
}
