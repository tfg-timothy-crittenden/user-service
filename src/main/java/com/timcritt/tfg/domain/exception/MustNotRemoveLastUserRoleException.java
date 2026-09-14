package com.timcritt.tfg.domain.exception;

public class MustNotRemoveLastUserRoleException extends RuntimeException {

    public MustNotRemoveLastUserRoleException() {
        super("A user must have at least one role");
    }
}