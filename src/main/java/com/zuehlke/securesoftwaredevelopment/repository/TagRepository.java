package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TagRepository {

    private final JdbcTemplate jdbc;

    public TagRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Tag> getAll() {
        return jdbc.query("SELECT id, name FROM tags ORDER BY id",
                (rs, rowNum) -> new Tag(rs.getInt("id"), rs.getString("name")));
    }
}
