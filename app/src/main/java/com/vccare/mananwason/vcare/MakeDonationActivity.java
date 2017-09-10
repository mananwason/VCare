package com.vccare.mananwason.vcare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MakeDonationActivity extends AppCompatActivity {
    TextView textView;
    Button makepymet;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_donation);
        final String hosp = getIntent().getExtras().getString("HOSP");
        textView = (TextView) findViewById(R.id.tv_money);
        makepymet = (Button) findViewById(R.id.button2);
        editText = (EditText) findViewById(R.id.ed_money);
        textView.setText(hosp);

        makepymet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText() != null) {
//                    ref.child("Transactions").child(hosp).setValue(editText.getText());
                    startActivity(new Intent(MakeDonationActivity.this, SuccessfulActivity.class));
                }
            }
        });


    }
}
