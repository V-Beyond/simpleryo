package com.simpleryo.leyotang.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;
import com.okhttplib.HttpInfo;
import com.simpleryo.leyotang.R;
import com.simpleryo.leyotang.activity.MyCourseActivity;
import com.simpleryo.leyotang.activity.MyMsgActivity;
import com.simpleryo.leyotang.adapter.CalendarCourseAdapter;
import com.simpleryo.leyotang.base.MyBaseProgressCallbackImpl;
import com.simpleryo.leyotang.base.XLibraryLazyFragment;
import com.simpleryo.leyotang.bean.BusEntity;
import com.simpleryo.leyotang.bean.CalendarListBean;
import com.simpleryo.leyotang.network.SimpleryoNetwork;
import com.simpleryo.leyotang.utils.SharedPreferencesUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author huanglei
 * @ClassNname：CourseFragment.java
 * @Describe 课程fragment
 * @time 2018/3/19 11:10
 */

public class CourseFragment extends XLibraryLazyFragment {

    @ViewInject(R.id.tv_current_date)
    TextView tv_current_date;
    @ViewInject(R.id.tv_current_month)
    TextView tv_current_month;
    @ViewInject(R.id.tv_current_day)
    TextView tv_current_day;
    @ViewInject(R.id.calenar_view)
    CalendarView calenar_view;
    @ViewInject(R.id.tv_name)
    TextView tv_name;
    @ViewInject(R.id.iv_back)
    ImageView iv_back;
    @ViewInject(R.id.lrecyclerview)
    LRecyclerView lrecyclerview;
    LRecyclerViewAdapter lRecyclerViewAdapter;
    CalendarCourseAdapter calendarCourseAdapter;
    @ViewInject(R.id.empty_view)
    private View mEmptyView;
    @ViewInject(R.id.tv_no_order)
    TextView tv_no_order;
    List<CalendarListBean.DataBean.CourseListBean> ordersBeanList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater
                    .inflate(R.layout.fragment_course, container, false);
            x.view().inject(this, mMainView);
            EventBus.getDefault().register(this);
            isPrepared = true;
            lazyLoad();
        }
        ViewGroup parent = (ViewGroup) mMainView.getParent();
        if (parent != null) {
            parent.removeView(mMainView);
        }
        return mMainView;
    }

    int mYear;
    int mMonth;
    int day;
    boolean isSwitch = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }
        iv_back.setVisibility(View.GONE);
        tv_name.setText(getActivity().getResources().getString(R.string.main_course));
        isLogin = SharedPreferencesUtils.getKeyBoolean("isLogin");//获取用户登录状态
        mYear = calenar_view.getCurYear();
        mMonth = calenar_view.getCurMonth();
        day = calenar_view.getCurDay();
        tv_current_date.setText(mYear + "年" + mMonth + "月");
        tv_current_day.setText(day + "");
        tv_current_month.setText(mMonth + "月");
        lrecyclerview.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        calendarCourseAdapter = new CalendarCourseAdapter(getActivity());
        lRecyclerViewAdapter = new LRecyclerViewAdapter(calendarCourseAdapter);
        calenar_view.setOnMonthChangeListener(new CalendarView.OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month) {
                isSwitch = true;
                tv_current_month.setText(month + "月");
                tv_current_date.setText(year + "年" + month + "月");
                mYear =year;
                mMonth = month;
                lrecyclerview.setVisibility(View.VISIBLE);
                tv_no_order.setVisibility(View.GONE);
                if (calendarListBean != null) {
                    for (CalendarListBean.DataBean dataBean : calendarListBean.getData()) {
                        String date = dataBean.getDateStr().split("-")[2];
                        schemes.add(getSchemeCalendar(year, month, Integer.parseInt(date), 0, ""));
                        calenar_view.setSchemeDate(schemes);
                    }
                }
                initData(year, month);
            }
        });
        calenar_view.setOnYearChangeListener(new CalendarView.OnYearChangeListener() {
            @Override
            public void onYearChange(int year) {

            }
        });
        calenar_view.setOnDateSelectedListener(new CalendarView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Calendar calendar, boolean isClick) {
                if (isLogin) {
                    if (calendarListBean != null) {
                        if (calendarListBean.getCode().equalsIgnoreCase("0")) {
                            day=calendar.getDay();
                            int day = calendar.getDay();
                            String currentDay;
                            if (day >= 1 && day <= 9) {
                                currentDay = "0" + day;
                            } else {
                                currentDay = String.valueOf(day);
                            }
                            int month = calendar.getMonth();
                            String currentMonth;
                            if (month >= 1 && month <= 9) {
                                currentMonth = "0" + month;
                            } else {
                                currentMonth = String.valueOf(month);
                            }
                            String date = calendar.getYear() + "-" + currentMonth + "-" + currentDay;
                            List<CalendarListBean.DataBean.CourseListBean> dataBeanList = courseList.get(date);
                            if (dataBeanList != null && dataBeanList.size() > 0) {
                                if (ordersBeanList != null && ordersBeanList.size() > 0) {
                                    ordersBeanList.clear();
                                }
                                tv_no_order.setVisibility(View.GONE);
                                lrecyclerview.setVisibility(View.VISIBLE);
                                for (CalendarListBean.DataBean.CourseListBean ordersBean : dataBeanList) {
                                    if (ordersBeanList.size() < 8) {
                                        ordersBeanList.add(ordersBean);
                                    }
                                }
                                calendarCourseAdapter.setDataList(ordersBeanList);
                                lrecyclerview.setAdapter(lRecyclerViewAdapter);
                                lrecyclerview.setLoadMoreEnabled(false);
                                lrecyclerview.setPullRefreshEnabled(false);
                                calendarCourseAdapter.notifyDataSetChanged();
                                lRecyclerViewAdapter.notifyDataSetChanged();
                            } else {
                                lrecyclerview.setVisibility(View.GONE);
                                tv_no_order.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
                tv_current_day.setText(calendar.getDay() + "");
            }
        });
        if (isLogin) {
            lrecyclerview.setVisibility(View.VISIBLE);
            tv_no_order.setVisibility(View.GONE);
            initData(mYear, mMonth);
        } else {

//            CalendarEventUtils.addCalendarEvent(getActivity(),"新普乐优日程提醒","新普乐优",System.currentTimeMillis());
//        CalendarEventUtils.deleteCalendarEvent(getActivity(),"新普乐优日程提醒");
            lrecyclerview.setVisibility(View.GONE);
            tv_no_order.setVisibility(View.VISIBLE);
            if (calendarListBean != null) {
                for (CalendarListBean.DataBean dataBean : calendarListBean.getData()) {
                    String date = dataBean.getDateStr().split("-")[2];
                    schemes.add(getSchemeCalendar(mYear, mMonth, Integer.parseInt(date), 0, ""));
                    calenar_view.setSchemeDate(schemes);
                }
            }
        }
        MobclickAgent.onPageStart(mPageName);
    }

    CalendarListBean calendarListBean;
    final List<Calendar> schemes = new ArrayList<>();


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BusMain(BusEntity bus) {
        if (bus.getType()==8001){
            String currentDay;
            if (day >= 1 && day <= 9) {
                currentDay = "0" + day;
            } else {
                currentDay = String.valueOf(day);
            }
            String currentMonth;
            if (mMonth >= 1 && mMonth <= 9) {
                currentMonth = "0" + mMonth;
            } else {
                currentMonth = String.valueOf(mMonth);
            }
            String date = mYear + "-" + currentMonth + "-" + currentDay;
            List<CalendarListBean.DataBean.CourseListBean> dataBeanList = courseList.get(date);
            if (dataBeanList != null && dataBeanList.size() > 0) {
                if (ordersBeanList != null && ordersBeanList.size() > 0) {
                    ordersBeanList.clear();
                }
                tv_no_order.setVisibility(View.GONE);
                lrecyclerview.setVisibility(View.VISIBLE);
                for (CalendarListBean.DataBean.CourseListBean ordersBean : dataBeanList) {
                    if (ordersBeanList.size() < 8) {
                        ordersBeanList.add(ordersBean);
                    }
                }
                calendarCourseAdapter.setDataList(ordersBeanList);
                lrecyclerview.setAdapter(lRecyclerViewAdapter);
                lrecyclerview.setLoadMoreEnabled(false);
                lrecyclerview.setPullRefreshEnabled(false);
                calendarCourseAdapter.notifyDataSetChanged();
                lRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                lrecyclerview.setVisibility(View.GONE);
                tv_no_order.setVisibility(View.VISIBLE);
            }
        }
    }
    public void initData(final int year, final int month) {
        SimpleryoNetwork.getCalendarOrderList(getActivity(), new MyBaseProgressCallbackImpl() {
            @Override
            public void onSuccess(HttpInfo info) {
                super.onSuccess(info);
                calendarListBean = info.getRetDetail(CalendarListBean.class);
                if (calendarListBean.getCode().equalsIgnoreCase("0")) {
                    if (schemes != null && schemes.size() > 0) {
                        schemes.clear();
                    }
                    for (CalendarListBean.DataBean dataBean : calendarListBean.getData()) {
                        courseList.put(dataBean.getDateStr(), dataBean.getCourses());
                        String date = dataBean.getDateStr().split("-")[2];
                        int day = calenar_view.getCurDay();
                        String currentDay;
                        if (day >= 1 && day <= 9) {
                            currentDay = "0" + day;
                        } else {
                            currentDay = String.valueOf(day);
                        }
                        if (currentDay.equalsIgnoreCase(date)) {
                            if (dataBean.getCourses() != null && dataBean.getCourses().size() > 0) {
                                schemes.add(getSchemeCalendar(year, month, Integer.parseInt(date), 0, dataBean.getCourses().size() + ""));
                                calenar_view.setSchemeDate(schemes);
                                if (ordersBeanList != null && ordersBeanList.size() > 0) {
                                    ordersBeanList.clear();
                                }
                                for (CalendarListBean.DataBean.CourseListBean ordersBean : dataBean.getCourses()) {
                                    if (ordersBeanList.size() < 8) {
                                        ordersBeanList.add(ordersBean);
                                    }
                                }
                                calendarCourseAdapter.setDataList(ordersBeanList);
                                lrecyclerview.setAdapter(lRecyclerViewAdapter);
                                lrecyclerview.setLoadMoreEnabled(false);
                                lrecyclerview.setPullRefreshEnabled(false);
                            } else {
                                lrecyclerview.setVisibility(View.GONE);
                                tv_no_order.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (dataBean.getCourses() != null && dataBean.getCourses().size() > 0) {
                                schemes.add(getSchemeCalendar(year, month, Integer.parseInt(date), 0, dataBean.getCourses().size() + ""));
                                calenar_view.setSchemeDate(schemes);
                            }
                        }
                    }
                    if (isSwitch==true){
                        EventBus.getDefault().post(new BusEntity(8001));
                    }
                }
            }

            @Override
            public void onFailure(HttpInfo info) {
                super.onFailure(info);
                lrecyclerview.setVisibility(View.GONE);
                tv_no_order.setVisibility(View.VISIBLE);
            }
        }, year, month);
    }


    @SuppressWarnings("all")
    private Calendar getSchemeCalendar(int year, int month, int day, int color, String text) {
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        if (!text.equalsIgnoreCase("")) {
            calendar.setSchemeColor(0xffff40ff);//如果单独标记颜色、则会使用这个颜色
            calendar.setScheme(text);
        }
        return calendar;
    }

    @Event(value = {R.id.iv_left, R.id.iv_right, R.id.tv_course_name, R.id.iv_msg}, type = View.OnClickListener.class)
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                calenar_view.scrollToPre();
                break;
            case R.id.iv_right:
                calenar_view.scrollToNext();
                break;
            case R.id.iv_msg:
                startActivity(new Intent(getActivity(), MyMsgActivity.class));
                break;
            case R.id.tv_course_name:
                startActivity(new Intent(getActivity(), MyCourseActivity.class));
                break;
        }
    }

    HashMap<String, List<CalendarListBean.DataBean.CourseListBean>> courseList = new HashMap<>();


    String mPageName = "CourseFragment";

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
