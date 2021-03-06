package com.simpleryo.leyotang.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.okhttplib.HttpInfo;
import com.simpleryo.leyotang.R;
import com.simpleryo.leyotang.base.BaseActivity;
import com.simpleryo.leyotang.base.MyBaseProgressCallbackImpl;
import com.simpleryo.leyotang.bean.BaseResult;
import com.simpleryo.leyotang.bean.RegisterBean;
import com.simpleryo.leyotang.network.SimpleryoNetwork;
import com.simpleryo.leyotang.utils.XActivityUtils;
import com.simpleryo.leyotang.utils.XStringPars;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

/**
 * @author huanglei
 * @ClassNname：RegisterActivity.java
 * @Describe 注册页面
 * @time 2018/3/21 14:08
 */
@ContentView(R.layout.activity_register)
public class RegisterActivity extends BaseActivity{
    @ViewInject(R.id.edittext_email)
    EditText edittext_email;
    @ViewInject(R.id.edittext_phone)
    EditText edittext_phone;
    @ViewInject(R.id.edittext_code)
    EditText edittext_code;
    @ViewInject(R.id.edittext_password)
    EditText edittext_password;
    @ViewInject(R.id.edittext_comfirm_password)
    EditText edittext_comfirm_password;
    @ViewInject(R.id.tv_get_code)
    TextView tv_get_code;
    @ViewInject(R.id.tv_register)
    TextView tv_register;
    String email;//邮箱
    String phone;//手机号
    String code;//验证码
    String password;//密码
    String comfirmPassWord;//确认密码
    public TimeCount mTime;
    String type;
    String typeCode;
    String loginName="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this)
                .statusBarColor(R.color.color_transparent)
                .navigationBarWithKitkatEnable(false)
                .fullScreen(false)
                .init();
        mTime = new TimeCount(60000, 1000);
        type=getIntent().getStringExtra("type");
        if (type.equalsIgnoreCase("register")){
            typeCode="REGIST";
            edittext_email.setHint(getResources().getString(R.string.mailbox_account));
            tv_register.setText(getResources().getString(R.string.register));
        }else if(type.equalsIgnoreCase("forget")){
            typeCode="UPDATE_PASSWORD";
            edittext_email.setHint(getResources().getString(R.string.login_name));
            tv_register.setText(getResources().getString(R.string.change_pwd));
        }
    }

    @Event(value = {R.id.tv_register, R.id.tv_get_code}, type = View.OnClickListener.class)
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_register://注册
                email = edittext_email.getText().toString().trim();
                phone = edittext_phone.getText().toString().trim();
                code = edittext_code.getText().toString().trim();
                password = edittext_password.getText().toString().trim();
                comfirmPassWord = edittext_comfirm_password.getText().toString().trim();
                loginName=edittext_email.getText().toString().trim();
                if(type.equalsIgnoreCase("forget")){
                    if (loginName.isEmpty()) {
                        makeText(RegisterActivity.this, "登录名不能为空", LENGTH_SHORT).show();
                        return;
                    }
                }else  if (type.equalsIgnoreCase("register")){
                    if (email.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "邮箱不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!XStringPars.isEmail(email)) {
                        Toast.makeText(RegisterActivity.this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (phone.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "手机不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (code.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "验证码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (comfirmPassWord.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equalsIgnoreCase(comfirmPassWord)) {
                    Toast.makeText(RegisterActivity.this, "密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length()<6) {
                    Toast.makeText(RegisterActivity.this, "请输入6位以上密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (comfirmPassWord.length()<6) {
                    Toast.makeText(RegisterActivity.this, "请输入6位以上密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(type.equalsIgnoreCase("forget")){//修改密码
                    SimpleryoNetwork.updatePwd(RegisterActivity.this, new MyBaseProgressCallbackImpl(RegisterActivity.this) {
                        @Override
                        public void onSuccess(HttpInfo info)   {
                            loadingDialog.dismiss();
                            BaseResult registerBean = info.getRetDetail(BaseResult.class);
                            if (registerBean.getCode().equalsIgnoreCase("0")){
                                Toast.makeText(RegisterActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                XActivityUtils.getInstance().popActivity(RegisterActivity.this);
                            }else{
                                Toast.makeText(RegisterActivity.this, registerBean.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(HttpInfo info)   {
                            loadingDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                        }
                    }, loginName, password,code,phone);
                }else  if (type.equalsIgnoreCase("register")){//注册
                    SimpleryoNetwork.userRegister(RegisterActivity.this, new MyBaseProgressCallbackImpl(RegisterActivity.this) {
                        @Override
                        public void onSuccess(HttpInfo info)   {
                            loadingDialog.dismiss();
                            RegisterBean registerBean = info.getRetDetail(RegisterBean.class);
                            if (registerBean.getCode().equalsIgnoreCase("0")){
                                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                                XActivityUtils.getInstance().popActivity(RegisterActivity.this);
                            }else{
                                Toast.makeText(RegisterActivity.this, registerBean.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(HttpInfo info)   {
                            loadingDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                        }
                    }, email, phone, code, password);
                }

                break;
            case R.id.tv_get_code://获取验证码
                phone = edittext_phone.getText().toString().trim();
                loginName=edittext_email.getText().toString().trim();
                if (phone.isEmpty()) {
                    makeText(RegisterActivity.this, "手机不能为空", LENGTH_SHORT).show();
                    return;
                }
                if(type.equalsIgnoreCase("forget")){
                    if (loginName.isEmpty()) {
                        makeText(RegisterActivity.this, "登录名不能为空", LENGTH_SHORT).show();
                        return;
                    }
                }
//                if (!XStringPars.isMobileNO(phone)) {
//                    makeText(RegisterActivity.this, "请输入正确的手机号", LENGTH_SHORT).show();
//                    return;
//                }
                mTime.start();
                SimpleryoNetwork.userGetCode(RegisterActivity.this, new MyBaseProgressCallbackImpl(RegisterActivity.this) {
                    @Override
                    public void onSuccess(HttpInfo info)  {
                        loadingDialog.dismiss();
                        BaseResult codeBean = info.getRetDetail(BaseResult.class);
                        if (codeBean.getCode().equalsIgnoreCase("0")) {
                            makeText(RegisterActivity.this, "验证码发送成功，请注意查收", LENGTH_SHORT).show();
                        }else{
                            handler.sendEmptyMessage(6);
                            makeText(RegisterActivity.this, codeBean.getMsg(), LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(HttpInfo info){
                        loadingDialog.dismiss();
                        makeText(RegisterActivity.this, "发送失败", LENGTH_SHORT).show();
                        handler.sendEmptyMessage(6);
                    }
                }, phone,typeCode,loginName);
                break;
        }
    }


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 6:
                    mTime.cancel();
                    tv_get_code.setBackgroundResource(R.drawable.shape_get_code);
                    tv_get_code.setText("获取验证码");
                    tv_get_code.setClickable(true);
                    break;
            }
        }
    };
    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tv_get_code.setClickable(false);
            tv_get_code.setBackgroundResource(R.drawable.bg_getcode_gray);
            tv_get_code.setText(millisUntilFinished / 1000 + "S重新获取");
        }

        @Override
        public void onFinish() {
            tv_get_code.setBackgroundResource(R.drawable.shape_get_code);
            tv_get_code.setText("重新获取验证码");
            tv_get_code.setClickable(true);

        }
    }
}
