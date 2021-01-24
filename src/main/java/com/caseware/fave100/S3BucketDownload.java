package com.caseware.fave100;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class S3BucketDownload {

    private static final Logger logger = LoggerFactory.getLogger(S3BucketDownload.class);

    public static final String FOLDER_PATH = "lucene-index/";
    private static final String BUCKET_NAME = "fave100search";

    //Lame check -- will restructure this class in the future
    private static boolean _downloading;

    public S3BucketDownload() {
        if (_downloading)
            return;

        logger.info("Starting S3 download");
        _downloading = true;

        _deleteFolderIfExists();

        AmazonS3 conn = AmazonS3Client.builder().build();

        for (S3ObjectSummary obj : conn.listObjects(BUCKET_NAME).getObjectSummaries()) {
            logger.info("Downloading " + obj.getKey() + "\t" + obj.getSize() + "\t" + StringUtils.fromDate(obj.getLastModified()));
            conn.getObject(new GetObjectRequest(BUCKET_NAME, obj.getKey()), new File(FOLDER_PATH + obj.getKey()));
        }

        _downloading = false;
        logger.info("Finished S3 download");
    }

    private void _deleteFolderIfExists() {
        try {
            File dir = new File(FOLDER_PATH);
            if (dir != null && dir.isDirectory())
                FileUtils.forceDelete(dir);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

}