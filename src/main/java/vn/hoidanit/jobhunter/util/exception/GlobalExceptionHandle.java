package vn.hoidanit.jobhunter.util.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import vn.hoidanit.jobhunter.domain.response.RestResponse;

@ControllerAdvice
public class GlobalExceptionHandle {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleAllException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage(ex.getMessage());
        res.setError("Internal Server Error");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(value = {
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            IdInvalidException.class })
    ResponseEntity<RestResponse<Object>> exceptionResponseEntity(Exception e) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(e.getMessage());
        res.setError("Exception occurs");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    ResponseEntity<RestResponse<Object>> notFoundException(Exception e) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setMessage(e.getMessage());
        res.setError("404 Not Found. URL may not exist");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        final List<FieldError> list = result.getFieldErrors();
        List<String> messages = list.stream().map(l -> l.getDefaultMessage()).collect(Collectors.toList());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(e.getBody().getDetail());
        res.setMessage(messages.size() > 1 ? messages : messages.get(0));

        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(StorageException.class)
    ResponseEntity<RestResponse<Object>> uploadFileError(StorageException e) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Exception upload file");
        res.setMessage(e.getMessage());

        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(PermissionException.class)
    ResponseEntity<RestResponse<Object>> handlePermissionException(PermissionException e) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError("Forbidden");
        res.setMessage(e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }
}
