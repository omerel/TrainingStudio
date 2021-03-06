package zina_eliran.app.API;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;

import java.util.ArrayList;
import java.util.Calendar;

import zina_eliran.app.API.EmailSender.EmailSendThread;
import zina_eliran.app.API.Listeners.JoinLeaveThread;
import zina_eliran.app.API.Listeners.OnSetValueCompleteListener;
import zina_eliran.app.API.Listeners.GetBEObjectEventListener;
import zina_eliran.app.API.Listeners.OnTrainingChangeListener;
import zina_eliran.app.API.Listeners.OnUserChangeListener;
import zina_eliran.app.BusinessEntities.BEBaseEntity;
import zina_eliran.app.BusinessEntities.BEResponse;
import zina_eliran.app.BusinessEntities.BEResponseStatusEnum;
import zina_eliran.app.BusinessEntities.BETraining;
import zina_eliran.app.BusinessEntities.BETrainingLevelEnum;
import zina_eliran.app.BusinessEntities.BETrainingLocation;
import zina_eliran.app.BusinessEntities.BETrainingStatusEnum;
import zina_eliran.app.BusinessEntities.BETrainingViewDetails;
import zina_eliran.app.BusinessEntities.BETypesEnum;
import zina_eliran.app.BusinessEntities.BEUser;
import zina_eliran.app.BusinessEntities.CMNLogHelper;
import zina_eliran.app.BusinessEntities.DALActionTypeEnum;
import zina_eliran.app.Utils.FireBaseHandler;

public class DAL {


    private static Firebase rootRef = new Firebase("https://trainingstudiofb.firebaseio.com");
    private static Firebase usersRef = rootRef.child("Users");
    private static Firebase trainingsRef = rootRef.child("Trainings");
    private static Firebase trainingViewDetailsRef = rootRef.child("TrainingViewDetails");


    public static void registerUser(BEUser user, FireBaseHandler fbHandler) {
        if (user != null) {
            user.setVerificationCode(ServerAPI.generateVerificationCode());
            user.setRegisteredDate(Calendar.getInstance().getTime());
            createObject(BETypesEnum.Users, DALActionTypeEnum.registerUser, user, fbHandler);

            //Send verification code
            EmailSendThread thread = new EmailSendThread(user.getEmail(), user.getName(), user.getVerificationCode());
            new Thread(thread).start();

        } else
            cannotPerformAction(fbHandler, DALActionTypeEnum.registerUser, "Cannot create null user");
    }

    public static void resendUserRegistrationEmail(String email, String name, String verificationCode) {
            //Send verification code
            EmailSendThread thread = new EmailSendThread(email, name, verificationCode);
            new Thread(thread).start();
    }


    public static void createTraining(BETraining training, FireBaseHandler fireBaseHandler) {
        if (training != null) {
            createObject(BETypesEnum.Trainings, DALActionTypeEnum.createTraining, training, fireBaseHandler);
        } else {
            cannotPerformAction(fireBaseHandler, DALActionTypeEnum.createTraining, "Cannot create null training");
        }

    }


    public static void updateUser(BEUser user, FireBaseHandler fireBaseHandler) {
        if (user != null) {
            updateObject(BETypesEnum.Users, DALActionTypeEnum.updateUser, user, fireBaseHandler);
        } else {
            cannotPerformAction(fireBaseHandler, DALActionTypeEnum.updateUser, "Cannot update user null");
        }
    }


    public static void updateTraining(BETraining training, FireBaseHandler fireBaseHandler) {
        if (training != null) {
            updateObject(BETypesEnum.Trainings, DALActionTypeEnum.updateTraining, training, fireBaseHandler);
        } else {
            cannotPerformAction(fireBaseHandler, DALActionTypeEnum.updateTraining, "Cannot update training null");
        }
    }


    public static void getUserByUID(String userId, FireBaseHandler fbHandler) {
        getObject(BETypesEnum.Users, DALActionTypeEnum.getUser, userId, fbHandler);
    }


    public static void getTraining(String trainingId, FireBaseHandler fireBaseHandler) {
        getObject(BETypesEnum.Trainings, DALActionTypeEnum.getTraining, trainingId, fireBaseHandler);
    }


    public static void getTrainingView(String trainingId,String userId,  FireBaseHandler fireBaseHandler) {
        getObject(BETypesEnum.TrainingViewDetails, DALActionTypeEnum.getTrainingViewDetails, trainingId + userId, fireBaseHandler);
    }

