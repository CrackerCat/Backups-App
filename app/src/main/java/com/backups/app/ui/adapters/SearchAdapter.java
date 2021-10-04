package com.backups.app.ui.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.backups.app.R;
import com.backups.app.data.pojos.ApkFile;
import java.util.ArrayList;
import java.util.List;

public final class SearchAdapter
    extends RecyclerView.Adapter<SearchAdapter.SearchResultViewHolder>
    implements Filterable {
  private final List<ApkFile> mDataSet;
  private final List<ApkFile> mSearchResults = new ArrayList<>();
  private ItemClickListener mClickListener;

  private Filter mApkSearchFilter;

  public SearchAdapter(final List<ApkFile> data) {
    mDataSet = data;

    initializeSearchFilter();
  }

  @NonNull
  @Override
  public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                   int viewType) {
    View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_item, parent, false);

    return new SearchResultViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull SearchResultViewHolder holder,
                               int position) {
    final ApkFile item = mSearchResults.get(position);

    holder.setAppName(item.getName());

    holder.setAppIcon(item.getIcon());
  }

  @Override
  public int getItemCount() {
    return mSearchResults.size();
  }

  @Override
  public Filter getFilter() {
    return mApkSearchFilter;
  }

  public final ApkFile getItem(final int position) {
    return mSearchResults.get(position);
  }

  public void setClickListener(ItemClickListener itemClickListener) {
    mClickListener = itemClickListener;
  }

  private void initializeSearchFilter() {
    mApkSearchFilter = new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        final String query = constraint.toString();

        final List<ApkFile> results = new ArrayList<>(mSearchResults.size());

        if (!query.isEmpty()) {
          for (ApkFile app : mDataSet) {
            if (app.getName().contains(query)) {
              results.add(app);
            }
          }
        }

        final FilterResults filterResults = new FilterResults();

        filterResults.values = results;

        return filterResults;
      }

      @Override
      protected void publishResults(CharSequence constraint,
                                    FilterResults results) {

        @SuppressWarnings("unchecked") final List<ApkFile> searchResults =
                (List<ApkFile>) results.values;

        mSearchResults.clear();
        if (!searchResults.isEmpty()) {
          mSearchResults.addAll(searchResults);
        }
        notifyDataSetChanged();
      }
    };
  }

  protected class SearchResultViewHolder
      extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView mAppName;
    private final ImageView mAppIcon;

    public SearchResultViewHolder(View view) {
      super(view);

      mAppName = view.findViewById(R.id.search_item_name_tv);
      mAppIcon = view.findViewById(R.id.search_item_iv);

      view.setOnClickListener(this);
    }

    public void setAppName(String appName) { mAppName.setText(appName); }

    public void setAppIcon(Drawable icon) { mAppIcon.setImageDrawable(icon); }

    @Override
    public void onClick(View view) {
      if (mClickListener != null) {
        mClickListener.onItemClick(view, getBindingAdapterPosition());
      }
    }
  }
}
