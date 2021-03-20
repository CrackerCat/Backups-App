package com.backups.app.ui.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.backups.app.R;
import com.backups.app.data.APKFile;
import java.util.List;

public class AppListAdapter
    extends RecyclerView.Adapter<AppListAdapter.ApkDataViewHolder> {
  private List<APKFile> mDataSet;
  private ItemClickListener mClickListener;

  public AppListAdapter(List<APKFile> dataSet) { mDataSet = dataSet; }

  @NonNull
  @Override
  public ApkDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
    View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.app_list_item, parent, false);

    return new ApkDataViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ApkDataViewHolder holder,
                               int position) {
    String appName = mDataSet.get(position).getName();
    String packageName = mDataSet.get(position).getPackageName();
    Drawable appIcon = mDataSet.get(position).getIcon();

    holder.setAppName(appName);
    holder.setPackageName(packageName);
    holder.setAppIcon(appIcon);
  }

  @Override
  public int getItemCount() {
    return mDataSet.size();
  }

  public final APKFile getItem(final int position) {
    return mDataSet.get(position);
  }

  public void changeDataSet(List<APKFile> apkFiles) {
    mDataSet = apkFiles;
    notifyDataSetChanged();
  }

  public void setClickListener(ItemClickListener itemClickListener) {
    mClickListener = itemClickListener;
  }

  protected class ApkDataViewHolder
      extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView mAppName;
    private final ImageView mAppIcon;
    private final TextView mPackageName;

    public ApkDataViewHolder(View view) {
      super(view);

      mAppName = view.findViewById(R.id.app_list_item_name_tv);
      mPackageName = view.findViewById(R.id.app_list_item_package_name_tv);
      mAppIcon = view.findViewById(R.id.app_list_item_iv);

      view.setOnClickListener(this);
    }

    public void setAppName(String appName) { mAppName.setText(appName); }

    public void setPackageName(String packageName) {
      mPackageName.setText(packageName);
    }

    public void setAppIcon(Drawable icon) { mAppIcon.setImageDrawable(icon); }

    @Override
    public void onClick(View view) {
      if (mClickListener != null) {
        mClickListener.onItemClick(view, getAdapterPosition());
      }
    }
  }
}