package vn.hoidanit.jobhunter.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import vn.hoidanit.jobhunter.domain.RestResponse;

@ControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(value = IdInvalidException.class)
    ResponseEntity<RestResponse<Object>> idResponseEntity(IdInvalidException e) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage("IdInvalidException");
        res.setError(e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
