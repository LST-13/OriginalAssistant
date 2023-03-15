package fun.qianxiao.originalassistant.fragment.me;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.InputConfirmPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import fun.qianxiao.originalassistant.MainActivity;
import fun.qianxiao.originalassistant.activity.AboutActivity;
import fun.qianxiao.originalassistant.activity.SettingsActivity;
import fun.qianxiao.originalassistant.api.hlx.HLXApiManager;
import fun.qianxiao.originalassistant.base.BaseActivity;
import fun.qianxiao.originalassistant.base.BaseFragment;
import fun.qianxiao.originalassistant.bean.HLXUserInfo;
import fun.qianxiao.originalassistant.config.SPConstants;
import fun.qianxiao.originalassistant.databinding.FragmentMeBinding;
import fun.qianxiao.originalassistant.view.loading.ILoadingView;

/**
 * MeFragment
 *
 * @Author QianXiao
 * @Date 2023/3/10
 */
public class MeFragment<A extends BaseActivity<?>> extends BaseFragment<FragmentMeBinding, A> implements ILoadingView {
    private InputConfirmPopupView keyInputConfirmPopupView;
    private InputConfirmPopupView userIdInputConfirmPopupView;

    @Override
    protected void initListener() {
        binding.tvNick.setOnClickListener(v -> showLogin());
        binding.tvSignin.setOnClickListener(v -> signIn());
        binding.tvId.setOnClickListener(v -> copyId());
        binding.ivAvatar.setOnClickListener(v -> binding.tvId.performClick());
        binding.llAbout.setOnClickListener(v -> {
            ActivityUtils.startActivity(new Intent(activity, AboutActivity.class));
        });
        binding.llSetting.setOnClickListener(v -> {
            ActivityUtils.startActivity(new Intent(activity, SettingsActivity.class));
        });
        binding.llSupport.setOnClickListener(v -> {
            ToastUtils.showShort("支持");
        });
        binding.llHelp.setOnClickListener(v -> {
            ToastUtils.showShort("帮助");
        });

        KeyboardUtils.registerSoftInputChangedListener(activity, height -> {
            if (keyInputConfirmPopupView != null) {
                XPopupUtils.moveUpToKeyboard(height + 100, keyInputConfirmPopupView);
            }
            if (userIdInputConfirmPopupView != null) {
                XPopupUtils.moveUpToKeyboard(height + 100, userIdInputConfirmPopupView);
            }
        });
    }

    private void baseInputXPopViewShow(InputConfirmPopupView popupView) {
        TextView tvContent = popupView.getPopupContentView().findViewById(com.lxj.xpopup.R.id.tv_content);
        tvContent.setGravity(Gravity.START);
        popupView.popupInfo.autoDismiss = false;
        popupView.show();
        ThreadUtils.runOnUiThreadDelayed(() -> popupView.getCancelTextView().setOnClickListener(v -> {
            baseInputXPopViewDismiss(popupView);
        }), 100);
    }

    private void baseInputXPopViewDismiss(InputConfirmPopupView popupView) {
        KeyboardUtils.hideSoftInput(popupView.getEditText());
        ThreadUtils.runOnUiThreadDelayed(popupView::dismiss, 50);
    }

    private void showLogin() {
        if (true) {
            keyInputConfirmPopupView = new XPopup.Builder(activity).asInputConfirm(
                    "Key登录", "可使用抓包软件抓包获取，见请求字段'_key'，长度112位", "", "请输入Key",
                    text -> {
                        final int KEY_VALID_LENGTH = 112;
                        if (TextUtils.isEmpty(text)) {
                            ToastUtils.showShort("输入为空");
                        } else if (text.length() != KEY_VALID_LENGTH) {
                            ToastUtils.showShort("Key长度应为112位");
                        } else {
                            HLXApiManager.INSTANCE.checkKey(text, (valid, errMsg) -> {
                                if (valid) {
                                    SPUtils.getInstance().put(SPConstants.KEY_HLX_KEY, text);
                                    baseInputXPopViewDismiss(keyInputConfirmPopupView);
                                    loginingByKey();
                                } else {
                                    ToastUtils.showShort(errMsg);
                                }
                            });
                        }
                    });
            baseInputXPopViewShow(keyInputConfirmPopupView);
        }
    }

    private void loginingByKey() {
        userIdInputConfirmPopupView = new XPopup.Builder(activity).asInputConfirm(
                "UserID", "当前不支持通过key获取用户信息，可使用抓包软件抓包获取，见请求字段'user_id'", "", "请输入user_id",
                userId -> {
                    if (TextUtils.isEmpty(userId)) {
                        ToastUtils.showShort("输入为空");
                    } else {
                        openLoadingDialog("登录中");
                        HLXApiManager.INSTANCE.getUserInfo(
                                SPUtils.getInstance().getString(SPConstants.KEY_HLX_KEY),
                                userId,
                                new HLXApiManager.OnGetUserInfoResult() {
                                    @Override
                                    public void onResult(boolean success, HLXUserInfo hlxUserInfo, String errMsg) {
                                        closeLoadingDialog();
                                        if (success) {
                                            ToastUtils.showShort("登录成功 " + hlxUserInfo.getAvatarUrl());
                                            baseInputXPopViewDismiss(userIdInputConfirmPopupView);
                                            displayUserInfo(hlxUserInfo);
                                        } else {
                                            ToastUtils.showShort(errMsg);
                                        }
                                    }
                                });
                    }
                });
        baseInputXPopViewShow(userIdInputConfirmPopupView);
    }

    private void signIn() {
        ToastUtils.showShort("签到");
    }

    private void copyId() {
        Object tag = binding.tvId.getTag();
        if (tag != null) {
            ClipboardUtils.copyText((CharSequence) tag);
            ToastUtils.showShort("ID已复制至剪贴板");
        }
    }

    @Override
    protected void initData() {

    }

    @SuppressLint("SetTextI18n")
    private void displayUserInfo(HLXUserInfo userInfo) {
        binding.tvId.setText("ID: " + userInfo.getUserId());
        binding.tvId.setTag(String.valueOf(userInfo.getUserId()));
        binding.tvNick.setText(userInfo.getNick());
        Glide.with(binding.ivAvatar).load(userInfo.getAvatarUrl()).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(binding.ivAvatar);
    }

    @Override
    public void openLoadingDialog(String msg) {
        ((MainActivity) activity).openLoadingDialog(msg);
    }

    @Override
    public void closeLoadingDialog() {
        ((MainActivity) activity).closeLoadingDialog();
    }
}
