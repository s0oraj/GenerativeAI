package blog.cosmos.home.generativeai;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/*Is Main screen of the app which creates a list of messages from user and bot,combined. It then displays this list in recyclerview */

public class MainActivity extends AppCompatActivity {

    private int currentPosition = 0;
    private  boolean isScrollToLastRequired = false;
    private RecyclerView chatsRV;
    private ImageButton sendMsgIB;
    private EditText userMsgEdt;
    private final String USER_KEY = "user";
    private final String BOT_KEY = "bot";
    private RequestQueue mRequestQueue;

    private static final String TAG = "TAG";

    JSONObject jsonBody;
    private ArrayList<Message> messageModalArrayList;
    private MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jsonBody = new JSONObject();
        try{

            jsonBody.put("prompt", "hi");
            jsonBody.put("n", 1);
            jsonBody.put("size","1024x1024");
        }
        catch (JSONException e){

        }
        init();
        clickListener();


    }

    private void clickListener() {
        sendMsgIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userMsgEdt.getText().toString().isEmpty()) {

                    Toast.makeText(MainActivity.this, "Please enter your message..", Toast.LENGTH_SHORT).show();
                    return;
                }

               // sendMessage(userMsgEdt.getText().toString());

                try {
                    sendMessageRequestForImageInput(userMsgEdt.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                userMsgEdt.setText("");

            }
        });

        messageAdapter = new MessageAdapter(messageModalArrayList, this,getSupportFragmentManager());


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);


        /**This block of code scrolls recyclerview to bottom when the keyboard is open and the user sends something
         *
         * **/

        RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                chatsRV.smoothScrollToPosition(messageAdapter.getItemCount());

            }
        };
        messageAdapter.registerAdapterDataObserver(dataObserver);


        /**
         * VVV IMPORTANT -
         * Similar to previous block of code, this block of code scrolls the recyclerview to bottom when keyboard is open (so keyboard doesnt hide recyclerview)
         * BUT, this code works only if we already are at the bottom of recyclerview and user clicks on keyboard
         * if we are not at the bottom of screen and user clicks keyboard, then we do not scroll to bottom or recyclerview
         *
         *
         * **/

        //Setup editText behavior for opening soft keyboard
        userMsgEdt.setOnTouchListener((view, motionEvent) -> {
            InputMethodManager keyboard = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) {
                isScrollToLastRequired = linearLayoutManager.findLastVisibleItemPosition() == messageAdapter.getItemCount() - 1;
                keyboard.showSoftInput(findViewById(R.id.idIBSend), InputMethodManager.SHOW_FORCED);
            }
            return false;
        });
        //Executes recycler view scroll if required.
        chatsRV.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && isScrollToLastRequired) {
                chatsRV.postDelayed(() -> chatsRV.scrollToPosition(
                        chatsRV.getAdapter().getItemCount() - 1), 100);
            }
        });


        chatsRV.setLayoutManager(linearLayoutManager);


        chatsRV.setAdapter(messageAdapter);
    }


    private void init(){
        chatsRV = findViewById(R.id.idRVChats);

        sendMsgIB = findViewById(R.id.idIBSend);
        userMsgEdt = findViewById(R.id.idEdtMessage);

        // Make a new volley request queue
        mRequestQueue = Volley.newRequestQueue(MainActivity.this);
        mRequestQueue.getCache().clear();

        messageModalArrayList = new ArrayList<>();
    }

  // Send message request using volley
    private void sendMessage(String userMsg) {


        String url1 = Utils.URL + "?apiKey=" + Utils.apiKey + "&message=" + userMsg + "&chatBotID=" + Utils.chatBotID + "&externalID=" + Utils.externalID;

        
        messageModalArrayList.add(new Message(userMsg, USER_KEY,null));
        messageAdapter.notifyDataSetChanged();

 
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "response " + response.toString());

                try {

                    //JSON parsing to get data back
                    if (response.getInt("success") == 1) {

                        JSONObject m = response.getJSONObject("message");
                        Log.d(TAG, "response " + m.toString());
                        String botResponse = m.getString("message");

                        messageModalArrayList.add(new Message(botResponse, BOT_KEY,null));

                        messageAdapter.notifyDataSetChanged();

                    } else {
                        String error = response.getString("errorMessage");
                        Toast.makeText(getApplicationContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d(TAG, "exception");

                    // handling error response from bot.
                    messageModalArrayList.add(new Message("No response", BOT_KEY,null));
                    messageAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error handling.
                messageModalArrayList.add(new Message("Sorry no response found", BOT_KEY,null));
                Toast.makeText(MainActivity.this, "No response from the bot..", Toast.LENGTH_SHORT).show();
            }
        });

       
        queue.add(jsonObjectRequest);
    }

    private void sendMessageRequestForImageInput (String userMsg) throws JSONException {

        jsonBody = new JSONObject();
        try{

            jsonBody.put("prompt", userMsg);
            jsonBody.put("n", 1);
            jsonBody.put("size","1024x1024");
        }
        catch (JSONException e){

        }
        final JSONObject jsonObject = new JSONObject();
        try{
      // jsonObject.put("prompt","a white siamese cat");
            jsonObject.put("prompt",userMsg);
        jsonObject.put("n",1);
        jsonObject.put("size","300x300");
        } catch (JSONException e){
        e.printStackTrace();

    }



        String url1 = "https://api.openai.com/v1/images/generations";

       messageModalArrayList.add(new Message(userMsg, USER_KEY,null));
       messageAdapter.notifyDataSetChanged();


       // RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url1,
             //   new JSONObject( "{ \"prompt\": \"a white siamese cat\", \"n\": 1, \"size\": \"1024x1024\"}" ),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG",response.toString());
                    //    Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            JSONObject urlObject = jsonArray.getJSONObject(0);
                            String imageUrl = urlObject.getString("url");

                            messageModalArrayList.add(new Message(null, BOT_KEY,imageUrl));

                            messageAdapter.notifyDataSetChanged();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                        // As of f605da3 the following should work
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                // Now you can use any deserializer to make sense of data
                                JSONObject obj = new JSONObject(res);
                                Log.d("TAG",obj.toString());
                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                e1.printStackTrace();
                            } catch (JSONException e2) {
                                // returned data is not JSONObject?
                                e2.printStackTrace();
                            }
                        }

                    }
                }){
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {


                    final String requestBody = jsonBody.toString();
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                  //  VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
            @NonNull
            @Override
            public Map<String, String> getHeaders() throws
                    AuthFailureError {
                // Build the headers

                final Map<String, String> headers = new
                        HashMap<>();
                headers.put("Content-Type",
                        "application/json");
                headers.put("Accept", "application/json");
                headers.put("Accept-Encoding", "utf-8");


              /*  String USERNAME = "Admin";
                String PASSWORD = "Admin";
                // add headers <key,value>
                String credentials = USERNAME+":"+PASSWORD;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);

               */
              /*  headers.put("Authorization", "Bearer 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");
                return headers;
               */
                String key = "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";
               // String auth = "Bearer " + Base64.encodeToString(key.getBytes(),Base64.NO_WRAP);
                String auth = "Bearer " +key;
                headers.put("Authorization", auth);
                return headers;
            }

        };

        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });


        // Start Request
        Volley.newRequestQueue(this).add(jsonObjectRequest);


    }

    
}
