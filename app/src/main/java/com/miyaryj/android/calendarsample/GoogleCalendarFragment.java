package com.miyaryj.android.calendarsample;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.miyaryj.android.calendarsample.google.GoogleService;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class GoogleCalendarFragment extends ListFragment {
    public static final String TAG = "calendar_fragment";

    public static final String TAG_EVENTS = "calendar_fragment_events";

    private static final String KEY_CALENDAR_ID = "key_calendar_id";

    private GoogleService mService;

    private Subscription mSubscription;

    private String mCalendarId;

    private ArrayAdapter mAdapter;

    public static GoogleCalendarFragment newInstance() {
        GoogleCalendarFragment fragment = new GoogleCalendarFragment();
        return fragment;
    }

    public static GoogleCalendarFragment newInstance(String calendarId) {
        GoogleCalendarFragment fragment = new GoogleCalendarFragment();
        Bundle args = new Bundle();
        args.putString(KEY_CALENDAR_ID, calendarId);
        fragment.setArguments(args);
        return fragment;
    }

    public GoogleCalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO Should check more strictly
        mService = ((MainActivity) getActivity()).getGoogleService();

        if (getArguments() != null) {
            mCalendarId = getArguments().getString(KEY_CALENDAR_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.fragment_google_calendar);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscription = fetch();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSubscription.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mAdapter == null) {
            return;
        }

        Object item = mAdapter.getItem(position);
        if (item instanceof CalendarListEntry) {
            startEventsFragment(((CalendarListEntry) item).getId());
        }
    }

    private Subscription fetch() {
        if (TextUtils.isEmpty(mCalendarId)) {
            return fetchCalendars();
        } else {
            return fetchEvents(mCalendarId);
        }
    }

    private Subscription fetchCalendars() {
        return mService.calendar().calendars()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<CalendarListEntry>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onNext(List<CalendarListEntry> result) {
                        mAdapter = new CalendarsAdapter(getActivity(), result);
                        setListAdapter(mAdapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mService.respond(e);
                    }
                });
    }

    private Subscription fetchEvents(String calendarId) {
        return mService.calendar().calendarEvents(calendarId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Event>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onNext(List<Event> result) {
                        mAdapter = new EventsAdapter(getActivity(), result);
                        setListAdapter(mAdapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mService.respond(e);
                    }
                });
    }

    private void startEventsFragment(String calendarId) {
        Fragment f = newInstance(calendarId);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, f, TAG_EVENTS)
                .addToBackStack(TAG).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private static class ViewHolder {
        private TextView mText1;
        private TextView mText2;
    }

    private static class CalendarsAdapter extends ArrayAdapter<CalendarListEntry> {
        private final LayoutInflater mInflator;

        public CalendarsAdapter(Context context, List<CalendarListEntry> calendars) {
            super(context, 0, calendars);
            mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflator.inflate(android.R.layout.simple_list_item_2, parent, false);
                holder = new ViewHolder();
                holder.mText1 = (TextView) convertView.findViewById(android.R.id.text1);
                holder.mText2 = (TextView) convertView.findViewById(android.R.id.text2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CalendarListEntry calendar = getItem(position);
            holder.mText1.setText(calendar.getSummary());
            holder.mText2.setText(calendar.getId());
            return convertView;
        }
    }

    private static class EventsAdapter extends ArrayAdapter<Event> {
        private final LayoutInflater mInflator;

        public EventsAdapter(Context context, List<Event> events) {
            super(context, 0, events);
            mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflator.inflate(android.R.layout.simple_list_item_2, parent, false);
                holder = new ViewHolder();
                holder.mText1 = (TextView) convertView.findViewById(android.R.id.text1);
                holder.mText2 = (TextView) convertView.findViewById(android.R.id.text2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Event event = getItem(position);
            holder.mText1.setText(event.getSummary());
            holder.mText2.setText(event.getStart().toString());
            return convertView;
        }
    }
}
