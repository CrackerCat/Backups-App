package com.backups.app.ui.actionbuttonactions;

import android.content.res.Resources;
import android.view.View;
import android.widget.Toast;

import com.backups.app.MainActivity;
import com.backups.app.R;
import com.backups.app.ui.actions.IActionButton;
import com.backups.app.ui.actions.IActionButtonMethods;
import com.backups.app.ui.actions.IDefaultAction;
import com.backups.app.ui.fragments.AboutUsDialogFragment;
import com.backups.app.utils.IntentLauncher;

import static com.backups.app.ui.Constants.ABOUT_US_SECTION_BUTTON;
import static com.backups.app.ui.Constants.RATE_APP_BUTTON;
import static com.backups.app.ui.Constants.SHARE_APP_BUTTON;

public final class SettingsFragmentButtonActions
    implements IActionButtonMethods {
  private final MainActivity mParentActivity;

  public SettingsFragmentButtonActions(final MainActivity parentActivity) {
    mParentActivity = parentActivity;
  }

  private void startShareAppAction() {
    Resources resources = mParentActivity.getResources();

    final String shareAppIntentTitle =
        resources.getString(R.string.share_app_intent_title);

    final String shareAppIntentBody =
        resources.getString(R.string.share_app_intent_body);

    final String appStoreLink = "https://playstore.link";

    IntentLauncher.composeShareableMessage(mParentActivity, shareAppIntentTitle,
                                           shareAppIntentBody,
                                           new String[] {appStoreLink});
  }

  @Override
  public void
  initializeActionButtonActions(int position, IActionButton action,
                                final IDefaultAction defaultChecks) {
    action.setAvailability(true);

    View.OnClickListener inactiveAction =
        (defaultChecks != null ? v -> defaultChecks.invoke() : v -> {});

    if (position == ABOUT_US_SECTION_BUTTON) {

      action.assignCallBacks(v -> {
        AboutUsDialogFragment aboutUsFragment = new AboutUsDialogFragment();

        aboutUsFragment.show(mParentActivity.getSupportFragmentManager(),
                             (aboutUsFragment.getClass().getSimpleName()));
      }, inactiveAction);

    } else if (position == RATE_APP_BUTTON) {

      action.assignCallBacks(
          v
          -> Toast.makeText(mParentActivity, "Rate me!", Toast.LENGTH_SHORT)
                 .show(),

          inactiveAction);

    } else if (position == SHARE_APP_BUTTON) {
      action.assignCallBacks(v -> startShareAppAction(), inactiveAction);
    }
  }
}
