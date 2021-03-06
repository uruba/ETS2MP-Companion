package cz.uruba.ets2mpcompanion.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.uruba.ets2mpcompanion.MeetupDetailActivity;
import cz.uruba.ets2mpcompanion.R;
import cz.uruba.ets2mpcompanion.adapters.viewholders.LastUpdatedWithFilterInfoViewHolder;
import cz.uruba.ets2mpcompanion.interfaces.fragments.AbstractDataReceiverFragment;
import cz.uruba.ets2mpcompanion.interfaces.adapters.AbstractDataReceiverListAdapter;
import cz.uruba.ets2mpcompanion.interfaces.model.MeetupInfo;
import cz.uruba.ets2mpcompanion.views.LastUpdatedTextView;

public class MeetupListAdapter extends AbstractDataReceiverListAdapter<MeetupInfo> {
    public MeetupListAdapter(Context context, List<MeetupInfo> dataCollection, AbstractDataReceiverFragment<?, ?> callbackDataReceiver) {
        super(context, dataCollection, callbackDataReceiver);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        switch (viewType) {
            case TYPE_LAST_UPDATED:
                itemView = LayoutInflater
                        .from(context)
                        .inflate(R.layout.block_lastupdatedwithfilterinfo, parent, false);

                lastUpdatedTextView = (LastUpdatedTextView) itemView.findViewById(R.id.last_updated);

                return new LastUpdatedWithFilterInfoViewHolder(itemView);

            case TYPE_DATA_ENTRY:
                itemView = LayoutInflater
                        .from(context)
                        .inflate(R.layout.cardview_meetupinfo, parent, false);

                return new MeetupInfoViewHolder(itemView);
        }

        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_LAST_UPDATED:
                LastUpdatedWithFilterInfoViewHolder lastUpdatedWithFilterInfoViewHolder = (LastUpdatedWithFilterInfoViewHolder) holder;

                if (!TextUtils.isEmpty(filteringMessage) && callbackDataReceiver.getDataSetSize() > 0) {
                    lastUpdatedWithFilterInfoViewHolder.filteringStatus.setVisibility(View.VISIBLE);
                    lastUpdatedWithFilterInfoViewHolder.filteringStatus.setText(filteringMessage);
                } else {
                    lastUpdatedWithFilterInfoViewHolder.filteringStatus.setVisibility(View.GONE);
                }

                break;

            case TYPE_DATA_ENTRY:
                final MeetupInfo meetupInfo = dataCollection.get(position - 1);

                MeetupInfoViewHolder meetupInfoViewHolder = (MeetupInfoViewHolder) holder;

                meetupInfoViewHolder.time.setText(meetupInfo.getWhen());
                meetupInfoViewHolder.location.setText(meetupInfo.getLocation());
                meetupInfoViewHolder.server.setText(meetupInfo.getServer());
                meetupInfoViewHolder.organiser.setText(meetupInfo.getOrganiser());
                meetupInfoViewHolder.language.setText(meetupInfo.getLanguage());
                meetupInfoViewHolder.participants.setText(
                        String.format(
                                context
                                        .getResources()
                                        .getString(
                                                meetupInfo.getParticipants().equals("1") ?
                                                        R.string.participant_count_singular :
                                                        R.string.participant_count_plural),
                                meetupInfo.getParticipants()
                        )
                );

                if (TextUtils.isEmpty(meetupInfo.getRelativeURL())) {
                    meetupInfoViewHolder.more.setVisibility(View.GONE);
                } else {
                    meetupInfoViewHolder.more.setVisibility(View.VISIBLE);
                    meetupInfoViewHolder.more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu popup = new PopupMenu(context, v);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.show_meetup_detail:
                                            Intent meetupDetailIntent = new Intent(context, MeetupDetailActivity.class);
                                            meetupDetailIntent.putExtra(MeetupDetailActivity.INTENT_EXTRA_URL, meetupInfo.getAbsoluteURL());
                                            context.startActivity(meetupDetailIntent);
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });
                            MenuInflater inflater = popup.getMenuInflater();
                            inflater.inflate(R.menu.menu_meetup_entry, popup.getMenu());
                            popup.show();
                        }
                    });
                }
                break;
        }

        super.onBindViewHolder(holder, position);
    }

    public static class MeetupInfoViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.time) TextView time;
        @Bind(R.id.location) TextView location;
        @Bind(R.id.server) TextView server;
        @Bind(R.id.organiser) TextView organiser;
        @Bind(R.id.language) TextView language;
        @Bind(R.id.participants) TextView participants;
        @Bind(R.id.more) ImageView more;

        public MeetupInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
