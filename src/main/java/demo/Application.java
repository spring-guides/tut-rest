package demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.*;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

//
// curl -X POST -vu android-bookmarks:123456 http://localhost:8080/oauth/token -H "Accept: application/json" -d "password=password&username=jlong&grant_type=password&scope=write&client_secret=123456&client_id=android-bookmarks"
// curl -v POST http://127.0.0.1:8080/tags --data "tags=cows,dogs"  -H "Authorization: Bearer 66953496-fc5b-44d0-9210-b0521863ffcb"

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Profile("ssl")
    @Bean
    EmbeddedServletContainerCustomizer containerCustomizer(@Value("${keystore.file}") Resource keystoreFile,
                                                           @Value("${keystore.pass}") String keystorePass)
            throws Exception {

        String absoluteKeystoreFile = keystoreFile.getFile().getAbsolutePath();
        return (ConfigurableEmbeddedServletContainer container) -> {
            if (container instanceof TomcatEmbeddedServletContainerFactory) {
                TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
                tomcat.addConnectorCustomizers(
                        (connector) -> {
                            connector.setPort(8443);
                            connector.setSecure(true);
                            connector.setScheme("https");
                            Http11NioProtocol proto = (Http11NioProtocol) connector.getProtocolHandler();
                            proto.setSSLEnabled(true);
                            proto.setKeystoreFile(absoluteKeystoreFile);
                            proto.setKeystorePass(keystorePass);
                            proto.setKeystoreType("PKCS12");
                            proto.setKeyAlias("tomcat");
                        }
                );
            }
        };
    }

    @Bean
    CommandLineRunner init(AccountRepository accountRepository, BookmarkRepository bookmarkRepository) {
        return (args) -> {
            List<String> names = Arrays.asList("jhoeller", "dsyer", "pwebb", "jlong");

            names.stream()
                    .map(n -> new Account(n, "password"))
                    .map(accountRepository::save)
                    .collect(Collectors.toList());

            names.stream()
                    .forEach(a -> {
                        Account account = accountRepository.findByUsername(a);
                        Bookmark bookmark = new Bookmark(account, "http://bookmark.com/" + a, "A description");
                        bookmarkRepository.save(bookmark);
                    });


        };
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {

        UserDetailsService userDetailsService = (username) -> {
            Account a = accountRepository.findByUsername(username);
            if (null != a) {
                return new User(a.username, a.password, true, true, true, true, AuthorityUtils.createAuthorityList("USER", "write"));
            } else {
                throw new UsernameNotFoundException("couldn't find the user " + username);
            }
        };
        auth.userDetailsService(userDetailsService);

    }
}


@Configuration
@EnableResourceServer
@EnableAuthorizationServer
class OAuth2Configuration extends AuthorizationServerConfigurerAdapter {

    String applicationName = "bookmarks";

    // This is required for password grants, which we specify below as one of the  {@literal authorizedGrantTypes()}.
    @Autowired
    AuthenticationManager authenticationManager;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("android-" + applicationName)
                .authorizedGrantTypes("password", "authorization_code", "refresh_token")
                .authorities("ROLE_USER")
                .scopes("write")
                .resourceIds(applicationName)
                .secret("123456");
    }
}

@RestController
@RequestMapping("/bookmarks")
class BookmarkRestController {

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(Principal principal,
                          @RequestBody Bookmark input) {

        Account account = accountRepository.findByUsername(principal.getName());
        Bookmark result = bookmarkRepository.save(new Bookmark(account, input.uri, input.description));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(result.id)
                .toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Bookmark> read(Principal principal) {
        return bookmarkRepository.findByAccountUsername(principal.getName());
    }

    @Autowired
    BookmarkRepository bookmarkRepository;

    @Autowired
    AccountRepository accountRepository;

}

interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Collection<Bookmark> findByAccountUsername(String username);
}

interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUsername(String n);
}

@Entity
class Account {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "account")
    public Set<Bookmark> bookmarks = new HashSet<>();

    @Id
    @GeneratedValue
    public Long id;

    @JsonIgnore
    public String password;
    public String username;

    Account(String name, String password) {
        this.username = name;
        this.password = password;
    }

    Account() { // jpa only
    }
}

@Entity
class Bookmark {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    public Account account;

    @Id
    @GeneratedValue
    public Long id;

    Bookmark() { // jpa only
    }

    Bookmark(Account account, String uri, String description) {
        this.uri = uri;
        this.description = description;
        this.account = account;
    }

    public String uri;
    public String description;
}