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
import com.backups.app.data.APKFile;
import com.backups.app.ui.fragments.OnFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter
    extends RecyclerView.Adapter<SearchAdapter.SearchResultViewHolder>
    implements Filterable {
  private final List<APKFile> mDataSet;
  private final List<APKFile> mSearchResults = new ArrayList<>();
  private ItemClickListener mClickListener;

  public SearchAdapter(final List<APKFile> data) { mDataSet = data; }

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
    String appName = mSearchResults.get(position).getName();
    Drawable appIcon = mSearchResults.get(position).getIcon();

    holder.setAppName(appName);
    holder.setAppIcon(appIcon);
  }

  @Override
  public int getItemCount() {
    return mSearchResults.size();
  }

  public final APKFile getItem(final int position) {
    return mSearchResults.get(position);
  }

  public void setClickListener(ItemClickListener itemClickListener) {
    mClickListener = itemClickListener;
  }

  private final Filter mApkSearchFilter = new Filter() {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      String query = constraint.toString();

      List<APKFile> results = new ArrayList<>(mSearchResults.size());

      if (!query.isEmpty()) {
        for (APKFile app : mDataSet) {
          if (app.getName().contains(query)) {
            results.add(app);
          }
        }
      }

      FilterResults filterResults = new FilterResults();
      filterResults.values = results;
      return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint,
                                  FilterResults results) {
      List<APKFile> searchResults = (List<APKFile>)results.values;

      mSearchResults.clear();
      if(!searchResults.isEmpty()) {
        mSearchResults.addAll(searchResults);
      }
      notifyDataSetChanged();
    }
  };

  @Override
  public Filter getFilter() {
    return mApkSearchFilter;
  }

  protected class SearchResultViewHolder
      extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView mAppName;
    private final ImageView mAppIcon;

    public SearchResultViewHolder(View view) {
      super(view);

      mAppName = view.findViewById(R.id.app_name);
      mAppIcon = view.findViewById(R.id.app_icon);

      view.setOnClickListener(this);
    }

    public void setAppName(String appName) { mAppName.setText(appName); }

    public void setAppIcon(Drawable icon) { mAppIcon.setImageDrawable(icon); }

    @Override
    public void onClick(View view) {
      if (mClickListener != null) {
        mClickListener.onItemClick(view, getAdapterPosition());
      }
    }
  }
}
