package com.daikuanchaoshi.daikuanguowai.ui.act.mainfgt.home.act;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.daikuanchaoshi.daikuanguowai.R;
import com.daikuanchaoshi.daikuanguowai.commt.BaseAct;
import com.daikuanchaoshi.daikuanguowai.commt.MyApplication;
import com.gyf.barlibrary.ImmersionBar;
import com.lykj.aextreme.afinal.utils.Debug;
import com.lykj.aextreme.afinal.utils.MyToast;
import com.maning.updatelibrary.InstallUtils;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class Act_Web extends BaseAct {
    private WebView webView;
    private String url;
    private TextView tvTitle;
    //关于进度显示
    private ProgressDialog progressDialog;

    @Override
    public int initLayoutId() {
        return R.layout.act_web;
    }

    @Override
    public void initView() {
        hideHeader();
        //绑定初始化ButterKnife
        ButterKnife.bind(this);
        ImmersionBar.with(this)
                .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
                .init();
        webView = (WebView) findViewById(R.id.web);
        tvTitle = findViewById(R.id.tv_title);
        setWebView();
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        if (intent.getStringExtra("tvtitle") != null) {
            tvTitle.setText(intent.getStringExtra("tvtitle"));
            url=url+"?token="+ MyApplication.getLognBean().getMember().getToken()+"";
            Debug.e("新-------url===="+url);
        }
        webView.setWebChromeClient(new WebChromeClient());
        Map<String, String> map = new HashMap<String, String>();
        map.put("token", MyApplication.getLognBean().getMember().getToken());
        webView.loadUrl(url, map);
        findViewById(R.id.logon_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getIntent().getStringExtra("title") != null && getIntent().getStringExtra("title").equals("no")) {
                    setResult(10);
                    finish();
                } else {
                    finish();
                }
            }
        });
    }

    private void installApk(String path) {
        //安装APK
        InstallUtils.installAPK(Act_Web.this, path, new InstallUtils.InstallCallBack() {
            @Override
            public void onSuccess() {
                //onSuccess：表示系统的安装界面被打开
                //防止用户取消安装，在这里可以关闭当前应用，以免出现安装被取消
                Toast.makeText(Act_Web.this, "正在安装程序", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(Exception e) {
                //安装出现异常，这里可以提示用用去用浏览器下载安装
                InstallUtils.installAPKWithBrower(Act_Web.this, url);
            }
        });
    }

    @Override
    public void initData() {

    }

    @Override
    public void updateUI() {

    }

    @Override
    public void onNoInterNet() {

    }

    public void setWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUseWideViewPort(true);//关键点
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptEnabled(true); // 设置支持javascript脚本
        webSettings.setAllowFileAccess(true); // 允许访问文件
        webSettings.setBuiltInZoomControls(true); // 设置显示缩放按钮
        webSettings.setSupportZoom(true); // 支持缩放
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String newurl) {
                try {
                    //处理intent协议
                    if (newurl.startsWith("intent://")) {
                        Intent intent;
                        try {
                            intent = Intent.parseUri(newurl, Intent.URI_INTENT_SCHEME);
                            intent.addCategory("android.intent.category.BROWSABLE");
                            intent.setComponent(null);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                                intent.setSelector(null);
                            }
                            List<ResolveInfo> resolves = context.getPackageManager().queryIntentActivities(intent, 0);
                            if (resolves.size() > 0) {
                                startActivityIfNeeded(intent, -1);
                            }
                            return true;
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    // 处理自定义scheme协议
                    if (!newurl.startsWith("http")) {
                        MyToast.show(getApplicationContext(), "处理自定义scheme-->" + newurl);
                        try {
                            // 以下固定写法
                            final Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(newurl));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        } catch (Exception e) {
                            // 防止没有安装的情况
                            e.printStackTrace();
                            MyToast.show(getApplicationContext(), "您所打开的第三方App未安装！");
                        }
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return super.shouldOverrideUrlLoading(view, newurl);
            }
        });
//相关属性
//        progressDialog = new ProgressDialog(Act_Web.this);
//        progressDialog.setMessage("正在下载...");
//        progressDialog.setIndeterminate(true);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressDialog.setCancelable(true);
    }

    public void launchAppDetail(String url) {    //appPkg 是应用的包名
        final String GOOGLE_PLAY = "com.android.vending";//这里对应的是谷歌商店，跳转别的商店改成对应的即可
        try {
            if (TextUtils.isEmpty(url))
                return;
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(GOOGLE_PLAY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
        }
    }
}
