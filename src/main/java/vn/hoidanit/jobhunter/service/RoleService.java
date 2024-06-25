package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Permission;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.PermissionRepository;
import vn.hoidanit.jobhunter.repository.RoleRepository;

@Service
public class RoleService {
    private RoleRepository roleRepository;
    private PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public boolean existByName(Role role) {
        return this.roleRepository.existsByName(role.getName());
    }

    public Role handleGetRoleById(long id) {
        Optional<Role> optional = this.roleRepository.findById(id);

        return optional.isPresent() ? optional.get() : null;
    }

    public Role handleCreateRole(Role role) {
        if (role.getPermissions() != null) {
            List<Long> idPermissions = role.getPermissions().stream().map(p -> p.getId()).collect(Collectors.toList());
            List<Permission> permissions = this.permissionRepository.findByIdIn(idPermissions);

            role.setPermissions(permissions);
        }
        return this.roleRepository.save(role);
    }

    public Role handleUpdateRole(Role role) {
        Role currentRole = handleGetRoleById(role.getId());

        if (currentRole != null) {
            if (role.getPermissions() != null) {
                List<Long> idPermissions = role.getPermissions().stream().map(p -> p.getId())
                        .collect(Collectors.toList());
                List<Permission> permissions = this.permissionRepository.findByIdIn(idPermissions);

                role.setPermissions(permissions);
            }

            currentRole.setName(role.getName());
            currentRole.setActive(role.isActive());
            currentRole.setDescription(role.getDescription());
            currentRole.setPermissions(role.getPermissions());

            return this.roleRepository.save(currentRole);
        }

        return null;
    }

    public void handleDeleteRole(long id) {
        this.roleRepository.deleteById(id);
    }

    public ResultPaginationDTO handleGetAllRoles(Specification<Role> spe, Pageable pageable) {
        Page<Role> pages = this.roleRepository.findAll(spe, pageable);
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
