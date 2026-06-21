package com.geosentinel.auth.exception;
import lombok.*; import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*; import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError; import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.stream.Collectors;
@Slf4j @RestControllerAdvice public class GlobalExceptionHandler {
    @Data @Builder static class E { int status; String error,message; Instant ts; }
    @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<E> bad(IllegalArgumentException e) { return err(HttpStatus.BAD_REQUEST,e.getMessage()); }
    @ExceptionHandler(BadCredentialsException.class)  public ResponseEntity<E> cred(BadCredentialsException e) { return err(HttpStatus.UNAUTHORIZED,"Invalid credentials"); }
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<E> val(MethodArgumentNotValidException e) {
        return err(HttpStatus.BAD_REQUEST, e.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", ")));
    }
    @ExceptionHandler(Exception.class) public ResponseEntity<E> all(Exception e) { log.error("Unhandled",e); return err(HttpStatus.INTERNAL_SERVER_ERROR,"Internal error"); }
    private ResponseEntity<E> err(HttpStatus s,String m) { return ResponseEntity.status(s).body(E.builder().status(s.value()).error(s.getReasonPhrase()).message(m).ts(Instant.now()).build()); }
}
