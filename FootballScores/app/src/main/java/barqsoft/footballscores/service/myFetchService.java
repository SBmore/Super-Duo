package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class MyFetchService extends IntentService
{
    public final String LOG_TAG ="MyFetchService";
    public MyFetchService()
    {
        super("MyFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getData("n2");
        getData("p2");
    }

    private void getData(String timeFrame) {
        if (Utilities.isNetworkAvailable(this)) {
            //Creating fetch URL
            final String BASE_URL = getString(R.string.base_url); //Base URL
            final String QUERY_TIME_FRAME = getString(R.string.time_frame); //Time Frame parameter to determine days
//        final String QUERY_MATCH_DAY = getString(R.string.match_day);

            Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                    appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
            //Log.v(LOG_TAG, "The url we are looking at is: "+fetch_build.toString()); //log spam
            HttpURLConnection m_connection = null;
            BufferedReader reader = null;
            String JSON_data = null;
            //Opening Connection

            try {
                URL fetch = new URL(fetch_build.toString());
                m_connection = (HttpURLConnection) fetch.openConnection();
                m_connection.setRequestMethod(getString(R.string.get_call));
                m_connection.addRequestProperty(getString(R.string.request_property_field), getString(R.string.api_key));
                m_connection.connect();

                // Read the input stream into a String
                InputStream inputStream = m_connection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    builder.append(line).append("\n");
                }

                if (builder.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }

                JSON_data = builder.toString();
            } catch (Exception e) {
                Log.e(LOG_TAG, getString(R.string.exception_general) + e.getMessage());
            } finally {
                if (m_connection != null) {
                    m_connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, getString(R.string.error_closing_stream));
                    }
                }
            }

            try {
                if (JSON_data != null) {
                    //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                    JSONArray matches = new JSONObject(JSON_data).getJSONArray(getString(R.string.fixtures));
                    if (matches.length() == 0) {
                        //if there is no data, call the function on dummy data
                        //this is expected behavior during the off season.
                        processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                        return;
                    }

                    processJSONdata(JSON_data, getApplicationContext(), true);
                } else {
                    //Could not Connect
                    Log.d(LOG_TAG, getString(R.string.error_cannot_connect));
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    CharSequence text = getResources().getString(R.string.no_internet);
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(getApplicationContext(), text, duration).show();
                }
            });
        }
    }

    private void processJSONdata (String JSONdata,Context mContext, boolean isReal)
    {
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = getString(R.string.bundesliga_1_num);
        final String BUNDESLIGA2 = getString(R.string.bundesliga_2_num);
        final String LIGUE1 = getString(R.string.ligue_1_num);
        final String LIGUE2 = getString(R.string.ligue_2_num);
        final String PREMIER_LEAGUE = getString(R.string.premier_league_num);
        final String PRIMERA_DIVISION = getString(R.string.primera_division_num);
        final String SEGUNDA_DIVISION = getString(R.string.segunda_division_num);
        final String SERIE_A = getString(R.string.serie_a_num);
        final String PRIMERA_LIGA = getString(R.string.primera_liga_num);
        final String Bundesliga3 = getString(R.string.bundesliga_3_num);
        final String EREDIVISIE = getString(R.string.eredivisie_num);


        final String SEASON_LINK = getString(R.string.season_link_url);
        final String MATCH_LINK = getString(R.string.match_link_url);
        final String FIXTURES = getString(R.string.fixtures);
        final String LINKS =  getString(R.string.links);
        final String SOCCER_SEASON = getString(R.string.soccer_season);
        final String SELF = getString(R.string.self);
        final String MATCH_DATE = getString(R.string.match_date);
        final String HOME_TEAM = getString(R.string.home_team);
        final String AWAY_TEAM = getString(R.string.away_team);
        final String RESULT = getString(R.string.result);
        final String HOME_GOALS = getString(R.string.home_goals);
        final String AWAY_GOALS = getString(R.string.away_goals);
        final String MATCH_DAY = getString(R.string.match_day);

        //Match data
        String league = null;
        String mDate = null;
        String mTime = null;
        String home = null;
        String away = null;
        String homeGoals = null;
        String awayGoals = null;
        String matchId = null;
        String matchDay = null;


        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
            for(int i = 0;i < matches.length();i++)
            {

                JSONObject match_data = matches.getJSONObject(i);
                league = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString(getString(R.string.href));
                league = league.replace(SEASON_LINK,"");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if(     league.equals(PREMIER_LEAGUE)      ||
                        league.equals(SERIE_A)             ||
                        league.equals(BUNDESLIGA1)         ||
                        league.equals(BUNDESLIGA2)         ||
                        league.equals(PRIMERA_DIVISION)    ||
                        league.equals(SEGUNDA_DIVISION)    ||
                        league.equals(LIGUE1)              ||
                        league.equals(LIGUE2)              ||
                        league.equals(PRIMERA_LIGA)        ||
                        league.equals(Bundesliga3)         ||
                        league.equals(EREDIVISIE))
                {
                    matchId = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString(getString(R.string.href));
                    matchId = matchId.replace(MATCH_LINK, "");
                    if(!isReal){
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        matchId=matchId+Integer.toString(i);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf(getString(R.string.time_letter)) + 1,
                            mDate.indexOf(getString(R.string.zone_letter)));
                    mDate = mDate.substring(0,mDate.indexOf(getString(R.string.time_letter)));
                    SimpleDateFormat match_date = new SimpleDateFormat(getString(R.string.match_date_format));
                    match_date.setTimeZone(TimeZone.getTimeZone(getString(R.string.utc)));
                    try {
                        Date parsedDate = match_date.parse(mDate+mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat(getString(R.string.new_date_format));
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parsedDate);
                        mTime = mDate.substring(mDate.indexOf(getString(R.string.time_divider)) + 1);
                        mDate = mDate.substring(0,mDate.indexOf(getString(R.string.time_divider)));

                        if(!isReal){
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentDate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                            SimpleDateFormat mFormat = new SimpleDateFormat(getString(R.string.mformat_format));
                            mDate = mFormat.format(fragmentDate);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d(LOG_TAG, this.getString(R.string.error_general));
                        Log.e(LOG_TAG,e.getMessage());
                    }
                    home = match_data.getString(HOME_TEAM);
                    away = match_data.getString(AWAY_TEAM);
                    homeGoals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    awayGoals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    matchDay = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID,matchId);
                    match_values.put(DatabaseContract.scores_table.DATE_COL,mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL,mTime);
                    match_values.put(DatabaseContract.scores_table.HOME_COL,home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL,away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,homeGoals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,awayGoals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL,league);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY,matchDay);
                    //log spam

                    //Log.v(LOG_TAG,match_id);
                    //Log.v(LOG_TAG,mDate);
                    //Log.v(LOG_TAG,mTime);
                    //Log.v(LOG_TAG,Home);
                    //Log.v(LOG_TAG,Away);
                    //Log.v(LOG_TAG,Home_goals);
                    //Log.v(LOG_TAG,Away_goals);

                    values.add(match_values);
                }
            }
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI,insert_data);

            //Log.v(LOG_TAG,"Successfully Inserted : " + String.valueOf(inserted_data));
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }

    }
}

