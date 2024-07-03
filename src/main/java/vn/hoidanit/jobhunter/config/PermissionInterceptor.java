package vn.hoidanit.jobhunter.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.hoidanit.jobhunter.domain.Permission;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.exception.IdInvalidException;

@Transactional
public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        if (email != null && !email.isEmpty()) {
            User currentUser = this.userService.handleGetUserByEmail(email);
            if (currentUser != null) {
                Role role = currentUser.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean checkPermission = permissions.stream()
                            .anyMatch(p -> p.getApiPath().equals(path) && p.getMethod().equals(httpMethod));

                    if(checkPermission==false){
                        throw new IdInvalidException("You don't have permission to access");
                    }
                }
                else{
                    throw new IdInvalidException("You don't have permission to access");
                }
            }
        }

        return true;
    }
}
