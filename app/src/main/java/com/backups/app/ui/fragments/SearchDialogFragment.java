package com.backups.app.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
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
import com.backups.app.data.pojos.ApkFile;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.data.viewmodels.apklist.ApkListViewModel;
import com.backups.app.data.viewmodels.appqueue.AppQueueViewModel;
import com.backups.app.ui.adapters.ItemClickListener;
import com.backups.app.ui.adapters.SearchAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

public final class SearchDialogFragment
    extends DialogFragment implements ItemClickListener {

  public enum DataSet { APP_LIST, APP_QUEUE }

  private DataSet mDataSetChoice = DataSet.APP_LIST;

  private ApkListViewModel mApkListViewModel;
  private AppQueueViewModel mAppQueueViewModel;

  private SearchAdapter mSearchAdapter;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    final FragmentActivity parent = requireActivity();

    initializeViewModels(parent);

    final MaterialAlertDialogBuilder builder =
        new MaterialAlertDialogBuilder(parent);

    final View dialogLayout =
        getLayoutInflater().inflate(R.layout.search_dialog_fragment, null);

    initializeSearchView(dialogLayout);

    initializeRecyclerView(dialogLayout, parent);

    builder.setView(dialogLayout);

    return builder.create();
  }

  @Override
  public void onItemClick(View view, int position) {
    if (!mAppQueueViewModel.isBackupInProgress()) {
      ApkFile selected = mSearchAdapter.getItem(position);

      mAppQueueViewModel.addApp(selected);
    }
  }

  public void setDataSetID(DataSet dataSetID) { mDataSetChoice = dataSetID; }

  private void initializeViewModels(FragmentActivity parent) {
    mApkListViewModel =
        new ViewModelProvider(parent).get(ApkListViewModel.class);

    mAppQueueViewModel =
        new ViewModelProvider(parent, new BackupsViewModelFactory(parent))
            .get(AppQueueViewModel.class);
  }

  private void initializeRecyclerView(final View view,
                                      final FragmentActivity activity) {
    final List<ApkFile> data = useDataSet(mDataSetChoice);

    mSearchAdapter = new SearchAdapter(data);

    mSearchAdapter.setClickListener(this);

    final RecyclerView recyclerview = view.findViewById(R.id.search_dialog_rv);

    recyclerview.setLayoutManager(new LinearLayoutManager(activity));

    recyclerview.setAdapter(mSearchAdapter);
  }

  private void initializeSearchView(final View view) {
    final SearchView mSearchView = view.findViewById(R.id.search_dialog_sv);

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

  private List<ApkFile> useDataSet(DataSet choice) {
    List<ApkFile> dataSet = null;

    if (choice == DataSet.APP_LIST) {
      dataSet = mApkListViewModel.getApkListLiveData().getValue();

    } else if (choice == DataSet.APP_QUEUE) {
      dataSet = mAppQueueViewModel.getAppsInQueue();

    }

    return dataSet;
  }
}
