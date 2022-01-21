package com.backups.app.ui.actionsets;

import static com.backups.app.Constants.ABOUT_US_SECTION_BUTTON;
import static com.backups.app.Constants.RATE_APP_BUTTON;
import static com.backups.app.Constants.SHARE_APP_BUTTON;

import android.content.res.Resources;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.backups.app.R;
import com.backups.app.ui.actions.DefaultAction;
import com.backups.app.ui.actions.IAction;
import com.backups.app.ui.actions.IActionSetFunctionality;
import com.backups.app.ui.fragments.AboutUsDialogFragment;
import com.backups.app.utils.IntentLauncher;

public final class SettingsActions implements IActionSetFunctionality {

  private final FragmentActivity mParentActivity;

  public SettingsActions(final FragmentActivity activity) {
    mParentActivity = activity;
  }

  @Override
  public void setup(int position, IAction action, DefaultAction defaultAction) {
    action.setAvailability(true);
    if (position == ABOUT_US_SECTION_BUTTON) {

      action.assignCallBacks(v -> displayAboutUsSection(), v -> {});

    } else if (position == RATE_APP_BUTTON) {

      action.assignCallBacks(
          v
          -> Toast.makeText(mParentActivity, mParentActivity.getString(R.string.rate_app_message), Toast.LENGTH_SHORT)
                 .show(),
          v -> {});

    } else if (position == SHARE_APP_BUTTON) {
      action.assignCallBacks(v -> launchShareIntent(), v -> {});
    }
  }

  private void launchShareIntent() {
    final Resources resources = mParentActivity.getResources();

    final String shareAppIntentTitle =
        resources.getString(R.string.share_app_intent_title);

    final String shareAppIntentBody =
        resources.getString(R.string.share_app_intent_body);

    final String appStoreLink = "https://github.com/flyingsl0ths/Backups-App";

    IntentLauncher.composeShareableMessage(mParentActivity, shareAppIntentTitle,
                                           shareAppIntentBody,
                                           new String[] {appStoreLink});
  }

  private void displayAboutUsSection() {
    final AboutUsDialogFragment aboutUsFragment = new AboutUsDialogFragment();

    aboutUsFragment.show(mParentActivity.getSupportFragmentManager(),
                         (aboutUsFragment.getClass().getSimpleName()));
  }
}
