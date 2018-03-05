package timecrypt.server.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is a temporary class for benchmarking purposes.
 * Keys must be stored in a configuration file instead.
 */
public class S3 implements Storage {

    private AmazonS3 client;
    public String bucket;

    public S3(String bucket, String[] args) {
        this.bucket = bucket;
        BasicAWSCredentials awsCreds = null;
        try {
            awsCreds = new BasicAWSCredentials(args[0], args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("AWS credentials were not provided.");
        }

        client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion("eu-west-2") // can be used as an argument
                .build();
        createBucket();
    }

    public void createBucket() {
        client.createBucket(new CreateBucketRequest(bucket));
    }

    //@Override
    public boolean store(String streamID, String key, byte[] data) {
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentLength(data.length);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

        try {
            client.putObject(new PutObjectRequest(bucket, key, byteArrayInputStream, metaData));
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public byte[] get(String streamID, String key) throws IOException {
        S3Object object = client.getObject(new GetObjectRequest(bucket, key));
        InputStream objectData = object.getObjectContent();

        return processInputStream(objectData);
    }

    private byte[] processInputStream(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();

        return buffer.toByteArray();
    }
}