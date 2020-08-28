package com.twisleton.gathering.services;

import com.twisleton.gathering.persistence.UserPersistence;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;

class UserServiceTest {

    @Test
    public void loadUsers() {
        var users = UserPersistence.loadUsers(Paths.get("src/test/resources/users.json"));
        assertThat(users.values().isEmpty(), is(false));
        assertThat(users.containsKey("0:0:0:0:0:0:0:1"), is(true));
    }

    @Test
    public void saveUsers() {

    }
}
