package com.simpleryo.leyotang.app;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.annotation.Encoding;
import com.okhttplib.cookie.PersistentCookieJar;
import com.okhttplib.cookie.cache.SetCookieCache;
import com.okhttplib.cookie.persistence.SharedPrefsCookiePersistor;
import com.simpleryo.leyotang.R;
import com.simpleryo.leyotang.activity.MainActivity;
import com.simpleryo.leyotang.activity.MyMsgActivity;
import com.simpleryo.leyotang.push.UmengNotificationService;
import com.simpleryo.leyotang.utils.HttpInterceptor;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.umeng.message.inapp.InAppMessageManager;
import com.umeng.socialize.PlatformConfig;

import net.latipay.mobile.LatipayAPI;

import org.android.agoo.huawei.HuaWeiRegister;
import org.xutils.BuildConfig;
import org.xutils.x;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author huanglei
 * @ClassNname：SimpleryoApplication.java
 * @Describe 应用application
 * @time 2018/3/19 10:52
 */
public class SimpleryoApplication extends MultiDexApplication {
    private static final String TAG = SimpleryoApplication.class.getName();
    public static final String UPDATE_STATUS_ACTION = "com.simpleryo.nz.action.UPDATE_STATUS";
    private Handler handler;
    public static String GOOGLEAPIKEY = "AIzaSyD-9vX_TpuLt0Mz2cM6CAJe6i8OEbfWbGE";
    String downloadFileDir = Environment.getExternalStorageDirectory().getPath()+"/okHttp_download/";
    String cacheDir = Environment.getExternalStorageDirectory().getPath()+"/okHttp_cache";
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(SimpleryoApplication.this);
        x.Ext.setDebug(BuildConfig.DEBUG);//是否输出debug日志，开启debug会影响性能。
        //TODO: setup apiKey, userId, walletId first
        LatipayAPI.setup("126vzmg8M7", "U000000348", "W000000381");
        //设置LOG开关，默认为false
        UMConfigure.setLogEnabled(true);
        try {
            Class<?> aClass = Class.forName("com.umeng.commonsdk.UMConfigure");
            Field[] fs = aClass.getDeclaredFields();
            for (Field f : fs) {
                Log.e("xxxxxx", "ff=" + f.getName() + "   " + f.getType().getName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //初始化组件化基础库, 统计SDK/推送SDK/分享SDK都必须调用此初始化接口
        UMConfigure.init(this, "5ad84bdd8f4a9d5761000069", "Simpleryo", UMConfigure.DEVICE_TYPE_PHONE,
                "195cd57308d0d88adb1c6578b25f5345");
        InAppMessageManager.getInstance(this).setInAppMsgDebugMode(true);
        initUpush();
        HuaWeiRegister.register(SimpleryoApplication.this);
        //建议在application 的onCreate()的方法中调用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        OkHttpUtil.init(this)
                .setConnectTimeout(15)//连接超时时间
                .setWriteTimeout(15)//写超时时间
                .setReadTimeout(15)//读超时时间
                .setMaxCacheSize(10 * 1024 * 1024)//缓存空间大小
                .setCacheType(CacheType.FORCE_NETWORK)//缓存类型
                .setHttpLogTAG("HttpLog")//设置请求日志标识
                .setIsGzip(false)//Gzip压缩，需要服务端支持
                .setShowHttpLog(true)//显示请求日志
                .setShowLifecycleLog(false)//显示Activity销毁日志
                .setRetryOnConnectionFailure(false)//失败后不自动重连
                .setCachedDir(new File(cacheDir))//设置缓存目录
                .setDownloadFileDir(downloadFileDir)//文件下载保存目录
                .setResponseEncoding(Encoding.UTF_8)//设置全局的服务器响应编码
                .setRequestEncoding(Encoding.UTF_8)//设置全局的请求参数编码
                .addResultInterceptor(HttpInterceptor.ResultInterceptor)//请求结果拦截器
                .addExceptionInterceptor(HttpInterceptor.ExceptionInterceptor)//请求链路异常拦截器
                .setCookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this)))//持久化cookie
                .build();
    }

    private void initUpush() {
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.setPushCheck(true);
        handler = new Handler(getMainLooper());

        //sdk开启通知声音
        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);

        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            /**
             * 通知的回调方法（通知送达时会回调）
             */
            @Override
            public void dealWithNotificationMessage(Context context, UMessage msg) {
                //调用super，会展示通知，不调用super，则不展示通知。
                super.dealWithNotificationMessage(context, msg);
            }

            /**
             * 自定义消息的回调方法
             */
            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        // 对自定义消息的处理方式，点击或者忽略
                        boolean isClickOrDismissed = true;
                        if (isClickOrDismissed) {
                            //自定义消息的点击统计
                            UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
                        } else {
                            //自定义消息的忽略统计
                            UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
                        }
                        Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
                    }
                });
            }

            /**
             * 自定义通知栏样式的回调方法
             */
            @Override
            public Notification getNotification(Context context, UMessage msg) {
                switch (msg.builder_id) {
                    case 1:
                        Notification.Builder builder = new Notification.Builder(context);
                        RemoteViews myNotificationView = new RemoteViews(context.getPackageName(),
                                R.layout.notification_view);
                        myNotificationView.setTextViewText(R.id.notification_title, msg.title);
                        myNotificationView.setTextViewText(R.id.notification_text, msg.text);
                        myNotificationView.setImageViewBitmap(R.id.notification_large_icon, getLargeIcon(context, msg));
                        myNotificationView.setImageViewResource(R.id.notification_small_icon,
                                getSmallIconId(context, msg));
                        builder.setContent(myNotificationView)
                                .setSmallIcon(getSmallIconId(context, msg))
                                .setTicker(msg.ticker)
                                .setAutoCancel(true);

                        return builder.getNotification();
                    default:
                        //默认为0，若填写的builder_id并不存在，也使用默认。
                        return super.getNotification(context, msg);
                }
            }
        };
        mPushAgent.setMessageHandler(messageHandler);

        /**
         * 自定义行为的回调处理，参考文档：高级功能-通知的展示及提醒-自定义通知打开动作
         * UmengNotificationClickHandler是在BroadcastReceiver中被调用，故
         * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
         * */
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {

            @Override
            public void launchApp(Context context, UMessage msg) {
                super.launchApp(context, msg);
                Map<String, String> extrs = msg.extra;
//                courseClass 课程开课
//                payComplete 支付完成
//                sysMessage 系统消息
                if (extrs.get("type").equalsIgnoreCase("courseClass")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("type","push");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                if (extrs.get("type").equalsIgnoreCase("payComplete")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("type","push");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                if (extrs.get("type").equalsIgnoreCase("sysMessage")) {
                    Intent intent = new Intent(context, MyMsgActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                Log.w("cc", "推送消息：" + msg.getRaw().toString());
            }

            @Override
            public void openUrl(Context context, UMessage msg) {
                super.openUrl(context, msg);
            }

            @Override
            public void openActivity(Context context, UMessage msg) {
                super.openActivity(context, msg);
                Map<String, String> extrs = msg.extra;
//                courseClass 课程开课
//                payComplete 支付完成
//                sysMessage 系统消息
                if (extrs.get("type").equalsIgnoreCase("courseClass")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("type","push");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                if (extrs.get("type").equalsIgnoreCase("payComplete")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("type","push");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                if (extrs.get("type").equalsIgnoreCase("sysMessage")) {
                    Intent intent = new Intent(context, MyMsgActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                Log.w("cc", "推送消息：" + msg.getRaw().toString());

            }

            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {
                Map<String, String> extrs = msg.extra;
//                courseClass 课程开课
//                payComplete 支付完成
//                sysMessage 系统消息
                if (extrs.get("type").equalsIgnoreCase("courseClass")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("type","push");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                if (extrs.get("type").equalsIgnoreCase("payComplete")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("type","push");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                if (extrs.get("type").equalsIgnoreCase("sysMessage")) {
                    Intent intent = new Intent(context, MyMsgActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                Log.w("cc", "推送消息：" + msg.getRaw().toString());
            }
        };
        //使用自定义的NotificationHandler
        mPushAgent.setNotificationClickHandler(notificationClickHandler);

        //注册推送服务 每次调用register都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                Log.w("cc", "device token: " + deviceToken);
                sendBroadcast(new Intent(UPDATE_STATUS_ACTION));
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.w("cc", "register failed: " + s + " " + s1);
                sendBroadcast(new Intent(UPDATE_STATUS_ACTION));
            }
        });

        //使用完全自定义处理
        mPushAgent.setPushIntentServiceClass(UmengNotificationService.class);

        //小米通道
        //MiPushRegistar.register(this, XIAOMI_ID, XIAOMI_KEY);
        //华为通道
        //HuaWeiRegister.register(this);
        //魅族通道
        //MeizuRegister.register(this, MEIZU_APPID, MEIZU_APPKEY);
    }

    {
        PlatformConfig.setWeixin("wx82b6fbe46e0289fc", "59eacc215322d41ea6f939c706ea81f3");
        PlatformConfig.setAlipay("2018050202618831");
    }
}
