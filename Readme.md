# Java Callout 101

This directory contains the Java source code and pom.xml file required to
compile a simple Java callout for Apigee Edge. The callout is very simple: it sleeps, and returns success.

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
      <ClassName>com.dinochiesa.edgecallouts.Sleeper</ClassName>
      <ResourceURL>java://edge-java-callout-101.jar</ResourceURL>
    </JavaCallout>
   ```  

5. use the Edge UI, or a command-line tool like pushapi (See
   https://github.com/carloseberhardt/apiploy) or similar to
   import the proxy into an Edge organization, and then deploy the proxy . 
   Eg,    
   ```./pushapi -v -d -o ORGNAME -e test -n sleep-via-callout bundle```

6. Use a client to generate and send http requests to the proxy. Eg,   
   ```curl -i http://ORGNAME-test.apigee.net/sleep-via-callout/sleep```




## Dependencies

- Apigee Edge expressions v1.0
- Apigee Edge message-flow v1.0
- Apache commons lang 2.6
- FasterXML Jackson

These jars must be available on the classpath for the compile to
succeed. The build.sh script should download all of these files for
you, automatically. You could also create a Gradle or maven pom file as
well. 

If you want to download them manually: 

    The first 2 jars are available in Apigee Edge. The first two are
    produced by Apigee; contact Apigee support to obtain these jars to allow
    the compile, or get them here: 
    https://github.com/apigee/api-platform-samples/tree/master/doc-samples/java-cookbook/lib

    The Apache jar is available in Apigee Edge at runtime. To download it for compile time, you can get them from maven.org. 

    The Jackson jar is available from your favorite public maven repo. 



## Notes

There is one callout class, com.dinochiesa.edgecallouts.Sleeper ,
which sleeps. 


## Example Usage

```
$ curl -i https://ORGNAME-test.apigee.net/sleep-via-callout/sleep
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

{
  "category" : "INFO",
  "source" : "sleep",
  "status" : "OK",
  "mystamp" : "11/04/2015 01:13:46",
  "properties" : {
    "os.version" : "3.14.44-32.39.amzn1.x86_64",
    "java.runtime.name" : "OpenJDK Runtime Environment",
    "os.arch" : "amd64",
    "java.runtime.version" : "1.7.0_75-mockbuild_2015_01_23_00_20-b00",
    "java.vm.name" : "OpenJDK 64-Bit Server VM",
    "java.version" : "1.7.0_75",
    "os.name" : "Linux",
    "java.vendor" : "Oracle Corporation"
  }
}
```

## Bugs

There are no unit tests for this project.
