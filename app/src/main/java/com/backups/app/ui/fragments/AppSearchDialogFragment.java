package com.backups.app.ui.fragments;

import android.app.Dialog;
import android.content.Context;
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
import com.backups.app.data.APKFile;
import com.backups.app.data.ApkListViewModel;
import com.backups.app.data.AppQueueViewModel;
import com.backups.app.ui.adapters.ItemClickListener;
import com.backups.app.ui.adapters.SearchAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

public class AppSearchDialogFragment
    extends DialogFragment implements ItemClickListener {

  private AppQueueViewModel mAppQueueViewModel;
  private ApkListViewModel mApkListViewModel;

  private OnFragmentInteractionListener mListener;
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
        new ViewModelProvider(parent).get(AppQueueViewModel.class);

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(parent);
    View dialogLayout = inflater.inflate(R.layout.search_dialog_fragment, null);
    initializeViews(dialogLayout);
    setupRecyclerView(parent);
    initializeSearchView();
    builder.setView(dialogLayout);

    return (builder.create());
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener)context;
    } else {
      throw new ClassCastException(
          context.getString(R.string.listener_cast_error_message));
    }
  }

  @Override
  public void onDestroyView() {
    mListener = null;
    super.onDestroyView();
  }

  private void initializeViews(View view) {
    mSearchResultsRecyclerView = view.findViewById(R.id.search_dialog_rv);
    mSearchView = view.findViewById(R.id.search_dialog_sv);
  }

  private void setupRecyclerView(FragmentActivity activity) {
    List<APKFile> installedApps =
        mApkListViewModel.getApkListLiveData().getValue();

    mSearchAdapter = new SearchAdapter(installedApps);
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
    APKFile selected = mSearchAdapter.getItem(position);
    mAppQueueViewModel.addApp(selected);
    mListener.onCall();
  }
}
