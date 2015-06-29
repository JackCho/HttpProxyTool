package ele.me.httpproxy;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class HttpProxyManager {

    private static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    private static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    private static void setProxySettings(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "proxySettings");
    }


    private static WifiConfiguration getCurrentWifiConfiguration(WifiManager manager) {
        if (!manager.isWifiEnabled())
            return null;

        List<WifiConfiguration> configurationList = manager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int cur = manager.getConnectionInfo().getNetworkId();
        for (int i = 0; i < configurationList.size(); ++i) {
            WifiConfiguration wifiConfiguration = configurationList.get(i);
            if (wifiConfiguration.networkId == cur)
                configuration = wifiConfiguration;
        }

        return configuration;
    }

    public static void setWifiProxySettings(Context context, String ip, String port) throws Exception {
        //get the current wifi configuration
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        //get the link properties from the wifi configuration
        Object linkProperties = getField(config, "linkProperties");
        if (null == linkProperties)
            return;

        //get the setHttpProxy method for LinkProperties
        Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
        Class[] setHttpProxyParams = new Class[1];
        setHttpProxyParams[0] = proxyPropertiesClass;
        Class lpClass = Class.forName("android.net.LinkProperties");
        Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
        setHttpProxy.setAccessible(true);

        //get ProxyProperties constructor
        Class[] proxyPropertiesCtorParamTypes = new Class[3];
        proxyPropertiesCtorParamTypes[0] = String.class;
        proxyPropertiesCtorParamTypes[1] = int.class;
        proxyPropertiesCtorParamTypes[2] = String.class;

        Constructor proxyPropertiesCtor = proxyPropertiesClass.getConstructor(proxyPropertiesCtorParamTypes);

        //create the parameters for the constructor
        Object[] proxyPropertiesCtorParams = new Object[3];
        proxyPropertiesCtorParams[0] = ip;
        proxyPropertiesCtorParams[1] = Integer.valueOf(port);
        proxyPropertiesCtorParams[2] = null;

        //create a new object using the params
        Object proxySettings = proxyPropertiesCtor.newInstance(proxyPropertiesCtorParams);

        //pass the new object to setHttpProxy
        Object[] params = new Object[1];
        params[0] = proxySettings;
        setHttpProxy.invoke(linkProperties, params);

        setProxySettings("STATIC", config);

        //save the settings
        manager.updateNetwork(config);
        manager.disconnect();
        manager.reconnect();

    }

    public static void unsetWifiProxySettings(Context context) throws Exception {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        //get the link properties from the wifi configuration
        Object linkProperties = getField(config, "linkProperties");
        if (null == linkProperties)
            return;

        //get the setHttpProxy method for LinkProperties
        Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
        Class[] setHttpProxyParams = new Class[1];
        setHttpProxyParams[0] = proxyPropertiesClass;
        Class lpClass = Class.forName("android.net.LinkProperties");
        Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
        setHttpProxy.setAccessible(true);

        //pass null as the proxy
        Object[] params = new Object[1];
        params[0] = null;
        setHttpProxy.invoke(linkProperties, params);

        setProxySettings("NONE", config);

        //save the config
        manager.updateNetwork(config);
        manager.disconnect();
        manager.reconnect();
    }
}
