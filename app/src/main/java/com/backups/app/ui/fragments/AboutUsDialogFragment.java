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

public final class AboutUsDialogFragment extends DialogFragment {

  private static final class Message {
    public String title;
    public String body;
  }

  private TextView mAppLicenseType;

  private LinearLayout mContactDevView;
  private LinearLayout mSendSuggestionView;
  private LinearLayout mViewSourceCodeView;

  private ImageView mDeveloperGithubLink;
  private ImageView mDesignerGithubLink;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    MaterialAlertDialogBuilder builder =
        new MaterialAlertDialogBuilder(requireActivity());

    View dialogLayout =
        getLayoutInflater().inflate(R.layout.about_us_dialog_fragment, null);

    initializeViews(dialogLayout);

    setupActions();

    builder.setView(dialogLayout);

    return builder.create();
  }

  private Message buildBugReportMessage(final Resources resources) {
    final String bugReportTitle =
        resources.getString(R.string.bug_report_email_subject);
    final String bugReportBody =
        resources.getString(R.string.bug_report_email_body);

    final Message bugReport = new Message();

    bugReport.title = bugReportTitle;
    bugReport.body = bugReportBody;

    return bugReport;
  }

  private Message buildFeatureRequestMessage(final Resources resources) {
    final String featureRequestTitle =
        resources.getString(R.string.feature_request_email_subject);

    final String featureRequestBody =
        resources.getString(R.string.feature_request_email_body);

    final Message featureRequest = new Message();

    featureRequest.title = featureRequestTitle;
    featureRequest.body = featureRequestBody;

    return featureRequest;
  }

  public void initializeViews(View parent) {
    mAppLicenseType = parent.findViewById(R.id.app_license_name_tv);

    mContactDevView = parent.findViewById(R.id.contact_dev_view);

    mSendSuggestionView =
        parent.findViewById(R.id.send_feature_suggestion_view);

    mViewSourceCodeView = parent.findViewById(R.id.view_source_code_view);

    mDeveloperGithubLink = parent.findViewById(R.id.member1_github);

    mDesignerGithubLink = parent.findViewById(R.id.member2_github);
  }

  private void setupGithubLinks(final FragmentActivity parent) {
    final String gplLicenseURL = "https://www.gnu.org/licenses/gpl-3.0.en.html";

    final String developerGithubURL = "https://github.com/flyingsl0ths";

    final String designerGithubURL = "https://github.com/hentai-chan";

    final String projectURL = developerGithubURL + "/Backups-App";

    mAppLicenseType.setOnClickListener(
        v -> IntentLauncher.launchWebPage(parent, gplLicenseURL));

    mDeveloperGithubLink.setOnClickListener(
        v -> IntentLauncher.launchWebPage(parent, developerGithubURL));

    mDesignerGithubLink.setOnClickListener(
        v -> IntentLauncher.launchWebPage(parent, designerGithubURL));

    mViewSourceCodeView.setOnClickListener(
        v -> IntentLauncher.launchWebPage(parent, projectURL));
  }

  private void setupContactLinks(final FragmentActivity parent) {
    final Resources resources = getResources();

    final Message bugReport = buildBugReportMessage(resources);

    final Message featureRequest = buildFeatureRequestMessage(resources);

    final String developerContactEmail = "thebackupsapp@gmail.com";

    mContactDevView.setOnClickListener(
        v
        -> IntentLauncher.composeEmail(parent,
                                       new String[] {developerContactEmail},
                                       bugReport.title, bugReport.body));

    mSendSuggestionView.setOnClickListener(
        v
        -> IntentLauncher.composeEmail(
            parent, new String[] {developerContactEmail}, featureRequest.title,
            featureRequest.body));
  }

  private void setupActions() {
    final FragmentActivity parent = requireActivity();

    setupGithubLinks(parent);

    setupContactLinks(parent);
  }
}
