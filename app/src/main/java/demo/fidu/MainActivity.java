package demo.fidu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import fidu.FiDu;
import fidu.FiDuCallback;
import fidu.FiDuUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    ProgressBar mProgressBar;


    String serverFileUrl = "http://sethfeng.github.io/assets/svn-book.pdf";
    String localFile = "/sdcard/svn-book.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        // 上传
        Button upload0 = (Button) findViewById(R.id.upload0);
        if (upload0 != null) {
            upload0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FiDu.getInstance().upload("", localFile, FiDuUtil.guessFileType(localFile), new
                            FiDuCallback() {
                                @Override
                                public void onResponse(Response response) {
                                    Toast.makeText(MainActivity.this, "onResponse", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(Request request, Exception e) {
                                    Toast.makeText(MainActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();

                                }

                                @Override
                                public void onProgress(int progress) {
                                    mProgressBar.setProgress(progress);
                                }
                            });
                }
            });
        }

        // 下载
        final Button download1 = (Button) findViewById(R.id.download1);
        if (download1 != null) {
            download1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FiDu.getInstance().download(serverFileUrl, localFile, new
                            FiDuCallback() {
                                @Override
                                public void onResponse(Response response)  {
                                    Toast.makeText(MainActivity.this, "onResponse", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Request request, Exception e) {
                                    Toast.makeText(MainActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
                                    if (e != null) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onProgress(int progress) {
                                    mProgressBar.setProgress(progress);
                                }
                            });


                }
            });
        }

        // Range下载
        Button download2 = (Button) findViewById(R.id.download2);
        if (download2 != null) {
            download2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FiDu.getInstance().downloadByRange(serverFileUrl, localFile, new FiDuCallback
                            () {
                        @Override
                        public void onResponse(Response response)  {
                            Toast.makeText(MainActivity.this, "onResponse", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Request request, Exception e) {
                            Toast.makeText(MainActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                        @Override
                        public void onProgress(int progress) {
                            mProgressBar.setProgress(progress);
                        }
                    });
                }
            });


        }

        // Range下载重试
        Button download3 = (Button) findViewById(R.id.download3);
        if (download3 != null) {
            download3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }

        // Range下载取消
        Button download4 = (Button) findViewById(R.id.download4);
        if (download4 != null) {
            download4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }
}
