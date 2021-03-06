package com.simpleryo.leyotang.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.simpleryo.leyotang.R;
import com.simpleryo.leyotang.view.ZzHorizontalProgressBar;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by 77429 on 2018/3/25.
 */

public class MyCourseItemViewHolder extends SuperViewHolder {
    @ViewInject(R.id.iv_collection_img)
  public  ImageView iv_collection_img;
    @ViewInject(R.id.tv_collection_name)
    public  TextView tv_collection_name;
    @ViewInject(R.id.tv_to_course_detail)
    public  TextView tv_to_course_detail;
    @ViewInject(R.id.tv_complete_count)
    public TextView tv_complete_count;
    @ViewInject(R.id.tv_course_time)
    public TextView tv_course_time;
    @ViewInject(R.id.horizontal_progressbar)
    public  ZzHorizontalProgressBar horizontal_progressbar;
    public MyCourseItemViewHolder(View itemView) {
        super(itemView);
        x.view().inject(this,itemView);
    }
}
