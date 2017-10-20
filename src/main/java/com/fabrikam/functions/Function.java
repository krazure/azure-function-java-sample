package com.fabrikam.functions;

import com.microsoft.azure.serverless.functions.OutputBinding;
import com.microsoft.azure.serverless.functions.annotation.*;
import com.microsoft.azure.serverless.functions.ExecutionContext;

import java.util.logging.Logger;

/**
 * Hello function with HTTP Trigger.
 */
public class Function {

    @FunctionName("hello")
    public String hello(
            @HttpTrigger(name = "req", methods = {"get", "post"}, authLevel = AuthorizationLevel.ANONYMOUS)
                    String req,
            @QueueOutput(name = "messageToSend", queueName = "messages", connection = "message")
                    OutputBinding<String> messageToSend,
            @QueueOutput(name = "mailToSend", queueName = "mails", connection = "message")
                    OutputBinding<String> mailToSend,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("hello Function called.");

        messageToSend.setValue(req);
        mailToSend.setValue(req);

        return String.format("Request reserved - `%s`", req);
    }
}
