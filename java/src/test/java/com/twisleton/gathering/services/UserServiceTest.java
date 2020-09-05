package com.twisleton.gathering.services;

import com.twisleton.gathering.persistence.UserPersistence;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;

class UserServiceTest {

    @Test
    public void givenUsersFileExistsOnDisk_whenLoadingUsers_UsersFromDiskAreLoaded() {
        var users = UserPersistence.loadUsers(Paths.get("src/test/resources/users.json"));
        assertThat(users.isEmpty(), is(false));
    }

    @Test
    public void givenUsersFileDoesntExist_whenLoadingUsers_WorldHasEmptyUsers() {
        var users = UserPersistence.loadUsers(Paths.get("no/file/here.json"));
        assertThat(Objects.isNull(users), is(false));
        assertThat(users.isEmpty(), is(true));
    }

}
