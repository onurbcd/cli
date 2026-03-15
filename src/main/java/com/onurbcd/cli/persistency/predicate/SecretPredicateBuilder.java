package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.persistency.entity.QSecret;
import org.apache.commons.lang3.StringUtils;

public class SecretPredicateBuilder extends BasePredicateBuilder {

    public SecretPredicateBuilder() {
        super(QSecret.secret.name, QSecret.secret.active);
    }

    @Override
    public SecretPredicateBuilder search(String search) {
        if (StringUtils.isNotBlank(search)) {
            builder().and(QSecret.secret.name.containsIgnoreCase(search)
                    .or(QSecret.secret.description.containsIgnoreCase(search))
                    .or(QSecret.secret.link.containsIgnoreCase(search))
                    .or(QSecret.secret.username.containsIgnoreCase(search)));
        }

        return this;
    }
}
