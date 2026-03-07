package com.learning.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tutors")
@Getter
@Setter
public class Tutor {

    @Id
    private Long id;

    @Column(length = 1000)
    private String intro;

    @Column(length = 500)
    private String certificate;

    @Column(name = "video_url_1", length = 500)
    private String videoUrl1;

    @Column(name = "video_url_2", length = 500)
    private String videoUrl2;

    @Column(name = "bank_code", length = 10)
    private String bankCode;

    @Column(name = "bank_account", length = 20)
    private String bankAccount;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;
}