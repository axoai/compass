package org.zalando.compass.resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.compass.domain.logic.BadArgumentException;
import org.zalando.compass.domain.persistence.NotFoundException;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.SpringAdviceTrait;

@ControllerAdvice
class ExceptionHandling implements ProblemHandling, SpringAdviceTrait {

    @ExceptionHandler
    public ResponseEntity<Problem> handleBadArgument(
            final BadArgumentException exception,
            final NativeWebRequest request) throws HttpMediaTypeNotAcceptableException {

        return create(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleNotFoundException(
            final NotFoundException exception,
            final NativeWebRequest request) throws HttpMediaTypeNotAcceptableException {

        return create(HttpStatus.NOT_FOUND, exception, request);
    }

}
