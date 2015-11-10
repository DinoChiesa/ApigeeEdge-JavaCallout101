// Utils.java
//
//
// Tuesday, 10 September 2013, 16:59
//
// ------------------------------------------------------------------

package com.dinochiesa.edgecallouts;

import java.util.concurrent.TimeUnit;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

// // Apache HttpComponents (if you wanted to call out to an HTTP endpoint)
// import org.apache.http.client.HttpClient;
// import org.apache.http.impl.client.DefaultHttpClient;
// import org.apache.http.client.methods.HttpPost;
// import org.apache.http.HttpEntity;
// import org.apache.http.HttpResponse;
// import org.apache.http.entity.StringEntity;
// import org.apache.http.util.EntityUtils;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
//import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.message.Message;

public class Utils {

    private static void putProp(Map<String, String> map, String propname) {
        map.put(propname, System.getProperty(propname));
    }

    public static void sleep() {
        sleep(6);
    }

    public static void sleep(int seconds) {
        try {
            if (seconds<0 || seconds>60) seconds=6;
            TimeUnit.SECONDS.sleep(seconds);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static void logEvent(Map properties, String payload) {
        try {
            // Will go to system.out of Message Processor.
            // Not visible to users if deployed on Apigee Edge public cloud.
            System.out.println(payload);
        }
        catch (java.lang.Exception exc1) {
            System.out.println("Exception:" + exc1.toString());
            exc1.printStackTrace();
        }
    }


    public static String getPayload(String label) {
        String jsonResult = "{}";
        Map<String, String> map0 = new HashMap<String, String>();
        putProp(map0,"java.version");
        putProp(map0,"java.vendor");
        putProp(map0,"os.name");
        putProp(map0,"os.arch");
        putProp(map0,"os.version");
        putProp(map0,"java.runtime.name");
        putProp(map0,"java.runtime.version");
        putProp(map0,"java.vm.name");
        //putProp(map0,"java.class.path"); // too long!

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", "OK");
        map.put("source", label);
        map.put("category", "INFO");
        map.put("properties", map0);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date now = Calendar.getInstance().getTime();
        map.put("mystamp", df.format(now));

        try {
            ObjectMapper om = new ObjectMapper();

            jsonResult = om.writer()
                .withDefaultPrettyPrinter()
                .writeValueAsString(map);
        }
        catch(java.io.IOException exc1) {
            jsonResult = "{\"exception\": \"" + exc1.toString() + "\"}";
        }

        return jsonResult;
    }

}
