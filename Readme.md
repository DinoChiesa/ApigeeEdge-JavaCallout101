# Java Callout 101

This directory contains the Java source code and pom.xml file required to
compile a simple Java callout for Apigee Edge. The callout is very simple: it
reads and sets context variables, and the message payload, and returns success.

## Building:

1. unpack (if you can read this, you've already done that).

2. configure the build on your machine by loading the Apigee jars into your local cache
  ```./buildsetup.sh```

2. Build with maven.
  ```mvn clean package```

3. if you edit proxy bundles offline, copy the resulting jar file, available in  target/httpsig-edge-callout.jar to your apiproxy/resources/java directory.  If you don't edit proxy bundles offline, upload the jar file into the API Proxy via the Edge API Proxy Editor .

4. include an XML file for the Java callout policy in your
   apiproxy/resources/policies directory. It should look
   like this:
   ```xml
    <JavaCallout name='Java-Simple-101'>
      <Properties>
        <Property name='sleeptime'>6</Property> <!-- in seconds -->
      </Properties>
      <ClassName>com.dinochiesa.edgecallouts.ExampleCallout</ClassName>
      <ResourceURL>java://edge-java-callout-101-20200131.jar</ResourceURL>
    </JavaCallout>
   ```

5. use the Edge UI, or a tool like [importAndDeploy.js](https://github.com/DinoChiesa/apigee-edge-js-examples/blob/master/importAndDeploy.js) or similar to
   import the proxy into an Edge organization, and then deploy the proxy .
   Eg,
   ```node ./importAndDeploy -v -u myemail@example.org -o $ORG -e $ENV  -d bundle```

6. Use a client to generate and send http requests to the proxy. Eg,
   ```curl -i http://ORGNAME-test.apigee.net/example-callout/example```




## Dependencies

- Apigee Edge expressions v1.0
- Apigee Edge message-flow v1.0


If you want to download them manually:

* The 2 jars are available in Apigee Edge. The first two are
  produced by Apigee; contact Apigee support to obtain these jars to allow
  the compile, or get them here:
  https://github.com/apigee/api-platform-samples/tree/master/doc-samples/java-cookbook/lib

## Notes

There is one callout class, com.dinochiesa.edgecallouts.ExampleCallout ,
which reads and writes context variables, and sets the header and content .


## Example Usage

```
$ curl -i https://ORGNAME-test.apigee.net/example-callout/example
HTTP/1.1 200 OK
Date: Wed, 04 Nov 2015 01:13:46 GMT
Content-Type: application/json
Content-Length: 478
Connection: keep-alive
JavaCallout-Sleep-Start: 11/04/2015 01:13:40
JavaCallout-Sleep-End: 11/04/2015 01:13:46
X-time-target-elapsed: 0.0
X-time-total-elapsed: 6138.0
Server: Apigee Router

{  "status" : "OK" }
```

## Bugs

There are no unit tests for this project.
