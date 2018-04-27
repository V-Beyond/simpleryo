package com.simpleryo.leyotang.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jdsjlzx.ItemDecoration.DividerDecoration;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.okhttplib.HttpInfo;
import com.simpleryo.leyotang.R;
import com.simpleryo.leyotang.adapter.MessageAdapter;
import com.simpleryo.leyotang.base.BaseActivity;
import com.simpleryo.leyotang.base.MyBaseProgressCallbackImpl;
import com.simpleryo.leyotang.bean.MessageListBean;
import com.simpleryo.leyotang.network.SimpleryoNetwork;
import com.simpleryo.leyotang.utils.XActivityUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassNname：MyCourse.java
 * @Describe 我的订单页面
 * @author huanglei
 * @time 2018/3/19 13:28
 */
@ContentView(R.layout.activity_my_msg)
public class MyMsgActivity extends BaseActivity {
    @ViewInject(R.id.tv_name)
    TextView tv_name;
    @ViewInject(R.id.iv_msg)
    ImageView iv_msg;
    @ViewInject(R.id.lrecyclerview)
    LRecyclerView lrecyclerview;
    LRecyclerViewAdapter lRecyclerViewAdapter;
    MessageAdapter messageAdapter;
    List<MessageListBean.DataBean> messageList = new ArrayList<>();
    @ViewInject(R.id.empty_view)
    private View mEmptyView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv_name.setText("我的消息");
        iv_msg.setVisibility(View.GONE);

        DividerDecoration divider = new DividerDecoration.Builder(this)
                .setHeight(50f)
                .setColorResource(R.color.color_transparent)
                .build();
        lrecyclerview.addItemDecoration(divider);
        lrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(MyMsgActivity.this);
        lRecyclerViewAdapter = new LRecyclerViewAdapter(messageAdapter);
        lrecyclerview.setAdapter(lRecyclerViewAdapter);
        lrecyclerview.setLoadMoreEnabled(false);
        lrecyclerview.setPullRefreshEnabled(true);
        lrecyclerview.setOnRefreshListener(onRefreshListener);
        getMessageList();
    }
    public void getMessageList(){
        SimpleryoNetwork.getMessageList(MyMsgActivity.this,new MyBaseProgressCallbackImpl(MyMsgActivity.this){
            @Override
            public void onSuccess(HttpInfo info) {
                super.onSuccess(info);
                loadingDialog.dismiss();
                MessageListBean messageListBean=info.getRetDetail(MessageListBean.class);
                if (messageListBean.getCode().equalsIgnoreCase("0")){
                    if (messageListBean.getData()!=null&&messageListBean.getData().size()>0){
                        messageList.addAll(messageListBean.getData());
                        messageAdapter.setDataList(messageList);
                    }
                }
            }

            @Override
            public void onFailure(HttpInfo info) {
                super.onFailure(info);
                loadingDialog.dismiss();
                Toast.makeText(MyMsgActivity.this,"数据一不小心走丢了，请稍后回来",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            getMessageList();
        }
    };
    @Event(value = {R.id.iv_back}, type = View.OnClickListener.class)
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                XActivityUtils.getInstance().popActivity(MyMsgActivity.this);
                break;
        }
    }
}
