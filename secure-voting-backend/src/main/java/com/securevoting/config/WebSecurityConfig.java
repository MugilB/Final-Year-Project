package com.securevoting.config;

import com.securevoting.security.jwt.AuthEntryPointJwt;
import com.securevoting.security.jwt.AuthTokenFilter;
import com.securevoting.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/elections").permitAll()
                .antMatchers("/api/elections/open").permitAll()
                .antMatchers("/api/elections/for-nominations").permitAll()
                .antMatchers("/api/elections/with-candidates").permitAll()
                .antMatchers("/api/elections/with-approved-candidates").permitAll()
                .antMatchers("/api/elections/*/with-candidates").permitAll()
                .antMatchers("/api/elections/*/with-approved-candidates").permitAll()
                .antMatchers("/api/elections/test").permitAll()
                .antMatchers("/api/candidates").permitAll()
                .antMatchers("/api/candidates/approved").permitAll()
                .antMatchers("/api/candidates/election/*").permitAll()
                .antMatchers("/api/candidates/election/*/approved").permitAll()
                .antMatchers("/api/candidates/election/*/vote-counts").permitAll()
                .antMatchers("/api/candidates/*").permitAll()
                .antMatchers("/api/blocks").permitAll()
                .antMatchers("/api/blocks/*").permitAll()
                .antMatchers("/api/blocks/*/decrypt-vote").permitAll()
                .antMatchers("/api/blocks/*/analyze-key").permitAll()
                .antMatchers("/api/blocks/test-encryption").permitAll()
                .antMatchers("/api/votes/**").permitAll()
                .antMatchers("/api/candidate-nominations/**").permitAll()
                .antMatchers("/api/voter-registration/**").permitAll()
                .antMatchers("/api/voters/register").permitAll()
                .antMatchers("/api/voters/test-curl").permitAll()
                .antMatchers("/api/wards/**").permitAll()
                .antMatchers("/api/voters/all").hasRole("ADMIN")
                .antMatchers("/api/voters/pending").hasRole("ADMIN")
                .antMatchers("/api/voters/approved").hasRole("ADMIN")
                .antMatchers("/api/voters/*/status").hasRole("ADMIN")
                .antMatchers("/api/debug/**").permitAll()
                .antMatchers("/api/elections/*").authenticated()
                .antMatchers("/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated();

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}