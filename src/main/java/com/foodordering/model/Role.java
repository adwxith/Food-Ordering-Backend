package com.foodordering.model;

import com.foodordering.util.AppConstants;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "roles", 
    uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true)
    private AppConstants.RoleName name;

    public Role() {}

    public Role(AppConstants.RoleName name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppConstants.RoleName getName() {
        return name;
    }

    public void setName(AppConstants.RoleName name) {
        this.name = name;
    }
}