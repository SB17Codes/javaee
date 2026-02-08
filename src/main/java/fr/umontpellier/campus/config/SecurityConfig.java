package fr.umontpellier.campus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/style.css").permitAll()
            .requestMatchers(HttpMethod.GET,
                "/",
                "/data",
                "/analytics",
                "/campus",
                "/batiments",
                "/salles",
                "/composantes",
                "/exploitations",
                "/queries",
                "/distance",
                "/map",
                "/itinerary",
                "/api/batiments/geojson",
                "/api/osm-buildings/geojson",
                "/api/campus-boundaries/geojson")
            .hasAnyRole("ADMIN", "MANAGER", "TEACHER", "STUDENT")
            .requestMatchers(HttpMethod.POST,
                "/campus/delete",
                "/batiments/delete",
                "/salles/delete",
                "/composantes/delete",
                "/exploitations/delete")
            .hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST,
                "/campus/create",
                "/campus/update",
                "/batiments/create",
                "/batiments/update",
                "/salles/create",
                "/salles/update",
                "/composantes/create",
                "/composantes/update",
                "/exploitations/create",
                "/exploitations/update")
            .hasAnyRole("ADMIN", "MANAGER")
            .requestMatchers(HttpMethod.POST, "/queries/**", "/distance/**")
            .hasAnyRole("ADMIN", "MANAGER", "TEACHER", "STUDENT")
            .requestMatchers(HttpMethod.POST, "/itinerary/upload", "/itinerary/plan")
            .hasAnyRole("ADMIN", "MANAGER", "TEACHER", "STUDENT")
            .anyRequest().authenticated())
        .formLogin(Customizer.withDefaults())
        .logout(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public UserDetailsService users() {
    return new InMemoryUserDetailsManager(
        User.withUsername("admin").password("{noop}admin").roles("ADMIN").build(),
        User.withUsername("manager").password("{noop}manager").roles("MANAGER").build(),
        User.withUsername("teacher").password("{noop}teacher").roles("TEACHER").build(),
        User.withUsername("student").password("{noop}student").roles("STUDENT").build()
    );
  }
}
