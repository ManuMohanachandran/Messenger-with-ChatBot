package com.example.manu.retry;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import layout.dispcontent;

public class Display extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        TextView et=(TextView) findViewById(R.id.oyaya);
        et.setText(this.getApplicationInfo().packageName);
        takevalues();
    }

    public void takevalues()
    {
        String s;
        Intent i=getIntent();
        s=i.getStringExtra(MainActivity.s);
        s="Hello " +s+" !";
        TextView et=(TextView) findViewById(R.id.txt);
        et.setText(s);
    }

    public void klik(View view)
    {
        TextView et=(TextView) findViewById(R.id.oyaya);
        int id=view.getId();
        PopupWindow p=new PopupWindow();

        Bundle b=new Bundle();
        dispcontent d=new dispcontent();

        if (id==R.id.b1)
        {
            b.putString("b","Oye1");
            et.setText("Oyaya1");
        }
        else if (id==R.id.b2)
        {
            b.putString("b","Oye2");
            et.setText("Oyaya2");
        }
        else if (id==R.id.b3)
        {
            b.putString("b","Oye3");
            et.setText("Oyaya3");
        }

        d.setArguments(b);
    }


}
