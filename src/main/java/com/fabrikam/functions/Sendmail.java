package com.fabrikam.functions;

import com.microsoft.azure.serverless.functions.ExecutionContext;
import com.microsoft.azure.serverless.functions.OutputBinding;
import com.microsoft.azure.serverless.functions.annotation.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Logger;

public class Sendmail {

    public static class MailSendResult {
        private String partitionKey = "mail";
        private String rowKey = UUID.randomUUID().toString();

        private String title;
        private String body;
        private String from;
        private String to;
        private boolean succeed;

        public String getTitle() { return title; }
        public void setTitle(String value) { title = value; }

        public String getBody() { return body; }
        public void setBody(String value) { body = value; }

        public String getFrom() { return from; }
        public void setFrom(String value) { from = value; }

        public String getTo() { return to; }
        public void setTo(String value) { to = value; }

        public boolean isSucceed() { return succeed; }
        public void setSucceed(boolean value) { succeed = value; }
    }

    @FunctionName("Sendmail")
    public void functionHandler(
            @QueueTrigger(name = "mailToSend", queueName = "mails", connection = "message")
                    String mailToSend,
            @TableOutput(name = "mailSendResult", tableName = "history", connection = "message")
                    OutputBinding<MailSendResult> mailSendResult,
            final ExecutionContext executionContext) {

        Logger logger = executionContext.getLogger();
        logger.info("Queue trigger input: " + mailToSend);
        MailSendResult sendResult = new MailSendResult();

        try {
            final String apiKey = "";

            final String title = "[Test] Test Mail";
            final String authorizationHeader = "Bearer " + apiKey;
            final String from = "";
            final String to = "";

            sendResult.setTitle(title);
            sendResult.setFrom(from);
            sendResult.setTo(to);
            sendResult.setBody(mailToSend);

            String requestBody = "{\n" +
                    "   \"personalizations\":[\n" +
                    "      {\n" +
                    "         \"to\":[\n" +
                    "            {\n" +
                    "               \"email\":\"" + to + "\"\n" +
                    "            }\n" +
                    "         ]\n" +
                    "      }\n" +
                    "   ],\n" +
                    "   \"from\":{\n" +
                    "      \"email\":\"" + from + "\"\n" +
                    "   },\n" +
                    "   \"subject\":\"" + title + "\",\n" +
                    "   \"content\":[\n" +
                    "      {\n" +
                    "         \"type\":\"text/plain\",\n" +
                    "         \"value\":\"" + mailToSend + "\"\n" +
                    "      }\n" +
                    "   ]\n" +
                    "}";
            byte[] requestBodyBlob = requestBody.getBytes(StandardCharsets.UTF_8);

            URL url = new URL("https://api.sendgrid.com/v3/mail/send");
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("POST");

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(false);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", authorizationHeader);

            try (OutputStream writer = connection.getOutputStream()) {
                writer.write(requestBodyBlob, 0, requestBodyBlob.length);
                writer.flush();
            }

            StringBuilder result = new StringBuilder();
            String buffer;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                while ((buffer = in.readLine()) != null) {
                    result.append(buffer);
                }
            }

            logger.info("Status Code: " + Integer.toString(connection.getResponseCode()));
            logger.info(result.toString());

            if (connection.getResponseCode() % 100 <= 2) {
                sendResult.setSucceed(true);
            } else {
                sendResult.setSucceed(false);
            }

            mailSendResult.setValue(sendResult);

        } catch (Exception ex) {
            logger.info(ex.toString());
            sendResult.setSucceed(false);
            mailSendResult.setValue(sendResult);
        }
    }
}
