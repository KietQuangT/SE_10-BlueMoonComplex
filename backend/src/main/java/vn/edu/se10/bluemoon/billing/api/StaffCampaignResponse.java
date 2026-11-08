package vn.edu.se10.bluemoon.billing.api;

import java.time.Instant;
import java.time.LocalDate;

public record StaffCampaignResponse(long id, String name, String description, LocalDate opensOn, LocalDate closesOn,
                                    String status, long version, long createdBy, Instant createdAt,
                                    Long publishedBy, Instant publishedAt, Long closedBy, Instant closedAt,
                                    long postedCount, String totalCollected) { }
