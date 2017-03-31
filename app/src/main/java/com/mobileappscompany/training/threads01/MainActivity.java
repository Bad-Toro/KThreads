package com.mobileappscompany.training.threads01;

import android.os.AsyncTask;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    TextView tV;
    Subscription mySubscription; //For RxJava

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tV = (TextView) findViewById(R.id.textView);
    }
////////////// Thread //////////////////////////////////////////////////
    public void onDo(View view) {
        aMethod();
    }

    public void aMethod() {
        new Thread() {
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                try {
                    Thread.sleep(500);
                    tV.post(new Runnable() {
                        public void run() {
                            tV.setText("Done Thread");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

//////////// AsyncTask ////////////////////////////////////////////////////

    public void onAT(View view) {

        new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... params) {

                int tTS = params[0] * 1000;

                try {
                    Thread.sleep(tTS);
                    return "Done AT!";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "Error AT!";
                }

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                tV.setText(s);
            }
        }.execute(1);

    }

//////////// Event Bus ////////////////////////////////////////////////////

    public class MyEvent {
        private String mssg;
        private int mssgId;

        public MyEvent(int msgId, String msg){
            this.mssg = msg;
            this.mssgId = msgId;
        }

        public String getMssg(){
            return mssg;
        }

        public int getMssgId() {
            return mssgId;
        }
    }

//    public void onEB(View view) {
//        new Thread() {
//            public void run() {
//                try {
//                    Thread.sleep(1500);
//                    EventBus.getDefault().post(new MyEvent("Done EB", 747));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMyEvent(MyEvent e){

        if(e.getMssgId() == 747) {
            tV.setText(e.getMssg());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    ///////// Rx Java //////////////////////////////////////////////

    public void onRx(View view) {
        doRx(2);
    }


    public void doRx(final int i){
        Observable<Integer> myObservable =
                Observable.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws Exception {
                        try {
                            Thread.sleep(i * 1000);
                            return i;

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return -1;
                        }

                    }

                });

        mySubscription = myObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {

                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) { }

                    @Override
                    public void onNext(Integer integer) {
                        //Log.d("FTMACTAG", "Rslt: " + integer);
                        tV.setText("RxJava  " + integer + " seconds");
                    }

                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!mySubscription.isUnsubscribed()||(mySubscription == null)){
            mySubscription.unsubscribe();
        }
    }
}