package com.backups.app.ui.fragments;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.backups.app.R;
import com.backups.app.utils.IntentLauncher;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AboutUsDialogFragment extends DialogFragment {
  private TextView mAppLicenseType;

  private LinearLayout mContactDevView;
  private LinearLayout mSendSuggestionView;
  private LinearLayout mViewSourceCodeView;

  private ImageView mMember1GithubLink;
  private ImageView mMember2GithubLink;

  private final String mGPLLicenseURL =
      "https://www.gnu.org/licenses/gpl-3.0.en.html";

  private final String mMember1GithubURL = "https://github.com/flyingsl0ths";

  private final String mMember2GithubURL = "https://github.com/hentai-chan";

  private final String mProjectURL = mMember1GithubURL + "/Backups-App";

  private final String mDeveloperContactEmail = "thebackupsapp@gmail.com";

  private String mFeatureRequestTitle;
  private String mFeatureRequestBody;
  private String mBugReportTitle;
  private String mBugReportBody;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    MaterialAlertDialogBuilder builder =
        new MaterialAlertDialogBuilder(requireActivity());

    View dialogLayout =
        getLayoutInflater().inflate(R.layout.about_us_dialog_fragment, null);

    initializeViews(dialogLayout);

    loadContactEmailStrings();

    setupViews();

    builder.setView(dialogLayout);

    return builder.create();
  }

  private void loadContactEmailStrings() {
    Resources resources = getResources();

    mFeatureRequestTitle =
        resources.getString(R.string.feature_request_email_subject);
    mFeatureRequestBody =
        resources.getString(R.string.feature_request_email_body);
    mBugReportTitle = resources.getString(R.string.bug_report_email_subject);
    mBugReportBody = resources.getString(R.string.bug_report_email_body);
  }

  public void initializeViews(View parent) {
    mAppLicenseType = parent.findViewById(R.id.app_license_name_tv);

    mContactDevView = parent.findViewById(R.id.contact_dev_view);

    mSendSuggestionView =
        parent.findViewById(R.id.send_feature_suggestion_view);

    mViewSourceCodeView = parent.findViewById(R.id.view_source_code_view);

    mMember1GithubLink = parent.findViewById(R.id.member1_github);

    mMember2GithubLink = parent.findViewById(R.id.member2_github);
  }

  private void setupViews() {
    FragmentActivity parent = requireActivity();

    mAppLicenseType.setOnClickListener(
        v -> IntentLauncher.launchWebPage(parent, mGPLLicenseURL));

    mMember1GithubLink.setOnClickListener(
        v -> IntentLauncher.launchWebPage(parent, mMember1GithubURL));

    mMember2GithubLink.setOnClickListener(
        v -> IntentLauncher.launchWebPage(parent, mMember2GithubURL));

    mContactDevView.setOnClickListener(
        v
        -> IntentLauncher.composeEmail(parent,
                                       new String[] {mDeveloperContactEmail},
                                       mBugReportTitle, mBugReportBody));

    mSendSuggestionView.setOnClickListener(
        v
        -> IntentLauncher.composeEmail(
            parent, new String[] {mDeveloperContactEmail}, mFeatureRequestTitle,
            mFeatureRequestBody));

    mViewSourceCodeView.setOnClickListener(
        v -> IntentLauncher.launchWebPage(parent, mProjectURL));
  }
}
