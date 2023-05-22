// ExampleCallout.java
//
// This is the source code for an example Java callout for Apigee.
// This callout is very simple - it retrieves a setting, sets
// a variable and a header, and then returns SUCCESS.
//
// ------------------------------------------------------------------

package com.google.apigee.callouts;

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
  private static final int INT_SETTING_DEFAULT = 42;
  private static final String STRING_SETTING_DEFAULT = "Not Set";

  private Map properties; // read-only

  public ExampleCallout(Map properties) {
    this.properties = properties;
  }

  private int getIntegerSetting(MessageContext msgCtxt) throws IllegalStateException {
    // This shows how to retrieve a setting from a property
    // in the JavaCallout policy configuration.
    // In this case the setting is parsed to an integer.
    String value = (String) this.properties.get("integer-setting");
    if (value == null || value.trim().equals("")) {
      return INT_SETTING_DEFAULT;
    }
    value = resolveVariableReferences(value, msgCtxt);
    if (value == null || value.equals("")) {
      // Could also return default here, but the thinking is,
      // if someone provided a string and it resolves to empty, that's a bad thing.
      throw new IllegalStateException("value resolves to null or empty.");
    }
    int actualValue = INT_SETTING_DEFAULT;
    try {
      actualValue = Integer.parseInt(value);
    } catch (java.lang.Exception exc1) {
      actualValue = INT_SETTING_DEFAULT;
      // could also throw here, if that is desired.
      msgCtxt.getMessage().setHeader("JavaCallout-parse-exception", exc1.toString());
    }

    return actualValue;
  }

  private String getStringSetting(MessageContext msgCtxt) throws IllegalStateException {
    // Retrieve a value from a named property, as a string.
    String value = (String) this.properties.get("string-setting");
    if (value == null || value.trim().equals("")) {
      return STRING_SETTING_DEFAULT;
    }
    value = resolveVariableReferences(value, msgCtxt);
    if (value == null || value.equals("")) {
      throw new IllegalStateException("value resolves to null or empty.");
    }
    return value;
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
    msgCtxt.setVariable("exampleCallout.stamp", formattedStamp);
    msg.setHeader("JavaCallout-Stamp", formattedStamp);

    // read a few settings from the policy configuration file
    int intSettingValue = getIntegerSetting(msgCtxt);
    String stringSettingValue = getStringSetting(msgCtxt);

    Instant end = Instant.now();
    String formattedEnd = DateTimeFormatter.ISO_INSTANT.format(end);
    // set variable and headers
    msgCtxt.setVariable("exampleCallout.int-setting", Integer.toString(intSettingValue));
    msg.setHeader("Content-Type", "text/plain");

    // Get a contrived "response payload".
    String result = "status: OK\n"
      + String.format("int-setting: %d\n", intSettingValue)
      + String.format("string-setting:\n%s\n", stringSettingValue)
      + "\n--end--\n";

    // Set the content of the current message with that payload.
    // This will be the response.content if the Java callout is
    // configured on the Response flow.  It will be the request.content
    // if the policy is configured on the Request flow.
    msg.setContent(result);

    return ExecutionResult.SUCCESS;
  }
}
