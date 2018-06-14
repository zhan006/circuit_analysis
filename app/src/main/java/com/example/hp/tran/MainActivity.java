package com.example.hp.tran;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import java.util.Arrays;
import java.util.ArrayList;
import java.math.*;
import java.util.stream.*;
import android.view.inputmethod.InputMethodManager;

import flanagan.circuits.Impedance;
import flanagan.complex.*;



public class MainActivity extends AppCompatActivity {
    private ImageSwitcher is1,is2,is3,is4,is5,is6,is7,is8,is9,is10;/* ten screen to display the component*/
    private Button btn,del,setvalue,run;   /*btn indicate the set button setting component,del used to delete component*/
    private EditText voltage,frequency;
    private TextView testb,setresistance,unit,current,p,q;
    public int numberlp=0;
    public int totalnumber=10;
    public double resi=0,creactance=0,lreactance=0;/*indicating resistance, capacitance, inductance respectively*/
    public int elemnumber=0;
    private NumberPicker np;/* the numberpiker used for selecting number*/
    private int[] gall=new int[]{R.drawable.dio,R.drawable.res,R.drawable.inductor,
            R.drawable.capacitor,R.drawable.imp,R.drawable.indinpara,R.drawable.caparalel};/* An Array that contains the picture of circuit elements*/
    private int[] resvalue=new int[totalnumber];/*resistor in series value*/
    private int[] indvalue=new int[totalnumber];/*inductor in series value*/
    private int[] capvalue=new int[totalnumber];/*capacitor in series value*/
    private int[] loopflag=new int[totalnumber];/*indicate the imageswicher id contains paralel*/
    private double[] resistance=new double[totalnumber];/* resistance in series, should be same with resvalue*/
    private double[] capacitance=new double[totalnumber];/*capacitance in series, value calculated by 1/2*pi*f*C */
    private double[] inductance=new double[totalnumber];/* inductance in series, value calculated by 2*pi*f*L*/

    private Complex[] impedance=Complex.oneDarray(totalnumber);/* total impedance of an element,real part as resistance imagine part as reactance */
    private Complex[] Currentinloop;/* current flow through each element */
    private int runindicator;/* indicates whether the analysis has been done or not*/
    public ImageSwitcher[] iss=new ImageSwitcher[totalnumber];
    public int[] elements=new int[totalnumber];

    ViewPager viewPager;/*using view pager to display the selectable component*/
    swipecustom adapter;/* scrollview adapter */
    public int position;/*indicate the type of component*/
    public ImageView source;
    public Complex Current,currentinsingle;//* Current in the Imatrix;Current in a single loop circuit */
    int Voltage=10,Resistance,Inductance,Capacitance,Frequency=50;
    public ComplexMatrix Imatrix,Zmatrix,Vmatrix;
    public String unitR="Ω",unitC="mF",unitL="mH";
    public int isnumber=0;
    public RelativeLayout Rl;




    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter=new swipecustom(this);
        viewPager=(ViewPager)findViewById(R.id.viepager);
        viewPager.setAdapter(adapter);

        voltage=(EditText)findViewById(R.id.voltage);
        frequency=(EditText)findViewById(R.id.frequency);
        Rl=(RelativeLayout)findViewById(R.id.testwhat);
        btn=(Button)findViewById(R.id.btn);
        run=(Button)findViewById(R.id.run);
        del=(Button)findViewById(R.id.del);
        setresistance=(TextView)findViewById(R.id.setresistance);
        setvalue=(Button)findViewById(R.id.setvalue);
        np=(NumberPicker)findViewById(R.id.np);
        source=(ImageView)findViewById(R.id.source);
        testb=(TextView) findViewById(R.id.testb);/*for test*/
        unit=(TextView)findViewById(R.id.unit);/*display unit*/
        current=(TextView) findViewById(R.id.current);
        p=(TextView)findViewById(R.id.p);
        q=(TextView)findViewById(R.id.q);
        runindicator=0;



