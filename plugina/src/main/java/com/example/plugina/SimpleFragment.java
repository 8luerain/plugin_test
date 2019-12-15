package com.example.plugina;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SimpleFragment extends Fragment {

    private View mRootView;
    private ImageView viewById;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("SimpleFragment", "onCreateView: R.layout.fragment_plugin_simple[" + Integer.toHexString(R.layout.fragment_plugin_simple) + "]");
        Log.d("SimpleFragment", "onCreateView: R.id.img_simple[" + Integer.toHexString(R.id.img_simple) + "]");
        Log.d("SimpleFragment", "onCreateView: R.drawable.aaa_client[" + Integer.toHexString(R.drawable.aaa_client) + "]");
        mRootView = inflater.inflate(R.layout.fragment_plugin_simple, null);
        viewById = mRootView.findViewById(R.id.img_simple);
//        viewById.setImageResource(R.drawable.aaa_client);
        viewById.setImageResource(R.drawable.baselib_img);
//        viewById.setImageResource(R.drawable.plugin_img);
        return mRootView;
    }
}
