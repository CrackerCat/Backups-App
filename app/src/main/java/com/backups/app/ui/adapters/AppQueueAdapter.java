package com.backups.app.ui.adapters;

import static com.backups.app.Constants.MIN_PROGRESS;
import static com.backups.app.Constants.REMOVE_FROM;

import android.graphics.Color;
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
import com.backups.app.data.pojos.ApkFile;
import java.util.List;

public final class AppQueueAdapter
    extends RecyclerView.Adapter<AppQueueAdapter.BackupsViewHolder> {

  private final int mItemBgViewColor;
  private ItemClickListener mClickListener;
  private final List<ApkFile> mDataSet;

  public AppQueueAdapter(List<ApkFile> dataSet, final int itemViewBgColor) {
    mDataSet = dataSet;

    mItemBgViewColor = itemViewBgColor;
  }

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
    final ApkFile item = mDataSet.get(position);

    holder.itemView.setBackgroundColor(item.marked() ? mItemBgViewColor
                                                     : Color.TRANSPARENT);

    populateBackupViewHolder(holder, item);
  }

  @Override
  public int getItemCount() {
    return mDataSet.size();
  }

  public void setClickListener(ItemClickListener itemClickListener) {
    mClickListener = itemClickListener;
  }

  public ApkFile getItemAt(final int position) {
    return mDataSet.get(position);
  }

  public void updateBackupViewHolder(final RecyclerView recyclerView, int by) {
    if (recyclerView != null) {
      final AppQueueAdapter.BackupsViewHolder backupsViewHolder =
          (AppQueueAdapter.BackupsViewHolder)
              recyclerView.findViewHolderForAdapterPosition(REMOVE_FROM);

      if (backupsViewHolder != null) {
        backupsViewHolder.updateProgressBy(by);
      }
    }
  }

  private void populateBackupViewHolder(final BackupsViewHolder holder,
                                        final ApkFile item) {
    holder.setAppName(item.getName());

    holder.setPackageName(item.getPackageName());

    holder.setAppIcon(item.getIcon());

    holder.resetProgressBar();
  }

  public class BackupsViewHolder
      extends RecyclerView.ViewHolder implements View.OnClickListener {
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

      view.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
      if (mClickListener != null) {
        final ApkFile item = mDataSet.get(getBindingAdapterPosition());

        v.setBackgroundColor(
            (item.mark(!item.marked()) ? mItemBgViewColor : Color.TRANSPARENT));

        mClickListener.onItemClick(v, getBindingAdapterPosition());
      }
    }
  }
}
