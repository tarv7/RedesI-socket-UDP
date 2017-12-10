package ninja.thales.udp_redes_i;

import android.content.Context;
import android.opengl.EGLExt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void setPostBt(View v){
        Button bt = (Button) findViewById(R.id.button);
        bt.setText(R.string.cadastrar);
    }

    public void setGetBt(View v){
        Button bt = (Button) findViewById(R.id.button);
        bt.setText(R.string.obter);
    }

    public void enviaMsg(View v){
        EditText et = (EditText) findViewById(R.id.msg);
        TextView msgTextView = (TextView) findViewById(R.id.msgServ);
        RadioGroup rg = findViewById(R.id.rdg_metodo);
        String rb = null;
        String messageStr = null;
        DatagramSocket s = null;
        List<String> resultados = null;
        ArrayAdapter<String> adaptador;
        ListView lsResultados = (ListView) findViewById(R.id.resultados);

        msgTextView.setText("");
        lsResultados.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,  new ArrayList<String> ()));

        switch (rg.getCheckedRadioButtonId()){
            case R.id.rdb_post:
                rb = "post";
                break;
            case R.id.rdb_get:
                rb = "get";
                break;
        }

        JSONObject obj = new JSONObject();

        try {
            obj.put("metodo", rb);
            messageStr = et.getEditableText().toString();
            obj.put("mensagem", messageStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String msg;
        int server_port = 5005;

        if(messageStr.equals("exit"))
            finish();

        try {
            s = new DatagramSocket();
            s.setSoTimeout(5000);
            InetAddress local = InetAddress.getByName("192.241.139.196");
            int obj_length = obj.toString().length();
            byte[] message = obj.toString().getBytes();
            DatagramPacket p = new DatagramPacket(message, obj_length, local,
                    server_port);
            s.send(p);


            byte[] lMsg = new byte[1024];
            p = new DatagramPacket(lMsg, lMsg.length);
            s.receive(p);
            msg = new String(lMsg, 0, p.getLength());

            if (messageStr.equals("broadcast") || rb.equals("get")) {

                StringBuilder sb = new StringBuilder(msg);
                sb.charAt(msg.length()-1);
                sb.charAt(0);
                msg = sb.toString();
                resultados = new ArrayList<String> (Arrays.asList(msg.split(",")));

                adaptador = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, resultados);
                lsResultados.setAdapter(adaptador);
            } else
                msgTextView.setText("Indice: " + msg);

        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (s != null) {
                s.close();
            }
        }

    }

}
