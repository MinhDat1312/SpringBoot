package vn.hoidanit.jobhunter.controller;

import java.util.regex.Pattern;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Permission;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.PermissionService;
import vn.hoidanit.jobhunter.util.exception.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class PermissionController {
    private PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/permissions")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody Permission permission)
            throws IdInvalidException {
        if (this.permissionService.existPermission(permission)) {
            throw new IdInvalidException("Permission exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.permissionService.handleCreatePermission(permission));
    }

    @PutMapping("/permissions")
    public ResponseEntity<Permission> updatePermission(@Valid @RequestBody Permission permission)
            throws IdInvalidException {
        if (this.permissionService.handleGetPermissionById(permission.getId()) == null) {
            throw new IdInvalidException("Permission doesn't exist");
        }

        if (this.permissionService.existPermission(permission)) {
            throw new IdInvalidException("Permission exists");
        }

        return ResponseEntity.ok().body(this.permissionService.handleUpdatePermission(permission));
    }

    @DeleteMapping("/permissions/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable("id") String id) throws IdInvalidException {
        if (Pattern.compile("^[0-9]+$").matcher(id).matches()) {
            if (this.permissionService.handleGetPermissionById(Long.parseLong(id)) != null) {
                this.permissionService.handleDeletePermssion(Long.parseLong(id));
                return ResponseEntity.ok().body(null);
            } else {
                throw new IdInvalidException("Permission doesn't exist");
            }
        } else {
            throw new IdInvalidException("Id is number");
        }
    }

    @GetMapping("/permissions")
    public ResponseEntity<ResultPaginationDTO> getAllPermissions(@Filter Specification<Permission> spe,
            Pageable pageable) {
        return ResponseEntity.ok().body(this.permissionService.handleGetAllPermissions(spe, pageable));
    }
}
