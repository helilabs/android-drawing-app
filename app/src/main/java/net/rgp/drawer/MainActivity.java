package net.rgp.drawer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.rgp.drawer.dialogs.BrushSizeChooserFragment;
import net.rgp.drawer.dialogs.IPChooserFragment;
import net.rgp.drawer.listeners.OnNewBrushSizeSelectedListener;
import net.rgp.drawer.listeners.OnNewIPSelectedListener;
import net.rgp.drawer.views.CustomView;

import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private PrintWriter socketSend;
    private boolean socketConnected = false;
    private static final int SERVERPORT = 3000;
    private String SERVER_IP = "192.168.43.114";

    private Toolbar mToolbar_top;
    private Toolbar mToolbar_bottom;
    private CustomView mCustomView;

    private String LOG_CAT = "Drawer";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCustomView = (CustomView)findViewById(R.id.custom_view);
        //mToolbar_top = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar_top);

        mToolbar_bottom = (Toolbar)findViewById(R.id.toolbar_bottom);
        mToolbar_bottom.inflateMenu(R.menu.toolbar);
        mToolbar_bottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                handleDrawingIconTouched(item.getItemId());
                return false;
            }
        });

        /*
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToolbar_bottom.setVisibility(View.VISIBLE);
                mFab.setVisibility(View.GONE);
            }
        });
        */
    }

    private boolean handleDrawingIconTouched(int itemId) {
        switch (itemId){
            case R.id.action_erase:
                mCustomView.eraseAll();
                break;
            case R.id.action_save:
                saveImage();
                break;
            case R.id.action_brush:
                brushSizePicker();
                break;
            case R.id.action_send_coordinations:
                sendCoordinates();
                break;
            case R.id.action_connect:
                connectSocket();
                break;
            default:
                return false;
        }
        return true;
    }

    private void saveImage(){
        mCustomView.setDrawingCacheEnabled(true);
        mCustomView.invalidate();
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        File file = new File(path,
                "android_drawing_app.png");
        file.getParentFile().mkdirs();

        try {
            file.createNewFile();
        } catch (Exception e) {
            Log.e(LOG_CAT, e.getCause() + e.getMessage());
        }

        try {
            fOut = new FileOutputStream(file);
        } catch (Exception e) {
            Log.e(LOG_CAT, e.getCause() + e.getMessage());
        }

        if (mCustomView.getDrawingCache() == null) {
            Log.e(LOG_CAT,"Unable to get drawing cache ");
        }

        mCustomView.getDrawingCache()
                .compress(Bitmap.CompressFormat.JPEG, 85, fOut);

        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e(LOG_CAT, e.getCause() + e.getMessage());
        }
    }

    private void brushSizePicker(){
        //Implement get/set brush size
        BrushSizeChooserFragment brushDialog = BrushSizeChooserFragment.NewInstance((int) mCustomView.getLastBrushSize());
        brushDialog.setOnNewBrushSizeSelectedListener(new OnNewBrushSizeSelectedListener() {
            @Override
            public void onNewBrushSizeSelected(float newBrushSize) {
                mCustomView.setBrushSize(newBrushSize);
                mCustomView.setLastBrushSize(newBrushSize);
            }

            @Override
            public void OnNewBrushSizeSelected(int progressChanged) {
                mCustomView.setBrushSize(progressChanged);
                mCustomView.setLastBrushSize(progressChanged);
            }
        });
        brushDialog.show(getSupportFragmentManager(), "Dialog");
    }

    private void sendCoordinates(){
        if(socketConnected){
            JSONArray coordinates = mCustomView.getCoordinates();
            socketSend.println(coordinates.toString());
            Log.d("Bytes Count"," "+coordinates.toString().getBytes().length);
            socketSend.flush();
        }else{
            Toast.makeText(getApplicationContext(),"Not Connected",Toast.LENGTH_LONG).show();
        }
    }

    public void connectSocket(){
        if(!socketConnected){
            new Thread(new ClientThread()).start();
            Log.d("Connect Socket","Trying to connect");
        }else{

        }
    }

    public void handleIP(){
        IPChooserFragment IPDialog = IPChooserFragment.newInstance(SERVER_IP);
        IPDialog.setOnNewIPChooserSelectedListener(new OnNewIPSelectedListener() {
            @Override
            public void onNewIPSelected(String IP) {
                SERVER_IP = IP;
                Log.d("Server address",SERVER_IP+"");
            }
        });
        IPDialog.show(getSupportFragmentManager(), "Dialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d("new item selected","setting item selected");
            handleIP();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                Log.d("ADDRESS on connect",SERVER_IP);
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);

                socketSend = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                socketConnected = true;
            } catch (UnknownHostException e1) {
                Log.d("UnKnownHostException", e1.getMessage());
                socketConnected = false;
            } catch (IOException e1) {
                Log.d("IOException ", e1.getMessage());
                socketConnected = false;
            }

        }

    }
}
