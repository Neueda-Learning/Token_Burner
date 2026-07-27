package com.example.paymentprocessing.entity;

/**
 * TODO Kylian:
 * Define the payment entity structure for the payment record.
 * Add fields, JPA mapping, and persistence-related annotations later.
 *
 * Future note:
 * - Payment creation may require payment password verification before persistence.
 *
 * Security rule:
 * - The Payment entity must never store a plain payment password.
 * - The Payment entity must never store a payment password hash.
 * - Password verification belongs to request handling and service-layer logic, not to the payment record itself.
 */
public class Payment {
    // TODO Define payment entity fields and mappings without storing payment password data.
}

