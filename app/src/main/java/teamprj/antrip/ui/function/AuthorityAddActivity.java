package teamprj.antrip.ui.function;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import teamprj.antrip.R;
import teamprj.antrip.data.model.Plan;

public class AuthorityAddActivity extends Activity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userName = user.getEmail().replace(".", "_");
    private List<String> authList;
    private List<String> userList;

    ProgressDialog progressDialog;

    EditText emailEditText;
    Button changePassword;

    String tripName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_authority_add);

        Intent intent = getIntent();
        tripName = Objects.requireNonNull(intent.getExtras()).getString("tripName");

        userList = new ArrayList<>();

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        emailEditText = findViewById(R.id.auth_add_emailText);

//        mAuth = FirebaseAuth.getInstance();
//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                if (firebaseAuth.getCurrentUser() == null) {
//                }
//            }
//        };

        changePassword = findViewById(R.id.auth_add_confirmBtn);

        myRef.child("users").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            userList.add(snapshot.child("email").getValue().toString());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("AuthorityAdd", "data receive error");
                    }
                });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authList = new ArrayList<>();
                if (checkEmail()) {
                    myRef.child("plan").child(userName).child(tripName).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String inputName = emailEditText.getText().toString();
                                    if (dataSnapshot.hasChild("authority")) {
                                        authList = (ArrayList) ((HashMap) dataSnapshot.getValue()).get("authority");
                                    }
                                    if (inputName.equals(userName.replace("_", "."))) {
                                        OkAlertDialog.viewOkAlertDialog(AuthorityAddActivity.this, "추가 실패", "자신의 이메일은 추가할 수 없습니다.");
                                    }
                                    else if (authList.contains(inputName.replace(".", "_"))) {
                                        OkAlertDialog.viewOkAlertDialog(AuthorityAddActivity.this, "추가 실패", "이미 추가된 이메일 입니다.");
                                    } else if (!userList.contains(inputName)) {
                                        OkAlertDialog.viewOkAlertDialog(AuthorityAddActivity.this, "추가 실패", "존재하지 않는 이메일 입니다.");
                                    } else {
                                        authList.add(inputName.replace(".", "_"));
                                        myRef.child("plan").child(userName).child(tripName).child("authority").setValue(authList);
                                        OkAlertDialog.viewOkAlertDialog(AuthorityAddActivity.this, "추가 성공", "추가가 완료되었습니다.");
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("AuthorityAdd", "authority add error");
                                }
                            });
                }
            }
        });
    }

    public boolean checkEmail() {
        emailEditText = findViewById(R.id.auth_add_emailText);

        if (emailEditText.getText().toString().equals("") || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()) {
            emailEditText.setError(getText(R.string.wrongEmail));
            return false;
        }
        emailEditText.setError(null);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        return event.getAction() != MotionEvent.ACTION_OUTSIDE;
    }
}
