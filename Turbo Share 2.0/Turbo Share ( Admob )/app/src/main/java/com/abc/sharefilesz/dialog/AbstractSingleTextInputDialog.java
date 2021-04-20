

package com.abc.sharefilesz.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import com.abc.sharefilesz.R;

/**

 * date: 26.02.2018 07:55
 */

abstract public class AbstractSingleTextInputDialog extends AbstractFailureAwareDialog
{
    private EditText mEditText;
    private ViewGroup mView;

    public AbstractSingleTextInputDialog(final Context context)
    {
        super(context);

        mView = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_single_text_input,
                null);
        mEditText = mView.findViewById(R.id.layout_dialog_single_text_input_text);

        setView(mView);
        setTitle(R.string.text_createFolder);
        setNegativeButton(R.string.butn_close, null);
    }

    public ViewGroup getContainerView()
    {
        return mView;
    }

    public EditText getEditText()
    {
        return mEditText;
    }

    @Override
    public AlertDialog show()
    {
        AlertDialog dialog = super.show();
        mEditText.requestFocus();
        return dialog;
    }
}
