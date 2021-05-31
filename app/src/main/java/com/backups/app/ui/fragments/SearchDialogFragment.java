package com.backups.app.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.backups.app.R;
import com.backups.app.data.pojos.APKFile;
import com.backups.app.data.viewmodels.ApkListViewModel;
import com.backups.app.data.viewmodels.AppQueueViewModel;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.ui.adapters.ItemClickListener;
import com.backups.app.ui.adapters.SearchAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

public class SearchDialogFragment
    extends DialogFragment implements ItemClickListener {
  public enum DataSet { APP_LIST, APP_QUEUE }

  private DataSet mDataSetChoice = DataSet.APP_LIST;

  private AppQueueViewModel mAppQueueViewModel;
  private ApkListViewModel mApkListViewModel;

  private SearchAdapter mSearchAdapter;
  private RecyclerView mSearchResultsRecyclerView;
  private SearchView mSearchView;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    FragmentActivity parent = requireActivity();
    LayoutInflater inflater = parent.getLayoutInflater();

    mApkListViewModel =
        new ViewModelProvider(parent).get(ApkListViewModel.class);
    mAppQueueViewModel =
        new ViewModelProvider(parent, new BackupsViewModelFactory(parent))
            .get(AppQueueViewModel.class);

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(parent);

    View dialogLayout = inflater.inflate(R.layout.search_dialog_fragment, null);

    initializeViews(dialogLayout);

    setupRecyclerView(parent);

    initializeSearchView();

    builder.setView(dialogLayout);

    return builder.create();
  }

  private void initializeViews(View view) {
    mSearchResultsRecyclerView = view.findViewById(R.id.search_dialog_rv);
    mSearchView = view.findViewById(R.id.search_dialog_sv);
  }

  private List<APKFile> useDataSet(DataSet choice) {
    List<APKFile> dataSet = null;
    if (choice.equals(DataSet.APP_LIST)) {
      dataSet = mApkListViewModel.getApkListLiveData().getValue();
    } else if (choice.equals(DataSet.APP_QUEUE)) {
      dataSet = mAppQueueViewModel.getAppsInQueue();
    }
    return dataSet;
  }

  private void setupRecyclerView(FragmentActivity activity) {
    List<APKFile> data = useDataSet(mDataSetChoice);

    mSearchAdapter = new SearchAdapter(data);

    mSearchAdapter.setClickListener(this);

    LinearLayoutManager layoutManager = new LinearLayoutManager(activity);

    mSearchResultsRecyclerView.setLayoutManager(layoutManager);

    mSearchResultsRecyclerView.setAdapter(mSearchAdapter);
  }

  private void initializeSearchView() {
    mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

    mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        mSearchAdapter.getFilter().filter(newText);
        return false;
      }
    });
  }

  @Override
  public void onItemClick(View view, int position) {
    if (!mAppQueueViewModel.isBackupInProgress()) {
      APKFile selected = mSearchAdapter.getItem(position);

      mAppQueueViewModel.addApp(selected);
    }
  }

  public void setDataSetID(DataSet dataSetID) { mDataSetChoice = dataSetID; }
}
