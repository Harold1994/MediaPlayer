package xdu.edu.cn.mediaplayer;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        PackageManager manager = getPackageManager();
        PackageInfo info = null;

        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        String version = info == null ? getString(R.string.unknown) : info.versionName;
        String msg = String.format(getString(R.string.version_info),version);

        TextView ver = (TextView) findViewById(R.id.version_info);
        ver.setText(msg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                default:
                    return super.onOptionsItemSelected(item);
        }

    }
}
