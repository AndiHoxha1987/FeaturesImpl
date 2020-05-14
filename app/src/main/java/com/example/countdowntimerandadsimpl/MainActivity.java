package com.example.countdowntimerandadsimpl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private CountDownTimer countDownIncrement;
    private CountDownTimer countDownDecrement;
    private int lives = 2;
    private TextView value;
    private TextView increment;
    private TextView decrement;
    private RewardedAd rewardedAd;
    private boolean userEarnedReward = false;
    private long incrementMillis = 4000;
    private long decrementMillis = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAds();
        value = findViewById(R.id.value);
        increment = findViewById(R.id.count_down_increment);
        decrement = findViewById(R.id.count_down_decrement);
        value.setText(String.valueOf(lives));

        startIncrement(incrementMillis);
        startDecrement(decrementMillis);
    }

    private void startIncrement(long l) {
        if (countDownIncrement != null) {
            countDownIncrement.cancel();
            countDownIncrement = null;
        }
        countDownIncrement = new CountDownTimer(l, 100) {

            public void onTick(long millisUntilFinished) {
                String milliseconds = Long.toString(millisUntilFinished / 100);
                increment.setText(getString(R.string.count_down_timer_string, milliseconds));
            }

            public void onFinish() {
                lives++;
                value.setText(String.valueOf(lives));
                countDownIncrement = null;
                if (lives > 0) {
                    startIncrement(incrementMillis);
                }
            }

        }.start();
    }

    private void startDecrement(long l) {
        if (countDownDecrement != null) {
            countDownDecrement.cancel();
            countDownDecrement = null;
        }
        countDownDecrement = new CountDownTimer(l, 100) {

            public void onTick(long millisUntilFinished) {
                String milliseconds = Long.toString(millisUntilFinished / 100);
                decrement.setText(getString(R.string.count_down_timer_string, milliseconds));
            }

            public void onFinish() {
                if (lives > 0) {
                    lives--;
                    value.setText(String.valueOf(lives));
                    startDecrement(decrementMillis);
                } else {
                    countDownDecrement = null;
                    countDownIncrement.cancel();
                    countDownIncrement = null;
                    showDialogAds();
                }
            }
        }.start();
    }

    private void initializeAds() {
        //initialize ads banner
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //initialize ads video for first time
        rewardedAd = new RewardedAd(this, "ca-app-pub-3940256099942544/5224354917");

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
    }

    //this method is used for second call to watch an ads video
    private RewardedAd createAndLoadRewardedAd() {
        RewardedAd rewardedAd = new RewardedAd(this,
                "ca-app-pub-3940256099942544/5224354917");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }

    private void showDialogAds() {
        CharSequence[] options = new CharSequence[]{
                "Cancel",
                "Watch an Ads Video "
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Message?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 1) {
                    if (rewardedAd.isLoaded()) {
                        RewardedAdCallback adCallback = new RewardedAdCallback() {
                            @Override
                            public void onRewardedAdOpened() {
                                // Ad opened.
                            }

                            @Override
                            public void onRewardedAdClosed() {
                                // Ad closed.
                                //canceling existing dialog, update boolean to false for existing dialog
                                //update lives to value 10, call setDirection to restart the game
                                //update rewardedAd for another video ads,
                                // update boolean parameter for earnedReward to false, this need to be ready for next reward video ads
                                if (userEarnedReward) {
                                    lives = 2;
                                    value.setText(String.valueOf(lives));
                                    rewardedAd = createAndLoadRewardedAd();
                                    userEarnedReward = false;
                                    startIncrement(incrementMillis);
                                    startDecrement(decrementMillis);
                                }
                            }

                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem reward) {
                                // User earned reward.
                                userEarnedReward = true;
                            }

                            @Override
                            public void onRewardedAdFailedToShow(int errorCode) {
                                // Ad failed to display.
                            }
                        };
                        rewardedAd.show(MainActivity.this, adCallback);
                    }
                }
            }
        });
        builder.show();
    }

}
