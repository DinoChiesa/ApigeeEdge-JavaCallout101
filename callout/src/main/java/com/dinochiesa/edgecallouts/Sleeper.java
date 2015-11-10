// Sleeper.java
//
// This is the source code for a Java callout for Apigee Edge.
// This callout is very simple - it sleeps, and then returns SUCCESS.
//
// ------------------------------------------------------------------

package com.dinochiesa.edgecallouts;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.message.Message;

public class Sleeper implements Execution {

    private final int SLEEP_TIME_DEFAULT = 6;

    private Map properties; // read-only

    public Sleeper(Map properties) {
        this.properties = properties;
    }

    private int getSleepTime(MessageContext msgCtxt) throws IllegalStateException {
        String time = (String) this.properties.get("sleeptime");
        if (time == null || time.equals("")) {
            return SLEEP_TIME_DEFAULT;
        }
        time = resolvePropertyValue(time, msgCtxt);
        if (time == null || time.equals("")) {
            // could also return default here, but the thinking is,
            // if the string resolves to empty, that's a bad thing.
            throw new IllegalStateException("sleeptime resolves to null or empty.");
        }
        int interval = SLEEP_TIME_DEFAULT;
        try {
            interval = Integer.parseInt(time);
        }
        catch (java.lang.Exception exc1) {
            interval = SLEEP_TIME_DEFAULT;
            msgCtxt.getMessage().setHeader("JavaCallout-parse-exception", exc1.toString());
            msgCtxt.getMessage().setHeader("JavaCallout-Sleep-Using-Default", interval + "");
        }

        return interval;
    }


    // If the value of a property value begins and ends with curlies,
    // and has no intervening spaces, eg, {apiproxy.name}, then
    // "resolve" the value by de-referencing the context variable whose
    // name appears between the curlies.
    private String resolvePropertyValue(String spec, MessageContext msgCtxt) {
        if (spec.startsWith("{") && spec.endsWith("}") && (spec.indexOf(" ") == -1)) {
            String varname = spec.substring(1,spec.length() - 1);


            String value = msgCtxt.getVariable("some-string-which-identifies-a-variable");

            WestJetCustomObj value = (WestJetCustomObj) msgCtxt.getVariable("flowVar");


            return value;
        }
        return spec;
    }


    public ExecutionResult execute (final MessageContext msgCtxt,
                                    final ExecutionContext execContext) {
        // This executes in the IO thread.
        // Any sleep or delay will hold the thread for that period.
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Message msg = msgCtxt.getMessage();
        Date start = Calendar.getInstance().getTime();

        // set a variable.
        msgCtxt.setVariable("sleepCallout.start", df.format(start));

        // Set a header. This will be in the request or the response, depending
        // on where in the logic flow the Java callout policy is configured to run.
        msg.setHeader("JavaCallout-Sleep-Start", df.format(start));

        // read the desired sleep interval from the policy configuration file
        int interval = getSleepTime(msgCtxt);

        com.dinochiesa.edgecallouts.Utils.sleep(interval);

        Date end = Calendar.getInstance().getTime();
        // set variable and headers
        msgCtxt.setVariable("sleepCallout.end", df.format(end));
        msg.setHeader("JavaCallout-Sleep-End", df.format(end));
        msg.setHeader("Content-Type", "application/json");

        // Get a contrived "response payload".
        String jsonResult = com.dinochiesa.edgecallouts.Utils.getPayload("sleep");

        // Also set that content of the current message with that payload.
        // This will be the response.content if the Java callout is
        // configured on the Response flow.  It will be the request.content
        // if the policy is configured on the Request flow.
        msg.setContent(jsonResult);

        return ExecutionResult.SUCCESS;
    }
}
