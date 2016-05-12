package com.miyaryj.android.calendarsample;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.Date;


public class LocalCalendarFragment extends ListFragment {
    public static final String TAG = "local_calendar_fragment";

    private static final String[] PROJECTION_EVENTS = {
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
    };

    private static final String SELECTION_EVENTS = "(" +
            "(" + CalendarContract.Events.DTSTART + " >= ?) AND " +
            "(" + CalendarContract.Events.DTEND + " <= ?)" +
            ")";

    private CursorAdapter mAdapter;

    private final LoaderManager.LoaderCallbacks<Cursor> mLoadCursorCallback = new LoaderManager
            .LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
            long start = System.currentTimeMillis();
            long end = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30 * 4;
            String[] selectionArgs = new String[]{Long.toString(start), Long.toString(end)};

            return new CursorLoader(getActivity(), CalendarContract.Events.CONTENT_URI,
                    PROJECTION_EVENTS, SELECTION_EVENTS, selectionArgs, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            if (cursor == null || mAdapter == null) {
                return;
            }
            mAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            if (mAdapter == null) {
                return;
            }
            mAdapter.swapCursor(null);
        }
    };

    public static LocalCalendarFragment newInstance() {
        LocalCalendarFragment fragment = new LocalCalendarFragment();
        return fragment;
    }

    public LocalCalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(R.string.fragment_local_calendar);

        mAdapter = new EventsAdapter(getActivity());
        setListAdapter(mAdapter);
        getActivity().getLoaderManager().initLoader(0, new Bundle(), mLoadCursorCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private static class ViewHolder {
        private TextView mText1;
        private TextView mText2;
    }

    private static class EventsAdapter extends CursorAdapter {
        private final LayoutInflater mInflator;

        public EventsAdapter(Context context) {
            super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
            mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View view = mInflator.inflate(android.R.layout.simple_list_item_2, parent, false);
            holder.mText1 = (TextView) view.findViewById(android.R.id.text1);
            holder.mText2 = (TextView) view.findViewById(android.R.id.text2);
            view.setTag(holder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
            long startTimeLong = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
            String startTimeString = new Date(startTimeLong).toString();

            ViewHolder holder = (ViewHolder) view.getTag();
            holder.mText1.setText(title);
            holder.mText2.setText(startTimeString);
        }
    }
}
