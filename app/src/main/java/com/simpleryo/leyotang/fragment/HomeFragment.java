package com.simpleryo.leyotang.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jdsjlzx.ItemDecoration.DividerDecoration;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.okhttplib.HttpInfo;
import com.simpleryo.leyotang.R;
import com.simpleryo.leyotang.activity.CourseSearchActivity;
import com.simpleryo.leyotang.activity.LoginActivity;
import com.simpleryo.leyotang.activity.MyMsgActivity;
import com.simpleryo.leyotang.adapter.HomeAdapter;
import com.simpleryo.leyotang.base.MyBaseProgressCallbackImpl;
import com.simpleryo.leyotang.base.XLibraryLazyFragment;
import com.simpleryo.leyotang.bean.BusEntity;
import com.simpleryo.leyotang.bean.CodeBean;
import com.simpleryo.leyotang.bean.CurrentAddressInfo;
import com.simpleryo.leyotang.bean.HomeDataBean;
import com.simpleryo.leyotang.bean.MultipleItem;
import com.simpleryo.leyotang.network.SimpleryoNetwork;
import com.simpleryo.leyotang.push.NotificationBroadcast;
import com.simpleryo.leyotang.utils.LocationUtils;
import com.simpleryo.leyotang.utils.SharedPreferencesUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * @author huanglei
 * @ClassNname：HomeFragment.java
 * @Describe 首页fragment
 * @time 2018/3/19 11:10
 */

