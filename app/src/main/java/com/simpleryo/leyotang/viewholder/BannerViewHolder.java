package com.simpleryo.leyotang.viewholder;

import android.view.View;
import android.widget.RelativeLayout;


import com.simpleryo.leyotang.R;
import com.simpleryo.leyotang.view.ArcViewPager;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by 77429 on 2017/11/10.
 */

public class BannerViewHolder extends SuperViewHolder {
    @ViewInject(R.id.arc_viewpager)
    public ArcViewPager arcViewPager;
    @ViewInject(R.id.rl_search)
    public  RelativeLayout rl_search;

    public BannerViewHolder(View itemView) {
        super(itemView);
        x.view().inject(this, itemView);
    }
}
