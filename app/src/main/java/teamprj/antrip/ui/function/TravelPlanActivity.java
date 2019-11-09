package teamprj.antrip.ui.function;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import teamprj.antrip.R;
import teamprj.antrip.data.model.Plan;
import teamprj.antrip.data.model.Travel;


public class TravelPlanActivity extends AppCompatActivity implements ExpandableListAdapter.OnStartDragListner {

    static RecyclerView recyclerview;
    ItemTouchHelper mItemTouchHelper;
    static List<ExpandableListAdapter.Item> data;
    static ExpandableListAdapter mAdapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    ExpandableListAdapter.OnStartDragListner thisListner = this;
    boolean isNewPlan = true;
    boolean isChange = true;
    boolean isFinish = false;
    String tripName = "새 여행";
    String beforeTripName = "";
    String admin = "admin";
    int period;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_plan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

            GoogleMapFragment googleMapFragment = new GoogleMapFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.googleMapFragment, googleMapFragment, "main")
                    .commit();
        }

        recyclerview = findViewById(R.id.recyclerView_travel_plan);
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        data = new ArrayList<>();

        Intent intent = getIntent();
        if (intent.getExtras().getString("savedTrip").equals("true")) {
//            tripName = intent.getExtras().getString("tripName");
//            admin =  intent.getExtras().getString("admin");
            tripName = "test trip";
            admin = "admin";
            setTitle(tripName);
            isNewPlan = false;

            myRef.child("plan").child("admin").child("test trip").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Plan plan = dataSnapshot.getValue(Plan.class);
                        period = plan.getPeriod();
                        for (int i = 0; i < plan.getAuthority().size(); i++) {
                            // 공유 목록
                        }
                        HashMap<String, ArrayList<Travel>> planHashMap = plan.getTravel();
                        String headerText = "0일차";
                        for (int i = 1; i <= plan.getPeriod(); i++) {
                            ArrayList<Travel> travelList = planHashMap.get(i + "_day");
                            headerText = headerText.replace(Integer.toString(i - 1), Integer.toString(i));
                            data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, headerText));
                            for (int j = 0; j < travelList.size(); j++) {
                                String name = travelList.get(j).getName();
                                String country = travelList.get(j).getCountry();
                                LatLng latLng = new LatLng(travelList.get(j).getLatitude(), travelList.get(j).getLongitude());
                                data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.DATA,
                                        name, country, latLng, travelList.get(j).isAccommodation()));
                                GoogleMapFragment.selectPlace(latLng, name, country);
                            }
                            data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "추가"));
                        }

                        mAdapter = new ExpandableListAdapter(data, thisListner);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("ErrorTravelPlanActivity", "data receive error");
                    }
                });
        } else {
            setTitle(tripName);
            period = Integer.parseInt(intent.getExtras().getString("period"));
            admin =  intent.getExtras().getString("admin");

            String headerText = "0일차";
            for (int i = 0; i < period; i++)
            {
                headerText = headerText.replace(Integer.toString(i), Integer.toString(i + 1));
                data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, headerText));
                data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "추가"));
            }

            mAdapter = new ExpandableListAdapter(data, this);
        }

        ItemTouchHelperCallback mCallback = new ItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerview);
        recyclerview.setAdapter(mAdapter);

        ImageButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickSaveButton();
            }
        });

        ImageButton calcButton = findViewById(R.id.calcButton);
        calcButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                List<Travel> calcList = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    ExpandableListAdapter.Item getData = data.get(i);
                    if (getData.type == ExpandableListAdapter.DATA) {
                        calcList.add(new Travel(getData.name, getData.country, getData.latLng.latitude, getData.latLng.longitude, getData.accommodation));
                    }
                }
                for (int i = 0; i < calcList.size(); i++) {
                    Log.d("calcList", calcList.get(i).getName() + ", " + calcList.get(i).isAccommodation() + ", " +
                            calcList.get(i).getLatitude() + ", " + calcList.get(i).getLongitude());
                }
            }
        });

        ImageButton shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickSaveButton();
            }
        });
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder holder) {
        mItemTouchHelper.startDrag(holder);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TravelPlanActivity.this);
        builder.setTitle("저장 확인");
        builder.setMessage("저장 하시겠습니까?.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isFinish = true;
                        clickSaveButton();
                    }
                });
        builder.setNegativeButton("아니요",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void addItem(int index, String name, String country, LatLng latLng, boolean accommodation){
        data.add(index, new ExpandableListAdapter.Item(ExpandableListAdapter.DATA, name, country, latLng, accommodation));
        if (accommodation) {
            index = mAdapter.moveAccommodation(index);
        }
        recyclerview.scrollToPosition(index);
        recyclerview.setAdapter(mAdapter);
        GoogleMapFragment.selectPlace(latLng, name, country);

    }

    public static int checkAccommodation(int position) {
        int count = 0;
        for (int i = position; i >= 0; i--) {
            if (data.get(i).type == ExpandableListAdapter.DATA) {
                if (data.get(i).accommodation) {
                    count++;
                    if (count >= 2) return -1;
                }
            } else if (data.get(i).type == ExpandableListAdapter.HEADER) {
                break;
            }
        }
        return 0;
    }

    public static boolean checkDuplicateData(String name, String country) {
        for (ExpandableListAdapter.Item item : data) {
            if (item.name.equals(name) && item.country.equals(country))
                return false;
        }
        return true;
    }

    private static boolean isAccommodationSelected() {
        for (int i = 0; i < data.size() - 1; i++) {
            ExpandableListAdapter.Item item = data.get(i);
            if (item.type == ExpandableListAdapter.HEADER) {
                List<ExpandableListAdapter.Item> invisibleChild = item.invisibleChildren;
                if (invisibleChild != null) {
                    if (!invisibleChild.get(0).accommodation) { // 접힌 데이터 중 첫번째
                        return false;
                    }
                }
                else if (!data.get(i + 1).accommodation) {
                    return false;
                }
            }
        }
        return true;
    }

    private void clickSaveButton() {
        if (!isAccommodationSelected()) {
            OkAlertDialog.viewOkAlertDialogFinish(TravelPlanActivity.this, "숙소를 선택하지 않았습니다.", "각 일차별로 숙소를 지정해주시기 바랍니다.", isFinish);
        } else {
            if (isNewPlan) {
                showTripNameAlert();
            } else {
                savePlan();
            }
        }
    }

    public void showTripNameAlert() {
        final EditText edittext = new EditText(TravelPlanActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(TravelPlanActivity.this);
        builder.setTitle("여행 이름 입력 창");
        builder.setMessage("여행 이름을 입력해주시기 바랍니다.");
        builder.setView(edittext);
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        tripName = edittext.getText().toString();
                        myRef.child("plan").child("admin").child(tripName).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(TravelPlanActivity.this);
                                            builder.setTitle("이름 중복");
                                            builder.setMessage("이미 존재하는 이름입니다.");
                                            builder.setPositiveButton("확인",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick (DialogInterface dialog,int which){
                                                            showTripNameAlert();
                                                        }
                                                    });
                                            builder.show();
                                        } else {
                                            setTitle(tripName);
                                            isNewPlan = false;
                                            savePlan();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d("ErrorTravelPlanActivity", "data receive error");
                                    }
                                });
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.show();
    }

    private void savePlan()
    {
        LinkedHashMap<String, ArrayList<Travel>> travelMap = new LinkedHashMap<>();
        ArrayList<Travel> travelLIst = new ArrayList<>();
        int day = 0;
        try {
            for (int i = 0; i < data.size(); i++)
            {
                int type = data.get(i).type;
                String name = data.get(i).name;
                String country = data.get(i).country;
                boolean accommodation = data.get(i).accommodation;

                if (type == ExpandableListAdapter.CHILD) {
                    // 한 일차의 마지막 지점
                    day++;
                    travelMap.put(day +"_day", travelLIst);
                    travelLIst = new ArrayList<>();
                }
                else if (type == ExpandableListAdapter.HEADER) {
                    List<ExpandableListAdapter.Item> invisibleChild = data.get(i).invisibleChildren;
                    if (invisibleChild != null) {
                        for (int j = 0; j < invisibleChild.size(); j++) {
                            if (invisibleChild.get(j).type == ExpandableListAdapter.CHILD) {
                                // 접힌 부분의 추가 버튼
                                day++;
                                travelMap.put(day +"_day", travelLIst);
                                travelLIst = new ArrayList<>();
                            } else {
                                LatLng latLng = invisibleChild.get(j).latLng;
                                travelLIst.add(new Travel(invisibleChild.get(j).name, country, latLng.latitude, latLng.longitude, accommodation));
                            }
                        }
                    }
                }
                else if (type == ExpandableListAdapter.DATA) {
                    LatLng latLng = data.get(i).latLng;
                    travelLIst.add(new Travel(name, country, latLng.latitude, latLng.longitude, accommodation));
                }
            }
            Plan plan = new Plan();
            List<String> friends = new ArrayList<>();
            friends.add("친구1");
            friends.add("친구2");
            friends.add("친구3");
            plan.setAuthority(friends);
            plan.setPeriod(period);
            plan.setTravel(travelMap);
            myRef.child("plan").child(admin).child(tripName).setValue(plan);

            OkAlertDialog.viewOkAlertDialogFinish(TravelPlanActivity.this, "저장 완료", "저장이 완료되었습니다.", isFinish);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
