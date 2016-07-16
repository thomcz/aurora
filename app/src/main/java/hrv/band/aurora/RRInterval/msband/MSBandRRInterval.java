package hrv.band.aurora.RRInterval.msband;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import hrv.band.aurora.RRInterval.IRRInterval;
import hrv.band.aurora.view.fragment.MeasuringFragment;

/**
 * Created by Thomas on 13.06.2016.
 */
public class MSBandRRInterval implements IRRInterval {
    private BandClient client;
    private Activity activity;
    private TextView rrStatus;
    private TextView statusTxt;
    private List<Double> rr;//stores the actual measurement-rrIntervals
    private WeakReference<Activity> reference;
    private ObjectAnimator animation;
    /**
     *Handels when a new RRInterval is incoming
     */
    private BandRRIntervalEventListener mRRIntervalEventListener;

    public MSBandRRInterval(Activity activity, TextView statusTxt, final TextView rrStatus) {
        this.activity = activity;
        this.statusTxt = statusTxt;
        this.rrStatus = rrStatus;
        reference = new WeakReference<>(activity);
        rr = new ArrayList<>();

        mRRIntervalEventListener = new BandRRIntervalEventListener() {
            @Override
            public void onBandRRIntervalChanged(final BandRRIntervalEvent event) {
                if (event != null) {
                    double help = event.getInterval();
                    updateTextView(rrStatus, String.format("%.2f", help));
                    rr.add(help);//add the actual rrInterval
                }
            }
        };
    }
    @Override
    public void startRRIntervalMeasuring(ObjectAnimator animation) {
        this.animation = animation;
        new MSBandRRIntervalSubscriptionTask(this).execute();
    }

    @Override
    public void startAnimation() {
        updateStatusText("Hold Still While Measuring");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (animation != null) {
                    animation.start();
                }
            }
        });

    }

    @Override
    public void stopMeasuring() {
        try {
            client.getSensorManager().unregisterRRIntervalEventListener(mRRIntervalEventListener);
            updateStatusText("Finished");
        } catch (BandIOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseMeasuring() {
        if (client != null) {
            stopMeasuring();
        }
    }

    @Override
    public void destroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
    }

    @Override
    public void getDevicePermission(){
        new MSBandHeartRateConsentTask(reference, this).execute();
    }

    @Override
    public boolean isDeviceConnected() {
        boolean connected = false;
        try {
            connected = getConnectedBandClient();
        } catch (InterruptedException inter) {

        }catch (BandException band) {

        }
        return connected;
    }

    /**
     * get the Microsoft band client, that handles all the connections and does the actual measurement
     * @return wether the band is connected
     * @throws InterruptedException	connection has dropped e.g.
     * @throws BandException ohter stuff that should not happen
     */
    public boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                updateTextView(statusTxt, "Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(activity.getApplicationContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        updateTextView(statusTxt, "Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }

    public BandClient getClient() {
        return client;
    }

    public BandRRIntervalEventListener getRRIntervalEventListener() {
        return mRRIntervalEventListener;
    }

    public Double[] getRRIntervals() {
        return (Double[]) rr.toArray(new Double[rr.size()]);
    }

    /**
     * write data to UI-thread
     * @param string the text to write
     */
    private void updateTextView(final TextView txt, final String string) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt.setText(string);
            }
        });
    }

    public void updateStatusText(final String msg) {
        updateTextView(statusTxt, msg);
    }

    public void showSnackbar(final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackBar = Snackbar.make(MeasuringFragment.v, msg, Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackBar.dismiss();
                    }
                });
                snackBar.show();
            }
        });
    }

    public void showConsentSnackbar(final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackBar = Snackbar.make(MeasuringFragment.v, msg, Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("Consent", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getDevicePermission();
                        snackBar.dismiss();
                    }
                });
                snackBar.show();
            }
        });
    }
}
