package com.example.binderpool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.example.aidl.IAdd;
import com.example.aidl.IEncode;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doWork();
                    }
                }).start();
            }
        });
    }

    private void doWork(){
        BinderPool binderPool = BinderPool.getInstance(this);
        IBinder addBinder = binderPool.queryBinder(BinderPool.BINDER_ADD);
        IAdd add = IAdd.Stub.asInterface(addBinder);
        try {
            int num = add.add(10,10);
            Log.e(TAG,"add: " + num );
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        IBinder encodeBinder = binderPool.queryBinder(BinderPool.BINDER_ENCODE);
        IEncode encode = IEncode.Stub.asInterface(encodeBinder);
        try {
            String encodeString = encode.encode("wonderful");
            Log.e(TAG,"encode: " + encodeString);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
