package com.github.fielddb.datacollection;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MultipartPostRequest {
  private HttpsURLConnection httpConn;
  private DataOutputStream request;
  private final String boundary = "*****";
  private final String crlf = "\r\n";
  private final String twoHyphens = "--";

  /**
   * This constructor initializes a new HTTP POST request with content type
   * is set to multipart/form-data
   *
   * @param requestURL
   * @throws IOException
   */
  public MultipartPostRequest(String requestURL) throws IOException {

    // creates a unique boundary based on time stamp
    URL url = new URL(requestURL);
    httpConn = (HttpsURLConnection) url.openConnection();
    httpConn.setUseCaches(false);
    httpConn.setDoOutput(true); // indicates POST method
    httpConn.setDoInput(true);
    httpConn.setRequestMethod("POST");
    httpConn.setRequestProperty("Accept", "application/json");
    httpConn.setRequestProperty("Connection", "Keep-Alive");
    httpConn.setRequestProperty("Cache-Control", "no-cache");
    httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + this.boundary);
    request = new DataOutputStream(httpConn.getOutputStream());
  }

  /**
   * Adds a form field to the request
   *
   * @param name  field name
   * @param value field value
   */
  public void addFormField(String name, String value) throws IOException {
    request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
    request.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + this.crlf);
    request.writeBytes("Content-Type: text/plain; charset=UTF-8" + this.crlf);
    request.writeBytes(this.crlf);
    request.writeBytes(value + this.crlf);
    request.flush();
  }

  /**
   * Adds a upload file section to the request
   *
   * @param fieldName  name attribute in &lt; input type="file" name="..." /&gt;
   * @param uploadFile a File to be uploaded
   * @throws IOException
   */
  public void addFilePart(String fieldName, File uploadFile)
      throws IOException {
    String fileName = uploadFile.getName();
    request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
    request.writeBytes("Content-Disposition: form-data; name=\"" +
        fieldName + "\";filename=\"" +
        fileName + "\"" + this.crlf);
    request.writeBytes(this.crlf);
    byte[] bytes = readFile(uploadFile);
    request.write(bytes);
    request.writeBytes(this.crlf);
  }

  /**
   * Completes the request and receives response from the server.
   *
   * @return a list of Strings as response in case the server returned
   * status OK, otherwise an exception is thrown.
   * @throws IOException
   */
  public String execute() throws IOException {
    String response = "";
    request.writeBytes(this.crlf);
    request.writeBytes(this.twoHyphens + this.boundary +
        this.twoHyphens + this.crlf);
    request.flush();
    request.close();
    // Cant read data from 400+ status streams so
    // checks server's status code first
    // https://stackoverflow.com/questions/9365829/filenotfoundexception-for-httpurlconnection-in-ice-cream-sandwich
    int status = httpConn.getResponseCode();
    InputStream responseStream;
    if (status == HttpsURLConnection.HTTP_OK) {
      responseStream = new BufferedInputStream(httpConn.getInputStream());
    } else {
      responseStream = new BufferedInputStream(httpConn.getErrorStream());
    }
    BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
    String line = "";
    StringBuilder stringBuilder = new StringBuilder();
    while ((line = responseStreamReader.readLine()) != null) {
      stringBuilder.append(line).append("\n");
    }
    responseStreamReader.close();
    response = stringBuilder.toString();
    httpConn.disconnect();
    return response;
  }

  /**
   * readFile into bytes without using
   *
   * byte[] java.nio.file.Files.readAllBytes(Path path)
   *
   * which errors in test suite (works fine in runtime on android)
   *
   * @param file
   * @return
   * @throws IOException
   */
  public static byte[] readFile(File file) throws IOException {
    // Open file in read mode
    RandomAccessFile f = new RandomAccessFile(file, "r");
    try {
      // Get and check length
      long longlength = f.length();
      int length = (int) longlength;
      if (length != longlength)
        throw new IOException("File size >= 2 GB");
      // Read file and return data
      byte[] data = new byte[length];
      f.readFully(data);
      return data;
    } finally {
      f.close();
    }
  }

  public static boolean checkAndRequestPermissions(Activity activity, int REQUEST_ID_MULTIPLE_PERMISSIONS) {
    List<String> listPermissionsNeeded = new ArrayList<>();

    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.INTERNET);
    }
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
    }
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
    }
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    if (!listPermissionsNeeded.isEmpty()) {
      ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
      return false;
    }
    return true;
  }
}
