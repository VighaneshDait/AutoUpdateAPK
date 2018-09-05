package com.xacsz.www.autoupdateapk;

/**
 * Created by Administrator on 7/5/2016.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


public class UpdateManager
{

    private static final int DOWNLOAD = 1;

    private static final int DOWNLOAD_FINISH = 2;

    HashMap<String, String> mHashMap;

    private String mSavePath;

    private int progress;

    private boolean cancelUpdate = false;

    private Context mContext;

    private ProgressBar mProgress;
    private Dialog mDownloadDialog;

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {

                case DOWNLOAD:

                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:

                    installApk();
                    break;
                default:
                    break;
            }
        };
    };

    public UpdateManager(Context context)
    {
        this.mContext = context;
    }


    public void checkUpdate()
    {
        if (isUpdate())
        {
            showNoticeDialog();
        } else
        {
            Toast.makeText(mContext, R.string.soft_update_no, Toast.LENGTH_LONG).show();
        }
    }


    private boolean isUpdate()
    {

        int versionCode = getVersionCode(mContext);

        InputStream inStream = ParseXmlService.class.getClassLoader().getResourceAsStream("version.xml");

        ParseXmlService service = new ParseXmlService();
        try
        {
            mHashMap = service.parseXml(inStream);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null != mHashMap)
        {
            int serviceCode = Integer.valueOf(mHashMap.get("version"));

            if (serviceCode > versionCode)
            {
                return true;
            }
        }
        return false;
    }


    private int getVersionCode(Context context)
    {
        int versionCode = 0;
        try
        {

            versionCode = context.getPackageManager().getPackageInfo("com.szy.update", 0).versionCode;
        } catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return versionCode;
    }


    private void showNoticeDialog()
    {

        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_update_title);
        builder.setMessage(R.string.soft_update_info);
        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        // 稍后更新
        builder.setNegativeButton(R.string.soft_update_later, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }


    private void showDownloadDialog()
    {

        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_updating);

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        builder.setView(v);

        builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();

                cancelUpdate = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();

        downloadApk();
    }

    private void downloadApk()
    {
        new downloadApkThread().start();
    }


    private class downloadApkThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                {

                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = sdpath + "download";
                    URL url = new URL(mHashMap.get("url"));

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();

                    int length = conn.getContentLength();

                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);

                    if (!file.exists())
                    {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, mHashMap.get("name"));
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;

                    byte buf[] = new byte[1024];

                    do
                    {
                        int numread = is.read(buf);
                        count += numread;

                        progress = (int) (((float) count / length) * 100);

                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0)
                        {

                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }

                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            mDownloadDialog.dismiss();
        }
    };


    private void installApk()
    {
        File apkfile = new File(mSavePath, mHashMap.get("name"));
        if (!apkfile.exists())
        {
            return;
        }

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}
