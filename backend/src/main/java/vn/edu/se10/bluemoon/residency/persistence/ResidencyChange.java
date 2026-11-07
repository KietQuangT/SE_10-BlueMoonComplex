package vn.edu.se10.bluemoon.residency.persistence;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name="residency_changes")
public class ResidencyChange {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="resident_id",nullable=false) private Long residentId;
 @Column(name="household_id",nullable=false) private Long householdId;
 @Column(name="change_type",nullable=false,length=30) private String changeType;
 @Column(name="effective_date",nullable=false) private LocalDate effectiveDate;
 @Column(length=255) private String reason;
 @Column(nullable=false,length=20) private String status;
 @Column(name="recorded_by",nullable=false) private Long recordedBy;
 @Column(name="created_at",nullable=false) private Instant createdAt;
 @Column(name="approved_by") private Long approvedBy;
 @Column(name="decided_at") private Instant decidedAt;
 @Column(name="decision_note",length=255) private String decisionNote;
 @Column(name="end_date") private LocalDate endDate;
 protected ResidencyChange() {}
 public ResidencyChange(Resident resident,LocalDate effectiveDate,String reason,long actor,Instant now){this.residentId=resident.getId();this.householdId=resident.getHouseholdId();this.changeType="MOVE_OUT";this.effectiveDate=effectiveDate;this.reason=reason;this.status="APPROVED";this.recordedBy=actor;this.createdAt=now;}
 public static ResidencyChange absence(Resident r,LocalDate date,LocalDate end,String reason,long actor,Instant now){ResidencyChange c=new ResidencyChange(r,date,reason,actor,now);c.changeType="TEMPORARY_ABSENCE";c.status="PENDING";c.endDate=end;return c;}
 public static ResidencyChange returned(Resident r,LocalDate date,long actor,Instant now){ResidencyChange c=new ResidencyChange(r,date,null,actor,now);c.changeType="RETURN";c.approvedBy=actor;c.decidedAt=now;return c;}
 public void decide(String status,Long actor,String note,Instant now){this.status=status;this.approvedBy=actor;this.decisionNote=note;this.decidedAt=now;}
 public Long getHouseholdId(){return householdId;} public String getReason(){return reason;} public LocalDate getEndDate(){return endDate;} public String getDecisionNote(){return decisionNote;} public Instant getDecidedAt(){return decidedAt;}
 public Long getId(){return id;} public Long getResidentId(){return residentId;} public String getChangeType(){return changeType;} public LocalDate getEffectiveDate(){return effectiveDate;} public String getStatus(){return status;} public Long getRecordedBy(){return recordedBy;} public Instant getCreatedAt(){return createdAt;}
}
