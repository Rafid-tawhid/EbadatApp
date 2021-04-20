

package com.abc.sharefilesz.adapter;

import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.abc.sharefilesz.object.Editable;
import com.abc.sharefilesz.widget.EditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.IEditableListFragment;
import com.abc.sharefilesz.config.AppConfig;
import com.abc.sharefilesz.util.NetworkUtils;
import com.abc.sharefilesz.util.TextUtils;
import com.genonbeta.android.framework.widget.RecyclerViewAdapter;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

/**

 * date: 4/7/19 10:35 PM
 */
public class ActiveConnectionListAdapter extends EditableListAdapter<
        ActiveConnectionListAdapter.EditableNetworkInterface, RecyclerViewAdapter.ViewHolder>
{
    public ActiveConnectionListAdapter(IEditableListFragment<EditableNetworkInterface, ViewHolder> fragment)
    {
        super(fragment);
    }

    @Override
    public List<EditableNetworkInterface> onLoad()
    {
        List<EditableNetworkInterface> resultList = new ArrayList<>();
        List<NetworkInterface> interfaceList = NetworkUtils.getInterfaces(true,
                AppConfig.DEFAULT_DISABLED_INTERFACES);

        for (NetworkInterface addressedInterface : interfaceList) {
            EditableNetworkInterface editableInterface = new EditableNetworkInterface(addressedInterface,
                    TextUtils.getAdapterName(getContext(), addressedInterface));

            if (filterItem(editableInterface))
                resultList.add(editableInterface);
        }

        return resultList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        ViewHolder holder = new ViewHolder(getInflater().inflate(R.layout.list_active_connection, parent,
                false));

        getFragment().registerLayoutViewClicks(holder);
        holder.itemView.findViewById(R.id.visitView)
                .setOnClickListener(v -> getFragment().performLayoutClickOpen(holder));
        holder.itemView.findViewById(R.id.selector)
                .setOnClickListener(v -> getFragment().setItemSelected(holder, true));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        EditableNetworkInterface object = getItem(position);

        TextView text1 = holder.itemView.findViewById(R.id.text);
        TextView text2 = holder.itemView.findViewById(R.id.text2);

        text1.setText(object.getSelectableTitle());
        text2.setText(TextUtils.makeWebShareLink(getContext(), NetworkUtils.getFirstInet4Address(object)
                .getHostAddress()));
    }

    public static class EditableNetworkInterface implements Editable
    {
        private NetworkInterface mInterface;
        private String mName;

        public EditableNetworkInterface(NetworkInterface addressedInterface, String name)
        {
            mInterface = addressedInterface;
            mName = name;
        }

        @Override
        public boolean applyFilter(String[] filteringKeywords)
        {
            for (String word : filteringKeywords) {
                String wordLC = word.toLowerCase();

                if (mInterface.getDisplayName().toLowerCase().contains(wordLC)
                        || mName.toLowerCase().contains(wordLC))
                    return true;
            }

            return false;
        }

        @Override
        public long getId()
        {
            return mInterface.hashCode();
        }

        @Override
        public void setId(long id)
        {
            // not required
        }

        @Override
        public boolean comparisonSupported()
        {
            return false;
        }

        @Override
        public String getComparableName()
        {
            return mName;
        }

        @Override
        public long getComparableDate()
        {
            return 0;
        }

        @Override
        public long getComparableSize()
        {
            return 0;
        }

        public NetworkInterface getInterface()
        {
            return mInterface;
        }

        public String getName()
        {
            return mName;
        }

        @Override
        public String getSelectableTitle()
        {
            return mName;
        }

        @Override
        public boolean isSelectableSelected()
        {
            return false;
        }

        @Override
        public boolean setSelectableSelected(boolean selected)
        {
            return false;
        }
    }
}
