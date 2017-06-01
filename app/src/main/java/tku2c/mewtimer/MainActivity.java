package tku2c.mewtimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    static final int NEW_TIMER_REQUEST = 0;
    static final int EDIT_TIMER_REQUEST = 1;

    static final int TIMER_MAX_COUNT = 8;

    static final String db_name = "tone_db";
    static final String tb_name = "tone_table";

    public static SQLiteDatabase dbTone;


    public static final ArrayList<Tone> lstTone = new ArrayList<>();
    private void initListTone() {
        lstTone.add(new Tone(R.raw.ovending, "oven_ding"));
        lstTone.add(new Tone(R.raw.catmeow, "cat_meow"));
        lstTone.add(new Tone(R.raw.catsound, "cat_meow2"));
        lstTone.add(new Tone(R.raw.a8bitbeep, "bit_beep"));
    }


    private ListView lvItems;
    private ArrayList<Product> lstProducts;
    private CountdownAdapter adapter;


    private void initDB() {
        /*
         *  I used to forget to type a space, and the DB just crashed.
         */

        initListTone();

        dbTone = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);

        dbTone.execSQL("DROP TABLE IF EXISTS "+tb_name);


        String createTable = "CREATE TABLE IF NOT EXISTS " + tb_name +
                "(" + "name VARCHAR UNIQUE, " +
                "resID INTEGER NOT NULL)";
        dbTone.execSQL(createTable);

        // Insert each Tone in the database db

        for (Tone tn : lstTone) {
            try {
                dbTone.execSQL("INSERT INTO " + tb_name +
                        "(name,resID) Values(\'" + tn.getName() + "\',\'" +
                        tn.getResID() + "\')");
            }
            catch (Exception e){}
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        switch (requestCode) {
            case EDIT_TIMER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Bundle bundleAdd = data.getExtras();
                    try {
                        int position = bundleAdd.getInt("timer_position");
                        long length = bundleAdd.getLong("edit_time");
                        Product pd = lstProducts.get(position);
                        pd.setName(bundleAdd.getString("edit_name"));
                        pd.setDuration(length);
                        pd.setToneID(bundleAdd.getInt("tone_id"));


                        /*TODO*/

                        long ct = System.currentTimeMillis();
                        switch (pd.getCurrentStatus()) {
                            case PAUSED:
                            case RUNNING:
                                pd.setCurrentStatus(ProductStatus.RUNNING);
                                pd.setExpirationTime(ct + pd.getDuration());
                                adapter.getArrViewHolder().get(position).updateTimeRemaining(ct);
                                pd.setCurrentStatus(ProductStatus.STOPPED);
                            default:
                                break;
                        }

                        adapter.getArrViewHolder().get(position).RefreshCurrentView(length);



                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case NEW_TIMER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Bundle bundleAdd = data.getExtras();
                    try {
                        lstProducts.add(new Product(bundleAdd.getString("edit_name"), bundleAdd.getLong("edit_time"), bundleAdd.getInt("tone_id")));
                        lvItems.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manu_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_timer:
                try {
                    if (lstProducts.size() >= TIMER_MAX_COUNT) throw new Exception();
                    Intent intent = new Intent(this, EditTimerActivity.class);

                    Bundle bundleAdd = new Bundle();
                    bundleAdd.putInt("timer_position", 0);
                    bundleAdd.putString("timer_name", Product.DEFAULT_NAME);
                    bundleAdd.putLong("timer_length", Product.DEFAULT_DURATION);

                    intent.putExtras(bundleAdd);
                    this.startActivityForResult(intent, MainActivity.NEW_TIMER_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Override the method so that this app won’t be closed accidentally
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.exit_message))
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
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
        setContentView(R.layout.activity_main);

        this.initDB();

        lvItems = (ListView) findViewById(R.id.lvItems);

        lstProducts = new ArrayList<>();

        adapter = new CountdownAdapter(this, lstProducts);

        lvItems.setAdapter(adapter);

    }

    private class CountdownAdapter extends ArrayAdapter<Product> {

        public List<ViewHolder> getArrViewHolder() {
            return arrViewHolder;
        }

        private final List<ViewHolder> arrViewHolder;
        private LayoutInflater inflater;
        private Handler handler;
        private Timer timer;
        private Runnable updateRemainingTimeRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (arrViewHolder) {
                    for (ViewHolder holder : arrViewHolder) {
                        holder.updateTimeRemaining(System.currentTimeMillis());
                    }
                }
            }
        };

        CountdownAdapter(Context context, ArrayList<Product> arrProduct) {
            super(context, 0, arrProduct);
            this.inflater = LayoutInflater.from(context);
            this.arrViewHolder = new ArrayList<>();
            this.handler = new Handler();
            this.timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(updateRemainingTimeRunnable);
                }
            }, 1, 200);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            final Product mProduct = this.getItem(position);

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                holder.tvTimerName = (TextView) convertView.findViewById(R.id.tvTimerName);
                holder.tvRemainTime = (TextView) convertView.findViewById(R.id.tvRemainTime);
                holder.btnPlayPause = (ImageButton) convertView.findViewById(R.id.btnPlayPause);
                holder.btnEdit = (ImageButton) convertView.findViewById(R.id.btnEdit);
                holder.btnReset = (ImageButton) convertView.findViewById(R.id.btnReset);


                holder.btnPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (mProduct.getCurrentStatus()) {
                            case RUNNING:
                                mProduct.setCurrentStatus(ProductStatus.PAUSED);
                                break;
                            case PAUSED:
                            case STOPPED:
                                if (mProduct.getCurrentStatus() == ProductStatus.STOPPED)
                                    mProduct.setExpirationTime(System.currentTimeMillis() + mProduct.getDuration());
                                mProduct.setCurrentStatus(ProductStatus.RUNNING);
                                break;
                        }
                    }
                });

                holder.btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long ct = System.currentTimeMillis();
                        switch (mProduct.getCurrentStatus()) {
                            case PAUSED:
                            case RUNNING:
                                mProduct.setCurrentStatus(ProductStatus.RUNNING);
                                mProduct.setExpirationTime(ct + mProduct.getDuration());
                                holder.updateTimeRemaining(ct);
                                mProduct.setCurrentStatus(ProductStatus.STOPPED);
                            default:
                                break;
                        }
                    }
                });

                holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProduct.setCurrentStatus(ProductStatus.PAUSED);
                        Intent intent = new Intent(CountdownAdapter.this.getContext(), EditTimerActivity.class);
                        Bundle bundleEdit;
                        bundleEdit = new Bundle();
                        bundleEdit.putInt("timer_position", position);
                        bundleEdit.putString("timer_name", mProduct.getName());
                        bundleEdit.putLong("timer_length", mProduct.getDuration());
                        intent.putExtras(bundleEdit);
                        ((Activity) getContext()).startActivityForResult(intent, MainActivity.EDIT_TIMER_REQUEST);
                    }
                });


                convertView.setTag(holder);
                synchronized (arrViewHolder) {
                    arrViewHolder.add(holder);
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.setData(getItem(position));
            return convertView;
        }

    }

    class ViewHolder {
        TextView tvTimerName;
        TextView tvRemainTime;
        ImageButton btnPlayPause;
        ImageButton btnEdit;
        ImageButton btnReset;
        Product mProduct;

        public void setData(Product item) {
            this.mProduct = item;
            this.tvTimerName.setText(item.getName());
            updateTimeRemaining(System.currentTimeMillis());
        }

        private void RefreshCurrentView(long time) {
            RefreshCurrentView();
            int seconds = (int) (time / 1000 % 60);
            int minutes = (int) (time / 1000 / 60 % 60);
            int hours = (int) (time / 1000 / 3600);
            tvRemainTime.setText(String.format("%02d", hours) + ":" +
                    String.format("%02d", minutes) + ":" +
                    String.format("%02d", seconds));

        }

        private void RefreshCurrentView() {
            switch (mProduct.getCurrentStatus()) {
                case PAUSED:
                    tvTimerName.setText(mProduct.getName());
                    tvRemainTime.setAlpha(1f);
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                    break;
                case STOPPED:
                    tvTimerName.setText(mProduct.getName());
                    tvRemainTime.setAlpha(0.6f);
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                    break;
                case RUNNING:
                    tvTimerName.setText(mProduct.getName());
                    tvRemainTime.setAlpha(1f);
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                    break;
            }
        }

        public void updateTimeRemaining(long currentTime) {

            long timeDiff = mProduct.getExpirationTime() - currentTime;
            long duration = mProduct.getDuration();

            if (timeDiff > 0) {
                switch (mProduct.getCurrentStatus()) {
                    case PAUSED:
                    case STOPPED:
                        mProduct.setExpirationTime(currentTime + mProduct.getRemainTime());
                        break;
                    case RUNNING:
                        mProduct.setRemainTime(timeDiff);
                        break;
                }
                RefreshCurrentView(timeDiff + 500);

            } else {
                if (mProduct.getCurrentStatus() != ProductStatus.STOPPED) {
                    mProduct.setCurrentStatus(ProductStatus.STOPPED);
                    timeUpEvent();
                }
                RefreshCurrentView(duration);
            }
        }

        private void timeUpEvent() {
            AlertDialog alertDialog;
            AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
            ad.setTitle(getResources().getString(R.string.timeup_message)).setMessage(mProduct.getName());
            ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog = ad.create();
            alertDialog.show();
            Vibrator myVibrator = (Vibrator) MainActivity.this.getSystemService(Service.VIBRATOR_SERVICE);
            myVibrator.vibrate(100);
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), mProduct.getToneID());
            mp.start();
        }
    }
}

