package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.MatchType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.Type;
import org.springframework.core.convert.converter.Converter;

public class StringToMatchTypeConverter implements Converter<String, MatchType> {

    @Override
    public MatchType convert(String type) {
        if (type.contains("=")) {
            String[] matchType = type.split("=");
            return new MatchType(Type.valueOf(matchType[0].toUpperCase()), matchType[1]);
        }
        return new MatchType(Type.valueOf(type.toUpperCase()), null);
    }
}
