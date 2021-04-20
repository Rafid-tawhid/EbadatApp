

package com.abc.sharefilesz.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.object.IndexOfTransferGroup;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.FileUtils;
import com.abc.sharefilesz.util.TextUtils;
import com.abc.sharefilesz.R;
import com.genonbeta.android.framework.io.DocumentFile;

import java.text.NumberFormat;

/**

 * date: 10.11.2017 14:59
 */

public class TransferInfoDialog extends AlertDialog.Builder
{
    public TransferInfoDialog(@NonNull final Activity activity, final IndexOfTransferGroup loadedGroup,
                              final TransferObject object, @Nullable String deviceId)
    {
        super(activity);

        DocumentFile attemptedFile = null;
        boolean isIncoming = TransferObject.Type.INCOMING.equals(object.type);

        try {
            // If it is incoming than get the received or cache file
            // If not then try to reach to the source file that is being send
            attemptedFile = isIncoming ? FileUtils.getIncomingPseudoFile(getContext(), object, loadedGroup.group,
                    false) : FileUtils.fromUri(getContext(), Uri.parse(object.file));
        } catch (Exception e) {
            e.printStackTrace();
        }

        final DocumentFile pseudoFile = attemptedFile;
        boolean fileExists = pseudoFile != null && pseudoFile.canRead();

        @SuppressLint("InflateParams")
        View rootView = LayoutInflater.from(activity).inflate(R.layout.layout_transfer_info, null);

        TextView nameText = rootView.findViewById(R.id.transfer_info_file_name);
        TextView sizeText = rootView.findViewById(R.id.transfer_info_file_size);
        TextView typeText = rootView.findViewById(R.id.transfer_info_file_mime);
        TextView flagText = rootView.findViewById(R.id.transfer_info_file_status);

        View incomingDetailsLayout = rootView.findViewById(R.id.transfer_info_incoming_details_layout);
        TextView receivedSizeText = rootView.findViewById(R.id.transfer_info_received_size);
        TextView locationText = rootView.findViewById(R.id.transfer_info_pseudo_location);

        setTitle(R.string.text_transactionDetails);
        setView(rootView);

        nameText.setText(object.name);
        sizeText.setText(FileUtils.sizeExpression(object.size, false));
        typeText.setText(object.mimeType);

        receivedSizeText.setText(fileExists
                ? FileUtils.sizeExpression(pseudoFile.length(), false)
                : getContext().getString(R.string.text_unknown));

        locationText.setText(fileExists
                ? FileUtils.getReadableUri(pseudoFile.getUri())
                : getContext().getString(R.string.text_unknown));

        flagText.setText(TextUtils.getTransactionFlagString(getContext(), object,
                NumberFormat.getPercentInstance(), deviceId));
        setPositiveButton(R.string.butn_close, null);
        setNegativeButton(R.string.butn_remove,
                (dialogInterface, i) -> DialogUtils.showRemoveDialog(activity, object));

        if (isIncoming) {
            incomingDetailsLayout.setVisibility(View.VISIBLE);

            if (TransferObject.Flag.INTERRUPTED.equals(object.getFlag())
                    || TransferObject.Flag.IN_PROGRESS.equals(object.getFlag())) {
                setNeutralButton(R.string.butn_retry, (dialogInterface, i) -> {
                    object.setFlag(TransferObject.Flag.PENDING);
                    AppUtils.getKuick(activity).publish(object);
                    AppUtils.getKuick(activity).broadcast();
                });
            } else if (fileExists) {
                if (TransferObject.Flag.REMOVED.equals(object.getFlag()) && pseudoFile.getParentFile() != null) {
                    setNeutralButton(R.string.butn_saveAnyway, (dialogInterface, i) -> {
                        AlertDialog.Builder saveAnyway = new AlertDialog.Builder(getContext());

                        saveAnyway.setTitle(R.string.ques_saveAnyway);
                        saveAnyway.setMessage(R.string.text_saveAnywaySummary);
                        saveAnyway.setNegativeButton(R.string.butn_cancel, null);
                        saveAnyway.setPositiveButton(R.string.butn_proceed, (dialog, which) -> {
                            try {
                                DocumentFile savedFile = FileUtils.saveReceivedFile(
                                        pseudoFile.getParentFile(), pseudoFile, object);
                                ;
                                object.setFlag(TransferObject.Flag.DONE);

                                AppUtils.getKuick(activity).update(object);
                                AppUtils.getKuick(activity).broadcast();

                                Toast.makeText(getContext(), R.string.mesg_fileSaved, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();
                            }
                        });

                        saveAnyway.show();
                    });
                } else if (TransferObject.Flag.DONE.equals(object.getFlag())) {
                    setNeutralButton(R.string.butn_open, (dialog, which) -> {
                        try {
                            FileUtils.openUri(getContext(), pseudoFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } else if (fileExists)
            try {
                final Intent startIntent = FileUtils.getOpenIntent(getContext(), attemptedFile);

                setNeutralButton(R.string.butn_open, (dialog, which) -> {
                    try {
                        getContext().startActivity(startIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception ignored) {
            }
    }
}
