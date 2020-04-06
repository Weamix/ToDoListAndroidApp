package com.example.myapplication2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static EditText edit;
    public static Button button;
    public static ListView list;
    private static ArrayList<String> todoItems;
    private ArrayAdapter<String> aa;
    private Object Menu;
    private NotesDbAdapter db;

    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor c = db.fetchAllNotes();
        startManagingCursor(c);

        String[] from = new String[] { NotesDbAdapter.KEY_TITLE };
        int[] to = new int[] { R.id.title };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.item, c, from, to);
        list.setAdapter(notes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit = findViewById(R.id.editText);
        list = findViewById(R.id.list);
        button = findViewById(R.id.button);
        todoItems = new ArrayList<String>() ;
        aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, todoItems);
        //list.setAdapter(aa);
        db = new NotesDbAdapter(this);
        db.open();
        fillData();

        edit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                    {
                        /*todoItems.add(0, edit.getText().toString()); // 1
                        aa.notifyDataSetChanged(); // 2*/
                        db.createNote(edit.getText().toString(),"");
                        edit.setText(""); // 3 - remise à vide de l'EditText
                        fillData();
                        return true;
                    }
                return false;
            }
        }) ;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*todoItems.add(0, edit.getText().toString());
                aa.notifyDataSetChanged(); // 2*/
                db.createNote(edit.getText().toString(),"");
                edit.setText(""); // 3 - remise à vide de l'EditText
                fillData();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //todoItems.remove(position);
                db.deleteNote(id);
                //aa.notifyDataSetChanged();
                fillData();
            }
        });
        registerForContextMenu(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
                // Add the buttons
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //todoItems.clear();
                        db.clear();
                        aa.notifyDataSetChanged();
                        fillData();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog = builder.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contextuel, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor SelectedTaskCursor = (Cursor) list.getItemAtPosition(info.position);
        final String SelectedTask = SelectedTaskCursor.getString(SelectedTaskCursor.getColumnIndex("title"));

        switch (item.getItemId()) {
            case R.id.find_maps:
                Uri location = Uri.parse("geo:0,0?q="+SelectedTask);
                // Or map point based on latitude/longitude
                // Uri location = Uri.parse("geo:37.422219,-122.08364?z=14"); // z param is zoom level
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
                startActivity(mapIntent);
                return true;
            case R.id.find_google:
                Uri webpage = Uri.parse("https://www.google.com/search?q="+SelectedTask);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
