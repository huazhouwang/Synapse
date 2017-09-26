package io.whz.androidneuralnetwork.element;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import io.whz.androidneuralnetwork.component.App;
import io.whz.androidneuralnetwork.R;

import static io.whz.androidneuralnetwork.util.Precondition.checkNotNull;

public class ExtraWidget {
    private static final String TAG = App.TAG + "-ExtraWidget";

    @IdRes private static final int BASE_CONTAINER_ID = R.id.__extra_container;

//    public static EmptyViewBuilder empty() {
//        return new EmptyViewBuilder();
//    }
//
//    public static ErrorViewBuilder error(@NonNull View.OnClickListener listener) {
//        checkNotNull(listener);
//
//        return new ErrorViewBuilder(listener);
//    }

    public static LoadingViewBuilder loading() {
        return new LoadingViewBuilder();
    }

    public static abstract class BaseViewBuilder {
        private View mReplacedView;

        final View createView(@NonNull LayoutInflater inflater) {
            final ViewGroup container = inflater.inflate(R.layout.extra_widget_base_container, null)
                    .findViewById(BASE_CONTAINER_ID);

            final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            container.setLayoutParams(layoutParams);

            View view = null;

            try {
                view = onCreateView(inflater, container);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (view == null) {
                throw new NullPointerException();
            }

            container.removeAllViews();
            container.addView(view);

            return container;
        }

        protected abstract View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container);

        public BaseViewBuilder replaces(@NonNull View view) {
            mReplacedView = checkNotNull(view);

            return this;
        }

        /**
         * 只支持 FrameLayout、RelativeLayout 和 CoordinatorLayout
         */
        public void attaches(@NonNull ViewGroup layout) {
            checkNotNull(layout);
            ExtraWidget.attaches(layout, mReplacedView, this);
        }
    }

    public static class LoadingViewBuilder extends BaseViewBuilder {
        @Override
        protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
            return inflater.inflate(R.layout.extra_widget_loading, container, false);
        }
    }

//    public static class ErrorViewBuilder extends BaseViewBuilder {
//        private ImageView mPromptImg;
//        private TextView mPromptMsg;
//        private Button mRetryButton;
//
//        private final View.OnClickListener mListener;
//
//        @DrawableRes private int mDrawableRes;
//        @StringRes private int mMsgRes;
//        @StringRes private int mButtonRes;
//
//        private ErrorViewBuilder(@NonNull View.OnClickListener listener) {
//            mListener = listener;
//        }
//
//        public ErrorViewBuilder image(@DrawableRes int res) {
//            mDrawableRes = res;
//
//            return this;
//        }
//
//        public ErrorViewBuilder message(@StringRes int res) {
//            mMsgRes = res;
//
//            return this;
//        }
//
//        public ErrorViewBuilder button(@StringRes int res) {
//            mButtonRes = res;
//
//            return this;
//        }
//
//        @Override
//        protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
//            final View v = inflater.inflate(R.layout.extra_widget_retry, container, false);
//
//            mPromptImg = (ImageView) v.findViewById(R.id.img);
//            mPromptMsg = (TextView) v.findViewById(R.id.msg);
//            mRetryButton = (Button) v.findViewById(R.id.button);
//
//            if (mDrawableRes != 0) {
//                mPromptImg.setImageResource(mDrawableRes);
//            }
//
//            if (mMsgRes != 0) {
//                mPromptMsg.setText(mMsgRes);
//            }
//
//            if (mButtonRes != 0) {
//                mRetryButton.setText(mButtonRes);
//            }
//
//            mRetryButton.setOnClickListener(mListener);
//
//            return v;
//        }
//    }

//    public static class EmptyViewBuilder extends BaseViewBuilder {
//        private ImageView mPromptImg;
//        private TextView mPromptMsg;
//        @DrawableRes private int mDrawableRes;
//        @StringRes private int mStringRes;
//
//        public EmptyViewBuilder image(@DrawableRes int res) {
//            mDrawableRes = res;
//
//            return this;
//        }
//
//        public EmptyViewBuilder message(@StringRes int res) {
//            mStringRes = res;
//
//            return this;
//        }
//
//        @Override
//        protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
//            final View v = inflater.inflate(R.layout.extra_widget_empty, container, false);
//
//            mPromptImg = (ImageView) v.findViewById(R.id.img);
//            mPromptMsg = (TextView) v.findViewById(R.id.msg);
//
//            if (mDrawableRes != 0) {
//                mPromptImg.setImageResource(mDrawableRes);
//            }
//
//            if (mStringRes != 0) {
//                mPromptMsg.setText(mStringRes);
//            }
//
//            return v;
//        }
//    }

    private static void attaches(@NonNull ViewGroup layout, @Nullable View replacedView, @NonNull BaseViewBuilder builder) {
        if (!isSupport(layout)) {
            return;
        }

        final LayoutInflater inflater = LayoutInflater.from(layout.getContext());
        final View son = builder.createView(inflater);
        final View old = layout.findViewById(BASE_CONTAINER_ID);
        final ViewGroup.LayoutParams layoutParams = genLayoutParams(layout);

        TransitionManager.beginDelayedTransition(layout);
        layout.removeView(old);

        if (replacedView != null) {
            layout.removeView(replacedView);
        }

        layout.addView(son, layoutParams);
    }

    public static void recover(@NonNull ViewGroup layout, @Nullable View recoveredView) {
        if (!isSupport(layout)) {
            return;
        }

        final View son = layout.findViewById(BASE_CONTAINER_ID);

        TransitionManager.beginDelayedTransition(layout);

        if (son != null) {
            layout.removeView(son);
        }

        if (recoveredView != null) {
            if (recoveredView.getParent() != null) {
                Log.e(TAG, "Error",new IllegalStateException(
                        String.format("%s already has parent", recoveredView.getClass().getSimpleName())));
                return;
            }

            layout.addView(recoveredView);
        }
    }

    private static boolean isSupport(@NonNull ViewGroup layout) {
        final boolean support = layout instanceof FrameLayout
                || layout instanceof RelativeLayout
                || layout instanceof CoordinatorLayout;

        if (!support) {
            throw new UnsupportedOperationException();
        } else {
            return true;
        }
    }

    private static ViewGroup.LayoutParams genLayoutParams(@NonNull ViewGroup layout) {
        ViewGroup.LayoutParams res = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (layout instanceof FrameLayout) {
            final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(res);
            lp.gravity = Gravity.CENTER;

            res = lp;
        } else if (layout instanceof RelativeLayout) {
            final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(res);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);

            res = lp;
        } else if (layout instanceof CoordinatorLayout) {
            final CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(res);
            lp.gravity = Gravity.CENTER;

            res = lp;
        }

        return res;
    }
}
