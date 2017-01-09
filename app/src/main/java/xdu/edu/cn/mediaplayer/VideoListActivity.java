package xdu.edu.cn.mediaplayer;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static xdu.edu.cn.mediaplayer.R.string.refresh;

public class VideoListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "Harold Player";

    private AsyncTask mVideoUpdateTask;
    private List<VideoItem> mVideoList;
    private ListView mVideoListView;
    private MenuItem mRefreshMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        this.setTitle(R.string.video_list);

        mVideoList = new ArrayList<VideoItem>();
        mVideoListView = (ListView) findViewById(R.id.video_list);
        VideoItemAdapter adapter = new VideoItemAdapter(this, R.layout.video_item, mVideoList);
        mVideoListView.setAdapter(adapter);
        mVideoListView.setOnItemClickListener(this);

        updateVideoList();
    }

    private void updateVideoList(){
        mVideoUpdateTask = new VideoUpdateTask();
        mVideoUpdateTask.execute();
        if(mRefreshMenuItem != null){
            mRefreshMenuItem.setTitle(R.string.in_refresh);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        VideoItem item = mVideoList.get(position);
        Intent i = new Intent(this, VideoPlayer.class);
        i.setData(Uri.parse(item.path));
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if((mVideoUpdateTask != null)&&(mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING)){
            mVideoUpdateTask.cancel(true);
        }
        mVideoUpdateTask = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
                mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
        if((mVideoUpdateTask != null)&&(mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING)){
            mRefreshMenuItem.setTitle(R.string.in_refresh);
        }else{
            mRefreshMenuItem.setTitle(R.string.refresh);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()){
           case R.id.menu_refresh:{
               if((mVideoUpdateTask != null)&&(mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING)){
                   mVideoUpdateTask.cancel(true);
                   mVideoUpdateTask = null;
               }
               else {
                  updateVideoList();
                   }
               }
           break;

           case R.id.menu_about:{
               Intent i = new Intent(this, AboutActivity.class);
               startActivity(i);
           }
           break;

           default:
               return super.onContextItemSelected(item);
       }
        return true;
    }



    class VideoUpdateTask extends AsyncTask<Object, VideoItem, Void>{

        List<VideoItem> mDataList = new ArrayList<VideoItem>();


        @Override
        protected Void doInBackground(Object... params) {

            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] searchKey = new String[]{
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_ADDED
            };

            String [] keywords = null;
            String where = MediaStore.Video.Media.DATA + " like \"%"+"/Video/"+"%\"";
            String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(
                    uri,
                    searchKey,
                    where,
                    keywords,
                    sortOrder
            );
            if(cursor != null){
                while(cursor.moveToNext() && ! isCancelled()){
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));

                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                    VideoItem data = new VideoItem(path, name, createdTime);

                    if(mVideoList.contains(data) == false){
                        data.createThumb();
                        publishProgress(data);
                    }
                    mDataList.add(data);
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(VideoItem... values) {
            VideoItem data = values[0];
            mVideoList.add(data);
            VideoItemAdapter adapter = (VideoItemAdapter) mVideoListView.getAdapter();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            updateResult();
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            updateResult();
        }

        private void updateResult() {

            for(int i = 0; i< mVideoList.size(); i++){
                if(!mDataList.contains(mVideoList.get(i))){
                    mVideoList.get(i).releaseThumb();
                    mVideoList.remove(i);
                    i--;
                }
            }
            mDataList.clear();
            VideoItemAdapter adapter = (VideoItemAdapter) mVideoListView.getAdapter();
            adapter.notifyDataSetChanged();

            if(mRefreshMenuItem != null){
                mRefreshMenuItem.setTitle(refresh);
            }
        }


    }
}
