package com.marcosbarbero.test.context;

import org.springframework.lang.Nullable;
import org.springframework.test.context.TestContext;
import org.springframework.util.Assert;

/**
 * Associates a given {@link TestContext} with the current execution thread.
 *
 * @author Eric Deandrea December 2018
 */
public final class TestContextHolder {
    private static final ThreadLocal<TestContext> TEST_CONTEXT_HOLDER = new ThreadLocal<>();

    private TestContextHolder() {
        super();
    }

    /**
     * Clears the context
     */
    public static void clearContext() {
        TEST_CONTEXT_HOLDER.remove();
    }

    /**
     * Gets the context
     * @return The {@link TestContext}
     */
    @Nullable
    public static TestContext getContext() {
        return TEST_CONTEXT_HOLDER.get();
    }

    /**
     * Sets the {@link TestContext}
     * @param context The {@link TestContext}
     */
    public static void setContext(TestContext context) {
        Assert.notNull(context, "context can not be null");
        TEST_CONTEXT_HOLDER.set(context);
    }
}
