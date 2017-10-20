package com.fabrikam.functions;

import com.microsoft.azure.serverless.functions.ExecutionContext;
import com.microsoft.azure.serverless.functions.OutputBinding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

/**
 * Unit test for Function class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FunctionTest {

    static class ExecutionContextMock implements ExecutionContext {

        @InjectMocks
        Logger logger;

        @Override
        public Logger getLogger() {
            return logger;
        }

        @Override
        public String getInvocationId() {
            return "invocation_id";
        }
    }

    @InjectMocks
    private ExecutionContextMock executionContext;

    @Mock
    private Logger logger;

    @Mock
    private OutputBinding<String> stringOutputBinding;

    @Mock
    private OutputBinding<Sendmail.MailSendResult> mailSendResultOutputBinding;

    @Mock
    private OutputBinding<Sendsms.SmsSendResult> smsSendResultOutputBinding;

    @Before
    public void initTest() {
        doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            System.out.println("Called with arguments: " + Arrays.toString(args));
            return null;
        }).when(logger).info(anyString());
    }

    /**
     * Unit test for hello method.
     */
    @Test
    public void testHello() throws Exception {
        final Function function = new Function();

        final String ret = function.hello("function", stringOutputBinding, stringOutputBinding, executionContext);

        assertEquals("Request reserved - `function`", ret);
    }

    @Test
    public void testSendmail() throws Exception {
        final Sendmail sendmail = new Sendmail();

        sendmail.functionHandler("Hello, World with E-MAIL", mailSendResultOutputBinding, executionContext);
    }

    @Test
    public void testSendsms() throws Exception {
        final Sendsms sendsms = new Sendsms();

        sendsms.functionHandler("Hello, World! with SMS", smsSendResultOutputBinding, executionContext);
    }
}
