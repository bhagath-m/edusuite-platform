package com.edusuite.platform.tenant;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * The Tenant table itself lives OUTSIDE row-level security — it's the table that defines what a
 * tenant even is, so it cannot be scoped by tenant_id. Access to this entity should be
 * restricted to platform-admin-only endpoints (see SecurityConfig), never exposed to normal
 * tenant-scoped user roles.
 */
@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String subdomain;

    @Column(nullable = false)
    private String instituteType; // SCHOOL | COLLEGE | COACHING

    @Column(nullable = false)
    private String planTier; // STARTER | GROWTH | SCALE

    @Column(nullable = false)
    private String billingStatus; // TRIAL | ACTIVE | PAST_DUE | SUSPENDED

    @Column(nullable = false)
    private String locale = "en-IN";

    @Column(nullable = false)
    private String currency = "INR";

    @Column(nullable = false)
    private String timezone = "Asia/Kolkata";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Tenant() {
        // JPA
    }

    public Tenant(String name, String subdomain, String instituteType) {
        this.name = name;
        this.subdomain = subdomain;
        this.instituteType = instituteType;
        this.planTier = "STARTER";
        this.billingStatus = "TRIAL";
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public String getInstituteType() {
        return instituteType;
    }

    public String getPlanTier() {
        return planTier;
    }

    public void setPlanTier(String planTier) {
        this.planTier = planTier;
    }

    public String getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(String billingStatus) {
        this.billingStatus = billingStatus;
    }

    public String getLocale() {
        return locale;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTimezone() {
        return timezone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
