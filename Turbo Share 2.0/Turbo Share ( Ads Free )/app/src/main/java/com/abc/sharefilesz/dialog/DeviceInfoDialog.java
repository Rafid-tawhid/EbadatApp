

package com.abc.sharefilesz.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.abc.sharefilesz.config.AppConfig;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.task.ReceiveUpdateTask;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.NetworkDeviceLoader;
import com.abc.sharefilesz.BuildConfig;
import com.abc.sharefilesz.R;
import com.genonbeta.android.database.exception.ReconstructionFailedException;

/**

 * Date: 5/18/17 5:16 PM
 */

public class DeviceInfoDialog extends AlertDialog.Builder
{
    public static final String TAG = DeviceInfoDialog.class.getSimpleName();

    public DeviceInfoDialog(@NonNull final Activity activity, final Device device)
    {
        super(activity);

        final Kuick kuick = AppUtils.getKuick(activity);

        try {
            kuick.reconstruct(device);
        } catch (ReconstructionFailedException ignored) {
        }

        @SuppressLint("InflateParams")
        View rootView = LayoutInflater.from(activity).inflate(R.layout.fragment_device_info, null);

        Device localDevice = AppUtils.getLocalDevice(activity);
        ImageView image = rootView.findViewById(R.id.image);
        TextView text1 = rootView.findViewById(R.id.text1);
        TextView notSupportedText = rootView.findViewById(R.id.notSupportedText);
        TextView modelText = rootView.findViewById(R.id.modelText);
        TextView versionText = rootView.findViewById(R.id.versionText);
        final SwitchCompat accessSwitch = rootView.findViewById(R.id.accessSwitch);
        final SwitchCompat trustSwitch = rootView.findViewById(R.id.trustSwitch);
        final boolean isDeviceNormal = Device.Type.NORMAL.equals(device.type);

        if (device.versionCode < AppConfig.SUPPORTED_MIN_VERSION)
            notSupportedText.setVisibility(View.VISIBLE);

        if (isDeviceNormal && (localDevice.versionCode < device.versionCode || BuildConfig.DEBUG))
            setNeutralButton(R.string.butn_update, (dialog, which) -> EstablishConnectionDialog.show(activity, device,
                    (connection) -> BackgroundService.run(activity, new ReceiveUpdateTask(device, connection))));

        NetworkDeviceLoader.showPictureIntoView(device, image, AppUtils.getDefaultIconBuilder(activity));
        text1.setText(device.nickname);
        modelText.setText(String.format("%s %s", device.brand.toUpperCase(), device.model.toUpperCase()));
        versionText.setText(device.versionName);
        accessSwitch.setChecked(!device.isRestricted);
        trustSwitch.setEnabled(!device.isRestricted);
        trustSwitch.setChecked(device.isTrusted);

        accessSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            device.isRestricted = !isChecked;
            kuick.publish(device);
            kuick.broadcast();
            trustSwitch.setEnabled(isChecked);
        });

        if (isDeviceNormal)
            trustSwitch.setOnCheckedChangeListener((button, isChecked) -> {
                device.isTrusted = isChecked;
                kuick.publish(device);
                kuick.broadcast();
            });
        else
            trustSwitch.setVisibility(View.GONE);

        setView(rootView);
        setPositiveButton(R.string.butn_close, null);
        setNegativeButton(R.string.butn_remove, (dialog, which) -> new RemoveDeviceDialog(activity, device).show());
    }
}
