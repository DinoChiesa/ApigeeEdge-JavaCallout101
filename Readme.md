# Java Callout 101

This directory contains the Java source code and pom.xml file required to
compile a simple Java callout for Apigee. The callout is very simple: it
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
        <Property name='integer-setting'>6</Property>
      </Properties>
      <ClassName>com.google.apigee.callouts.ExampleCallout</ClassName>
      <ResourceURL>java://apigee-java-callout-101-20230522.jar</ResourceURL>
    </JavaCallout>
   ```

5. use the Google Cloud Console UI, or a tool like [importAndDeploy.js](https://github.com/DinoChiesa/apigee-edge-js-examples/blob/master/importAndDeploy.js) or similar to
   import the proxy into an Edge organization, and then deploy the proxy .
   Eg,
   ```
   TOKEN=`gcloud auth print-access-token`
   node ./importAndDeploy -v --token $TOKEN --apigeex -o $ORG -e $ENV  -d bundle
   ```

6. Use a client to generate and send http requests to the proxy. Eg,
   ```
   curl -i https://my-endpoint.net/callout-101/example1
   curl -i https://my-endpoint.net/callout-101/example2
   curl -i https://my-endpoint.net/callout-101/example3
   ```




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
$ curl -i https://my-apigee-endpoint.com/callout-101/example3

HTTP/2 200
javacallout-stamp: 2023-05-22T22:32:33.983307Z
content-type: text/plain
x-time-target-elapsed: 0.0
x-time-total-elapsed: 3.0
x-request-id: 21a87ac8-aa2b-4870-9acd-e53991ab9407
content-length: 283
date: Mon, 22 May 2023 22:32:33 GMT
via: 1.1 google
alt-svc: h3=":443"; ma=2592000,h3-29=":443"; ma=2592000

status: OK
int-setting: 42
string-setting:

  Whitespace in the following XML element will be significant;
  it appears in the property value that is retrieved at runtime.

  proxy: example-callout r6
  current conditional flow name: flow3


--end--
```

## Bugs

There are no unit tests for this project.
