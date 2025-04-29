package com.onurbcd.eruservice.annotation;

import com.onurbcd.eruservice.config.enums.Domain;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Service
@Qualifier
public @interface PrimeService {

    Domain value();
}
