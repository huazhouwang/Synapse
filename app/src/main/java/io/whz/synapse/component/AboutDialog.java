package io.whz.synapse.component;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;

import io.whz.synapse.BuildConfig;
import io.whz.synapse.R;
import io.whz.synapse.pojo.constant.TrackCons;
import io.whz.synapse.track.Tracker;

public class AboutDialog extends DialogFragment implements View.OnClickListener {
    private static final String APP_IN_GOOGLE_PLAY =
            "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(R.string.text_about)
                .setMessage(R.string.text_dialog_about_msg)
                .setView(inflateCustom())
                .setPositiveButton(R.string.text_dialog_about_positive, null);

        return builder.create();
    }

    private View inflateCustom() {
        final View view = View.inflate(getContext(), R.layout.dialog_about, null);

        view.findViewById(R.id.github).setOnClickListener(this);
        view.findViewById(R.id.rate).setOnClickListener(this);
        view.findViewById(R.id.share).setOnClickListener(this);

        final ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        view.setLayoutParams(lp);

        return view;
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();

        switch (id) {
            case R.id.github:
                handleGitHubAction();
                break;

            case R.id.rate:
                handleRateAction();
                break;

            case R.id.share:
                handleShareAction();
                break;

            default:
                break;
        }
    }

    private void handleGitHubAction() {
        final Activity activity = getActivity();

        if (activity == null || activity.isFinishing()) {
            return;
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/huazhouwang/Synapse"));
        final PackageManager manager = activity.getPackageManager();

        if (intent.resolveActivity(manager) != null) {
            activity.startActivity(intent);
        }

        Tracker.getInstance()
                .logEvent(TrackCons.About.CLICK_GITHUB);
    }

    private void handleRateAction() {
        final Activity activity = getActivity();

        if (activity == null || activity.isFinishing()) {
            return;
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_IN_GOOGLE_PLAY));
        final PackageManager manager = activity.getPackageManager();

        if (intent.resolveActivity(manager) != null) {
            activity.startActivity(intent);
        }

        Tracker.getInstance()
                .logEvent(TrackCons.About.CLICK_RATE);
    }

    private void handleShareAction() {
        ShareCompat.IntentBuilder.from(getActivity())
                .setChooserTitle(R.string.text_share_chooser_title)
                .setSubject(getString(R.string.text_share_subject))
                .setText(getString(R.string.text_share_text) + " " + APP_IN_GOOGLE_PLAY)
                .setType("text/plain")
                .startChooser();

        Tracker.getInstance()
                .logEvent(TrackCons.About.CLICK_SHARE);
    }
}
