package com.guo.guoaiagentbackend.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private static final RowMapper<AppUser> ROW_MAPPER = (rs, rowNum) ->
            new AppUser(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"));

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AppUser> findByUsername(String username) {
        var list = jdbcTemplate.query(
                "SELECT id, username, password_hash FROM app_user WHERE username = ?",
                ROW_MAPPER,
                username);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*)::int FROM app_user WHERE username = ?",
                Integer.class,
                username);
        return count != null && count > 0;
    }

    public long insert(String username, String passwordHash) {
        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO app_user (username, password_hash)
                        VALUES (?, ?)
                        RETURNING id
                        """,
                Long.class,
                username,
                passwordHash);
    }
}
