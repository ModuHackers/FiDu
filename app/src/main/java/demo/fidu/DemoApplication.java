package demo.fidu;

import android.app.Application;

import fidu.FiDu;

/**
 * Created by fengshzh on 16/3/16.
 */
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FiDu.init(this);
    }
}
