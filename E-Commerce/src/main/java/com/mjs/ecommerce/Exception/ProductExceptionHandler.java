package com.mjs.ecommerce.Exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ProductExceptionHandler{
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String,String> ValidationException(MethodArgumentNotValidException me ){
        Map<String,String> m=new HashMap<>();
        me.getBindingResult().getFieldErrors().forEach(f->m.put(f.getField(),f.getDefaultMessage()));
        return m;
    }
}
