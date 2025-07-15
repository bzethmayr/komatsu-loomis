package com.bzethmayr.komatsu.test.core.models;

public class NamePojo {
    private final String firstName;
    private final String lastName;

    public NamePojo(final String firstName, final String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }
}
