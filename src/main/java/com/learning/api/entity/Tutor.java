package com.learning.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tutors")
@Getter
@Setter
public class Tutor {
    @Id
    private Long id;

    @Column(name = "title", length = 50)
    private String title;

    @Column(length = 1000)
    private String intro;

    @Column(name = "certificate_1", length = 500)
    private String certificate1;

    @Column(name = "certificate_name_1", length = 500)
    private String certificateName1;

    @Column(name = "certificate_2", length = 500)
    private String certificate2;

    @Column(name = "certificate_name_2", length = 500)
    private String certificateName2;

    @Column(name = "video_url_1", length = 500)
    private String videoUrl1;

    @Column(name = "video_url_2", length = 500)
    private String videoUrl2;

    @Column(name = "bank_code", length = 10)
    private String bankCode;

    @Column(name = "bank_account", length = 20)
    private String bankAccount;
}