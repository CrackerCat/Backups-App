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
import com.backups.app.data.pojos.ApkFile;
import java.util.List;

public final class AppListAdapter
    extends RecyclerView.Adapter<AppListAdapter.ApkDataViewHolder> {
  private List<ApkFile> mDataSet;
  private ItemClickListener mClickListener;

  public AppListAdapter(List<ApkFile> dataSet) { mDataSet = dataSet; }

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
    final ApkFile item = mDataSet.get(position);

    holder.setAppName(item.getName());

    holder.setPackageName(item.getPackageName());

    holder.setAppIcon(item.getIcon());
  }

  @Override
  public int getItemCount() {
    return mDataSet.size();
  }

  public final ApkFile getItem(final int position) {
    return mDataSet.get(position);
  }

  public void changeDataSet(List<ApkFile> apkFiles) {
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