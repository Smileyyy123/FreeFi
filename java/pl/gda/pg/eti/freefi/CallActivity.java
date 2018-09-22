package pl.gda.pg.eti.freefi;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.net.sip.*;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;

public class CallActivity extends AppCompatActivity {

    private static final String USERNAME = "1000";
    private static final String PASSWORD = "test";
    private static final String DOMAIN = "192.168.137.63";
    private static final String REMOTE = "2000";

    public SipAudioCall call;
    public SipManager manager;
    public SipProfile localProfile;

    private Button button;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call);

        button = findViewById(R.id.button);
        statusText = findViewById(R.id.callState);

        initializeManager();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall();
            }
        });
    }

    public void initializeManager() {
        if (manager == null) {
            manager = SipManager.newInstance(this);
        }
        initializeLocalProfile();
    }

    public void initializeLocalProfile() {
        if (manager == null) {
            return;
        }
        if (localProfile != null) {
            closeLocalProfile();
        }

        try {
            SipProfile.Builder profileBuilder = new SipProfile.Builder(USERNAME, DOMAIN);
            profileBuilder.setPassword(PASSWORD);
            localProfile = profileBuilder.build();

            Intent intent = new Intent();
            intent.setAction("android.SipDemo.");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
            manager.open(localProfile, pendingIntent, null);

            manager.setRegistrationListener(localProfile.getUriString(), new SipRegistrationListener() {
                @Override
                public void onRegistering(String s) {
                    updateStatus("Registering with SIP server...");
                }

                @Override
                public void onRegistrationDone(String s, long l) {
                    updateStatus("Registered. Ready to make a call.");
                }

                @Override
                public void onRegistrationFailed(String s, int i, String s1) {
                    updateStatus("Registration failed.");
                }
            });
        } catch (ParseException pe) {
            updateStatus("Connection error");
        } catch (SipException se) {
            updateStatus("Connection error");
        }
    }

    public void closeLocalProfile() {
        if (manager == null){
            return;
        }
        try {
            if (localProfile != null) {
                manager.close(localProfile.getUriString());
            }
        } catch (Exception e) {
            Log.d("Profile Close Error", "Failed to close local profile.", e);
        }
    }

    public void updateStatus (final String status) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(status);
            }
        });
    }

    public void makeCall() {
        updateStatus("Calling to " + REMOTE);

        try {
            SipAudioCall.Listener callListener = new SipAudioCall.Listener() {
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(true);
                }

                @Override
                public void onCallEnded (SipAudioCall call) {
                    updateStatus("Ready to make a call.");
                }
            };

            call = manager.makeAudioCall(localProfile.getUriString(), "sip:" + REMOTE + "@" + DOMAIN, callListener, 30);

        } catch (Exception e ) {
            Log.d("Call initiation error.", "Error when trying to make a call.");
            if (localProfile != null) {
                try {
                    manager.close(localProfile.getUriString());
                } catch (Exception ee) {
                    Log.d("Manager closing error.", "Error when trying to close manager", ee);
                    ee.printStackTrace();
                }
            }
            if(call != null) {
                call.close();
            }
        }
    }
}