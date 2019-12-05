package com.sec.scloud.file;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.AccessCondition;
import java.io.InputStream;

public class BlobClient {
    private static final int SINGLE_BLOB_PUT_THRESHOLD_BYTES = 5 * 1024 * 1024;
    private static final int timeout = 60000;

    private CloudBlobContainer container;

    public BlobClient() {
        try {
            final CloudStorageAccount account = CloudStorageAccount.parse(Config.CONNECTION_STRING);
            final CloudBlobClient serviceClient = account.createCloudBlobClient();

            container = serviceClient.getContainerReference(Config.CONTAINER_NAME);
            container.createIfNotExists();
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void upload(final InputStream is, final String path, final long length, final String contentType) throws Exception {
        final BlobRequestOptions options = new BlobRequestOptions();
        options.setConcurrentRequestCount(1);
        options.setSingleBlobPutThresholdInBytes(SINGLE_BLOB_PUT_THRESHOLD_BYTES);
        options.setTimeoutIntervalInMs(timeout);
        options.setMaximumExecutionTimeInMs(Config.TIMEOUT_MAX);

        final CloudBlockBlob blob = container.getBlockBlobReference(path);
        // Adding AccessCondition to contain lease ID
        final AccessCondition blobAccessCondition = new AccessCondition();       

        while(!blob.exists()){

            System.out.println("\nBlob doesn't exists.");
            System.out.println("\nSetting up the content type and 0 byte on the blob.");
            
            try {
                blob.getProperties().setContentType(contentType);
                blob.upload(is, 0, null, options, null);
                System.out.println("\nAquiring a lease againt this blob and assign to blobAccessCondition");
                blobAccessCondition.setLeaseID(blob.acquireLease(15,null));
                System.out.println("\nWriting full content with lease ID aquired.");
                blob.upload(is, length, blobAccessCondition, options, null);
                System.out.println("\nRelease the Lease on the blob.");
                blob.releaseLease(blobAccessCondition);
            } catch (final Exception e) {
                e.printStackTrace();
                if(e.toString().contains("There is currently a lease on the blob and no lease ID was specified in the request") || 
                   e.toString().contains("There is already a lease present")){
                       System.out.println("This is expected exception. Will retake this loop.");
                       continue;
                } else {
                    System.out.println("None expected exception occurred. Throwing exception");
                    throw e;
                }                       
            }
            Thread.sleep(1000);
        }
        System.out.println("The Blob exists.");              
    }
}