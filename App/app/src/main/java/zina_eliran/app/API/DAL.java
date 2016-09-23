package zina_eliran.app.API;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.UUID;

import zina_eliran.app.API.Listeners.EventListenerCallback;
import zina_eliran.app.API.Listeners.GetUserEventListener;
import zina_eliran.app.BaseActivity;
import zina_eliran.app.BusinessEntities.BEResponse;
import zina_eliran.app.BusinessEntities.BEResponseStatusEnum;
import zina_eliran.app.BusinessEntities.BETrainingListTypeEnum;
import zina_eliran.app.BusinessEntities.BEUser;
import zina_eliran.app.BusinessEntities.CMNLogHelper;

/**
 * Created by Zina K on 9/10/2016.
 */
public class DAL{

    //TODO Zina - please add try catch :)
    private static Firebase rootRef = new Firebase("https://trainingstudiofb.firebaseio.com");
    private static Firebase usersRef;// = rootRef.child("Users");
//    private static Firebase usersRef = rootRef.child("Users");
//    private static Firebase trainingsRef= rootRef.child("Trainings");
//    Firebase ref = new Firebase("https://docs-examples.firebaseio.com/android/saving-data/fireblog");


    public DAL(){
//        Firebase usersRef = rootRef.child("Users");
////        Firebase trainingsRef = rootRef.child("Trainings");
//
//        BEUser testUser = new BEUser();
//        testUser.setName("Eliran");
//        testUser.setEmail("eliran@com");
//        testUser.setVerificationCode("12345");
////        registerUser(testUser);
//        Firebase newUserRef = usersRef.push();
//        newUserRef.setValue(testUser);
//        // Get the unique ID generated by push()
//        String generatedUniqueID = newUserRef.getKey();
//        testUser.setId(generatedUniqueID);
//
//        //Save user including UUID
//        newUserRef.setValue(testUser);
//        int i = 1;

        usersRef = rootRef.child("Users");



//        Firebase childRef = rootRef.child("Users2");
//        childRef.setValue("Zina2");
//        Firebase training = rootRef.child("Training");
//        training.push("NewTrainig");
//        training.setValue("NewTraining");
        // Generate a reference to a new location and add some data using push()
        Firebase.goOnline();
        Firebase newUserRef = usersRef.push();
        // Add some data to the new location
        BEUser user = new BEUser();
        user.setEmail("dima@whatever.com");
        user.setName("aaaaaa");
        user.setVerificationCode("12665789");

        String generatedUniqueID = newUserRef.getKey();
        //user.setId(generatedUniqueID);
        newUserRef.setValue(user, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                Log.e("On complete", "HARA AVAD");
            }
        });
        //int i = 1;
        //getUserByUID(generatedUniqueID);


    }

    public static void registerUser(BEUser user) {
        usersRef = rootRef.child("Users");
        Firebase newRef = usersRef.child("blabla");
        newRef.setValue("Zina");

        user.setVerificationCode("9999");

        try{
            usersRef = rootRef.child("Users");
            //Set location to push
            Firebase newUserRef = usersRef.push();

            // Get the unique ID generated by push()
            String generatedUniqueID = newUserRef.getKey();
            user.setId(generatedUniqueID);

            //Save user including UUID
            newUserRef.setValue(user);
            Thread.sleep(3000);
            getUserByUID(generatedUniqueID);
        } catch (Exception e) {

            CMNLogHelper.logError("DAL", e.getMessage());
        }


    }

    private static void getUserByUID(String uid){
        //final BEUser[] resUser = {new BEUser()};
        Firebase singleUserRef = usersRef.child(uid);
        //BEResponse res = new BEResponse();
        GetUserEventListener listener = new GetUserEventListener();

        singleUserRef.addListenerForSingleValueEvent(listener);
//        singleUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                BEUser user = dataSnapshot.getValue(BEUser.class);
//                Log.e("DAL", "Listener Zina");
//                Log.e("DAL", user.getName());
//
//                resUser[0] = user;
//                int i = 1;
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });

//        singleUserRef.setValue(user);
//        Log.e("DAL response", ((BEUser)listener.getRes().getEntity()).getEmail());


//        int a = 1;
        //return null;
    }


    //////////////////////////////////////////////////////////////////
    //register to listeners here

    //this function will be called when server API initiate
    private static void registerToEvents(){

    }


    //this function will be called when need
    private static void registerToEventsOnce(){

    }


    /////////////////////////////////////////////////////////////////
    //callbacks

    public void setActionResponse(BEResponse response) {

        //use this to update App entities when need
        ServerAPI sApi = ServerAPI.getInstance();

        //set here response with error message which will get from firebase error if need
        sApi.setActionResponse(response);
    }

}
