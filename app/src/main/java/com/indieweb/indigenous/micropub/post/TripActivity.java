package com.indieweb.indigenous.micropub.post;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.BaseCreate;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class TripActivity extends BaseCreate {

    private TextView pointsInfo;
    private List<String> points = new ArrayList<>();
    private int PICK_GPX_REQUEST = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Trip";
        addCounter = true;
        canAddMedia = false;
        canAddLocation = false;
        setContentView(R.layout.activity_trip);
        super.onCreate(savedInstanceState);
        saveAsDraft.setVisibility(View.GONE);
        saveAsDraft = null;
        pointsInfo = findViewById(R.id.pointsInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.loadGpx);
        item.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.loadGpx) {
            Intent ii = new Intent();
            ii.setType("application/gpx+xml");
            ii.setAction(Intent.ACTION_OPEN_DOCUMENT);
            if (!isMediaRequest) {
                ii.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            startActivityForResult(Intent.createChooser(ii, getString(R.string.trip_load_gpx)), PICK_GPX_REQUEST);
            return true;
        }

        return onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_GPX_REQUEST && resultCode == RESULT_OK) {

            if (data.getData() != null) {

                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                    GPXParser mParser = new GPXParser();

                    Gpx parsedGpx = null;
                    try {
                        InputStream in = getContentResolver().openInputStream(data.getData());
                        parsedGpx = mParser.parse(in);
                    }
                    catch (IOException | XmlPullParserException e) {
                        Snackbar.make(layout, String.format(getString(R.string.trip_reading_error), e.getMessage()), Snackbar.LENGTH_LONG).show();
                    }

                    if (parsedGpx != null) {
                        points.clear();
                        List<Track> tracks = parsedGpx.getTracks();

                        for (int i = 0; i < tracks.size(); i++) {
                            Track track = tracks.get(i);
                            List<TrackSegment> segments = track.getTrackSegments();
                            for (int j = 0; j < segments.size(); j++) {
                                TrackSegment segment = segments.get(j);
                                for (TrackPoint trackPoint : segment.getTrackPoints()) {
                                    String coordinates = String.format("%s,%s,%s", trackPoint.getLatitude(), trackPoint.getLongitude(), trackPoint.getElevation());
                                    String GeoURI = "geo:" + coordinates;
                                    points.add(GeoURI);
                                }
                            }
                        }

                        if (points.size() > 0) {
                            String message = String.format(getString(R.string.trip_points_count), points.size());
                            pointsInfo.setText(message);
                            Snackbar.make(layout, message, Snackbar.LENGTH_LONG).show();
                        }
                        else {
                            Snackbar.make(layout, getString(R.string.trip_no_points_found), Snackbar.LENGTH_LONG).show();
                        }
                    }

                }
                catch (Exception e) {
                    Snackbar.make(layout, String.format(getString(R.string.trip_reading_error), e.getMessage()), Snackbar.LENGTH_LONG).show();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        boolean hasErrors = false;

        if (TextUtils.isEmpty(title.getText())) {
            hasErrors = true;
            title.setError(getString(R.string.required_field));
        }

        if (points.size() == 0) {
            hasErrors = true;
            Snackbar.make(layout, getString(R.string.trip_no_points), Snackbar.LENGTH_LONG).show();
        }

        if (!hasErrors) {

            int i = 0;
            for (String p : points) {
                bodyParams.put("route_multiple_["+ i +"]", p);
                i++;
            }

            sendBasePost(item);
        }
    }

}