@RuntimePermissions
public class HomeFragment extends XLibraryLazyFragment{
    @ViewInject(R.id.lrecyclerview)
    LRecyclerView lrecyclerview;
    @ViewInject(R.id.tv_address)
    TextView tv_address;
    HomeAdapter homeAdapter;
    LRecyclerViewAdapter lRecyclerViewAdapter;
    private List<MultipleItem> mItemModels = new ArrayList<>();
    ArrayList<HomeDataBean.DataBeanX.BannersBean> bannerListBeans = new ArrayList<>();
    List<HomeDataBean.DataBeanX.CoursesBeanX> hotCourseList = new ArrayList<>();
    List<HomeDataBean.DataBeanX.CourseTypesBean> courseTypetBeans = new ArrayList<>();
    List<HomeDataBean.DataBeanX.CoursesBeanX> excellentListBeans = new ArrayList<>();
    List<HomeDataBean.DataBeanX.CoursesBeanX> introductoryListBeans = new ArrayList<>();
    @ViewInject(R.id.empty_view)
    View empty_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater
                    .inflate(R.layout.fragment_home, container, false);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    @SuppressLint("MissingPermission")
    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }
        simpleryoNetwork = new SimpleryoNetwork();
        //是否允许嵌套滑动
        lrecyclerview.setNestedScrollingEnabled(false);
        homeAdapter = new HomeAdapter(getActivity());
        DividerDecoration divider = new DividerDecoration.Builder(getActivity())
                .setHeight(30f)
                .setColorResource(R.color.color_transparent)
                .build();
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
        lrecyclerview.setLayoutManager(layoutManager);
        lRecyclerViewAdapter = new LRecyclerViewAdapter(homeAdapter);
        lrecyclerview.setAdapter(lRecyclerViewAdapter);
        lrecyclerview.removeItemDecoration(divider);
        lrecyclerview.addItemDecoration(divider);
        lrecyclerview.setHasFixedSize(true);
        lrecyclerview.setLoadMoreEnabled(false);
        lrecyclerview.setOnRefreshListener(onRefreshListener);
        if (SharedPreferencesUtils.getKeyBoolean("deny")) {
            lat = -41.095977;
            lng = 175.093335;
            getUserCurrentLocation();
        } else {
            getUserLocation();
        }
    }

    public void getUserLocation(){
        Location location = LocationUtils.getInstance(getActivity()).showLocation();
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            getUserCurrentLocation();
        }
    }
    double lat;
    double lng;

    public void getUserCurrentLocation() {
        //获取当前位置信息
        SimpleryoNetwork.getAddressInfo(getActivity(), new MyBaseProgressCallbackImpl(getActivity()) {
            @Override
            public void onSuccess(HttpInfo info) {
                super.onSuccess(info);
                loadingDialog.dismiss();
                CurrentAddressInfo currentAddressInfo = info.getRetDetail(CurrentAddressInfo.class);
                if (currentAddressInfo.getStatus().equalsIgnoreCase("OK")) {
                    StringBuilder address = new StringBuilder();
                    List<CurrentAddressInfo.ResultsBean.AddressComponentsBean> addressComponentsBeans = currentAddressInfo.getResults().get(0).getAddress_components();
                    Collections.reverse(addressComponentsBeans);
                    for (int i = 0; i < addressComponentsBeans.size(); i++) {
                        if (i == 3 || i == 4) {
                            address.append(addressComponentsBeans.get(i).getLong_name()+" ");
                        }
                    }
                    tv_address.setText(address.toString());
                }
            }

            @Override
            public void onFailure(HttpInfo info) {
                super.onFailure(info);
                loadingDialog.dismiss();
                tv_address.setText("Upper Hutt");
            }
        }, lat, lng);
    }

    SimpleryoNetwork simpleryoNetwork;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            getCourseType();
        }
    };


    private Location mLastKnownLocation;

    public void getCourseType() {
        simpleryoNetwork.getHomeCourse(getActivity(), new MyBaseProgressCallbackImpl() {
            @Override
            public void onSuccess(HttpInfo info) {
                super.onSuccess(info);
                mHasLoadedOnce = true;
                HomeDataBean homeDataBean = info.getRetDetail(HomeDataBean.class);
                if (homeDataBean.getCode().equalsIgnoreCase("0")) {
                    MultipleItem item;
                    if (mItemModels != null && mItemModels.size() > 0) {
                        mItemModels.clear();
                    }
                    if (homeDataBean.getData().getBanners() != null && homeDataBean.getData().getBanners().size() > 0) {
                        if (bannerListBeans != null && bannerListBeans.size() > 0) {
                            bannerListBeans.clear();
                        }
                        item = new MultipleItem(MultipleItem.HOMEBANNER);
                        mItemModels.add(item);
                        bannerListBeans.addAll(homeDataBean.getData().getBanners());
                        homeAdapter.setImages(bannerListBeans);//设置banner

                    }
                    if (homeDataBean.getData().getCourseTypes() != null && homeDataBean.getData().getCourseTypes().size() > 0) {
                        if (courseTypetBeans != null && courseTypetBeans.size() > 0) {
                            courseTypetBeans.clear();
                        }
                        item = new MultipleItem(MultipleItem.HOMECOURSETYPE);
                        mItemModels.add(item);
                        for (HomeDataBean.DataBeanX.CourseTypesBean courseTypesBean : homeDataBean.getData().getCourseTypes()) {
                            if (courseTypetBeans.size() <= 7) {
                                courseTypetBeans.add(courseTypesBean);
                            }
                        }
                        homeAdapter.setCourseTypetBeans(courseTypetBeans);
                    }
                    if (homeDataBean.getData().getCourses() != null && homeDataBean.getData().getCourses().size() > 0) {
                        if (introductoryListBeans != null && introductoryListBeans.size() > 0) {
                            introductoryListBeans.clear();
                        }
                        if (hotCourseList != null && hotCourseList.size() > 0) {
                            hotCourseList.clear();
                        }
                        if (excellentListBeans != null && excellentListBeans.size() > 0) {
                            excellentListBeans.clear();
                        }
                        int count=homeDataBean.getData().getCourses().size();
                        if (count>0){
                            item = new MultipleItem(MultipleItem.HOMEEXCELLENT);
                            mItemModels.add(item);
                            excellentListBeans.add(homeDataBean.getData().getCourses().get(0));
                            homeAdapter.setExcellentListBeans(homeDataBean.getData().getCourses().get(0));
                        }
                        if(count>1){
                            item = new MultipleItem(MultipleItem.HOMEINTRODUCTORYCOURSE);
                            mItemModels.add(item);
                            introductoryListBeans.add(homeDataBean.getData().getCourses().get(1));
                            homeAdapter.setIntroductoryListBeans(homeDataBean.getData().getCourses().get(1));
                        }
                        if(count>2){
                            item = new MultipleItem(MultipleItem.HOMEHOTCOURSE);
                            mItemModels.add(item);
                            hotCourseList.add(homeDataBean.getData().getCourses().get(2));
                            homeAdapter.setOrderListBeans(homeDataBean.getData().getCourses().get(2));
                        }
//                        for (HomeDataBean.DataBeanX.CoursesBeanX coursesBeanList : homeDataBean.getData().getCourses()) {
////                            if (coursesBeanList.getTag().getSpaceCode().equalsIgnoreCase("HOT")) {
////                                item = new MultipleItem(MultipleItem.HOMEHOTCOURSE);
////                                mItemModels.add(item);
////                                hotCourseList.add(coursesBeanList);
////                                homeAdapter.setOrderListBeans(coursesBeanList);
////                            }
////                            if (coursesBeanList.getTag().getSpaceCode().equalsIgnoreCase("RECOMMEND")) {
////                                item = new MultipleItem(MultipleItem.HOMEEXCELLENT);
////                                mItemModels.add(item);
////                                excellentListBeans.add(coursesBeanList);
////                                homeAdapter.setExcellentListBeans(coursesBeanList);
////                            }
////                            if (coursesBeanList.getTag().getSpaceCode().equalsIgnoreCase("OFFCIAL")) {
////                                item = new MultipleItem(MultipleItem.HOMEINTRODUCTORYCOURSE);
////                                mItemModels.add(item);
////                                introductoryListBeans.add(coursesBeanList);
////                                homeAdapter.setIntroductoryListBeans(coursesBeanList);
////                            }
//                            if (coursesBeanList.getTag().getName().equalsIgnoreCase("热门课程")) {
//                                item = new MultipleItem(MultipleItem.HOMEHOTCOURSE);
//                                mItemModels.add(item);
//                                hotCourseList.add(coursesBeanList);
//                                homeAdapter.setOrderListBeans(coursesBeanList);
//                            } else if (coursesBeanList.getTag().getName().equalsIgnoreCase("官方推荐")) {
//                                item = new MultipleItem(MultipleItem.HOMEINTRODUCTORYCOURSE);
//                                mItemModels.add(item);
//                                introductoryListBeans.add(coursesBeanList);
//                                homeAdapter.setIntroductoryListBeans(coursesBeanList);
//                            }else{
//                                item = new MultipleItem(MultipleItem.HOMEEXCELLENT);
//                                mItemModels.add(item);
//                                excellentListBeans.add(coursesBeanList);
//                                homeAdapter.setExcellentListBeans(coursesBeanList);
//                            }
//                        }
                    }
                    homeAdapter.setDataList(mItemModels);
                } else if (homeDataBean.getCode().equalsIgnoreCase("401")) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), NotificationBroadcast.class);
                    intent.setAction(NotificationBroadcast.ACTION_REFRESHTOKEN);
                    getActivity().sendBroadcast(intent);
//                    SharedPreferencesUtils.saveKeyString("token", "simpleryo");
//                    lrecyclerview.forceToRefresh();
                }
                lrecyclerview.refreshComplete(mItemModels.size());
                homeAdapter.notifyDataSetChanged();
                lRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpInfo info) {
                super.onFailure(info);
                if (mItemModels != null && mItemModels.size() > 0) {
                    lrecyclerview.refreshComplete(mItemModels.size());
                    lrecyclerview.setEmptyView(empty_view);
                } else {
                    lrecyclerview.setEmptyView(empty_view);
                }
                Toast.makeText(getActivity(), info.getRetDetail(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BusMain(BusEntity bus) {
        if (bus.getType() == 999) {
            lrecyclerview.refreshComplete(mItemModels.size());
            lRecyclerViewAdapter.notifyDataSetChanged();
        }
        if (bus.getType() == 021) {
            getCourseType();
        }
        if (bus.getType() == 022) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }
        if (bus.getType() == 122) {
            if (isLogin) {
                HomeDataBean.DataBeanX.CoursesBeanX.CoursesBean coursesBean = bus.getCoursesBean();
                if (coursesBean.isHasCollect()) {
                    SimpleryoNetwork.disCollectCourse(getActivity(), new MyBaseProgressCallbackImpl(getActivity()) {
                        @Override
                        public void onSuccess(HttpInfo info) {
                            super.onSuccess(info);
                            loadingDialog.dismiss();
                            CodeBean createOrderBean = info.getRetDetail(CodeBean.class);
                            if (createOrderBean.getCode().equalsIgnoreCase("0")) {
//                                getCourseType();
                                lrecyclerview.forceToRefresh();
                                Toast.makeText(getActivity(), "取消收藏成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), createOrderBean.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(HttpInfo info) {
                            super.onFailure(info);
                            loadingDialog.dismiss();
                        }
                    }, coursesBean.getId());
                } else {
                    SimpleryoNetwork.collectCourse(getActivity(), new MyBaseProgressCallbackImpl(getActivity()) {
                        @Override
                        public void onSuccess(HttpInfo info) {
                            super.onSuccess(info);
                            loadingDialog.dismiss();
                            CodeBean createOrderBean = info.getRetDetail(CodeBean.class);
                            if (createOrderBean.getCode().equalsIgnoreCase("0")) {
//                                getCourseType();
                                lrecyclerview.forceToRefresh();
                                Toast.makeText(getActivity(), "收藏成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), createOrderBean.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(HttpInfo info) {
                            super.onFailure(info);
                            loadingDialog.dismiss();
                        }
                    }, coursesBean.getId());
                }
            } else {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }

        }
    }

    String mPageName = "HomeFragment";

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
        EventBus.getDefault().post(new BusEntity(444));//通知banner暂停轮播
    }

    @Override
    public void onResume() {
        super.onResume();
        isLogin = SharedPreferencesUtils.getKeyBoolean("isLogin");//获取用户登录状态
        lrecyclerview.forceToRefresh();
        MobclickAgent.onPageStart(mPageName);
        EventBus.getDefault().post(new BusEntity(555));//通知banner开始轮播
    }

    @SuppressLint("MissingPermission")
    @Event(value = {R.id.iv_msg, R.id.rl_search, R.id.tv_address}, type = View.OnClickListener.class)
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_msg:
                startActivity(new Intent(getActivity(), MyMsgActivity.class));
                break;
            case R.id.rl_search:
                startActivity(new Intent(getActivity(), CourseSearchActivity.class));
                break;
            case R.id.tv_address:
                if (SharedPreferencesUtils.getKeyBoolean("deny")) {
                    HomeFragmentPermissionsDispatcher.getLocationPermissionWithCheck(this);
                } else {
                    getUserLocation();
                }
                break;
        }
    }

    //定义请求
    private static final int READ_CONTACTS_REQUEST = 1;

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void getLocationPermission() {
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //确保是我们的请求
        if (requestCode == READ_CONTACTS_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.w("cc", "位置权限拒绝");
                SharedPreferencesUtils.saveKeyBoolean("deny", true);
            } else if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.w("cc", "位置权限获取成功");
                SharedPreferencesUtils.saveKeyBoolean("deny", false);
                getUserLocation();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        HomeFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}
