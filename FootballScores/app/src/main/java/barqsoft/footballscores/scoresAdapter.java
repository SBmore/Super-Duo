package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {
    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_LEAGUE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHDAY = 9;
    public double detail_match_id = 0;

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        String homeTeam = cursor.getString(COL_HOME);
        int homeGoals = cursor.getInt(COL_HOME_GOALS);
        String awayTeam = cursor.getString(COL_AWAY);
        int awayGoals = cursor.getInt(COL_AWAY_GOALS);
        String matchTime = cursor.getString(COL_MATCHTIME);
        String contentDescription;

        // Moizeé's feedback mentioned that the app isn't very accessible to those without perfect
        // vision so lots of content description changes have happened here

        mHolder.home_name.setText(homeTeam);
        mHolder.away_name.setText(awayTeam);
        mHolder.date.setText(matchTime);
        mHolder.date.setContentDescription("Match time: " + matchTime);
        mHolder.score.setText(Utilities.getScores(homeGoals, awayGoals));

        // make the content description read better depending on whether there is a score
        // available for this match
        if (homeGoals > -1 && awayGoals > -1) {
            mHolder.score.setContentDescription("Score: " + homeGoals + " " + awayGoals);
            contentDescription = matchTime + ", " + homeTeam + " " + homeGoals + ", " +
                    awayTeam + " " + awayGoals;
        } else {
            mHolder.score.setContentDescription("No score available.");
            contentDescription = matchTime + ", " + homeTeam + " versus " + awayTeam;
        }

        // give the whole list item a content description so clicking on it once will read the
        // whole information in a clear manner
        view.setContentDescription(contentDescription);
        mHolder.match_id = cursor.getDouble(COL_ID);

        String homeCrestDesc = homeTeam + " " + context.getString(R.string.team_crest);
        int homeCrestId = Utilities.getTeamCrestByTeamName(homeTeam);
        // make it clear that the image is a placeholder if the crest can't be found (same below)
        if (homeCrestId == R.drawable.ic_launcher) {
            homeCrestDesc = homeCrestDesc + " unavailable";
        }
        mHolder.home_crest.setImageResource(homeCrestId);
        mHolder.home_crest.setContentDescription(homeCrestDesc);

        String awayCrestDesc = awayTeam + " " + context.getString(R.string.team_crest);
        int awayCrestId = Utilities.getTeamCrestByTeamName(awayTeam);
        if (awayCrestId == R.drawable.ic_launcher) {
            awayCrestDesc = awayCrestDesc + " unavailable";
        }
        mHolder.away_crest.setImageResource(awayCrestId);
        mHolder.away_crest.setContentDescription(awayCrestDesc);

        //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        final ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);

        if (mHolder.match_id == detail_match_id) {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");

            String leagueName = Utilities.getLeague(context, cursor.getInt(COL_LEAGUE));
            String matchDay = Utilities.getMatchDay(cursor.getInt(COL_MATCHDAY),
                    cursor.getInt(COL_LEAGUE));

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);

            match_day.setText(matchDay);

            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(leagueName);
            Button share_button = (Button) v.findViewById(R.id.share_button);

            // add detail information to the content description for the list item
            contentDescription = contentDescription + ", League:  " + leagueName + " " + matchDay;
            view.setContentDescription(contentDescription);

            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(context, mHolder.home_name.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.away_name.getText() + " "
                            + context.getString(R.string.hashtag)));
                }
            });
        } else {
            container.removeAllViews();
        }

    }

    public Intent createShareForecastIntent(Context context, String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType(context.getString(R.string.share_intent_type));
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText);
        return shareIntent;
    }
}