    public static void createTrainingView(BETrainingViewDetails trainingView, FireBaseHandler fbHandler) {
        if (trainingView != null) {
            createObject(BETypesEnum.TrainingViewDetails, DALActionTypeEnum.createTrainingViewDetails, trainingView, fbHandler);
        } else {
            cannotPerformAction(fbHandler, DALActionTypeEnum.createTrainingViewDetails, "Cannot create null trainingView");
        }
    }

    public static void updateTrainingView(BETrainingViewDetails trainingView, FireBaseHandler fbHandler) {
        updateObject(BETypesEnum.TrainingViewDetails, DALActionTypeEnum.updateTrainingViewDetails, trainingView, fbHandler);
    }


    //*
    public static void getUsersByTraining(String trainingId, FireBaseHandler fireBaseHandler) {
        if (!trainingId.isEmpty()) {

            try {

            } catch (Exception e) {
                CMNLogHelper.logError("DAL", e.getMessage());
            }

        }
    }


    public static void getAllTrainings(FireBaseHandler fireBaseHandler) {
        getList(BETypesEnum.Trainings, DALActionTypeEnum.getAllTrainings, fireBaseHandler);
    }

    public static void getAllTrainingViewDetails(FireBaseHandler fireBaseHandler){
        getList(BETypesEnum.TrainingViewDetails, DALActionTypeEnum.getAllTrainingsViews, fireBaseHandler);
    }

    public static void joinTraining(String trainingId, String userId, FireBaseHandler fireBaseHandler) {
        try {
            if (trainingId != null && userId != null) {
                //Create DB reference
                Firebase userRef = usersRef.child(userId);
                Firebase trainingRef = trainingsRef.child(trainingId);

                //Set up listeners
                JoinLeaveThread joinLeaveThread = new JoinLeaveThread(fireBaseHandler, DALActionTypeEnum.joinTraining);

                //Get objects from DB
                GetBEObjectEventListener listenerTraining = new GetBEObjectEventListener(BETypesEnum.Trainings, joinLeaveThread, DALActionTypeEnum.getTraining );
                GetBEObjectEventListener listenerUser = new GetBEObjectEventListener(BETypesEnum.Users, joinLeaveThread, DALActionTypeEnum.getUser );

                //Add listeners to DB references
                userRef.addListenerForSingleValueEvent(listenerUser);
                trainingRef.addListenerForSingleValueEvent(listenerTraining);

            } else
                cannotPerformAction(fireBaseHandler, DALActionTypeEnum.joinTraining, "One of the parameters invalid");
        } catch (Exception e) {
            CMNLogHelper.logError("joinTraining", e.getMessage());
        }
    }

    public static void leaveTraining(String trainingId, String userId, FireBaseHandler fireBaseHandler) {
        try {
            if (trainingId != null && userId != null) {
                //Create DB reference
                Firebase userRef = usersRef.child(userId);
                Firebase trainingRef = trainingsRef.child(trainingId);

                //Set up listeners
                JoinLeaveThread joinLeaveThread = new JoinLeaveThread(fireBaseHandler, DALActionTypeEnum.leaveTraining);

                //Get objects from DB
                GetBEObjectEventListener listenerTraining = new GetBEObjectEventListener(BETypesEnum.Trainings, joinLeaveThread, DALActionTypeEnum.getTraining );
                GetBEObjectEventListener listenerUser = new GetBEObjectEventListener(BETypesEnum.Users, joinLeaveThread, DALActionTypeEnum.getUser );

                //Add listeners to DB references
                userRef.addListenerForSingleValueEvent(listenerUser);
                trainingRef.addListenerForSingleValueEvent(listenerTraining);
            } else
                cannotPerformAction(fireBaseHandler, DALActionTypeEnum.joinTraining, "One of the parameters invalid");
        } catch (Exception e) {
            CMNLogHelper.logError("leaveTraining", e.getMessage());
        }
    }


    //////////////////////////////////////////////////////////////////
    //////////// Generic DAL actions /////////////////////////////////


    //Send Negative response
    private static void cannotPerformAction(FireBaseHandler fireBaseHandler, DALActionTypeEnum action, String message) {
        BEResponse res = new BEResponse();
        res.setStatus(BEResponseStatusEnum.error);
        res.setActionType(action);
        res.setMessage(message);
        if (fireBaseHandler != null)
            fireBaseHandler.onActionCallback(res);
    }


