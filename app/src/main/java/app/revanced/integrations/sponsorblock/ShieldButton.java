package app.revanced.integrations.sponsorblock;

import android.content.Context;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

import static app.revanced.integrations.sponsorblock.PlayerController.getCurrentVideoLength;
import static app.revanced.integrations.sponsorblock.PlayerController.getLastKnownVideoTime;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ShieldButton {
    static RelativeLayout _youtubeControlsLayout;
    static WeakReference<ImageView> _shieldBtn = new WeakReference<>(null);
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    static boolean isShowing;

    public static void initialize(Object viewStub) {
        try {
            LogHelper.debug(ShieldButton.class, "initializing shield button");

            _youtubeControlsLayout = (RelativeLayout) viewStub;

            ImageView imageView = (ImageView) _youtubeControlsLayout
                    .findViewById(getIdentifier("sponsorblock_button", "id"));

            if (imageView == null) {
                LogHelper.debug(ShieldButton.class, "Couldn't find imageView with \"sponsorblock_button\"");
            }
            if (imageView == null) return;
            imageView.setOnClickListener(SponsorBlockUtils.sponsorBlockBtnListener);
            _shieldBtn = new WeakReference<>(imageView);

            // Animations
            fadeDurationFast = getInteger("fade_duration_fast");
            fadeDurationScheduled = getInteger("fade_duration_scheduled");
            fadeIn = getAnimation("fade_in");
            fadeIn.setDuration(fadeDurationFast);
            fadeOut = getAnimation("fade_out");
            fadeOut.setDuration(fadeDurationScheduled);
            isShowing = true;
            changeVisibilityImmediate(false);
        } catch (Exception ex) {
            LogHelper.printException(ShieldButton.class, "Unable to set RelativeLayout", ex);
        }
    }

    public static void changeVisibilityImmediate(boolean visible) {
        changeVisibility(visible, true);
    }

    public static void changeVisibilityNegatedImmediate(boolean visible) {
        changeVisibility(!visible, true);
    }

    public static void changeVisibility(boolean visible) {
        changeVisibility(visible, false);
    }

    public static void changeVisibility(boolean visible, boolean immediate) {
        if (isShowing == visible) return;
        isShowing = visible;

        ImageView iView = _shieldBtn.get();
        if (_youtubeControlsLayout == null || iView == null) return;

        if (visible && shouldBeShown()) {
            if (getLastKnownVideoTime() >= getCurrentVideoLength()) {
                return;
            }
            LogHelper.debug(ShieldButton.class, "Fading in");

            iView.setVisibility(View.VISIBLE);
            if (!immediate)
                iView.startAnimation(fadeIn);
            return;
        }

        if (iView.getVisibility() == View.VISIBLE) {
            LogHelper.debug(ShieldButton.class, "Fading out");
            if (!immediate)
                iView.startAnimation(fadeOut);
            iView.setVisibility(shouldBeShown() ? View.INVISIBLE : View.GONE);
        }
    }

    static boolean shouldBeShown() {
        return SponsorBlockUtils.isSettingEnabled(SponsorBlockSettings.isAddNewSegmentEnabled);
    }

    //region Helpers
    private static int getIdentifier(String name, String defType) {
        Context context = ReVancedUtils.getContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    private static int getInteger(String name) {
        return ReVancedUtils.getContext().getResources().getInteger(getIdentifier(name, "integer"));
    }

    private static Animation getAnimation(String name) {
        return AnimationUtils.loadAnimation(ReVancedUtils.getContext(), getIdentifier(name, "anim"));
    }
    //endregion
}
