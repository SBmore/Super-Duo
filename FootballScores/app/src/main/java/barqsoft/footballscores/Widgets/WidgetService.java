package barqsoft.footballscores.widgets;

/**
 * Created by Steven on 17/10/2015 using Tutorial by Dharmang Soni
 * http://dharmangsoni.blogspot.co.uk/2014/03/collection-widget-with-event-handling.html
 */

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.RemoteViewsService;

@SuppressLint("NewApi")
public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        WidgetDataProvider dataProvider = new WidgetDataProvider(
                getApplicationContext(), intent);
        return dataProvider;
    }
}