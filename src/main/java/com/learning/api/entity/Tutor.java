package com.learning.api.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tutors")
@Getter
@Setter
public class Tutor {

    @Id
    private Long id; // 與 users.id 共享（@MapsId）

    @OneToOne
    @MapsId // 讓此 ID 同時作為外鍵指向 User 的 ID
    @JoinColumn(name = "id")
    private User user;

    @Column(length = 50)
    private String title;  // 吸睛標題，如「TESL認證英語教師」

    @Column(length = 500)
    private String avatar;

    @Column(length = 1000)
    private String intro; // 自我介紹

    @Column(name ="certificate_1" ,length = 500)
    private String certificate1;//位址

    @Column(name ="certificate_name_1" ,length = 500)
    private String certificateName1;//證照名稱

    @Column(name ="certificate_2" ,length = 500)
    private String certificate2;//位址
    
    @Column(name ="certificate_name_2" ,length = 500)
    private String certificateName2;//證照名稱

    @Column(name = "video_url_1", length = 500)
    private String videoUrl1; // 自我介紹影片

    @Column(name = "video_url_2", length = 500)
    private String videoUrl2; // 教學示範影片

    @Column(name = "bank_code", length = 10)
    private String bankCode;

    @Column(name = "bank_account", length = 20)
    private String bankAccount;
    
    //為了讓 CourseSpec 能順利從課程連動到課表
    @OneToMany(mappedBy = "tutor")
    private List<TutorSchedule> schedules;
    
}
