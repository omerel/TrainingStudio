package zina_eliran.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import zina_eliran.app.BusinessEntities.BEResponse;
import zina_eliran.app.BusinessEntities.BEResponseStatusEnum;
import zina_eliran.app.BusinessEntities.BETraining;
import zina_eliran.app.BusinessEntities.BETypesEnum;
import zina_eliran.app.BusinessEntities.BEUser;
import zina_eliran.app.BusinessEntities.CMNLogHelper;
import zina_eliran.app.BusinessEntities.DALActionTypeEnum;
import zina_eliran.app.Utils.FireBaseHandler;

public class LobbyActivity extends BaseActivity implements View.OnClickListener, FireBaseHandler {

    Button createTrainingBtn;
    Button publicTrainingsBtn;
    Button myProfileSettingsBtn;
    Button myTrainingsBtn;
    Button startTrainingBtn;
    Button myProgressBtn;
    ProgressBar pBar;
    RelativeLayout pBarRl;
    LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        pBar = (ProgressBar) findViewById(R.id.lobby_pbar);
        pBar.setVisibility(View.VISIBLE);
        pBar.bringToFront();
        pBarRl = (RelativeLayout) findViewById(R.id.lobby_pbar_rl);
        mainLayout = (LinearLayout) findViewById(R.id.lobby_ll);
        handleLoginState();

    }

    private void onCreateUI() {
        try {

            //bind ui
            createTrainingBtn = (Button) findViewById(R.id.lobby_create_training_btn);
            publicTrainingsBtn = (Button) findViewById(R.id.lobby_public_trainings_btn);
            myProfileSettingsBtn = (Button) findViewById(R.id.lobby_my_profile_settings_btn);
            myTrainingsBtn = (Button) findViewById(R.id.lobby_my_trainings_btn);
            startTrainingBtn = (Button) findViewById(R.id.lobby_start_training_btn);
            myProgressBtn = (Button) findViewById(R.id.lobby_my_progress_btn);

            //add events
            createTrainingBtn.setOnClickListener(this);
            publicTrainingsBtn.setOnClickListener(this);
            myProfileSettingsBtn.setOnClickListener(this);
            myTrainingsBtn.setOnClickListener(this);
            startTrainingBtn.setOnClickListener(this);
            myProgressBtn.setOnClickListener(this);

        } catch (Exception e) {
            CMNLogHelper.logError("LobbyActivity", e.getMessage());
        }
    }


    @Override
    public void onClick(View v) {
        try {
            Map<String, String> intentParams = new HashMap<>();
            switch (v.getId()) {
                case R.id.lobby_create_training_btn:
                    //navigate to create new training activity
                    intentParams.put(_getString(R.string.training_details_activity_mode), _getString(R.string.training_details_create_mode));
                    navigateToActivity(this, TrainingDetailsActivity.class, false, intentParams);
                    break;

                case R.id.lobby_public_trainings_btn:
                    intentParams.put(_getString(R.string.training_list_public_mode), "true");
                    navigateToActivity(this, TrainingsListActivity.class, false, intentParams);
                    break;

                case R.id.lobby_my_profile_settings_btn:
                    navigateToActivity(this, ProfileSettingsActivity.class, false, null);
                    break;

                case R.id.lobby_my_trainings_btn:
                    intentParams.put(_getString(R.string.training_list_manage_training_permission), "true");
                    intentParams.put(_getString(R.string.training_list_my_trainings_mode), "true");
                    navigateToActivity(this, TrainingsListActivity.class, false, intentParams);
                    break;

                case R.id.lobby_start_training_btn:
                    //will enable to start recording data while running
                    intentParams.put(_getString(R.string.training_view_activity_mode), _getString(R.string.training_view_run_mode));
                    navigateToActivity(this, TrainingViewActivity.class, false, intentParams);
                    break;

                case R.id.lobby_my_progress_btn:
                    //disables in phase 1
                    //will enable to see data about past trainings, progress etc.
                    navigateToActivity(this, TrainingProgressActivity.class, false, null);
                    break;

            }
        } catch (Exception e) {
            CMNLogHelper.logError("LobbyActivity", e.getMessage());
        }
    }

    @Override
    public void onActionCallback(BEResponse response) {
        try {

            if (response != null) {
                if (response.getStatus() == BEResponseStatusEnum.error) {
                    CMNLogHelper.logError("LobbyActivity", "error in get user callback on app load | err:" + response.getMessage());
                    Toast.makeText(_getAppContext(), "Error while retrieving user data, please try again later.", Toast.LENGTH_LONG).show();
                } else if (response.getActionType() == DALActionTypeEnum.getUser && response.getEntityType() == BETypesEnum.Users) {
                    sApi.setAppUser((BEUser) response.getEntities().get(0));

                    //get next training here, using my training ids and update server api
                    sApi.getAllTrainings(this);

                } else if (response.getActionType() == DALActionTypeEnum.getAllTrainings && response.getEntityType() == BETypesEnum.Trainings) {

                    //get next training here and update server api
                    ArrayList<BETraining> myJoinedTrainingsList = sApi.filterMyTrainings(sApi.getAppUser().getId(), response.getEntities(), false);
                    BETraining nextTraining = sApi.getMyNextTraining(myJoinedTrainingsList);

                    onCreateUI();

                    if (myJoinedTrainingsList.size() > 0) {
                        startTrainingBtn.setEnabled(true);
                        sApi.setNextTraining(myJoinedTrainingsList.get(0));
                    }


                   /* if (nextTraining != null) {
                        sApi.setNextTraining(nextTraining);
                        if (isNext10MinSelectedDate(nextTraining.getTrainingDateTimeCalender())) {
                            startTrainingBtn.setEnabled(true);
                        }
                    }*/

                    pBar.setVisibility(View.GONE);
                    pBarRl.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);

                } else {
                    CMNLogHelper.logError("LobbyActivity", "wrong action type in callback" + response.getEntityType() + ", " + response.getActionType());
                }
            }

        } catch (Exception e) {
            CMNLogHelper.logError("LobbyActivity", e.getMessage());
        }
    }


    private void handleLoginState() {
        try {
            if (isVerified()) {

                sApi.getUser(readFromSharedPreferences(_getString(R.string.user_id)), this);
            }
            //navigate to Lobby if the user is verified
            else {
                navigateToActivity(this, RegisterActivity.class, true, null);
            }
        } catch (Exception e) {
            CMNLogHelper.logError("LobbyActivity", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            handleLoginState();
        } catch (Exception e) {
            CMNLogHelper.logError("LobbyActivity", e.getMessage());
        }
    }


}
