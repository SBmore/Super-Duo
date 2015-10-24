package barqsoft.footballscores;

import android.content.Context;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities {
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int BUNDESLIGA1 = 394;
    public static final int BUNDESLIGA2 = 395;
    public static final int LIGUE1 = 396;
    public static final int LIGUE2 = 397;
    public static final int PREMIER_LEAGUE = 398;
    public static final int PRIMERA_DIVISION = 399;
    public static final int SEGUNDA_DIVISION = 400;
    public static final int SERIE_A = 401;
    public static final int PRIMERA_LIGA = 402;
    public static final int Bundesliga3 = 403;
    public static final int EREDIVISIE = 404;

    public static final String ARSENAL = "Arsenal London FC";
    public static final String ASTON = "Aston Villa FC";
    public static final String CHELSEA = "Chelsea FC";
    public static final String CRYSTAL = "Crystal Palace FC";
    public static final String EVERTON = "Everton FC";
    public static final String LEICESTER = "Leicester City FC";
    public static final String LIVERPOOL = "Liverpool FC";
    public static final String MAN_C = "Manchester City FC";
    public static final String MAN_U = "Manchester United FC";
    public static final String NEWCASTLE = "Newcastle United FC";
    public static final String SOUTHAMPTON = "Southampton FC";
    public static final String STOKE = "Stoke City FC";
    public static final String SUNDERLAND = "Sunderland AFC";
    public static final String SWANSEA = "Swansea City";
    public static final String TOTTENHAM = "Tottenham Hotspur FC";
    public static final String WEST_BROM = "West Bromwich Albion";
    public static final String WEST_HAM = "West Ham United FC";

    public static String getLeague(Context context, int league_num) {

        switch (league_num) {
            case BUNDESLIGA1 : return context.getString(R.string.bundesliga_1_name);
            case BUNDESLIGA2 : return context.getString(R.string.bundesliga_2_name);
            case LIGUE1 : return context.getString(R.string.ligue_1_name);
            case LIGUE2 : return context.getString(R.string.ligue_2_name);
            case PREMIER_LEAGUE : return context.getString(R.string.premier_league_name);
            case CHAMPIONS_LEAGUE : return context.getString(R.string.champions_league_name);
            case PRIMERA_DIVISION : return context.getString(R.string.primera_division_name);
            case SEGUNDA_DIVISION : return context.getString(R.string.secunda_division_name);
            case SERIE_A : return context.getString(R.string.serie_a_name);
            case PRIMERA_LIGA : return context.getString(R.string.primera_liga_name);
            case Bundesliga3 : return context.getString(R.string.bundesliga_3_name);
            case EREDIVISIE : return context.getString(R.string.eredivisie_name);

            default:
                return context.getString(R.string.no_league_found);
        }
    }

    public static String getMatchDay(int match_day, int league_num) {
        if (league_num == CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                return "Group Stages, Matchday : 6";
            } else if (match_day == 7 || match_day == 8) {
                return "First Knockout round";
            } else if (match_day == 9 || match_day == 10) {
                return "QuarterFinal";
            } else if (match_day == 11 || match_day == 12) {
                return "SemiFinal";
            } else {
                return "Final";
            }
        } else {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals, int awaygoals) {
        if (home_goals < 0 || awaygoals < 0) {
            return " - ";
        } else {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName(String teamname) {
        if (teamname == null) {
            return R.drawable.no_icon;
        }
        switch (teamname) { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case ARSENAL: return R.drawable.arsenal;
            case ASTON: return R.drawable.aston_villa;
            case CHELSEA: return R.drawable.chelsea;
            case CRYSTAL: return R.drawable.crystal_palace_fc;
            case EVERTON: return R.drawable.everton_fc_logo1;
            case LEICESTER: return R.drawable.leicester_city_fc_hd_logo;
            case LIVERPOOL: return  R.drawable.liverpool;
            case MAN_C: return  R.drawable.manchester_city;
            case MAN_U: return R.drawable.manchester_united;
            case NEWCASTLE: return R.drawable.newcastle_united;
            case SOUTHAMPTON: return R.drawable.southampton_fc;
            case STOKE: return R.drawable.stoke_city;
            case SUNDERLAND: return R.drawable.sunderland;
            case SWANSEA: return R.drawable.swansea_city_afc;
            case TOTTENHAM: return R.drawable.tottenham_hotspur;
            case WEST_BROM: return R.drawable.west_bromwich_albion_hd_logo;
            case WEST_HAM: return R.drawable.west_ham;

            default:
                return R.drawable.ic_launcher; // looks less harsh than the no_icon.png
        }
    }
}
