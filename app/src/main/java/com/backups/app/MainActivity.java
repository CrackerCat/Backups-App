package com.backups.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.backups.app.data.ApkListViewModel;
import com.backups.app.data.AppQueueViewModel;
import com.backups.app.data.BackupCreator;
import com.backups.app.permissionshandler.PermissionsHandler;
import com.backups.app.ui.actions.ActionButton;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.actions.IAction;
import com.backups.app.ui.adapters.TabAdapter;
import com.backups.app.ui.fragments.AppListFragment;
import com.backups.app.ui.fragments.AppQueueFragment;
import com.backups.app.ui.fragments.AppSearchDialogFragment;
import com.backups.app.ui.fragments.OnFragmentInteractionListener;
import com.backups.app.ui.fragments.SettingsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity
    extends AppCompatActivity implements OnFragmentInteractionListener,
                                         ActionPresenter.IActionAvailability {
  private ApkListViewModel mApkListViewModel;
  private AppQueueViewModel mAppQueueViewModel;

  private TextView mBackupCounterView;
  private TabAdapter mTabAdapter;
  private TabLayout mTabLayout;
  private ViewPager2 mViewPager;
  private ActionPresenter mActionPresenter;
  private final AppSearchDialogFragment mAppSearchDialogFragment =
      new AppSearchDialogFragment();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mApkListViewModel = new ViewModelProvider(this).get(ApkListViewModel.class);
    mAppQueueViewModel =
        new ViewModelProvider(this).get(AppQueueViewModel.class);
    mApkListViewModel.fetchInstalledApps(getPackageManager(), true);

    initializeViews();

    initializeTabLayout();

    initializeFAButton();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode,
                                  Intent resultData) {
    super.onActivityResult(requestCode, resultCode, resultData);

    Uri newDirectoryUri;

    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == PermissionsHandler.CREATE_DIRECTORY_CODE) {
        if (resultData != null) {
          newDirectoryUri = resultData.getData();
          DocumentFile pickedDirectory =
              DocumentFile.fromTreeUri(this, newDirectoryUri);
          if (pickedDirectory != null) {
            pickedDirectory.createDirectory(BackupCreator.getOutputDirectory());
          }
        }
      }
    }
  }

  private void addTabs(TabAdapter tabAdapter) {
    tabAdapter.addTab(getString(R.string.apps_tab_name), new AppListFragment());
    tabAdapter.addTab(getString(R.string.queue_tab_name),
                      new AppQueueFragment());
    tabAdapter.addTab(getString(R.string.settings_tab_name),
                      new SettingsFragment());
  }

  private void initializeViews() {
    mBackupCounterView = findViewById(R.id.main_backup_count_label);
    String backupCountLabel = mAppQueueViewModel.getBackupCountLabel();
    if (backupCountLabel != null) {
      mBackupCounterView.setText(backupCountLabel);
    }

    mTabLayout = findViewById(R.id.main_tab_layout);
    mViewPager = findViewById(R.id.main_pager);
  }

  private void initializeTabLayout() {
    mTabAdapter = new TabAdapter(this);

    addTabs(mTabAdapter);

    mViewPager.setAdapter(mTabAdapter);

    new TabLayoutMediator(
        mTabLayout, mViewPager,
        (tab, position) -> tab.setText(mTabAdapter.getTabName(position)))
        .attach();
  }

  private void initializeFAButton() {

    mActionPresenter =
        new ActionPresenter(this, R.id.main_floating_action_button);

    int[] searchButtonLayoutIds =
        new int[] {R.id.main_search_button_label, R.id.main_search_button};
    ActionButton searchButton =
        new ActionButton(mActionPresenter, this, searchButtonLayoutIds, false);
    searchButton.assignActiveCallback(
        v
        -> mAppSearchDialogFragment.show(
            getSupportFragmentManager(),
            (mAppSearchDialogFragment.getClass().getSimpleName())));
    searchButton.assignInactiveCallback(
        v
        -> Toast
               .makeText(this, R.string.fetching_data_message,
                         Toast.LENGTH_SHORT)
               .show());

    IAction[] actions = {searchButton};

    mActionPresenter.addActions(actions);

    mActionPresenter.present();
  }

  @Override
  public void onCall() {
    String quantityString = getResources().getQuantityString(
        R.plurals.amount_of_backups, mAppQueueViewModel.getSelectedAppCount());

    String backupCountLabel =
        String.format(quantityString, mAppQueueViewModel.getSelectedAppCount());
    mBackupCounterView.setText(backupCountLabel);

    mAppQueueViewModel.setBackupCountLabel(backupCountLabel);
  }

  @Override
  public int totalAvailableActions() {
    return mActionPresenter.totalAvailableActions();
  }

  @Override
  public void makeActionAvailable(int actionID, boolean flag) {
    mActionPresenter.available(actionID, flag);
  }
}
