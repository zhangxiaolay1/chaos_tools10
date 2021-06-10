/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import java.io.*;

/**
 * This sample demonstrates how to make basic requests to Amazon S3 using
 * the AWS SDK for Java.
 */
public class S3Sample {

    private static final String AWS_ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
    private static final String AWS_SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";


    public static boolean createBucket(AmazonS3 s3, String bucketName) throws IOException {
        /*
         * Create a new S3 bucket - Amazon S3 bucket names are globally unique,
         * so once a bucket name has been taken by any user, you can't create
         * another bucket with that same name.
         *
         * You can optionally specify a location for your bucket if you want to
         * keep your data closer to your applications or users.
         */
        System.out.println("Creating bucket " + bucketName + "\n");
        s3.createBucket(bucketName);

        /*
         * List the buckets in your account
         */
        System.out.println("Listing buckets");
        for (Bucket bucket : s3.listBuckets()) {
            System.out.println(" - " + bucket.getName());
        }
        return true;
    }


    public static void main(String[] args) throws IOException {
        AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
/*        AmazonS3 s3 = new AmazonS3Client(credentials);
//        Region usWest2 = Region.getRegion("ghtest1");
//        s3.setRegion(usWest2);
        s3.setEndpoint("http://ozone.s3gtest.com:9878");
        s3.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));*/


        String bucketName = "/bucket2";
//        String key = UUID.randomUUID().toString();
        String key = "chinadaily.txt";


        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                "http://ozone.s3gtest.com:9878",
                "ghtest1"))
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withPathStyleAccessEnabled(true)
            .withClientConfiguration(new ClientConfiguration().withUseExpectContinue(false))
            .build();


        try {
            //createBucket(s3,bucketName);


//            System.out.println("Uploading a new object to S3 from a file\n");
//            s3.putObject(new PutObjectRequest(bucketName, key, createSampleFile()));


            System.out.println("Downloading an object");
            S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
            System.out.println("Content-Type: " + object.getObjectMetadata().getContentType());
            displayTextInputStream(object.getObjectContent());
            System.exit(111);
            /*
             * List objects in your bucket by prefix - There are many options for
             * listing the objects in your bucket.  Keep in mind that buckets with
             * many objects might truncate their results when listing their objects,
             * so be sure to check if the returned object listing is truncated, and
             * use the AmazonS3.listNextBatchOfObjects(...) operation to retrieve
             * additional results.
             */
            System.out.println("Listing objects");
            ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix("My"));
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")");
            }
            System.out.println();

            /*
             * Delete an object - Unless versioning has been turned on for your bucket,
             * there is no way to undelete an object, so use caution when deleting objects.
             */
            System.out.println("Deleting an object\n");
            s3.deleteObject(bucketName, key);

            /*
             * Delete a bucket - A bucket must be completely empty before it can be
             * deleted, so remember to delete any objects from your buckets before
             * you try to delete them.
             */
            System.out.println("Deleting bucket " + bucketName + "\n");
            s3.deleteBucket(bucketName);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with S3, "
                + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    /**
     * Creates a temporary file with text data to demonstrate uploading a file
     * to Amazon S3
     *
     * @return A newly created temporary file with text data.
     * @throws IOException
     */
    private static File createSampleFile() throws IOException {
        File file = File.createTempFile("aws-java-sdk-", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("01234567890112345678901234\n");
        writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
        writer.write("01234567890112345678901234\n");
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.close();

        return file;
    }

    /**
     * Displays the contents of the specified input stream as text.
     *
     * @param input The input stream to display as text.
     * @throws IOException
     */
    private static void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }

}