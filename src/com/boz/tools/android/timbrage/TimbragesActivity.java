package com.boz.tools.android.timbrage;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TimbragesActivity extends Activity implements OnClickListener {

    public static final String FILE_PARENT = "timbrage";
    public static final String FILE_PATTERN = "times{0,date,-yyyy-MM}.csv";

    private GroupArrayAdapter adapter;
    private TextView textReportMonthlyH;
    private TextView textReportMonthlyM;
    private TextView textReportMonthlyS;
    private TextView textTimeH;
    private TextView textTimeM;
    private TextView textTimeS;

    private ImageView addBtn;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbrages);

        addBtn = (ImageView) findViewById(R.id.imageViewAdd);
        addBtn.setOnClickListener(this);

        adapter = new GroupArrayAdapter(this, GroupArrayAdapter.group(TimeReport.loadTimes(getFile(), this)));
        final ListView timesList = (ListView) findViewById(R.id.timesList);
        timesList.setAdapter(adapter);
        timesList.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                // reporting
                timesChanged(adapter.getAllTimes());
            }
        });

        // add report field
        textReportMonthlyH = (TextView) findViewById(R.id.textViewReportMonthlyH);
        textReportMonthlyM = (TextView) findViewById(R.id.textViewReportMonthlyM);
        textReportMonthlyS = (TextView) findViewById(R.id.textViewReportMonthlyS);

        // check directory
        if (!new File(Environment.getExternalStorageDirectory(), FILE_PARENT).exists()) {
            new File(Environment.getExternalStorageDirectory(), FILE_PARENT).mkdir();
        }

        // load times
        timesChanged(adapter.getAllTimes());

        // current time
        textTimeH = (TextView) findViewById(R.id.textViewH);
        textTimeM = (TextView) findViewById(R.id.textViewM);
        textTimeS = (TextView) findViewById(R.id.textViewS);
        // start thread to update time and report
        new Timer().schedule(new UpdateTimeTask(), 0, 500);
    }

    public static String getFileName() {
        return MessageFormat.format(FILE_PATTERN, new Date());
    }

    public static File getFile() {
        return getFile(new Date());
    }

    public static File getFile(final Date date) {
        // Uri.parse("android.resource://YOUR_PACKAGENAME/" + resources);
        final String fileName = MessageFormat.format(FILE_PATTERN, date);

        return new File(new File(Environment.getExternalStorageDirectory(), FILE_PARENT), fileName);
    }

    public void onClick(final View v) {
        adapter.add(LocalDateTime.now());
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemShare:
                shareIt(this, this.adapter.getAllTimes(), LocalDate.now());
                break;
            case R.id.itemCalendar:
                final Intent toCalendar = new Intent(this, CalendarActivity.class);
                startActivity(toCalendar);
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public static void shareIt(final Context context, final Iterable<LocalDateTime> times, final LocalDate month) {
        // sharing implementation
        final Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_SUBJECT,
                MessageFormat.format(context.getString(R.string.send_subject), month.toDate()));

        final String monthly = TimeReport.report(times, month.withDayOfMonth(1), month.withDayOfMonth(1).plusMonths(1));
        share.putExtra(Intent.EXTRA_TEXT, MessageFormat.format(context.getString(R.string.send_object), monthly));
        final Uri uri = Uri.fromFile(getFile(month.toDate()));
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("text/plain"); // TODO define file content
        context.startActivity(Intent.createChooser(share, "Share times with..."));

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_timbrages, menu);
        return true;
    }

    public void timesChanged(final List<LocalDateTime> times) {
        // synchronize file
        // TODO make async ? non UI thread ? take care of multi thread adapter list access
        TimeReport.sync(adapter.getAllTimes(), getFile(), this);

        // update btn add icon
        if (adapter.getAllTimes().size() % 2 != 0) {
            // working
            addBtn.setImageResource(R.drawable.plus2_red);
        } else {
            // not working
            addBtn.setImageResource(R.drawable.plus2_green);
        }
    }

    class UpdateTimeTask extends TimerTask {

        @Override
        public void run() {
            TimbragesActivity.this.runOnUiThread(new Runnable() {

                public void run() {
                    // calculate daily elapsed time
                    final Period dailyElapsed = TimeReport.calculateElapsed(adapter.getAllTimes(), LocalDate.now());
                    textTimeH.setText(StringUtils.leftPad(String.valueOf(dailyElapsed.getHours()), 2, "0"));
                    textTimeM.setText(StringUtils.leftPad(String.valueOf(dailyElapsed.getMinutes()), 2, "0"));
                    textTimeS.setText(StringUtils.leftPad(String.valueOf(dailyElapsed.getSeconds()), 2, "0"));

                    // update report
                    final Period monthlyElapsed = TimeReport.calculateElapsed(adapter.getAllTimes(), LocalDate.now()
                            .withDayOfMonth(1), LocalDate.now().withDayOfMonth(1).plusMonths(1));
                    textReportMonthlyH.setText(StringUtils.leftPad(String.valueOf(monthlyElapsed.getHours()), 2, "0"));
                    textReportMonthlyM.setText(StringUtils.leftPad(String.valueOf(monthlyElapsed.getMinutes()), 2, "0"));
                    textReportMonthlyS.setText(StringUtils.leftPad(String.valueOf(monthlyElapsed.getSeconds()), 2, "0"));
                }
            });
        }
    };
}