    private static void createObject(BETypesEnum objectType, DALActionTypeEnum action, BEBaseEntity object, FireBaseHandler fbHandler) {
        try {

            //Set DB ref
            Firebase ref = rootRef.child(objectType.toString());

            if (action == DALActionTypeEnum.createTrainingViewDetails){
                //Generate ID that consost of training & user IDs
                String id = ((BETrainingViewDetails)object).getTrainingId() + ((BETrainingViewDetails)object).getUserId();
                object.setId(id.toString());

                //Create FB reference to save the object
                Firebase objectRef = ref.child(id);

                //Save including ID
                ArrayList<BEBaseEntity> entities = new ArrayList<>();
                entities.add(object);
                OnSetValueCompleteListener listener = new OnSetValueCompleteListener(fbHandler, entities, action, objectType);
                objectRef.setValue(object, listener);
            }

            else {
                //Check if already exists
                if (object.getId() != null) {
                    CMNLogHelper.logError("CREATE-OBJECT", "Object Already exists");
                    cannotPerformAction(fbHandler, action, "Object already exists");
                }

                //Object does not exist in DB, so it will be created
                else {
                    Firebase newObject = ref.push();

                    // Get the unique ID generated by push()
                    String generatedUniqueID = newObject.getKey();
                    object.setId(generatedUniqueID);

                    //Save including ID
                    ArrayList<BEBaseEntity> entities = new ArrayList<>();
                    entities.add(object);
                    OnSetValueCompleteListener listener = new OnSetValueCompleteListener(fbHandler, entities, action, objectType);
                    newObject.setValue(object, listener);
                }

                CMNLogHelper.logError("Object created", object.toString());
            }
        } catch (Exception e) {
            cannotPerformAction(fbHandler, action, "Could not create new object");
            CMNLogHelper.logError("Create-Object-DB", e.getMessage());
        }
    }


    private static void updateObject(BETypesEnum objectType, DALActionTypeEnum action, BEBaseEntity object, FireBaseHandler fbHandler) {
        try {
            if (objectType != null && object.getId() != null) {
                //Set DB ref
                Firebase ref = rootRef.child(objectType.toString());
                Firebase objectRef = ref.child(object.getId());
                ArrayList<BEBaseEntity> entities = new ArrayList<>();
                entities.add(object);
                OnSetValueCompleteListener listener = new OnSetValueCompleteListener(fbHandler, entities, action, objectType);
                objectRef.setValue(object, listener);
                CMNLogHelper.logError("DAL", "Object updated" + object.toString());
            } else {
                cannotPerformAction(fbHandler, action, "Could not update object");
            }

        } catch (Exception e) {
            cannotPerformAction(fbHandler, action, "Could not update object");
            CMNLogHelper.logError("Create-Object-DB", e.getMessage());
        }
    }


    private static void getObject(BETypesEnum objectType, DALActionTypeEnum action, String uid, FireBaseHandler fireBaseHandler) {

        if (!uid.isEmpty() && objectType != null) {
            try {
                Firebase specificObjectRef = rootRef.child(objectType.toString()).child(uid);
                GetBEObjectEventListener listener = new GetBEObjectEventListener(objectType, fireBaseHandler, action);
                specificObjectRef.addListenerForSingleValueEvent(listener);
            } catch (Exception e) {
                CMNLogHelper.logError("GETOBJECT", e.getMessage());
                cannotPerformAction(fireBaseHandler, action, "Cannot get object");
            }

        } else {
            cannotPerformAction(fireBaseHandler, action, "Cannot get object, invalid parameters");
        }
    }


    private static void getList(BETypesEnum objectType, DALActionTypeEnum action, FireBaseHandler fireBaseHandler) {
        try {
            if (objectType != null && action != null) {
                //Set DB ref
                Firebase ref = rootRef.child(objectType.toString());

                //Set listener
                GetBEObjectEventListener listener = new GetBEObjectEventListener(objectType, fireBaseHandler, action);
                ref.addListenerForSingleValueEvent(listener);
                CMNLogHelper.logError("GET-LIST-FUNC", "Listener called");
            } else
                cannotPerformAction(fireBaseHandler, action, "Could get training list, invalid parameters");

        } catch (Exception e) {
            cannotPerformAction(fireBaseHandler, action, "Could get training list");
            CMNLogHelper.logError("GET-LIST-FUNC", e.getMessage());
        }
    }



    ////////////////////////////////////////////////////////////////
    //Zina's tests

