/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.functional.IOnListModeChangeListener;
import com.gabm.fancyplaces.functional.OnFancyPlaceSelectedListener;
import com.melnykov.fab.FloatingActionButton;

/**
 * Created by gabm on 15/05/15.
 */
public class FPListView extends TabItem {

    private OnFancyPlaceSelectedListener fancyPlaceSelectedCallback = null;
    private ListView fancyPlacesList = null;
    private MainWindow parent = null;
    private IOnListModeChangeListener onListModeChangeListener = null;



    public static FPListView newInstance() {
        FPListView result = new FPListView();

        return result;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fancy_places_list_view, container, false);

        // add places to list
        fancyPlacesList = (ListView) v.findViewById(R.id.fp_list_view);
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fp_list_fab);
        fab.attachToListView(fancyPlacesList);

        // set on click listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fancyPlaceSelectedCallback.onFancyPlaceSelected(0, OnFancyPlaceSelectedListener.INTENT_CREATE_NEW);
            }
        });

        //registerForContextMenu(fancyPlacesList);

        // set adapter
        fancyPlacesList.setAdapter(parent.fancyPlaceArrayAdapter);
        //fancyPlacesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // add on mode change listener
        onListModeChangeListener = parent.fancyPlaceArrayAdapter;

        changeListMode(IOnListModeChangeListener.MODE_NORMAL);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            fancyPlaceSelectedCallback = (MainWindow) activity;
            parent = (MainWindow) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.fp_list_view_title);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        menu.setHeaderTitle(parent.fancyPlaceArrayAdapter.getItem(info.position).getTitle());

        String[] menuItems = {getString(R.string.context_menu_delete)/*, getString(R.string.context_menu_share)*/, "Export to GPX"};

        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        if (menuItemIndex == 0) {
            // delete
            fancyPlaceSelectedCallback.onFancyPlaceSelected(info.position, OnFancyPlaceSelectedListener.INTENT_DELETE);
        } else if (menuItemIndex == 1) {
            // share
            //fancyPlaceSelectedCallback.onFancyPlaceSelected(info.position, OnFancyPlaceSelectedListener.INTENT_SHARE);
            fancyPlaceSelectedCallback.onFancyPlaceSelected(info.position, OnFancyPlaceSelectedListener.INTENT_EXPORT_TO_GPX);
        }
        return true;
    }

    protected void changeListMode(int newMode) {
        if (newMode == IOnListModeChangeListener.MODE_NORMAL) {
            // add click listener
            fancyPlacesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    fancyPlaceSelectedCallback.onFancyPlaceSelected(position, OnFancyPlaceSelectedListener.INTENT_VIEW);
                }
            });

            fancyPlacesList.setOnItemLongClickListener(
                    new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                            changeListMode(IOnListModeChangeListener.MODE_MULTI_SELECT);
                            parent.fancyPlaceArrayAdapter.toggleSelected(i);
                            return true;
                        }
                    });
        }
        if (newMode == IOnListModeChangeListener.MODE_MULTI_SELECT) {
            fancyPlacesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    parent.fancyPlaceArrayAdapter.toggleSelected(i);
                }
            });
            fancyPlacesList.setOnItemLongClickListener(null);
        }

        onListModeChangeListener.onListModeChange(newMode);
    }

}