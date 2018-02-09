package com.yechaoa.matissedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private UriAdapter mAdapter;
    private static final int REQUEST_CODE_CHOOSE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.zhihu).setOnClickListener(this);
        findViewById(R.id.dracula).setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(mAdapter = new UriAdapter());
    }

    /**
     * 1 预览  2 已选择带过去  3 剪裁  4 压缩
     * <p>
     * 120 显示三列  100显示四列
     */

    @Override
    public void onClick(final View v) {
        RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            switch (v.getId()) {
                                case R.id.zhihu:
                                    Matisse.from(MainActivity.this)
                                            .choose(MimeType.allOf())//ofAll()
                                            .theme(R.style.Matisse_Zhihu)//主题，夜间模式R.style.Matisse_Dracula
                                            .countable(true)//是否显示选中数字
                                            .capture(true)//是否提供拍照功能
                                            .captureStrategy(new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider"))//存储地址
                                            .maxSelectable(9)//最大选择数
                                            //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))//筛选条件
                                            .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))//图片大小
                                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)//屏幕方向
                                            .thumbnailScale(0.85f)//缩放比例
                                            .imageEngine(new MyGlideEngine())//图片加载方式
                                            .forResult(REQUEST_CODE_CHOOSE);//请求码
                                    break;
                                case R.id.dracula:
                                    Matisse.from(MainActivity.this)
                                            .choose(MimeType.of(MimeType.JPEG))//ofImage()
                                            .theme(R.style.Matisse_Dracula)
                                            .countable(false)
                                            .maxSelectable(9)
                                            .imageEngine(new MyGlideEngine())
                                            .forResult(REQUEST_CODE_CHOOSE);
                                    break;
                            }
                            mAdapter.setData(null);
                        } else {
                            Toast.makeText(MainActivity.this, "权限被拒绝了..", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mAdapter.setData(Matisse.obtainResult(data));
        }
    }

    private class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;

        void setData(List<Uri> uris) {
            mUris = uris;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            Glide.with(MainActivity.this).load(mUris.get(position)).into(holder.mImg);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        class UriViewHolder extends RecyclerView.ViewHolder {

            private ImageView mImg;

            UriViewHolder(View contentView) {
                super(contentView);
                mImg = (ImageView) contentView.findViewById(R.id.img);
            }
        }
    }

}
