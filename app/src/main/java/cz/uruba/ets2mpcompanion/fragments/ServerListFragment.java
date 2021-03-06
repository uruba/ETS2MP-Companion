package cz.uruba.ets2mpcompanion.fragments;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.uruba.ets2mpcompanion.R;
import cz.uruba.ets2mpcompanion.adapters.ServerListAdapter;
import cz.uruba.ets2mpcompanion.constants.URL;
import cz.uruba.ets2mpcompanion.filters.ServerFilter;
import cz.uruba.ets2mpcompanion.interfaces.DataReceiverJSON;
import cz.uruba.ets2mpcompanion.interfaces.filters.FilterCallback;
import cz.uruba.ets2mpcompanion.interfaces.fragments.AbstractDataReceiverFragment;
import cz.uruba.ets2mpcompanion.model.ServerInfo;
import cz.uruba.ets2mpcompanion.model.general.DataSet;
import cz.uruba.ets2mpcompanion.tasks.FetchServerListTask;
import cz.uruba.ets2mpcompanion.tasks.FetchServerTimeTask;

public class ServerListFragment extends AbstractDataReceiverFragment<ServerInfo, ServerListAdapter> implements FilterCallback<ServerInfo> {
    @Bind(R.id.recyclerview_serverlist) RecyclerView serverList;

    private ServerFilter serverFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_serverlist, container, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        ButterKnife.bind(this, view);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchDataList(true);
                submitOnRefreshAnalytics("Server list");
            }
        });

        serverList.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));

        listAdapter = new ServerListAdapter(getContext(), new ArrayList<ServerInfo>(), this);
        serverList.setAdapter(listAdapter);

        serverFilter = new ServerFilter(getContext(), this);

        fetchServerTime();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_game_filter, menu);

        menuItems.add(menu.findItem(R.id.action_game_filter));
        showMenuItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_game_filter:
                serverFilter.showFilterDialog(dataSet.getCollection());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void fetchDataList(boolean notifyUser) {
        showLoadingOverlay();

        new FetchServerListTask(this, URL.SERVER_LIST, notifyUser).execute();
    }

    private void fetchServerTime() {
        new FetchServerTimeTask(new DataReceiverJSON<Date>() {
            private final Date lastUpdated = new Date();

            @Override
            public void processData(Date data, boolean notifyUser) {
                listAdapter.setServerTime(data);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void handleIOException(IOException e) {

            }

            @Override
            public void handleJSONException(JSONException e) {

            }

            @Override
            public Date getLastUpdated() {
                return lastUpdated;
            }
        }, URL.GAME_TIME, false).execute();
    }

    @Override
    public void handleReceivedData(DataSet<ServerInfo> serverList, boolean notifyUser) {
        fetchServerTime();

        dataSet = serverList;

        Collections.sort(dataSet.getCollection(), Collections.reverseOrder());
        listAdapter.resetDataCollection(new ArrayList<>(dataSet.getCollection()));

        serverFilter.filterByGame(dataSet.getCollection());

        if (notifyUser) {
            Snackbar.make(fragmentWrapper, this.getResources().getString(R.string.server_list_refreshed), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void dataFiltered(DataSet<ServerInfo> data) {
        listAdapter.setDataCollection(data.getCollection());

        String gameLiteral = serverFilter.getCurrentGameLiteral();

        if (!TextUtils.isEmpty(gameLiteral)) {
            listAdapter.setFilteringMessage(
                    String.format(
                            getString(R.string.filtering_status),
                            gameLiteral
                    )
            );
        } else {
            listAdapter.setFilteringMessage();
        }

        serverList.scrollToPosition(0);
    }
}