        /* pass the value of index in viewpager to the value of index of element array*/
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int step) {
                position=step;
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

       /* set the voltage and frequency*/
        voltage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Voltage=Integer.parseInt(voltage.getText().toString());
                return false;
            }
        });
        frequency.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Frequency=Integer.parseInt(frequency.getText().toString());
                return false;
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runindicator=0;/* analysis hasnt done */
                /*the step is limited by 9, and set the type of element into the contentdescription attribute*/
                if(isnumber<totalnumber) {
                    iss[isnumber] = new ImageSwitcher(getApplicationContext());
                    iss[isnumber].setFactory(new ViewSwitcher.ViewFactory() {
                        @Override
                        public View makeView() {
                            ImageView imageview = new ImageView(getApplicationContext());
                            imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            return imageview;
                        }
                    });
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(200, 400);
                    lp.setMargins(255 + isnumber * 200, 10, 10, 10);

                    elements[isnumber] = position;
                    if (position == 4 | position == 5 | position == 6) {
                        loopflag[isnumber] = position;
                        numberlp++;
                    }
                    iss[isnumber].setLayoutParams(lp);
                    Rl.addView(iss[isnumber]);
                    for(int i=0;i<isnumber+1;i++){
                        iss[i].setOnClickListener(new myonclicklistener(i));
                    }

                    isnumber++;
                    elemnumber=max(isnumber,elemnumber);
                    for (int i = 0; i <elemnumber; i++) {
                        if(elements[i]!=0) {
                            iss[i].setImageResource(gall[elements[i]]);
                        }
                        else{iss[i].setImageDrawable(null);}
                    testb.setText(Arrays.toString(elements));
                    }
                }
                else{isnumber=0;}

            }
        });
        /* reinitialize all the value and image*/
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runindicator=0;
                if(isnumber==elemnumber){isnumber--;}
                resvalue[isnumber]=0;
                indvalue[isnumber]=0;
                capvalue[isnumber]=0;
                impedance[isnumber]=new Complex(0,0);
                elements[isnumber]=0;
                elements=makenewelements(elements);
                iss[isnumber].setTransitionName("NA");
                for(int i=0;i<elemnumber;i++){
                    if(elements[i]!=0) {
                        iss[i].setImageResource(gall[elements[i]]);
                    }
                    else{iss[i].setImageDrawable(null);}
                }
                testb.setText(Arrays.toString(elements));
                if(position==4|position==5|position==6){
                    loopflag[isnumber]=0;
                    numberlp--;
                }
                if(isnumber>iss.length-1){
                    isnumber=0;
                }

            }
        });
        setvalue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runindicator=0;
                /*set value for resistor*/
                if(elements[isnumber]==1|elements[isnumber]==4){
                    resvalue[isnumber]=np.getValue();
                }
                /*set value for inductor*/
                if(elements[isnumber]==2|elements[isnumber]==5){
                    indvalue[isnumber]=np.getValue();
                }
                /*set value for capacitor*/
                if(elements[isnumber]==3|elements[isnumber]==6){
                    capvalue[isnumber]=np.getValue();
                }
                /*make the impedance matrix and voltage matrix */
                impedance=getImpedance();
                Zmatrix=makematrix();
                if(numberlp>0) {
                    Vmatrix = vmatrix();
                }
                /*initialize the Imatrix */
                Complex[] i=Complex.oneDarray(numberlp);
                Imatrix=ComplexMatrix.columnMatrix(i);
                testb.setText(String.valueOf(np.getValue()));


            }
        });
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runindicator=1;/*mark the analysis has been done*/
                /* alert for uncompleted circuit*/
                if(numberlp==0){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Alert ");
                    builder.setMessage("You need" +
                            " to complete circuit with element in parallel");
                    builder.setPositiveButton("confirm", null);
                    builder.show();
                }
                /* calculate the current,power for single loop circuit*/
                if(numberlp==1){
                    Complex sum=new Complex(0,0);
                    for(int i=0;i<impedance.length;i++){
                        sum=impedance[i].plus(sum);
                    }
                    Complex xiaov=new Complex(Voltage,0);
                    Complex xiaoi=xiaov.over(sum);
                    currentinsingle=xiaoi;
                    double a=xiaov.times(xiaoi.conjugate()).getReal();
                    double b=xiaov.times(xiaoi.conjugate()).getImag();
                    String realpower=String.format("%.2f",a);
                    String reacpower=String.format("%.2f",b);
                    String c=complexform(xiaoi);
                    current.setText(c);
                    p.setText(realpower);
                    q.setText(reacpower);

                }
                /*calculate the current, power for circuit has more than 1 loop*/
                if(numberlp>1){
                    Imatrix=Zmatrix.inverse().times(Vmatrix);
                    p.setText(power());
                    q.setText(qower());
                    String c=complexform(Imatrix.getElementReference(0,0));
                    int[] indicator=getpureindicator();
                    testb.setText(Arrays.toString(indicator));
                    int j=0;
                    int flag=0;
                    Currentinloop=new Complex[iss.length];
                    Complex ooo=Imatrix.getElementReference(0,0).minus(Imatrix.getElementReference(1,0));
                    for(int i=0;i<iss.length;i++){
                        if(islist(indicator,i)){
                            if(flag>=numberlp-1){
                                iss[i].setTransitionName(complexform(Imatrix.getElementReference(flag,0)));
                                Currentinloop[i]=Imatrix.getElementReference(flag,0);
                            }
                            if(flag<numberlp-1){
                                Complex I=Imatrix.getElementReference(flag,0).minus(Imatrix.getElementReference(flag+1,0));
                                iss[i].setTransitionName(complexform(I));
                                Currentinloop[i]=I;
                                flag+=1;}
                        }
                        else{
                            iss[i].setTransitionName(complexform(Imatrix.getElementReference(flag,0)));
                            Currentinloop[i]=Imatrix.getElementReference(flag,0);
                        }
                    }
                    current.setText(c);
                }
            }
        });
        source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        /* add click listener for piles of imageswitcher*/



        np.setMaxValue(1000);
        np.setMinValue(0);

    }


    /*get the voltage*/
    public int getVoltage(){
       int thevalue=Integer.parseInt(voltage.getText().toString());
       return thevalue;
    }
    /*get the frequency*/
    public int getFrequency(){
        int thevalue=Integer.parseInt(frequency.getText().toString());
        return thevalue;
    }
    /*make an array of resistance*/
    public double[] getResistance(){
        int j=0;
        for(int i:resvalue){
            resistance[j++]=i;
        }
        return resistance;
    }
    /*make an array of inductance*/
    public double[] getInductance(){
        int j=0;
        for(int i:indvalue){
            inductance[j++]=i*Frequency*2*Math.PI/1000;
        }
        return inductance;
    }
   /*make an array of capacitance*/
    public double[] getCapacitance(){
        int j=0;
        for(int i:capvalue){
            if(i==0){
                capacitance[j++]=0;
            }
            else{
                capacitance[j++]=1000/(i*Frequency*2*Math.PI);
            }
        }
        return capacitance;
    }
