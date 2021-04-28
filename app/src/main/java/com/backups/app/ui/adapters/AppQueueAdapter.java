package com.backups.app.ui.adapters;

import static com.backups.app.ui.Constants.MIN_PROGRESS;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.backups.app.R;
import com.backups.app.data.APKFile;
import java.util.List;

public class AppQueueAdapter
    extends RecyclerView.Adapter<AppQueueAdapter.BackupsViewHolder> {

  private final List<APKFile> mDataSet;

  public AppQueueAdapter(List<APKFile> dataSet) { mDataSet = dataSet; }

  @NonNull
  @Override
  public BackupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
    View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.app_queue_item, parent, false);

    return new BackupsViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull BackupsViewHolder holder,
                               int position) {
    APKFile item = mDataSet.get(position);

    String appName = item.getName();
    String packageName = item.getPackageName();
    Drawable appIcon = item.getIcon();

    holder.setAppName(appName);
    holder.setPackageName(packageName);
    holder.setAppIcon(appIcon);
    holder.resetProgressBar();
  }

  @Override
  public int getItemCount() {
    return mDataSet.size();
  }

  public static class BackupsViewHolder extends RecyclerView.ViewHolder {
    private final TextView mAppName;
    private final TextView mPackageName;
    private final ImageView mAppIcon;
    private final ProgressBar mProgressBar;

    public BackupsViewHolder(View view) {
      super(view);

      mAppName = view.findViewById(R.id.app_queue_item_name_tv);
      mPackageName = view.findViewById(R.id.app_queue_item_package_name_tv);
      mAppIcon = view.findViewById(R.id.app_queue_item_iv);
      mProgressBar = view.findViewById(R.id.app_queue_item_pb);
    }

    public void setAppName(final String name) { mAppName.setText(name); }

    public void setPackageName(final String packageName) {
      mPackageName.setText(packageName);
    }

    public void setAppIcon(final Drawable icon) {
      mAppIcon.setImageDrawable(icon);
    }

    public void updateProgressBy(final int progress) {
      mProgressBar.incrementProgressBy(progress);
    }

    public void resetProgressBar() { mProgressBar.setProgress(MIN_PROGRESS); }
  }
}
