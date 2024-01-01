package es.uca.cadicom.service;

import es.uca.cadicom.entity.Usuario;
import es.uca.cadicom.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean createUser(Usuario usuario) {
        if (usuario == null) {
            System.err.println("Usuario es null");
            return false;
        }

        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            System.err.println("Email is null or empty");
            return false;
        }

        Usuario existingUser = usuarioRepository.findByEmail(usuario.getEmail());

        if (existingUser != null) {
            System.err.println("A user with the same email already exists");
            return false;
        }

        try {
            String encryptedPassword = passwordEncoder.encode(usuario.getPassword());
            usuario.setPassword(encryptedPassword);
            usuarioRepository.save(usuario);
            return true;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    public boolean validateUserCredentials(Usuario usuario) {
        if (usuario == null || usuario.getEmail() == null || usuario.getPassword() == null) {
            System.err.println("Usuario or credentials are null");
            return false;
        }
        try {
            // Find the user by email
            Usuario existingUser = usuarioRepository.findByEmail(usuario.getEmail());
            if (existingUser == null) {
                System.err.println("User not found");
                return false;
            }
            return passwordEncoder.matches(usuario.getPassword(), existingUser.getPassword());
        } catch (Exception e) {
            System.err.println("Error validating user: " + e.getMessage());
            return false;
        }
    }

    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        CustomAuthenticationSuccessHandler handler = new CustomAuthenticationSuccessHandler();
        handler.setUseReferer(false);
        handler.setDefaultTargetUrl("/panel");

        return handler;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return new User(usuario.getEmail(), usuario.getPassword(), Collections.singletonList(authority));
    }
}