package ele.me.httpproxy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;

public class MainActivity extends Activity {

    private final String sp_name = "ip_port";
    private final String key_ip = "ip";
    private final String key_port = "port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText ipView = (EditText) findViewById(R.id.ip);
        final EditText portView = (EditText) findViewById(R.id.port);
        ipView.setText(get(key_ip));
        portView.setText(get(key_port));

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipView.getText().toString();
                if (TextUtils.isEmpty(ip)) {
                    Toast.makeText(MainActivity.this, "ip cannot be null", Toast.LENGTH_SHORT).show();
                    return;
                }
                Matcher ipMatcher = Patterns.IP_ADDRESS.matcher(ip);
                if (!ipMatcher.find()) {
                    Toast.makeText(MainActivity.this, "wrong ip address", Toast.LENGTH_SHORT).show();
                    return;
                }
                String port = portView.getText().toString();
                if (TextUtils.isEmpty(port)) {
                    Toast.makeText(MainActivity.this, "port cannot be null", Toast.LENGTH_SHORT).show();
                    return;
                }
                save(key_ip, ip);
                save(key_port, port);
                try {
                    HttpProxyManager.setWifiProxySettings(getApplicationContext(), ip, port);
                    hideSoftKeyboard();
                    Toast.makeText(MainActivity.this, "Congratulation, your phone is configured successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Sorry! Your phone is not supported now", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.unset_proxy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    HttpProxyManager.unsetWifiProxySettings(getApplicationContext());
                    Toast.makeText(MainActivity.this, "Congratulation, succeed to clear http proxy", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Sorry! Fail to clear http proxy", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void hideSoftKeyboard() {
        EditText ipView = (EditText) findViewById(R.id.ip);
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ipView.getWindowToken(), 0);
    }

    private void save(String key, String value) {
        getSharedPreferences(sp_name, 0).edit().putString(key, value).commit();
    }

    private String get(String key) {
        return getSharedPreferences(sp_name, 0).getString(key, null);
    }

}
