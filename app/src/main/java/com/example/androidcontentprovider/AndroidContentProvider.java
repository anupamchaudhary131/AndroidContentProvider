package com.example.androidcontentprovider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AndroidContentProvider extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.andtoid_content_provider);
    }

    public void onClickAddStudent(View view)
    {
        ContentValues values = new ContentValues();
        values.put(MyContentProvider._Student,((EditText)findViewById(R.id.studentName)).getText().toString();
        values.put(MyContentProvider._School,((EditText)findViewById(R.id.studentSchool)).getText().toString();

        Uri uri = getContentResolver().insert(MyContentProvider.CONTENT_URI, values);

        Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();

    }

    public void onClickRetrieveStudent(View view)
    {
        String URL = "content://com.example.androidcontentprovider/Students";
        Uri students = Uri.parse(URL);
        Cursor c = managedQuery(students, null, null, null, "name");

        if(c.moveToFirst())
        {
        }

        Toast.makeText(getBaseContext(), toString(), Toast.LENGTH_LONG).show();

    }

}