package teamprj.antrip.ui.function;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import teamprj.antrip.R;

public class NoticeArticleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_article);

        Intent intent = getIntent();
        String main_title = intent.getStringExtra("articleTitle");
        String sub_title = intent.getStringExtra("articleDate");
        String contents = intent.getStringExtra("articleContent");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView temp = findViewById(R.id.main_title);
        temp.setText(main_title);
        temp = findViewById(R.id.sub_title);
        temp.setText(sub_title);
        temp = findViewById(R.id.content);
        temp.setText(contents);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 뒤로 가기
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
