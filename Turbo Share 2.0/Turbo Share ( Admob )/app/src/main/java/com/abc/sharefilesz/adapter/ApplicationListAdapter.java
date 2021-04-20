

package com.abc.sharefilesz.adapter;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.GlideApp;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.IEditableListFragment;
import com.abc.sharefilesz.io.Containable;
import com.abc.sharefilesz.object.Container;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.FileUtils;
import com.genonbeta.android.framework.util.listing.Merger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApplicationListAdapter extends GroupEditableListAdapter<ApplicationListAdapter.PackageHolder,
        GroupEditableListAdapter.GroupViewHolder>
{
    private SharedPreferences mPreferences;
    private PackageManager mManager;

    public ApplicationListAdapter(IEditableListFragment<PackageHolder, GroupViewHolder> fragment)
    {
        super(fragment, MODE_GROUP_BY_DATE);
        mPreferences = AppUtils.getDefaultPreferences(getContext());
        mManager = getContext().getPackageManager();
    }

    @Override
    protected void onLoad(GroupLister<PackageHolder> lister)
    {
        boolean showSystemApps = mPreferences.getBoolean("show_system_apps", false);

        for (PackageInfo packageInfo : getContext().getPackageManager().getInstalledPackages(
                PackageManager.GET_META_DATA)) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;

            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1 || showSystemApps) {
                PackageHolder packageHolder = new PackageHolder(String.valueOf(appInfo.loadLabel(mManager)), appInfo,
                        packageInfo.versionName, packageInfo.packageName, new File(appInfo.sourceDir));

                lister.offerObliged(this, packageHolder);
            }
        }
    }

    @Override
    protected PackageHolder onGenerateRepresentative(String text, Merger<PackageHolder> merger)
    {
        return new PackageHolder(VIEW_TYPE_REPRESENTATIVE, text);
    }

    @NonNull
    @Override
    public GroupEditableListAdapter.GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        GroupViewHolder holder = viewType == VIEW_TYPE_DEFAULT
                ? new GroupViewHolder(getInflater().inflate(R.layout.list_application, parent, false))
                : createDefaultViews(parent, viewType, false);

        if (!holder.isRepresentative()) {
            getFragment().registerLayoutViewClicks(holder);

            holder.itemView.findViewById(R.id.visitView)
                    .setOnClickListener(v -> getFragment().performLayoutClickOpen(holder));
            holder.itemView.findViewById(R.id.selector)
                    .setOnClickListener(v -> getFragment().setItemSelected(holder, true));
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupEditableListAdapter.GroupViewHolder holder, final int position)
    {
        try {
            final View parentView = holder.itemView;
            final PackageHolder object = getItem(position);

            if (!holder.tryBinding(object)) {
                ImageView image = parentView.findViewById(R.id.image);
                TextView text1 = parentView.findViewById(R.id.text);
                TextView text2 = parentView.findViewById(R.id.text2);
                ViewGroup layoutSplitApk = parentView.findViewById(R.id.layout_split_apk);
                boolean isSplitApk = Build.VERSION.SDK_INT >= 21 && object.appInfo.splitSourceDirs != null;

                text1.setText(object.friendlyName);
                text2.setText(object.version);
                layoutSplitApk.setVisibility(isSplitApk ? View.VISIBLE : View.GONE);

                parentView.setSelected(object.isSelectableSelected());

                GlideApp.with(getContext())
                        .load(object.appInfo)
                        .override(160)
                        .centerCrop()
                        .into(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class PackageHolder extends GroupEditableListAdapter.GroupShareable implements Container
    {
        public static final String FORMAT = ".apk";
        public static final String MIME_TYPE = FileUtils.getFileContentType(FORMAT);

        public ApplicationInfo appInfo;
        public String version;
        public String packageName;

        public PackageHolder(int viewType, String representativeText)
        {
            super(viewType, representativeText);
        }

        public PackageHolder(String friendlyName, ApplicationInfo appInfo, String version, String packageName,
                             File executableFile)
        {
            initialize(appInfo.packageName.hashCode(), friendlyName, friendlyName + "_" + version + FORMAT,
                    MIME_TYPE, executableFile.lastModified(), executableFile.length(), Uri.fromFile(executableFile));

            this.appInfo = appInfo;
            this.version = version;
            this.packageName = packageName;
        }

        @Nullable
        @Override
        public Containable expand()
        {
            if (Build.VERSION.SDK_INT < 21 || appInfo.splitSourceDirs == null)
                return null;

            List<Uri> fileList = new ArrayList<>();

            for (String location : appInfo.splitSourceDirs)
                fileList.add(Uri.fromFile(new File(location)));

            return new Containable(uri, fileList);
        }
    }
}
