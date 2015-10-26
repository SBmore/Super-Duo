package barqsoft.footballscores.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by Steven on 11/10/2015 using Tutorial by Dharmang Soni
 * http://dharmangsoni.blogspot.co.uk/2014/03/collection-widget-with-event-handling.html
 */
@SuppressLint("NewApi")
public class WidgetDataProvider implements RemoteViewsFactory {
    public static final int COL_MATCHTIME = 0;
    public static final int COL_HOME = 1;
    public static final int COL_HOME_GOALS = 2;
    public static final int COL_AWAY = 3;
    public static final int COL_AWAY_GOALS = 4;

    List mCollections = new ArrayList();

    Context mContext = null;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mCollections.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
        mView.setTextViewText(R.id.widget_list_text, (CharSequence) mCollections.get(position));

        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(WidgetProvider.ACTION_TOAST);
        final Bundle bundle = new Bundle();
        bundle.putString(WidgetProvider.EXTRA_STRING, (String) mCollections.get(position));
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(R.id.widget_list_text, fillInIntent);
        return mView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {

        // Jennie mentioned hin her feedback that it would be nice to see the scrores without
        // having to open the app so here is a collection widget that will show todays data
        final long dateInMillis = new LocalDate().toDateTimeAtStartOfDay().getMillis();
        final String[] projection = {
                DatabaseContract.scores_table.TIME_COL,
                DatabaseContract.scores_table.HOME_COL,
                DatabaseContract.scores_table.HOME_GOALS_COL,
                DatabaseContract.scores_table.AWAY_COL,
                DatabaseContract.scores_table.AWAY_GOALS_COL,
        };
        Thread thread = new Thread() {
            public void run() {
                Cursor widgetDataCursor = mContext.getContentResolver().query(
                        DatabaseContract.scores_table.buildScoreWithDate(),
                        projection,
                        DatabaseContract.scores_table.DATE_COL + " = ?",
                        new String[]{Utilities.getDateInMillis(dateInMillis)},
                        null);

                String widgetText;
                mCollections.clear();
                if (widgetDataCursor.moveToFirst()) {
                    while (!widgetDataCursor.isAfterLast()) {
                        String matchTime = widgetDataCursor.getString(COL_MATCHTIME);
                        String homeTeam = widgetDataCursor.getString(COL_HOME);
                        int homeGoals = widgetDataCursor.getInt(COL_HOME_GOALS);
                        String awayTeam = widgetDataCursor.getString(COL_AWAY);
                        int awayGoals = widgetDataCursor.getInt(COL_AWAY_GOALS);

                        if (homeGoals > -1 && awayGoals > -1) {
                            widgetText = "[" + matchTime + "] " + homeTeam + ": " + homeGoals
                                    + " - " + awayTeam + ": " + awayGoals;
                        } else {
                            widgetText = "[" + matchTime + "] " + homeTeam + " v " + awayTeam;
                        }
                        mCollections.add(widgetText);
                        widgetDataCursor.moveToNext();
                    }

                } else {
                    mCollections.add("No data avalailable.");
                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void onDestroy() {

    }

}