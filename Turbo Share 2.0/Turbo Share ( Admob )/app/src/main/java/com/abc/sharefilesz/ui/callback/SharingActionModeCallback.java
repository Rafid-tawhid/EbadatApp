

package com.abc.sharefilesz.ui.callback;

import com.abc.sharefilesz.object.Shareable;

/**

 * date: 14/04/18 15:59
 */
public class SharingActionModeCallback<T extends Shareable>
{/*extends EditableListFragment.SelectionCallback<T>
{
    public SharingActionModeCallback(EditableListFragmentImpl<T> fragment)
    {
        super(fragment);
    }

    @Override
    public boolean onPrepareActionMenu(Context context, PowerfulActionMode actionMode)
    {
        super.onPrepareActionMenu(context, actionMode);
        return true;
    }

    @Override
    public boolean onCreateActionMenu(Context context, PowerfulActionMode actionMode, Menu menu)
    {
        super.onCreateActionMenu(context, actionMode, menu);
        actionMode.getMenuInflater().inflate(R.menu.action_mode_share, menu);
        return true;
    }

    @Override
    public boolean onActionMenuItemSelected(Context context, PowerfulActionMode actionMode, MenuItem item)
    {
        int id = item.getItemId();
        boolean isLocalShare = id == R.id.action_mode_share_TurboShare;
        boolean isSharing = isLocalShare || id == R.id.action_mode_share_all_apps;
        List<T> selectedItemList = new ArrayList<>(getFragment().getEngineConnection().getSelectedItemList());

        if (selectedItemList.size() > 0 && isSharing) {
            Intent shareIntent = (isLocalShare ? new Intent(context, ShareActivity.class) : new Intent())
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setAction(selectedItemList.size() > 1 ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND);

            ArrayList<Containable> containerList = new ArrayList<>();

            if (selectedItemList.size() > 1) {
                MIMEGrouper mimeGrouper = new MIMEGrouper();
                ArrayList<Uri> uriList = new ArrayList<>();
                ArrayList<CharSequence> nameList = new ArrayList<>();

                for (T sharedItem : selectedItemList) {
                    uriList.add(sharedItem.uri);
                    nameList.add(sharedItem.fileName);

                    addIfEligible(containerList, sharedItem);

                    if (!mimeGrouper.isLocked())
                        mimeGrouper.process(sharedItem.mimeType);
                }

                shareIntent.setType(mimeGrouper.toString())
                        .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                        .putCharSequenceArrayListExtra(ShareActivity.EXTRA_FILENAME_LIST, nameList);
            } else if (selectedItemList.size() == 1) {
                T sharedItem = selectedItemList.get(0);

                addIfEligible(containerList, sharedItem);

                shareIntent.setType(sharedItem.mimeType)
                        .putExtra(Intent.EXTRA_STREAM, sharedItem.uri)
                        .putExtra(ShareActivity.EXTRA_FILENAME_LIST, sharedItem.fileName);
            }

            if (containerList.size() > 0)
                shareIntent.putParcelableArrayListExtra(ShareActivity.EXTRA_FILE_CONTAINER, containerList);

            try {
                if (isLocalShare)
                    new ChooseSharingMethodDialog<T>(getFragment().getActivity(), shareIntent).show();
                else
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(
                            R.string.text_fileShareAppChoose)));
                return true;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.mesg_noActivityFound, Toast.LENGTH_SHORT).show();
            } catch (Throwable e) {
                e.printStackTrace();
                Toast.makeText(context, R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();
            }
        } else
            return super.onActionMenuItemSelected(context, actionMode, item);

        return false;
    }

    private void addIfEligible(ArrayList<Containable> list, T sharedItem)
    {
        if (sharedItem instanceof Container) {
            Containable containable = ((Container) sharedItem).expand();

            if (containable != null)
                list.add(containable);
        }
    }

    public static class SelectionDuo<T extends Shareable>
    {
        private EditableListFragmentImpl<T> mFragment;
        private EditableListAdapterImpl<T> mAdapter;

        public SelectionDuo(EditableListFragmentImpl<T> fragment, EditableListAdapterImpl<T> adapter)
        {
            mFragment = fragment;
            mAdapter = adapter;
        }

        public EditableListAdapterImpl<T> getAdapter()
        {
            return mAdapter;
        }

        public EditableListFragmentImpl<T> getFragment()
        {
            return mFragment;
        }
    }*/
}
