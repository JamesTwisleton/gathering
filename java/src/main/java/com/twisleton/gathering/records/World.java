package com.twisleton.gathering.records;

import java.util.HashMap;

public record World(String id, HashMap<String,User> users) {};
