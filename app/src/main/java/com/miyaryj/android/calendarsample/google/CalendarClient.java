package com.miyaryj.android.calendarsample.google;

import android.text.TextUtils;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class CalendarClient {
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};

    private static final String APPLICATION_NAME = "Calendar Sample";

    private final GoogleService mGoogleService;

    public CalendarClient(GoogleService service) {
        if(service == null) {
            throw new IllegalArgumentException("Args cannot be null.");
        }

        mGoogleService = service;
    }

    public List<CalendarListEntry> getCalenders() throws IOException {
        CalendarList calendars = getService().calendarList().list()
                .setMaxResults(10)
                .execute();
        return calendars.getItems();
    }

    public Observable calendars() {
        return Observable.create(new Observable.OnSubscribe<List<CalendarListEntry>>() {
            @Override
            public void call(Subscriber<? super List<CalendarListEntry>> subscriber) {
                try {
                    subscriber.onNext(getCalenders());
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread());
    }

    public List<Event> getCalenderEvents(String calendarId) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());

        Events events = getService().events().list(calendarId)
                .setTimeMin(now)
                .setMaxResults(10)
                .execute();
        return events.getItems();
    }

    public Observable calendarEvents(final String calendarId) {
        return Observable.create(new Observable.OnSubscribe<List<Event>>() {
            @Override
            public void call(Subscriber<? super List<Event>> subscriber) {
                try {
                    subscriber.onNext(getCalenderEvents(calendarId));
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread());
    }

    private Calendar getService() throws NoAccountException {
        if (TextUtils.isEmpty(mGoogleService.getAccountName())) {
            throw new NoAccountException();
        }

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Calendar.Builder(
                transport, jsonFactory, mGoogleService.getCredential(SCOPES))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
