package it.jaschke.alexandria;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Steven on 06/10/2015.
 */
public class Utility {
    // Source:  http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
    // User:    Alexandre Jasmin
    // Changes: getActivity().getApplicationContext() to access getSystemService
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
