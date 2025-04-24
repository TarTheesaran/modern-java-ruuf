package org.example.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.entity.DocumentTransactionEntity;
import org.example.entity.RequestCase;
import org.example.entity.UserComplicate;
import org.example.entity.UserLegacy;
import org.example.entity.UserSimple;
import redis.clients.jedis.UnifiedJedis;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class DocumentRepository {
    UnifiedJedis jedis;
    public DocumentRepository() {
        jedis = new UnifiedJedis("redis://localhost:6379");
        String documentTransactionEntity = "{"
                + "\"caseNo\":\"CASE12345\","
                + "\"docType\":\"DOC_TYPE_CODE\","
                + "\"docClass\":\"DOC_CLASS_CODE\","
                + "\"destinationCompanyCode\":\"COMP123\","
                + "\"destinationOfficeCode\":\"OFFICE456\","
                + "\"totalDocument\":1,"
                + "\"docStatus\":\"NEW_DOCUMENT_STATUS\","
                + "\"hireeNo\":\"HIREENO789\","
                + "\"createdDate\":\"2025-04-11T10:20:30\","
                + "\"createdBy\":\"admin_user\""
                + "};";
        jedis.set("CASE12345", documentTransactionEntity);

        List<RequestCase> cases = List.of(
                new RequestCase("CASE001", "COMP001"),
                new RequestCase("CASE002", "COMP002"),
                new RequestCase("CASE003", "COMP003"),
                new RequestCase("CASE004", "COMP004"),
                new RequestCase("CASE005", "COMP005")
        );

        Gson gson = new Gson();
        String casesJsonString = gson.toJson(cases);
        jedis.set("ALL.CASE", casesJsonString);
    }

    public Optional<List<RequestCase>> getAllRequestCase(){
        String requestCases = jedis.get("ALL.CASE");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<RequestCase>>() {}.getType();
        List<RequestCase> cases = gson.fromJson(requestCases, listType);
        return Optional.of(cases);
    }

    public Optional<Boolean> saveDocumentTransactionEntity(DocumentTransactionEntity documentTransactionEntity) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(documentTransactionEntity);
        jedis.set(documentTransactionEntity.getCaseNo(), jsonString);
        return Optional.of(true);
    }
}
