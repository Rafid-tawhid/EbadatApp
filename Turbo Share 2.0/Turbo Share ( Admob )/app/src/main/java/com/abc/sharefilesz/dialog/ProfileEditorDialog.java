

package com.abc.sharefilesz.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.R;

public class ProfileEditorDialog extends AlertDialog.Builder
{
    private AlertDialog mDialog;

    public ProfileEditorDialog(@NonNull final Activity activity)
    {
        super(activity);

        final View view = LayoutInflater.from(activity).inflate(R.layout.layout_profile_editor, null, false);
        final ImageView image = view.findViewById(R.id.layout_profile_picture_image_default);
        final ImageView editImage = view.findViewById(R.id.layout_profile_picture_image_preferred);
        final EditText editText = view.findViewById(R.id.editText);
        final String deviceName = AppUtils.getLocalDeviceName(getContext());

        editText.getText().clear();
        editText.getText().append(deviceName);
        activity.loadProfilePictureInto(deviceName, image);
        editText.requestFocus();

        editImage.setOnClickListener(v -> {
            activity.requestProfilePictureChange();
            saveNickname(activity, editText);
            closeIfPossible();
        });

        setView(view);

        setNegativeButton(R.string.butn_remove, (dialog, which) -> {
            activity.deleteFile("profilePicture");
            activity.notifyUserProfileChanged();
        });

        setPositiveButton(R.string.butn_save, (dialog, which) -> saveNickname(activity, editText));

        setNeutralButton(R.string.butn_close, null);
    }

    protected void closeIfPossible()
    {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.dismiss();
            else
                mDialog = null;
        }
    }

    @Override
    public AlertDialog show()
    {
        return mDialog = super.show();
    }

    public void saveNickname(Activity activity, EditText editText)
    {
        AppUtils.getDefaultPreferences(getContext()).edit()
                .putString("device_name", editText.getText().toString())
                .apply();

        activity.notifyUserProfileChanged();
    }
}
