package com.kr.inputmethod;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.kr.banner.Banner;
import com.kr.banner.IndicatorView;
import com.kr.keyboard.CustomKeyboardManager;

import java.util.Arrays;
import java.util.List;


public final class InputMethodDemoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_inputmethod);
        EditText editText = findViewById(R.id.tv_pwd);
        CustomKeyboardManager customKeyboardManager = new CustomKeyboardManager(editText);
        customKeyboardManager.subscribe();





        final List<Integer> resources = Arrays.asList(R.mipmap.a, R.mipmap.b, R.mipmap.c,R.mipmap.d,R.mipmap.a);

        ViewPager container = findViewById(R.id.container);
        IndicatorView pi = findViewById(R.id.pi);

        Banner banner = new Banner.Builder().setItems(resources).setViewPager(container).setImtemViewId(R.layout.item).setPageIndicator(pi).setBindViewListener(new Banner.BindViewListener() {
            @Override
            public void onBind(View view, int pos) {
                Log.d("test","onBind:"+pos);
                ((ImageView) view).setImageResource(resources.get(pos));
            }
        }).build();
        banner.start();

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(InputMethodDemoActivity.this, "on clicked", Toast.LENGTH_SHORT).show();
            }
        });

    }


}
