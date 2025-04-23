package org.example.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FtpService {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 2121;
    private static final String USERNAME = "one";
    private static final String PASSWORD = "1234";

    private static final Logger logger = LoggerFactory.getLogger(FtpService.class);
    private final FTPClient ftpClient;

    public FtpService() {
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(SERVER_ADDRESS, PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FtpService(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public Optional<ArrayList<String>> searchAndDeleteFiles(String keyword) {
        return searchFiles(keyword).flatMap(this::deleteTargetFiles);
    }

    public Optional<Boolean> createDirectory(String directoryName){
        try {
            ftpClient.makeDirectory(directoryName);
            return Optional.of(true);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<Boolean> checkIfDirectoryIsAlreadyExist(String directoryName){
        try {
            boolean directoryAlreadyExist = ftpClient.changeWorkingDirectory(directoryName);
            if(!directoryAlreadyExist) return Optional.of(true);
            return Optional.of(false);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<ArrayList<String>> deleteTargetFiles(ArrayList<String> targets) {
        List<String> deletedFiles = targets.stream()
                .filter(filename -> {
                    try {
                        return ftpClient.deleteFile(filename);
                    } catch (IOException e) {
                        return false;
                    }
                })
                .toList();
        return deletedFiles.isEmpty() ? Optional.empty() : Optional.of(new ArrayList<>(deletedFiles));
    }

    public Optional<ArrayList<String>> searchFiles(String keyword) {
        return listFiles()
                .map(files -> new ArrayList<>(Arrays.stream(files)
                        .map(FTPFile::getName)
                        .filter(name -> name.contains(keyword))
                        .toList())); // Stream and filter file names, then collect into ArrayList
    }

    public Optional<FTPFile[]> listFiles() {
        try {
            FTPFile[] listFiles = this.ftpClient.listFiles();
            return (listFiles.length != 0) ? Optional.of(listFiles) : Optional.empty();
        } catch (IOException e) {
            logger.error("Error fetching file list: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Boolean> uploadFile(String directory, String localFile) {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String remoteFile = "remote_file_"+localFile.replace(".json", "")+"_"+ timeStamp + ".txt";
        try {
            if (!Objects.equals(directory, "")) ftpClient.changeWorkingDirectory(directory);
            //System.out.println(directory);
        }catch(IOException e) {
            return Optional.empty();
        }
        try (FileInputStream inputStream = new FileInputStream(localFile)) {
            return this.ftpClient.storeFile(remoteFile, inputStream)
                    ? Optional.of(true)
                    : Optional.empty();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }

    public void terminateConnection(){
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
