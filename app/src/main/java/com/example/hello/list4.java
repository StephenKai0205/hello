package com.example.hello;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class list4<handler> extends ListActivity implements Runnable,AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {


    private static final String TAG = "conversion";
    SharedPreferences sp;
    String date;
    Set rate3;
    ListView listView;
    list<HashMap<String,String>> listItems;
    SimpleAdapter listItemAdapter;
    int position;


    int length = 3;
    public List<HashMap<String,String>> getRate2(){
        String url = "https://www.usd-cny.com/bankofchina.htm";
        Document doc = null;
        List<String> list = new ArrayList<>();
        List<HashMap<String,String>> listItems = null;
        try {
            doc = (Document) Jsoup.connect(url).get();
            Log.i(TAG, "getRate2: "+doc.title());
            Elements tables = doc.getElementsByTag("table");
            Element table = tables.get(0);//因为本网页只有一个table，等价于.first()
            Elements tds = table.getElementsByTag("td");//从table中找<td>，即列
            listItems = new ArrayList<HashMap<String, String>>();

            for (int i = 0;i < tds.size();i+=6){
                Element td1 = tds.get(i);
                Element td2 = tds.get(i+5);
                String str1 = td1.text();
                String val = td2.text();
                HashMap<String, String> map = new HashMap<String,
                        String>();
                map.put("ItemTitle", "" + str1); // 标题文字
                map.put("ItemDetail", "" + val); // 详情描述
                listItems.add(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listItems;
    }

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.list2);

        handler = new  Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 5) {

                    List<String> message = (List<String>)msg.obj;

                    sp = getSharedPreferences("rate", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putStringSet("allRate", new HashSet<String>((List<String>)msg.obj));
                    editor.putString("date",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    editor.apply();


                    List<HashMap<String,String>> str1 = (List<HashMap<String, String>>) msg.obj;

                    for(HashMap<String,String> s:str1){
                        String ss = s.get("itemTitle") + ":" + s.get("itemDetail");
                    }
                    SimpleAdapter listItemAdapter = new SimpleAdapter(list4.this,
                            str1, // listItems 数据源
                            R.layout.mylist2, // ListItem 的 XML 布局实现
                            new String[] { "ItemTitle", "ItemDetail" },
                            new int[] { R.id.itemTitle, R.id.itemDetail }
                    );
                    getListView().setAdapter(listItemAdapter);
                }
                super.handleMessage(msg);
            }
        };

        sp = getSharedPreferences("rate",Activity.MODE_PRIVATE);
        date = sp.getString("date","");
        Date date2 = new Date();
        SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd");
        String now = simple.format(date2);

        if(date==now){
            rate3 = sp.getStringSet("allRate",null);
            List<String> list =  new ArrayList(rate3);
            ListAdapter adapter = new ArrayAdapter<String>(list4.this,android.R.layout.simple_list_item_1,list);
            setListAdapter(adapter);
        }
        //不是同一天则从网络中获取数据，写入新的数据和日期
        else{
            Thread t = new Thread(this);
            t.start();
        }
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void run() {
        Log.i(TAG, "run: run()……");
        // 获取网络数据
        URL url = null;
        ArrayList<String> arrayList;
        List<HashMap<String,String>> list = getRate2();
        Message msg = handler.obtainMessage(5);
        //msg.what = 5;
        msg.obj = list;
        handler.sendMessage(msg);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object itemAtPosition = getListView().getItemAtPosition(position);
        HashMap<String,String> map = (HashMap<String, String>) itemAtPosition;
        String titleStr = map.get("ItemTitle");
        String detailStr = map.get("ItemDetail");
        Log.i(TAG, "onItemClick: titleStr=" + titleStr);
        Log.i(TAG, "onItemClick: detailStr=" + detailStr);


        TextView title = (TextView) view.findViewById(R.id.itemTitle);
        TextView detail = (TextView) view.findViewById(R.id.itemDetail);
        String title2 = String.valueOf(title.getText());
        String detail2 = String.valueOf(detail.getText());
        Log.i(TAG, "onItemClick: title2=" + title2);
        Log.i(TAG, "onItemClick: detail2=" + detail2);


        SharedPreferences sharedPreferences = getSharedPreferences("currency", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currency",title2);
        editor.putString("rate",detail2);
        editor.commit();
        Toast.makeText(this,"currency.XML is updated",Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.setClass(list4.this, list3.class);
        startActivity(intent);
     //   listItems.remove();
      //  listItemAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        new AlertDialog.Builder(this)
                .setTitle("删除数据")
                .setMessage("确定删除吗")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG,"onClick:对话框事件处理");
                     //  listItems.remove(position);
                        listItemAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("否",null);
      // builder.create().show();

        return false;
    }
}
