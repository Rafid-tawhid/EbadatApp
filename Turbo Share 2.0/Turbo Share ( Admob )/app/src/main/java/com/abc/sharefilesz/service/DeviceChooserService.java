

package com.abc.sharefilesz.service;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.chooser.ChooserTarget;
import android.service.chooser.ChooserTargetService;
import androidx.annotation.RequiresApi;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.activity.ShareActivity;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.graphics.drawable.TextDrawable;
import com.abc.sharefilesz.util.AppUtils;
import com.genonbeta.android.database.SQLQuery;

import java.util.ArrayList;
import java.util.List;

/**

 * Date: 5/23/17 5:16 PM
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class DeviceChooserService extends ChooserTargetService
{
    @Override
    public List<ChooserTarget> onGetChooserTargets(ComponentName targetActivityName, IntentFilter matchedFilter)
    {
        Kuick kuick = AppUtils.getKuick(getApplicationContext());
        List<ChooserTarget> list = new ArrayList<>();

        // use default accent color for light theme
        TextDrawable.IShapeBuilder iconBuilder = AppUtils.getDefaultIconBuilder(getApplicationContext());

        for (Device device : kuick.castQuery(new SQLQuery.Select(Kuick.TABLE_DEVICES),
                Device.class)) {
            if (device.isLocal)
                continue;

            Bundle bundle = new Bundle();
            bundle.putString(ShareActivity.EXTRA_DEVICE_ID, device.id);

            TextDrawable textImage = iconBuilder.buildRound(device.nickname);
            Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            textImage.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            textImage.draw(canvas);

            float result = (float) device.lastUsageTime / (float) System.currentTimeMillis();

            list.add(new ChooserTarget(device.nickname, Icon.createWithBitmap(bitmap), result, targetActivityName,
                    bundle
            ));
        }

        return list;
    }
}
