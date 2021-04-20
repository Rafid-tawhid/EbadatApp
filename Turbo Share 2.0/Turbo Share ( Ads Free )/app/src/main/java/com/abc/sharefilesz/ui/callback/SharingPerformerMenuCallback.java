

package com.abc.sharefilesz.ui.callback;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.abc.sharefilesz.app.EditableListFragment;
import com.abc.sharefilesz.object.MappedSelectable;
import com.abc.sharefilesz.object.Shareable;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.dialog.ChooseSharingMethodDialog;
import com.abc.sharefilesz.util.MIMEGrouper;
import com.genonbeta.android.framework.ui.PerformerMenu;
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine;
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider;

import java.util.ArrayList;
import java.util.List;

public class SharingPerformerMenuCallback extends EditableListFragment.SelectionCallback
{
    public SharingPerformerMenuCallback(Activity activity, PerformerEngineProvider provider)
    {
        super(activity, provider);
    }

    @Override
    public boolean onPerformerMenuList(PerformerMenu performerMenu, MenuInflater inflater, Menu targetMenu)
    {
        super.onPerformerMenuList(performerMenu, inflater, targetMenu);
        inflater.inflate(R.menu.action_mode_share, targetMenu);
        return true;
    }

    @Override
    public boolean onPerformerMenuSelected(PerformerMenu performerMenu, MenuItem item)
    {
        int id = item.getItemId();
        IPerformerEngine performerEngine = getPerformerEngine();

        if (performerEngine == null)
            return false;

        List<Shareable> shareableList = compileShareableListFrom(MappedSelectable.compileFrom(performerEngine));

        if (id == R.id.action_mode_share_TurboShare) {
            if (shareableList.size() > 0)
                new ChooseSharingMethodDialog(getActivity(), shareableList).show();
        } else if (id == R.id.action_mode_share_all_apps) {
            if (shareableList.size() <= 0)
                return false;

            Intent intent = new Intent(shareableList.size() > 1 ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (shareableList.size() > 1) {
                MIMEGrouper mimeGrouper = new MIMEGrouper();
                ArrayList<Uri> uriList = new ArrayList<>();

                for (Shareable sharedItem : shareableList) {
                    uriList.add(sharedItem.uri);

                    if (!mimeGrouper.isLocked())
                        mimeGrouper.process(sharedItem.mimeType);
                }

                intent.setType(mimeGrouper.toString())
                        .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
            } else if (shareableList.size() == 1) {
                Shareable sharedItem = shareableList.get(0);

                intent.setType(sharedItem.mimeType)
                        .putExtra(Intent.EXTRA_STREAM, sharedItem.uri);
            }

            try {
                getActivity().startActivity(Intent.createChooser(intent, getActivity().getString(
                        R.string.text_fileShareAppChoose)));
                return true;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), R.string.mesg_noActivityFound, Toast.LENGTH_SHORT).show();
            } catch (Throwable e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();
            }
        } else
            return super.onPerformerMenuSelected(performerMenu, item);

        // I want the menus to keep showing because sharing does not alter data. If it is so descendants should
        // check and return 'true'.
        return false;
    }

    private List<Shareable> compileShareableListFrom(List<MappedSelectable<?>> mappedSelectableList)
    {
        List<Shareable> shareableList = new ArrayList<>();

        for (MappedSelectable<?> mappedSelectable : mappedSelectableList)
            if (mappedSelectable.selectable instanceof Shareable)
                shareableList.add((Shareable) mappedSelectable.selectable);

        return shareableList;
    }
}
