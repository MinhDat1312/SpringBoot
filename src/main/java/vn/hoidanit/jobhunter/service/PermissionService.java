package vn.hoidanit.jobhunter.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Permission;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.PermissionRepository;

@Service
public class PermissionService {
    private PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean existPermission(Permission permission) {
        return this.permissionRepository.existsByModuleAndApiPathAndMethod(permission.getModule(),
                permission.getApiPath(), permission.getMethod());
    }

    public Permission handleCreatePermission(Permission permission) {
        return this.permissionRepository.save(permission);
    }

    public Permission handleGetPermissionById(long id) {
        Optional<Permission> optional = this.permissionRepository.findById(id);

        return optional.isPresent() ? optional.get() : null;
    }

    public Permission handleUpdatePermission(Permission permission) {
        Permission currentPermission = handleGetPermissionById(permission.getId());

        if (currentPermission != null) {
            currentPermission.setName(permission.getName());
            currentPermission.setModule(permission.getModule());
            currentPermission.setApiPath(permission.getApiPath());
            currentPermission.setMethod(permission.getMethod());

            return this.permissionRepository.save(currentPermission);
        }
        return null;
    }

    public void handleDeletePermssion(long id) {
        Permission currentPermission = handleGetPermissionById(id);

        if (currentPermission != null) {
            currentPermission.getRoles().forEach(role -> role.getPermissions().remove(currentPermission));
            this.permissionRepository.delete(currentPermission);
        }
    }

    public ResultPaginationDTO handleGetAllPermissions(Specification<Permission> spe, Pageable pageable) {
        Page<Permission> pages = this.permissionRepository.findAll(spe, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pages.getTotalPages());
        meta.setTotal(pages.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(pages.getContent());

        return resultPaginationDTO;
    }
}
