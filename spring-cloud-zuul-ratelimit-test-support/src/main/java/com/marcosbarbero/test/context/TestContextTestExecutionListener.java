package com.marcosbarbero.test.context;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * {@link org.springframework.test.context.TestExecutionListener} which populates the {@link TestContextHolder} with the current
 * {@link TestContext}.
 *
 * @author Eric Deandrea December 2018
 */
public class TestContextTestExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestMethod(TestContext testContext) {
        TestContextHolder.setContext(testContext);
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        TestContextHolder.clearContext();
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
