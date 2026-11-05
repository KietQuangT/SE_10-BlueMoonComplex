package vn.edu.se10.bluemoon.identity.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Map bảng roles (V2). */
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, updatable = false)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    protected Role() {
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank() || newName.length() > 100) {
            throw new IllegalArgumentException("Tên vai trò phải có 1 đến 100 ký tự");
        }
        this.name = newName;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
