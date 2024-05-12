package com.example.chapter13.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.chapter13.FriendChatActivity;
import com.example.chapter13.MainApplication;
import com.example.chapter13.R;
import com.example.chapter13.adapter.EntityListAdapter;
import com.example.chapter13.bean.EntityInfo;
import com.example.chapter13.widget.NoScrollListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;

public class FriendListFragment extends Fragment implements AdapterView.OnItemClickListener {   //mSocket.emit("self_online", MainApplication.getInstance().wechatName);
    private static final String TAG = "FriendListFragment"; //ImageDetailActivity 用来展示聊天窗口内点击图片时间
    protected View mView; // 声明一个视图对象
    protected Context mContext; // 声明一个上下文对象
    private TextView tv_title; // 声明一个文本视图对象
    private NoScrollListView nslv_friend; // 声明一个不滚动视图对象
    private Map<String, EntityInfo> mFriendMap = new HashMap<>(); // 好友的名称映射，起到映射的作用为mFriendList所服务的。
    private List<EntityInfo> mFriendList = new ArrayList<>(); // 好友列表{name,relation}
    private EntityListAdapter mAdapter; // 好友列表适配器
    private Socket mSocket; // 声明一个套接字对象
    private Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象


//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { //以前简单Fragment
//        mContext = getActivity(); // 获取活动页面的上下文
//        // 根据布局文件fragment_tab_first.xml生成视图对象
//        mView = inflater.inflate(R.layout.fragment_tab_first, container, false);
//        TextView tv_first = mView.findViewById(R.id.tv_first);
//        tv_first.setText("我是首页页面");
//        return mView;
//    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity(); // 获取活动页面的上下文
        mView = inflater.inflate(R.layout.fragment_friend_list, container, false);//用的listview没有用功能更强大的RecycleView
        initView(); // 初始化视图
        initSocket(); // 初始化套接字 会调用mAdapter.notifyDataSetChanged();
        return mView;
    }

    // 初始化视图
    private void initView() {
        tv_title = mView.findViewById(R.id.tv_title);
        tv_title.setText(String.format("好友（%d）", mFriendList.size()));
        mView.findViewById(R.id.iv_back).setOnClickListener(v -> getActivity().finish());//返回按钮 返回到上一个Fragment

        nslv_friend = mView.findViewById(R.id.nslv_friend);//listview
        mAdapter = new EntityListAdapter(mContext, mFriendList);// (item LayoutInflater中定义)+数据
        nslv_friend.setAdapter(mAdapter);// 填入item的数据
        nslv_friend.setOnItemClickListener(this);
    }

    // 初始化套接字
    private void initSocket() {
        mSocket = MainApplication.getInstance().getSocket();
        // 开始监听好友上线事件
        mSocket.on("friend_online", (args) -> { // clientMap存储每一个连接上服务器的人
            String friend_name = (String) args[0];//args[1]是Session ID;
            Log.d(TAG, "friend_name="+friend_name);
            if (friend_name != null) {
                // 把刚上线的好友加入好友列表
                mFriendMap.put(friend_name, new EntityInfo(friend_name, "好友"));
                mFriendList.clear();
                mFriendList.addAll(mFriendMap.values());
                mHandler.postDelayed(mRefresh, 200);//延迟处理 Handler+Runnable
            }
        });
        // 开始监听好友下线事件
        mSocket.on("friend_offline", (args) -> {
            String friend_name = (String) args[0];
            mFriendMap.remove(friend_name); // 从好友列表移除已下线的好友
            mFriendList.clear();
            mFriendList.addAll(mFriendMap.values());
            mHandler.postDelayed(mRefresh, 200);
        });
        // 通知服务器“我已上线”
        mSocket.emit("self_online", MainApplication.getInstance().wechatName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 通知服务器“我已下线”
        mSocket.emit("self_offline", MainApplication.getInstance().wechatName);
        mSocket.off("friend_online"); // 取消监听好友上线事件
        mSocket.off("friend_offline"); // 取消监听好友下线事件
    }

    private Runnable mRefresh = () -> doRefresh(); // 好友列表的刷新任务
    // 刷新好友列表
    private void doRefresh() {
        mHandler.removeCallbacks(mRefresh); // 防止频繁刷新造成列表视图崩溃
        tv_title.setText(String.format("好友（%d）", mFriendList.size()));
        mAdapter.notifyDataSetChanged();
    }








    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EntityInfo friend = mFriendList.get(position);
        // 以下跳到与指定好友聊天的界面
        Intent intent = new Intent(mContext, FriendChatActivity.class);
        intent.putExtra("self_name", MainApplication.getInstance().wechatName);
        intent.putExtra("friend_name", friend.name);
        startActivity(intent);
    }

}
