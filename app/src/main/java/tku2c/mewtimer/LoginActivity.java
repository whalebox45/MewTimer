package tku2c.mewtimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;

public class LoginActivity extends Activity {

    EditText etUser, etPasswd;
    Button btnLogin;
    static SQLiteDatabase dbUser;
    static final String db_name = "user_db";
    static final String tb_name = "user_table";


    private void initDB() {
        dbUser = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);

        //dbUser.execSQL("DROP TABLE IF EXISTS " + tb_name);

        String createTable = "CREATE TABLE IF NOT EXISTS " + tb_name +
                "(" + "name VARCHAR NOT NULL UNIQUE, " + "passwd VARCHAR NOT NULL);";
        dbUser.execSQL(createTable);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Override the method so that this app won’t be closed accidentally
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.close_message))
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initDB();

        etUser = (EditText) findViewById(R.id.etUser);
        etPasswd = (EditText) findViewById(R.id.etPasswd);

        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = etUser.getText().toString();
                final String password = etPasswd.getText().toString();

                Cursor cursor = dbUser.rawQuery("SELECT * FROM " + tb_name +
                        " WHERE name=\'" + username + "\'", null);

                //When username exists
                if (cursor.moveToFirst()) {
                    if (password.equals(cursor.getString(cursor.getColumnIndex("passwd")))) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        dbUser.close();
                    } else {
                        Toast.makeText(LoginActivity.this, "Password Incorrect", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage("Create New Account?")
                            .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbUser.execSQL("INSERT INTO " + tb_name +
                                            "(name,passwd) Values(\'" + username + "\',\'" +
                                            password + "\')");
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    dbUser.close();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }
            }
        });

    }
}
