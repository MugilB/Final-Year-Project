package com.securevoting.model;

/**
 * Defines the roles a user can have within the system.
 */
public enum UserRole {
    /**
     * Administrator with full access to the system.
     */
    ADMIN,

    /**
     * A standard user with voting rights.
     */
    USER,

    /**
     * A special account for system-level operations, like creating the genesis block.
     */
    SYSTEM
}