package com.fitness.nosql_lab1.configs;

import com.fitness.nosql_lab1.objects.Role;
import com.fitness.nosql_lab1.objects.user.User;
import com.fitness.nosql_lab1.db.repos.UserDao;
import org.jspecify.annotations.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SessionFilter extends OncePerRequestFilter {
    private final UserDao userDao;

    public SessionFilter(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String sessionToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("SESSION".equals(cookie.getName())) {
                    sessionToken = cookie.getValue();
                    break;
                }
            }
        }

        if (sessionToken != null && !sessionToken.isEmpty()) {
            try {
                User user = userDao.findBySession(sessionToken);
                if (!(user == null)) {
                    Role role = user.getRole();
                    String impersonateHeader = request.getHeader("X-Impersonate-Role");
                    if (user.getRole() == Role.MODER && "USER".equalsIgnoreCase(impersonateHeader)) {
                        role = Role.USER;
                    }
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, Collections.singletonList(new SimpleGrantedAuthority(role.getSecurityAuthority())));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
            }
        }

        filterChain.doFilter(request, response);
    }
}