/* make an array of impedance*/
    public Complex[]getImpedance(){
        resistance=getResistance();
        inductance=getInductance();
        capacitance=getCapacitance();
        int j=0;
        for(;j<totalnumber;j++){
            impedance[j]=new Complex(resistance[j],inductance[j]-capacitance[j]);
        }

        return impedance;}
/*transfer the complex number into magnitude and angle form */
    public String complexform(Complex c){
        double mag=c.abs();
        double theta=Math.atan(c.getImag()/c.getReal());
        String result=String.format("%.2f∠%.2f",mag,theta);
        return result;
    }
/* power calculation*/
    public String power(){
        Complex v=new Complex(Voltage,0);
        Current=Imatrix.getElementReference(0,0);
        Complex po=Current.conjugate().times(v);
        String result=String.format("%.2f",po.getReal());
        return result;
    }
/*reactive power calculation*/
    public String qower(){
        Complex v=new Complex(Voltage,0);
        Current=Imatrix.getElementReference(0,0);
        Complex po=Current.conjugate().times(v);
        String result=String.format("%.2f",po.getImag());
        return result;
    }
    /*return the values of diagonal line of Zmatrix*/
    public Complex[] loopidentify(){
        int[] sortloop=Arrays.copyOf(loopflag,totalnumber);
        Arrays.sort(sortloop);
        Complex[] selfimp=Complex.oneDarray(totalnumber-1);
        Complex[] selfimpedance=Complex.oneDarray(numberlp);
        for(int i=0;i<totalnumber-1;i++){
            int min=sortloop[i];
            int max=sortloop[i+1];
            for(int j=min;j<=max&j>=min;j++){
                selfimp[i]=selfimp[i].plus(impedance[j]);
            }
        }
        for(int j=0;j<numberlp;j++){
            selfimpedance[j]=selfimp[totalnumber-1-numberlp+j];
        }
        return selfimpedance;

    }
    /*make a impedance matrix*/
    public ComplexMatrix makematrix(){
        ComplexMatrix Zmatrix=new ComplexMatrix(numberlp,numberlp);
        Complex[] element=loopidentify();
        int[] sortloop=Arrays.copyOf(loopflag,totalnumber);
        Arrays.sort(sortloop);
        int[] a=new int[numberlp];
        for(int j=0;j<numberlp;j++){
            a[j]=sortloop[totalnumber-numberlp+j];
        }
        for(int i=0;i<numberlp;i++){
            for(int j=0;j<numberlp;j++){
                if(i==j){
                    Zmatrix.setElement(i,i,element[i]);
                }
                if(j==i+1){
                    Zmatrix.setElement(i,j,impedance[a[i]].times(-1));
                    Zmatrix.setElement(j,i,impedance[a[i]].times(-1));
                }
            }
        }
        return Zmatrix;
    }
    /*make the voltage matrix*/
    public ComplexMatrix vmatrix(){
        Complex[] aa=Complex.oneDarray(numberlp);
        aa[0]=new Complex(Voltage,0);
        ComplexMatrix Vmatrix=ComplexMatrix.columnMatrix(aa);
        return Vmatrix;
    }
    public int[] makenewelements(int[] array){
        int[] newarray=new int[totalnumber];
        int a=0;
        for(int i:array){
            if(i!=0){
                newarray[a++]=i;
            }
        }
        return  newarray;
    }
    /*indicate the the appearance of element in parallel*/
    public int[] getpureindicator(){
        int[] b=new int[numberlp];
        int[] sortloop=Arrays.copyOf(loopflag,totalnumber);
        Arrays.sort(sortloop);
        for(int i=0;i<numberlp;i++){
            b[i]=sortloop[totalnumber-numberlp+i];
        }
        return b;
    }
    public int lengthof(int[] array){
        int len=0;
        for(int i:array){
            if(i!=0){len++;}
            else{}
        }
        return len;
    }
    /*judge whether the element in circuit is the node*/
    public boolean islist(int[] a,int b){
        for(int i:a){
            if(i==b){
                return true;
            }
        }
        return false;

    }
    public int max(int a,int b){
        if(a>b){
            return a;
        }
        return b;
    }
    /*click listener for piles of imageswicher*/
    private class myonclicklistener implements View.OnClickListener{
        private int temp;
        public myonclicklistener(int i){
            temp=i;
        }

        @Override
        public void onClick(View v) {
            setresistance.setVisibility(View.INVISIBLE);
            setvalue.setVisibility(View.INVISIBLE);
            np.setVisibility(View.INVISIBLE);
            isnumber=temp;
            resi=temp;

            if(runindicator==1){
                if(numberlp>1) {
                    Complex vtg = Currentinloop[temp].times(impedance[temp]);
                    Complex pw = vtg.times(Currentinloop[temp].conjugate());
                    p.setText(String.format("%.2f", pw.getReal()));
                    q.setText(String.format("%.2f", pw.getImag()));
                    current.setText(complexform(Currentinloop[temp]));
                }
                if(numberlp==1){
                    Complex vtg =  currentinsingle.times(impedance[temp]);
                    Complex pw=vtg.times(currentinsingle.conjugate());
                    p.setText(String.format("%.2f",pw.getReal()));
                    q.setText(String.format("%.2f",pw.getImag()));

                }



            }
            if(elements[temp]==1|elements[temp]==4){
                setvalue.setVisibility(View.VISIBLE);
                setresistance.setVisibility(View.VISIBLE);
                np.setVisibility(View.VISIBLE);
                unit.setText(unitR);
                unit.setVisibility(View.VISIBLE);}
            if(elements[temp]==2|elements[temp]==5){
                setvalue.setVisibility(View.VISIBLE);
                setresistance.setVisibility(View.VISIBLE);
                np.setVisibility(View.VISIBLE);
                unit.setText(unitL);
                unit.setVisibility(View.VISIBLE);
            }
            if(elements[temp]==3|elements[temp]==6){
                setvalue.setVisibility(View.VISIBLE);
                setresistance.setVisibility(View.VISIBLE);
                np.setVisibility(View.VISIBLE);
                unit.setText(unitC);
                unit.setVisibility(View.VISIBLE);
            }
        }
    }

}


