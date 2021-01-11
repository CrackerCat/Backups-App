package com.backups.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.backups.app.R;
import com.backups.app.data.APKFileRepository;
import com.backups.app.data.AppListFragmentViewModel;
import com.backups.app.data.ViewModelFactory;
import com.backups.app.fileoperations.APKFile;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppListFragment extends Fragment {
    private final APKFileRepository mApkFileRepository;

    private TextView mAppTextView;
    private ProgressBar mProgressBar;

    public AppListFragment() {
        super();
        Executor mExecutor = Executors.newSingleThreadExecutor();
        mApkFileRepository = new APKFileRepository(mExecutor);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.app_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentActivity activity = requireActivity();
        mAppTextView = view.findViewById(R.id.appTextView);
        mProgressBar = view.findViewById(R.id.progressBar);
        showProgressBar();

        AppListFragmentViewModel mViewModel = new ViewModelProvider(activity, new ViewModelFactory(activity.getPackageManager(), mApkFileRepository)).get(AppListFragmentViewModel.class);

        if (mViewModel.getAppList() == null) {
            showProgressBar();
        }

        mViewModel.getApks().observe(getViewLifecycleOwner(), apkFiles -> {
            if (!apkFiles.isEmpty()) {

                if (mViewModel.getAppList() == null) {
                    mViewModel.storeAppList(apkFiles);
                }

                for (APKFile apkFile : apkFiles) {
                    mAppTextView.append(String.format("%s\n\n\n", apkFile.getName()));
                }
            } else {
                mAppTextView.setText(R.string.no_apps_installed_message);
            }
            showCompletion();
        });
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mAppTextView.setVisibility(View.INVISIBLE);
    }

    private void showCompletion() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mAppTextView.setVisibility(View.VISIBLE);
    }
}
