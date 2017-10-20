package com.fabrikam.functions;

import com.microsoft.azure.serverless.functions.ExecutionContext;
import com.microsoft.azure.serverless.functions.OutputBinding;
import com.microsoft.azure.serverless.functions.annotation.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

public class Sendsms {

    public static class SmsSendResult {
        private String partitionKey = "sms";
        private String rowKey = UUID.randomUUID().toString();

        private String body;
        private String from;
        private String to;
        private boolean succeed;

        public String getBody() { return body; }
        public void setBody(String value) { body = value; }

        public String getFrom() { return from; }
        public void setFrom(String value) { from = value; }

        public String getTo() { return to; }
        public void setTo(String value) { to = value; }

        public boolean isSucceed() { return succeed; }
        public void setSucceed(boolean value) { succeed = value; }
    }

    @FunctionName("Sendsms")
    public void functionHandler(
            @QueueTrigger(name = "messageToSend", queueName = "messages", connection = "message")
                    String messageToSend,
            @TableOutput(name = "smsSendResult", tableName = "history", connection = "message")
                    OutputBinding<SmsSendResult> smsSendResult,
            final ExecutionContext executionContext) {

        Logger logger = executionContext.getLogger();
        logger.info("Queue trigger input: " + messageToSend);
        SmsSendResult sendResult = new SmsSendResult();

        try {
            final String appSid = "";
            final String appSecret = "";
            final String digest = appSid + ":" + appSecret;
            final String targetUrl = "https://api.twilio.com/2010-04-01/Accounts/" + appSid + "/Messages.json";
            final String from = "";
            final String to = "";

            sendResult.setFrom(from);
            sendResult.setTo(to);
            sendResult.setBody(messageToSend);

            final String authorizationHeader = "Basic " + new String(Base64.getEncoder().encode(digest.getBytes()));

            String requestBody = "To=" + URLEncoder.encode(to, "utf-8") +
                    "&From=" + URLEncoder.encode(from, "utf-8") +
                    "&Body=" + URLEncoder.encode(messageToSend, "utf-8");
            byte[] requestBodyBlob = requestBody.getBytes(StandardCharsets.UTF_8);

            URL url = new URL(targetUrl);
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("POST");

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(false);

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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

            smsSendResult.setValue(sendResult);

        } catch (Exception ex) {
            logger.info(ex.toString());
            sendResult.setSucceed(false);
            smsSendResult.setValue(sendResult);
        }
    }
}
