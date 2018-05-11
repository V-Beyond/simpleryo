package com.simpleryo.leyotang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.simpleryo.leyotang.R;
import com.simpleryo.leyotang.bean.BusEntity;
import com.simpleryo.leyotang.bean.CourseListBean;
import com.simpleryo.leyotang.utils.XStringPars;
import com.simpleryo.leyotang.viewholder.ExcellentCourseItemViewHolder;
import com.simpleryo.leyotang.viewholder.SuperViewHolder;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.NumberFormat;


/**
 * @author huanglei
 * @version V1.0
 * @Title: JingXuanAdapter
 * @Package com.hpkj.kexue.adapter
 * @Description: 精选推荐item适配器
 * @date 2017/11/10 18:55
 */

public class BusinessCourseAdapter extends BaseAdapter<CourseListBean.DataBeanX> {

    public BusinessCourseAdapter(Context context) {
        super(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public SuperViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        return new ExcellentCourseItemViewHolder(layoutInflater.inflate(R.layout.layout_excellent_course_item, parent, false));
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateCollect(BusEntity bus) {

    }
    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void onBindItemHolder(SuperViewHolder holder, int position) {
        final CourseListBean.DataBeanX bean = listData.get(position);
        if (bean.getCoverUrl()!=null){
            Picasso.with(mContext).load(bean.getCoverUrl()).into(((ExcellentCourseItemViewHolder) holder).iv_collection_img);
        }else{
            Picasso.with(mContext).load("http://p0.so.qhmsg.com/bdr/_240_/t01eb2a6c6319b04655.jpg").into(((ExcellentCourseItemViewHolder) holder).iv_collection_img);
        }
        if (bean.getName()!=null){
            ((ExcellentCourseItemViewHolder) holder).tv_collection_name.setText(bean.getName());
        }else{
            ((ExcellentCourseItemViewHolder) holder).tv_collection_name.setText("暂无课程名称");
        }
        if (bean.getRecommends().get(0).getValue().equalsIgnoreCase("HOT")){
            ((ExcellentCourseItemViewHolder) holder).rl_collect.setBackgroundResource(R.mipmap.iv_collection_bg);
        }
        else if (bean.getRecommends().get(0).getValue().equalsIgnoreCase("EXCELLENT")){
            ((ExcellentCourseItemViewHolder) holder).rl_collect.setBackgroundResource(R.mipmap.iv_collection_blue);
        }
        else if (bean.getRecommends().get(0).getValue().equalsIgnoreCase("OFFCIAL")){
            ((ExcellentCourseItemViewHolder) holder).rl_collect.setBackgroundResource(R.mipmap.iv_collection_ping);
        }
        if (bean.isHasCollect()){
            ((ExcellentCourseItemViewHolder) holder).iv_collection_star.setImageResource(R.mipmap.iv_collection_star);
        }else{
            ((ExcellentCourseItemViewHolder) holder).iv_collection_star.setImageResource(R.mipmap.iv_collection_white_star);
        }
        ((ExcellentCourseItemViewHolder) holder).rl_collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new BusEntity(1003,bean.getId(),bean.isHasCollect()));//收藏or取消收藏
            }
        });
        ((ExcellentCourseItemViewHolder) holder).tv_price.setText(XStringPars.foramtPrice(Integer.valueOf(bean.getPrice()))+"$/hour");
        int collectCount=bean.getCollectCount();
        ((ExcellentCourseItemViewHolder) holder).tv_popular.setText(collectCount+" people");

        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        int totalCount=100;
        if (collectCount>=1&&collectCount<=99){
            totalCount=100;
        }
        if (collectCount>=100&&collectCount<=999){
            totalCount=1000;
        }
        if (collectCount>=1000&&collectCount<=9999){
            totalCount=1000;
        }
        float percent= (float)collectCount / (float) totalCount * 100;
        ((ExcellentCourseItemViewHolder) holder).horizontal_progressbar.setProgress((int) percent);

    }
    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }
}
