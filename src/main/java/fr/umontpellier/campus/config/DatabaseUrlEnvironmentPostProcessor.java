package fr.umontpellier.campus.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    Map<String, Object> props = new HashMap<>();

    String explicitUrl = environment.getProperty("spring.datasource.url");
    if (StringUtils.hasText(explicitUrl)) {
      return;
    }

    String jdbcUrl = environment.getProperty("JDBC_DATABASE_URL");
    if (StringUtils.hasText(jdbcUrl)) {
      props.put("spring.datasource.url", jdbcUrl);
      props.putIfAbsent("spring.datasource.driver-class-name", driverFor(jdbcUrl));
      applyCredentials(environment, props);
      applyDefaults(environment, props);
      return;
    }

    String databaseUrl = environment.getProperty("DATABASE_URL");
    if (StringUtils.hasText(databaseUrl)) {
      applyDatabaseUrl(environment, databaseUrl, props);
      applyCredentials(environment, props);
      applyDefaults(environment, props);
      return;
    }

    // Fallback: file-based H2 so it still works if no DB is configured.
    props.put("spring.datasource.url",
        "jdbc:h2:file:./data/campus;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    props.put("spring.datasource.driver-class-name", "org.h2.Driver");
    props.putIfAbsent("spring.datasource.username", "sa");
    props.putIfAbsent("spring.datasource.password", "");
    environment.getPropertySources().addFirst(new MapPropertySource("autoDatabase", props));
  }

  private void applyDatabaseUrl(ConfigurableEnvironment environment, String databaseUrl, Map<String, Object> props) {
    URI uri;
    try {
      uri = new URI(databaseUrl);
    } catch (URISyntaxException ex) {
      return;
    }
    String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
    if (!scheme.startsWith("postgres")) {
      return;
    }

    String userInfo = uri.getUserInfo();
    if (StringUtils.hasText(userInfo)) {
      String[] parts = userInfo.split(":", 2);
      if (parts.length > 0 && StringUtils.hasText(parts[0])) {
        props.putIfAbsent("spring.datasource.username", parts[0]);
      }
      if (parts.length > 1 && StringUtils.hasText(parts[1])) {
        props.putIfAbsent("spring.datasource.password", parts[1]);
      }
    }

    String host = uri.getHost();
    int port = uri.getPort();
    String path = uri.getPath() == null ? "" : uri.getPath();
    String query = uri.getQuery();
    StringBuilder jdbc = new StringBuilder("jdbc:postgresql://");
    jdbc.append(host);
    if (port > 0) {
      jdbc.append(":").append(port);
    }
    jdbc.append(path);
    if (StringUtils.hasText(query)) {
      jdbc.append("?").append(query);
    }
    props.put("spring.datasource.url", jdbc.toString());
    props.putIfAbsent("spring.datasource.driver-class-name", "org.postgresql.Driver");
  }

  private void applyDefaults(ConfigurableEnvironment environment, Map<String, Object> props) {
    if (!props.isEmpty()) {
      environment.getPropertySources().addFirst(new MapPropertySource("autoDatabase", props));
    }
  }

  private void applyCredentials(ConfigurableEnvironment environment, Map<String, Object> props) {
    String user = environment.getProperty("DB_USER");
    String pass = environment.getProperty("DB_PASS");
    if (!StringUtils.hasText(user)) {
      user = environment.getProperty("POSTGRES_USER");
    }
    if (!StringUtils.hasText(pass)) {
      pass = environment.getProperty("POSTGRES_PASSWORD");
    }
    if (StringUtils.hasText(user)) {
      props.putIfAbsent("spring.datasource.username", user);
    }
    if (pass != null) {
      props.putIfAbsent("spring.datasource.password", pass);
    }
  }

  private String driverFor(String jdbcUrl) {
    String lower = jdbcUrl.toLowerCase();
    if (lower.startsWith("jdbc:postgresql:")) {
      return "org.postgresql.Driver";
    }
    if (lower.startsWith("jdbc:h2:")) {
      return "org.h2.Driver";
    }
    if (lower.startsWith("jdbc:mariadb:") || lower.startsWith("jdbc:mysql:")) {
      return "org.mariadb.jdbc.Driver";
    }
    return "";
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
