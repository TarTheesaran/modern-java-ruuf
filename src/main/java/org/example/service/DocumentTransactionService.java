package org.example.service;

import com.google.gson.Gson;
import org.example.entity.*;
import org.example.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DocumentTransactionService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentTransactionService.class);
    public void uploadDocumentTransaction() {
        DocumentRepository documentRepository = new DocumentRepository();
        List<RequestCase> requestCases = documentRepository.getAllRequestCase().orElse(Collections.emptyList());

        Optional<UserLegacy> maybeUserLegacy = documentRepository.getUserLegacyById("complicate@sample.com");
        UserComplicate userComplicate;
        if (maybeUserLegacy.isPresent()) {
            UserLegacy userLegacy = maybeUserLegacy.get();
            UserSimple userSimple = new UserSimple(
                    userLegacy.name,
                    userLegacy.email,
                    userLegacy.age,
                    userLegacy.isDeveloper);
            userComplicate = new UserComplicate(
                    userSimple,
                    "f1",
                    "f2",
                    "f3",
                    "f4",
                    "f5");

        } else {
            UserSimple userSimple = new UserSimple(
                    "New",
                    "new@customer",
                    22,
                    false
            );
            userComplicate = new UserComplicate(
                    userSimple,
                    "ff1",
                    "ff2",
                    "ff3",
                    "ff4",
                    "ff5");
        }

        //cases.stream().filter()

        DocumentTransactionEntity documentTransactionEntity = new DocumentTransactionEntity();
        documentTransactionEntity.setCaseNo(userComplicate.userSimple.name + "-" + userComplicate.userSimple.email);
        documentTransactionEntity.setCreatedBy(userComplicate.userSimple.name);
        documentTransactionEntity.setTotalDocument(3);
        documentTransactionEntity.setCreatedDate(new Date());

        if (!requestCases.isEmpty()) {
            Optional<RequestCase> case005 = requestCases.stream()
                    .filter(requestCase -> requestCase.getCaseNo().equals("CASE005"))
                    .findFirst();
            case005.ifPresentOrElse(
                    c5 -> documentTransactionEntity.setDestinationCompanyCode(c5.getDestinationCompanyCode()),
                    () -> documentTransactionEntity.setDestinationCompanyCode("COMP001")
            );
        } else {
            documentTransactionEntity.setDestinationCompanyCode("COMP002");

        }
        documentTransactionEntity.setDocStatus("status1");
        documentTransactionEntity.setDocClass("CASE003");
        documentTransactionEntity.setHireeNo("hireNo");

        Gson gson = new Gson();
        String jsonStringOfUserComplicate = gson.toJson(userComplicate);
        String jsonStringOfDocumentTransactionEntity = gson.toJson(documentTransactionEntity);

        documentRepository.saveUserComplicate(userComplicate);
        documentRepository.saveDocumentTransactionEntity(documentTransactionEntity);

        try (FileWriter fileWriter = new FileWriter("user_complicate.json")) {
            fileWriter.write(jsonStringOfUserComplicate);
            logger.info("JSON string has been saved to user_data.json");
        } catch (IOException e) {
            logger.error("Error writing JSON to file: " + e.getMessage());
        }

        try (FileWriter fileWriter = new FileWriter("document_transaction.json")) {
            fileWriter.write(jsonStringOfDocumentTransactionEntity);
            logger.info("JSON string has been saved to user_data.json");
        } catch (IOException e) {
            logger.error("Error writing JSON to file: " + e.getMessage());
        }

        ArrayList<String> sourceFileList = new ArrayList<>();
        sourceFileList.add("user_complicate.json");
        sourceFileList.add("document_transaction.json");

        FtpService ftpService = new FtpService("localhost",
                2121,
                "one",
                "1234");

        // Specify the file to upload
        String localFile = "user_data.json"; // Path to local file
        LocalDateTime currentDateTime = LocalDateTime.now();
        // Define the formatter with the desired pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // Format the date-time into the desired string format
        String directoryName = currentDateTime.format(formatter);
        Optional<Boolean> directoryNotExist = ftpService.checkIfDirectoryIsAlreadyExist(directoryName);
        directoryNotExist.ifPresent(
                result -> ftpService.createDirectory(directoryName)
        );
        sourceFileList.forEach(fileName ->
                ftpService.uploadFile(directoryName, fileName)
                        .ifPresentOrElse(
                                result -> logger.info("Upload successfully"),
                                () -> logger.error("Upload failed for some reasons")
                        )
        );

        ftpService.terminateConnection();
    }
}
