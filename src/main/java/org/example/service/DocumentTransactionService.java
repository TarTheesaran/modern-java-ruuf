package org.example.service;

import com.google.gson.Gson;
import org.example.entity.*;
import org.example.repository.DocumentRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DocumentTransactionService {
    private static final String USER_ID = "complicate@sample.com";
    private static final String USER_COMPLICATE_FILE_NAME = "user_complicate.json";
    private static final String DOCUMENT_TRANSACTION_FILE_NAME = "document_transaction.json";

    private static final Logger logger = LoggerFactory.getLogger(DocumentTransactionService.class);

    public void uploadDocumentTransaction() {
        DocumentRepository documentRepository = new DocumentRepository();
        UserRepository userRepository = new UserRepository();
        List<RequestCase> requestCases = documentRepository.getAllRequestCase().orElse(Collections.emptyList());
        Optional<UserLegacy> maybeUserLegacy = userRepository.getUserLegacyById(USER_ID);

        UserComplicate userComplicate = getUserComplicate(maybeUserLegacy);
        DocumentTransactionEntity documentTransactionEntity = createDocumentTransaction(userComplicate, requestCases);

        userRepository.saveUserComplicate(userComplicate);
        documentRepository.saveDocumentTransactionEntity(documentTransactionEntity);

        Gson gson = new Gson();
        Map<String, String> sourceFileList = Map.of(
                USER_COMPLICATE_FILE_NAME, gson.toJson(userComplicate),
                DOCUMENT_TRANSACTION_FILE_NAME, gson.toJson(documentTransactionEntity)
        );

        sourceFileList.forEach((fileName, jsonString) -> writeFileAsJson(fileName, jsonString));

        uploadFilesToFtp(new ArrayList<>(sourceFileList.keySet().stream().toList()));
    }

    private static UserComplicate getUserComplicate(Optional<UserLegacy> maybeUserLegacy) {
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
        return userComplicate;
    }

    private static DocumentTransactionEntity createDocumentTransaction(UserComplicate userComplicate, List<RequestCase> requestCases) {
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
        return documentTransactionEntity;
    }

    private static void writeFileAsJson(String fileName, String jsonString) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(jsonString);
            logger.info("JSON string has been saved to user_data.json");
        } catch (IOException e) {
            logger.error("Error writing JSON to file: " + e.getMessage());
        }
    }

    private static void uploadFilesToFtp(ArrayList<String> fileNameList) {
        FtpService ftpService = new FtpService("localhost",
                2121,
                "one",
                "1234");

        String directoryName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        ftpService.checkIfDirectoryIsAlreadyExist(directoryName)
                .ifPresent(result -> ftpService.createDirectory(directoryName));

        fileNameList.forEach(fileName ->
                ftpService.uploadFile(directoryName, fileName)
                        .ifPresentOrElse(
                                result -> logger.info("Upload successfully"),
                                () -> logger.error("Upload failed for some reasons")
                        )
        );

        ftpService.terminateConnection();
    }
}
