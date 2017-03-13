package org.kontinuity.catapult.web.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Helper class that helps us with file uploads.
 */
class FileUploadHelper {

    /**
     * Unzip a zip file into a temporary location
     *
     * @param zipFile the zip file to be unzipped
     * @throws IOException when we could not read the file
     */
    static void unzip(File zipFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File file = new File(zipFile.getParent(), fileName);
                if (!zipEntry.isDirectory()) {
                    IOUtils.copy(zis, new FileOutputStream(file));
                } else {
                    FileUtils.forceMkdir(file);
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }

    /**
     * Get the file name from the `Content-Disposition` in the request headers of a form-data request
     *
     * @param headers to find the file name in.
     * @return the name of the file or `unknown` if not found
     */
    static String getFileName(MultivaluedMap<String, String> headers) {
        String[] contentDisposition = headers.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                return sanitizeFilename(name[1]);
            }
        }
        return "unknown";
    }

    private static String sanitizeFilename(String s) {
        return s.trim().replaceAll("\"", "");
    }

}
