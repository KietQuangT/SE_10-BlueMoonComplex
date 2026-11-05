package vn.edu.se10.bluemoon.identity.application;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.se10.bluemoon.identity.persistence.UserRepository;

@Service
public class AccountUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public AccountUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Account không tồn tại"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().getCode())
                .disabled(!"ACTIVE".equals(user.getStatus()))
                .build();
    }
}
