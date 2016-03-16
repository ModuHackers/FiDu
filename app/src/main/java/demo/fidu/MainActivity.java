package demo.fidu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import fidu.FiDu;
import fidu.FiDuCallback;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Button mButton;

    ProgressBar mProgressBar1;
    ProgressBar mProgressBar2;
    ProgressBar mProgressBar3;

    String serverFileUrl = "http://sethfeng.github.io/assets/svn-book.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mProgressBar1 = (ProgressBar) findViewById(R.id.progress1);
        mProgressBar2 = (ProgressBar) findViewById(R.id.progress2);
        mProgressBar3 = (ProgressBar) findViewById(R.id.progress3);

        mButton = (Button) findViewById(R.id.download1);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick in thread: " + Thread.currentThread().getId());

                FiDu.getInstance().download(serverFileUrl, "/sdcard/svn-book/pdf", new
                        FiDuCallback() {
                            @Override
                            public void onResponse(Response response) throws IOException {
                                Log.d(TAG, "onResponse in thread: " + Thread.currentThread()
                                        .getId());
                                // TODO UI线程不能处理网络: response.body().bytes(),非UI线程不能处理UI
                                Log.d(TAG, new String(response.body().bytes()));
                            }

                            @Override
                            public void onFailure(Request request, Exception e) {
                                Log.d(TAG, "onFailure in thread: " + Thread.currentThread()
                                        .getId());
                                e.printStackTrace();
                            }

                            @Override
                            public void onProgress(int progress) {
                                // TODO 非主线程不能处理UI
//                                Log.d(TAG, "onProgress in thread: " + Thread.currentThread()
//                                        .getId());
                                mProgressBar1.setProgress(progress);

                                // TODO do not!
//                                mButton.setText(String.valueOf(progress));
                            }
                        });


            }
        });

        findViewById(R.id.download2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        findViewById(R.id.download3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }


}
