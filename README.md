## java concurrent uploader
To compile 
```sh 
mvn compile
```
To run 
```sh
sh ./run.sh
```
Actual command in run.sh.
```sh
mvn exec:java -Dexec.mainClass="com.sec.scloud.file.App" -e
``` 
To run from jar file, execute the following command. 
```sh
java -jar target/blob-problem-0.1-jar-with-dependencies.jar
```
## What has been changed from the original file.

1. upload() method for BlobClient class has been modified to aquire lease for the blob.
Some exceptions are given since the there will be multiple threads trying to write on the same blob.
Only one of these thread will be successful writing the whole block, and others will loop again.
2. UPLOAD_BYTES and NUM_OF_THREAD variables in Config.java has been increased.
3. CONNECTION_STRING variable in Config.java is reading from system variable. 

## Code highlight
```sh
blob.getProperties().setContentType(contentType);
blob.upload(is, 0, null, options, null);
System.out.println("\nAquiring a lease againt this blob and assign to blobAccessCondition");
blobAccessCondition.setLeaseID(blob.acquireLease(15,null));
System.out.println("\nWriting full content with lease ID aquired.");
blob.upload(is, length, blobAccessCondition, options, null);
System.out.println("\nRelease the Lease on the blob.");
blob.releaseLease(blobAccessCondition);
```