    public static void tests(FireBaseHandler fireBaseHandler) {

//        Intent intent = new Intent(this, DBMonitoringService.class);
//        startService(intent);


        //Create user test
//        BEUser user = new BEUser();
//        user.setEmail("test@whatever.com");
//        user.setName("test");
//        user.setActive(true);
//        CMNLogHelper.logError("Create-user", "test");
//        registerUser(user, fireBaseHandler);

        //Find user by id test

//        BEUser getUser = getUserByUID(user.getId(), fireBaseHandler);
//        getUserByUID(user.getId(), fireBaseHandler);
////        CMNLogHelper.logError("GET USER", getUser.toString());
//        CMNLogHelper.logError("GET USER", "tsts");
//        getUserByUID(user.getId(), fireBaseHandler);


        //Find user by training id


        //Update user
//        user.setHeigth(""+180);
//        user.setName("NEWuser name");
//        updateUser(user, fireBaseHandler);


        //Create training test
//        BETraining t = new BETraining();
//        t.setName("NewTestTraining");
//        t.setCreatorId("-KVZtKZSHHRhhEyb8XOv");
//        Calendar cal = Calendar.getInstance();
//        cal.set(2016,11,15,16,00);
//        t.setTrainingDateTimeCalender(cal);
//        t.setCurrentNumberOfParticipants(1);
//        t.setDescription("NewTraining");
//        t.setLevel(BETrainingLevelEnum.Beginner);
//        t.setStatus(BETrainingStatusEnum.open);
//        t.setDuration(45);
//        BETrainingLocation location = new BETrainingLocation();
//        location.setAltitude(23.0);
//        location.setLatitude(39.6);
//        location.setLocationAddress("blablabla");
//        location.setLocationName("aaaaa");
//        t.setLocation(location);
//        t.setTrainingFullNotificationFlag(true);
//        t.setJoinTrainingNotificationFlag(true);
//        t.setMaxNumberOfParticipants(5);
//        CMNLogHelper.logError("Create-training", "test");
//        createTraining(t, fireBaseHandler);



        //Create trainingView
//        BETrainingViewDetails trainingDetails = new BETrainingViewDetails();
//        trainingDetails.setTrainingId("-KVvUwpEudTje_09bJbB");
//        trainingDetails.setUserId("-KVoEMNsk98sLfKl2WKp");
//        trainingDetails.setAvgSpeed(4);
//        trainingDetails.setMaxSpeed(6);
//        trainingDetails.setTotalCalories(200);
//        trainingDetails.setTotalDistance(2);
//        CMNLogHelper.logError("Create-trainingViewDetails", "test");
//        createTrainingView(trainingDetails, fireBaseHandler);

        //Get trainingView
//        CMNLogHelper.logError("GET-trainingViewDetails", "test");
//        getTrainingView("-KVvUwpEudTje_09bJbB", "-KVoEMNsk98sLfKl2WKp", fireBaseHandler);

        //Find training by id test
//        getTraining(t.getId(), fireBaseHandler);
//        CMNLogHelper.logError("GET TRAINING", "test");

        //Update training
//        t.setName("NEWUpdatedName");
//        updateTraining(t, fireBaseHandler);


        //join training
//        CMNLogHelper.logError("JOIN TRAINING", "test1");
//        joinTraining(t.getId(), user.getId(), fireBaseHandler);
//
//        CMNLogHelper.logError("JOIN TRAINING", "test2");
//        joinTraining(t.getId(), "-KV_-5FxpqKiqo5hWz9h", fireBaseHandler);



        //join training
//        CMNLogHelper.logError("Leave TRAINING", "test1");
//        leaveTraining(t.getId(), "-KV_-5FxpqKiqo5hWz9h", fireBaseHandler);

        //get all public trainings
//        CMNLogHelper.logError("GET ALL TRAININGS", "test");
//        getAllTrainings(fireBaseHandler);
//        ArrayList<String> arr = new ArrayList<>();
//        arr.add("-KSNfQcBV2d9ESKsQqic");
//        arr.add("-KSNfUF-UvM66pv4ZzRt");
//        arr.add("-KSNfUInzvpo27SmmZDS");
//        getPublicTrainings(arr, fireBaseHandler);
//        joinTraining("-KVZHP8m8MGWBskGpE1l", "-KVUEkQ_xFxVw3VHyFam", null);


    }

    public static Firebase getUsersRef() {
        return usersRef;
    }
    public static Firebase getTrainingsRef() {
        return trainingsRef;
    }
    public static void addListenerToTraining(String trainingID, FireBaseHandler fbHandler){
        Firebase tRef = trainingsRef.child(trainingID);
        tRef.addChildEventListener(new OnTrainingChangeListener(fbHandler, trainingID));
    }
    public static void addListenerToUser(BEUser user, FireBaseHandler fireBaseHandler){
        Firebase uRef = usersRef.child(user.getId());
        uRef.addChildEventListener(new OnUserChangeListener(user, fireBaseHandler));
    }

}
