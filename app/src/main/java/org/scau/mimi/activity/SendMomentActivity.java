package org.scau.mimi.activity;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.scau.mimi.R;
import org.scau.mimi.base.BaseActivity;
import org.scau.mimi.fragment.SendMomentFragment;

public class SendMomentActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        return SendMomentFragment.newInstance();
    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void initViews() {

    }

    @Override
    protected void loadData() {

    }
}
