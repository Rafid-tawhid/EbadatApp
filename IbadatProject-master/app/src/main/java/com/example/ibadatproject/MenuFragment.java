package com.example.ibadatproject;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class MenuFragment extends Fragment {
    ExpandableListView expandableListView;
    List<String> listDataHeader;
    HashMap<String,List<String>> listDataChild;
    ListViewCustomAdapter listViewCustomAdapter;

    private int lastExpandedPosition=-1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_menu, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandableListView=view.findViewById(R.id.expandableListVieId);
        prepareData();
        listViewCustomAdapter=new ListViewCustomAdapter(getContext(),listDataHeader,listDataChild);
        expandableListView.setAdapter(listViewCustomAdapter);

        // header or group item click listener
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                String value=listDataHeader.get(groupPosition);
                //Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        //when Collapse
        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onGroupCollapse(int groupPosition) {
                String value=listDataHeader.get(groupPosition);
//                Toast.makeText(getActivity(), value +" item Collapse", Toast.LENGTH_SHORT).show();
//                LinearLayout lly=view.findViewById(R.id.singel_item);
//                lly.setBackgroundColor(Color.WHITE);
//                TextView text=view.findViewById(R.id.headerTextViewId);
//                text.setTextColor(R.color.clicked_item);
            }
        });

        //when Expand
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onGroupExpand(int groupPosition) {
                String value=listDataHeader.get(groupPosition);
//                Toast.makeText(getActivity(), value +" item Expand", Toast.LENGTH_SHORT).show();


                if (lastExpandedPosition!=-1 && lastExpandedPosition!=groupPosition){
                    expandableListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition=groupPosition;
            }
        });

        // child click
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String value= Objects.requireNonNull(listDataChild.get(listDataHeader.get(groupPosition))).get(childPosition);
//                Toast.makeText(getActivity(), value +" click ", Toast.LENGTH_SHORT).show();
                return false;
            }
        });


    }

    private void prepareData() {
        String[]headerString=getResources().getStringArray(R.array.list_header);
        String[]childString=getResources().getStringArray(R.array.list_child);
        listDataHeader=new ArrayList<>();
        listDataChild=new HashMap<>();
        for (int i=0;i<headerString.length;i++){
            listDataHeader.add(headerString[i]);
            List<String>child=new ArrayList<>();
            child.add(childString[i]);

            listDataChild.put(listDataHeader.get(i),child);

        }
    }
}