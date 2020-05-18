package com.test.verify;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import cn.jiguang.verifysdk.api.AuthPageEventListener;
import cn.jiguang.verifysdk.api.JVerificationInterface;
import cn.jiguang.verifysdk.api.JVerifyUIClickCallback;
import cn.jiguang.verifysdk.api.JVerifyUIConfig;
import cn.jiguang.verifysdk.api.PreLoginListener;
import cn.jiguang.verifysdk.api.VerifyListener;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private TextView tvLog;
    private Button btnGetToken;
    private Button btn_login;
    private Button btnPreLogin;
    private CheckBox isDialogModeCB;
    private boolean autoFinish = true;
    private static final int CENTER_ID = 1000;
    private static final int LOGIN_CODE_UNSET = -1562;
    private static final String LOGIN_CODE = "login_code";
    private static final String LOGIN_CONTENT = "login_content";
    private static final String LOGIN_OPERATOR = "login_operator";
    private static Bundle savedLoginState = new Bundle();
    private static String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private ScreenShotListenManager manager;
    private ImageView iv_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


        manager = ScreenShotListenManager.newInstance(this);
        manager.setListener(
                new ScreenShotListenManager.OnScreenShotListener() {
                    public void onShot(String imagePath) {
                        // do something
                        Log.e(TAG, imagePath);

                        Glide.with(MainActivity.this).load(imagePath).into(iv_img);
                    }
                }
        );

        if (checkPermission()) {
            manager.startListen();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, 100);
            }
        }
    }

    private boolean checkPermission() {
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                granted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    break;
                }
            }
        }
        return granted;
    }

    private void initView() {

        iv_img = (ImageView) findViewById(R.id.iv_img);
        tvLog = (TextView) findViewById(R.id.tv_log);
        tvLog.setOnClickListener(this);
        btnGetToken = (Button) findViewById(R.id.btn_get_token);
        btnGetToken.setOnClickListener(this);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        btnPreLogin = (Button) findViewById(R.id.btn_pre_login);
        btnPreLogin.setOnClickListener(this);
        isDialogModeCB = (CheckBox) findViewById(R.id.cb_dialog_mode);
        findViewById(R.id.cb_auto_finish).setOnClickListener(this);
        findViewById(R.id.btn_get_douyin).setOnClickListener(this);
        findViewById(R.id.btn_get_kuaishou).setOnClickListener(this);
        findViewById(R.id.btn_get_kuaishou_main).setOnClickListener(this);
        findViewById(R.id.btn_get_main).setOnClickListener(this);
        findViewById(R.id.btn_del_pre_login_cache).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != savedLoginState) {
            initLoginResult(savedLoginState);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (null != intent.getExtras()) {
            initLoginResult(intent.getExtras());
        }
    }

    private void initLoginResult(Bundle extras) {
        int loginCode = extras.getInt(LOGIN_CODE, LOGIN_CODE_UNSET);
        if (loginCode != LOGIN_CODE_UNSET) {
            String loginContent = extras.getString(LOGIN_CONTENT);
            String operator = extras.getString(LOGIN_OPERATOR);
            if (null != tvLog) {
                tvLog.setText("[" + loginCode + "]message=" + loginContent + ", operator=" + operator);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "is init success = " + JVerificationInterface.isInitSuccess());
        switch (v.getId()) {
            case R.id.btn_get_token:
                getToken();
                break;
            case R.id.btn_pre_login:

                preLogin();
                break;
            case R.id.btn_login:
                loginAuth(isDialogModeCB.isChecked());
                break;
            case R.id.cb_auto_finish:
                autoFinish = ((CheckBox) v).isChecked();
                break;
            case R.id.btn_get_main://打开抖音个人主页
                String url = "snssdk1128://user/profile/3733569708763603";//跳转到他的个人主页  去关注
                start(url);
                break;
            case R.id.btn_get_douyin://打开抖音作品页
                url = "snssdk1128://aweme/detail/6824072228976594180";
                start(url);
                break;
            case R.id.btn_get_kuaishou://打开快手作品页
//                url = "kwai://work/5243597359195255548";
                url = "kwai://work/5231212467651145015";
                start(url);
                break;
            case R.id.btn_get_kuaishou_main://打开快手个人主页
                url = "kwai://profile/1162429530";
                start(url);
                break;
            case R.id.btn_del_pre_login_cache:
                delPreLoginCache();
                break;
        }
    }

    private void start(String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delPreLoginCache() {
        JVerificationInterface.clearPreLoginCache();
        tvLog.setText("删除成功");
    }

    private String token;

    private void getToken() {
        boolean verifyEnable = JVerificationInterface.checkVerifyEnable(this);
        if (!verifyEnable) {
            tvLog.setText("[2016],msg = 当前网络环境不支持认证");
            return;
        }
        tvLog.setText(null);
        showLoadingDialog();
        JVerificationInterface.getToken(this, 5000, new VerifyListener() {
            @Override
            public void onResult(final int code, final String content, final String operator) {
                savedLoginState = null;
                tvLog.post(new Runnable() {
                    @Override
                    public void run() {
                        if (code == 2000) {
                            token = content;
                            tvLog.setText("[" + code + "]token=" + content + ", operator=" + operator);
                        } else {
                            tvLog.setText("[" + code + "]message=" + content);
                        }
                        dismissLoadingDialog();

                    }
                });
            }
        });
    }

    private void preLogin() {
        boolean verifyEnable = JVerificationInterface.checkVerifyEnable(this);
        if (!verifyEnable) {
            tvLog.setText("[2016],msg = 当前网络环境不支持认证");
            return;
        }
        showLoadingDialog();
        JVerificationInterface.preLogin(this, 5000, new PreLoginListener() {
            @Override
            public void onResult(final int code, final String content) {
                savedLoginState = null;
                tvLog.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "[" + code + "]message=" + content);
                        tvLog.setText("[" + code + "]message=" + content);
                        dismissLoadingDialog();
                    }
                });
            }
        });
    }

    private void loginAuth(boolean isDialogMode) {
        boolean verifyEnable = JVerificationInterface.checkVerifyEnable(this);
        if (!verifyEnable) {
            tvLog.setText("[2016],msg = 当前网络环境不支持认证");
            return;
        }
        showLoadingDialog();

        setUIConfig(isDialogMode);
        JVerificationInterface.loginAuth(this, autoFinish, new VerifyListener() {
            @Override
            public void onResult(final int code, final String content, final String operator) {
                Log.d(TAG, "[" + code + "]message=" + content + ", operator=" + operator);
                Bundle bundle = new Bundle();
                bundle.putInt(LOGIN_CODE, code);
                bundle.putString(LOGIN_CONTENT, content);
                bundle.putString(LOGIN_OPERATOR, operator);
                savedLoginState = bundle;
                //这里通过static bundle保存数据是为了防止出现授权页方向和MainActivity不相同时，MainActivity被销毁重新初始化导致回调数据无法展示到MainActivity
                tvLog.post(new Runnable() {
                    @Override
                    public void run() {
                        tvLog.setText("[" + code + "]message=" + content + ", operator=" + operator);
                        dismissLoadingDialog();
                    }
                });
            }
        }, new AuthPageEventListener() {
            @Override
            public void onEvent(int cmd, String msg) {
                Log.d(TAG, "[onEvent]. [" + cmd + "]message=" + msg);
            }
        });
    }

    private void setUIConfig(boolean isDialogMode) {
        JVerifyUIConfig portrait = getPortraitConfig(isDialogMode);
        JVerifyUIConfig landscape = getLandscapeConfig(isDialogMode);

        //支持授权页设置横竖屏两套config，在授权页中触发横竖屏切换时，sdk自动选择对应的config加载。
        JVerificationInterface.setCustomUIWithConfig(portrait, landscape);
    }

    private JVerifyUIConfig getPortraitConfig(boolean isDialogMode) {
        JVerifyUIConfig.Builder configBuilder = new JVerifyUIConfig.Builder();

        //自定义按钮示例1
        ImageView mBtn = new ImageView(this);
        mBtn.setImageResource(R.drawable.qq);
        RelativeLayout.LayoutParams mLayoutParams1 = new RelativeLayout.LayoutParams(dp2Pix(getApplicationContext(), 40), dp2Pix(getApplicationContext(), 40));
        mLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        //自定义按钮示例2
        ImageView mBtn2 = new ImageView(this);
        mBtn2.setImageResource(R.drawable.weixin);
        RelativeLayout.LayoutParams mLayoutParams2 = new RelativeLayout.LayoutParams(dp2Pix(getApplicationContext(), 40), dp2Pix(getApplicationContext(), 40));
        mLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        ImageView loadingView = new ImageView(getApplicationContext());
        loadingView.setImageResource(R.drawable.umcsdk_load_dot_white);
        RelativeLayout.LayoutParams loadingParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingParam.addRule(RelativeLayout.CENTER_IN_PARENT);
        loadingView.setLayoutParams(loadingParam);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.umcsdk_anim_loading);

        if (isDialogMode) {
            //窗口竖屏
            mLayoutParams1.setMargins(dp2Pix(this, 100), dp2Pix(this, 250.0f), 0, 0);
            mBtn.setLayoutParams(mLayoutParams1);

            mLayoutParams2.setMargins(0, dp2Pix(this, 250.0f), dp2Pix(this, 100.0f), 0);
            mBtn2.setLayoutParams(mLayoutParams2);

            //自定义返回按钮示例
            ImageButton sampleReturnBtn = new ImageButton(getApplicationContext());
            sampleReturnBtn.setImageResource(R.drawable.umcsdk_return_bg);
            RelativeLayout.LayoutParams returnLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            returnLP.setMargins(10, 10, 0, 0);
            sampleReturnBtn.setLayoutParams(returnLP);

            configBuilder.setAuthBGImgPath("main_bg")
                    .setNavColor(0xff0086d0)
                    .setNavText("登录")
                    .setNavTextColor(0xffffffff)
                    .setNavReturnImgPath("umcsdk_return_bg")
                    .setLogoWidth(70)
                    .setLogoHeight(70)
                    .setLogoHidden(false)
                    .setNumberColor(0xff333333)
                    .setLogBtnText("本机号码一键登录")
                    .setLogBtnTextColor(0xffffffff)
                    .setLogBtnImgPath("umcsdk_login_btn_bg")
                    //私条款url为网络网页或本地网页地址(sd卡的地址，需自行申请sd卡读权限)，
                    // 如：assets下路径："file:///android_asset/t.html"，
                    // sd卡下路径："file:"+Environment.getExternalStorageDirectory().getAbsolutePath() +"/t.html"
                    .setAppPrivacyOne("应用自定义服务条款一", "https://www.jiguang.cn/about")
                    .setAppPrivacyTwo("应用自定义服务条款二", "https://www.jiguang.cn/about")
                    .setAppPrivacyColor(0xff666666, 0xff0085d0)
                    .setUncheckedImgPath("umcsdk_uncheck_image")
                    .setCheckedImgPath("umcsdk_check_image")
                    .setSloganTextColor(0xff999999)
                    .setLogoOffsetY(25)
                    .setLogoImgPath("logo_cm")
                    .setNumFieldOffsetY(130)
                    .setSloganOffsetY(160)
                    .setLogBtnOffsetY(184)
                    .setNumberSize(18)
                    .setPrivacyState(false)
                    .setNavTransparent(false)
                    .setPrivacyOffsetY(5)
                    .setDialogTheme(360, 390, 0, 0, false)
                    .setLoadingView(loadingView, animation)
                    .enableHintToast(true, Toast.makeText(getApplicationContext(), "checkbox未选中，自定义提示", Toast.LENGTH_SHORT))
                    .addCustomView(mBtn, true, new JVerifyUIClickCallback() {
                        @Override
                        public void onClicked(Context context, View view) {
                            Toast.makeText(context, "自定义的按钮1，会finish授权页", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addCustomView(mBtn2, false, new JVerifyUIClickCallback() {
                        @Override
                        public void onClicked(Context context, View view) {
                            Toast.makeText(context, "自定义的按钮2，不会finish授权页", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addCustomView(sampleReturnBtn, true, null);
        } else {
            //全屏竖屏
            mLayoutParams1.setMargins(dp2Pix(this, 100), dp2Pix(this, 400.0f), 0, 0);
            mBtn.setLayoutParams(mLayoutParams1);

            mLayoutParams2.setMargins(0, dp2Pix(this, 400.0f), dp2Pix(this, 100.0f), 0);
            mBtn2.setLayoutParams(mLayoutParams2);

            //导航栏按钮示例
            Button navBtn = new Button(this);
            navBtn.setText("导航栏按钮");
            navBtn.setTextColor(0xff3a404c);
            navBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            RelativeLayout.LayoutParams navBtnParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            navBtnParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            navBtn.setLayoutParams(navBtnParam);

            configBuilder.setAuthBGImgPath("main_bg")
                    .setNavColor(0xff0086d0)
                    .setNavText("登录")
                    .setNavTextColor(0xffffffff)
                    .setNavReturnImgPath("umcsdk_return_bg")
                    .setLogoWidth(70)
                    .setLogoHeight(70)
                    .setLogoHidden(false)
                    .setNumberColor(0xff333333)
                    .setLogBtnText("本机号码一键登录")
                    .setLogBtnTextColor(0xffffffff)
                    .setLogBtnImgPath("umcsdk_login_btn_bg")
                    //私条款url为网络网页或本地网页地址(sd卡的地址，需自行申请sd卡读权限)，
                    // 如：assets下路径："file:///android_asset/t.html"，
                    // sd卡下路径："file:"+Environment.getExternalStorageDirectory().getAbsolutePath() +"/t.html"
                    .setAppPrivacyOne("应用自定义服务条款一", "https://www.jiguang.cn/about")
                    .setAppPrivacyTwo("应用自定义服务条款二", "https://www.jiguang.cn/about")
                    .setAppPrivacyColor(0xff666666, 0xff0085d0)
                    .setUncheckedImgPath("umcsdk_uncheck_image")
                    .setCheckedImgPath("umcsdk_check_image")
                    .setSloganTextColor(0xff999999)
                    .setLogoOffsetY(50)
                    .setLogoImgPath("logo_cm")
                    .setNumFieldOffsetY(190)
                    .setSloganOffsetY(220)
                    .setLogBtnOffsetY(254)
                    .setNumberSize(18)
                    .setPrivacyState(false)
                    .setNavTransparent(false)
                    .setPrivacyOffsetY(35)
                    .addCustomView(mBtn, true, new JVerifyUIClickCallback() {
                        @Override
                        public void onClicked(Context context, View view) {
                            Toast.makeText(context, "自定义的按钮1，会finish授权页", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addCustomView(mBtn2, false, new JVerifyUIClickCallback() {
                        @Override
                        public void onClicked(Context context, View view) {
                            Toast.makeText(context, "自定义的按钮2，不会finish授权页", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addNavControlView(navBtn, new JVerifyUIClickCallback() {
                        @Override
                        public void onClicked(Context context, View view) {
                            Toast.makeText(context, "导航栏自定义按钮", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return configBuilder.build();
    }

    private JVerifyUIConfig getLandscapeConfig(boolean isDialogMode) {
        JVerifyUIConfig.Builder configBuilder = new JVerifyUIConfig.Builder();

        //自定义按钮示例1
        ImageView mBtn = new ImageView(this);
        mBtn.setImageResource(R.drawable.qq);
        RelativeLayout.LayoutParams mLayoutParams1 = new RelativeLayout.LayoutParams(dp2Pix(getApplicationContext(), 40), dp2Pix(getApplicationContext(), 40));
        mLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        //自定义按钮示例2
        ImageView mBtn2 = new ImageView(this);
        mBtn2.setImageResource(R.drawable.weixin);
        RelativeLayout.LayoutParams mLayoutParams2 = new RelativeLayout.LayoutParams(dp2Pix(getApplicationContext(), 40), dp2Pix(getApplicationContext(), 40));
        mLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        if (isDialogMode) {
            //窗口横屏
            mLayoutParams1.setMargins(dp2Pix(this, 200), dp2Pix(this, 235.0f), 0, 0);
            mBtn.setLayoutParams(mLayoutParams1);

            mLayoutParams2.setMargins(0, dp2Pix(this, 235.0f), dp2Pix(this, 200.0f), 0);
            mBtn2.setLayoutParams(mLayoutParams2);

            //自定义返回按钮示例
            ImageButton sampleReturnBtn = new ImageButton(getApplicationContext());
            sampleReturnBtn.setImageResource(R.drawable.umcsdk_return_bg);
            RelativeLayout.LayoutParams returnLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            returnLP.setMargins(10, 10, 0, 0);
            sampleReturnBtn.setLayoutParams(returnLP);

            configBuilder.setAuthBGImgPath("main_bg")
                    .setNavColor(0xff0086d0)
                    .setNavText("登录")
                    .setNavTextColor(0xffffffff)
                    .setNavReturnImgPath("umcsdk_return_bg")
                    .setLogoWidth(70)
                    .setLogoHeight(70)
                    .setLogoHidden(false)
                    .setNumberColor(0xff333333)
                    .setLogBtnText("本机号码一键登录")
                    .setLogBtnTextColor(0xffffffff)
                    .setLogBtnImgPath("umcsdk_login_btn_bg")
                    //私条款url为网络网页或本地网页地址(sd卡的地址，需自行申请sd卡读权限)，
                    // 如：assets下路径："file:///android_asset/t.html"，
                    // sd卡下路径："file:"+Environment.getExternalStorageDirectory().getAbsolutePath() +"/t.html"
                    .setAppPrivacyOne("应用自定义服务条款一", "https://www.jiguang.cn/about")
                    .setAppPrivacyTwo("应用自定义服务条款二", "https://www.jiguang.cn/about")
                    .setAppPrivacyColor(0xff666666, 0xff0085d0)
                    .setUncheckedImgPath("umcsdk_uncheck_image")
                    .setCheckedImgPath("umcsdk_check_image")
                    .setSloganTextColor(0xff999999)
                    .setLogoOffsetY(25)
                    .setLogoImgPath("logo_cm")
                    .setNumFieldOffsetY(120)
                    .setSloganOffsetY(155)
                    .setLogBtnOffsetY(180)
                    .setPrivacyOffsetY(10)
                    .setDialogTheme(500, 350, 0, 0, false)
                    .enableHintToast(true, null)
                    .addCustomView(mBtn, true, new JVerifyUIClickCallback() {
                        @Override
                        public void onClicked(Context context, View view) {
                            Toast.makeText(context, "自定义的按钮1，会finish授权页", Toast.LENGTH_SHORT).show();
                        }
                    }).addCustomView(mBtn2, false, new JVerifyUIClickCallback() {
                @Override
                public void onClicked(Context context, View view) {
                    Toast.makeText(context, "自定义的按钮2，不会finish授权页", Toast.LENGTH_SHORT).show();
                }
            })
                    .addCustomView(sampleReturnBtn, true, null);
        } else {
            //全屏横屏
            mLayoutParams1.setMargins(dp2Pix(this, 200), dp2Pix(this, 100.0f), 0, 0);
            mBtn.setLayoutParams(mLayoutParams1);

            mLayoutParams2.setMargins(0, dp2Pix(this, 100.0f), dp2Pix(this, 200.0f), 0);
            mBtn2.setLayoutParams(mLayoutParams2);

            //导航栏按钮示例
            Button navBtn = new Button(this);
            navBtn.setText("导航栏按钮");
            navBtn.setTextColor(0xff3a404c);
            navBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            RelativeLayout.LayoutParams navBtnParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            navBtnParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            navBtn.setLayoutParams(navBtnParam);

            configBuilder
                    .setAuthBGImgPath("main_bg")
                    .setNavColor(0xff0086d0)
                    .setNavText("登录")
                    .setNavTextColor(0xffffffff)
                    .setNavReturnImgPath("umcsdk_return_bg")
                    .setLogoWidth(70)
                    .setLogoHeight(70)
                    .setLogoHidden(false)
                    .setNumberColor(0xff333333)
                    .setLogBtnText("本机号码一键登录")
                    .setLogBtnTextColor(0xffffffff)
                    .setLogBtnImgPath("umcsdk_login_btn_bg")
                    //私条款url为网络网页或本地网页地址(sd卡的地址，需自行申请sd卡读权限)，
                    // 如：assets下路径："file:///android_asset/t.html"，
                    // sd卡下路径："file:"+Environment.getExternalStorageDirectory().getAbsolutePath() +"/t.html"
                    .setAppPrivacyOne("应用自定义服务条款一", "https://www.jiguang.cn/about")
                    .setAppPrivacyTwo("应用自定义服务条款二", "https://www.jiguang.cn/about")
                    .setAppPrivacyColor(0xff666666, 0xff0085d0)
                    .setUncheckedImgPath("umcsdk_uncheck_image")
                    .setCheckedImgPath("umcsdk_check_image")
                    .setSloganTextColor(0xff999999)
                    .setLogoOffsetY(30)
                    .setLogoImgPath("logo_cm")
                    .setNumFieldOffsetY(150)
                    .setSloganOffsetY(185)
                    .setLogBtnOffsetY(210)
                    .setPrivacyOffsetY(10)
                    .addCustomView(mBtn, true, new JVerifyUIClickCallback() {
                        @Override
                        public void onClicked(Context context, View view) {
                            Toast.makeText(context, "自定义的按钮1，会finish授权页", Toast.LENGTH_SHORT).show();
                        }
                    }).addCustomView(mBtn2, false, new JVerifyUIClickCallback() {
                @Override
                public void onClicked(Context context, View view) {
                    Toast.makeText(context, "自定义的按钮2，不会finish授权页", Toast.LENGTH_SHORT).show();
                }
            })
                    .addNavControlView(navBtn, new JVerifyUIClickCallback() {
                        @Override
                        public void onClicked(Context context, View view) {
                            Toast.makeText(context, "导航栏自定义按钮", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return configBuilder.build();
    }


    private AlertDialog alertDialog;

    public void showLoadingDialog() {
        dismissLoadingDialog();
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        alertDialog.setCancelable(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK)
                    return true;
                return false;
            }
        });
        alertDialog.show();
        alertDialog.setContentView(R.layout.loading_alert);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    public void dismissLoadingDialog() {
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private int dp2Pix(Context context, float dp) {
        try {
            float density = context.getResources().getDisplayMetrics().density;
            return (int) (dp * density + 0.5F);
        } catch (Exception e) {
            return (int) dp;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadingDialog();
        manager.stopListen();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        for (int grantResult : grantResults) {
//            if (grantResult != PackageManager.PERMISSION_GRANTED) {//说明全部都有了吗？
////                if (checkOverlayPermission()) {
//
////                }
//
//                return;
//            }
//        }
        if (requestCode == 100) {
            manager.startListen();
        }

    }

}
