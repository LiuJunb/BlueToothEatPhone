package com.xmg.bluetoothearphone.base;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;

import com.xmg.bluetoothearphone.bean.BlueDevice;

/**
 * Description :
 * Author : liujun
 * Email  : liujin2son@163.com
 * Date   : 2016/8/11 0011
 */
public class BaseActivity extends AppCompatActivity {


    protected ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    public void showProgressDailog(){
        if(progressDialog==null)
            progressDialog=new ProgressDialog(BaseActivity.this);
        progressDialog.setMessage("正在搜索...");
        progressDialog.show();

    }

    public void hideProgressDailog(){
        if(progressDialog!=null){
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    }

    public void showDailog(String msg, DialogInterface.OnClickListener listenter){
        alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"确认",listenter);
        alertDialog.show();
    }

    public void hideDailog(){
        if(alertDialog!=null){
            if(alertDialog.isShowing()){
                alertDialog.dismiss();
            }
        }
    }


}
