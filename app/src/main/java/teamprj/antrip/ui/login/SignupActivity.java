package teamprj.antrip.ui.login;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import teamprj.antrip.R;
import teamprj.antrip.data.AppSingleton;

public class SignupActivity extends Activity {


    private static final String TAG = "signUp";
    private static final String URL_FOR_REGISTRATION = "http://antrip.kro.kr/app/signup.php";
    private EditText emailText, passwordText, pwCheckText, nameText, birthText;

    Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener myDatePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup);

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.pwText);
        pwCheckText = findViewById(R.id.pwCheckText);
        nameText = findViewById(R.id.nameText);
        birthText = findViewById(R.id.birthText);
        birthText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SignupActivity.this, myDatePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    public void signUp(View v) {
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.pwText);
        pwCheckText = findViewById(R.id.pwCheckText);
        nameText = findViewById(R.id.nameText);
        birthText = findViewById(R.id.birthText);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String pwCheck = pwCheckText.getText().toString();
        String name = nameText.getText().toString();
        String birth = birthText.getText().toString();

        if (checkError(email, password, pwCheck, name, birth)) {
            register(email, password, name, birth);
//            finish();
        }
    }

    public boolean checkError(String email, String password, String pwCheck, String name, String birth) {
        if (email.equals("") || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            Toast.makeText(getApplicationContext(), R.string.wrongEmail, Toast.LENGTH_SHORT).show();
        else if (password.equals("") || !password.equals(pwCheck))
            Toast.makeText(getApplicationContext(), R.string.wrongPassword, Toast.LENGTH_SHORT).show();
        else if (name.equals(""))
            Toast.makeText(getApplicationContext(), R.string.wrongName, Toast.LENGTH_SHORT).show();
        else if (birth.equals(""))
            Toast.makeText(getApplicationContext(), R.string.wrongBirth, Toast.LENGTH_SHORT).show();
        else
            return true;
        return false;
    }

    private void register(final String email, final String password, final String name, final String birth) {
        // Tag used to cancel the request
        String cancel_req_tag = "register";

        StringRequest strReq = new StringRequest(Request.Method.POST, URL_FOR_REGISTRATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        String user = jObj.getJSONObject("user").getString("name");
                        Toast.makeText(getApplicationContext(), R.string.welcome + user, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                final String types = "1";
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                params.put("name", name);
                params.put("birth", birth);
                params.put("type", types);
                return params;
            }
        };
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, cancel_req_tag);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        return event.getAction() != MotionEvent.ACTION_OUTSIDE;
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";    // 출력형식   2019-06-26
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);
        birthText.setText(sdf.format(myCalendar.getTime()));
    }

}