// ExampleCallout.java
//
// This is the source code for an example Java callout for Apigee.
// This callout is very simple - it retrieves a setting, sets
// a variable and a header, and then returns SUCCESS.
//
// ------------------------------------------------------------------

package com.dinochiesa.edgecallouts;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExampleCallout implements Execution {
  private static final Pattern variableReferencePattern =
      Pattern.compile("(.*?)\\{([^\\{\\} :][^\\{\\} ]*?)\\}(.*?)");
  private static final int SETTING_DEFAULT = 42;
  private Map properties; // read-only

  public ExampleCallout(Map properties) {
    this.properties = properties;
  }

  private int getSetting(MessageContext msgCtxt) throws IllegalStateException {
    // This shows how to retrieve a setting from a property
    // in the JavaCallout policy configuration.
    // In this case the setting is coerced to an integer value.
    String value = (String) this.properties.get("setting");
    if (value == null || value.equals("")) {
      return SETTING_DEFAULT;
    }
    value = resolveVariableReferences(value, msgCtxt);
    if (value == null || value.equals("")) {
      // Could also return default here, but the thinking is,
      // if someone provided a string and it resolves to empty, that's a bad thing.
      throw new IllegalStateException("value resolves to null or empty.");
    }
    int actualValue = SETTING_DEFAULT;
    try {
      actualValue = Integer.parseInt(value);
    } catch (java.lang.Exception exc1) {
      actualValue = SETTING_DEFAULT;
      msgCtxt.getMessage().setHeader("JavaCallout-parse-exception", exc1.toString());
    }

    return actualValue;
  }

  /*
   *
   * If a property holds one or more segments wrapped with begin and end
   * curlies, eg, {apiproxy.name}, then "resolve" the value by de-referencing
   * the context variable whose name appears between the curlies.
   **/
  protected String resolveVariableReferences(String spec, MessageContext msgCtxt) {
    if (spec == null || spec.equals("")) return spec;
    Matcher matcher = variableReferencePattern.matcher(spec);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(sb, "");
      sb.append(matcher.group(1));
      String ref = matcher.group(2);
      String[] parts = ref.split(":", 2);
      Object v = msgCtxt.getVariable(parts[0]);
      if (v != null) {
        sb.append((String) v);
      } else if (parts.length > 1) {
        sb.append(parts[1]);
      }
      sb.append(matcher.group(3));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  public ExecutionResult execute(final MessageContext msgCtxt, final ExecutionContext execContext) {
    // This executes in the IO thread.
    // Any time consumed here will hold the thread for that period.
    Instant stamp = Instant.now();
    String formattedStamp = DateTimeFormatter.ISO_INSTANT.format(stamp);
    Message msg = msgCtxt.getMessage();

    // set a variable.
    msgCtxt.setVariable("sleepCallout.stamp", formattedStamp);

    // Set a header. This will be in the request or the response, depending
    // on where in the logic flow the Java callout policy is configured to run.
    msg.setHeader("JavaCallout-Sleep-Stamp", formattedStamp);

    // read a setting from the policy configuration file
    int settingValue = getSetting(msgCtxt);

    Instant end = Instant.now();
    String formattedEnd = DateTimeFormatter.ISO_INSTANT.format(end);
    // set variable and headers
    msgCtxt.setVariable("sleepCallout.setting", Integer.toString(settingValue));
    msg.setHeader("JavaCallout-setting", Integer.toString(settingValue));
    msg.setHeader("Content-Type", "application/json");

    // Get a contrived "response payload".
    String jsonResult = "{ \"status\" : \"OK\" }";

    // Set the content of the current message with that payload.
    // This will be the response.content if the Java callout is
    // configured on the Response flow.  It will be the request.content
    // if the policy is configured on the Request flow.
    msg.setContent(jsonResult);

    return ExecutionResult.SUCCESS;
  }
}
