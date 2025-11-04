package model;

import java.io.Serializable;

/**
 * Tracks the lifecycle of a student's withdrawal request for an application.
 */
public enum WithdrawalStatus implements Serializable {
    NONE,
    REQUESTED,
    APPROVED,
    REJECTED
}
