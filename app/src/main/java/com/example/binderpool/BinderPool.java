package com.example.binderpool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.aidl.IBinderPool;

import java.util.concurrent.CountDownLatch;

//binder 连接池，模拟一种场景：在一个较大的项目中有多个模块分别使用了不同的aidl（假设它们都在同一进程）
//为了提高效率，只提供一个Service来处理，这就需要用到binder连接池的思想
public class BinderPool {
    public static final int BINDER_ADD = 0;
    public static final int BINDER_ENCODE = 1;
    private static BinderPool binderPool;
    private Context context;
    private IBinderPool iBinderPool;
    private CountDownLatch countDownLatch;

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (iBinderPool != null){
                iBinderPool.asBinder().unlinkToDeath(deathRecipient,0);
                iBinderPool = null;
            }
            bindService();
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                if (iBinderPool != null){
                    iBinderPool.asBinder().linkToDeath(deathRecipient,0);
                    Log.e("MainActivity","bind success" );
                }else {
                    Log.e("MainActivity","bind fail" );
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //初始化的时候就建立服务端的连接
    private BinderPool(Context context){
        this.context = context.getApplicationContext();
        bindService();
    }

    public static BinderPool getInstance(Context context){
        if (binderPool == null){
            synchronized (BinderPool.class){
                if (binderPool == null){
                    binderPool = new BinderPool(context);
                }
            }
        }
        return binderPool;
    }

    private synchronized void bindService(){
        countDownLatch = new CountDownLatch(1);
        Intent intent = new Intent();
        intent.setAction("com.example.wonderful.pool");
        intent.setPackage("com.example.binderpoolserver");
        context.bindService(intent,connection,Context.BIND_AUTO_CREATE);//这是一个异步操作，使用CountDownLatch转换成同步
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public IBinder queryBinder(int code){
        IBinder iBinder = null;
        try {
            if (iBinderPool != null){
                iBinder = iBinderPool.queryBinder(code);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return iBinder;
    }
}
