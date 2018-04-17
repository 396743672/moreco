package xyz.taroco.oauth2.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private UserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http//
        .exceptionHandling()//
        .accessDeniedPage("/login.html?authorization_error=true")//
        .and()//
        .logout()//
        .permitAll()//
        .and()//
        .formLogin()//
        .loginPage("/login.html")//
        .permitAll()//
        .and()//
        .authorizeRequests()//
        .anyRequest().authenticated();//
  }

  @Override
  public void configure(WebSecurity security) {
    security.ignoring().antMatchers("/resources/**");
  }

  @Bean
  public AuditorAware<String> auditorAwareBean() {
    return () -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null
          || new AuthenticationTrustResolverImpl().isAnonymous(authentication)) {
        return "@SYSTEM";
      }

      Object principal = authentication.getPrincipal();
      if (principal instanceof String) {
        return (String) principal;
      } else if (principal instanceof UserDetails) {
        return ((UserDetails) principal).getUsername();
      } else {
        return String.valueOf(principal);
      }
    };
  }


  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
  protected static class GlobalSecurityConfiguration extends GlobalMethodSecurityConfiguration {


    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
      return new OAuth2MethodSecurityExpressionHandler();
    }

  }
}
