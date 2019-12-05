package com.example.plugintest;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.plugintest.dummy.DummyContent;

public class Main2Activity extends BaseActivity implements ItemFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Fragment fragment = (Fragment) getInstanceFromPlugin("com.example.plugina.ItemFragment");
        getSupportFragmentManager().beginTransaction().add(R.id.aty2_content, fragment).commit();

    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    @Override
    public Resources getResources() {
        return mPluginInfo.resources;
    }
}
