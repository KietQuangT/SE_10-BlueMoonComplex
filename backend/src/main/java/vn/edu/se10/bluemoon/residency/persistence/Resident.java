package vn.edu.se10.bluemoon.residency.persistence;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name="residents")
public class Resident {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="household_id",nullable=false) private Long householdId;
 @Column(name="full_name",nullable=false,length=150) private String fullName;
 @Column(name="identity_number",length=50) private String identityNumber;
 @Column(name="date_of_birth") private LocalDate dateOfBirth;
 @Column(length=30) private String phone;
 @Column(name="relationship_to_head",length=100) private String relationshipToHead;
 @Enumerated(EnumType.STRING) @Column(name="residency_status",nullable=false,length=30) private ResidencyStatus residencyStatus;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 protected Resident() {}
 public Resident(long householdId,String fullName,String identityNumber,LocalDate dateOfBirth,String phone,String relationship,Instant now){this.householdId=householdId;this.fullName=fullName;this.identityNumber=identityNumber;this.dateOfBirth=dateOfBirth;this.phone=phone;this.relationshipToHead=relationship;this.residencyStatus=ResidencyStatus.RESIDING;this.createdAt=now;this.updatedAt=now;}
 public void update(String name,String identity,LocalDate birth,String phone,String relationship,Instant now){if(name!=null)fullName=name;if(identity!=null)identityNumber=identity;if(birth!=null)dateOfBirth=birth;if(phone!=null)this.phone=phone;if(relationship!=null)relationshipToHead=relationship;updatedAt=now;}
 public void setResidencyStatus(ResidencyStatus status,Instant now){residencyStatus=status;updatedAt=now;}
 public void moveOut(Instant now){residencyStatus=ResidencyStatus.MOVED_OUT;updatedAt=now;}
 public Long getId(){return id;} public Long getHouseholdId(){return householdId;} public String getFullName(){return fullName;} public String getIdentityNumber(){return identityNumber;} public LocalDate getDateOfBirth(){return dateOfBirth;} public String getPhone(){return phone;} public String getRelationshipToHead(){return relationshipToHead;} public ResidencyStatus getResidencyStatus(){return residencyStatus;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
