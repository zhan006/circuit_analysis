package com.example.hp.tran;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by hp on 2018/2/18.
 */

public class swipecustom extends PagerAdapter{
    public int[] Imageresources=new int[]{R.drawable.dio,R.drawable.res,R.drawable.inductor,R.drawable.capacitor,R.drawable.imp,R.drawable.indinpara,R.drawable.caparalel};
    private Context ctx;
    public int position;
    public View itemview;
    public String cs;
    public swipecustom(Context c){
        ctx=c;
    }
    public String[] name={"n","resistor(series)","inductor(series)","capacitor(series)","resistor(parallel)","inductor(parallel)","capacitor(parallel)"};


    @Override
    public int getCount() {
        return Imageresources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container,int foot) {

        LayoutInflater layoutInflater=(LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        itemview=layoutInflater.inflate(R.layout.swipe_component,container,false);
        ImageView imageView=(ImageView)itemview.findViewById(R.id.component);
        TextView tex3=itemview.findViewById(R.id.text3);
        TextView tex4=itemview.findViewById(R.id.text4);
        imageView.setImageResource(Imageresources[foot]);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        tex3.setText(String.valueOf(foot));
        returnposition();
        tex4.setText(name[position]);
        container.addView(itemview);

        return itemview;

    }
    public void getposition(){
        TextView tex3=itemview.findViewById(R.id.text3);
        cs=tex3.getText().toString();
        position=Integer.parseInt(cs);
    }
    public int returnposition(){
        getposition();
        return  position;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }
}
